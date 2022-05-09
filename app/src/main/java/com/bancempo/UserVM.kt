package com.bancempo

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class UserVM(private val app: Application): AndroidViewModel(app) {

    val fullname = MutableLiveData<String>()
    val nickname = MutableLiveData<String>()
    val description = MutableLiveData<String>()
    val location = MutableLiveData<String>()
    val email = MutableLiveData<String>()

    val profilePictureBitmap = MutableLiveData<Bitmap>()
    private val profilePictureFileName = "profile.jpeg"

    val skills = MutableLiveData<String>()

    init {
        val sharedPref = app.getSharedPreferences("profile.bancempo.lab3", Context.MODE_PRIVATE)
        val jsonString = sharedPref.getString("profileJSON", "{}") ?: "{}"
        val json = JSONObject(jsonString)


        json.apply {
            fullname.value = optString("fullname", "")
            nickname.value = optString("nickname", "")
            description.value = optString("description", "")
            location.value = optString("location", "")
            email.value = optString("email", "")
            skills.value = optString("skills", "")
        }
        loadProfilePicture()
    }

    fun updateFromEditProfile(view: View){
        fullname.value = view.findViewById<TextView>(R.id.editTextFullName).text.toString()
        nickname.value = view.findViewById<TextView>(R.id.editTextNickname).text.toString()
        description.value = view.findViewById<TextView>(R.id.editTextDescription).text.toString()
        location.value = view.findViewById<TextView>(R.id.editTextLocation).text.toString()
        email.value = view.findViewById<TextView>(R.id.editTextEmail).text.toString()

        val chips = view.findViewById<ChipGroup>(R.id.chipGroup)
        var chipText = ""
        for (i in 0 until chips.childCount) {
            val chip = chips.getChildAt(i) as Chip
            chipText += "${chip.text},"
        }

        skills.value = chipText
        save(chipText)
    }

    private fun save(skillsText: String) {
        JSONObject()
            .apply {
                put("fullname", fullname.value)
                put("nickname", nickname.value)
                put("description", description.value)
                put("location", location.value)
                put("email", email.value)
                put("skills", skillsText)
            }.toString()
            .run {
                app
                    .getSharedPreferences("profile.bancempo.lab3", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .putString("profileJSON", this)
                    .apply()
            }

    }

    fun storeProfilePicture(bmp : Bitmap) {
        saveToInternalStorage(bmp)
        loadProfilePicture()
    }



    private fun loadProfilePicture() : Bitmap {
        val fileDir = "/data/user/0/com.bancempo/app_imageDir"
        return File(fileDir, profilePictureFileName)
            .run {
                when (exists()) {
                    true -> BitmapFactory.decodeFile(File(fileDir, profilePictureFileName).absolutePath)
                    false -> BitmapFactory.decodeResource(app.resources, R.drawable.profile_pic_default)
                }
            }.also {
                profilePictureBitmap.value = it
            }

    }


    fun updateProfilePictureFromURI(uri : Uri) {
        val bmp:Bitmap = MediaStore.Images.Media.getBitmap(app.contentResolver, uri)
        val ins: InputStream? = app.applicationContext.contentResolver.openInputStream(uri)


        val ei = ExifInterface(ins!!)
        val rotatedBitmap: Bitmap = when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bmp, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bmp, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bmp, 270f)
            ExifInterface.ORIENTATION_NORMAL -> bmp
            else -> bmp
        }

        ins.close()
        saveToInternalStorage(rotatedBitmap)
        loadProfilePicture()
    }


    private fun saveToInternalStorage(bitmapImage: Bitmap) {
        val cw = ContextWrapper(app.applicationContext)
        // path to /data/data/yourapp/app_data/imageDir
        val directory: File = cw.getDir("imageDir", AppCompatActivity.MODE_PRIVATE)
        // Create imageDir
        val mypath = File(directory, "profile.jpeg")
        val fos: FileOutputStream?
        try {
            fos = FileOutputStream(mypath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)

        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }



}
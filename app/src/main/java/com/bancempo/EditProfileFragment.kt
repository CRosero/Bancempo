package com.bancempo

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    val userVM: UserVM by activityViewModels()
    private lateinit var photo: ImageView
    private lateinit var editPicture: ImageButton

    private lateinit var fullName: TextInputLayout
    private lateinit var fullName_ed: TextInputEditText
    private lateinit var description_ed: TextInputEditText
    private lateinit var nickname_ed: TextInputEditText
    private lateinit var email_ed: TextInputEditText
    private lateinit var location_ed: TextInputEditText
    private lateinit var skills_ed: TextInputEditText

    private lateinit var nickname: TextInputLayout
    private lateinit var email: TextInputLayout
    private lateinit var location: TextInputLayout
    private lateinit var skills: TextInputLayout
    private lateinit var description: TextInputLayout

    private lateinit var chipGroup: ChipGroup

    private val REQUEST_IMAGE_CAPTURE = 1
    private val SELECT_PICTURE = 200


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photo = view.findViewById(R.id.profile_pic)
        editPicture = view.findViewById(R.id.changeImageButton)
        fullName = view.findViewById(R.id.full_name)
        fullName_ed = view.findViewById(R.id.editTextFullName)
        nickname = view.findViewById(R.id.nick_name)
        nickname_ed = view.findViewById(R.id.editTextNickname)
        email = view.findViewById(R.id.email)
        email_ed = view.findViewById(R.id.editTextEmail)
        location = view.findViewById(R.id.location)
        location_ed = view.findViewById(R.id.editTextLocation)
        description = view.findViewById(R.id.description)
        description_ed = view.findViewById(R.id.editTextDescription)
        skills =view.findViewById(R.id.skills)
        skills_ed =view.findViewById(R.id.editTextSkills)
        chipGroup = view.findViewById(R.id.chipGroup)


        skills.setEndIconOnClickListener {
            if (skills_ed.text.toString().isNotEmpty()) {
                addChip(skills_ed.text.toString())
                skills_ed.setText("")
            }

        }

        //get value from showprofile
        photo.setImageBitmap(userVM.profilePictureBitmap.value)
        fullName_ed.setText(arguments?.getString("fullname"))
        nickname_ed.setText(arguments?.getString("nickname"))
        email_ed.setText(arguments?.getString("email"))
        location_ed.setText(arguments?.getString("location"))
        description_ed.setText(arguments?.getString("description"))
        var skills_string : String? = arguments?.getString("skills")


        if (skills_string != null) {
            chipGroup.removeAllViews()
            skills_string.split(",")?.forEach {
                val chip = Chip(activity)
                if (it.isNotEmpty()) {
                    chip.text = it
                    chip.isCloseIconVisible = true

                    chip.setOnCloseIconClickListener {
                        chipGroup.removeView(chip)
                    }
                    chipGroup.addView(chip)
                }
            }
        }

        if (savedInstanceState != null) {
            val skillsString = savedInstanceState.getString("skills")
            if (skillsString != null) {
                chipGroup.removeAllViews()
                skillsString.split(",").forEach {
                    var chip = Chip(activity);
                    if(!it.isEmpty()) {
                        chip.setText(it);
                        chip.isCloseIconVisible = true;

                        chip.setOnCloseIconClickListener {
                            chipGroup.removeView(chip)
                        }
                        chipGroup.addView(chip);
                    }
                }
            }
        }

        //handling on-press small image button
        editPicture.setOnClickListener {
            showPopup(editPicture)
        }


        //handling on back pressed
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if(validation()) {
                        userVM.updateFromEditProfile(view)
                        setFragmentResult("backPressed", bundleOf())
                        Toast.makeText(context, R.string.prof_edit_succ, Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
            })

    }

    @SuppressLint("SdCardPath")
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var chipText = ""
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            chipText += "${chip.text},"
        }
        outState.putString("skills", chipText)
    }


    private fun validateTextInput(text: TextInputLayout, textEdit: TextInputEditText): Boolean {
        if (textEdit.text.isNullOrEmpty()) {
            text.error = "Please, fill in this field!"
            return false
        } else {
            if (text.hint == "Description") {
                return if (textEdit.text?.length!! > 200) {
                    text.error = "Your ${text.hint} is too long."
                    false
                } else {
                    text.error = null
                    true
                }
            } else if (text.hint == "Full name" || text.hint == "Location") {
                return if (textEdit.text?.length!! > 20) {
                    text.error = "Your ${text.hint} is too long."
                    false
                } else {
                    text.error = null
                    return true
                }
            } else if (text.hint == "Nickname") {
                return if (textEdit.text?.length!! > 10) {
                    text.error = "Your ${text.hint} is too long."
                    false
                } else {
                    text.error = null
                    true
                }
            }
            else if(text.hint == "Email" ){
                return if (textEdit.text?.length!! > 40) {
                    text.error = "Your ${text.hint} is too long."
                    false
                } else {
                    text.error = null
                    return true
                }
            }
            else return false

        }

    }

    private fun validation(): Boolean {
        var valid = true

        if (!validateTextInput(fullName, fullName_ed)) {
            valid = false
        }
        if (!validateTextInput(description, description_ed)) {
            valid = false
        }
        /*
        if (!validateTextInput(nickname, nickname_ed)) {
            valid = false
        }
        */
        if (!validateTextInput(email, email_ed)) {
            valid = false
        }
        if (!validateTextInput(location, location_ed)) {
            valid = false
        }
        return valid
    }


    private fun showPopup(v: View) {

        val popup = PopupMenu(context, v)
        popup.menuInflater.inflate(R.menu.menu_profile_picture, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.select_image_gallery -> {
                    val i = Intent(Intent.ACTION_GET_CONTENT)
                    i.type = "image/*"
                    // pass the constant to compare it with the returned requestCode
                    startActivityForResult(
                        Intent.createChooser(i, "Select Picture"),
                        SELECT_PICTURE
                    )

                }
                R.id.use_camera -> {
                    dispatchTakePictureIntent()

                }
            }
            false
        }

        popup.show()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
            Toast.makeText(context, R.string.camera_not_available, Toast.LENGTH_SHORT).show()
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val bitmapPhoto = data.extras?.get("data") as Bitmap
            userVM.storeProfilePicture(bitmapPhoto)
            photo.setImageBitmap(userVM.profilePictureBitmap.value)
        }

        else if (requestCode == SELECT_PICTURE && resultCode == AppCompatActivity.RESULT_OK && data != null){
            val uriPhoto = data.data
            userVM.updateProfilePictureFromURI(uriPhoto!!)
            photo.setImageBitmap(userVM.profilePictureBitmap.value)
        }
    }


    private fun addChip(text: String) {
        val chip = Chip(context)
        chip.text = text
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            chipGroup.removeView(chip)
        }
        chipGroup.addView(chip)
    }

}
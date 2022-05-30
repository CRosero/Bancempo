package com.bancempo.fragments

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.bancempo.R
import com.bancempo.SmallAdv
import com.bancempo.models.SharedViewModel
import com.google.android.material.textfield.TextInputEditText
import java.io.File


class TimeSlotRateFragment : Fragment(R.layout.fragment_time_slot_rate) {
    private val sharedVM: SharedViewModel by activityViewModels()


    private lateinit var timeSlot: SmallAdv

    private lateinit var ratingBar: RatingBar

    private var tsRating: Double = 0.0

    private lateinit var ratingDescriptionEdit: TextInputEditText

    @SuppressLint("ResourceAsColor", "ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //SmallAdvAdapter from SmallAdvertisement.kt
        val title: TextView = view.findViewById(R.id.tvSmallAdvTitle)
        val date: TextView = view.findViewById(R.id.tvsmallAdvDate)
        val time: TextView = view.findViewById(R.id.tvSmallAdvTime)
        val location: TextView = view.findViewById(R.id.tvSmallAdvLocation)
        val duration: TextView = view.findViewById(R.id.tvsmallAdvDuration)
        val image: ImageView = view.findViewById(R.id.smallAdv_image)
        val res = view.context.resources

        title.text = timeSlot.title
        date.text = "Date: ${timeSlot.date}"
        time.text = "Time: ${timeSlot.time}"
        location.text = "Location: ${timeSlot.location}"
        duration.text = "Duration: ${timeSlot.duration}"
        val fileDir = "/data/user/0/com.bancempo/app_imageDir"
        val profilePictureFileName = "profile.jpeg"

        File(fileDir, profilePictureFileName)
            .run {
                when (exists()) {
                    true -> BitmapFactory.decodeFile(
                        File(
                            fileDir,
                            profilePictureFileName
                        ).absolutePath
                    )
                    false -> BitmapFactory.decodeResource(
                        res, R.drawable.profile_pic_default
                    )
                }
            }.also {
                image.setImageBitmap(it)
            }

        ratingBar = view.findViewById(R.id.ratingBar)
        tsRating = ratingBar.rating.toDouble()
        ratingDescriptionEdit = view.findViewById(R.id.edit_rating_description_text)

        //handling on back pressed
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val bundle = Bundle()
                    bundle.putDouble("rating", tsRating)
                    if (tsRating < 1) {
                        Toast.makeText(requireContext(), "", Toast.LENGTH_SHORT).show()
                    } else {
                        if (timeSlot.userId == sharedVM.authUser.value!!.email!!) {
                            sharedVM.submitNewRating(timeSlot.userId, true, 0.0, tsRating)
                        } else {
                            sharedVM.submitNewRating(timeSlot.userId, false, tsRating, 0.0)
                        }
                        setFragmentResult("backFromRate", bundleOf())
                        findNavController().popBackStack()
                    }
                }
            }
            )
    }
}
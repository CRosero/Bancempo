package com.bancempo.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.bancempo.R
import com.bancempo.models.SharedViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*


class TimeSlotRateFragment : Fragment(R.layout.fragment_time_slot_rate) {
    private val sharedVM: SharedViewModel by activityViewModels()

    private lateinit var timeSlot: TextInputLayout

    private lateinit var ratingBar: RatingBar

    private var tsRating: Double = 0.0

    private lateinit var ratingDescriptionEdit: TextInputEditText

    @SuppressLint("ResourceAsColor", "ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                        if (timeSlot == myAdv) {
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
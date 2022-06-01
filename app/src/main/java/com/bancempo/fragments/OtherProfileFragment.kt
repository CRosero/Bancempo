package com.bancempo.fragments


import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.bancempo.R
import com.bancempo.models.SharedViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class OtherProfileFragment : Fragment(R.layout.fragment_show_profile) {

    private val sharedVM: SharedViewModel by activityViewModels()
    private lateinit var fullNameEd: TextInputEditText
    private lateinit var descriptionEd: TextInputEditText
    private lateinit var nicknameEd: TextInputEditText
    private lateinit var emailEd: TextInputEditText
    private lateinit var locationEd: TextInputEditText
    private lateinit var skillsEd: TextInputEditText
    private lateinit var creditEd: TextInputEditText


    private lateinit var photo: ImageView
    private lateinit var chipGroup: ChipGroup
    private var loadImg = true


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        fullNameEd = view.findViewById(R.id.textViewFullName_ed)
        photo = view.findViewById(R.id.profile_pic)
        nicknameEd = view.findViewById(R.id.textViewNickname_ed)
        emailEd = view.findViewById(R.id.textViewEmail_ed)
        locationEd = view.findViewById(R.id.textViewLocation_ed)
        descriptionEd = view.findViewById(R.id.textViewDescription_ed)
        creditEd = view.findViewById(R.id.tvCredit_ed)

        skillsEd = view.findViewById(R.id.textViewSkills_ed)
        chipGroup = view.findViewById(R.id.chipGroup)

        val userId = arguments?.getString("userId")
        val user = sharedVM.users.value!!.get(userId)!!

        fullNameEd.setText(user.fullname)
        nicknameEd.setText(user.nickname)
        emailEd.setText(user.email)
        locationEd.setText(user.location)
        descriptionEd.setText(user.description)
        creditEd.setText(user.credit.toString())

        skillsEd.setText("")
        chipGroup.removeAllViews()
        user.skills.forEach {
            val chip = Chip(activity)
            chip.text = it
            chip.setChipBackgroundColorResource(R.color.divider_color)
            chip.isCheckable = false
            chipGroup.addView(chip)
        }


        if (loadImg) {
            sharedVM.loadImageUser(photo, view, user)
        } else {
            val pb = view.findViewById<ProgressBar>(R.id.progressBar)
            pb.visibility = View.VISIBLE
            photo.visibility = View.INVISIBLE
            loadImg = true
        }

    }

}
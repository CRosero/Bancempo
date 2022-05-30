package com.bancempo.fragments


import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.bancempo.R
import com.bancempo.models.SharedViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class ShowProfileFragment : Fragment(R.layout.fragment_show_profile) {

    private val sharedVM: SharedViewModel by activityViewModels()
    private lateinit var fullNameEd: TextInputEditText
    private lateinit var descriptionEd: TextInputEditText
    private lateinit var nicknameEd: TextInputEditText
    private lateinit var emailEd: TextInputEditText
    private lateinit var locationEd: TextInputEditText
    private lateinit var skillsEd: TextInputEditText

    private lateinit var photo: ImageView
    private lateinit var chipGroup: ChipGroup
    private var loadImg = true

    private lateinit var giverRatingBar: RatingBar
    private lateinit var receiverRatingBar: RatingBar


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        giverRatingBar = view.findViewById(R.id.ratingBarGiver)
        receiverRatingBar = view.findViewById(R.id.ratingBarReceiver)

        fullNameEd = view.findViewById(R.id.textViewFullName_ed)
        photo = view.findViewById(R.id.profile_pic)
        nicknameEd = view.findViewById(R.id.textViewNickname_ed)
        emailEd = view.findViewById(R.id.textViewEmail_ed)
        locationEd = view.findViewById(R.id.textViewLocation_ed)
        descriptionEd = view.findViewById(R.id.textViewDescription_ed)

        skillsEd = view.findViewById(R.id.textViewSkills_ed)
        chipGroup = view.findViewById(R.id.chipGroup)



        sharedVM.currentUser.observe(viewLifecycleOwner) { user ->
            giverRatingBar.rating = user.giverRating
            receiverRatingBar.rating = user.receiverRating
            fullNameEd.setText(user.fullname)
            nicknameEd.setText(user.nickname)
            emailEd.setText(user.email)
            locationEd.setText(user.location)
            descriptionEd.setText(user.description)
            skillsEd.setText("")


            chipGroup.removeAllViews()
            user.skills.forEach {
                val chip = Chip(activity)
                chip.text = it
                chip.setChipBackgroundColorResource(R.color.divider_color)
                chip.isCheckable = false
                chipGroup.addView(chip)
            }

            setFragmentResultListener("backFromEdit") { _, _ ->
                loadImg = false
            }

        }

        sharedVM.haveIloadNewImage.observe(viewLifecycleOwner) {
            if (it) {
                //la foto è nuova
                if (loadImg) {
                    sharedVM.loadImageUser(photo, view)
                } else {
                    val pb = view.findViewById<ProgressBar>(R.id.progressBar)
                    pb.visibility = View.VISIBLE
                    photo.visibility = View.INVISIBLE
                    loadImg = true
                }
            } else {
                //la foto è vecchia
                sharedVM.loadImageUser(photo, view)
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_profile -> {
                editProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editProfile() {
        val bundle = Bundle()
        bundle.putString("fullname", fullNameEd.text.toString())
        bundle.putString("nickname", nicknameEd.text.toString())
        bundle.putString("description", descriptionEd.text.toString())
        bundle.putString("location", locationEd.text.toString())
        bundle.putString("email", emailEd.text.toString())

        var chipText = ""
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (i == chipGroup.childCount - 1) {
                chipText += "${chip.text}"

            } else {
                chipText += "${chip.text},"
            }
        }
        bundle.putString("skill", chipText)

        findNavController().navigate(R.id.action_showProfileFragment_to_editProfileFragment, bundle)
    }


}
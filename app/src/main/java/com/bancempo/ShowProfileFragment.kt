package com.bancempo


import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ShowProfileFragment : Fragment(R.layout.fragment_show_profile) {

    private val userVM: UserVM by activityViewModels()
    private lateinit var fullName: TextInputLayout
    private lateinit var fullName_ed: TextInputEditText
    private lateinit var description_ed: TextInputEditText
    private lateinit var nickname_ed: TextInputEditText
    private lateinit var email_ed: TextInputEditText
    private lateinit var location_ed: TextInputEditText
    private lateinit var skills_ed: TextInputEditText

    private lateinit var photo: ImageView
    private lateinit var nickname: TextInputLayout
    private lateinit var email: TextInputLayout
    private lateinit var location: TextInputLayout
    private lateinit var skills: TextInputLayout
    private lateinit var description: TextInputLayout
    private lateinit var chipGroup: ChipGroup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        fullName = view.findViewById(R.id.textViewFullName)
        fullName_ed = view.findViewById(R.id.textViewFullName_ed)
        photo = view.findViewById(R.id.profile_pic)
        nickname = view.findViewById(R.id.textViewNickname)
        nickname_ed = view.findViewById(R.id.textViewNickname_ed)
        email = view.findViewById(R.id.textViewEmail)
        email_ed = view.findViewById(R.id.textViewEmail_ed)
        location = view.findViewById(R.id.textViewLocation)
        location_ed = view.findViewById(R.id.textViewLocation_ed)
        description = view.findViewById(R.id.textViewDescription)
        description_ed = view.findViewById(R.id.textViewDescription_ed)

        skills =view.findViewById(R.id.textViewSkills)
        skills_ed =view.findViewById(R.id.textViewSkills_ed)
        chipGroup = view.findViewById(R.id.chipGroup)


        photo.setImageBitmap(userVM.profilePictureBitmap.value)
        fullName_ed.setText(userVM.fullname.value.toString())
        nickname_ed.setText(userVM.nickname.value.toString())
        email_ed.setText(userVM.email.value.toString())
        location_ed.setText(userVM.location.value.toString())
        description_ed.setText(userVM.description.value.toString())
        skills_ed.setText("")


        var skillsString = userVM.skills.value.toString()
        chipGroup.removeAllViews()
        if (skillsString != "") {
            skillsString.split(",").forEach {
                val chip = Chip(activity)
                if (it.isNotEmpty()) {
                    chip.text = it
                    chipGroup.addView(chip)
                }
            }
        }



        //after backpressed from editprofile
        fragmentManager?.setFragmentResultListener("backPressed", viewLifecycleOwner){ _, _ ->

            photo.setImageBitmap(userVM.profilePictureBitmap.value)
            fullName_ed.setText(userVM.fullname.value.toString())
            nickname_ed.setText(userVM.nickname.value.toString())
            email_ed.setText(userVM.email.value.toString())
            location_ed.setText(userVM.location.value.toString())
            description_ed.setText(userVM.description.value.toString())
            skills_ed.setText("")
            skillsString = userVM.skills.value.toString()

            chipGroup.removeAllViews()
            if (skillsString != "") {
                skillsString.split(",").forEach {
                    val chip = Chip(activity)
                    if (it.isNotEmpty()) {
                        chip.text = it
                        chipGroup.addView(chip)
                    }
                }
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

        bundle.putString("fullname", fullName_ed.text.toString())
        bundle.putString("nickname", nickname_ed.text.toString())
        bundle.putString("description", description_ed.text.toString())
        bundle.putString("location", location_ed.text.toString())
        bundle.putString("email", email_ed.text.toString())

        //bundle.putString("photo", encodeToBase64(photo.drawToBitmap()))
        //bundle.putParcelable("bitmapImg", photo.drawToBitmap())
        var chipText = ""
        for (j in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(j) as Chip
            chipText += "${chip.text},"
        }
        bundle.putString("skills", chipText)
        println("--------------------EDIT PROFILE")
        findNavController().navigate(R.id.action_showProfileFragment_to_editProfileFragment, bundle)
    }




}
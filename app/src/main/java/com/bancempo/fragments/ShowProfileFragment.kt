package com.bancempo.fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.*
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
    private lateinit var creditEd: TextInputEditText
    private lateinit var ratingBar: RatingBar
    private lateinit var ratingNum: TextView
    private lateinit var noRatings: TextView
    private lateinit var completeRatingBar: LinearLayout


    private lateinit var photo: ImageView
    private lateinit var chipGroup: ChipGroup
    private var loadImg = true


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        ratingBar = view.findViewById(R.id.ratingBar)
        ratingNum = view.findViewById(R.id.rating_num)
        fullNameEd = view.findViewById(R.id.textViewFullName_ed)
        photo = view.findViewById(R.id.profile_pic)
        nicknameEd = view.findViewById(R.id.textViewNickname_ed)
        emailEd = view.findViewById(R.id.textViewEmail_ed)
        locationEd = view.findViewById(R.id.textViewLocation_ed)
        descriptionEd = view.findViewById(R.id.textViewDescription_ed)
        creditEd = view.findViewById(R.id.tvCredit_ed)
        completeRatingBar = view.findViewById(R.id.complete_rating_bar)
        noRatings = view.findViewById(R.id.noRatings)

        skillsEd = view.findViewById(R.id.textViewSkills_ed)
        chipGroup = view.findViewById(R.id.chipGroup)

        sharedVM.currentUser.observe(viewLifecycleOwner) { user ->
            val numRatings = sharedVM.ratings.value!!.values
                .filter { x -> x.idReceiver == user.email }.size

            if (numRatings > 0) {
                noRatings.visibility = View.GONE
                completeRatingBar.visibility = View.VISIBLE
                ratingBar.rating = user.rating.toFloat()
                ratingNum.text = " ( ".plus(numRatings.toString()).plus(" ) ")
            }
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

            ratingBar.setOnTouchListener(View.OnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val bundle = Bundle()
                    bundle.putString("userId", user.email)
                    findNavController().navigate(
                        R.id.action_showProfileFragment_to_ratingsFragment,
                        bundle
                    )
                }
                return@OnTouchListener true
            })

            setFragmentResultListener("backFromEdit") { _, _ ->
                loadImg = false
            }

        }

        sharedVM.haveIloadNewImage.observe(viewLifecycleOwner) {
            if (it) {
                //la foto è nuova
                if (loadImg) {
                    sharedVM.loadImageUser(photo, view, sharedVM.currentUser.value!!)
                } else {
                    val pb = view.findViewById<ProgressBar>(R.id.progressBar)
                    pb.visibility = View.VISIBLE
                    photo.visibility = View.INVISIBLE
                    loadImg = true
                }
            } else {
                //la foto è vecchia
                sharedVM.loadImageUser(photo, view, sharedVM.currentUser.value!!)
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
            chipText += if (i == chipGroup.childCount - 1) {
                "${chip.text}"

            } else {
                "${chip.text},"
            }
        }
        bundle.putString("skill", chipText)

        findNavController().navigate(R.id.action_showProfileFragment_to_editProfileFragment, bundle)
    }


}
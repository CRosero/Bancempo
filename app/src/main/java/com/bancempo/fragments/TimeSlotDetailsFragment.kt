package com.bancempo.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.bancempo.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class TimeSlotDetailsFragment : Fragment(R.layout.fragment_time_slot_details) {
    private lateinit var title: TextInputLayout
    private lateinit var titleEd: TextInputEditText

    private lateinit var description: TextInputLayout
    private lateinit var descriptionEd: TextInputEditText


    private lateinit var date: TextInputLayout
    private lateinit var dateEd: TextInputEditText


    private lateinit var location: TextInputLayout
    private lateinit var locationEd: TextInputEditText

    private lateinit var duration: TextInputLayout
    private lateinit var durationEd: TextInputEditText

    private lateinit var time: TextInputLayout
    private lateinit var timeEd: TextInputEditText

    private lateinit var note: TextInputLayout
    private lateinit var noteEd: TextInputEditText

    private lateinit var chipGroup: ChipGroup

    private var skills: String? = ""

    private var isMyAdv = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        title = view.findViewById(R.id.title_adv)
        titleEd = view.findViewById(R.id.edit_title_text)
        description = view.findViewById(R.id.description_adv)
        descriptionEd = view.findViewById(R.id.edit_description_text)
        date = view.findViewById(R.id.date_adv)
        dateEd = view.findViewById(R.id.edit_date_text)
        time = view.findViewById(R.id.time_adv)
        timeEd = view.findViewById(R.id.edit_time_text)
        duration = view.findViewById(R.id.duration_adv)
        durationEd = view.findViewById(R.id.edit_duration_text)
        location = view.findViewById(R.id.edit_duration)
        locationEd = view.findViewById(R.id.edit_location_text)
        note = view.findViewById(R.id.note_adv)
        noteEd = view.findViewById(R.id.edit_note_text)
        chipGroup = view.findViewById(R.id.chipGroup)
        titleEd.setText(arguments?.getString("title"))
        descriptionEd.setText(arguments?.getString("description"))
        dateEd.setText(arguments?.getString("date"))
        timeEd.setText(arguments?.getString("time"))
        durationEd.setText(arguments?.getString("duration"))
        locationEd.setText(arguments?.getString("location"))
        noteEd.setText(arguments?.getString("note"))
        skills = arguments?.getString("skill")

        skills?.split(",")?.forEach {
            if (it != "") {
                val chip = Chip(activity)
                chip.text = it
                chip.setChipBackgroundColorResource(R.color.divider_color)
                chip.isCheckable = false
                chipGroup.addView(chip)
            }
        }

        isMyAdv = arguments?.getBoolean("isMyAdv")!!


        setFragmentResultListener("confirmationOkModifyToDetails") { _, bundle ->
            titleEd.setText(bundle.getString("title"))
            descriptionEd.setText(bundle.getString("description"))
            dateEd.setText(bundle.getString("date"))
            timeEd.setText(bundle.getString("time"))
            locationEd.setText(bundle.getString("location"))
            noteEd.setText(bundle.getString("note"))
            durationEd.setText(bundle.getString("duration"))
            skills = bundle.getString("skill")

            chipGroup.removeAllViews()
            skills!!.split(",").forEach {
                if (it != "") {
                    val chip = Chip(activity)
                    chip.text = it
                    chip.setChipBackgroundColorResource(R.color.divider_color)
                    chip.isCheckable = false
                    chipGroup.addView(chip)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (isMyAdv) {
            inflater.inflate(R.menu.options_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val bundle = Bundle()
        when (item.itemId) {
            R.id.inDetailsEditAdv -> {
                bundle.putBoolean("modifyFromDetails", true)
                bundle.putString("id", arguments?.getString("id"))
                bundle.putString("title", titleEd.text.toString())
                bundle.putInt("position", arguments?.getInt("position")!!)
                bundle.putString("description", descriptionEd.text.toString())
                bundle.putString("duration", durationEd.text.toString())
                bundle.putString("date", dateEd.text.toString())
                bundle.putString("time", timeEd.text.toString())
                bundle.putString("location", locationEd.text.toString())
                bundle.putString("note", noteEd.text.toString())

                var chipText = ""
                for (i in 0 until chipGroup.childCount) {
                    val chip = chipGroup.getChildAt(i) as Chip
                    chipText += "${chip.text}"
                    if (i != chipGroup.childCount - 1) {
                        chipText += ","
                    }
                }
                bundle.putString("skill", chipText)
                requireView().findNavController()
                    .navigate(R.id.action_timeSlotDetailsFragment_to_timeSlotEditFragment, bundle)
                return super.onOptionsItemSelected(item)
            }
            //Click back button
            else -> {
                return NavigationUI.onNavDestinationSelected(
                    item,
                    requireView().findNavController()
                ) || super.onOptionsItemSelected(item)
            }
        }
    }

}
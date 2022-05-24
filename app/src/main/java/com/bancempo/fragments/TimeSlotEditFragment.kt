package com.bancempo.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.bancempo.R
import com.bancempo.models.SharedViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*


class TimeSlotEditFragment : Fragment(R.layout.fragment_time_slot_edit) {
    private val sharedVM: SharedViewModel by activityViewModels()

    private lateinit var title: TextInputLayout
    private lateinit var titleEdit: TextInputEditText
    private lateinit var description: TextInputLayout
    private lateinit var descriptionEdit: TextInputEditText
    private lateinit var location: TextInputLayout
    private lateinit var locationEdit: TextInputEditText
    private lateinit var note: TextInputLayout
    private lateinit var noteEdit: TextInputEditText
    private lateinit var date: TextInputLayout
    private lateinit var dateEdit: TextInputEditText
    private lateinit var timeslot: TextInputLayout
    private lateinit var timeslotEdit: TextInputEditText
    private lateinit var duration: TextInputLayout
    private lateinit var durationEdit: TextInputEditText
    private lateinit var chipGroup: ChipGroup
    private var skillsString: String? = ""
    private lateinit var skills: List<String>
    private lateinit var skillsError: TextInputLayout
    private lateinit var skillsErrorEdit: TextInputEditText

    private lateinit var slider: Slider


    @SuppressLint("ResourceAsColor", "ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = view.findViewById(R.id.edit_title)
        titleEdit = view.findViewById(R.id.edit_title_text)
        description = view.findViewById(R.id.edit_description)
        descriptionEdit = view.findViewById(R.id.edit_description_text)
        location = view.findViewById(R.id.edit_location)
        locationEdit = view.findViewById(R.id.edit_location_text)
        note = view.findViewById(R.id.edit_note)
        noteEdit = view.findViewById(R.id.edit_note_text)
        date = view.findViewById(R.id.tvDate)
        dateEdit = view.findViewById(R.id.tvDate_text)
        timeslot = view.findViewById(R.id.tvTime)
        timeslotEdit = view.findViewById(R.id.tvTime_text)
        duration = view.findViewById(R.id.edit_duration)
        durationEdit = view.findViewById(R.id.edit_duration_text)
        chipGroup = view.findViewById(R.id.chipGroup)
        skillsError = view.findViewById(R.id.skills_error)
        skillsErrorEdit = view.findViewById(R.id.error_skills_text)

        slider = view.findViewById(R.id.slider)
        titleEdit.setText(arguments?.getString("title"))
        descriptionEdit.setText(arguments?.getString("description"))
        locationEdit.setText(arguments?.getString("location"))
        noteEdit.setText(arguments?.getString("note"))
        dateEdit.setText(arguments?.getString("date"))
        timeslotEdit.setText(arguments?.getString("time"))
        durationEdit.setText(arguments?.getString("duration"))
        skillsString = arguments?.getString("skill")
        title.error = null
        description.error = null
        location.error = null
        note.error = null
        date.error = null
        timeslot.error = null
        skillsError.error = null

        val createNewAdv = arguments?.getBoolean("createNewAdv")
        val modify = createNewAdv == null || createNewAdv == false

        val buttonDate = view.findViewById<Button>(R.id.buttonDate)
        buttonDate.setOnClickListener { showDialogOfDatePicker(modify) }

        val buttonTime = view.findViewById<Button>(R.id.buttonTime)
        buttonTime.setOnClickListener { showDialogOfTimeSlotPicker(modify) }

        val confirmButton = view.findViewById<Button>(R.id.confirmationButton)

        skills = sharedVM.currentUser.value?.skills!!

        if (modify) {
            confirmButton.visibility = View.GONE
            slider.value = durationEdit.text.toString().toFloat()

            if (skillsString != null) {
                skills.forEach {
                    val chip = Chip(activity)

                    if (skillsString!!.split(",").contains(it)) {
                        chip.isCheckable = true
                        chip.text = it
                        chip.isChecked = true
                        chip.isCloseIconVisible = true
                        chip.setChipBackgroundColorResource(R.color.light_primary_color)
                        chip.isSelected = true
                        chipGroup.addView(chip)
                    } else {
                        chip.isCheckable = true
                        chip.text = it
                        chip.isChecked = false
                        chip.setChipBackgroundColorResource(R.color.divider_color)
                        chipGroup.addView(chip)
                    }
                    chip.setOnClickListener {
                        chip.isChecked = true
                        chip.isCloseIconVisible = true
                        chip.setChipBackgroundColorResource(R.color.light_primary_color)
                    }
                    chip.setOnCloseIconClickListener {
                        chip.isChecked = false
                        chip.isCloseIconVisible = false
                        chip.setChipBackgroundColorResource(R.color.divider_color)
                    }
                }
            }
        } else {
            skills.forEach {
                val chip = Chip(activity)
                chip.isCheckable = true
                chip.text = it
                chip.setChipBackgroundColorResource(R.color.divider_color)
                chipGroup.addView(chip)

                chip.setOnClickListener {
                    chip.isChecked = true
                    chip.isCloseIconVisible = true
                    chip.setChipBackgroundColorResource(R.color.light_primary_color)
                }
                chip.setOnCloseIconClickListener {
                    chip.isChecked = false
                    chip.isCloseIconVisible = false
                    chip.setChipBackgroundColorResource(R.color.divider_color)
                }
            }
        }

        //CREATE A NEW ADV
        confirmButton.setOnClickListener {
            if (validation()) {
                val bundle = Bundle()
                bundle.putString("title", titleEdit.text.toString())
                bundle.putString("date", dateEdit.text.toString())
                bundle.putString("description", descriptionEdit.text.toString())
                bundle.putString("time", timeslotEdit.text.toString())
                bundle.putString("duration", durationEdit.text.toString())
                bundle.putString("location", locationEdit.text.toString())
                bundle.putString("note", noteEdit.text.toString())
                bundle.putString("userId", "de96wgyM8s4GvwM6HFPr")

                var chipText = ""
                var count = 0
                for (i in 0 until chipGroup.childCount) {
                    val chip = chipGroup.getChildAt(i) as Chip
                    if (chip.isChecked) {
                        chipText += "${chip.text}"
                        if (count != chipGroup.checkedChipIds.size - 1) {
                            chipText += ","
                        }
                        count += 1
                    }
                }


                bundle.putString("skill", chipText)

                sharedVM.addNewAdv(bundle)
                setFragmentResult("confirmationOkCreate", bundleOf())
                findNavController().popBackStack()
            }
        }

        //handler slider
        slider.addOnChangeListener { _, value, _ ->
            // Responds to when slider's value is changed
            durationEdit.setText(value.toString())
        }


        //handling on back pressed
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (modify) {
                        if (validation()) {
                            val bundle = Bundle()

                            //Modify advertisement on Firestore
                            val id = arguments?.getString("id")
                            bundle.putString("title", titleEdit.text.toString())
                            bundle.putString("date", dateEdit.text.toString())
                            bundle.putString("description", descriptionEdit.text.toString())
                            bundle.putString("time", timeslotEdit.text.toString())
                            bundle.putString("duration", durationEdit.text.toString())
                            bundle.putString("location", locationEdit.text.toString())
                            bundle.putString("note", noteEdit.text.toString())
                            bundle.putString("userId", "de96wgyM8s4GvwM6HFPr")

                            var chipText = ""
                            var count = 0
                            for (i in 0 until chipGroup.childCount) {
                                val chip = chipGroup.getChildAt(i) as Chip
                                if (chip.isChecked) {
                                    chipText += "${chip.text}"
                                    if (count != chipGroup.checkedChipIds.size - 1) {
                                        chipText += ","
                                    }
                                    count += 1
                                }
                            }
                            bundle.putString("skill", chipText)
                            sharedVM.modifyAdv(id!!, bundle)
                            val modifyFromList = arguments?.getBoolean("modifyFromList")
                            if (modifyFromList == false) {
                                setFragmentResult("confirmationOkModifyToDetails", bundle)
                            }
                            findNavController().popBackStack()
                        }
                    } else {
                        findNavController().popBackStack()
                    }
                }
            })


    }

    private fun validateTextInput(text: TextInputLayout, textEdit: TextInputEditText): Boolean {
        if (textEdit.text.isNullOrEmpty()) {
            text.error = "Please, fill in this field!"
            return false
        } else {
            when (text.hint) {
                "Description" -> {
                    return if (textEdit.text?.length!! > 200) {
                        text.error = "Your ${text.hint} is too long."
                        false
                    } else {
                        text.error = null
                        true
                    }
                }
                "Note" -> {
                    return if (textEdit.text?.length!! > 200) {
                        text.error = "Your ${text.hint} is too long."
                        false
                    } else {
                        text.error = null
                        true
                    }
                }
                "Title" -> {
                    return if (textEdit.text?.length!! > 20) {
                        text.error = "Your ${text.hint} is too long."
                        false
                    } else {
                        text.error = null
                        return true
                    }
                }

                "Location" -> {
                    return if (textEdit.text?.length!! > 20) {
                        text.error = "Your ${text.hint} is too long."
                        false
                    } else {
                        text.error = null
                        return true
                    }
                }

                "Date" -> {
                    return if (textEdit.text.toString() == "dd/mm/yyyy") {
                        text.error = "Please, choose a date for your adv!"
                        false
                    } else {
                        text.error = null
                        true
                    }
                }

                "Time" -> {
                    return if (textEdit.text.toString() == "hh:mm") {
                        text.error = "Please, choose a start time for your adv!"
                        false
                    } else {
                        text.error = null
                        true
                    }
                }

                "Duration (h)" -> {
                    text.error = null
                    return true
                }

                else -> return false
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun validateSkills(skills: List<String>, text: TextInputLayout): Boolean {
        var valid = true
        if (skills.isEmpty()) {
            text.error = R.string.advs_edit_prof_skill_error.toString()
            valid = false
        }
        if (chipGroup.checkedChipIds.isEmpty()) {
            text.error = R.string.advs_edit_skill_error.toString()
            valid = false
        }
        return valid
    }

    private fun validation(): Boolean {
        var valid = true

        if (!validateTextInput(title, titleEdit)) {
            valid = false
        }
        if (!validateTextInput(description, descriptionEdit)) {
            valid = false
        }
        if (!validateTextInput(date, dateEdit)) {
            valid = false
        }
        if (!validateTextInput(timeslot, timeslotEdit)) {
            valid = false
        }
        if (!validateTextInput(location, locationEdit)) {
            valid = false
        }
        if (!validateTextInput(note, noteEdit)) {
            valid = false
        }
        if (!validateTextInput(duration, durationEdit)) {
            valid = false
        }
        if (!validateSkills(skills, skillsError)) {
            valid = false
        }
        return valid
    }

    //Date
    private fun showDialogOfDatePicker(modifyFromList: Boolean) {
        val datePickerFragment = DatePickerFragment(dateEdit, modifyFromList)
        datePickerFragment.show(requireActivity().supportFragmentManager, "datePicker")
    }


    class DatePickerFragment(private val date: TextInputEditText, private val modify: Boolean) :
        DialogFragment(), DatePickerDialog.OnDateSetListener {

        private var c = Calendar.getInstance()
        private var year = c.get(Calendar.YEAR)
        private var month = c.get(Calendar.MONTH)
        private var day = c.get(Calendar.DAY_OF_MONTH)

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if (modify) {
                val arr = date.text.toString().split("/")
                val dd = arr[0].toInt()
                val mm = arr[1].toInt()
                val yyyy = arr[2].toInt()
                c.set(yyyy, mm, dd)
            }
            retainInstance = true
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putInt("year", year)
            outState.putInt("month", month)
            outState.putInt("day", day)
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            if (savedInstanceState != null) {
                year = savedInstanceState.getInt("year")
                month = savedInstanceState.getInt("month")
                day = savedInstanceState.getInt("day")
            } else {
                if (date.text.toString() != "") {
                    year = date.text!!.split("/").elementAt(2).toInt()
                    month = date.text!!.split("/").elementAt(1).toInt() - 1
                    day = date.text!!.split("/").elementAt(0).toInt()
                } else {
                    year = c.get(Calendar.YEAR)
                    month = c.get(Calendar.MONTH)
                    day = c.get(Calendar.DAY_OF_MONTH)
                }
            }
            return DatePickerDialog(requireContext(), this, year, month, day)
        }

        @SuppressLint("SetTextI18n")
        override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
            this.year = year
            this.month = month
            this.day = day
            date.setText("${day}/${(month + 1)}/${year}")
        }

    }

    //Time
    private fun showDialogOfTimeSlotPicker(modify: Boolean) {
        val timeslotPickerFragment = TimePickerFragment(timeslotEdit, modify)
        timeslotPickerFragment.show(requireActivity().supportFragmentManager, "timePicker")
    }

    class TimePickerFragment(private val timeslot: TextInputEditText, private val modify: Boolean) :
        DialogFragment(), TimePickerDialog.OnTimeSetListener {

        private var c = Calendar.getInstance()
        private var hour = c.get(Calendar.HOUR_OF_DAY)
        private var minute = c.get(Calendar.MINUTE)

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if (modify) {
                val arrTime = timeslot.text.toString().split(":")
                val hh = arrTime[0].toInt()
                val mm = arrTime[1].toInt()
                c.set(Calendar.HOUR_OF_DAY, hh)
                c.set(Calendar.MINUTE, mm)
            }
            retainInstance = true
        }

        override fun onSaveInstanceState(outState: Bundle) {
            outState.putInt("hour", hour)
            outState.putInt("minute", minute)
            super.onSaveInstanceState(outState)
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

            if (savedInstanceState != null) {
                hour = savedInstanceState.getInt("hour")
                minute = savedInstanceState.getInt("minute")
            } else {
                if (timeslot.text.toString() != "") {
                    hour = timeslot.text!!.split(":").elementAt(0).toInt()
                    minute = timeslot.text!!.split(":").elementAt(1).toInt()
                } else {
                    hour = c.get(Calendar.HOUR_OF_DAY)
                    minute = c.get(Calendar.MINUTE)
                }
            }

            return TimePickerDialog(
                activity,
                this,
                hour,
                minute,
                DateFormat.is24HourFormat(activity)
            )
        }

        @SuppressLint("SetTextI18n")
        override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
            if (minute in 0..9) {
                timeslot.setText("$hourOfDay:0$minute")
            } else {
                timeslot.setText("$hourOfDay:$minute")
            }
        }
    }
}


package com.bancempo.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.bancempo.R
import com.bancempo.models.SharedViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class TimeSlotDetailsFragment : Fragment(R.layout.fragment_time_slot_details) {
    private val sharedVM: SharedViewModel by activityViewModels()

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

    private lateinit var advof: TextView

    private lateinit var chipGroup: ChipGroup

    private lateinit var chatButton: Button

    private lateinit var slotUnavailable: TextView

    private lateinit var idAdv: String

    private var skills: String? = ""

    private var isMyAdv = false
    private var reservationPage = false

    private lateinit var idBidder: String

    private lateinit var rateButton: Button

    private lateinit var ratingBar: RatingBar

    private var advRating: Double = 0.0

    private lateinit var ratingText: TextInputEditText

    @SuppressLint("ResourceAsColor", "SetTextI18n")
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

        advof = view.findViewById(R.id.advof)

        chipGroup = view.findViewById(R.id.chipGroup)

        chatButton = view.findViewById(R.id.button_chat)
        slotUnavailable = view.findViewById(R.id.slotNotAvailable)
        slotUnavailable.isVisible = false

        rateButton = view.findViewById(R.id.button_rate)
        ratingBar = view.findViewById(R.id.ratingBar)
        advRating = ratingBar.rating.toDouble()
        ratingText = view.findViewById(R.id.edit_rating_description_text)

        val userId = arguments?.getString("userId")

        titleEd.setText(arguments?.getString("title"))
        descriptionEd.setText(arguments?.getString("description"))
        dateEd.setText(arguments?.getString("date"))
        timeEd.setText(arguments?.getString("time"))
        advof.setText("Adv of ".plus(userId))
        durationEd.setText(arguments?.getString("duration"))
        locationEd.setText(arguments?.getString("location"))
        noteEd.setText(arguments?.getString("note"))


        var createNewConv: Boolean? = false
        idAdv = arguments?.getString("id")!!

        idBidder = arguments?.getString("idBidder")!!

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
        reservationPage = arguments?.getBoolean("reservationPage")!!


        sharedVM.conversations.observe(viewLifecycleOwner) { convs ->
            createNewConv = false
            if (isMyAdv) {
                advof.isVisible = false
                //L'UTENTE LOGGATO E' IL BIDDER, VEDO SE ESISTONO CONVERSAZIONI APERTE PER QUELL'ANNUNCIO
                val filtered = convs.values.filter { conv ->
                    conv.idBidder == sharedVM.currentUser.value!!.email &&
                            conv.idAdv == idAdv && !conv.closed
                }
                if (filtered.isNotEmpty()) {
                    //SE NE ESISTE ALMENO UNA VISUALIZZO IL BOTTONE CHAT
                    chatButton.visibility = View.VISIBLE
                } else {
                    chatButton.visibility = View.GONE
                }
                slotUnavailable.isVisible = false

            } else {
                if (reservationPage) {
                    //SONO LOGGATO COME BIDDER MA NON POSSO MODIFICARE L'ANNUNCIO
                    chatButton.visibility = View.VISIBLE
                    slotUnavailable.isVisible = false
                } else {
                    //SE SONO LOGGATO COME ASKER
                    println("------------- ${convs.values}}")

                    // tutte le conversazioni di quell'annuncio
                    val advConvs = convs.values.filter { conv -> conv.idAdv == idAdv }
                    println("-------------ADVCONVS $idAdv ${convs.values.filter { conv -> conv.idAdv == idAdv }}")
                    //tutte le mie conversazioni di quell'annuncio
                    val myAdvConvs =
                        advConvs.filter { conv -> conv.idAsker == sharedVM.currentUser.value!!.email }
                    println("-------------MYADVCONVS ${advConvs.filter { conv -> conv.idAsker == sharedVM.currentUser.value!!.email }}")

                    //tutte le mie conversazioni di quell'annuncio aperte
                    val myAdvsOpened = myAdvConvs.filter { x -> !x.closed }
                    println("-------------MYADVOPENED ${myAdvConvs.filter { x -> !x.closed }}")

                    //tutte le conversazioni di quell'annuncio non mie
                    val otherAdvConvs =
                        advConvs.filter { conv -> conv.idAsker != sharedVM.currentUser.value!!.email }
                    //tutte le conversazioni di quell'annuncio chiuse non mie
                    val otherAdvsClosed = otherAdvConvs.filter { x -> x.closed }

                    println("now: closed degli altri:${otherAdvsClosed.size}; convs altri:${otherAdvConvs.size}")
                    //NON ESISTONO CONVERSAZIONI PER QUESTO ANNUNCIO
                    if (advConvs.isEmpty()) {
                        //il bottone chat è visibile a tutti gli asker
                        chatButton.visibility = View.VISIBLE
                        slotUnavailable.isVisible = false
                        createNewConv = true
                    }

                    //ESISTONO DELLE CONVERSAZIONI LEGATE A ME
                    else if (myAdvConvs.isNotEmpty()) {

                        if (myAdvsOpened.isEmpty()) {
                            //se sono chiuse non posso più visualizzare il pulsante chat
                            // vengo avvisato che il bidder ha rifiutato la mia richiesta
                            chatButton.visibility = View.GONE
                            slotUnavailable.text = getString(R.string.conversationRefused)
                            slotUnavailable.isVisible = true
                        } else {
                            println("------------------ GIUSTO")
                            chatButton.visibility = View.VISIBLE
                            slotUnavailable.isVisible = false
                            createNewConv = false
                        }
                    }
                    //ESISTONO DELLE CONVERSAZIONI NON MIE MA TUTTE CHIUSE
                    else if (otherAdvsClosed.size == otherAdvConvs.size) {
                        println("------------------ GIUSTO")
                        chatButton.visibility = View.VISIBLE
                        slotUnavailable.isVisible = false
                        createNewConv = true
                    }
                    //ESISTONO CONVERSAZIONI NON MIE TRA CUI ALMENO UNA APERTA
                    else {
                        //non posso visualizzare bottone chat e mi avvisano della negoziazione in corso
                        chatButton.visibility = View.GONE
                        slotUnavailable.text = getString(R.string.adv_unavailable)
                        slotUnavailable.isVisible = true
                    }
                }
            }
        }

        advof.setOnClickListener{
            val bundle = bundleOf()
            bundle.putString("userId", userId)
            requireView().findNavController()
                .navigate(R.id.action_timeSlotDetailsFragment_to_otherProfileFragment, bundle)
        }
        chatButton.setOnClickListener {
            val bundle = Bundle()
            val idAdv = arguments?.getString("id")

            bundle.putString("idAdv", idAdv)
            bundle.putString("title", titleEd.text.toString())
            bundle.putString("duration", durationEd.text.toString())
            bundle.putString("description", descriptionEd.text.toString())
            bundle.putString("date", dateEd.text.toString())
            bundle.putString("time", timeEd.text.toString())
            bundle.putString("location", locationEd.text.toString())
            bundle.putString("note", noteEd.text.toString())
            bundle.putString("skill", skills)
            bundle.putBoolean("isMyAdv", isMyAdv)
            bundle.putString("idBidder", idBidder)
            bundle.putBoolean("newConv", createNewConv!!)

            //NAVIGATION CHAT FRAGMENT
            requireView().findNavController()
                .navigate(R.id.action_timeSlotDetailsFragment_to_chatFragment, bundle)
        }

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

        if (sharedVM.ratings.value!!.values.filter { ratingObj -> ratingObj.idAdv == idAdv }
                .isEmpty()) {
            rateButton.visibility = View.VISIBLE
        }

        rateButton.setOnClickListener {
            val rateFragment = RateAdvDialogFragment(
                idAdv,
                advRating,
                ratingText.text.toString()
            )
            rateFragment.show(requireActivity().supportFragmentManager, "rate")
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
            //clicking on edit adv
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
                    if (i == chipGroup.childCount - 1) {
                        chipText += "${chip.text}"

                    } else {
                        chipText += "${chip.text},"
                    }
                }

                bundle.putString("skill", chipText)

                requireView().findNavController()
                    .navigate(R.id.action_timeSlotDetailsFragment_to_timeSlotEditFragment, bundle)
                return super.onOptionsItemSelected(item)
            }
            //clicking back button
            else -> {
                return NavigationUI.onNavDestinationSelected(
                    item,
                    requireView().findNavController()
                ) || super.onOptionsItemSelected(item)
            }
        }
    }


}

class RateAdvDialogFragment(
    idAdv: String,
    advRating: Double,
    advRatingText: String
) :
    DialogFragment() {
    private val sharedVM: SharedViewModel by activityViewModels()
    private val idAdv = idAdv
    private val idAsker = getAskerId(idAdv) ?: ""
    private val idBidder = getGiverId(idAdv) ?: ""
    private val advRating = advRating
    private val advRatingText = advRatingText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val userId = arguments?.getString("userId")
            builder.setView(inflater.inflate(R.layout.dialog_rate, null))
                // Add action buttons
                .setPositiveButton(R.string.submit,
                    DialogInterface.OnClickListener { dialog, id ->
                        if (advRating < 1) {
                            Toast.makeText(
                                requireContext(),
                                R.string.rating_min_err,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val otherUserId = if (sharedVM.currentUser.value!!.email == idAsker) {
                                idBidder
                            } else {
                                idAsker
                            }
                            sharedVM.submitNewRating(
                                otherUserId,
                                idAdv,
                                idAsker,
                                idBidder,
                                advRating,
                                advRatingText
                            )
                        }
                        getDialog()?.dismiss()

                    })
                .setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        getDialog()?.cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun getAskerId(idAdv: String): String? {
        return sharedVM.conversations.value!!.values.filter { conv -> !conv.closed && conv.idAdv == idAdv }
            .getOrNull(0)?.idAsker
    }

    fun getGiverId(idAdv: String): String? {
        return sharedVM.conversations.value!!.values.filter { conv -> !conv.closed && conv.idAdv == idAdv }
            .getOrNull(0)?.idBidder
    }

}
package com.bancempo.data

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bancempo.R
import com.bancempo.models.SharedViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore


data class SmallAdv(
    val id: String,
    val title: String,
    val date: String,
    val description: String,
    val time: String,
    val duration: String,
    val location: String,
    val note: String,
    val creationTime: String,
    val skill: String,
    val userId: String,
    val booked: Boolean
)

class SmallAdvAdapter(
    private val data: List<SmallAdv>,
    private val isMyAdvs: Boolean,
    private val reservationPage: Boolean,
    private val sharedVM: SharedViewModel
) : RecyclerView.Adapter<SmallAdvAdapter.SmallAdvHolder>() {

    class SmallAdvHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.tvSmallAdvTitle)
        private val date: TextView = v.findViewById(R.id.tvsmallAdvDate)
        private val time: TextView = v.findViewById(R.id.tvSmallAdvTime)
        private val location: TextView = v.findViewById(R.id.tvSmallAdvLocation)
        private val duration: TextView = v.findViewById(R.id.tvsmallAdvDuration)
        private val delete: FloatingActionButton = v.findViewById(R.id.delete_adv)
        private val edit: FloatingActionButton = v.findViewById(R.id.edit_adv)
        private val image: ImageView = v.findViewById(R.id.smallAdv_image)
        private val db = FirebaseFirestore.getInstance()


        @SuppressLint("SetTextI18n")
        fun bind(
            adv: SmallAdv,
            position: Int,
            isMyAdvs: Boolean,
            sharedVM: SharedViewModel,
            view: View,
        ) {
            title.text = adv.title
            date.text = "Date: ${adv.date}"
            time.text = "Time: ${adv.time}"
            location.text = adv.location
            duration.text = "Duration: ${adv.duration}"

            if (isMyAdvs) {
                edit.isVisible = true
                edit.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("id", adv.id)
                    bundle.putBoolean("modifyFromList", true)
                    bundle.putInt("position", position)
                    bundle.putString("title", adv.title)
                    bundle.putString("date", adv.date)
                    bundle.putString("userId", adv.userId)
                    bundle.putString("description", adv.description)
                    bundle.putString("time", adv.time)
                    bundle.putString("duration", adv.duration)
                    bundle.putString("location", adv.location)
                    bundle.putString("note", adv.note)
                    bundle.putString("skill", adv.skill)
                    findNavController(it).navigate(
                        R.id.action_timeSlotListFragment_to_timeSlotEditFragment,
                        bundle
                    )
                }
                image.visibility = View.GONE
                delete.isVisible = true
                delete.setOnClickListener {

                    db.collection("advertisements").document(adv.id)
                        .delete()
                        .addOnSuccessListener {}
                        .addOnFailureListener {}


                }
            } else {
                edit.isVisible = false
                delete.isVisible = false
                sharedVM.loadImageUserById(adv.userId, view)
            }


        }

        fun unbind() {
            edit.setOnClickListener(null)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmallAdvHolder {
        val vg = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.small_advertisment, parent, false)

        return SmallAdvHolder(vg)
    }

    //ti dice quale elemento della lista è correntemente visibile e la sua posizione il lista
    override fun onBindViewHolder(holder: SmallAdvHolder, position: Int) {
        holder.bind(data[position], position, isMyAdvs, sharedVM, holder.itemView)

        holder.itemView.setOnClickListener {

            val bundle = Bundle()
            bundle.putString("id", data[position].id)
            bundle.putInt("position", position)
            bundle.putString("title", data[position].title)
            bundle.putString("date", data[position].date)
            bundle.putString("description", data[position].description)
            bundle.putString("time", data[position].time)
            bundle.putString("userId", data[position].userId)
            bundle.putString("duration", data[position].duration)
            bundle.putString("location", data[position].location)
            bundle.putString("note", data[position].note)
            bundle.putString("skill", data[position].skill)
            bundle.putString("idBidder", data[position].userId)
            bundle.putBoolean("isMyAdv", isMyAdvs)
            bundle.putBoolean("reservationPage", reservationPage)

            findNavController(it).navigate(
                R.id.action_timeSlotListFragment_to_timeSlotDetailsFragment,
                bundle
            )
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onViewDetachedFromWindow(holder: SmallAdvHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.unbind()
    }

}
package com.bancempo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView

data class Skill(val title: String, val creationTime: String, val createdBy: String)

class ItemAdapter(private val data: List<Skill>) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tvSkillTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val vg = LayoutInflater.from(parent.context).inflate(R.layout.skill_item, parent, false)
        return ItemViewHolder(vg)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.title.text = data[position].title
        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("skill", data[position].title)

            Navigation.findNavController(it)
                .navigate(R.id.action_listSkills_to_timeSlotListFragment, bundle)
        }
    }

    override fun getItemCount(): Int = data.size

}
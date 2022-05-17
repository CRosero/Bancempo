package com.bancempo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView

data class Skill (val title:String, val creationTime: String, val createdBy: String)

class ItemAdapter(private val data:List<Skill>): RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(v: View): RecyclerView.ViewHolder(v){

        val title: TextView = v.findViewById(R.id.tvSkillTitle)
        //val numberAdv: TextView = v.findViewById(R.id.tv_adv_count)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAdapter.ItemViewHolder {

        val vg = LayoutInflater.from(parent.context).inflate(R.layout.skill_item, parent, false)
        return ItemViewHolder(vg)

    }

    override fun onBindViewHolder(holder: ItemAdapter.ItemViewHolder, position: Int) {
        holder.title.text = data[position].title

        /*
        holder.bind(data[position], position)

        holder.itemView.setOnClickListener{

            val bundle = Bundle()
            bundle.putString("id", data[position].id)
            bundle.putInt("position", position)
            bundle.putString("title", data[position].title)
            bundle.putString("date", data[position].date)
            bundle.putString("description", data[position].description)
            bundle.putString("time", data[position].time)
            bundle.putString("duration", data[position].duration)
            bundle.putString("location", data[position].location)
            bundle.putString("note", data[position].note)

            Navigation.findNavController(it)
                .navigate(R.id.action_timeSlotListFragment_to_timeSlotDetailsFragment, bundle)
        }
        //holder.numberAdv.text = data[position].numberAdvù

         */
        holder.itemView.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("skill", data[position].title)

            Navigation.findNavController(it)
                .navigate(R.id.action_listSkills_to_timeSlotListFragment, bundle)
        }
    }

    override fun getItemCount(): Int = data.size


}
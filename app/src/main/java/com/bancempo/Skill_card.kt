package com.bancempo

import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


data class Skill_card (val title:String, val numberAdv:String)

class ItemAdapter(val data:List<Skill_card>): RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(v: View): RecyclerView.ViewHolder(v){

        val title: TextView = v.findViewById(R.id.tvSkillTitle)
        val numberAdv: TextView = v.findViewById(R.id.tv_adv_count)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAdapter.ItemViewHolder {

        val vg = LayoutInflater.from(parent.context).inflate(R.layout.skill_item, parent, false)
        return ItemViewHolder(vg)

    }

    override fun onBindViewHolder(holder: ItemAdapter.ItemViewHolder, position: Int) {
        holder.title.text = data[position].title
        holder.numberAdv.text = data[position].numberAdv
    }

    override fun getItemCount(): Int = data.size


}
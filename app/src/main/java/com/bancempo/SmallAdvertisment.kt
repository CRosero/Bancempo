package com.bancempo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

data class SmallAdv(val title:String, val date:String, val description:String, val time:String, val duration:String, val location:String, val note:String)

class SmallAdvAdapter(private val data: List<SmallAdv>) : RecyclerView.Adapter<SmallAdvAdapter.SmallAdvHolder>(){
    class SmallAdvHolder(v:View) : RecyclerView.ViewHolder(v){
        private val title: TextView = v.findViewById(R.id.tvSmallAdvTitle)
        private val date: TextView = v.findViewById(R.id.tvsmallAdvDate)
        private val time: TextView = v.findViewById(R.id.tvSmallAdvTime)
        private val location: TextView = v.findViewById(R.id.tvSmallAdvLocation)
        private val duration: TextView = v.findViewById(R.id.tvsmallAdvDuration)
        private val edit: FloatingActionButton = v.findViewById(R.id.edit_adv)
        private val image: ImageView = v.findViewById(R.id.smallAdv_image)
        private val res = v.context.resources

        fun bind(adv: SmallAdv, position: Int){
            title.text = adv.title
            date.text = "Date: ${adv.date}"
            time.text = "Time: ${adv.time}"
            location.text = "Location: ${adv.location}"
            duration.text = "Duration: ${adv.duration}"
            loadProfileImage()

            edit.setOnClickListener{
                val bundle = Bundle()
                bundle.putBoolean("modifyFromList", true)
                bundle.putInt("position", position)
                bundle.putString("title", adv.title)
                bundle.putString("date", adv.date)
                bundle.putString("description", adv.description)
                bundle.putString("time", adv.time)
                bundle.putString("duration", adv.duration)
                bundle.putString("location", adv.location)
                bundle.putString("note", adv.note)
                findNavController(it).navigate(R.id.action_timeSlotListFragment_to_timeSlotEditFragment, bundle)
            }

        }




        fun unbind(){
            edit.setOnClickListener(null)
        }

        private fun loadProfileImage(): Bitmap {
            val fileDir = "/data/user/0/com.bancempo/app_imageDir"
            val profilePictureFileName = "profile.jpeg"



            return File(fileDir, profilePictureFileName)
                .run {
                    when (exists()) {
                        true -> BitmapFactory.decodeFile(File(fileDir, profilePictureFileName).absolutePath)
                        false -> BitmapFactory.decodeResource(
                            res, R.drawable.profile_pic_default)
                    }
                }.also {
                    image.setImageBitmap(it)
                }
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
        holder.bind(data[position], position)

        holder.itemView.setOnClickListener{

            val bundle = Bundle()
            bundle.putInt("position", position)
            bundle.putString("title", data[position].title)
            bundle.putString("date", data[position].date)
            bundle.putString("description", data[position].description)
            bundle.putString("time", data[position].time)
            bundle.putString("duration", data[position].duration)
            bundle.putString("location", data[position].location)
            bundle.putString("note", data[position].note)

            findNavController(it).navigate(R.id.action_timeSlotListFragment_to_timeSlotDetailsFragment, bundle)
        }
    }

    override fun getItemCount(): Int = data.size

    //serve per non avere i listener attivi sui ghost elements
    //TODO: da controllare come lo fa il prof a lezione
    override fun onViewDetachedFromWindow(holder: SmallAdvHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.unbind()
    }

}
package com.bancempo.data

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bancempo.R
import com.bancempo.SmallAdv
import com.bancempo.SmallAdvAdapter
import com.bancempo.models.SharedViewModel


data class Message(
    val idMsg: String,
    val idConv: String,
    val date: String,
    val text: String,
    val from: String,
    val to: String,
    val readed: Boolean
)

private object Const{
    const val MYMESSAGE = 1
    const val OTHERMESSAGE = 0
}

class MessageAdapter(private val messageList: List<Message>, private val sharedVM: SharedViewModel)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int = messageList.size

    inner class MyMessageViewHolder(v: View): RecyclerView.ViewHolder(v){
        val dateMsg = v.findViewById<TextView>(R.id.text_gchat_date_me)
        val textMsg = v.findViewById<TextView>(R.id.text_gchat_message_me)
        val timeMsg = v.findViewById<TextView>(R.id.text_gchat_timestamp_me)

        fun bind(mes: Message){
            val arrDate = mes.date.split(" ")
            val effDate = arrDate[0]
            val arrTime = arrDate[1].split(":")
            val effTime = arrTime[0].plus(":").plus(arrTime[1])

            textMsg.text = mes.text
            dateMsg.text = effDate
            timeMsg.text = effTime
        }
    }

    inner class OtherMessageViewHolder(v: View): RecyclerView.ViewHolder(v){
        val nickname = v.findViewById<TextView>(R.id.text_gchat_user_other)
        val dateMsg = v.findViewById<TextView>(R.id.text_gchat_date_other)
        val textMsg = v.findViewById<TextView>(R.id.text_gchat_message_other)
        val timeMsg = v.findViewById<TextView>(R.id.text_gchat_timestamp_other)

        fun bind(mes: Message){
            val arrDate = mes.date.split(" ")
            val effDate = arrDate[0]
            val arrTime = arrDate[1].split(":")
            val effTime = arrTime[0].plus(":").plus(arrTime[1])
            println("${sharedVM.users.value}")
            val sender = sharedVM.users.value!![mes.from]

            textMsg.text = mes.text
            dateMsg.text = effDate
            timeMsg.text = effTime
            nickname.text = sender!!.nickname
            if(mes.from != sharedVM.currentUser.value!!.email){
                sharedVM.readMessage(mes.idMsg)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(sharedVM.currentUser.value!!.email == messageList[position].from)
            return Const.MYMESSAGE
        else return Const.OTHERMESSAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == Const.MYMESSAGE){
            val vg = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_chat_me, parent, false)
            MyMessageViewHolder(vg)
        }
        else{
            val vg = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_chat_other, parent, false)
            OtherMessageViewHolder(vg)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if( getItemViewType(position) == Const.MYMESSAGE){
            (holder as MyMessageViewHolder).bind(messageList[position])
        }
        else{
            (holder as OtherMessageViewHolder).bind(messageList[position])
        }

    }

}
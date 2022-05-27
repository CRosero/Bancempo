package com.bancempo.data

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bancempo.R
import com.bancempo.Skill

data class Conversation(val idConv: String,
                        val idAdv: String,
                        val idAsker: String,
                        val idBidder: String,
                        val approved: Boolean
                        )


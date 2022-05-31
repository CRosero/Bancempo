package com.bancempo.data

data class Conversation(val idConv: String,
                        val idAdv: String,
                        val idAsker: String,
                        val idBidder: String,
                        val closed: Boolean
)

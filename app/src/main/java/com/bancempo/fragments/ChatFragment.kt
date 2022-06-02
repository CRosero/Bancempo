package com.bancempo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.*
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bancempo.R
import com.bancempo.data.Conversation
import com.bancempo.data.MessageAdapter
import com.bancempo.data.User
import com.bancempo.models.SharedViewModel


class ChatFragment : Fragment(R.layout.fragment_chat) {
    private val sharedVM: SharedViewModel by activityViewModels()

    private var currentConversation: Conversation? = null

    private lateinit var title: String
    private lateinit var idAdv: String
    private lateinit var idBidder: String
    private lateinit var duration: String

    private lateinit var sendButton: Button

    private lateinit var textAcceptOrRefuse: TextView
    private lateinit var acceptButton: ImageButton
    private lateinit var refuseButton: ImageButton
    private lateinit var tvNoCredit: TextView

    private lateinit var textMsg: EditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.recycler_gchat)
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter =
            MessageAdapter(listOf(), sharedVM)


        title = arguments?.getString("title")!!
        idAdv = arguments?.getString("idAdv")!!
        idBidder = arguments?.getString("idBidder")!!
        duration = arguments?.getString("duration")!!

        tvNoCredit = view.findViewById(R.id.tvNoCredit)
        tvNoCredit.visibility = View.GONE


        sendButton = view.findViewById(R.id.button_gchat_send)
        textMsg = view.findViewById(R.id.edit_gchat_message)

        textAcceptOrRefuse = view.findViewById(R.id.textAcceptOrRefuse)
        acceptButton = view.findViewById<ImageButton>(R.id.acceptProposal)
        refuseButton = view.findViewById<ImageButton>(R.id.refuseProposal)


        //Appena apro la chat se non è una conversazione nuova devo caricare i messaggi
        val createNewConv = arguments?.getBoolean("newConv")!!
        if(!createNewConv){
            currentConversation = sharedVM.conversations.value!!.values.filter { conv -> conv.idAdv == idAdv && !conv.closed }.get(0)
            sharedVM.loadMessages(currentConversation!!.idConv)
        }
        else{
            //CANCELLARE MESSAGGI

        }

        //Nei miei annunci posso accettare/rifiutare la richiesta quindi compaiono i bottoni
        if(idBidder == sharedVM.currentUser.value!!.email){
            textAcceptOrRefuse.visibility = View.VISIBLE
            acceptButton.visibility = View.VISIBLE
            refuseButton.visibility = View.VISIBLE
        }
        else{
            textAcceptOrRefuse.visibility = View.GONE
            acceptButton.visibility = View.GONE
            refuseButton.visibility = View.GONE
        }

//Se l'annuncio è booked non posso visualizzare i bottoni accept/reject
        sharedVM.bookedAdvs.observe(viewLifecycleOwner){ advs ->
            val filtered = advs.values.filter { adv -> adv.id == idAdv}
            if(filtered.isNotEmpty()){
                textAcceptOrRefuse.visibility = View.GONE
                acceptButton.visibility = View.GONE
                refuseButton.visibility = View.GONE
            }
        }

        sendButton.setOnClickListener{
            //Quando clicco sul sendButton, se la conversazione non esiste la creo, setto la current conv e visualizzo il messaggio nuovo
            if(currentConversation == null){
                if(textMsg.text.isEmpty()){
                    textMsg.error = "Write a message!"
                }
                else{
                    sharedVM.createNewConversation(idAdv, idBidder, textMsg.text.toString())

                    sharedVM.conversations.observe(viewLifecycleOwner) { convs ->
                        println("------------ ${sharedVM.conversations.value}}")
                        val filteredConvs =
                            convs.values.filter { conv -> conv.idAdv == idAdv && !conv.closed }

                        if(filteredConvs.isNotEmpty()){
                            currentConversation = filteredConvs.get(0)
                            sharedVM.loadMessages(currentConversation!!.idConv)
                            textMsg.setText("")
                        }
                    }
                }
            }
            else{
                //Se la conversazione esiste già aggiungo solo il nuovo messaggio e lo visualizzo
                if (textMsg.text.isEmpty()) {
                    textMsg.error = "Write a message!"
                } else {
                    //Se il messaggio nuovo arriva dall'asker verso il bidder
                        if(idBidder != sharedVM.currentUser.value!!.email) {
                            sharedVM.createNewMessage(
                                currentConversation!!.idConv,
                                textMsg.text.toString(),
                                to = idBidder,
                                from = sharedVM.currentUser.value!!.email
                            )
                        }
                    else{
                            sharedVM.createNewMessage(
                                currentConversation!!.idConv,
                                textMsg.text.toString(),
                                to = currentConversation!!.idAsker,
                                from = sharedVM.currentUser.value!!.email
                            )
                        }
                    sharedVM.loadMessages(currentConversation!!.idConv)
                    textMsg.setText("")
                }
            }
        }


        var idConv: String? = null
        var idAsker : String? = null
        var asker : User? = null
        val _convs = sharedVM.conversations.value!!.values.filter { x -> !x.closed && x.idAdv == idAdv}
        if( _convs.isNotEmpty()) {
            idConv = _convs[0].idConv
            idAsker = _convs[0].idAsker
            asker = sharedVM.users.value!!.get(idAsker)
        }

        val amountOfTime = duration.toDouble()
            acceptButton.setOnClickListener{
            println("now: setta $idAdv come accepted")

            if(asker != null && asker.credit > amountOfTime){
                sharedVM.bookAdv(idAdv)
                sharedVM.createNewTransaction(idBidder, idAsker!!, amountOfTime)
                acceptButton.visibility = View.GONE
                refuseButton.visibility = View.GONE
                textAcceptOrRefuse.visibility = View.GONE
                tvNoCredit.visibility = View.GONE

                findNavController().popBackStack()
            }
            else{
                acceptButton.isEnabled = false
                tvNoCredit.visibility = View.VISIBLE
                //TODO: cosa fare se non ha soldi?
            }

        }

        refuseButton.setOnClickListener{
            println("now: setta $idConv")
            sharedVM.closeConversation(idConv!!)
            acceptButton.visibility = View.GONE
            refuseButton.visibility = View.GONE
            textAcceptOrRefuse.visibility = View.GONE

            findNavController().popBackStack()

        }

        sharedVM.messages.observe(viewLifecycleOwner) { messagesConv ->
            if (currentConversation != null) {
                messagesConv.values.filter { msg -> msg.idConv == currentConversation!!.idConv }
                rv.adapter =
                    MessageAdapter(messagesConv.values.sortedBy { x -> x.date }.toList(), sharedVM)
            }
            else{
                rv.adapter =
                    MessageAdapter(listOf(), sharedVM)
            }
        }

    }

}
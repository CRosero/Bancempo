package com.bancempo.models

import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.getBitmap
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.bancempo.R
import com.bancempo.data.Skill
import com.bancempo.data.SmallAdv
import com.bancempo.data.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap


class SharedViewModel(private val app: Application) : AndroidViewModel(app) {
    private lateinit var auth: FirebaseAuth
    private var userState: FirebaseUser? = null
    private val rootStorageDirectory = "gs://bancempo.appspot.com/"
    private val db = FirebaseFirestore.getInstance()
    private val storageReference = FirebaseStorage.getInstance()

    val haveIloadNewImage = MutableLiveData(true)

    val authUser: MutableLiveData<FirebaseUser?> by lazy {
        MutableLiveData<FirebaseUser?>().also {
            auth = Firebase.auth
            auth.addAuthStateListener { authState ->
                userState = authState.currentUser
                authUser.value = userState
            }
        }
    }

    val currentUser: MutableLiveData<User> by lazy {
        MutableLiveData<User>().also {
            if (authUser.value != null) {
                loadUser(authUser.value!!.email!!)
            }
        }
    }

    val services: MutableLiveData<HashMap<String, Skill>> by lazy {
        MutableLiveData<HashMap<String, Skill>>().also {
            if (authUser.value != null)
                loadServices()
        }
    }

    val myAdvs: MutableLiveData<HashMap<String, SmallAdv>> by lazy {
        MutableLiveData<HashMap<String, SmallAdv>>().also {
            if (authUser.value != null) {
                loadMyAdvs(authUser.value!!.email!!)
            }
        }
    }

    val advs: MutableLiveData<HashMap<String, SmallAdv>> by lazy {
        MutableLiveData<HashMap<String, SmallAdv>>().also {
            if (authUser.value != null)
                loadAdvs()
        }
    }

    val bookedAdvs: MutableLiveData<HashMap<String, SmallAdv>> by lazy {
        MutableLiveData<HashMap<String, SmallAdv>>().also {
            if (authUser.value != null)
                loadBookedAdvs()
        }
    }

    val conversations: MutableLiveData<HashMap<String, Conversation>> by lazy {
        MutableLiveData<HashMap<String, Conversation>>().also {
            if (authUser.value != null) {
                loadConversations()
            }
        }
    }

    val messages: MutableLiveData<HashMap<String, Message>> by lazy {
        MutableLiveData<HashMap<String, Message>>().also {
        }
    }

    val users: MutableLiveData<HashMap<String, User>> by lazy {
        MutableLiveData<HashMap<String, User>>().also {
            if (authUser.value != null)
                loadUsers()
        }
    }

    val ratings: MutableLiveData<HashMap<String, Rating>> by lazy {
        MutableLiveData<HashMap<String, Rating>>().also {
            if (authUser.value != null) {
                loadAllRatings()
            }
        }
    }

    val myReceivedRatings: MutableLiveData<HashMap<String, Rating>> by lazy {
        MutableLiveData<HashMap<String, Rating>>().also {
            if (authUser.value != null) {
                loadMyRatings(authUser.value!!.email!!)
            }
        }
    }

    fun afterLogin(currentUser: FirebaseUser) {
        val email = currentUser.email!!
        loadUser(email)
        loadMyAdvs(email)
        loadConversations()
        loadServices()
        loadUsers()
        loadAdvs()
        loadBookedAdvs()
        loadMyRatings(email)
        loadAllRatings()
    }

    fun uploadBitmap(
        btmStr: String?,
        view: View,
        skillsString: String,
        uriStr: Uri?,
        contentResolver: ContentResolver?
    ) {

        val user = currentUser.value!!
        val email = user.email
        val toDelete = user.imageUser

        val creationTimeNewImage = System.currentTimeMillis()
        val emailTruncated = email.split("@")[0]
        val imageName = "profile_".plus(creationTimeNewImage.toString()).plus(".jpg")
        val url = "$rootStorageDirectory/$emailTruncated/$imageName"
        val myNewRef = storageReference.getReferenceFromUrl(url)
        val baos = ByteArrayOutputStream()

        val btm: Bitmap
        if (btmStr != null){
            btm = stringToBitmap(btmStr)!!
        }
        else if( uriStr != null){
            btm = updateProfilePictureFromURI(uriStr, contentResolver)
        }
        else{
            return
        }
        btm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()


        myNewRef.putBytes(data).addOnSuccessListener {
            db.collection("users").document(email)
                .update("imageUser", myNewRef.toString())
                .addOnSuccessListener {
                    updateUser(view, skillsString, true)
                    if (toDelete.isNotEmpty()) {
                        val myOldRef = storageReference.getReferenceFromUrl(toDelete)
                        myOldRef.delete()
                            .addOnFailureListener {
                                Toast.makeText(
                                    app.applicationContext,
                                    "There was a problem on deleting old Image",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }

                }
                .addOnFailureListener {
                    Toast.makeText(
                        app.applicationContext,
                        "There was a problem on deleting old Image",
                        Toast.LENGTH_SHORT
                    ).show()
                }

        }.addOnFailureListener {
            Toast.makeText(
                app.applicationContext,
                "There was a problem on adding new Image",
                Toast.LENGTH_SHORT
            ).show()
        }

    }


    fun loadImageUserById(userId: String, view: View) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                val imageUser = doc!!.getString("imageUser")
                if (imageUser != "") {
                    val ref = storageReference.getReferenceFromUrl(imageUser!!)
                    val smallAdvIV = view.findViewById<ImageView>(R.id.smallAdv_image)
                    Glide.with(app.applicationContext).load(ref)
                        .into(smallAdvIV)
                }
            }
    }

    fun loadImageUser(iv: ImageView, view: View, user: User) {
        if (user.imageUser != "") {
            val myRef = storageReference.getReferenceFromUrl(user.imageUser)
            val pb = view.findViewById<ProgressBar>(R.id.progressBar)
            if (pb != null)
                pb.visibility = View.VISIBLE

            Glide.with(app.applicationContext).load(myRef)
                .listener(object : RequestListener<Drawable> {
                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        if (pb != null)
                            pb.visibility = View.GONE
                        iv.visibility = View.VISIBLE
                        return false
                    }

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        iv.visibility = View.VISIBLE
                        return false
                    }
                }
                )
                .circleCrop()
                .into(iv)
        } else {
            val idDrawable = app.resources.getDrawable(R.drawable.ic_icons8_image_501)
            iv.setImageDrawable(idDrawable)
        }
    }

    fun updateUser(view: View, skillsString: String, updatingImg: Boolean) {
        val currentUserRef = db.collection("users").document(currentUser.value!!.email)
        val servicesDocRef = db.collection("services")
        val advsDocRef = db.collection("advertisements")
        val convsDocRef = db.collection("conversations")

        val fullname = view.findViewById<TextInputEditText>(R.id.editTextFullName).text.toString()
        val nickname = view.findViewById<TextInputEditText>(R.id.editTextNickname).text.toString()
        val location = view.findViewById<TextInputEditText>(R.id.editTextLocation).text.toString()
        val description =
            view.findViewById<TextInputEditText>(R.id.editTextDescription).text.toString()

        val finalList = mutableListOf<String>()
        for (skill in skillsString.split(",")) {
            if (skill != "")
                finalList.add(skill)
        }

        if (!updatingImg) {
            haveIloadNewImage.value = false
        }

        servicesDocRef
            .whereEqualTo("createdBy", currentUser.value!!.email)
            .get()
            .addOnSuccessListener { r ->
                val initialList = mutableListOf<String>()
                for (doc in r!!) {
                    initialList.add(doc.id)
                }
                val toAdd = finalList.minus(initialList.toSet())
                val toDelete = initialList.minus(finalList.toSet())

                val user = User(
                    fullname,
                    nickname,
                    description,
                    location,
                    currentUser.value!!.email,
                    finalList,
                    currentUser.value!!.imageUser,
                    currentUser.value!!.credit,
                    currentUser.value!!.rating,
                )



                //elimina solo gli annunci che non sono prenotati,
                //quelli booked restano con una skill "pendente"
                val advsToDelete = advs.value!!.values
                    .filter { x ->
                        x.userId == currentUser.value!!.email
                                && containsSkill(toDelete, x.skill.split(","))
                                && !x.booked
                    }
                    .toList()

                //dobbiamo eliminare anche le relative conversazioni con i vari messaggi
                val convsToDelete = mutableListOf<Conversation>()
                for (advtoDel in advsToDelete) {
                    //elimino tutte le conversazioni a chiuse relative agli annunci da eliminare
                    convsToDelete.addAll(conversations.value!!.values.filter { x -> x.idAdv == advtoDel.id })
                }

                db.runBatch { batch ->
                    //1st: create new service for each element of addingList
                    for (adding in toAdd) {
                        batch.set(
                            servicesDocRef.document(adding),
                            createService(adding, currentUser.value!!.email)
                        )
                    }

                    //2nd: delete old services
                    var newCreatorId: String?
                    for (deleting in toDelete) {
                        val skillToDelete = services.value!![deleting]
                        if (skillToDelete != null) {
                            //se tra tutti gli advs che non sono creati da me, ce ne è almeno uno di questa skill
                            val advsNotCreatedByMeAssociatedToSkill =
                                advs.value!!.values.filter { x ->
                                    x.userId != currentUser.value!!.email &&
                                            x.skill.split(",").contains(skillToDelete.title)
                                }.toList()

                            if (advsNotCreatedByMeAssociatedToSkill.isNotEmpty()) {
                                newCreatorId = advsNotCreatedByMeAssociatedToSkill[0].userId

                                //update skill con questo creatore
                                batch.update(
                                    servicesDocRef.document(deleting),
                                    "createdBy",
                                    newCreatorId
                                )
                            } else {
                                //elimina skill
                                batch.delete(servicesDocRef.document(deleting))
                            }
                        }
                    }
                    //in ogni caso elimina tutti i miei annunci associati a questa skill

                    for (advToDelete in advsToDelete) {
                        val docToDel = advsDocRef.document(advToDelete.id)
                        batch.delete(docToDel)
                    }

                    for (convToDel in convsToDelete) {
                        val docToDel = convsDocRef.document(convToDel.idConv)
                        batch.delete(docToDel)
                        deleteMessageOfConv(convToDel.idConv)
                    }

                    //3rd: replace list of skills in user
                    batch.set(currentUserRef, user)
                }.addOnSuccessListener {
                    if (updatingImg) {
                        haveIloadNewImage.value = true
                        Toast.makeText(
                            app.applicationContext,
                            R.string.adv_edit_succ,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }

            }

    }

    private fun containsSkill(listToDelete: List<String>, skillsOfAdv: List<String>): Boolean {
        for (del in listToDelete) {
            if (skillsOfAdv.contains(del)) {
                return true
            }
        }
        return false
    }

    fun createUserIfDoesNotExists() {
        db.collection("users")
            .whereEqualTo("email", authUser.value!!.email)
            .get()
            .addOnSuccessListener { r ->
                if (r.isEmpty) {
                    val newUser = User(
                        authUser.value!!.displayName!!,
                        authUser.value!!.displayName!!,
                        "",
                        "",
                        authUser.value!!.email!!,
                        listOf(),
                        "",
                        5.0,
                        0.0,
                    )
                    db.collection("users").document(authUser.value!!.email!!)
                        .set(newUser)
                        .addOnSuccessListener {
                            currentUser.value = newUser
                        }
                        .addOnFailureListener {
                        }
                }
            }
            .addOnFailureListener {
            }
    }

    private fun loadUser(mail: String) {
        db.collection("users")
            .whereEqualTo("email", mail)
            .addSnapshotListener { r, e ->
                if (e != null)
                    currentUser.value = User()
                else {
                    for (doc in r!!) {
                        val nickname = doc.getString("nickname")
                        val fullname = doc.getString("fullname")
                        val email = doc.getString("email")
                        val location = doc.getString("location")
                        val description = doc.getString("description")
                        val listOfSkills = doc.data["skills"] as List<String>
                        val imageUser = doc.getString("imageUser")
                        val credit = doc.getDouble("credit") as Double
                        val rating = doc.getDouble("rating") as Double

                        val user = User(
                            fullname!!, nickname!!, description!!, location!!,
                            email!!, listOfSkills, imageUser!!, credit, rating
                        )

                        currentUser.value = user
                    }
                }
            }
    }


    private fun loadServices() {
        db.collection("services")
            .addSnapshotListener { r, e ->
                if (e != null)
                    services.value = hashMapOf()
                else {
                    val serviceMap: HashMap<String, Skill> = hashMapOf()
                    for (doc in r!!) {
                        val title = doc.getString("title")
                        val creationTime = doc.getString("creationTime")
                        val createdBy = doc.getString("createdBy")
                        val service = Skill(title!!, creationTime!!, createdBy!!)
                        serviceMap[doc.id] = service
                    }
                    services.value = serviceMap
                }
            }
    }

    private fun createService(title: String, createdBy: String): Skill {
        return Skill(
            title,
            getCreationTime(),
            createdBy
        )
    }


    private fun loadMyAdvs(userId: String) {
        db.collection("advertisements")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { r, e ->
                if (e != null) {
                    myAdvs.value = hashMapOf()
                } else {
                    val advMap: HashMap<String, SmallAdv> = hashMapOf()
                    for (doc in r!!) {
                        val date = doc.getString("date")
                        val description = doc.getString("description")
                        val duration = doc.getString("duration")
                        val location = doc.getString("location")
                        val note = doc.getString("note")
                        val time = doc.getString("time")
                        val title = doc.getString("title")
                        val creationTime = doc.getString("creationTime")
                        val skill = doc.getString("skill")
                        val uid = doc.getString("userId")
                        val booked = doc.getBoolean("booked")
                        val adv = SmallAdv(
                            doc.id,
                            title!!,
                            date!!,
                            description!!,
                            time!!,
                            duration!!,
                            location!!,
                            note!!,
                            creationTime!!,
                            skill!!,
                            uid!!,
                            booked!!,
                        )
                        advMap[doc.id] = adv
                    }
                    myAdvs.value = advMap
                }
            }
    }

    private fun loadAdvs() {
        db.collection("advertisements")
            .whereEqualTo("booked", false)
            .orderBy("creationTime", Query.Direction.DESCENDING)
            .addSnapshotListener { r, e ->
                if (e == null) {
                    val advMap: HashMap<String, SmallAdv> = hashMapOf()
                    for (doc in r!!) {
                        val date = doc.getString("date")
                        val description = doc.getString("description")
                        val duration = doc.getString("duration")
                        val location = doc.getString("location")
                        val note = doc.getString("note")
                        val time = doc.getString("time")
                        val title = doc.getString("title")
                        val creationTime = doc.getString("creationTime")
                        val skill = doc.getString("skill")
                        val userId = doc.getString("userId")
                        val booked = doc.getBoolean("booked")

                        val adv = SmallAdv(
                            doc.id,
                            title!!,
                            date!!,
                            description!!,
                            time!!,
                            duration!!,
                            location!!,
                            note!!,
                            creationTime!!,
                            skill!!,
                            userId!!,
                            booked!!,
                        )
                        advMap[doc.id] = adv
                    }
                    advs.value = advMap
                }
            }
    }


    private fun loadBookedAdvs() {
        db.collection("advertisements")
            .whereEqualTo("booked", true)
            .orderBy("creationTime", Query.Direction.DESCENDING)
            .addSnapshotListener { r, e ->
                if (e == null) {
                    val advMap: HashMap<String, SmallAdv> = hashMapOf()
                    for (doc in r!!) {
                        val date = doc.getString("date")
                        val description = doc.getString("description")
                        val duration = doc.getString("duration")
                        val location = doc.getString("location")
                        val note = doc.getString("note")
                        val time = doc.getString("time")
                        val title = doc.getString("title")
                        val creationTime = doc.getString("creationTime")
                        val skill = doc.getString("skill")
                        val userId = doc.getString("userId")
                        val booked = doc.getBoolean("booked")

                        val adv = SmallAdv(
                            doc.id,
                            title!!,
                            date!!,
                            description!!,
                            time!!,
                            duration!!,
                            location!!,
                            note!!,
                            creationTime!!,
                            skill!!,
                            userId!!,
                            booked!!,
                        )
                        advMap[doc.id] = adv
                    }
                    bookedAdvs.value = advMap
                }
            }
    }


    private fun createAdvFromBundle(bundle: Bundle, id: String): SmallAdv {
        val title = bundle.getString("title") ?: ""
        val date = bundle.getString("date") ?: ""
        val description = bundle.getString("description") ?: ""
        val timeslot = bundle.getString("time") ?: ""
        val duration = bundle.getString("duration") ?: ""
        val location = bundle.getString("location") ?: ""
        val note = bundle.getString("note") ?: ""
        val skill = bundle.getString("skill") ?: ""
        val userId = bundle.getString("userId") ?: ""

        return SmallAdv(
            id,
            title,
            date,
            description,
            timeslot,
            duration,
            location,
            note,
            getCreationTime(),
            skill,
            userId,
            false,
        )
    }

    private fun getCreationTime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS")
        return current.format(formatter)
    }

    fun addNewAdv(newAdvBundle: Bundle) {

        val userId = authUser.value!!.email
        newAdvBundle.putString("userId", userId)

        val newId = db.collection("advertisements").document().id
        db.collection("advertisements").document(newId)
            .set(createAdvFromBundle(newAdvBundle, newId))
            .addOnSuccessListener {
                Toast.makeText(app.applicationContext, R.string.adv_create_succ, Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(app.applicationContext, "Error", Toast.LENGTH_SHORT).show()
            }
    }

    fun modifyAdv(id: String, advBundle: Bundle) {
        val userId = authUser.value!!.email
        advBundle.putString("userId", userId)


        db.collection("advertisements").document(id)
            .set(createAdvFromBundle(advBundle, id))
            .addOnSuccessListener {
                Toast.makeText(app.applicationContext, R.string.adv_edit_succ, Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(app.applicationContext, "Error", Toast.LENGTH_SHORT).show()
            }

    }

    private fun loadConversations() {
        db.collection("conversations")
            .addSnapshotListener { r, e ->
                if (e != null) {
                    conversations.value = hashMapOf()
                } else {
                    val convsMap: HashMap<String, Conversation> = hashMapOf()
                    for (doc in r!!) {
                        val idConv = doc.getString("idConv")
                        val idAdv = doc.getString("idAdv")
                        val idAsker = doc.getString("idAsker")
                        val idBidder = doc.getString("idBidder")
                        val closed = doc.getBoolean("closed")
                        val conversation =
                            Conversation(idConv!!, idAdv!!, idAsker!!, idBidder!!, closed!!)
                        convsMap[idConv] = conversation
                    }
                    conversations.value = convsMap
                }
            }
    }

    fun createNewConversation(idAdv: String, idBidder: String, text: String) {
        val newId = db.collection("conversations").document().id
        val newConv = Conversation(newId, idAdv, currentUser.value!!.email, idBidder, false)
        db.collection("conversations").document(newId)
            .set(newConv)
            .addOnSuccessListener {
                createNewMessage(newId, text, to = idBidder, from = currentUser.value!!.email)
            }
            .addOnCanceledListener {
            }
    }

    fun createNewMessage(idConv: String, text: String, from: String, to: String) {
        val date = getCreationTime()
        val newId = db.collection("messages").document().id
        val newMsg = Message(newId, idConv, date, text, from, to, false)

        db.collection("messages").document(newId)
            .set(newMsg)
            .addOnSuccessListener {
            }
            .addOnCanceledListener {
            }
    }

    fun loadMessages(idConv: String) {
        db.collection("messages")
            .whereEqualTo("idConv", idConv)
            .addSnapshotListener { r, e ->
                if (e != null)
                    messages.value = hashMapOf()
                else {
                    val msgsMap: HashMap<String, Message> = hashMapOf()
                    for (doc in r!!) {
                        val idMsg = doc.getString("idMsg")
                        val date = doc.getString("date")
                        val text = doc.getString("text")
                        val from = doc.getString("from")
                        val to = doc.getString("to")
                        val readed = doc.getBoolean("readed")
                        val msg = Message(idMsg!!, idConv, date!!, text!!, from!!, to!!, readed!!)
                        msgsMap[doc.id] = msg
                    }
                    messages.value = msgsMap
                }
            }
    }

    fun bookAdv(idAdv: String) {
        db.collection("advertisements").document(idAdv)
            .update("booked", true)
            .addOnSuccessListener {
                Toast.makeText(app.applicationContext, "Booked!", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(app.applicationContext, "Error", Toast.LENGTH_SHORT).show()
            }
    }

    fun closeConversation(idConv: String) {
        db.collection("conversations").document(idConv)
            .update("closed", true)
            .addOnSuccessListener {
                Toast.makeText(app.applicationContext, "Closed!", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(app.applicationContext, "Error", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUsers() {
        db.collection("users")
            .addSnapshotListener { r, e ->
                if (e != null)
                    users.value = hashMapOf()
                else {
                    val usersMap: HashMap<String, User> = hashMapOf()
                    for (doc in r!!) {
                        val nickname = doc.getString("nickname")
                        val fullname = doc.getString("fullname")
                        val email = doc.getString("email")
                        val location = doc.getString("location")
                        val description = doc.getString("description")
                        val listOfSkills = doc.data["skills"] as List<String>
                        val imageUser = doc.getString("imageUser")
                        val credit = doc.getDouble("credit") as Double
                        val rating = doc.getDouble("rating") as Double
                        val user = User(
                            fullname!!, nickname!!, description!!, location!!,
                            email!!, listOfSkills, imageUser!!, credit, rating
                        )

                        usersMap[doc.id] = user
                    }
                    users.value = usersMap
                }
            }
    }

    fun readMessage(idMess: String) {
        db.collection("messages")
            .document(idMess)
            .update("readed", true)
            .addOnSuccessListener {
            }
    }

    fun createNewTransaction(idBidder: String, idAsker: String, amountOfTime: Double) {
        val askerDocRef = db.collection("users").document(idAsker)
        val bidderDocRef = db.collection("users").document(idBidder)

        val creditAsker = users.value!![idAsker]!!.credit
        val creditBidder = users.value!![idBidder]!!.credit

        db.runBatch { batch ->
            batch.update(askerDocRef, "credit", creditAsker - amountOfTime)
            batch.update(bidderDocRef, "credit", creditBidder + amountOfTime)
        }
            .addOnSuccessListener {
                Toast.makeText(app.applicationContext, "Transaction Completed!", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(app.applicationContext, "Transaction Failed!", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun deleteMessageOfConv(idConv: String) {
        val messagesDocRef = db.collection("messages")

        messagesDocRef.whereEqualTo("idConv", idConv).get().addOnSuccessListener { documents ->
            for (doc in documents) {
                doc.reference.delete()
            }
        }
    }


    private fun loadAllRatings() {
        db.collection("ratings")
            .orderBy("rating", Query.Direction.DESCENDING)
            .addSnapshotListener { r, e ->
                if (e != null)
                    ratings.value = hashMapOf()
                else {
                    val ratingMap: HashMap<String, Rating> = hashMapOf()
                    for (doc in r!!) {
                        val idAuthor = doc.getString("idAuthor")
                        val idReceiver = doc.getString("idReceiver")
                        val idAdv = doc.getString("idAdv")
                        val rating = doc.getDouble("rating") as Double
                        val ratingText = doc.getString("ratingText")
                        val ratingObj =
                            Rating(idAuthor!!, idReceiver!!, idAdv!!, rating, ratingText!!)
                        ratingMap[doc.id] = ratingObj
                    }
                    ratings.value = ratingMap
                }
            }
    }

    private fun loadMyRatings(userId: String) {
        db.collection("ratings")
            .whereEqualTo("idReceiver", userId)
            .orderBy("rating", Query.Direction.DESCENDING)
            .addSnapshotListener { r, e ->
                if (e != null)
                    ratings.value = hashMapOf()
                else {
                    val ratingMap: HashMap<String, Rating> = hashMapOf()
                    for (doc in r!!) {
                        val idAuthor = doc.getString("idAuthor")
                        val idReceiver = doc.getString("idReceiver")
                        val idAdv = doc.getString("idAdv")
                        val rating = doc.getDouble("rating") as Double
                        val ratingText = doc.getString("ratingText")
                        val ratingObj =
                            Rating(idAuthor!!, idReceiver!!, idAdv!!, rating, ratingText!!)
                        ratingMap[doc.id] = ratingObj
                    }
                    myReceivedRatings.value = ratingMap
                }
            }
    }


    private fun createNewRating(
        idAuthor: String,
        idReceiver: String,
        idAdv: String,
        rating: Double,
        ratingText: String
    ) {
        val newId = db.collection("ratings").document().id
        val newRating = Rating(idAuthor, idReceiver, idAdv, rating, ratingText)
        db.collection("ratings").document(newId)
            .set(newRating)
            .addOnSuccessListener {
            }
            .addOnCanceledListener {
            }
    }

    fun submitNewRating(
        idAuthor: String,
        idReceiver: String,
        idAdv: String,
        advRating: Double,
        advRatingText: String
    ) {
        var amount = 0.0

        val nRatings = ratings.value!!.values.filter { rating ->
            rating.idReceiver == idReceiver
        }.map { rating ->
            amount += rating.rating
        }.size

        val newRating = (amount + advRating) / (nRatings + 1)

        db.collection("users").document(idReceiver).update("rating", newRating)
        createNewRating(idAuthor, idReceiver, idAdv, advRating, advRatingText)
    }

    private fun stringToBitmap(encodedString: String): Bitmap? {
        return try {
            val encodedByte = Base64.getDecoder().decode(encodedString)
            val bitmap = BitmapFactory.decodeByteArray(encodedByte, 0, encodedByte.size)
            bitmap
        } catch (e: Exception) {
            e.message
            null
        }
    }

    private fun updateProfilePictureFromURI(uri: Uri, contentResolver: ContentResolver?): Bitmap {
        val bmp: Bitmap = getBitmap(contentResolver, uri)
        val ins: InputStream? = contentResolver?.openInputStream(uri)

        val ei = ExifInterface(ins!!)
        val rotatedBitmap: Bitmap = when (ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bmp, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bmp, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bmp, 270f)
            ExifInterface.ORIENTATION_NORMAL -> bmp
            else -> bmp
        }

        ins.close()
        return rotatedBitmap
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)

        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

}


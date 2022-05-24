package com.bancempo.models

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.bancempo.R
import com.bancempo.Skill
import com.bancempo.SmallAdv
import com.bancempo.data.User
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class SharedViewModel(private val app: Application) : AndroidViewModel(app) {
    private lateinit var auth: FirebaseAuth
    private var userState: FirebaseUser? = null
    val rootStorageDirectory = "gs://bancempo.appspot.com/"
    private val db = FirebaseFirestore.getInstance()
    private val storageReference = FirebaseStorage.getInstance()

    val haveIloadNewImage = MutableLiveData<Boolean>(true)

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
            loadAdvs()
        }
    }

    fun afterLogin() {
        val email = authUser.value!!.email!!
        loadMyAdvs(email)
        loadServices()
        loadUser(email)
    }

    fun uploadBitmap(btm: Bitmap, view: View, skillsString: String) {
        //println("bitmap: upload")
        val creationTimeNewImage = System.currentTimeMillis()
        val emailTruncated = currentUser.value!!.email.split("@")[0]
        val imageName = "profile_".plus(creationTimeNewImage.toString()).plus(".jpg")
        val url = "$rootStorageDirectory/$emailTruncated/$imageName"
        val toDelete = currentUser.value!!.imageUser.isNotEmpty()
        val myNewRef = storageReference.getReferenceFromUrl(url)

        val baos = ByteArrayOutputStream()
        btm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        myNewRef.putBytes(data).addOnSuccessListener {
            if (toDelete) {
                val myOldRef = storageReference.getReferenceFromUrl(currentUser.value?.imageUser!!)
                myOldRef.delete().addOnSuccessListener {
                    db.collection("users").document(authUser.value!!.email!!)
                        .update("imageUser", myNewRef.toString())
                        .addOnSuccessListener {

                            val photo = view.findViewById<ImageView>(R.id.profile_pic)
                            photo.setImageBitmap(btm)
                            updateUser(view, skillsString, true)
                        }
                        .addOnFailureListener { println("--------------failing updating user") }
                }.addOnFailureListener {
                    println("---------------------not ok: $it")
                }
            } else {
                db.collection("users").document(authUser.value!!.email!!)
                    .update("imageUser", myNewRef.toString())
                    .addOnSuccessListener { println("----------------update user") }
                    .addOnFailureListener { println("---------------failing updating user") }
            }

        }.addOnFailureListener {
            Toast.makeText(app.applicationContext, "----------------Error", Toast.LENGTH_SHORT)
                .show()
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

    @SuppressLint("UseCompatLoadingForDrawables")
    fun loadImageUser(iv: ImageView, view: View) {
        println(currentUser.value?.imageUser)
        if (currentUser.value?.imageUser != "") {
            val myRef = storageReference.getReferenceFromUrl(currentUser.value?.imageUser!!)
            println("bitmap: load")
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
                        println("bitmap: ready!")
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
                        println("bitmap: failed!")
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

    fun loadSpinner(view: ImageView) {
        Glide.with(app.applicationContext)
            .load(R.drawable.loader)
            .circleCrop()
            .into(view)
    }

    fun updateUser(view: View, skillsString: String, updatingImg: Boolean) {
        val currentUserRef = db.collection("users").document(currentUser.value!!.email)
        val servicesDocRef = db.collection("services")
        val advsDocRef = db.collection("advertisements")
        val fullname = view.findViewById<TextInputEditText>(R.id.editTextFullName).text.toString()
        val nickname = view.findViewById<TextInputEditText>(R.id.editTextNickname).text.toString()
        val email = view.findViewById<TextInputEditText>(R.id.editTextEmail).text.toString()
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
                val toAdd = finalList.minus(initialList)
                val toDelete = initialList.minus(finalList)

                val user = User(
                    fullname,
                    nickname,
                    description,
                    location,
                    email,
                    finalList,
                    currentUser.value!!.imageUser
                )

                println("chip: -> myadv ${myAdvs.value!!.values}")
                println("chip: -> idToDel $toDelete")
                val advsToDelete = myAdvs.value!!.values
                    .filter { x -> containsSkill(toDelete, x.skill.split(",")) }
                    .toList()
                println("chip: todelete -> ${advsToDelete.map { x -> x.id }}")


                db.runBatch { batch ->
                    //1st: create new service for each element of addingList
                    for (adding in toAdd) {
                        batch.set(
                            servicesDocRef.document(adding),
                            createService(adding, currentUser.value!!.email)
                        )
                    }

                    //2nd: delete old services
                    var newCreatorId: String? = null
                    for (deleting in toDelete) {
                        val skillToDelete = services.value!!.get(deleting)
                        if (skillToDelete != null) {
                            //se tra tutti gli advs che non sono creati da me, ce ne è almeno uno di questa skill
                            advs.value!!.values.map { x ->
                                println(
                                    "adv: ${x.title}, skills: ${
                                        x.skill.split(
                                            ","
                                        )
                                    }"
                                )
                            }
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
                        println("batch: prova $advToDelete")
                        val docToDel = advsDocRef.document(advToDelete.id)
                        println("batch: docref${docToDel.path}")
                        batch.delete(docToDel)
                    }

                    //3rd: replace list of skills in user
                    batch.set(currentUserRef, user)
                }.addOnSuccessListener {
                    if (updatingImg) {
                        println("bitmap: end upload")
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
        Log.d("sharedVM", "--------------------create user")
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
                        ""
                    )
                    db.collection("users").document(authUser.value!!.email!!)
                        .set(newUser)
                        .addOnSuccessListener {
                            currentUser.value = newUser
                            println("user creato nel db")
                        }
                        .addOnFailureListener {
                            println("impossibile creare user nel db")
                        }
                }
            }
            .addOnFailureListener {
                println("problema nel trovare lo user con questa email")
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
                        val user = User(
                            fullname!!, nickname!!, description!!, location!!,
                            email!!, listOfSkills, imageUser!!
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
                    println("--- ERR ${e.message.toString()}")
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
                            uid!!
                        )
                        advMap[doc.id] = adv
                    }
                    myAdvs.value = advMap
                }
            }
    }

    private fun loadAdvs() {
        db.collection("advertisements")
            .orderBy("creationTime", Query.Direction.DESCENDING)
            .addSnapshotListener { r, e ->
                if (e != null)
                    println("--- ERR $e")
                else {
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
                            userId!!
                        )
                        advMap[doc.id] = adv
                    }
                    advs.value = advMap
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

        println("---------userid: $userId")
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
            userId
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


}


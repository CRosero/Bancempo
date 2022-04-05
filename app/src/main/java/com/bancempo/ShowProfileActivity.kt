package com.bancempo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ShowProfileActivity : AppCompatActivity() {
    lateinit var fullName : TextView;
    lateinit var photo : ImageView
    lateinit var nickname : TextView
    lateinit var email : TextView
    lateinit var location : TextView
    lateinit var skills : TextView
    lateinit var description : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        fullName = findViewById<TextView>(R.id.textViewFullName)
        photo = findViewById<ImageView>(R.id.profile_pic)
        nickname = findViewById<TextView>(R.id.textViewNickname)
        email = findViewById<TextView>(R.id.textViewEmail)
        location = findViewById<TextView>(R.id.textViewLocation)
        skills = findViewById<TextView>(R.id.textViewSkills)
        description = findViewById<TextView>(R.id.textViewDescription)

        if (savedInstanceState != null) {
            //PHOTO: fullName.text = savedInstanceState.getString("full_name");
            fullName.text = savedInstanceState.getString("full_name");
            nickname.text = savedInstanceState.getString("nickname");
            email.text = savedInstanceState.getString("email");
            location.text = savedInstanceState.getString("location");
            skills.text = savedInstanceState.getString("skills")
            description.text = savedInstanceState.getString("description")
            println("restoring from instance state")
        }

        else{
            val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return

            fullName.text = sharedPref.getString(getString(R.string.full_name), "");
            nickname.text = sharedPref.getString(getString(R.string.nickname), "");
            email.text = sharedPref.getString(getString(R.string.email), "");
            location.text = sharedPref.getString(getString(R.string.location), "");
            skills.text = sharedPref.getString(getString(R.string.skills), "");
            description.text = sharedPref.getString(getString(R.string.description), "");

            println(getString(R.string.email))
            println(sharedPref.getString(getString(R.string.email), ""))
            println("loading from sharedPrefs")
        }



    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("full_name", fullName.text.toString())
        outState.putString("nickname", nickname.text.toString())
        outState.putString("email", email.text.toString())
        outState.putString("location", location.text.toString())
        outState.putString("skills", skills.text.toString())
        outState.putString("description", description.text.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //Inflate the menu. this adds items to the action bar if it is present
        menuInflater.inflate(R.menu.menu_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_profile -> {
                editProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editProfile() {
        val i = Intent(this, EditProfileActivity::class.java)
        //i.putExtra("com.bancempo.PHOTO", photo.tag.toString())
        i.putExtra("com.bancempo.FULL_NAME", fullName.text.toString())
        i.putExtra("com.bancempo.NICKNAME", nickname.text.toString())
        i.putExtra("com.bancempo.EMAIL", email.text.toString())
        i.putExtra("com.bancempo.LOCATION", location.text.toString())
        i.putExtra("com.bancempo.SKILLS", skills.text.toString())
        i.putExtra("com.bancempo.DESCRIPTION", description.text.toString())

        startActivityForResult(i, 0)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            //val photo = findViewById<ImageView>(R.id.profile_pic)
            //val fullName =
            findViewById<TextView>(R.id.textViewFullName).setText(data.getStringExtra("com.bancempo.FULL_NAME"))
            //val nickname =
            findViewById<TextView>(R.id.textViewNickname).setText(data.getStringExtra("com.bancempo.NICKNAME"))
            //val email =
            findViewById<TextView>(R.id.textViewEmail).setText(data.getStringExtra("com.bancempo.EMAIL"))
            //val location =
            findViewById<TextView>(R.id.textViewLocation).setText(data.getStringExtra("com.bancempo.LOCATION"))
            //val skills =
            findViewById<TextView>(R.id.textViewSkills).setText(data.getStringExtra("com.bancempo.SKILLS"))
            //val description =
            findViewById<TextView>(R.id.textViewDescription).setText(data.getStringExtra("com.bancempo.DESCRIPTION"))


            //save all the textviews in the shared_preferences file
            val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
            with (sharedPref.edit()) {
                putString(getString(R.string.full_name), findViewById<TextView>(R.id.textViewFullName).text.toString());
                putString(getString(R.string.nickname), findViewById<TextView>(R.id.textViewNickname).text.toString());
                putString(getString(R.string.email), findViewById<TextView>(R.id.textViewEmail).text.toString());
                putString(getString(R.string.location), findViewById<TextView>(R.id.textViewLocation).text.toString());
                putString(getString(R.string.skills), findViewById<TextView>(R.id.textViewSkills).text.toString());
                putString(getString(R.string.description), findViewById<TextView>(R.id.textViewDescription).text.toString());
                apply()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


}
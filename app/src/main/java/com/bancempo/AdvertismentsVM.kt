package com.bancempo

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class AdvertismentsVM(private val app: Application): AndroidViewModel(app) {
    val advs = MutableLiveData<MutableList<SmallAdv>>()

    fun deleteAnAdv(pos: Int){
        println("---------DELETE 4 $pos")

        if(advs.value == null){
            println("------------error")
        }
        else{
            advs.value!!.removeAt(pos)

            val myGson = Gson()
            val jsonAdvList = myGson.toJson(advs.value)

            val mySharedPref = app.applicationContext.getSharedPreferences("advs_list.bancempo.lab3", Context.MODE_PRIVATE)
            with(mySharedPref?.edit()) {
                this?.putString("json_advs_list", jsonAdvList)
            }?.apply()
        }
    }

    fun addNewAdv(newAdv: SmallAdv){
        if(advs.value == null){
            println("------------error")
        }
        else{
            advs.value?.add(0, newAdv)

            val myGson = Gson()
            val jsonAdvList = myGson.toJson(advs.value)

            val mySharedPref = app.applicationContext.getSharedPreferences("advs_list.bancempo.lab3", Context.MODE_PRIVATE)
            with(mySharedPref?.edit()) {
                this?.putString("json_advs_list", jsonAdvList)
            }?.apply()
        }
    }

    fun modifyAdv(modAdv: SmallAdv, pos: Int){
        if(advs.value == null){
            println("------------error")
        }
        else{
            advs.value!![pos] = modAdv

            val myGson = Gson()
            val jsonAdvList = myGson.toJson(advs.value)

            val mySharedPref = app.applicationContext.getSharedPreferences("advs_list.bancempo.lab3", Context.MODE_PRIVATE)

            mySharedPref?.edit()?.clear()?.apply()
            with(mySharedPref?.edit()) {
                this?.putString("json_advs_list", jsonAdvList)
            }?.apply()
        }
    }

    init {
        val gson = Gson()
        val sharedPref = app.getSharedPreferences("advs_list.bancempo.lab3", Context.MODE_PRIVATE)
        if( sharedPref == null ){
            println("-----------create shared pref")
            with(sharedPref?.edit()){
                this?.putString("json_advs_list", "")
            }?.apply()
        }

        val stringJSON:String? = sharedPref?.getString("json_advs_list", "")
        if(stringJSON != null && stringJSON != ""){
            val myType = object : TypeToken<MutableList<SmallAdv>>() {}.type
            advs.value = gson.fromJson(stringJSON, myType)
        }
        else{
            advs.value = mutableListOf()
        }



    }

}
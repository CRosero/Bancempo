package com.bancempo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class ListSkills : Fragment(R.layout.fragment_list_skills) {
    //private val skillVM: SkillVM by activityViewModels()
    private lateinit var llm: LinearLayoutManager
    private lateinit var adapter: ItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.rvSkill)
        rv.layoutManager = LinearLayoutManager(findNavController().context)

        val adapter = ItemAdapter(createItems(20))
        rv.adapter = adapter

    }

    private fun createItems(n: Int): List<Skill_card>{
        val l = mutableListOf<Skill_card>()
        for(i in 1..n)
        {
            val i = Skill_card("Title$i","$i")
            l.add(i)
        }

        return l

    }

   /* private fun createAdvFromBundle(bundle: Bundle) : Skill_card{
        val title = bundle.getString("number") ?: ""
        val numberAdv = bundle.getString("numberAdv") ?: ""

        return Skill_card(
            title,
            numberAdv,

            )
    }
*/




}
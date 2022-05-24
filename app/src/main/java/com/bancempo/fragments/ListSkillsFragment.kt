package com.bancempo.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bancempo.ItemAdapter
import com.bancempo.R
import com.bancempo.models.SharedViewModel


class ListSkillsFragment : Fragment(R.layout.fragment_list_skills) {

    private val sharedVM: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        sharedVM.services.observe(viewLifecycleOwner) { services ->

            val rv = view.findViewById<RecyclerView>(R.id.rvSkill)
            rv.layoutManager = LinearLayoutManager(findNavController().context)

            val adapter = ItemAdapter(services.values.sortedBy { x -> x.title }.toList())
            rv.adapter = adapter

            val sb = view.findViewById<SearchView>(R.id.Search_bar)
            sb.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    val searchListOfSkills = services.values.filter { x ->
                        x.title.lowercase().contains(newText.lowercase())
                    }
                    val newAdapter = ItemAdapter(searchListOfSkills.toList())
                    rv.adapter = newAdapter
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }
            })
        }
    }

}
package com.bancempo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bancempo.data.ItemAdapter
import com.bancempo.R
import com.bancempo.models.SharedViewModel
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import androidx.core.view.isVisible
import com.bancempo.R.id.Search_bar


class ListSkillsFragment : Fragment(R.layout.fragment_list_skills) {

    private val sharedVM: SharedViewModel by activityViewModels()
    private lateinit var emptyListTV: TextView

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_filter, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.search -> {
                val sb = view?.findViewById<SearchView>(
                    Search_bar)
                if (sb != null) {

                    if(sb.isVisible)
                        sb.isVisible=false
                    else
                        sb.isVisible=true

                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)


        setHasOptionsMenu(true)


        sharedVM.services.observe(viewLifecycleOwner) { services ->


            emptyListTV = view.findViewById<TextView>(R.id.empty_list_tv)
            val rv = view.findViewById<RecyclerView>(R.id.rvSkill)
            rv.layoutManager = LinearLayoutManager(findNavController().context)

            val adapter = ItemAdapter(services.values.sortedBy { x -> x.title }.toList())

            if (services.values.isEmpty()) {
                rv.visibility = View.GONE
                emptyListTV.visibility = View.VISIBLE
                emptyListTV.text = getString(R.string.no_skill_at_all)
            } else {
                rv.visibility = View.VISIBLE
                emptyListTV.visibility = View.GONE
            }
            rv.adapter = adapter

            val sb = view.findViewById<SearchView>(Search_bar)
            sb.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    val searchListOfSkills = services.values.filter { x ->
                        x.title.toLowerCase().contains(newText.toLowerCase())
                    }
                    val newAdapter = ItemAdapter(searchListOfSkills.toList())
                    if (searchListOfSkills.isEmpty()) {
                        rv.visibility = View.GONE
                        emptyListTV.visibility = View.VISIBLE
                        emptyListTV.text = getString(R.string.no_skill)
                    } else {
                        rv.visibility = View.VISIBLE
                        emptyListTV.visibility = View.GONE
                    }
                    rv.adapter = adapter
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
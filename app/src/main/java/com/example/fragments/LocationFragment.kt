package com.example.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adaptes.FriendAdapter
import com.example.kotlinmap.R
import com.example.kotlinmap.databinding.FragmentLocationBinding
import com.example.models.FriendRequest
import com.example.views.SearchActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate

class LocationFragment : Fragment() {
    private lateinit var binding: FragmentLocationBinding
    private lateinit var adapter: FriendAdapter
    private var listF = mutableListOf<String>()
    private var database = Firebase.database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchLayout.setOnClickListener {
            startActivity(Intent(context, SearchActivity::class.java))
        }

        initRecyclerView()
        getListFriendById()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_location, container, false)
        var view = binding.root
        return view
    }

    private fun initRecyclerView() {
        var manager: LinearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rcvListFriend.layoutManager = manager
        adapter = FriendAdapter(listF)
        binding.rcvListFriend.adapter = adapter
    }

    private fun getListFriendById() {
        var auth = Firebase.auth
        var uid = auth.currentUser?.uid
        if (uid != null) {
            database.getReference("Friends").child(uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (it in snapshot.children) {
                        var id = it.getValue(String::class.java)
                        if (id != null) {
                            listF.add(id)
                            adapter.setData(listF)
                        }
                        Log.e("h", it.getValue(String::class.java).toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }

}
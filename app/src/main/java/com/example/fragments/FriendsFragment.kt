package com.example.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adaptes.FriendRequestAdapter
import com.example.kotlinmap.R
import com.example.kotlinmap.databinding.FragmentFriendsBinding
import com.example.models.FriendRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FriendsFragment : Fragment() {
    private lateinit var binding: FragmentFriendsBinding
    private val auth = Firebase.auth
    private val database = Firebase.database
    private var listF: MutableList<FriendRequest> = mutableListOf()
    private lateinit var adapter: FriendRequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // hien thi cac loi moi ket ban, cung voi update UI
        initRecyclerView()
        getListFriendRequestOfAnAccount(auth.currentUser?.uid ?: "")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_friends, container, false)
        var view = binding.root
        return view
    }

    private fun initRecyclerView() {
        var manager: LinearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rcvListRequest.layoutManager = manager
        adapter = FriendRequestAdapter(listF)
        binding.rcvListRequest.adapter = adapter
    }

    private fun getListFriendRequestOfAnAccount(id: String) {
        auth.currentUser?.let {
            database.getReference("FriendRequests").child(it.uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        var friendRequest = dataSnapshot.getValue(FriendRequest::class.java)
                        if (friendRequest != null) {
                            listF.add(friendRequest)
                            adapter.setData(listF)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    //TODO("Not yet implemented")
                }

            })
        }
    }

}
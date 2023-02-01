package com.example.adaptes

import android.accounts.Account
import android.content.ClipData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.example.kotlinmap.R
import com.example.models.FriendRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import org.w3c.dom.Text

class FriendRequestAdapter : RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {
    private var listRequest: MutableList<FriendRequest>

    constructor(listRequest: MutableList<FriendRequest>) {
        this.listRequest = listRequest
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_request_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var friendRequest = listRequest.get(position)
        holder.onBind(friendRequest, position)
    }

    override fun getItemCount(): Int {
        return listRequest.size
    }

    fun setData(list: MutableList<FriendRequest>) {
        this.listRequest = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var avatar = itemView.findViewById<ImageView>(R.id.profile_image)
        var name = itemView.findViewById<TextView>(R.id.name)
        var phoneNumber = itemView.findViewById<TextView>(R.id.txt_sodt)
        var acceptButton = itemView.findViewById<Button>(R.id.btn_accept)

        private val database: FirebaseDatabase = Firebase.database
        private val storage: FirebaseStorage = Firebase.storage

        fun onBind(friendRequest: FriendRequest, pos: Int) {
            val ref = database.getReference("Accounts").child(friendRequest.fromId).get()
                .addOnSuccessListener {
                    var account = it.getValue(com.example.models.Account::class.java)
                    if (account != null) {
                        name.text = account.displayName
                        phoneNumber.text = account.phoneNumber
                        storage.getReference("avatar")
                            .child(friendRequest.fromId).downloadUrl.addOnSuccessListener {
                                Glide.with(avatar).load(it).into(avatar)
                            }
                    }
                }

            // chap nhan loi moi ket ban
            acceptButton.setOnClickListener {
                // them vao list ban be cua moi nguoi id cua nguoi do
                database.getReference("Friends").child(friendRequest.toId)
                    .child(friendRequest.fromId).setValue(friendRequest.fromId)
                database.getReference("Friends").child(friendRequest.fromId)
                    .child(friendRequest.toId).setValue(friendRequest.toId)

                // xoa di loi moi ket ban tren list


                // xoa di loi moi ket ban tren db
                database.getReference("FriendRequests").child(friendRequest.toId)
                    .child(friendRequest.fromId).removeValue().addOnSuccessListener {
                       // removeItemAtPosition()
                        listRequest.removeAt(pos)
                        notifyDataSetChanged()
                    }
            }
        }

    }
}
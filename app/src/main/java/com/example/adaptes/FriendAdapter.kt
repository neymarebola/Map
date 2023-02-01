package com.example.adaptes

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlinmap.MapsActivity
import com.example.kotlinmap.R
import com.example.models.Account
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import org.w3c.dom.Text

class FriendAdapter : RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    private lateinit var list: MutableList<String>
    private var database: FirebaseDatabase = Firebase.database
    private var storage: FirebaseStorage = Firebase.storage

    constructor(list: MutableList<String>) {
        this.list = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view =
            LayoutInflater.from(parent.context).inflate(R.layout.friend_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var friend = list.get(position)
        holder.onBind(friend)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setData(list: MutableList<String>) {
        this.list = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var avatar = itemView.findViewById<ImageView>(R.id.iv_profile_image)
        var name = itemView.findViewById<TextView>(R.id.txt_name)

        fun onBind(uid: String) {
            // get name vs avatar from firebase
            database.getReference("Accounts").child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var account = snapshot.getValue(Account::class.java)
                        if (account != null) {
                            name.text = account.displayName
                            storage.getReference("avatar")
                                .child(uid).downloadUrl.addOnSuccessListener {
                                    Glide.with(avatar).load(it).into(avatar)
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

            // bat su kien click vao item tren rcv friend
            itemView.setOnClickListener {
                var intent = Intent(itemView.context, MapsActivity::class.java)
                // gui kem theo id cua ng mk muon xem vi tri
                intent.putExtra("tracker_name", name.text)
                intent.putExtra("tracking_uid", uid)
                itemView.context.startActivity(intent)
            }
        }
    }
}
package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.codingcafe.whatsapp.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

/**
 * A simple [Fragment] subclass.
 */
class ChatsFragment : Fragment() {
    private var PrivateChatsView: View? = null
    private var chatsList: RecyclerView? = null
    private var ChatsRef: DatabaseReference? = null
    private var UsersRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var currentUserID = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        PrivateChatsView = inflater.inflate(R.layout.fragment_chats, container, false)
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth!!.currentUser!!.uid
        ChatsRef = FirebaseDatabase.getInstance().reference.child("Contacts").child(currentUserID)
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")
        chatsList = PrivateChatsView.findViewById<View>(R.id.chats_list) as RecyclerView
        chatsList!!.layoutManager = LinearLayoutManager(context)
        return PrivateChatsView
    }

    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsRef!!, Contacts::class.java)
                .build()
        val adapter: FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> = object : FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            override fun onBindViewHolder(holder: ChatsViewHolder, position: Int, model: Contacts) {
                val usersIDs = getRef(position).key
                val retImage = arrayOf("default_image")
                UsersRef!!.child(usersIDs!!).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("image")) {
                                retImage[0] = dataSnapshot.child("image").value.toString()
                                Picasso.get().load(retImage[0]).into(holder.profileImage)
                            }
                            val retName = dataSnapshot.child("name").value.toString()
                            val retStatus = dataSnapshot.child("status").value.toString()
                            holder.userName.text = retName
                            if (dataSnapshot.child("userState").hasChild("state")) {
                                val state = dataSnapshot.child("userState").child("state").value.toString()
                                val date = dataSnapshot.child("userState").child("date").value.toString()
                                val time = dataSnapshot.child("userState").child("time").value.toString()
                                if (state == "online") {
                                    holder.userStatus.text = "online"
                                } else if (state == "offline") {
                                    holder.userStatus.text = "Last Seen: $date $time"
                                }
                            } else {
                                holder.userStatus.text = "offline"
                            }
                            holder.itemView.setOnClickListener {
                                val chatIntent = Intent(context, ChatActivity::class.java)
                                chatIntent.putExtra("visit_user_id", usersIDs)
                                chatIntent.putExtra("visit_user_name", retName)
                                chatIntent.putExtra("visit_image", retImage[0])
                                startActivity(chatIntent)
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ChatsViewHolder {
                val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.users_display_layout, viewGroup, false)
                return ChatsViewHolder(view)
            }
        }
        chatsList!!.adapter = adapter
        adapter.startListening()
    }

    class ChatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView
        var userStatus: TextView
        var userName: TextView

        init {
            profileImage = itemView.findViewById(R.id.users_profile_image)
            userStatus = itemView.findViewById(R.id.user_status)
            userName = itemView.findViewById(R.id.user_profile_name)
        }
    }
}
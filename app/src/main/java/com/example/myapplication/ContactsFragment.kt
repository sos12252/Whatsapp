package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
class ContactsFragment : Fragment() {
    private var ContactsView: View? = null
    private var myContactsList: RecyclerView? = null
    private var ContacsRef: DatabaseReference? = null
    private var UsersRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var currentUserID: String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false)
        myContactsList = ContactsView.findViewById<View>(R.id.contacts_list) as RecyclerView
        myContactsList!!.layoutManager = LinearLayoutManager(context)
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth!!.currentUser!!.uid
        ContacsRef = FirebaseDatabase.getInstance().reference.child("Contacts").child(currentUserID!!)
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")
        return ContactsView
    }

    override fun onStart() {
        super.onStart()
        val options: FirebaseRecyclerOptions<*> = FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContacsRef!!, Contacts::class.java)
                .build()
        val adapter: FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> = object : FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            override fun onBindViewHolder(holder: ContactsViewHolder, position: Int, model: Contacts) {
                val userIDs = getRef(position).key
                UsersRef!!.child(userIDs!!).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.child("userState").hasChild("state")) {
                                val state = dataSnapshot.child("userState").child("state").value.toString()
                                val date = dataSnapshot.child("userState").child("date").value.toString()
                                val time = dataSnapshot.child("userState").child("time").value.toString()
                                if (state == "online") {
                                    holder.onlineIcon.visibility = View.VISIBLE
                                } else if (state == "offline") {
                                    holder.onlineIcon.visibility = View.INVISIBLE
                                }
                            } else {
                                holder.onlineIcon.visibility = View.INVISIBLE
                            }
                            if (dataSnapshot.hasChild("image")) {
                                val userImage = dataSnapshot.child("image").value.toString()
                                val profileName = dataSnapshot.child("name").value.toString()
                                val profileStatus = dataSnapshot.child("status").value.toString()
                                holder.userName.text = profileName
                                holder.userStatus.text = profileStatus
                                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage)
                            } else {
                                val profileName = dataSnapshot.child("name").value.toString()
                                val profileStatus = dataSnapshot.child("status").value.toString()
                                holder.userName.text = profileName
                                holder.userStatus.text = profileStatus
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ContactsViewHolder {
                val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.users_display_layout, viewGroup, false)
                return ContactsViewHolder(view)
            }
        }
        myContactsList!!.adapter = adapter
        adapter.startListening()
    }

    class ContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userName: TextView
        var userStatus: TextView
        var profileImage: CircleImageView
        var onlineIcon: ImageView

        init {
            userName = itemView.findViewById(R.id.user_profile_name)
            userStatus = itemView.findViewById(R.id.user_status)
            profileImage = itemView.findViewById(R.id.users_profile_image)
            onlineIcon = itemView.findViewById<View>(R.id.user_online_status) as ImageView
        }
    }
}
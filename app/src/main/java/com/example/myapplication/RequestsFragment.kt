package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
class RequestsFragment : Fragment() {
    private var RequestsFragmentView: View? = null
    private var myRequestsList: RecyclerView? = null
    private var ChatRequestsRef: DatabaseReference? = null
    private var UsersRef: DatabaseReference? = null
    private var ContactsRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var currentUserID: String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        RequestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false)
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth!!.currentUser!!.uid
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")
        ChatRequestsRef = FirebaseDatabase.getInstance().reference.child("Chat Requests")
        ContactsRef = FirebaseDatabase.getInstance().reference.child("Contacts")
        myRequestsList = RequestsFragmentView.findViewById<View>(R.id.chat_requests_list) as RecyclerView
        myRequestsList!!.layoutManager = LinearLayoutManager(context)
        return RequestsFragmentView
    }

    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestsRef!!.child(currentUserID!!), Contacts::class.java)
                .build()
        val adapter: FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> = object : FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
            override fun onBindViewHolder(holder: RequestsViewHolder, position: Int, model: Contacts) {
                holder.itemView.findViewById<View>(R.id.request_accept_btn).visibility = View.VISIBLE
                holder.itemView.findViewById<View>(R.id.request_cancel_btn).visibility = View.VISIBLE
                val list_user_id = getRef(position).key
                val getTypeRef = getRef(position).child("request_type").ref
                getTypeRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val type = dataSnapshot.value.toString()
                            if (type == "received") {
                                UsersRef!!.child(list_user_id!!).addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        if (dataSnapshot.hasChild("image")) {
                                            val requestProfileImage = dataSnapshot.child("image").value.toString()
                                            Picasso.get().load(requestProfileImage).into(holder.profileImage)
                                        }
                                        val requestUserName = dataSnapshot.child("name").value.toString()
                                        val requestUserStatus = dataSnapshot.child("status").value.toString()
                                        holder.userName.text = requestUserName
                                        holder.userStatus.text = "wants to connect with you."
                                        holder.itemView.setOnClickListener {
                                            val options = arrayOf<CharSequence>(
                                                    "Accept",
                                                    "Cancel"
                                            )
                                            val builder = AlertDialog.Builder(context)
                                            builder.setTitle("$requestUserName  Chat Request")
                                            builder.setItems(options) { dialogInterface, i ->
                                                if (i == 0) {
                                                    ContactsRef!!.child(currentUserID!!).child(list_user_id).child("Contact")
                                                            .setValue("Saved").addOnCompleteListener { task ->
                                                                if (task.isSuccessful) {
                                                                    ContactsRef!!.child(list_user_id).child(currentUserID!!).child("Contact")
                                                                            .setValue("Saved").addOnCompleteListener { task ->
                                                                                if (task.isSuccessful) {
                                                                                    ChatRequestsRef!!.child(currentUserID!!).child(list_user_id)
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener { task ->
                                                                                                if (task.isSuccessful) {
                                                                                                    ChatRequestsRef!!.child(list_user_id).child(currentUserID!!)
                                                                                                            .removeValue()
                                                                                                            .addOnCompleteListener { task ->
                                                                                                                if (task.isSuccessful) {
                                                                                                                    Toast.makeText(context, "New Contact Saved", Toast.LENGTH_SHORT).show()
                                                                                                                }
                                                                                                            }
                                                                                                }
                                                                                            }
                                                                                }
                                                                            }
                                                                }
                                                            }
                                                }
                                                if (i == 1) {
                                                    ChatRequestsRef!!.child(currentUserID!!).child(list_user_id)
                                                            .removeValue()
                                                            .addOnCompleteListener { task ->
                                                                if (task.isSuccessful) {
                                                                    ChatRequestsRef!!.child(list_user_id).child(currentUserID!!)
                                                                            .removeValue()
                                                                            .addOnCompleteListener { task ->
                                                                                if (task.isSuccessful) {
                                                                                    Toast.makeText(context, "Contact Deleted", Toast.LENGTH_SHORT).show()
                                                                                }
                                                                            }
                                                                }
                                                            }
                                                }
                                            }
                                            builder.show()
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {}
                                })
                            } else if (type == "sent") {
                                val request_sent_btn = holder.itemView.findViewById<Button>(R.id.request_accept_btn)
                                request_sent_btn.text = "Req Sent"
                                holder.itemView.findViewById<View>(R.id.request_cancel_btn).visibility = View.INVISIBLE
                                UsersRef!!.child(list_user_id!!).addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        if (dataSnapshot.hasChild("image")) {
                                            val requestProfileImage = dataSnapshot.child("image").value.toString()
                                            Picasso.get().load(requestProfileImage).into(holder.profileImage)
                                        }
                                        val requestUserName = dataSnapshot.child("name").value.toString()
                                        val requestUserStatus = dataSnapshot.child("status").value.toString()
                                        holder.userName.text = requestUserName
                                        holder.userStatus.text = "you have sent a request to $requestUserName"
                                        holder.itemView.setOnClickListener {
                                            val options = arrayOf<CharSequence>(
                                                    "Cancel Chat Request"
                                            )
                                            val builder = AlertDialog.Builder(context)
                                            builder.setTitle("Already Sent Request")
                                            builder.setItems(options) { dialogInterface, i ->
                                                if (i == 0) {
                                                    ChatRequestsRef!!.child(currentUserID!!).child(list_user_id)
                                                            .removeValue()
                                                            .addOnCompleteListener { task ->
                                                                if (task.isSuccessful) {
                                                                    ChatRequestsRef!!.child(list_user_id).child(currentUserID!!)
                                                                            .removeValue()
                                                                            .addOnCompleteListener { task ->
                                                                                if (task.isSuccessful) {
                                                                                    Toast.makeText(context, "you have cancelled the chat request.", Toast.LENGTH_SHORT).show()
                                                                                }
                                                                            }
                                                                }
                                                            }
                                                }
                                            }
                                            builder.show()
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {}
                                })
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RequestsViewHolder {
                val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.users_display_layout, viewGroup, false)
                return RequestsViewHolder(view)
            }
        }
        myRequestsList!!.adapter = adapter
        adapter.startListening()
    }

    class RequestsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userName: TextView
        var userStatus: TextView
        var profileImage: CircleImageView
        var AcceptButton: Button
        var CancelButton: Button

        init {
            userName = itemView.findViewById(R.id.user_profile_name)
            userStatus = itemView.findViewById(R.id.user_status)
            profileImage = itemView.findViewById(R.id.users_profile_image)
            AcceptButton = itemView.findViewById(R.id.request_accept_btn)
            CancelButton = itemView.findViewById(R.id.request_cancel_btn)
        }
    }
}
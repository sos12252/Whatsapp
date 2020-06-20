package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.codingcafe.whatsapp.R
import com.example.myapplication.FindFriendsActivity
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class FindFriendsActivity : AppCompatActivity() {
    private var mToolbar: Toolbar? = null
    private var FindFriendsRecyclerList: RecyclerView? = null
    private var UsersRef: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friends)
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")
        FindFriendsRecyclerList = findViewById<View>(R.id.find_friends_recycler_list) as RecyclerView
        FindFriendsRecyclerList!!.layoutManager = LinearLayoutManager(this)
        mToolbar = findViewById<View>(R.id.find_friends_toolbar) as Toolbar
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Find Friends"
    }

    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(UsersRef!!, Contacts::class.java)
                .build()
        val adapter: FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> = object : FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
            override fun onBindViewHolder(holder: FindFriendViewHolder, position: Int, model: Contacts) {
                holder.userName.text = model.getName()
                holder.userStatus.text = model.getStatus()
                Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage)
                holder.itemView.setOnClickListener {
                    val visit_user_id = getRef(position).key
                    val profileIntent = Intent(this@FindFriendsActivity, ProfileActivity::class.java)
                    profileIntent.putExtra("visit_user_id", visit_user_id)
                    startActivity(profileIntent)
                }
            }

            override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): FindFriendViewHolder {
                val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.users_display_layout, viewGroup, false)
                return FindFriendViewHolder(view)
            }
        }
        FindFriendsRecyclerList!!.adapter = adapter
        adapter.startListening()
    }

    class FindFriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userName: TextView
        var userStatus: TextView
        var profileImage: CircleImageView

        init {
            userName = itemView.findViewById(R.id.user_profile_name)
            userStatus = itemView.findViewById(R.id.user_status)
            profileImage = itemView.findViewById(R.id.users_profile_image)
        }
    }
}
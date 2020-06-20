package com.example.myapplication

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.codingcafe.whatsapp.R
import com.example.myapplication.GroupChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class GroupChatActivity : AppCompatActivity() {
    private var SendMessageButton: ImageButton? = null
    private var userMessageInput: EditText? = null
    private var mScrollView: ScrollView? = null
    private var displayTextMessages: TextView? = null
    private var mAuth: FirebaseAuth? = null
    private var UsersRef: DatabaseReference? = null
    private var GroupNameRef: DatabaseReference? = null
    private var GroupMessageKeyRef: DatabaseReference? = null
    private var currentGroupName: String? = null
    private var currentUserID: String? = null
    private var currentUserName: String? = null
    private var currentDate: String? = null
    private var currentTime: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)
        currentGroupName = intent.extras!!["groupName"].toString()
        Toast.makeText(this@GroupChatActivity, currentGroupName, Toast.LENGTH_SHORT).show()
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth!!.currentUser!!.uid
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")
        GroupNameRef = FirebaseDatabase.getInstance().reference.child("Groups").child(currentGroupName!!)
        InitializeFields()
        GetUserInfo()
        SendMessageButton!!.setOnClickListener {
            SaveMessageInfoToDatabase()
            userMessageInput!!.setText("")
            mScrollView!!.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    override fun onStart() {
        super.onStart()
        GroupNameRef!!.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                if (dataSnapshot.exists()) {
                    DisplayMessages(dataSnapshot)
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                if (dataSnapshot.exists()) {
                    DisplayMessages(dataSnapshot)
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun InitializeFields() {
        val mToolbar = findViewById<View>(R.id.group_chat_bar_layout) as Toolbar
        setSupportActionBar(mToolbar)
        supportActionBar!!.title = currentGroupName
        SendMessageButton = findViewById<View>(R.id.send_message_button) as ImageButton
        userMessageInput = findViewById<View>(R.id.input_group_message) as EditText
        displayTextMessages = findViewById<View>(R.id.group_chat_text_display) as TextView
        mScrollView = findViewById<View>(R.id.my_scroll_view) as ScrollView
    }

    private fun GetUserInfo() {
        UsersRef!!.child(currentUserID!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").value.toString()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun SaveMessageInfoToDatabase() {
        val message = userMessageInput!!.text.toString()
        val messagekEY = GroupNameRef!!.push().key
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show()
        } else {
            val calForDate = Calendar.getInstance()
            val currentDateFormat = SimpleDateFormat("MMM dd, yyyy")
            currentDate = currentDateFormat.format(calForDate.time)
            val calForTime = Calendar.getInstance()
            val currentTimeFormat = SimpleDateFormat("hh:mm a")
            currentTime = currentTimeFormat.format(calForTime.time)
            val groupMessageKey = HashMap<String, Any>()
            GroupNameRef!!.updateChildren(groupMessageKey)
            GroupMessageKeyRef = GroupNameRef!!.child(messagekEY!!)
            val messageInfoMap = HashMap<String, Any?>()
            messageInfoMap["name"] = currentUserName
            messageInfoMap["message"] = message
            messageInfoMap["date"] = currentDate
            messageInfoMap["time"] = currentTime
            GroupMessageKeyRef!!.updateChildren(messageInfoMap)
        }
    }

    private fun DisplayMessages(dataSnapshot: DataSnapshot) {
        val iterator: Iterator<*> = dataSnapshot.children.iterator()
        while (iterator.hasNext()) {
            val chatDate = (iterator.next() as DataSnapshot).value as String?
            val chatMessage = (iterator.next() as DataSnapshot).value as String?
            val chatName = (iterator.next() as DataSnapshot).value as String?
            val chatTime = (iterator.next() as DataSnapshot).value as String?
            displayTextMessages!!.append("$chatName :\n$chatMessage\n$chatTime     $chatDate\n\n\n")
            mScrollView!!.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}
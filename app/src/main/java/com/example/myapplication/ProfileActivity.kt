package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.codingcafe.whatsapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class ProfileActivity : AppCompatActivity() {
    private var receiverUserID: String? = null
    private var senderUserID: String? = null
    private var Current_State: String? = null
    private var userProfileImage: CircleImageView? = null
    private var userProfileName: TextView? = null
    private var userProfileStatus: TextView? = null
    private var SendMessageRequestButton: Button? = null
    private var DeclineMessageRequestButton: Button? = null
    private var UserRef: DatabaseReference? = null
    private var ChatRequestRef: DatabaseReference? = null
    private var ContactsRef: DatabaseReference? = null
    private var NotificationRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        mAuth = FirebaseAuth.getInstance()
        UserRef = FirebaseDatabase.getInstance().reference.child("Users")
        ChatRequestRef = FirebaseDatabase.getInstance().reference.child("Chat Requests")
        ContactsRef = FirebaseDatabase.getInstance().reference.child("Contacts")
        NotificationRef = FirebaseDatabase.getInstance().reference.child("Notifications")
        receiverUserID = intent.extras!!["visit_user_id"].toString()
        senderUserID = mAuth!!.currentUser!!.uid
        userProfileImage = findViewById<View>(R.id.visit_profile_image) as CircleImageView
        userProfileName = findViewById<View>(R.id.visit_user_name) as TextView
        userProfileStatus = findViewById<View>(R.id.visit_profile_status) as TextView
        SendMessageRequestButton = findViewById<View>(R.id.send_message_request_button) as Button
        DeclineMessageRequestButton = findViewById<View>(R.id.decline_message_request_button) as Button
        Current_State = "new"
        RetrieveUserInfo()
    }

    private fun RetrieveUserInfo() {
        UserRef!!.child(receiverUserID!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("image")) {
                    val userImage = dataSnapshot.child("image").value.toString()
                    val userName = dataSnapshot.child("name").value.toString()
                    val userstatus = dataSnapshot.child("status").value.toString()
                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage)
                    userProfileName!!.text = userName
                    userProfileStatus!!.text = userstatus
                    ManageChatRequests()
                } else {
                    val userName = dataSnapshot.child("name").value.toString()
                    val userstatus = dataSnapshot.child("status").value.toString()
                    userProfileName!!.text = userName
                    userProfileStatus!!.text = userstatus
                    ManageChatRequests()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun ManageChatRequests() {
        ChatRequestRef!!.child(senderUserID!!)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserID!!)) {
                            val request_type = dataSnapshot.child(receiverUserID!!).child("request_type").value.toString()
                            if (request_type == "sent") {
                                Current_State = "request_sent"
                                SendMessageRequestButton!!.text = "Cancel Chat Request"
                            } else if (request_type == "received") {
                                Current_State = "request_received"
                                SendMessageRequestButton!!.text = "Accept Chat Request"
                                DeclineMessageRequestButton!!.visibility = View.VISIBLE
                                DeclineMessageRequestButton!!.isEnabled = true
                                DeclineMessageRequestButton!!.setOnClickListener { CancelChatRequest() }
                            }
                        } else {
                            ContactsRef!!.child(senderUserID!!)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserID!!)) {
                                                Current_State = "friends"
                                                SendMessageRequestButton!!.text = "Remove this Contact"
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {}
                                    })
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        if (senderUserID != receiverUserID) {
            SendMessageRequestButton!!.setOnClickListener {
                SendMessageRequestButton!!.isEnabled = false
                if (Current_State == "new") {
                    SendChatRequest()
                }
                if (Current_State == "request_sent") {
                    CancelChatRequest()
                }
                if (Current_State == "request_received") {
                    AcceptChatRequest()
                }
                if (Current_State == "friends") {
                    RemoveSpecificContact()
                }
            }
        } else {
            SendMessageRequestButton!!.visibility = View.INVISIBLE
        }
    }

    private fun RemoveSpecificContact() {
        ContactsRef!!.child(senderUserID!!).child(receiverUserID!!)
                .removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        ContactsRef!!.child(receiverUserID!!).child(senderUserID!!)
                                .removeValue()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        SendMessageRequestButton!!.isEnabled = true
                                        Current_State = "new"
                                        SendMessageRequestButton!!.text = "Send Message"
                                        DeclineMessageRequestButton!!.visibility = View.INVISIBLE
                                        DeclineMessageRequestButton!!.isEnabled = false
                                    }
                                }
                    }
                }
    }

    private fun AcceptChatRequest() {
        ContactsRef!!.child(senderUserID!!).child(receiverUserID!!)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        ContactsRef!!.child(receiverUserID!!).child(senderUserID!!)
                                .child("Contacts").setValue("Saved")
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        ChatRequestRef!!.child(senderUserID!!).child(receiverUserID!!)
                                                .removeValue()
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        ChatRequestRef!!.child(receiverUserID!!).child(senderUserID!!)
                                                                .removeValue()
                                                                .addOnCompleteListener {
                                                                    SendMessageRequestButton!!.isEnabled = true
                                                                    Current_State = "friends"
                                                                    SendMessageRequestButton!!.text = "Remove this Contact"
                                                                    DeclineMessageRequestButton!!.visibility = View.INVISIBLE
                                                                    DeclineMessageRequestButton!!.isEnabled = false
                                                                }
                                                    }
                                                }
                                    }
                                }
                    }
                }
    }

    private fun CancelChatRequest() {
        ChatRequestRef!!.child(senderUserID!!).child(receiverUserID!!)
                .removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        ChatRequestRef!!.child(receiverUserID!!).child(senderUserID!!)
                                .removeValue()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        SendMessageRequestButton!!.isEnabled = true
                                        Current_State = "new"
                                        SendMessageRequestButton!!.text = "Send Message"
                                        DeclineMessageRequestButton!!.visibility = View.INVISIBLE
                                        DeclineMessageRequestButton!!.isEnabled = false
                                    }
                                }
                    }
                }
    }

    private fun SendChatRequest() {
        ChatRequestRef!!.child(senderUserID!!).child(receiverUserID!!)
                .child("request_type").setValue("sent")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        ChatRequestRef!!.child(receiverUserID!!).child(senderUserID!!)
                                .child("request_type").setValue("received")
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val chatNotificationMap = HashMap<String, String?>()
                                        chatNotificationMap["from"] = senderUserID
                                        chatNotificationMap["type"] = "request"
                                        NotificationRef!!.child(receiverUserID!!).push()
                                                .setValue(chatNotificationMap)
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        SendMessageRequestButton!!.isEnabled = true
                                                        Current_State = "request_sent"
                                                        SendMessageRequestButton!!.text = "Cancel Chat Request"
                                                    }
                                                }
                                    }
                                }
                    }
                }
    }
}
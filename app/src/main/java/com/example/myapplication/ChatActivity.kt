package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.codingcafe.whatsapp.R
import com.example.myapplication.ChatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private var messageReceiverID: String? = null
    private var messageSenderID: String? = null
    private var userName: TextView? = null
    private var userLastSeen: TextView? = null
    private var userImage: CircleImageView? = null
    private var RootRef: DatabaseReference? = null
    private var SendMessageButton: ImageButton? = null
    private var SendFilesButton: ImageButton? = null
    private var MessageInputText: EditText? = null
    private val messagesList: MutableList<Messages?> = ArrayList()
    private var linearLayoutManager: LinearLayoutManager? = null
    private var messageAdapter: MessageAdapter? = null
    private var userMessagesList: RecyclerView? = null
    private var saveCurrentTime: String? = null
    private var saveCurrentDate: String? = null
    private var checker = ""
    private var myUrl = ""
    private var uploadTask: StorageTask<*>? = null
    private var fileUri: Uri? = null
    private var loadingBar: ProgressDialog? = null

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val mAuth = FirebaseAuth.getInstance()
        messageSenderID = mAuth.currentUser!!.uid
        RootRef = FirebaseDatabase.getInstance().reference
        messageReceiverID = intent.extras!!["visit_user_id"].toString()
        val messageReceiverName = intent.extras!!["visit_user_name"].toString()
        val messageReceiverImage = intent.extras!!["visit_image"].toString()
        IntializeControllers()
        userName!!.text = messageReceiverName
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage)
        SendMessageButton!!.setOnClickListener { SendMessage() }
        DisplayLastSeen()
        SendFilesButton!!.setOnClickListener {
            val options = arrayOf<CharSequence>(
                    "Images",
                    "PDF Files",
                    "Ms Word Files"
            )
            val builder = AlertDialog.Builder(this@ChatActivity)
            builder.setTitle("Select the File")
            builder.setItems(options) { dialogInterface, i ->
                if (i == 0) {
                    checker = "image"
                    val intent = Intent()
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.type = "image/*"
                    startActivityForResult(Intent.createChooser(intent, "Select Image"), 438)
                }
                if (i == 1) {
                    checker = "pdf"
                }
                if (i == 2) {
                    checker = "docx"
                }
            }
            builder.show()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun IntializeControllers() {
        val chatToolBar = findViewById<View>(R.id.chat_toolbar) as Toolbar
        setSupportActionBar(chatToolBar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowCustomEnabled(true)
        val layoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null)
        actionBar.customView = actionBarView
        userName = findViewById<View>(R.id.custom_profile_name) as TextView
        userLastSeen = findViewById<View>(R.id.custom_user_last_seen) as TextView
        userImage = findViewById<View>(R.id.custom_profile_image) as CircleImageView
        SendMessageButton = findViewById<View>(R.id.send_message_btn) as ImageButton
        SendFilesButton = findViewById<View>(R.id.send_files_btn) as ImageButton
        MessageInputText = findViewById<View>(R.id.input_message) as EditText
        messageAdapter = MessageAdapter(messagesList)
        userMessagesList = findViewById<View>(R.id.private_messages_list_of_users) as RecyclerView
        linearLayoutManager = LinearLayoutManager(this)
        userMessagesList!!.layoutManager = linearLayoutManager
        userMessagesList!!.adapter = messageAdapter
        loadingBar = ProgressDialog(this)
        val calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("MMM dd, yyyy")
        saveCurrentDate = currentDate.format(calendar.time)
        val currentTime = SimpleDateFormat("hh:mm a")
        saveCurrentTime = currentTime.format(calendar.time)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 438 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            loadingBar!!.setTitle("Sending File")
            loadingBar!!.setMessage("Please wait, We are sending this file...")
            loadingBar!!.setCanceledOnTouchOutside(false)
            loadingBar!!.show()
            fileUri = data.data
            if (checker != "image") {
            } else if (checker == "image") {
                val storageReference = FirebaseStorage.getInstance().reference.child("Image Files")
                val messageSenderRef = "Messages/$messageSenderID/$messageReceiverID"
                val messageReceiverRef = "Messages/$messageReceiverID/$messageSenderID"
                val userMessageKeyRef = RootRef!!.child("Messages")
                        .child(messageSenderID!!).child(messageReceiverID!!).push()
                val messagePushID = userMessageKeyRef.key
                val filePath = storageReference.child("$messagePushID.jpg")
                uploadTask = filePath.putFile(fileUri!!)


                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                    }
                    filePath.downloadUrl
                }.addOnCompleteListener(OnCompleteListener<Uri?> { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()
                        val messageTextBody: MutableMap<String, String?> = HashMap()
                        messageTextBody["message"] = myUrl
                        messageTextBody["name"] = fileUri!!.lastPathSegment
                        messageTextBody["type"] = checker
                        messageTextBody["to"] = messageReceiverID
                        messageTextBody["messageID"] = messagePushID
                        messageTextBody["time"] = saveCurrentTime
                        messageTextBody["date"] = saveCurrentDate
                        val messageBodyDetails: MutableMap<String, Any> = HashMap()
                        messageBodyDetails["$messageSenderRef/$messagePushID"] = messageTextBody
                        messageBodyDetails["$messageReceiverRef/$messagePushID"] = messageTextBody
//
//
                        RootRef!!.updateChildren(messageBodyDetails).addOnCompleteListener{task ->
                            if(task.isSuccessful){
                                loadingBar!!.dismiss()
                                Toast.makeText(this@ChatActivity, "Message Sent Successfully...", Toast.LENGTH_SHORT).show()
                            }else{
                                loadingBar!!.dismiss()
                                Toast.makeText(this@ChatActivity, "Error", Toast.LENGTH_SHORT).show()
                            }
                                MessageInputText!!.setText("")

                        }
                    }
                })
            } else {
                loadingBar!!.dismiss()
                Toast.makeText(this, "Nothing Selected, Error.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun DisplayLastSeen() {
        RootRef!!.child("Users").child(messageReceiverID!!)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.child("userState").hasChild("state")) {
                            val state = dataSnapshot.child("userState").child("state").value.toString()
                            val date = dataSnapshot.child("userState").child("date").value.toString()
                            val time = dataSnapshot.child("userState").child("time").value.toString()
                            if (state == "online") {
                                userLastSeen!!.text = "online"
                            } else if (state == "offline") {
                                userLastSeen!!.text = "Last Seen: $date $time"
                            }
                        } else {
                            userLastSeen!!.text = "offline"
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
    }

    override fun onStart() {
        super.onStart()
        RootRef!!.child("Messages").child(messageSenderID!!).child(messageReceiverID!!)
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        val messages = dataSnapshot.getValue(Messages::class.java)
                        messagesList.add(messages)
                        messageAdapter!!.notifyDataSetChanged()
                        userMessagesList!!.smoothScrollToPosition(userMessagesList!!.adapter!!.itemCount)
                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                    override fun onCancelled(databaseError: DatabaseError) {}
                })
    }

    private fun SendMessage() {
        val messageText = MessageInputText!!.text.toString()
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show()
        } else {
            val messageSenderRef = "Messages/$messageSenderID/$messageReceiverID"
            val messageReceiverRef = "Messages/$messageReceiverID/$messageSenderID"
            val userMessageKeyRef = RootRef!!.child("Messages")
                    .child(messageSenderID!!).child(messageReceiverID!!).push()
            val messagePushID = userMessageKeyRef.key
            val messageTextBody: MutableMap<String, String?> = HashMap()
            messageTextBody["message"] = messageText
            messageTextBody["type"] = "text"
            messageTextBody["from"] = messageSenderID
            messageTextBody["to"] = messageReceiverID
            messageTextBody["messageID"] = messagePushID
            messageTextBody["time"] = saveCurrentTime
            messageTextBody["date"] = saveCurrentDate
            val messageBodyDetails: MutableMap<String, Any> = HashMap()
            messageBodyDetails["$messageSenderRef/$messagePushID"] = messageTextBody
            messageBodyDetails["$messageReceiverRef/$messagePushID"] = messageTextBody

            RootRef!!.updateChildren(messageBodyDetails).addOnCompleteListener{ task ->
                    if (task.isSuccessful)
                        Toast.makeText(this@ChatActivity, "Message Sent Successfully...", Toast.LENGTH_SHORT).show()
                    else {
                        Toast.makeText(this@ChatActivity, "Error", Toast.LENGTH_SHORT).show()
                    }
                    MessageInputText!!.setText("")
                }
            }
        }
    }

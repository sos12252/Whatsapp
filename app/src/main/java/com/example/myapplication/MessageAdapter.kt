package com.example.myapplication

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.codingcafe.whatsapp.R
import com.example.myapplication.MessageAdapter.MessageViewHolder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MessageAdapter(private val userMessagesList: MutableList<Messages?>) : RecyclerView.Adapter<MessageViewHolder>() {
    private var mAuth: FirebaseAuth? = null
    private var usersRef: DatabaseReference? = null

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var senderMessageText: TextView
        var receiverMessageText: TextView
        var receiverProfileImage: CircleImageView
        var messageSenderPicture: ImageView
        var messageReceiverPicture: ImageView

        init {
            senderMessageText = itemView.findViewById<View>(R.id.sender_messsage_text) as TextView
            receiverMessageText = itemView.findViewById<View>(R.id.receiver_message_text) as TextView
            receiverProfileImage = itemView.findViewById<View>(R.id.message_profile_image) as CircleImageView
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view)
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MessageViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.custom_messages_layout, viewGroup, false)
        mAuth = FirebaseAuth.getInstance()
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(messageViewHolder: MessageViewHolder, i: Int) {
        val messageSenderId = mAuth!!.currentUser!!.uid
        val messages = userMessagesList[i]
        val fromUserID = messages?.getFrom
        val fromMessageType = messages?.getType
        usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(fromUserID!!)
        usersRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    val receiverImage = dataSnapshot.child("image").value.toString()
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        messageViewHolder.receiverMessageText.visibility = View.GONE
        messageViewHolder.receiverProfileImage.visibility = View.GONE
        messageViewHolder.senderMessageText.visibility = View.GONE
        messageViewHolder.messageSenderPicture.visibility = View.GONE
        messageViewHolder.messageReceiverPicture.visibility = View.GONE
        if (fromMessageType == "text") {
            if (fromUserID == messageSenderId) {
                messageViewHolder.senderMessageText.visibility = View.VISIBLE
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout)
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK)
                messageViewHolder.senderMessageText.text = """${messages.getMessage}

${messages.getTime} - ${messages.getDate}"""
            } else {
                messageViewHolder.receiverProfileImage.visibility = View.VISIBLE
                messageViewHolder.receiverMessageText.visibility = View.VISIBLE
                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout)
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK)
                messageViewHolder.receiverMessageText.text = """${messages.getMessage}

${messages.getTime} - ${messages.getDate}"""
            }
        }
    }

    override fun getItemCount(): Int {
        return userMessagesList.size
    }

}


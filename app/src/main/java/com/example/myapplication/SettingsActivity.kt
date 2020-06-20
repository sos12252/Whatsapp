package com.example.myapplication

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.codingcafe.whatsapp.R
import com.example.myapplication.SettingsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.annotations.Nullable
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class SettingsActivity : AppCompatActivity() {
    private var UpdateAccountSettings: Button? = null
    private var userName: EditText? = null
    private var userStatus: EditText? = null
    private var userProfileImage: CircleImageView? = null
    private var currentUserID: String? = null
    private var mAuth: FirebaseAuth? = null
    private var RootRef: DatabaseReference? = null
    private var UserProfileImagesRef: StorageReference? = null
    private var loadingBar: ProgressDialog? = null
    private var SettingsToolBar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth!!.currentUser!!.uid
        RootRef = FirebaseDatabase.getInstance().reference
        UserProfileImagesRef = FirebaseStorage.getInstance().reference.child("Profile Images")
        InitializeFields()
        userName!!.visibility = View.INVISIBLE
        UpdateAccountSettings!!.setOnClickListener { UpdateSettings() }
        RetrieveUserInfo()
        userProfileImage!!.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, GalleryPick)
        }
    }

    private fun InitializeFields() {
        UpdateAccountSettings = findViewById<View>(R.id.update_settings_button) as Button
        userName = findViewById<View>(R.id.set_user_name) as EditText
        userStatus = findViewById<View>(R.id.set_profile_status) as EditText
        userProfileImage = findViewById<View>(R.id.set_profile_image) as CircleImageView
        loadingBar = ProgressDialog(this)
        SettingsToolBar = findViewById<View>(R.id.settings_toolbar) as Toolbar
        setSupportActionBar(SettingsToolBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowCustomEnabled(true)
        supportActionBar!!.title = "Account Settings"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GalleryPick && resultCode == Activity.RESULT_OK && data != null) {
            val ImageUri = data.data
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this)
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                loadingBar!!.setTitle("Set Profile Image")
                loadingBar!!.setMessage("Please wait, your profile image is updating...")
                loadingBar!!.setCanceledOnTouchOutside(false)
                loadingBar!!.show()
                val resultUri = result.uri
                val filePath = UserProfileImagesRef!!.child("$currentUserID.jpg")
                filePath.putFile(resultUri).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@SettingsActivity, "Profile Image uploaded Successfully...", Toast.LENGTH_SHORT).show()

                        //final String downloadedUrl = StorageReference.getDownloadUrl().toString();
                        val downloadedUrl = task.result!!.storage.downloadUrl.toString()
                        RootRef!!.child("Users").child(currentUserID!!).child("image")
                                .setValue(downloadedUrl) //원래 downloadedUrl
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this@SettingsActivity, "Image save in Database, Successfully...", Toast.LENGTH_SHORT).show()
                                        loadingBar!!.dismiss()
                                    } else {
                                        val message = task.exception.toString()
                                        Toast.makeText(this@SettingsActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                                        loadingBar!!.dismiss()
                                    }
                                }
                    } else {
                        val message = task.exception.toString()
                        Toast.makeText(this@SettingsActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                        loadingBar!!.dismiss()
                    }
                }
            }
        }
    }

    private fun UpdateSettings() {
        val setUserName = userName!!.text.toString()
        val setStatus = userStatus!!.text.toString()
        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(this, "Please write your user name first....", Toast.LENGTH_SHORT).show()
        }
        if (TextUtils.isEmpty(setStatus)) {
            Toast.makeText(this, "Please write your status....", Toast.LENGTH_SHORT).show()
        } else {
            val profileMap = HashMap<String, Any?>()
            profileMap["uid"] = currentUserID
            profileMap["name"] = setUserName
            profileMap["status"] = setStatus
            RootRef!!.child("Users").child(currentUserID!!).updateChildren(profileMap)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            SendUserToMainActivity()
                            Toast.makeText(this@SettingsActivity, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show()
                        } else {
                            val message = task.exception.toString()
                            Toast.makeText(this@SettingsActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }

    private fun RetrieveUserInfo() {
        RootRef!!.child("Users").child(currentUserID!!)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image")) {
                            val retrieveUserName = dataSnapshot.child("name").value.toString()
                            val retrievesStatus = dataSnapshot.child("status").value.toString()
                            val retrieveProfileImage = dataSnapshot.child("image").value.toString()
                            userName!!.setText(retrieveUserName)
                            userStatus!!.setText(retrievesStatus)
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage)
                        } else if (dataSnapshot.exists() && dataSnapshot.hasChild("name")) {
                            val retrieveUserName = dataSnapshot.child("name").value.toString()
                            val retrievesStatus = dataSnapshot.child("status").value.toString()
                            userName!!.setText(retrieveUserName)
                            userStatus!!.setText(retrievesStatus)
                        } else {
                            userName!!.visibility = View.VISIBLE
                            Toast.makeText(this@SettingsActivity, "Please set & update your profile information...", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
    }

    private fun SendUserToMainActivity() {
        val mainIntent = Intent(this@SettingsActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }

    companion object {
        private const val GalleryPick = 1
    }
}
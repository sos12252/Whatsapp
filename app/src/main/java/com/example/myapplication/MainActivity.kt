package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.example.codingcafe.whatsapp.R
import com.example.myapplication.MainActivity
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mToolbar: Toolbar? = null
    private var myViewPager: ViewPager? = null
    private var myTabLayout: TabLayout? = null
    private var myTabsAccessorAdapter: TabsAccessorAdapter? = null
    private var currentUser: FirebaseUser? = null
    private var mAuth: FirebaseAuth? = null
    private var RootRef: DatabaseReference? = null
    private var currentUserID: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser
        currentUserID = mAuth!!.currentUser!!.uid
        RootRef = FirebaseDatabase.getInstance().reference
        mToolbar = findViewById<View>(R.id.main_page_toolbar) as Toolbar
        setSupportActionBar(mToolbar)
        supportActionBar!!.title = "WhatsApp"
        myViewPager = findViewById<View>(R.id.main_tabs_pager) as ViewPager
        myTabsAccessorAdapter = TabsAccessorAdapter(supportFragmentManager)
        myViewPager!!.adapter = myTabsAccessorAdapter
        myTabLayout = findViewById<View>(R.id.main_tabs) as TabLayout
        myTabLayout!!.setupWithViewPager(myViewPager)
    }

    override fun onStart() {
        super.onStart()
        if (currentUser == null) {
            SendUserToLoginActivity()
        } else {
            updateUserStatus("online")
            VerifyUserExistance()
        }
    }

    override fun onStop() {
        super.onStop()
        if (currentUser != null) {
            updateUserStatus("offline")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (currentUser != null) {
            updateUserStatus("offline")
        }
    }

    private fun VerifyUserExistance() {
        val currentUserID = mAuth!!.currentUser!!.uid
        RootRef!!.child("Users").child(currentUserID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child("name").exists()) {
                    Toast.makeText(this@MainActivity, "Welcome", Toast.LENGTH_SHORT).show()
                } else {
                    SendUserToSettingsActivity()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId == R.id.main_logout_option) {
            updateUserStatus("offline")
            mAuth!!.signOut()
            SendUserToLoginActivity()
        }
        if (item.itemId == R.id.main_settings_option) {
            SendUserToSettingsActivity()
        }
        if (item.itemId == R.id.main_create_group_option) {
            RequestNewGroup()
        }
        if (item.itemId == R.id.main_find_friends_option) {
            SendUserToFindFriendsActivity()
        }
        return true
    }

    private fun RequestNewGroup() {
        val builder = AlertDialog.Builder(this@MainActivity, R.style.AlertDialog)
        builder.setTitle("Enter Group Name :")
        val groupNameField = EditText(this@MainActivity)
        groupNameField.hint = "e.g Coding Cafe"
        builder.setView(groupNameField)
        builder.setPositiveButton("Create") { dialogInterface, i ->
            val groupName = groupNameField.text.toString()
            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(this@MainActivity, "Please write Group Name...", Toast.LENGTH_SHORT).show()
            } else {
                CreateNewGroup(groupName)
            }
        }
        builder.setNegativeButton("Cancel") { dialogInterface, i -> dialogInterface.cancel() }
        builder.show()
    }

    private fun CreateNewGroup(groupName: String) {
        RootRef!!.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@MainActivity, "$groupName group is Created Successfully...", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun SendUserToLoginActivity() {
        val loginIntent = Intent(this@MainActivity, LoginActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(loginIntent)
    }

    private fun SendUserToSettingsActivity() {
        val settingsIntent = Intent(this@MainActivity, SettingsActivity::class.java)
        startActivity(settingsIntent)
    }

    private fun SendUserToFindFriendsActivity() {
        val findFriendsIntent = Intent(this@MainActivity, FindFriendsActivity::class.java)
        startActivity(findFriendsIntent)
    }

    private fun updateUserStatus(state: String) {
        val saveCurrentTime: String
        val saveCurrentDate: String
        val calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("MMM dd, yyyy")
        saveCurrentDate = currentDate.format(calendar.time)
        val currentTime = SimpleDateFormat("hh:mm a")
        saveCurrentTime = currentTime.format(calendar.time)
        val onlineStateMap = HashMap<String, Any>()
        onlineStateMap["time"] = saveCurrentTime
        onlineStateMap["date"] = saveCurrentDate
        onlineStateMap["state"] = state
        RootRef!!.child("Users").child(currentUserID!!).child("userState")
                .updateChildren(onlineStateMap)
    }
}
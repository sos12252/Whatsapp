package com.example.myapplication

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.codingcafe.whatsapp.R
import com.example.myapplication.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId

class LoginActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var loadingBar: ProgressDialog? = null
    private var LoginButton: Button? = null
    private var PhoneLoginButton: Button? = null
    private var UserEmail: EditText? = null
    private var UserPassword: EditText? = null
    private var NeedNewAccountLink: TextView? = null
    private var ForgetPasswordLink: TextView? = null
    private var UsersRef: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")
        InitializeFields()
        NeedNewAccountLink!!.setOnClickListener { SendUserToRegisterActivity() }
        LoginButton!!.setOnClickListener { AllowUserToLogin() }
        PhoneLoginButton!!.setOnClickListener {
            val phoneLoginIntent = Intent(this@LoginActivity, PhoneLoginActivity::class.java)
            startActivity(phoneLoginIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            SendUserToMainActivity()
        }
    }

    private fun AllowUserToLogin() {
        val email = UserEmail!!.text.toString()
        val password = UserPassword!!.text.toString()
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email...", Toast.LENGTH_SHORT).show()
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password...", Toast.LENGTH_SHORT).show()
        } else {
            loadingBar!!.setTitle("Sign In")
            loadingBar!!.setMessage("Please wait....")
            loadingBar!!.setCanceledOnTouchOutside(true)
            loadingBar!!.show()
            mAuth!!.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val currentUserId = mAuth!!.currentUser!!.uid
                            val deviceToken = FirebaseInstanceId.getInstance().token

                            //이부분조금다름 튶토리얼12
                            UsersRef!!.child(currentUserId).child("device_token")
                                    .setValue(deviceToken)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            SendUserToMainActivity()
                                            Toast.makeText(this@LoginActivity, "Logged in Successful...", Toast.LENGTH_SHORT).show()
                                            loadingBar!!.dismiss()
                                        }
                                    }
                        } else {
                            val message = task.exception.toString()
                            Toast.makeText(this@LoginActivity, "Error : $message", Toast.LENGTH_SHORT).show()
                            loadingBar!!.dismiss()
                        }
                    }
        }
    }

    private fun InitializeFields() {
        LoginButton = findViewById<View>(R.id.login_button) as Button
        PhoneLoginButton = findViewById<View>(R.id.phone_login_button) as Button
        UserEmail = findViewById<View>(R.id.login_email) as EditText
        UserPassword = findViewById<View>(R.id.login_password) as EditText
        NeedNewAccountLink = findViewById<View>(R.id.need_new_account_link) as TextView
        ForgetPasswordLink = findViewById<View>(R.id.forget_password_link) as TextView
        loadingBar = ProgressDialog(this)
    }

    private fun SendUserToMainActivity() {
        val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }

    private fun SendUserToRegisterActivity() {
        val registerIntent = Intent(this@LoginActivity, RegisterActivity::class.java)
        startActivity(registerIntent)
    }
}
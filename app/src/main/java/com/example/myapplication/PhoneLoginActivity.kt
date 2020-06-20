package com.example.myapplication

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.codingcafe.whatsapp.R
import com.example.myapplication.PhoneLoginActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import java.util.concurrent.TimeUnit

class PhoneLoginActivity : AppCompatActivity() {
    private var SendVerificationCodeButton: Button? = null
    private var VerifyButton: Button? = null
    private var InputPhoneNumber: EditText? = null
    private var InputVerificationCode: EditText? = null
    private var callbacks: OnVerificationStateChangedCallbacks? = null
    private var mAuth: FirebaseAuth? = null
    private var loadingBar: ProgressDialog? = null
    private var mVerificationId: String? = null
    private var mResendToken: ForceResendingToken? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_login)
        mAuth = FirebaseAuth.getInstance()
        SendVerificationCodeButton = findViewById<View>(R.id.send_ver_code_button) as Button
        VerifyButton = findViewById<View>(R.id.verify_button) as Button
        InputPhoneNumber = findViewById<View>(R.id.phone_nnumber_input) as EditText
        InputVerificationCode = findViewById<View>(R.id.verification_code_input) as EditText
        loadingBar = ProgressDialog(this)
        SendVerificationCodeButton!!.setOnClickListener {
            val phoneNumber = InputPhoneNumber!!.text.toString()
            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(this@PhoneLoginActivity, "Please enter your phone number first...", Toast.LENGTH_SHORT).show()
            } else {
                loadingBar!!.setTitle("Phone Verification")
                loadingBar!!.setMessage("please wait, while we are authenticating your phone...")
                loadingBar!!.setCanceledOnTouchOutside(false)
                loadingBar!!.show()
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber,  // Phone number to verify
                        60,  // Timeout duration
                        TimeUnit.SECONDS,  // Unit of timeout
                        this@PhoneLoginActivity,  // Activity (for callback binding)
                        callbacks!!) // OnVerificationStateChangedCallbacks
            }
        }
        VerifyButton!!.setOnClickListener {
            SendVerificationCodeButton!!.visibility = View.INVISIBLE
            InputPhoneNumber!!.visibility = View.INVISIBLE
            val verificationCode = InputVerificationCode!!.text.toString()
            if (TextUtils.isEmpty(verificationCode)) {
                Toast.makeText(this@PhoneLoginActivity, "Please write verification code first...", Toast.LENGTH_SHORT).show()
            } else {
                loadingBar!!.setTitle("Verification Code")
                loadingBar!!.setMessage("please wait, while we are verifying verification code...")
                loadingBar!!.setCanceledOnTouchOutside(false)
                loadingBar!!.show()
                val credential = PhoneAuthProvider.getCredential(mVerificationId!!, verificationCode)
                signInWithPhoneAuthCredential(credential)
            }
        }
        callbacks = object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                loadingBar!!.dismiss()
                Toast.makeText(this@PhoneLoginActivity, "Invalid Phone Number, Please enter correct phone number with your country code...", Toast.LENGTH_SHORT).show()
                SendVerificationCodeButton!!.visibility = View.VISIBLE
                InputPhoneNumber!!.visibility = View.VISIBLE
                VerifyButton!!.visibility = View.INVISIBLE
                InputVerificationCode!!.visibility = View.INVISIBLE
            }

            override fun onCodeSent(verificationId: String,
                                    token: ForceResendingToken) {
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token
                loadingBar!!.dismiss()
                Toast.makeText(this@PhoneLoginActivity, "Code has been sent, please check and verify...", Toast.LENGTH_SHORT).show()
                SendVerificationCodeButton!!.visibility = View.INVISIBLE
                InputPhoneNumber!!.visibility = View.INVISIBLE
                VerifyButton!!.visibility = View.VISIBLE
                InputVerificationCode!!.visibility = View.VISIBLE
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        loadingBar!!.dismiss()
                        Toast.makeText(this@PhoneLoginActivity, "Congratulations, you're logged in successfully...", Toast.LENGTH_SHORT).show()
                        SendUserToMainActivity()
                    } else {
                        val message = task.exception.toString()
                        Toast.makeText(this@PhoneLoginActivity, "Error : $message", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun SendUserToMainActivity() {
        val mainIntent = Intent(this@PhoneLoginActivity, MainActivity::class.java)
        startActivity(mainIntent)
        finish()
    }
}
package com.example.instaapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        signup_link.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
        }
        login_btn.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email = login_email.text.toString()
        val password = login_password.text.toString()

        when {
            TextUtils.isEmpty(email) -> Toast.makeText(
                this,
                "Username is required",
                Toast.LENGTH_SHORT
            ).show()
            TextUtils.isEmpty(password) -> Toast.makeText(
                this,
                "Password is required",
                Toast.LENGTH_SHORT
            ).show()

            else -> {
                val progressDialog = ProgressDialog(this@LoginActivity)
                progressDialog.setTitle("Login")
                progressDialog.setMessage("Logging in...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            progressDialog.dismiss()
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        } else {
                            val message = task.exception!!.toString()
                            Toast.makeText(this, "Password or Email Invalid", Toast.LENGTH_LONG).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }

    }

    override fun onStart() {
        super.onStart()

        if(FirebaseAuth.getInstance().currentUser!=null)
        {
            //forwarding to home page
            val intent=Intent(this@LoginActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}
package com.example.eventticketbookingsystem414

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    private  lateinit var  un:EditText
    private lateinit var signupEmailEditText: EditText
    private lateinit var signupPasswordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var loginTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, AllEventsActivity::class.java)
            startActivity(intent)
            finish() // Close the current activity to prevent going back to the login screen
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize Firebase components
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference

        un = findViewById(R.id.un)
        signupEmailEditText = findViewById(R.id.signupEmailEditText)
        signupPasswordEditText = findViewById(R.id.signupPasswordEditText)
        signupButton = findViewById(R.id.signupButton)
        loginTextView = findViewById(R.id.loginTextview)

        signupButton.setOnClickListener {
            val username = un.text.toString().trim()
            val email = signupEmailEditText.text.toString().trim()
            val password = signupPasswordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                // Create a user entry in the "users" table with the user ID
                                val userRef = database.child("users").child(userId)
                                userRef.child("username").setValue(username)
                                userRef.child("email").setValue(email)


                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish() // Close the current activity to prevent going back to the signup screen
                                Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Log.d("SignUpActivity", "Authentication failed => $email -- $password")
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter valid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        loginTextView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}

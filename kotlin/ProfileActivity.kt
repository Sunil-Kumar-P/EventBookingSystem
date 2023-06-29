package com.example.eventticketbookingsystem414

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

data class User(
    val username: String? = null,
    val email: String? = null,
    val hostedEvents: List<String>? = null,
    val joinedEvents: List<String>? = null
)

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileUsernameTextView: TextView
    private lateinit var profileEmailTextView: TextView
    private lateinit var hostedEventsContainer: LinearLayout
    private lateinit var joinedEventsContainer: LinearLayout
    private lateinit var un: String
    private lateinit var databaseReference: DatabaseReference
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUserID: FirebaseUser? = firebaseAuth.currentUser

    override fun onStart() {
        super.onStart()
        currentUserID?.let {
            un = it.uid // Get the user's unique ID
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileUsernameTextView = findViewById(R.id.profile_username_textview)
        profileEmailTextView = findViewById(R.id.profile_email_textview)
        hostedEventsContainer = findViewById(R.id.hosted_events_container)
        joinedEventsContainer = findViewById(R.id.joined_events_container)
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")
            .child(currentUserID?.uid ?: "")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.let {
                        displayUserData(it)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
    }

    private fun displayUserData(user: User) {
        profileUsernameTextView.text = user.username
        profileEmailTextView.text = user.email

        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        val eventsRef = FirebaseDatabase.getInstance().reference.child("events")

        val hostedEvents = mutableListOf<String>()
        val joinedEvents = mutableListOf<String>()

        eventsRef.orderByChild("email").equalTo(user.email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (eventSnapshot in dataSnapshot.children) {
                        val eventName = eventSnapshot.child("name").getValue(String::class.java)
                        eventName?.let {
                            hostedEvents.add(it)
                            val eventTextView = TextView(this@ProfileActivity)
                            eventTextView.text = it
                            eventTextView.gravity = Gravity.CENTER
                            eventTextView.textSize = 18f
                            eventTextView.setTypeface(null, Typeface.BOLD)
                            eventTextView.setOnClickListener {
                                val eventID = eventSnapshot.child("id").getValue(String::class.java)
                                eventID?.let {
                                    val intent = Intent(this@ProfileActivity, Event_DetailActivity::class.java)
                                    intent.putExtra("event_id", it)
                                    startActivity(intent)
                                }
                            }
                            hostedEventsContainer.addView(eventTextView)
                        }
                    }

                    if (hostedEvents.isNotEmpty()) {

                    } else {
                        val noHostedEventsTextView = TextView(this@ProfileActivity)
                        noHostedEventsTextView.text = "No hosted events yet"
                        noHostedEventsTextView.gravity = Gravity.CENTER
                        noHostedEventsTextView.textSize = 18f
                        noHostedEventsTextView.setTypeface(null, Typeface.BOLD)
                        hostedEventsContainer.addView(noHostedEventsTextView)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })

        val ticketsRef = FirebaseDatabase.getInstance().reference.child("tickets")

        ticketsRef.orderByChild("username").equalTo(currentUserID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val joinedEvents = mutableListOf<String>()

                if (dataSnapshot.exists()) {
                    val eventIDs = dataSnapshot.children.mapNotNull { it.child("eventID").getValue(String::class.java) }.distinct()

                    val eventsRef = FirebaseDatabase.getInstance().reference.child("events")
                    eventsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(eventsSnapshot: DataSnapshot) {
                            for (eventSnapshot in eventsSnapshot.children) {
                                val eventID = eventSnapshot.child("id").getValue(String::class.java)
                                val eventName = eventSnapshot.child("name").getValue(String::class.java)

                                if (eventID in eventIDs) {
                                    eventName?.let {
                                        joinedEvents.add(it)
                                        val eventTextView = TextView(this@ProfileActivity)
                                        eventTextView.text = it
                                        eventTextView.gravity = Gravity.CENTER
                                        eventTextView.textSize = 18f
                                        eventTextView.setTypeface(null, Typeface.BOLD)
                                        val eventID = eventSnapshot.child("id").getValue(String::class.java)
                                        eventTextView.setOnClickListener {
                                            val eventID = eventSnapshot.child("id").getValue(String::class.java)
                                            eventID?.let {
                                                val intent = Intent(this@ProfileActivity, Event_DetailActivity::class.java)
                                                intent.putExtra("event_id", it)
                                                startActivity(intent)
                                            }
                                        }
                                        joinedEventsContainer.addView(eventTextView)
                                    }
                                }
                            }

                            if (joinedEvents.isNotEmpty()) {
//                                for (event in joinedEvents) {
//
//                                }
                            } else {
                                val noJoinedEventsTextView = TextView(this@ProfileActivity)
                                noJoinedEventsTextView.text = "No joined events yet"
                                noJoinedEventsTextView.gravity = Gravity.CENTER
                                noJoinedEventsTextView.textSize = 18f
                                noJoinedEventsTextView.setTypeface(null, Typeface.BOLD)
                                joinedEventsContainer.addView(noJoinedEventsTextView)
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle the error
                        }
                    })
                } else {
                    val noJoinedEventsTextView = TextView(this@ProfileActivity)
                    noJoinedEventsTextView.text = "No joined events yet"
                    noJoinedEventsTextView.gravity = Gravity.CENTER
                    noJoinedEventsTextView.textSize = 18f
                    noJoinedEventsTextView.setTypeface(null, Typeface.BOLD)
                    joinedEventsContainer.addView(noJoinedEventsTextView)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
    }
}

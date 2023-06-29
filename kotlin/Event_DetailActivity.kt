package com.example.eventticketbookingsystem414

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Event_DetailActivity : AppCompatActivity() {
    private lateinit var eventNameTextView: TextView
    private lateinit var eventDescriptionTextView: TextView
    private lateinit var eventAccommodationTextView: TextView
    private lateinit var eventDateTextView: TextView
    private lateinit var eventLocationTextView: TextView
    private lateinit var registered_users_textview: LinearLayout
    private lateinit var eventId: String
    val registeredUsers = StringBuilder() // StringBuilder to store the registered users

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        eventNameTextView = findViewById(R.id.event_name_textview)
        eventDescriptionTextView = findViewById(R.id.event_description_textview)
        eventAccommodationTextView = findViewById(R.id.event_accommodation_textview)
        eventDateTextView = findViewById(R.id.event_date_textview)
        eventLocationTextView = findViewById(R.id.event_location_textview)
        registered_users_textview = findViewById(R.id.registered_users_textview)

        val intent = intent
        eventId = intent.getStringExtra("event_id").toString()

        val eventsRef = FirebaseDatabase.getInstance().reference.child("events")
        eventsRef.child(eventId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val eventName = dataSnapshot.child("name").getValue(String::class.java)
                    val eventDescription = dataSnapshot.child("description").getValue(String::class.java)
                    val eventDate = dataSnapshot.child("date").getValue(String::class.java)
                    val eventLocation = dataSnapshot.child("location").getValue(String::class.java)
                    val eventAccommodation = dataSnapshot.child("accommodationAvailable").getValue(String::class.java)

                    eventNameTextView.text = eventName
                    eventDescriptionTextView.text = eventDescription
                    eventDateTextView.text = "Date: $eventDate"
                    eventLocationTextView.text = "Location: $eventLocation"
                    eventAccommodationTextView.text = "Accommodation available: ${eventAccommodation.toString()}"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })

        val ticketsRef = FirebaseDatabase.getInstance().reference.child("tickets")
        val usersRef = FirebaseDatabase.getInstance().reference.child("users")

        ticketsRef.orderByChild("eventID").equalTo(eventId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (ticketSnapshot in dataSnapshot.children) {
                        val userId = ticketSnapshot.child("username").getValue(String::class.java)
                        usersRef.child(userId.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                if (userSnapshot.exists()) {
                                    val username = userSnapshot.child("username").getValue(String::class.java)
                                    val eventTextView = TextView(this@Event_DetailActivity)
                                    eventTextView.text = username
                                    eventTextView.gravity = Gravity.CENTER
                                    eventTextView.textSize = 18f

                                    val layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    eventTextView.layoutParams = layoutParams
                                    eventTextView.setTypeface(null, Typeface.BOLD)
                                    registered_users_textview.addView(eventTextView)
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle the error
                            }
                        })
                    }
                } else {
                    // No tickets found for the event
                    println("No tickets found for the event")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
    }
}

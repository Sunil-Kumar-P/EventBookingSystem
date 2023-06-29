package com.example.eventticketbookingsystem414

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class AllEventsActivity : AppCompatActivity() {
    private lateinit var logoutBtn: TextView
    private lateinit var hostEventBtn: TextView
    private lateinit var profileBtn: TextView
    private lateinit var eventCardsContainer: LinearLayout
    private lateinit var errorMessageTextView: TextView
    private lateinit var database: DatabaseReference
    private lateinit var un: String
    private lateinit var eventID: String

    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser: FirebaseUser? = firebaseAuth.currentUser

    public override fun onStart() {
        super.onStart()
        currentUser?.let {
            un = it.uid // Get the user's unique ID
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_events)

        val menuIcon: View = findViewById(R.id.menu_icon)
        val navigationView: View = findViewById(R.id.navigation_view)
        logoutBtn = findViewById(R.id.logout_button)
        hostEventBtn = findViewById(R.id.host_event_button)
        profileBtn = findViewById(R.id.profile_button)
        eventCardsContainer = findViewById(R.id.event_cards_container)
        errorMessageTextView = findViewById(R.id.error_message_textview)

        logoutBtn.visibility = View.GONE // Hide the logout button initially
        hostEventBtn.visibility = View.GONE // Hide the host event button initially
        navigationView.visibility = View.GONE
        errorMessageTextView.visibility = View.GONE

        menuIcon.setOnClickListener {
            toggleVisibility(navigationView)
            toggleVisibility(logoutBtn) // Toggle the visibility of the logout button
            toggleVisibility(hostEventBtn) // Toggle the visibility of the host event button
        }
        profileBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        logoutBtn.setOnClickListener {
            // Handle logout button click
            Firebase.auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close the current activity to prevent going back to the signup screen
        }

        hostEventBtn.setOnClickListener {
            // Handle host event button click
            val intent = Intent(this, HostEventActivity::class.java)
            startActivity(intent)
        }

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference.child("events")

        // Fetch events from Firebase Database and create event cards dynamically
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                eventCardsContainer.removeAllViews() // Clear existing event cards
                errorMessageTextView.visibility = View.GONE

                for (eventSnapshot in snapshot.children) {
                    val event = eventSnapshot.getValue(EventModel::class.java)
                    if (event != null) {
                        val eventCard = createEventCard(
                            eventSnapshot.key ?: "",
                            event.name,
                            event.description,
                            event.date,
                            event.location,
                            event.accommodationAvailable?.toInt(),
                            event.email
                        )
                        eventCardsContainer.addView(eventCard)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database read error
                val errorMessage = "Database read error: ${error.message}"
                Toast.makeText(this@AllEventsActivity, errorMessage, Toast.LENGTH_SHORT).show()
                errorMessageTextView.text = errorMessage
                errorMessageTextView.visibility = View.VISIBLE
            }
        })
    }

    private fun createEventCard(
        eventKey: String,
        eventName: String?,
        description: String?,
        eventDate: String?,
        eventLocation: String?,
        accommodationAvailable: Int?,
        hostedByEmail: String?
    ): CardView {
        val cardView = CardView(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 16) // Add margin between event cards
        cardView.layoutParams = layoutParams
        cardView.cardElevation = 4f

        val cardContentLayout = LinearLayout(this)
        cardContentLayout.orientation = LinearLayout.VERTICAL
        cardContentLayout.setPadding(16, 16, 16, 16)

        val eventNameTextView = TextView(this)
        eventNameTextView.text = eventName ?: ""
        eventNameTextView.textSize = 18f
        eventNameTextView.setTypeface(null, Typeface.BOLD)

        val descriptionTextView = TextView(this)
        descriptionTextView.text = description ?: ""

        val eventDateTextView = TextView(this)
        eventDateTextView.text = eventDate ?: ""

        val eventLocationTextView = TextView(this)
        eventLocationTextView.text = eventLocation ?: ""

        val accommodationTextView = TextView(this)
        accommodationTextView.text = "Accommodation available: ${accommodationAvailable ?: 0}"

        val hostedByEmailTextView = TextView(this)
        hostedByEmailTextView.text = "Hosted by: ${hostedByEmail ?: ""}"

        val bookButton = Button(this)
        bookButton.text = "Book"
        val ticketsRef = FirebaseDatabase.getInstance().reference.child("tickets")
        ticketsRef.orderByChild("username").equalTo(un).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookedEvents = mutableListOf<String>()
                for (ticketSnapshot in snapshot.children) {
                    val ticket = ticketSnapshot.getValue(TicketModel::class.java)
                    ticket?.eventID?.let { bookedEvents.add(it) }
                }

                // Disable the button if the user has already booked the ticket for this event
                if (bookedEvents.contains(eventKey)) {
                    bookButton.isEnabled = false
                    bookButton.text = "Booked"
                } else {
                    bookButton.isEnabled = true
                    bookButton.setOnClickListener {
                        bookButton.isEnabled = false // Disable the book button
                        decrementAccommodation(eventKey, accommodationAvailable)
                    }
                }

                cardContentLayout.addView(eventNameTextView)
                cardContentLayout.addView(descriptionTextView)
                cardContentLayout.addView(eventDateTextView)
                cardContentLayout.addView(eventLocationTextView)
                cardContentLayout.addView(accommodationTextView)
                cardContentLayout.addView(hostedByEmailTextView)
                cardContentLayout.addView(bookButton)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database read error
                val errorMessage = "Database read error: ${error.message}"
                Toast.makeText(this@AllEventsActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })

        cardView.addView(cardContentLayout)
        return cardView
    }

    private fun decrementAccommodation(eventKey: String, accommodationAvailable: Int?) {
        if (accommodationAvailable != null && accommodationAvailable > 0) {
            database = FirebaseDatabase.getInstance().reference.child("events")
            val updatedAccommodation = (accommodationAvailable - 1).toString()
            database.child(eventKey).child("accommodationAvailable").setValue(updatedAccommodation)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@AllEventsActivity,
                            "Accommodation booked successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@AllEventsActivity,
                            "Failed to book accommodation. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            database = FirebaseDatabase.getInstance().reference.child("tickets")
            val ticketID = database.child("tickets").push().key ?: ""
            val userRef = database.child(ticketID)
            userRef.child("username").setValue(un)
            userRef.child("eventID").setValue(eventKey)
            // Update the accommodation value in Firebase Database
        }
    }

    private fun toggleVisibility(view: View) {
        if (view.visibility == View.GONE) {
            view.visibility = View.VISIBLE // Show the view if it's hidden
        } else {
            view.visibility = View.GONE // Hide the view if it's visible
        }
    }
}

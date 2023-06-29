package com.example.eventticketbookingsystem414

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class HostEventActivity : AppCompatActivity() {

    private lateinit var eventNameEditText: EditText
    private lateinit var eventLocationEditText: EditText
    private lateinit var eventDateEditText: EditText
    private lateinit var accommodationEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var currentUserEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host_event)

        eventNameEditText = findViewById(R.id.event_name)
        eventLocationEditText = findViewById(R.id.event_location)
        eventDateEditText = findViewById(R.id.event_date)
        accommodationEditText = findViewById(R.id.accommodation_edit_text)
        descriptionEditText = findViewById(R.id.description_edit_text)
        saveButton = findViewById(R.id.host_event_button)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Get the current user's email
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUserEmail = currentUser?.email ?: ""

        saveButton.setOnClickListener {
            saveEvent()
        }
    }

    private fun saveEvent() {
        val eventName = eventNameEditText.text.toString().trim()
        val eventLocation = eventLocationEditText.text.toString().trim()
        val eventDate = eventDateEditText.text.toString().trim()
        val accommodationAvailableStr = accommodationEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()

        // Validate input
        if (eventName.isEmpty() || eventLocation.isEmpty() || eventDate.isEmpty() || accommodationAvailableStr.isEmpty() || description.isEmpty()) {
            // Show an error message if any field is empty
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate event date
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateFormat.isLenient = false
        try {
            val eventDateObj = dateFormat.parse(eventDate)
            if (eventDateObj == null) {
                // Invalid date format
                Toast.makeText(this, "Invalid event date format", Toast.LENGTH_SHORT).show()
                return
            } else {
                // Check if the event date is in the future or present
                val currentDate = Calendar.getInstance().time
                if (eventDateObj.before(currentDate)) {
                    Toast.makeText(this, "Event date should be in the future", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        } catch (e: java.text.ParseException) {
            // Invalid date format
            Toast.makeText(this, "Invalid event date format", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate accommodation number
        val accommodationAvailable: Int
        try {
            accommodationAvailable = accommodationAvailableStr.toInt()
            if (accommodationAvailable < 0) {
                Toast.makeText(this, "Accommodation number should be a positive integer", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid accommodation number", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate a unique key for the event
        val eventId = database.child("events").push().key ?: ""

        // Get current timestamp
        val timestamp = System.currentTimeMillis()

        // Create an Event object with the input data, current user's email, accommodation availability, description, and timestamp
        val event = EventModel(eventId, eventName, eventLocation, eventDate, currentUserEmail, accommodationAvailable.toString(), description, timestamp)

        // Save the event to the Firebase Database
        database.child("events").child(eventId).setValue(event)
            .addOnSuccessListener {
                Toast.makeText(this, "Event saved successfully", Toast.LENGTH_SHORT).show()

                // Clear the input fields
                eventNameEditText.text.clear()
                eventLocationEditText.text.clear()
                eventDateEditText.text.clear()
                accommodationEditText.text.clear()
                descriptionEditText.text.clear()

                // Navigate to AllEventsActivity
                val intent = Intent(this, AllEventsActivity::class.java)
                startActivity(intent)
                finish() // Close the current activity
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to save event: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

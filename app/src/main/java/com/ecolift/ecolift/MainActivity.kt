package com.ecolift.ecolift

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import android.app.PendingIntent
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {

    private lateinit var pinLocationBtn: MaterialButton
    private val latitudeOne = "43.74836909945278"
    private val longitudeOne = "-79.19189612254064"

    private val CHANNEL_ID = "example_channel_id"
    private val NOTIFICATION_ID = 1 // ID for event notification

    // Request notification permission for Android 13+
    private val requestNotificationPermission: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission(), ActivityResultCallback { granted ->
            if (granted) {
                // Permission granted, show notification
                showEventNotification()
            } else {
                // Permission denied
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request permission for Android 13+ if not already granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // If permission is already granted, show the notification
                showEventNotification()
            }
        } else {
            // For devices below Android 13, no need for POST_NOTIFICATIONS permission
            showEventNotification()
        }

        // Initialize the MaterialButton (check if import is correct)
        pinLocationBtn = findViewById(R.id.pinLocationBtn)

        pinLocationBtn.setOnClickListener {
            pinLocationMap(latitudeOne, longitudeOne)
        }

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Handle the "Join Event!" button
        val joinButton: Button = findViewById(R.id.joinButton)

        joinButton.setOnClickListener {
            // Change button text and background color
            joinButton.text = "Joined!"
            joinButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))

            // Dismiss the "event available" notification
            dismissEventNotification()

            // Show the "you've joined" notification
            showJoinedNotification()
        }
    }

    private fun showEventNotification() {
        // Create Notification Channel (Required for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Event Notifications"
            val descriptionText = "Channel for event notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Create an Intent to launch the app when the notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Create a PendingIntent to launch the app when the notification is tapped
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Build the "Event Available" notification with the pending intent
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ecolift_transparent) // Your app's icon
            .setContentTitle("New clean up opportunity nearby!")
            .setContentText("Guild Park Clean Up on November 23rd, All Day")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Set the PendingIntent here
            .setAutoCancel(true) // Dismiss the notification when clicked
            .build()

        // Issue the notification
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, notification) // Use the ID for event notification
    }

    private fun dismissEventNotification() {
        // Dismiss the existing event notification
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun showJoinedNotification() {
        // Show "You've joined" notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ecolift_transparent) // Your app's icon
            .setContentTitle("You've joined the event!")
            .setContentText("You are now signed up for the Guild Park Clean Up event.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // Issue the "you've joined" notification
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(2, notification) // New notification ID for "You've joined"
    }

    private fun pinLocationMap(latitude: String, longitude: String) {
        val mapUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
        val intent = Intent(Intent.ACTION_VIEW, mapUri)
        intent.setPackage("com.google.android.apps.maps")

        // Fallback to opening Google Maps in the browser if the app isn't available
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=$latitude,$longitude"))
            startActivity(browserIntent)
        }
    }
}

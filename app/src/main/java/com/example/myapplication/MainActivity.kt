package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.networking.OkHttpExample

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.image)
        textView = findViewById(R.id.title)
        button = findViewById(R.id.button)

        button.setOnClickListener {
            // making the button invisible
            button.visibility = GONE
            // start a network request
            OkHttpExample(URL_REQUEST)
                    .asyncCall(this)
        }

        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(messageReceiver,
                        IntentFilter(BROADCAST_EVENT_NAME))

        imageView.setImageResource(R.drawable.ic_launcher_background)

    }

    override fun onStart() {
        super.onStart()
    }

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broadcasted.
    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            // Get extra data included in the Intent
            val imageUrl = intent.getStringExtra(BROADCAST_KEY_IMAGEURL)
            loadImageWithGlide(imageUrl?:URL_IMAGE_FALLBACK)

            val title = intent.getStringExtra(BROADCAST_KEY_TITLE)
            textView.text = title
        }
    }

    private fun loadImageWithGlide(imageUrl:String) {
        val options: RequestOptions = RequestOptions()
                .centerCrop()
                .placeholder(android.R.drawable.ic_lock_lock)
                .error(android.R.drawable.stat_notify_error)
        Glide.with(imageView).load(imageUrl).apply(options).into(imageView)
    }

    override fun onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
        super.onDestroy()
    }
}


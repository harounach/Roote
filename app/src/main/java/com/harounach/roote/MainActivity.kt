package com.harounach.roote

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.harounach.roote.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    lateinit var ref: DatabaseReference


    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Firebase realtime database
        val database = Firebase.database
        ref = database.getReference("message")

        // Write to database
        binding.sendButton.setOnClickListener {
            ref.setValue(binding.editText.text.toString())
        }

        // Read from database
        ref.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", error.toException().toString())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val message = dataSnapshot.getValue(String::class.java)
                binding.resultText.text = message
            }

        })


    }
}

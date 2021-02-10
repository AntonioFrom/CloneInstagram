package com.example.cloneinstagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.cloneinstagram.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity() {
    private val TAG = "EditProfileActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        Log.e(TAG, "onCreate")

        close_image.setOnClickListener {
            finish()
        }
        val auth = FirebaseAuth.getInstance()
        val userAuth = auth.currentUser
        val database = FirebaseDatabase.getInstance().reference
        database.child("users").child(userAuth!!.uid)
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                val user = it.getValue(User::class.java)
                editprofile_name_input.setText(user!!.name, TextView.BufferType.EDITABLE)
                editprofile_username_input.setText(user.username, TextView.BufferType.EDITABLE)
                editprofile_website_input.setText(user.website, TextView.BufferType.EDITABLE)
                editprofile_bio_input.setText(user.bio, TextView.BufferType.EDITABLE)
                editprofile_email_input.setText(user.email, TextView.BufferType.EDITABLE)
                editprofile_phone_input.setText(user.phone, TextView.BufferType.EDITABLE)
            })
    }
}



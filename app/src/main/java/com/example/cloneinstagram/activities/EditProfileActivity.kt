package com.example.cloneinstagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.cloneinstagram.models.User
import com.example.cloneinstagram.views.PasswordDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity() ,PasswordDialog.Listener{

    private val TAG = "EditProfileActivity"
    private lateinit var mPendingUser: User
    private lateinit var mUser: User
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        Log.e(TAG, "onCreate")

        close_image.setOnClickListener { finish() }
        save_image.setOnClickListener { updateprofile() }

        mAuth = FirebaseAuth.getInstance()
        val userAuth = mAuth.currentUser
        mDatabase = FirebaseDatabase.getInstance().reference
        mDatabase.child("users").child(userAuth!!.uid)
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                mUser = it.getValue(User::class.java)!!
                editprofile_name_input.setText(mUser.name, TextView.BufferType.EDITABLE)
                editprofile_username_input.setText(mUser.username, TextView.BufferType.EDITABLE)
                editprofile_website_input.setText(mUser.website, TextView.BufferType.EDITABLE)
                editprofile_bio_input.setText(mUser.bio, TextView.BufferType.EDITABLE)
                editprofile_email_input.setText(mUser.email, TextView.BufferType.EDITABLE)
                editprofile_phone_input.setText(mUser.phone, TextView.BufferType.EDITABLE)
            })
    }

    private fun updateprofile() {
        mPendingUser = User(
            name = editprofile_name_input.text.toString(),
            username = editprofile_username_input.text.toString(),
            website = editprofile_website_input.text.toString(),
            bio = editprofile_bio_input.text.toString(),
            email = editprofile_email_input.text.toString(),
            phone = editprofile_phone_input.text.toString()
        )
        val error = validate(mPendingUser)
        if (error == null) {
            if (mPendingUser.email == mUser.email) {
                updateUser(mPendingUser)
            } else {
                PasswordDialog().show(supportFragmentManager, "password_dialog")
            }
        } else {
            showToast(error)
        }
    }

    override fun onPasswordConfirm(password: String) {
        val credential = EmailAuthProvider.getCredential(mUser.email,password)
        mAuth.currentUser!!.reauthenticate(credential).addOnCompleteListener {
            if (it.isSuccessful){
                mAuth.currentUser!!.updateEmail(mPendingUser.email).addOnCompleteListener{
                    if (it.isSuccessful){
                        updateUser(mPendingUser)
                    } else{
                        it.exception?.message?.let { it1 -> showToast(it1) }
                    }
                }
            } else{
                it.exception?.message?.let { it1 -> showToast(it1) }
            }
        }
    }

    private fun updateUser(user: User) {
        val updateMap = mutableMapOf<String, Any>()
        if (user.name != mUser.name) updateMap["name"] = user.name
        if (user.username != mUser.username) updateMap["username"] = user.username
        if (user.website != mUser.website) updateMap["website"] = user.website
        if (user.bio != mUser.bio) updateMap["bio"] = user.bio
        if (user.email != mUser.email) updateMap["email"] = user.email
        if (user.phone != mUser.phone) updateMap["phone"] = user.phone

        mDatabase.child("users").child(mAuth.currentUser!!.uid).updateChildren(updateMap)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    showToast("Profile saved")
                    finish()
                } else {
                    it.exception?.message?.let { it1 -> showToast(it1) }
                }
            }
    }

    private fun validate(user: User): String? =
        when {
            user.name.isEmpty() -> "Please enter name"
            user.username.isEmpty() -> "Please enter username"
            user.email.isEmpty() -> "Please enter email"
            else -> null
        }
}



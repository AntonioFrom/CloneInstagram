package com.example.cloneinstagram.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.example.cloneinstagram.EditProfileActivity
import com.example.cloneinstagram.R
import com.example.cloneinstagram.models.User
import com.example.cloneinstagram.utils.FirebaseHelper
import com.example.cloneinstagram.utils.ValueEventListenerAdapter
import com.example.cloneinstagram.utils.loadUserPhoto
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : BaseActivity(4) {

    private lateinit var mUser: User
    private lateinit var mFirebaseHelper: FirebaseHelper

    private val TAG = "ProfileActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setupBottomNavigation()
        Log.e(TAG, "onCreate")

        profile_btn.setOnClickListener{
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
        mFirebaseHelper = FirebaseHelper(this)
        mFirebaseHelper.currentUserReference().addValueEventListener(ValueEventListenerAdapter{
            mUser = it.getValue(User::class.java)!!
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                profile_images.loadUserPhoto(mUser.photo)
                username_text.text = mUser.username
            }
        })

    }
}
package com.example.cloneinstagram

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.cloneinstagram.models.User
import com.example.cloneinstagram.utils.*
import com.example.cloneinstagram.views.PasswordDialog
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_profile.*

class EditProfileActivity : AppCompatActivity(), PasswordDialog.Listener {


    private val TAG = "EditProfileActivity"
    private lateinit var mPendingUser: User
    private lateinit var mUser: User
    private lateinit var mFirebaseHelper: FirebaseHelper
    private lateinit var cameraPictureTaker: CameraPictureTaker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        Log.e(TAG, "onCreate")
        cameraPictureTaker = CameraPictureTaker(this)

        close_image.setOnClickListener { finish() }
        save_image.setOnClickListener { updateProfile() }
        change_photo_text.setOnClickListener { cameraPictureTaker.takeCameraPicture() }

        mFirebaseHelper = FirebaseHelper(this)

        mFirebaseHelper.currentUserReference()
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                mUser = it.getValue(User::class.java)!!
                editprofile_name_input.setText(mUser.name)
                editprofile_username_input.setText(mUser.username)
                editprofile_website_input.setText(mUser.website)
                editprofile_bio_input.setText(mUser.bio)
                editprofile_email_input.setText(mUser.email)
                editprofile_phone_input.setText(mUser.phone)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                circleImageView?.loadUserPhoto(mUser.photo)
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraPictureTaker.TAKE_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            mFirebaseHelper.uploadUserPhoto(cameraPictureTaker.mImageUri!!) {
                val uri = it.storage.downloadUrl
                uri.addOnCompleteListener {
                    val photoUrl = it.result.toString()
                    mFirebaseHelper.updateUserPhoto(photoUrl) {
                        mUser = mUser.copy(photo = photoUrl)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        circleImageView.loadUserPhoto(mUser.photo)
                       }
                    }
                }


            }
        }
    }

    private fun updateProfile() {
        mPendingUser = readInputs()
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

    private fun readInputs(): User {
        return User(
            name = editprofile_name_input.text.toString(),
            username = editprofile_username_input.text.toString(),
            email = editprofile_email_input.text.toString(),
            website = editprofile_website_input.text.toStringOrNull(),
            bio = editprofile_bio_input.text.toStringOrNull(),
            phone = editprofile_phone_input.text.toStringOrNull()
        )
    }

    override fun onPasswordConfirm(password: String) {
        if (password.isNotEmpty()) {
            val credential = EmailAuthProvider.getCredential(mUser.email, password)
            mFirebaseHelper.reAuthenticate(credential) {
                mFirebaseHelper.updateEmail(mPendingUser.email) {
                    updateUser(mPendingUser)
                }
            }
        } else showToast("You should enter your password")
    }

    private fun updateUser(user: User) {
        val updateMap = mutableMapOf<String, Any?>()
        if (user.name != mUser.name) updateMap["name"] = user.name
        if (user.username != mUser.username) updateMap["username"] = user.username
        if (user.website != mUser.website) updateMap["website"] = user.website
        if (user.bio != mUser.bio) updateMap["bio"] = user.bio
        if (user.email != mUser.email) updateMap["email"] = user.email
        if (user.phone != mUser.phone) updateMap["phone"] = user.phone
        mFirebaseHelper.updateUser(updateMap) {
            showToast("Profile saved")
            finish()
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



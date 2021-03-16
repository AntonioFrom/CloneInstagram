package com.example.cloneinstagram

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.cloneinstagram.models.User
import com.example.cloneinstagram.views.PasswordDialog
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity(), PasswordDialog.Listener {

    private lateinit var mStorage: StorageReference
    private val TAKE_PICTURE_REQUEST_CODE = 1
    private val TAG = "EditProfileActivity"
    private lateinit var mPendingUser: User
    private lateinit var mUser: User
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    private lateinit var mImageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        Log.e(TAG, "onCreate")

        close_image.setOnClickListener { finish() }
        save_image.setOnClickListener { updateProfile() }
        change_photo_text.setOnClickListener { takeCameraPicture() }

        mAuth = FirebaseAuth.getInstance()
        val userAuth = mAuth.currentUser
        mDatabase = FirebaseDatabase.getInstance().reference
        mStorage = FirebaseStorage.getInstance().reference
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

    @SuppressLint("QueryPermissionsNeeded")
    private fun takeCameraPicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val imageFile = createImageFile()
            mImageUri = FileProvider.getUriForFile(
                this,
                "com.example.cloneinstagram.fileprovider", imageFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)
            startActivityForResult(intent, TAKE_PICTURE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TAKE_PICTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uid = mAuth.currentUser!!.uid
            mStorage.child("users/$uid/photo").putFile(mImageUri)
                .addOnCompleteListener { taskSnapshot ->
                    if (taskSnapshot.isSuccessful) {
                        val url = taskSnapshot.result.toString()
                        mStorage.downloadUrl.addOnSuccessListener {
                            mDatabase.child("users/$uid/photo").setValue(url)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Log.d(TAG, "onActivityResult: photo saved successfully")
                                    } else {
                                        showToast(it.exception!!.message!!)
                                    }
                                }
                        }
                    } else {
                        showToast(taskSnapshot.exception!!.message!!)
                    }
                }
        }
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${simpleDateFormat.format(Date())}_",
            ".jpg",
            storageDir
        )
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
        val phoneStr = editprofile_phone_input.text.toString()
        return User(
            name = editprofile_name_input.text.toString(),
            username = editprofile_username_input.text.toString(),
            website = editprofile_website_input.text.toString(),
            bio = editprofile_bio_input.text.toString(),
            email = editprofile_email_input.text.toString(),
            phone = if (phoneStr.isEmpty()) "0" else phoneStr
        )
    }

    override fun onPasswordConfirm(password: String) {
        if (password.isNotEmpty()) {
            val credential = EmailAuthProvider.getCredential(mUser.email, password)
            mAuth.currentUser!!.reAuthenticate(credential) {
                mAuth.currentUser!!.updateEmail(mPendingUser.email) {
                    updateUser(mPendingUser)
                }
            }
        } else showToast("You should enter your password")
    }

    private fun updateUser(user: User) {
        val updateMap = mutableMapOf<String, Any>()
        if (user.name != mUser.name) updateMap["name"] = user.name
        if (user.username != mUser.username) updateMap["username"] = user.username
        if (user.website != mUser.website) updateMap["website"] = user.website
        if (user.bio != mUser.bio) updateMap["bio"] = user.bio
        if (user.email != mUser.email) updateMap["email"] = user.email
        if (user.phone != mUser.phone) updateMap["phone"] = user.phone
        mDatabase.updateUser(mAuth.currentUser!!.uid, updateMap) {
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

    private fun FirebaseUser.updateEmail(email: String, onSuccess: () -> Unit) {
        updateEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess()
            } else {
                it.exception?.message?.let { it1 -> showToast(it1) }
            }
        }
    }

    private fun FirebaseUser.reAuthenticate(credential: AuthCredential, onSuccess: () -> Unit) {
        reauthenticate(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess()
            } else {
                it.exception?.message?.let { it1 -> showToast(it1) }
            }
        }
    }

    private fun DatabaseReference.updateUser(
        uid: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit
    ) {
        child("users").child(uid).updateChildren(updates)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()
                } else {
                    it.exception?.message?.let { it1 -> showToast(it1) }
                }
            }
    }
}



package com.example.cloneinstagram.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.cloneinstagram.R
import com.example.cloneinstagram.coordinateBtnAndInputs
import com.example.cloneinstagram.models.User
import com.example.cloneinstagram.showToast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_register_email.*
import kotlinx.android.synthetic.main.fragment_register_namepass.*
import java.util.*

class RegisterActivity : AppCompatActivity(), EmailFragment.Listener, NamePassFragment.Listener {

    private val TAG = "RegisterActivity"
    private var mEmail: String? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.frame_layout, EmailFragment())
                .commit()
        }
    }

    override fun onNext(email: String) {
        if (email.isNotEmpty()) {
            mEmail = email
            mAuth.fetchSignInMethodsForEmail(email) { signInMethods ->
                if (signInMethods.isEmpty()) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, NamePassFragment())
                        .addToBackStack(null)
                        .commit()
                } else {
                    showToast("this email already exists")
                }
            }
        } else {
            showToast("Please enter email")
        }
    }

    override fun onRegister(fullname: String, password: String) {
        if (fullname.isNotEmpty() && password.isNotEmpty()) {
            val email = mEmail
            if (email != null) {
                mAuth.createUserWithEmailAndPassword(email, password) {
                    mDatabase.createUser(it.user!!.uid, makeUser(fullname, email)) {
                        startHomeActivity()
                    }
                }
            } else {
                Log.e(TAG, "onRegister: email is null")
                showToast("Please enter email")
                supportFragmentManager.popBackStack()
            }
        } else {
            showToast("Please enter full name and password")
        }
    }

    private fun FirebaseAuth.fetchSignInMethodsForEmail(
        email: String,
        onSuccess: (List<String>) -> Unit
    ) {
        fetchSignInMethodsForEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess(it.result?.signInMethods ?: emptyList<String>())
            } else {
                showToast(it.exception!!.message!!)
            }
        }
    }

    private fun FirebaseAuth.createUserWithEmailAndPassword(
        email: String,
        password: String,
        onSuccess: (AuthResult) -> Unit
    ) {
        createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess(it.result!!)
                } else {
                    unknowRegisterError(it)
                }
            }
    }

    private fun DatabaseReference.createUser(uid: String, user: User, onSuccess: () -> Unit) {
        val reference = child("users").child(uid)
        reference.setValue(user)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()
                } else {
                    unknowRegisterError(it)
                }
            }
    }

    private fun unknowRegisterError(it: Task<out Any>) {
        Log.e(TAG, "failed to create user profile", it.exception)
        showToast("Something wrong happened. Please try again")
    }

    private fun startHomeActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun makeUserName(fullname: String) =
        fullname.toLowerCase(Locale.getDefault()).replace(" ", ".")

    private fun makeUser(fullname: String, email: String): User {
        val username = makeUserName(fullname)
        return User(name = fullname, username = username, email = email)
    }

}

class EmailFragment : Fragment() {
    private lateinit var mListener: Listener

    interface Listener {
        fun onNext(email: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        coordinateBtnAndInputs(next_btn, register_email_input)
        next_btn.setOnClickListener {
            val email = register_email_input.text.toString()
            mListener.onNext(email)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }
}

class NamePassFragment : Fragment() {
    private lateinit var mListener: Listener

    interface Listener {
        fun onRegister(fullname: String, password: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_namepass, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        coordinateBtnAndInputs(register_btn, register_fullname_input, register_password_input)
        register_btn.setOnClickListener {
            val fullname = register_fullname_input.text.toString()
            val password = register_password_input.text.toString()
            mListener.onRegister(fullname, password)

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }
}
package com.example.cloneinstagram

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : BaseActivity(0) {
    private val TAG = "HomeActivity"
    private val mAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupBottomNavigation()
        Log.e(TAG, "onCreate")


        mAuth.signOut()
//        mAuth.signInWithEmailAndPassword("divo@mail.ru","password")
//            .addOnCompleteListener{
//                if (it.isSuccessful){
//                    Log.e(TAG, "SingIn: success")
//                }else{
//                    Log.e(TAG, "SingIn: failure",it.exception)
//                }
//            }

    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser == null){
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
    }
}
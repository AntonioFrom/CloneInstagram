package com.example.cloneinstagram

import android.os.Bundle
import android.util.Log

class ShareActivity : BaseActivity(2) {
    private val TAG = "ShareActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupBottomNavigation()
        Log.e(TAG, "onCreate")
    }
}
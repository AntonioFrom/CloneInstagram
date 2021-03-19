package com.example.cloneinstagram.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.cloneinstagram.R
import com.example.cloneinstagram.utils.CameraHelper
import com.example.cloneinstagram.utils.GlideApp
import kotlinx.android.synthetic.main.activity_share.*

class ShareActivity : BaseActivity(2) {
    private val TAG = "ShareActivity"
    private lateinit var mCameraHelper: CameraHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        Log.e(TAG, "onCreate")

        mCameraHelper = CameraHelper(this)
        mCameraHelper.takeCameraPicture()

        back_image.setOnClickListener { finish() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mCameraHelper.TAKE_PICTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            GlideApp.with(this).load(mCameraHelper.mImageUri).centerCrop().into(post_image)
        }
    }
}
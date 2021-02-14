package com.example.cloneinstagram.views

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.ScrollView
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener

class keyboardAwareScrollView(context: Context, attributeSet: AttributeSet) :
    ScrollView(context, attributeSet), KeyboardVisibilityEventListener {

    init {
        isFillViewport = true
        isVerticalScrollBarEnabled = false

    }

    override fun onVisibilityChanged(isKeyboardOpen: Boolean) {
        if (isKeyboardOpen) {
            scrollTo(0, bottom)
        } else {
            scrollTo(0, top)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        KeyboardVisibilityEvent.setEventListener(context as Activity, this)
    }
}
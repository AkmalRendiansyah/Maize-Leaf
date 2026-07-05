package com.akmal.maizeleaf.helper

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView

class LoadingAnimator(
    private val ringView: ImageView,
    private val logoView: ImageView
) {
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var currentRotation = 0f

    private var logoAnimator: ObjectAnimator? = null

    private val rotationRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return
            currentRotation = (currentRotation + 10f) % 360f
            ringView.rotation = currentRotation
            handler.postDelayed(this, 16) // ~60fps
        }
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        handler.post(rotationRunnable)

        logoAnimator = ObjectAnimator.ofPropertyValuesHolder(
            logoView,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.15f, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.15f, 1f)
        ).apply {
            duration = 900
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
    }

    fun stop() {
        isRunning = false
        handler.removeCallbacks(rotationRunnable)
        logoAnimator?.cancel()
        ringView.rotation = 0f
        logoView.scaleX = 1f
        logoView.scaleY = 1f
        currentRotation = 0f
    }
}
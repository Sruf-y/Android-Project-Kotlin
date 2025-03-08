package com.example.composepls

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import androidx.annotation.Px
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.hypot
import kotlin.math.roundToInt


class AccessibleExpandingFAB @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FloatingActionButton(context, attrs, defStyleAttr) {

    private var originalSize = 0
    private var maxSize = 0
    private var activationThreshold = 400f
    private var initialX = 0f
    private var initialY = 0f
    private var isActivated = false

    // Prepare interface
    interface OnActivationStateChangedListener {
        fun onActivationStateChanged(isActivated: Boolean)
    }

    private var activationListener: OnActivationStateChangedListener? = null

    // Allow setting the listener from outside
    fun setOnActivationStateChangedListener(listener: OnActivationStateChangedListener) {
        activationListener = listener // Store the listener for later use
    }



    init {
        post {
            // Initialize original and maximum size after the view is laid out
            originalSize = layoutParams.width
            maxSize = (originalSize * 2.5).roundToInt()
        }
    }

    override fun performClick(): Boolean {
        // Call super to handle accessibility and framework interactions
        if (super.performClick()) return true

        // Custom click action logic
        println("FAB clicked!")
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = event.rawX
                initialY = event.rawY

            }
            MotionEvent.ACTION_MOVE -> {
                val distance = calculateDistance(event.rawX, event.rawY)
                resizeFab(distance)
            }
            MotionEvent.ACTION_UP -> {
                val totalDistance = calculateDistance(event.rawX, event.rawY)


                if (totalDistance > activationThreshold && !isActivated) {
                    isActivated = true
                    activationListener?.onActivationStateChanged(isActivated) //call this to notify of a change
                    animateFabSize(originalSize) // Animate to max size
                } else {
                    // Smoothly return to the original size
                    animateFabSize(originalSize) // Animate back to original size


                        performClick() // Explicitly call performClick for accessibility

                }
            }
        }
        return true
    }

    private fun calculateDistance(x: Float, y: Float): Float {
        return hypot((x - initialX).toDouble(), (y - initialY).toDouble()).toFloat()
    }

    private fun resizeFab(distance: Float) {
        val newSize = (originalSize + (distance / activationThreshold) * (maxSize - originalSize))
            .coerceAtMost(maxSize.toFloat()).toInt()

        layoutParams.width = newSize
        layoutParams.height = newSize
        requestLayout()
    }

    private fun animateFabSize(@Px toSize: Int) {
        val animator = ValueAnimator.ofInt(layoutParams.width, toSize)
        animator.addUpdateListener {
            val newSize = it.animatedValue as Int
            layoutParams.width = newSize
            layoutParams.height = newSize
            requestLayout()
        }
        animator.duration = 300
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }
}

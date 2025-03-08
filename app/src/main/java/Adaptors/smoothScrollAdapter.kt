package Adaptors // Update this to match your package name

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView


class CustomRecyclerView(context: Context, attrs: AttributeSet? = null) : RecyclerView(context, attrs) {
    private val MAX_VELOCITY = 12000 // Max fling speed (px/sec)

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        val limitedVelocityX = velocityX.coerceIn(-MAX_VELOCITY, MAX_VELOCITY)
        val limitedVelocityY = velocityY.coerceIn(-MAX_VELOCITY, MAX_VELOCITY)
        return super.fling(limitedVelocityX, limitedVelocityY)
    }
}

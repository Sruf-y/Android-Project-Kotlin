package Utilities

import android.content.res.Resources


class Utils {
    companion object {
        val Int.dP: Int
            get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

        val Float.dP: Int
            get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

        val Double.dP:Int
            get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

    }
}
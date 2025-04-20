package Utilities

import android.content.res.Resources
import android.util.DisplayMetrics
import java.io.File
import kotlin.math.roundToInt
import kotlin.math.roundToLong


class Utils {
    companion object {
        val Int.dP: Int
            get() = (this/(Resources.getSystem().displayMetrics.densityDpi/160f)*9).roundToInt()

        val Float.dP: Int
            get() = (this/(Resources.getSystem().displayMetrics.densityDpi/160f)*9).roundToInt()

        val Double.dP: Int
            get() = (this/(Resources.getSystem().displayMetrics.densityDpi/160f)*9).roundToInt()


    }
}
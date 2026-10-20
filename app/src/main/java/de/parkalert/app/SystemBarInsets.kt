package de.parkalert.app

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Edge-to-edge is enforced from targetSdk 35/36 on: the app draws behind the
 * status and navigation bars. Pads this view by the system bar (and display
 * cutout) insets so content such as the settings button and the banner ad
 * stays clear of them. The view's own padding from XML is preserved.
 */
fun View.applySystemBarInsets(top: Boolean = true, bottom: Boolean = true) {
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.setPadding(
            initialLeft + insets.left,
            initialTop + if (top) insets.top else 0,
            initialRight + insets.right,
            initialBottom + if (bottom) insets.bottom else 0
        )
        windowInsets
    }
}

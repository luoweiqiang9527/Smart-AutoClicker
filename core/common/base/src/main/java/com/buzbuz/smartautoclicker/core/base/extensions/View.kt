/*
 * Copyright (C) 2025 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.buzbuz.smartautoclicker.core.base.extensions

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams


fun View.doWhenMeasured(closure: () -> Unit) {
    if (width != 0 && height != 0) {
        closure()
        return
    }

    doOnLayout { doWhenMeasured(closure) }
}

fun View.delayDrawUntil(timeOutMs: Long = DEFAULT_DRAW_DELAY_TIMEOUT_MS, closure: () -> Boolean) {
    val timeOutTs = System.currentTimeMillis() + timeOutMs

    viewTreeObserver.addOnPreDrawListener(
        object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                return if (closure() || timeOutTs < System.currentTimeMillis()) {
                    viewTreeObserver.removeOnPreDrawListener(this)
                    true
                } else {
                    false
                }
            }
        }
    )
}

fun View.applySystemBarInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        // Apply the system bar insets as a margin to the view.
        windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).let { insets ->
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin += insets.left
                bottomMargin += insets.bottom
                rightMargin += insets.right
            }
        }

        // Return CONSUMED as we don't want want the window insets to keep passing down to descendant views.
        WindowInsetsCompat.CONSUMED
    }
}

private const val DEFAULT_DRAW_DELAY_TIMEOUT_MS = 3_000L
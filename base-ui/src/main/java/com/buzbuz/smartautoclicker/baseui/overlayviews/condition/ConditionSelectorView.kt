/*
 * Copyright (C) 2021 Nain57
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */
package com.buzbuz.smartautoclicker.baseui.overlayviews.condition

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.view.KeyEvent.ACTION_UP
import android.view.MotionEvent
import android.view.View

import androidx.core.content.res.use

import com.buzbuz.smartautoclicker.baseui.overlayviews.condition.selector.Selector
import com.buzbuz.smartautoclicker.baseui.overlayviews.condition.selector.HintsController
import com.buzbuz.smartautoclicker.extensions.ScreenMetrics
import com.buzbuz.smartautoclicker.ui.R

/**
 * Overlay view used as screenOverlayView showing the area to capture the content as a click condition.
 * This view allows to zoom/move the bitmap displayed as background, as well as display a selector over it allowing to
 * easily select a section of the screen for a click condition.
 *
 * @param context the Android context
 * @param screenMetrics the current screen metrics.
 */
@SuppressLint("ViewConstructor") // Not intended to be used from XML
class ConditionSelectorView(
    context: Context,
    private val screenMetrics: ScreenMetrics
) : View(context) {

    /** */
    private val capture = Capture(context, screenMetrics, ::invalidate)
    /** */
    private lateinit var selector: Selector
    /** Controls the display of the user hints around the selector. */
    private lateinit var hintsIcons: HintsController

    /** */
    private lateinit var animations: Animations

    /** */
    init {
        context.obtainStyledAttributes(R.style.OverlaySelectorView_Condition, R.styleable.ConditionSelectorView).use { ta ->
            animations = Animations(ta)
            selector = Selector(context, ta, screenMetrics, ::invalidate)
            hintsIcons = HintsController(context, ta, screenMetrics, ::invalidate)
        }
    }

    /** */
    init {
        animations.apply {
            onCaptureZoomLevelChanged = capture::setZoomLevel
            onSelectorBorderAlphaChanged = { alpha ->
                selector.selectorAlpha = alpha
            }
            onSelectorBackgroundAlphaChanged = { alpha ->
                selector.backgroundAlpha = alpha
            }
            onHintsAlphaChanged = { alpha ->
                hintsIcons.alpha = alpha
                android.util.Log.i("TOTO", "Hints alpha = $alpha")
            }
        }

        selector.onSelectorPositionChanged = { position ->
            hintsIcons.setSelectorArea(position)
        }
    }

    /** Tell if the content of this view should be hidden or not. */
    var hide = true
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Shows the capture on the screen.
     *
     * @param bitmap the capture the be shown.
     */
    fun showCapture(bitmap: Bitmap) {
        capture.screenCapture = BitmapDrawable(resources, bitmap)
        hintsIcons.showAll()
        animations.startShowSelectorAnimation(
            onAnimationCompleted = {
                animations.startHideHintsAnimation()
            }
        )
    }

    /**
     * Get the part of the capture that is currently selected within the selector.
     *
     * @return a pair of the capture area and a bitmap of its content.
     */
    fun getSelection(): Pair<Rect, Bitmap> {
        val selectionArea = selector.getSelectionArea(capture.captureArea, capture.zoomLevel)
        return selectionArea to Bitmap.createBitmap(capture.screenCapture!!.bitmap, selectionArea.left,
            selectionArea.top, selectionArea.width(), selectionArea.height())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        selector.onViewSizeChanged(w, h)
        capture.onViewSizeChanged(w, h)
        hintsIcons.onViewSizeChanged(w, h)
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        selector.currentGesture?.let { gestureType ->
            hintsIcons.show(gestureType)
            animations.cancelHideHintsAnimation()

            if (event.action == ACTION_UP) {
                animations.startHideHintsAnimation()
            }
        }

        return selector.onTouchEvent(event) || capture.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        if (hide) {
            return
        }

        capture.onDraw(canvas)
        selector.onDraw(canvas)
        hintsIcons.onDraw(canvas)
    }
}
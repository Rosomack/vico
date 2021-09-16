/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.core.component.text

import android.graphics.Canvas
import android.graphics.Color.DKGRAY
import android.graphics.Paint
import android.graphics.Typeface
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import pl.patrykgoworowski.vico.core.DEF_LABEL_LINE_COUNT
import pl.patrykgoworowski.vico.core.component.dimension.DefaultMargins
import pl.patrykgoworowski.vico.core.component.dimension.DefaultPadding
import pl.patrykgoworowski.vico.core.component.dimension.Margins
import pl.patrykgoworowski.vico.core.component.dimension.Padding
import pl.patrykgoworowski.vico.core.component.shape.ShapeComponent
import pl.patrykgoworowski.vico.core.extension.half
import pl.patrykgoworowski.vico.core.extension.lineHeight
import pl.patrykgoworowski.vico.core.extension.piRad
import pl.patrykgoworowski.vico.core.extension.sp
import pl.patrykgoworowski.vico.core.shape.Shape
import pl.patrykgoworowski.vico.core.text.staticLayout
import pl.patrykgoworowski.vico.core.text.widestLineWidth
import kotlin.math.roundToInt

typealias OnPreDrawListener =
            (canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) -> Unit

public open class TextComponent(
    color: Int = DKGRAY,
    textSize: Float = 12f.sp,
    public val ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END,
    public val lineCount: Int = DEF_LABEL_LINE_COUNT,
    public open var background: ShapeComponent<Shape>? = null,
) : Padding by DefaultPadding(), Margins by DefaultMargins() {

    public val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    public val lineHeight: Int
        get() = textPaint.lineHeight.roundToInt()

    public val allLinesHeight: Int
        get() = lineHeight * lineCount

    public var isLTR: Boolean = true
    public var color: Int by textPaint::color
    public var textSize: Float by textPaint::textSize
    public var typeface: Typeface by textPaint::typeface
    public var rotationDegrees: Float = 0f
    private var layout: StaticLayout = staticLayout("", textPaint, 0)

    private val layoutCache = HashMap<Int, StaticLayout>()

    init {
        textPaint.color = color
        textPaint.textSize = textSize
    }

    public fun drawText(
        canvas: Canvas,
        text: CharSequence,
        textX: Float,
        textY: Float,
        horizontalPosition: HorizontalPosition = HorizontalPosition.Center,
        verticalPosition: VerticalPosition = VerticalPosition.Center,
        width: Int = Int.MAX_VALUE,
        onPreDraw: OnPreDrawListener? = null,
    ) {

        if (text.isBlank()) return
        layout = getLayout(text, width)
        val layoutWidth = layout.widestLineWidth
        val layoutHeight = layout.height

        val textStartPosition = horizontalPosition.getTextStartPosition(textX, layoutWidth)
        val textTopPosition = verticalPosition.getTextTopPosition(textY, layoutHeight.toFloat())

        val bgLeft = textStartPosition - padding.getLeft(isLTR)
        val bgTop = textTopPosition - ((layoutHeight / 2) + padding.top)
        val bgRight = textStartPosition + layoutWidth + padding.getRight(isLTR)
        val bgBottom = textTopPosition + ((layoutHeight / 2) + padding.bottom)

        onPreDraw?.invoke(canvas, bgLeft, bgTop, bgRight, bgBottom)

        background?.draw(
            canvas = canvas,
            left = bgLeft,
            top = bgTop,
            right = bgRight,
            bottom = bgBottom,
        )

        val centeredY = textTopPosition - layoutHeight.half

        canvas.save()
        if (rotationDegrees != 0f) {
            canvas.rotate(0.25f.piRad, textStartPosition, textTopPosition)
        }
        canvas.translate(textStartPosition, centeredY)

        layout.draw(canvas)

        canvas.restore()
    }

    private fun HorizontalPosition.getTextStartPosition(baseXPosition: Float, width: Float) =
        when (this) {
            HorizontalPosition.Start ->
                if (isLTR) getTextLeftPosition(baseXPosition)
                else getTextRightPosition(baseXPosition, width)
            HorizontalPosition.Center ->
                baseXPosition - width.half
            HorizontalPosition.End ->
                if (isLTR) getTextRightPosition(baseXPosition, width)
                else getTextLeftPosition(baseXPosition)
        }

    private fun getTextLeftPosition(baseXPosition: Float): Float =
        baseXPosition + padding.getLeft(isLTR) + margins.getLeft(isLTR)

    private fun getTextRightPosition(baseXPosition: Float, width: Float): Float =
        baseXPosition - (padding.getRight(isLTR) + margins.getRight(isLTR) + width)

    @JvmName("getTextTopPositionExt")
    private fun VerticalPosition.getTextTopPosition(
        textY: Float,
        layoutHeight: Float,
    ) = when (this) {
        VerticalPosition.Top -> textY + layoutHeight.half + padding.top + margins.top
        VerticalPosition.Center -> textY
        VerticalPosition.Bottom -> textY - (layoutHeight.half + padding.bottom + margins.bottom)
    }

    public fun getTextTopPosition(
        verticalPosition: VerticalPosition,
        textY: Float,
        layoutHeight: Float,
    ) = verticalPosition.getTextTopPosition(textY, layoutHeight)

    public fun getWidth(text: CharSequence): Float {
        return getLayout(text).widestLineWidth + padding.horizontal + margins.horizontal
    }

    public fun getHeight(
        text: CharSequence = TEXT_MEASUREMENT_CHAR,
        width: Int = Int.MAX_VALUE,
        includePadding: Boolean = true,
        includeMargin: Boolean = true,
    ): Float = getLayout(text, width).height +
            (if (includePadding) padding.vertical else 0f) +
            (if (includeMargin) margins.vertical else 0f)

    public fun clearLayoutCache() {
        layoutCache.clear()
    }

    private fun getLayout(text: CharSequence, width: Int = Int.MAX_VALUE): StaticLayout =
        layoutCache.getOrPut(text.hashCode() + HASHING_MULTIPLIER * width) {
            staticLayout(text, textPaint, width, maxLines = lineCount, ellipsize = ellipsize)
        }

    companion object {
        const val TEXT_MEASUREMENT_CHAR = "1"
        const val HASHING_MULTIPLIER = 31
    }
}

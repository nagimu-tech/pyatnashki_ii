package com.example.pyatnashki

import android.graphics.*
import android.graphics.drawable.Drawable

class Tile3DDrawable(
    private val baseColor: Int,
    private val borderColor: Int,
    private val cornerPx: Float
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mainFace = RectF()
    private val shadowFace = RectF()

    override fun draw(canvas: Canvas) {
        val w = bounds.width().toFloat()
        val h = bounds.height().toFloat()
        val d = (minOf(w, h) * 0.09f).coerceAtLeast(5f)

        // Shadow (bottom-right extruded face)
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = adj(baseColor, 0.30f)
        shadowFace.set(d, d, w, h)
        canvas.drawRoundRect(shadowFace, cornerPx, cornerPx, paint)

        // Main face — gradient top→bottom (lighter→darker)
        mainFace.set(0f, 0f, w - d, h - d)
        paint.shader = LinearGradient(
            0f, 0f, 0f, h - d,
            adj(baseColor, 1.60f),
            adj(baseColor, 0.78f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(mainFace, cornerPx, cornerPx, paint)
        paint.shader = null

        // Top-edge white highlight
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = d * 0.30f
        paint.color = Color.argb(85, 255, 255, 255)
        canvas.drawLine(cornerPx + 2f, d * 0.16f, w - d - cornerPx - 2f, d * 0.16f, paint)

        // Left-edge white highlight
        canvas.drawLine(d * 0.16f, cornerPx + 2f, d * 0.16f, h - d - cornerPx - 2f, paint)

        // Border around main face
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = borderColor
        mainFace.inset(1f, 1f)
        canvas.drawRoundRect(mainFace, cornerPx, cornerPx, paint)
    }

    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun setColorFilter(cf: ColorFilter?) { paint.colorFilter = cf }
    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.TRANSLUCENT

    private fun adj(color: Int, f: Float) = Color.argb(
        Color.alpha(color),
        (Color.red(color)   * f).toInt().coerceIn(0, 255),
        (Color.green(color) * f).toInt().coerceIn(0, 255),
        (Color.blue(color)  * f).toInt().coerceIn(0, 255)
    )
}

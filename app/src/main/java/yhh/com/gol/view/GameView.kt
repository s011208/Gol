package yhh.com.gol.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import timber.log.Timber

class GameView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    private var drawingBitmap: Bitmap? = null

    fun update(bitmap: Bitmap) {
        drawingBitmap = bitmap
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (drawingBitmap != null) {
            canvas.drawBitmap(drawingBitmap!!, 0f, 0f, null)
        }
    }
}
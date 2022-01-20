package org.wonday.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.util.Log
import android.util.SparseArray
import androidx.core.util.getOrElse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class PdfBitmapPool(
    val pdfRenderer: PdfRenderer,
    val config: Bitmap.Config,
    private val densityDpi: Int,
    val onDataChanged: OnDataChanged
) {

    private val bitmaps: SparseArray<Bitmap> = SparseArray()

    init {
        val t = measureTimeMillis {
            val page = pdfRenderer.openPage(0)
            val width = page.width.toPixelDimension()
            val height = page.height.toPixelDimension()
            val emptyPage = newWhiteBitmap(width, height)
            page.close()
            for (i in 0 until pdfRenderer.pageCount) {
                bitmaps.append(i, emptyPage)
            }
            for (i in 0 until pdfRenderer.pageCount) {
                CoroutineScope(Dispatchers.Default).launch {
                    val bitmap = loadPage(i)
                    bitmaps.remove(i)
                    bitmaps.append(i, bitmap)
                    withContext(Dispatchers.Main) {
                        onDataChanged.onDataChanged(i)
                    }
                }
            }
        }

        Log.d("BitmapPool", t.toString())
    }

    companion object {
        const val PDF_RESOLUTION_DPI = 72
    }

    fun getPage(index: Int): Bitmap {
        return bitmaps.getOrElse(index) {
            loadPage(index)
        }
    }

    private fun loadPage(pageIndex: Int): Bitmap {
        synchronized(pdfRenderer) {
            val page = pdfRenderer.openPage(pageIndex)
            val bitmap =
                newWhiteBitmap(page.width.toPixelDimension(), page.height.toPixelDimension())
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            return bitmap
        }
    }

    private fun newWhiteBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        return bitmap
    }

    private fun Int.toPixelDimension(scaleFactor: Float = 0.4f): Int {
        return ((densityDpi * this / PDF_RESOLUTION_DPI) * scaleFactor).toInt()
    }
}
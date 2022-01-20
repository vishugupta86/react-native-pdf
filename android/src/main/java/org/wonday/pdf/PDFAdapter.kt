package org.wonday.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.github.chrisbanes.photoview.PhotoView
import java.io.File

class PDFAdapter(
    file: File,
    private val context: Context
) : RecyclerView.Adapter<PDFAdapter.PDFPageViewHolder>(), OnDataChanged {

    private val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    private val pdfRenderer = PdfRenderer(fileDescriptor)

    val bitmapPool = PdfBitmapPool(
        pdfRenderer,
        Bitmap.Config.ARGB_8888,
        context.resources.displayMetrics.densityDpi,
        this
    )

    inner class PDFPageViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(position: Int) {
            val pageView = view.findViewById<PhotoView>(R.id.page)
            pageView.setImageBitmap(bitmapPool.getPage(position))
            pageView.attacher.scaleType = ImageView.ScaleType.FIT_XY
            //bitmapPool.loadMore(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PDFPageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.pdf_page_layout, parent, false)
        return PDFPageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PDFPageViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = pdfRenderer.pageCount

    override fun onDataChanged(position: Int) {
        notifyItemChanged(position)
    }
}

interface OnDataChanged {
    fun onDataChanged(position: Int)
}
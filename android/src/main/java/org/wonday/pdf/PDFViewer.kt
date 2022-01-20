package org.wonday.pdf

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

 open class PDFViewer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : ConstraintLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_pdf_viewer, this, true)
    }

    fun loadFile(file: File) {
        try {
            val adapter = PDFAdapter(file, context)
            val view = findViewById<RecyclerView>(R.id.pdf_view)
            view.adapter = adapter

            with(view) {
                clipToPadding = false
                clipChildren = false
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            }
        } catch (ex: Exception) {
            Log.e("log", "Exception in PDFViewer::loadFile() = ", ex)
        }
    }

}
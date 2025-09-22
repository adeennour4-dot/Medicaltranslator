package com.medical.translator.ui

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.medical.translator.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class PdfPreviewAdapter(
    private val pdfPath: String,
    private val pageCount: Int
) : RecyclerView.Adapter<PdfPreviewAdapter.PageViewHolder>() {

    private var pdfRenderer: PdfRenderer? = null
    private val renderedBitmaps = mutableMapOf<Int, Bitmap>()

    init {
        initializePdfRenderer()
    }

    private fun initializePdfRenderer() {
        try {
            val parcelFileDescriptor = ParcelFileDescriptor.open(
                java.io.File(pdfPath),
                ParcelFileDescriptor.MODE_READ_ONLY
            )
            pdfRenderer = PdfRenderer(parcelFileDescriptor)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    inner class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.pdf_page_image)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        val pageNumberText: TextView = itemView.findViewById(R.id.page_number_text)
        val errorText: TextView = itemView.findViewById(R.id.error_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pdf_page, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.pageNumberText.text = "Page ${position + 1}"
        holder.progressBar.visibility = View.VISIBLE
        holder.errorText.visibility = View.GONE

        // Check if we already rendered this page
        renderedBitmaps[position]?.let { bitmap ->
            holder.imageView.setImageBitmap(bitmap)
            holder.progressBar.visibility = View.GONE
            return
        }

        // Render the page in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = renderPage(position)
                withContext(Dispatchers.Main) {
                    renderedBitmaps[position] = bitmap
                    holder.imageView.setImageBitmap(bitmap)
                    holder.progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    holder.progressBar.visibility = View.GONE
                    holder.errorText.visibility = View.VISIBLE
                    holder.errorText.text = "Failed to load page"
                }
            }
        }
    }

    private fun renderPage(position: Int): Bitmap {
        val renderer = pdfRenderer ?: throw IOException("PDF renderer not initialized")
        
        // Use synchronized block to prevent multiple page access
        synchronized(this) {
            val page = renderer.openPage(position)
            try {
                // Calculate dimensions while maintaining aspect ratio
                val width = page.width
                val height = page.height
                
                // Create bitmap with proper configuration
                val bitmap = Bitmap.createBitmap(
                    width,
                    height,
                    Bitmap.Config.ARGB_8888
                )
                
                // Render the page
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                return bitmap
            } finally {
                page.close()
            }
        }
    }

    override fun getItemCount(): Int = pageCount

    fun cleanup() {
        renderedBitmaps.values.forEach { bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        renderedBitmaps.clear()
        
        pdfRenderer?.close()
        pdfRenderer = null
    }

    override fun onViewRecycled(holder: PageViewHolder) {
        super.onViewRecycled(holder)
        holder.imageView.setImageBitmap(null)
    }
}
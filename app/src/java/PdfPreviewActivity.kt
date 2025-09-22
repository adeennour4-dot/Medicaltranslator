package com.medical.translator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.medical.translator.databinding.ActivityPdfPreviewBinding
import com.medical.translator.ui.PdfPreviewAdapter
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.File

class PdfPreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfPreviewBinding
    private lateinit var pdfAdapter: PdfPreviewAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pdfPath = intent.getStringExtra("pdf_path")
        if (pdfPath != null) {
            setupPdfPreview(pdfPath)
        } else {
            finish()
        }
    }

    private fun setupPdfPreview(pdfPath: String) {
        try {
            val document = PDDocument.load(File(pdfPath))
            val pageCount = document.numberOfPages
            document.close()

            recyclerView = binding.pdfRecyclerView
            pdfAdapter = PdfPreviewAdapter(pdfPath, pageCount)
            
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = pdfAdapter
            
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::pdfAdapter.isInitialized) {
            pdfAdapter.cleanup()
        }
    }
}
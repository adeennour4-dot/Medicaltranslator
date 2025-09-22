package com.medical.translator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.medical.translator.databinding.ActivityMainBinding
import com.medical.translator.services.TranslationPipeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var translationPipeline: TranslationPipeline
    private var selectedPdfUri: Uri? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize translation pipeline
        translationPipeline = TranslationPipeline(this)
        
        // Setup button listeners
        binding.btnSelectPdf.setOnClickListener { selectPdf() }
        binding.btnTranslate.setOnClickListener { startTranslation() }
        binding.btnExport.setOnClickListener { exportPdf() }
    }
    
    private fun selectPdf() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        startActivityForResult(intent, PDF_SELECTION_CODE)
    }
    
    private fun startTranslation() {
        selectedPdfUri?.let { uri ->
            binding.progressBar.visibility = android.view.View.VISIBLE
            binding.btnTranslate.isEnabled = false
            binding.statusText.text = "Starting translation..."
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = translationPipeline.processPdf(uri)
                    
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = android.view.View.GONE
                        binding.statusText.text = "Translation complete!"
                        binding.btnExport.isEnabled = true
                        Toast.makeText(this@MainActivity, "Translation completed successfully", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = android.view.View.GONE
                        binding.btnTranslate.isEnabled = true
                        binding.statusText.text = "Error: ${e.message}"
                        Toast.makeText(this@MainActivity, "Translation failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } ?: run {
            Toast.makeText(this, "Please select a PDF file first", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun exportPdf() {
        Toast.makeText(this, "Export functionality would be implemented here", Toast.LENGTH_SHORT).show()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PDF_SELECTION_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                selectedPdfUri = uri
                binding.selectedFileText.text = "Selected: ${getFileName(uri)}"
                binding.btnTranslate.isEnabled = true
                binding.statusText.text = "PDF selected. Ready to translate."
            }
        }
    }
    
    private fun getFileName(uri: Uri): String {
        var result = ""
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    result = cursor.getString(displayNameIndex)
                }
            }
        }
        return result
    }
    
    companion object {
        private const val PDF_SELECTION_CODE = 101
    }
}
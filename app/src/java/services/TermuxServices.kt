package com.medical.translator.services

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class TermuxService(private val context: Context) {
    
    fun areServicesRunning(): Boolean {
        return isServiceRunning(8010) && isServiceRunning(8082)
    }
    
    fun setupTermuxEnvironment() {
        // Copy setup scripts to Termux directory
        copyAssetToTermux("termux_scripts/setup_termux.sh", "setup_termux.sh")
        copyAssetToTermux("termux_scripts/start_services.sh", "start_services.sh")
        
        // Execute setup script
        executeTermuxCommand("chmod +x ~/setup_termux.sh && ~/setup_termux.sh")
    }
    
    fun startServices() {
        executeTermuxCommand("~/start_services.sh")
    }
    
    private fun isServiceRunning(port: Int): Boolean {
        return try {
            val url = URL("http://127.0.0.1:$port")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 2000
            connection.readTimeout = 2000
            connection.requestMethod = "GET"
            connection.responseCode == 200
        } catch (e: IOException) {
            false
        }
    }
    
    private fun copyAssetToTermux(assetPath: String, destinationName: String) {
        try {
            context.assets.open(assetPath).use { inputStream ->
                val termuxFile = java.io.File("/data/data/com.termux/files/home/$destinationName")
                java.io.FileOutputStream(termuxFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: IOException) {
            Log.e("TermuxService", "Failed to copy asset: $assetPath", e)
        }
    }
    
    private fun executeTermuxCommand(command: String) {
        try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "termux-exec", "bash", "-c", command))
        } catch (e: IOException) {
            Log.e("TermuxService", "Failed to execute command: $command", e)
        }
    }
}
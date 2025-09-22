
package com.medical.translator.services

import android.content.Context
import org.json.JSONObject
import java.io.InputStream

class MedicalDictionary(private val context: Context) {
    private val dictionary: Map<String, String> by lazy {
        loadDictionaryFromJson()
    }
    
    private fun loadDictionaryFromJson(): Map<String, String> {
        val terms = mutableMapOf<String, String>()
        
        try {
            val inputStream: InputStream = context.assets.open("dictionary.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                terms[key] = jsonObject.getString(key)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return terms
    }
    
    fun translateTerm(term: String): String? {
        // Try exact match first
        val cleanTerm = term.toLowerCase().trim()
        val exactMatch = dictionary[cleanTerm]
        if (exactMatch != null) {
            return exactMatch
        }
        
        // Try partial matches for multi-word terms
        return dictionary.entries
            .firstOrNull { it.key.contains(cleanTerm) }
            ?.value
    }
}
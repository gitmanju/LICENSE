package com.dheemai.treecounter.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit

object InaturalistService {

    // Get your token from: https://www.inaturalist.org/users/api_token
    private const val API_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VyX2lkIjoxMDI5NjQwMywiZXhwIjoxNzc0ODQyMzU2fQ.iNrF0aCdSpjyH-6ol0THa046TEVgAnYhyWYDBct_tYAcDD-ddUUQYo3vzaZc5Hf530HzAgQKLfp9_YIdl5puZQ"

    private const val MAX_DIMENSION = 1024
    private const val JPEG_QUALITY = 85

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    data class IdentificationResult(
        val commonName: String,
        val scientificName: String,
        val score: Double
    )

    private fun compressImage(photoPath: String): ByteArray {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(photoPath, options)

        val scale = maxOf(options.outWidth, options.outHeight) / MAX_DIMENSION
        val sampleSize = if (scale > 1) scale else 1

        val bitmap = BitmapFactory.decodeFile(photoPath, BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        })

        return ByteArrayOutputStream().also { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            bitmap.recycle()
        }.toByteArray()
    }

    suspend fun identifyTree(photoPath: String): Result<IdentificationResult> = withContext(Dispatchers.IO) {
        try {
            val imageBytes = compressImage(photoPath)

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image", File(photoPath).name,
                    imageBytes.toRequestBody("image/jpeg".toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url("https://api.inaturalist.org/v1/computervision/score_image?taxon_id=47126")
                .addHeader("Authorization", API_TOKEN)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()
                ?: return@withContext Result.failure(Exception("Empty response from server"))

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Server error ${response.code}: $body"))
            }

            val json = JSONObject(body)
            val results = json.getJSONArray("results")
            if (results.length() == 0) {
                return@withContext Result.failure(Exception("No species identified"))
            }

            // Find the top plant result, ignoring animals, birds, etc.
            var top: org.json.JSONObject? = null
            for (i in 0 until results.length()) {
                val candidate = results.getJSONObject(i)
                val iconicTaxon = candidate.getJSONObject("taxon")
                    .optString("iconic_taxon_name", "")
                if (iconicTaxon == "Plantae") {
                    top = candidate
                    break
                }
            }
            if (top == null) {
                return@withContext Result.failure(Exception("No plant identified in this photo"))
            }
            val taxon = top.getJSONObject("taxon")
            val common = taxon.optString("preferred_common_name", "").ifBlank { null }
            val scientific = taxon.getString("name")
            val score = top.getDouble("combined_score")

            Result.success(
                IdentificationResult(
                    commonName = common ?: scientific,
                    scientificName = scientific,
                    score = score
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

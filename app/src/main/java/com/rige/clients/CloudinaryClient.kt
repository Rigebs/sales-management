package com.rige.clients

import android.content.Context
import android.net.Uri
import android.util.Log
import com.rige.BuildConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object CloudinaryClient {

    private const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/${BuildConfig.CLOUDINARY_CLOUD_NAME}/image/upload"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val requestBody = inputStream?.readBytes()

        if (requestBody == null) {
            callback(null)
            return
        }

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "image.jpg",
                requestBody.toRequestBody("image/*".toMediaTypeOrNull())
            )
            .addFormDataPart("upload_preset", BuildConfig.CLOUDINARY_UPLOAD_PRESET)
            .build()

        val request = Request.Builder()
            .url(UPLOAD_URL)
            .post(multipartBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("CloudinaryClient", "Upload failed: ${e.message}")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                Log.d("CloudinaryClient", "Response: $responseBody")

                val json = JSONObject(responseBody)
                if (json.has("secure_url")) {
                    val imageUrl = json.getString("secure_url")
                    callback(imageUrl)
                } else {
                    callback(null)
                }
            }
        })
    }
}
package com.radiogapp.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.android.exoplayer2.MediaMetadata
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.ByteArrayOutputStream

class SharedPrefHelper(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
    private val customGson: Gson

    init {
        // Creating a custom Gson instance with the CharSequenceAdapter registered
        customGson = GsonBuilder()
            .registerTypeAdapter(CharSequence::class.java, CharSequenceAdapter())
            .create()
    }

    fun saveMetaData(metaData: MediaMetadata) {
        val json = customGson.toJson(metaData)
        sharedPreferences.edit().putString("meta_data", json).apply()
    }

    fun getMetaData(): MediaMetadata? {
        val json = sharedPreferences.getString("meta_data", "")
        return if (json == null) null else customGson.fromJson(json, MediaMetadata::class.java)
    }

    fun saveImage(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        val imageEncoded = Base64.encodeToString(b, Base64.DEFAULT)
        sharedPreferences.edit().putString("image", imageEncoded).apply()
    }

    fun getImage(): Bitmap? {
        val imageEncoded = sharedPreferences.getString("image", null) ?: return null
        val imageAsBytes = Base64.decode(imageEncoded.toByteArray(), Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
    }

    // Custom TypeAdapter for CharSequence
    private class CharSequenceAdapter : TypeAdapter<CharSequence>() {
        override fun write(out: JsonWriter, value: CharSequence?) {
            out.value(value?.toString())
        }

        override fun read(input: JsonReader): CharSequence {
            return input.nextString()
        }
    }
}

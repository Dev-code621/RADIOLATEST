package com.radiogapp.app.utils

import android.util.Log
import com.radiogapp.app.interfaces.MetadataListener

class MyMetadataListener : MetadataListener {
    var artist: String? = null
    var song: String? = null
    var imageUrl: String? = null
    override fun onMetadataReceived(artist: String?, song: String?, imageUrl: String?) {
        // Handle the received metadata here
        this.artist = artist
        this.song = song
        this.imageUrl = imageUrl
        Log.d("mainData***","$artist$song$imageUrl")
        println("Artist: $artist")
        println("Song: $song")
        println("Image URL: $imageUrl")
    }
}
package com.radiogapp.app.interfaces

interface MetadataListener {
    fun onMetadataReceived(artist: String?, song: String?, imageUrl: String?)
}
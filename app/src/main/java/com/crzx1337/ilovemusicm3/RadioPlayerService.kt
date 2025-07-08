package com.crzx1337.ilovemusicm3

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Metadata
import androidx.media3.extractor.metadata.icy.IcyHeaders
import androidx.media3.extractor.metadata.icy.IcyInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RadioPlayerService : Service() {
    
    private var exoPlayer: ExoPlayer? = null
    private val binder = RadioPlayerBinder()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _currentStation = MutableStateFlow("")
    val currentStation: StateFlow<String> = _currentStation.asStateFlow()
    
    private val _currentSong = MutableStateFlow("")
    val currentSong: StateFlow<String> = _currentSong.asStateFlow()
    
    private val _currentArtist = MutableStateFlow("")
    val currentArtist: StateFlow<String> = _currentArtist.asStateFlow()
    
    override fun onCreate() {
        super.onCreate()
        initializePlayer()
    }
    
    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    _isLoading.value = playbackState == Player.STATE_BUFFERING
                    _isPlaying.value = playbackState == Player.STATE_READY && playWhenReady
                }
                
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }
                
                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    super.onMediaMetadataChanged(mediaMetadata)
                    
                    val title = mediaMetadata.title?.toString() ?: ""
                    val artist = mediaMetadata.artist?.toString() ?: ""
                    
                    // Debug logging to check if metadata is available
                    android.util.Log.d("RadioPlayerService", "Metadata changed - Title: '$title', Artist: '$artist'")
                    android.util.Log.d("RadioPlayerService", "Full metadata: $mediaMetadata")
                    
                    _currentSong.value = title
                    _currentArtist.value = artist
                }
                
                override fun onMetadata(metadata: Metadata) {
                    super.onMetadata(metadata)
                    
                    for (i in 0 until metadata.length()) {
                        val entry = metadata.get(i)
                        android.util.Log.d("RadioPlayerService", "ICY Metadata entry: $entry")
                        
                        when (entry) {
                            is IcyInfo -> {
                                val icyTitle = entry.title ?: ""
                                android.util.Log.d("RadioPlayerService", "ICY Info - Title: '$icyTitle'")
                                
                                // Parse ICY title which usually contains "Artist - Song"
                                if (icyTitle.contains(" - ")) {
                                    val parts = icyTitle.split(" - ", limit = 2)
                                    if (parts.size == 2) {
                                        _currentArtist.value = parts[0].trim()
                                        _currentSong.value = parts[1].trim()
                                        android.util.Log.d("RadioPlayerService", "Parsed ICY - Artist: '${parts[0].trim()}', Song: '${parts[1].trim()}'")
                                    }
                                } else if (icyTitle.isNotEmpty()) {
                                    // If no separator, treat as song title
                                    _currentSong.value = icyTitle
                                    _currentArtist.value = ""
                                    android.util.Log.d("RadioPlayerService", "ICY title without separator: '$icyTitle'")
                                }
                            }
                            is IcyHeaders -> {
                                android.util.Log.d("RadioPlayerService", "ICY Headers - Name: ${entry.name}, Genre: ${entry.genre}, Bitrate: ${entry.bitrate}")
                            }
                        }
                    }
                }
            })
        }
    }
    
    fun playRadio(streamUrl: String, stationName: String) {
        exoPlayer?.let { player ->
            val mediaItem = MediaItem.fromUri(streamUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true
            _currentStation.value = stationName
        }
    }
    
    fun pauseRadio() {
        exoPlayer?.playWhenReady = false
    }
    
    fun resumeRadio() {
        exoPlayer?.playWhenReady = true
    }
    
    fun stopRadio() {
        exoPlayer?.stop()
        _currentStation.value = ""
        _currentSong.value = ""
        _currentArtist.value = ""
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
    
    inner class RadioPlayerBinder : Binder() {
        fun getService(): RadioPlayerService = this@RadioPlayerService
    }
}
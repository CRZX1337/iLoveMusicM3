package com.crzx1337.ilovemusicm3

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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
                    
                    _currentSong.value = title
                    _currentArtist.value = artist
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
package com.crzx1337.ilovemusicm3

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RadioStation(
    val name: String,
    val streamUrl: String,
    val description: String
)

class RadioViewModel : ViewModel() {
    
    private var radioService: RadioPlayerService? = null
    private var isBound = false
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    val currentStation: StateFlow<RadioStation?> = _currentStation.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _currentSong = MutableStateFlow("")
    val currentSong: StateFlow<String> = _currentSong.asStateFlow()
    
    private val _currentArtist = MutableStateFlow("")
    val currentArtist: StateFlow<String> = _currentArtist.asStateFlow()
    
    // Available radio stations
    val radioStations = listOf(
        RadioStation(
            name = "iLoveRadio",
            streamUrl = "https://play.ilovemusic.de/ilm_iloveradio/",
            description = "The best music mix"
        ),
        RadioStation(
            name = "iLove2Dance",
            streamUrl = "https://play.ilovemusic.de/ilm_ilove2dance/",
            description = "Electronic dance music"
        ),
        RadioStation(
            name = "iLove2000+ Throwbacks",
            streamUrl = "https://play.ilovemusic.de/ilm_ilove2000throwbacks/",
            description = "The best throwback hits from 2000+"
        ),
        RadioStation(
            name = "iLoveBiggest POP Hits",
            streamUrl = "https://play.ilovemusic.de/ilm_ilovenewpop/",
            description = "The biggest pop hits"
        ),
        RadioStation(
            name = "iLoveCHILLHOP",
            streamUrl = "https://play.ilovemusic.de/ilm_ilovechillhop/",
            description = "Relaxing chillhop beats"
        ),
        RadioStation(
            name = "iLove2Dance 2025",
            streamUrl = "https://play.ilovemusic.de/ilm_dance-2023-jahrescharts/",
            description = "Dance hits of 2025"
        ),
        RadioStation(
            name = "iLoveDEUTSCHRAP BESTE",
            streamUrl = "https://play.ilovemusic.de/ilm_ilovedeutschrapbeste/",
            description = "The best German rap"
        ),
        RadioStation(
            name = "iLoveDEUTSCHRAP FIRST",
            streamUrl = "https://play.ilovemusic.de/ilm_ilovedeutschrapfirst/",
            description = "First German rap hits"
        ),
        RadioStation(
            name = "iLoveHIPHOP 2025",
            streamUrl = "https://play.ilovemusic.de/ilm_ilovehiphop/",
            description = "Hip hop hits of 2025"
        ),
        RadioStation(
            name = "iLoveHIPHOP History",
            streamUrl = "https://play.ilovemusic.de/ilm_ilovehiphophistory/",
            description = "Classic hip hop history"
        ),
        RadioStation(
            name = "iLoveHits 2025",
            streamUrl = "https://play.ilovemusic.de/ilm_hits-2023-jahrescharts/",
            description = "The biggest hits of 2025"
        )
    )
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RadioPlayerService.RadioPlayerBinder
            radioService = binder.getService()
            isBound = true
            
            // Observe service state
            viewModelScope.launch {
                radioService?.isPlaying?.collect {
                    _isPlaying.value = it
                }
            }
            
            viewModelScope.launch {
                radioService?.isLoading?.collect {
                    _isLoading.value = it
                }
            }
            
            viewModelScope.launch {
                radioService?.currentSong?.collect {
                    _currentSong.value = it
                }
            }
            
            viewModelScope.launch {
                radioService?.currentArtist?.collect {
                    _currentArtist.value = it
                }
            }
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            radioService = null
            isBound = false
        }
    }
    
    fun bindService(context: Context) {
        val intent = Intent(context, RadioPlayerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    fun unbindService(context: Context) {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
    }
    
    fun playStation(station: RadioStation) {
        try {
            radioService?.playRadio(station.streamUrl, station.name)
            _currentStation.value = station
            _errorMessage.value = null
        } catch (e: Exception) {
            _errorMessage.value = "Failed to play radio: ${e.message}"
        }
    }
    
    fun pauseRadio() {
        radioService?.pauseRadio()
    }
    
    fun resumeRadio() {
        radioService?.resumeRadio()
    }
    
    fun stopRadio() {
        radioService?.stopRadio()
        _currentStation.value = null
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        radioService = null
    }
}
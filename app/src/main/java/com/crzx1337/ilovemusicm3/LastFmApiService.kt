package com.crzx1337.ilovemusicm3

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

data class LastFmResponse(
    val results: LastFmResults?
)

data class LastFmResults(
    val trackmatches: TrackMatches?
)

data class TrackMatches(
    val track: List<Track>?
)

data class Track(
    val name: String?,
    val artist: String?,
    val image: List<ImageInfo>?
)

data class ImageInfo(
    @SerializedName("#text")
    val text: String?,
    val size: String?
)

interface LastFmApiService {
    @GET("2.0/")
    suspend fun searchTrack(
        @Query("method") method: String = "track.search",
        @Query("track") track: String,
        @Query("artist") artist: String = "",
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 1
    ): LastFmResponse

    companion object {
        private const val BASE_URL = "https://ws.audioscrobbler.com/"
        // Free Last.fm API key - you can get your own at https://www.last.fm/api
        const val API_KEY = "abc6726ccb53459bd14e729a55e03f13"

        fun create(): LastFmApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(LastFmApiService::class.java)
        }
    }
}

class AlbumCoverRepository {
    private val apiService = LastFmApiService.create()

    suspend fun getAlbumCover(songTitle: String, artist: String): String? {
        return try {
            if (songTitle.isBlank() || artist.isBlank()) return null
            
            val response = apiService.searchTrack(
                track = songTitle,
                artist = artist,
                apiKey = LastFmApiService.API_KEY
            )
            
            // Get the largest available image
            response.results?.trackmatches?.track?.firstOrNull()?.image
                ?.filter { !it.text.isNullOrBlank() }
                ?.maxByOrNull { imageInfo ->
                    when (imageInfo.size) {
                        "extralarge" -> 4
                        "large" -> 3
                        "medium" -> 2
                        "small" -> 1
                        else -> 0
                    }
                }?.text
        } catch (e: Exception) {
            null
        }
    }
}
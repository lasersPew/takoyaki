package tachiyomi.domain.entries.anime.model

import eu.kanade.tachiyomi.animesource.model.AnimeUpdateStrategy
import tachiyomi.core.preference.TriState
import tachiyomi.domain.entries.anime.interactor.GetCustomAnimeInfo
import uy.kohesive.injekt.injectLazy
import java.io.Serializable
import kotlin.math.pow

data class Anime(
    val id: Long,
    val source: Long,
    val favorite: Boolean,
    val lastUpdate: Long,
    val nextUpdate: Long,
    val fetchInterval: Int,
    val dateAdded: Long,
    val viewerFlags: Long,
    val episodeFlags: Long,
    val coverLastModified: Long,
    val url: String,
    // AM (CU) -->
    val ogTitle: String,
    val ogArtist: String?,
    val ogAuthor: String?,
    val ogDescription: String?,
    val ogGenre: List<String>?,
    val ogStatus: Long,
    // <-- AM (CU)
    val thumbnailUrl: String?,
    val updateStrategy: AnimeUpdateStrategy,
    val initialized: Boolean,
    val lastModifiedAt: Long,
    val favoriteModifiedAt: Long?,
) : Serializable {

    // AM (CU) -->
    private val customAnimeInfo = if (favorite) {
        getCustomAnimeInfo.get(id)
    } else {
        null
    }

    val title: String
        get() = customAnimeInfo?.title ?: ogTitle

    val author: String?
        get() = customAnimeInfo?.author ?: ogAuthor

    val artist: String?
        get() = customAnimeInfo?.artist ?: ogArtist

    val description: String?
        get() = customAnimeInfo?.description ?: ogDescription

    val genre: List<String>?
        get() = customAnimeInfo?.genre ?: ogGenre

    val status: Long
        get() = customAnimeInfo?.status ?: ogStatus
    // <-- AM (CU)

    val sorting: Long
        get() = episodeFlags and EPISODE_SORTING_MASK

    val displayMode: Long
        get() = episodeFlags and EPISODE_DISPLAY_MASK

    val unseenFilterRaw: Long
        get() = episodeFlags and EPISODE_UNSEEN_MASK

    val downloadedFilterRaw: Long
        get() = episodeFlags and EPISODE_DOWNLOADED_MASK

    val bookmarkedFilterRaw: Long
        get() = episodeFlags and EPISODE_BOOKMARKED_MASK

    // AM (FILLER) -->
    val fillermarkedFilterRaw: Long
        get() = episodeFlags and EPISODE_FILLERMARKED_MASK
    // <-- AM (FILLER)

    val skipIntroLength: Int
        get() = (viewerFlags and ANIME_INTRO_MASK).toInt()

    val nextEpisodeToAir: Int
        get() = (viewerFlags and ANIME_AIRING_EPISODE_MASK).removeHexZeros(zeros = 2).toInt()

    val nextEpisodeAiringAt: Long
        get() = (viewerFlags and ANIME_AIRING_TIME_MASK).removeHexZeros(zeros = 6)

    val unseenFilter: TriState
        get() = when (unseenFilterRaw) {
            EPISODE_SHOW_UNSEEN -> TriState.ENABLED_IS
            EPISODE_SHOW_SEEN -> TriState.ENABLED_NOT
            else -> TriState.DISABLED
        }

    val bookmarkedFilter: TriState
        get() = when (bookmarkedFilterRaw) {
            EPISODE_SHOW_BOOKMARKED -> TriState.ENABLED_IS
            EPISODE_SHOW_NOT_BOOKMARKED -> TriState.ENABLED_NOT
            else -> TriState.DISABLED
        }

    // AM (FILLER) -->
    val fillermarkedFilter: TriState
        get() = when (fillermarkedFilterRaw) {
            EPISODE_SHOW_FILLERMARKED -> TriState.ENABLED_IS
            EPISODE_SHOW_NOT_FILLERMARKED -> TriState.ENABLED_NOT
            else -> TriState.DISABLED
        }
    // <-- AM (FILLER)

    fun sortDescending(): Boolean {
        return episodeFlags and EPISODE_SORT_DIR_MASK == EPISODE_SORT_DESC
    }

    private fun Long.removeHexZeros(zeros: Int): Long {
        val hex = 16.0
        return this.div(hex.pow(zeros)).toLong()
    }

    companion object {
        // Generic filter that does not filter anything
        const val SHOW_ALL = 0x00000000L

        const val EPISODE_SORT_DESC = 0x00000000L
        const val EPISODE_SORT_ASC = 0x00000001L
        const val EPISODE_SORT_DIR_MASK = 0x00000001L

        const val EPISODE_SHOW_UNSEEN = 0x00000002L
        const val EPISODE_SHOW_SEEN = 0x00000004L
        const val EPISODE_UNSEEN_MASK = 0x00000006L

        const val EPISODE_SHOW_DOWNLOADED = 0x00000008L
        const val EPISODE_SHOW_NOT_DOWNLOADED = 0x00000010L
        const val EPISODE_DOWNLOADED_MASK = 0x00000018L

        const val EPISODE_SHOW_BOOKMARKED = 0x00000020L
        const val EPISODE_SHOW_NOT_BOOKMARKED = 0x00000040L
        const val EPISODE_BOOKMARKED_MASK = 0x00000060L

        // AM (FILLER) -->
        const val EPISODE_SHOW_FILLERMARKED = 0x00000080L
        const val EPISODE_SHOW_NOT_FILLERMARKED = 0x00000100L
        const val EPISODE_FILLERMARKED_MASK = 0x00000180L

        const val EPISODE_SORTING_SOURCE = 0x00000000L
        const val EPISODE_SORTING_NUMBER = 0x00000200L
        const val EPISODE_SORTING_UPLOAD_DATE = 0x00000400L
        const val EPISODE_SORTING_ALPHABET = 0x00000600L
        const val EPISODE_SORTING_MASK = 0x00000600L
        // <-- AM (FILLER)

        const val EPISODE_DISPLAY_NAME = 0x00000000L
        const val EPISODE_DISPLAY_NUMBER = 0x00100000L
        const val EPISODE_DISPLAY_MASK = 0x00100000L

        const val ANIME_INTRO_MASK = 0x000000000000FFL
        const val ANIME_AIRING_EPISODE_MASK = 0x00000000FFFF00L
        const val ANIME_AIRING_TIME_MASK = 0xFFFFFFFF000000L

        fun create() = Anime(
            id = -1L,
            url = "",
            // AM (CU) -->
            ogTitle = "",
            // <-- AM (CU)
            source = -1L,
            favorite = false,
            lastUpdate = 0L,
            nextUpdate = 0L,
            fetchInterval = 0,
            dateAdded = 0L,
            viewerFlags = 0L,
            episodeFlags = 0L,
            coverLastModified = 0L,
            // AM (CU) -->
            ogArtist = null,
            ogAuthor = null,
            ogDescription = null,
            ogGenre = null,
            ogStatus = 0L,
            // <-- AM (CU)
            thumbnailUrl = null,
            updateStrategy = AnimeUpdateStrategy.ALWAYS_UPDATE,
            initialized = false,
            lastModifiedAt = 0L,
            favoriteModifiedAt = null,
        )

        // AM (CU) -->
        private val getCustomAnimeInfo: GetCustomAnimeInfo by injectLazy()
        // <-- AM (CU)
    }
}

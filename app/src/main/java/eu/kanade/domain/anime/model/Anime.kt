package eu.kanade.domain.anime.model

import eu.kanade.data.listOfStringsAdapter
import eu.kanade.tachiyomi.animesource.LocalAnimeSource
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.data.animelib.CustomAnimeManager
import eu.kanade.tachiyomi.data.cache.AnimeCoverCache
import eu.kanade.tachiyomi.data.database.models.AnimeImpl
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.widget.ExtendedNavigationView
import tachiyomi.animesource.model.AnimeInfo
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy
import java.io.Serializable
import eu.kanade.tachiyomi.data.database.models.Anime as DbAnime

data class Anime(
    val id: Long,
    val source: Long,
    val favorite: Boolean,
    val lastUpdate: Long,
    val dateAdded: Long,
    val viewerFlags: Long,
    val episodeFlags: Long,
    val coverLastModified: Long,
    val url: String,
    // AM -->
    val ogTitle: String,
    val ogArtist: String?,
    val ogAuthor: String?,
    val ogDescription: String?,
    val ogGenre: List<String>?,
    val ogStatus: Long,
    // AM <--
    val thumbnailUrl: String?,
    val initialized: Boolean,
) : Serializable {

    // AM -->
    private val customAnimeInfo = if (favorite) {
        customAnimeManager.getAnime(this)
    } else null

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
        get() = customAnimeInfo?.statusLong ?: ogStatus
    // AM <--

    val sorting: Long
        get() = episodeFlags and EPISODE_SORTING_MASK

    fun toSAnime(): SAnime = SAnime.create().also {
        it.url = url
        it.title = title
        it.artist = artist
        it.author = author
        it.description = description
        it.genre = genre.orEmpty().joinToString()
        it.status = status.toInt()
        it.thumbnail_url = thumbnailUrl
        it.initialized = initialized
    }

    val displayMode: Long
        get() = episodeFlags and EPISODE_DISPLAY_MASK

    val unseenFilterRaw: Long
        get() = episodeFlags and EPISODE_UNSEEN_MASK

    val downloadedFilterRaw: Long
        get() = episodeFlags and EPISODE_DOWNLOADED_MASK

    val bookmarkedFilterRaw: Long
        get() = episodeFlags and EPISODE_BOOKMARKED_MASK

    val fillermarkedFilterRaw: Long
        get() = episodeFlags and EPISODE_FILLERMARKED_MASK

    val unseenFilter: TriStateFilter
        get() = when (unseenFilterRaw) {
            EPISODE_SHOW_UNSEEN -> TriStateFilter.ENABLED_IS
            EPISODE_SHOW_SEEN -> TriStateFilter.ENABLED_NOT
            else -> TriStateFilter.DISABLED
        }

    val downloadedFilter: TriStateFilter
        get() {
            if (forceDownloaded()) return TriStateFilter.ENABLED_IS
            return when (downloadedFilterRaw) {
                EPISODE_SHOW_DOWNLOADED -> TriStateFilter.ENABLED_IS
                EPISODE_SHOW_NOT_DOWNLOADED -> TriStateFilter.ENABLED_NOT
                else -> TriStateFilter.DISABLED
            }
        }

    val bookmarkedFilter: TriStateFilter
        get() = when (bookmarkedFilterRaw) {
            EPISODE_SHOW_BOOKMARKED -> TriStateFilter.ENABLED_IS
            EPISODE_SHOW_NOT_BOOKMARKED -> TriStateFilter.ENABLED_NOT
            else -> TriStateFilter.DISABLED
        }

    val fillermarkedFilter: TriStateFilter
        get() = when (fillermarkedFilterRaw) {
            EPISODE_SHOW_FILLERMARKED -> TriStateFilter.ENABLED_IS
            EPISODE_SHOW_NOT_FILLERMARKED -> TriStateFilter.ENABLED_NOT
            else -> TriStateFilter.DISABLED
        }

    fun episodesFiltered(): Boolean {
        return unseenFilter != TriStateFilter.DISABLED ||
            downloadedFilter != TriStateFilter.DISABLED ||
            bookmarkedFilter != TriStateFilter.DISABLED ||
            fillermarkedFilter != TriStateFilter.DISABLED
    }

    fun forceDownloaded(): Boolean {
        return favorite && Injekt.get<PreferencesHelper>().downloadedOnly().get()
    }

    fun sortDescending(): Boolean {
        return episodeFlags and EPISODE_SORT_DIR_MASK == EPISODE_SORT_DESC
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

        const val EPISODE_SHOW_FILLERMARKED = 0x00000200L
        const val EPISODE_SHOW_NOT_FILLERMARKED = 0x00000400L
        const val EPISODE_FILLERMARKED_MASK = 0x00000600L

        const val EPISODE_SORTING_SOURCE = 0x00000000L
        const val EPISODE_SORTING_NUMBER = 0x00000100L
        const val EPISODE_SORTING_UPLOAD_DATE = 0x00000200L
        const val EPISODE_SORTING_MASK = 0x00000300L

        const val EPISODE_DISPLAY_NAME = 0x00000000L
        const val EPISODE_DISPLAY_NUMBER = 0x00100000L
        const val EPISODE_DISPLAY_MASK = 0x00100000L

        // AM -->
        private val customAnimeManager: CustomAnimeManager by injectLazy()
        // AM <--

        fun create() = Anime(
            id = -1L,
            url = "",
            // AM -->
            ogTitle = "",
            // AM <--
            source = -1L,
            favorite = false,
            lastUpdate = -1L,
            dateAdded = -1L,
            viewerFlags = -1L,
            episodeFlags = -1L,
            coverLastModified = -1L,
            // AM -->
            ogArtist = null,
            ogAuthor = null,
            ogDescription = null,
            ogGenre = null,
            ogStatus = 0L,
            // AM <--
            thumbnailUrl = null,
            initialized = false,
        )
    }
}

enum class TriStateFilter {
    DISABLED, // Disable filter
    ENABLED_IS, // Enabled with "is" filter
    ENABLED_NOT, // Enabled with "not" filter
}

fun TriStateFilter.toTriStateGroupState(): ExtendedNavigationView.Item.TriStateGroup.State {
    return when (this) {
        TriStateFilter.DISABLED -> ExtendedNavigationView.Item.TriStateGroup.State.IGNORE
        TriStateFilter.ENABLED_IS -> ExtendedNavigationView.Item.TriStateGroup.State.INCLUDE
        TriStateFilter.ENABLED_NOT -> ExtendedNavigationView.Item.TriStateGroup.State.EXCLUDE
    }
}

// TODO: Remove when all deps are migrated
fun Anime.toDbAnime(): DbAnime = AnimeImpl().also {
    it.id = id
    it.source = source
    it.favorite = favorite
    it.last_update = lastUpdate
    it.date_added = dateAdded
    it.viewer_flags = viewerFlags.toInt()
    it.episode_flags = episodeFlags.toInt()
    it.cover_last_modified = coverLastModified
    it.url = url
    // AM -->
    it.title = ogTitle
    it.artist = ogArtist
    it.author = ogAuthor
    it.description = ogDescription
    it.genre = ogGenre?.let(listOfStringsAdapter::encode)
    it.status = ogStatus.toInt()
    // AM <--
    it.thumbnail_url = thumbnailUrl
    it.initialized = initialized
}

fun Anime.toAnimeInfo(): AnimeInfo = AnimeInfo(
    // AM -->
    artist = ogArtist ?: "",
    author = ogAuthor ?: "",
    cover = thumbnailUrl ?: "",
    description = ogDescription ?: "",
    genres = ogGenre ?: emptyList(),
    key = url,
    status = ogStatus.toInt(),
    title = ogTitle,
    // AM <--
)

fun Anime.toAnimeUpdate(): AnimeUpdate {
    return AnimeUpdate(
        id = id,
        source = source,
        favorite = favorite,
        lastUpdate = lastUpdate,
        dateAdded = dateAdded,
        viewerFlags = viewerFlags,
        episodeFlags = episodeFlags,
        coverLastModified = coverLastModified,
        url = url,
        title = title,
        artist = artist,
        author = author,
        description = description,
        genre = genre,
        status = status,
        thumbnailUrl = thumbnailUrl,
        initialized = initialized,
    )
}

fun Anime.isLocal(): Boolean = source == LocalAnimeSource.ID

fun Anime.hasCustomCover(coverCache: AnimeCoverCache = Injekt.get()): Boolean {
    return coverCache.getCustomCoverFile(id).exists()
}
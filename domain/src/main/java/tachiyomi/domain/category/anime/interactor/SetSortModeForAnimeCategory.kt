package tachiyomi.domain.category.anime.interactor

import tachiyomi.domain.category.anime.repository.AnimeCategoryRepository
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.category.model.CategoryUpdate
import tachiyomi.domain.library.anime.model.AnimeLibrarySort
import tachiyomi.domain.library.model.AnimeLibraryGroup
import tachiyomi.domain.library.model.plus
import tachiyomi.domain.library.service.LibraryPreferences

class SetSortModeForAnimeCategory(
    private val preferences: LibraryPreferences,
    private val categoryRepository: AnimeCategoryRepository,
) {

    suspend fun await(
        categoryId: Long?,
        type: AnimeLibrarySort.Type,
        direction: AnimeLibrarySort.Direction,
    ) {
        // AM (GU) -->
        if (preferences.groupLibraryBy().get() != AnimeLibraryGroup.BY_DEFAULT) {
            preferences.animeSortingMode().set(AnimeLibrarySort(type, direction))
            return
        }
        // <-- AM (GU)
        val category = categoryId?.let { categoryRepository.getAnimeCategory(it) }
        val flags = (category?.flags ?: 0) + type + direction
        if (category != null && preferences.categorizedDisplaySettings().get()) {
            categoryRepository.updatePartialAnimeCategory(
                CategoryUpdate(
                    id = category.id,
                    flags = flags,
                ),
            )
        } else {
            preferences.animeSortingMode().set(AnimeLibrarySort(type, direction))
            categoryRepository.updateAllAnimeCategoryFlags(flags)
        }
    }

    suspend fun await(
        category: Category?,
        type: AnimeLibrarySort.Type,
        direction: AnimeLibrarySort.Direction,
    ) {
        await(category?.id, type, direction)
    }
}

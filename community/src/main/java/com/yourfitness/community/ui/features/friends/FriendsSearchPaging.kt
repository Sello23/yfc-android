package com.yourfitness.community.ui.features.friends

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yourfitness.community.data.entity.FriendsEntity
import timber.log.Timber

class SearchFriendsPagingSource(
    private val fetch: suspend (Int) -> List<FriendsEntity>?
) : PagingSource<Int, FriendsEntity>() {
    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, FriendsEntity> {
        return try {
            val nextPageNumber = params.key ?: 0
            val data = fetch(nextPageNumber).orEmpty()
            LoadResult.Page(
                data = data,
                prevKey = null,
                nextKey = if (data.size < 20) null else nextPageNumber + 20
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, FriendsEntity>): Int? {
        Timber.tag("issue").e(">>> anchorPosition = ${state.anchorPosition},  ")
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            Timber.tag("issue").e(
                ">>>[][][] prevKey = ${anchorPage?.prevKey?.plus(20)} ?: nextKey = ${
                    anchorPage?.nextKey?.minus(20)
                }\n=================================================================="
            )
            anchorPage?.prevKey?.plus(20) ?: anchorPage?.nextKey?.minus(20)
        }
    }
}

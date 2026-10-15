package com.topten.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed interface FeedState {
    data object Loading : FeedState
    data class Loaded(
        val items: List<NewsItem>,
        val fetchedAt: Long,
        val refreshing: Boolean = false,
        val refreshError: String? = null,
    ) : FeedState
    data class Failed(val message: String) : FeedState
}

class NewsViewModel : ViewModel() {

    private val _feeds = MutableStateFlow<Map<Category, FeedState>>(emptyMap())
    val feeds: StateFlow<Map<Category, FeedState>> = _feeds.asStateFlow()

    private val jobs = mutableMapOf<Category, Job>()

    /** Loads a tab the first time it's shown, and again once its stories are 15+ minutes old. */
    fun ensureLoaded(category: Category) {
        val state = _feeds.value[category]
        val isStale = state is FeedState.Loaded &&
            System.currentTimeMillis() - state.fetchedAt > STALE_AFTER_MS
        if (state == null || isStale) load(category)
    }

    fun refresh(category: Category) = load(category)

    private fun load(category: Category) {
        if (jobs[category]?.isActive == true) return
        val previous = _feeds.value[category] as? FeedState.Loaded
        setState(category, previous?.copy(refreshing = true, refreshError = null) ?: FeedState.Loading)

        jobs[category] = viewModelScope.launch {
            val result: FeedState = try {
                FeedState.Loaded(NewsRepository.topTen(category), System.currentTimeMillis())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                previous?.copy(refreshing = false, refreshError = describe(e))
                    ?: FeedState.Failed(describe(e))
            }
            setState(category, result)
        }
    }

    private fun setState(category: Category, state: FeedState) =
        _feeds.update { it + (category to state) }

    private fun describe(e: Exception): String = when (e) {
        is UnknownHostException -> "No internet connection. Check your network and try again."
        is SocketTimeoutException -> "The news service took too long to respond. Try again."
        else -> "Couldn't load stories right now. Try again."
    }

    private companion object {
        const val STALE_AFTER_MS = 15 * 60 * 1000L
    }
}

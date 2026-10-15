package com.topten.news

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { TopTenTheme { NewsApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsApp(viewModel: NewsViewModel = viewModel()) {
    val categories = Category.entries
    val pagerState = rememberPagerState { categories.size }
    val scope = rememberCoroutineScope()
    val feeds by viewModel.feeds.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Top Ten", fontWeight = FontWeight.Black) },
                    actions = {
                        IconButton(onClick = { viewModel.refresh(categories[pagerState.currentPage]) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh stories")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background),
                )
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 12.dp,
                    containerColor = colors.background,
                    contentColor = colors.primary,
                ) {
                    categories.forEachIndexed { index, category ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(category.label) },
                            unselectedContentColor = colors.onSurfaceVariant,
                        )
                    }
                }
            }
        },
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(padding).fillMaxSize(),
        ) { page ->
            val category = categories[page]
            CategoryPage(
                state = feeds[category],
                onShown = { viewModel.ensureLoaded(category) },
                onRefresh = { viewModel.refresh(category) },
                category = category,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPage(
    category: Category,
    state: FeedState?,
    onShown: () -> Unit,
    onRefresh: () -> Unit,
) {
    LaunchedEffect(category) { onShown() }

    when (state) {
        null, FeedState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
        }
        is FeedState.Failed -> MessageView(state.message, "Try again", onRefresh)
        is FeedState.Loaded -> PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (state.items.isEmpty()) {
                MessageView("No recent stories for ${category.label} yet.", "Refresh", onRefresh)
            } else {
                StoryList(state)
            }
        }
    }
}

@Composable
private fun StoryList(state: FeedState.Loaded) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Text(
                text = state.refreshError ?: updatedLabel(state.fetchedAt),
                style = MaterialTheme.typography.labelMedium,
                color = if (state.refreshError != null) colors.error else colors.onSurfaceVariant,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
            )
        }
        itemsIndexed(state.items, key = { _, item -> item.link }) { index, item ->
            StoryRow(rank = index + 1, item = item, onClick = { openLink(context, item.link) })
            if (index < state.items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 72.dp, end = 20.dp),
                    color = colors.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun StoryRow(rank: Int, item: NewsItem, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = rank.toString(),
            style = TextStyle(fontSize = 34.sp, lineHeight = 34.sp, fontWeight = FontWeight.Black),
            color = colors.secondary,
            modifier = Modifier.width(52.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp,
                ),
                color = colors.onSurface,
            )
            val meta = listOfNotNull(
                item.source.ifBlank { null },
                item.publishedAt?.let { relativeTime(it) },
            ).joinToString(", ")
            if (meta.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(meta, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MessageView(message: String, action: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onAction) { Text(action) }
    }
}

private fun relativeTime(time: Long): String =
    DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()

private fun updatedLabel(fetchedAt: Long): String =
    if (System.currentTimeMillis() - fetchedAt < 60_000) "Updated just now"
    else "Updated ${relativeTime(fetchedAt)}"

private fun openLink(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Install a browser to read the full story.", Toast.LENGTH_SHORT).show()
    }
}

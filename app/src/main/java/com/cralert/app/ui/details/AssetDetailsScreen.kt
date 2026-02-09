package com.cralert.app.ui.details

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cralert.app.R
import com.cralert.app.data.HistoricalPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailsScreen(
    viewModel: AssetDetailsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.observeAsState(AssetDetailsState())
    val context = LocalContext.current
    val selectedPoint = remember { mutableStateOf<HistoricalPoint?>(null) }

    LaunchedEffect(state.hasError, state.error) {
        if (state.hasError) {
            val message = state.error?.takeIf { it.isNotBlank() } ?: context.getString(R.string.error_history_load)
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            R.string.title_asset_details
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${state.baseSymbol}/${state.quoteSymbol} · ${state.baseName}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = state.currentPrice?.let { formatPrice(it, state.quoteSymbol) }
                    ?: "--",
                style = MaterialTheme.typography.headlineMedium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.label_history_range),
                    style = MaterialTheme.typography.labelLarge
                )
                if (state.fromCache) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.label_history_cached),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HistoryChart(
                points = state.points,
                selectedPoint = selectedPoint.value,
                onSelect = { selectedPoint.value = it }
            )
            selectedPoint.value?.let { point ->
                Text(
                    text = stringResource(
                        R.string.label_history_selected,
                        formatDate(point.timeMs),
                        formatPrice(point.price, state.quoteSymbol)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = stringResource(R.string.label_history_min), value = state.min?.let { formatPrice(it, state.quoteSymbol) })
                StatItem(label = stringResource(R.string.label_history_max), value = state.max?.let { formatPrice(it, state.quoteSymbol) })
                StatItem(
                    label = stringResource(R.string.label_history_change),
                    value = state.changePercent?.let { formatPercent(it) }
                )
            }
            Text(
                text = stringResource(R.string.label_history_updated, formatTimestamp(state.updatedAt)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (state.hasError) {
                Button(onClick = { viewModel.retry() }) {
                    Text(text = stringResource(R.string.action_retry))
                }
            }
        }
    }
}

@Composable
private fun HistoryChart(
    points: List<HistoricalPoint>,
    selectedPoint: HistoricalPoint?,
    onSelect: (HistoricalPoint) -> Unit
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val crosshairColor = MaterialTheme.colorScheme.secondary
    val scale = remember { mutableStateOf(1f) }
    val center = remember { mutableStateOf(0.5f) }
    val maxScale = 8f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        if (points.size < 2) {
            Text(
                text = "--",
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return
        }
        val visibleRange = computeVisibleRange(points.size, scale.value, center.value, maxScale)
        val selectedIndex = selectedPoint?.let { point ->
            val index = points.indexOfFirst { it.timeMs == point.timeMs }
            if (index in visibleRange.startIndex..visibleRange.endIndex) index else null
        }
        val visiblePoints = points.subList(visibleRange.startIndex, visibleRange.endIndex + 1)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            scale.value = 1f
                            center.value = 0.5f
                        }
                    )
                }
                .pointerInput(points) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val width = size.width.toFloat().coerceAtLeast(1f)
                        val oldScale = scale.value
                        val newScale = (oldScale * zoom).coerceIn(1f, maxScale)
                        val oldVisible = 1f / oldScale
                        val newVisible = 1f / newScale
                        val focus = (centroid.x / width).coerceIn(0f, 1f)
                        val oldStart = (center.value - oldVisible / 2f).coerceIn(0f, 1f - oldVisible)
                        val anchor = oldStart + focus * oldVisible
                        val panFraction = -pan.x / width / oldScale
                        var newCenter = anchor - (focus - 0.5f) * newVisible + panFraction
                        val minCenter = newVisible / 2f
                        val maxCenterValue = 1f - newVisible / 2f
                        newCenter = newCenter.coerceIn(minCenter, maxCenterValue)
                        scale.value = newScale
                        center.value = newCenter
                        if (zoom == 1f) {
                            selectPoint(centroid, width, points, visibleRange, onSelect)
                        }
                    }
                }
        ) {
            val min = visiblePoints.minOf { it.price }
            val max = visiblePoints.maxOf { it.price }
            val range = (max - min).takeIf { it > 0.0 } ?: 1.0
            if (visiblePoints.size < 2) return@Canvas
            val stepX = size.width / (visiblePoints.size - 1)
            val path = Path()
            visiblePoints.forEachIndexed { index, point ->
                val x = stepX * index
                val normalized = (point.price - min) / range
                val y = size.height - (normalized * size.height).toFloat()
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            if (selectedIndex != null) {
                val localIndex = selectedIndex - visibleRange.startIndex
                val point = points[selectedIndex]
                val x = stepX * localIndex
                val normalized = (point.price - min) / range
                val y = size.height - (normalized * size.height).toFloat()
                drawLine(
                    color = crosshairColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 2f
                )
                drawCircle(
                    color = crosshairColor,
                    radius = 6f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value ?: "--", style = MaterialTheme.typography.titleMedium)
    }
}

private fun formatPrice(value: Double, symbol: String): String {
    return String.format(Locale.US, "%.2f %s", value, symbol)
}

private fun formatPercent(value: Double): String {
    return String.format(Locale.US, "%.2f%%", value)
}

private fun formatTimestamp(value: Long): String {
    if (value == 0L) return "--"
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return format.format(Date(value))
}

private fun formatDate(value: Long): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.format(Date(value))
}

private fun selectPoint(
    offset: Offset,
    width: Float,
    points: List<HistoricalPoint>,
    visibleRange: VisibleRange,
    onSelect: (HistoricalPoint) -> Unit
) {
    if (points.size < 2 || width <= 0f) return
    val ratio = (offset.x / width).coerceIn(0f, 1f)
    val localIndex = ((visibleRange.length - 1) * ratio).roundToInt()
        .coerceIn(0, visibleRange.length - 1)
    val index = visibleRange.startIndex + localIndex
    onSelect(points[index])
}

private data class VisibleRange(
    val startIndex: Int,
    val endIndex: Int,
    val length: Int
)

private fun computeVisibleRange(
    total: Int,
    scale: Float,
    center: Float,
    maxScale: Float
): VisibleRange {
    val safeTotal = max(2, total)
    val clampedScale = scale.coerceIn(1f, maxScale)
    val visibleFraction = 1f / clampedScale
    val visibleCount = max(2, ceil(safeTotal * visibleFraction).toInt())
    val centerIndex = ((safeTotal - 1) * center.coerceIn(0f, 1f)).roundToInt()
    val half = visibleCount / 2
    var start = centerIndex - half
    var end = start + visibleCount - 1
    if (start < 0) {
        start = 0
        end = visibleCount - 1
    }
    if (end > safeTotal - 1) {
        end = safeTotal - 1
        start = max(0, end - visibleCount + 1)
    }
    return VisibleRange(startIndex = start, endIndex = end, length = end - start + 1)
}

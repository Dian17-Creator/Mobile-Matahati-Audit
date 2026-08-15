package id.my.matahati.audit.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.verticalScrollbar(
    state: LazyListState,
    width: Dp = 4.dp,
    color: Color = Color(0xFFB63352).copy(alpha = 0.7f)
): Modifier = drawWithContent {
    drawContent()

    val layoutInfo = state.layoutInfo
    val visibleItemsInfo = layoutInfo.visibleItemsInfo
    val totalItemsCount = layoutInfo.totalItemsCount

    if (visibleItemsInfo.isNotEmpty() && totalItemsCount > visibleItemsInfo.size) {
        val viewportHeight = size.height
        
        // Approximate total height based on average item height
        val avgItemHeight = visibleItemsInfo.sumOf { it.size }.toFloat() / visibleItemsInfo.size
        val totalHeightApprox = avgItemHeight * totalItemsCount
        
        val thumbHeight = ((viewportHeight / totalHeightApprox) * viewportHeight).coerceAtLeast(24.dp.toPx())
        
        // Calculate offset based on items
        val scrollOffset = (state.firstVisibleItemIndex.toFloat() / totalItemsCount) * viewportHeight +
                (state.firstVisibleItemScrollOffset.toFloat() / totalHeightApprox) * viewportHeight

        // Draw thumb
        drawRect(
            color = color,
            topLeft = Offset(size.width - width.toPx() - 4.dp.toPx(), scrollOffset.coerceIn(0f, viewportHeight - thumbHeight)),
            size = Size(width.toPx(), thumbHeight)
        )
    }
}

fun Modifier.verticalScrollbar(
    state: ScrollState,
    width: Dp = 4.dp,
    color: Color = Color(0xFFB63352).copy(alpha = 0.7f)
): Modifier = drawWithContent {
    drawContent()
    
    // Explicitly read state values to ensure redraw on scroll
    val scrollValue = state.value
    val scrollMax = state.maxValue
    
    if (scrollMax > 0) {
        val viewportHeight = size.height
        val contentHeight = scrollMax + viewportHeight
        
        // Calculate thumb height proportionally
        val thumbHeight = ((viewportHeight / contentHeight) * viewportHeight).coerceAtLeast(32.dp.toPx())
        
        // Calculate scroll offset within the viewport
        val scrollOffset = (scrollValue.toFloat() / scrollMax) * (viewportHeight - thumbHeight)

        // Draw track (subtle background)
        drawRect(
            color = color.copy(alpha = 0.05f),
            topLeft = Offset(size.width - width.toPx() - 4.dp.toPx(), 0f),
            size = Size(width.toPx(), viewportHeight)
        )

        // Draw thumb
        drawRect(
            color = color,
            topLeft = Offset(size.width - width.toPx() - 4.dp.toPx(), scrollOffset),
            size = Size(width.toPx(), thumbHeight)
        )
    }
}

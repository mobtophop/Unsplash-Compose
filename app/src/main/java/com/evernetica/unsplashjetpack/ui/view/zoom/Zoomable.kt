package com.evernetica.unsplashjetpack.ui.view.zoom

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.launch

private suspend fun PointerInputScope.detectTransformGestures(
    cancelIfZoomCanceled: Boolean,
    canConsumeGesture: (pan: Offset, zoom: Float) -> Boolean,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float, timeMillis: Long) -> Unit,
    onGestureStart: () -> Unit = {},
    onGestureEnd: () -> Unit = {},
    onTap: (position: Offset) -> Unit = {},
    onDoubleTap: (position: Offset) -> Unit = {},
    enableOneFingerZoom: Boolean = true,
) = awaitEachGesture {
    val firstDown = awaitFirstDown(requireUnconsumed = false)
    onGestureStart()

    var firstUp: PointerInputChange = firstDown
    var hasMoved = false
    var isMultiTouch = false
    var isLongPressed = false
    var isCanceled = false
    forEachPointerEventUntilReleased(
        onCancel = { isCanceled = true },
    ) { event, isTouchSlopPast ->
        if (isTouchSlopPast) {
            val zoomChange = event.calculateZoom()
            val panChange = event.calculatePan()
            if (zoomChange != 1f || panChange != Offset.Zero) {
                val centroid = event.calculateCentroid(useCurrent = true)
                val timeMillis = event.changes[0].uptimeMillis
                if (canConsumeGesture(panChange, zoomChange)) {
                    onGesture(centroid, panChange, zoomChange, timeMillis)
                    event.consumePositionChanges()
                }
            }
            hasMoved = true
        }
        if (event.changes.size > 1) {
            isMultiTouch = true
        }
        firstUp = event.changes[0]
        val cancelGesture = cancelIfZoomCanceled && isMultiTouch && event.changes.size == 1
        !cancelGesture
    }

    if (firstUp.uptimeMillis - firstDown.uptimeMillis > viewConfiguration.longPressTimeoutMillis) {
        isLongPressed = true
    }

    val isTap = !hasMoved && !isMultiTouch && !isLongPressed && !isCanceled
    if (isTap) {
        val secondDown = awaitSecondDown(firstUp)
        if (secondDown == null) {
            onTap(firstUp.position)
        } else {
            var isDoubleTap = true
            var isSecondCanceled = false
            var secondUp: PointerInputChange = secondDown
            forEachPointerEventUntilReleased(
                onCancel = { isSecondCanceled = true }
            ) { event, isTouchSlopPast ->
                if (isTouchSlopPast) {
                    if (enableOneFingerZoom) {
                        val panChange = event.calculatePan()
                        val zoomChange = 1f + panChange.y * 0.004f
                        if (zoomChange != 1f) {
                            val centroid = event.calculateCentroid(useCurrent = true)
                            val timeMillis = event.changes[0].uptimeMillis
                            if (canConsumeGesture(Offset.Zero, zoomChange)) {
                                onGesture(centroid, Offset.Zero, zoomChange, timeMillis)
                                event.consumePositionChanges()
                            }
                        }
                    }
                    isDoubleTap = false
                }
                if (event.changes.size > 1) {
                    isDoubleTap = false
                }
                secondUp = event.changes[0]
                true
            }

            if (secondUp.uptimeMillis - secondDown.uptimeMillis > viewConfiguration.longPressTimeoutMillis) {
                isDoubleTap = false
            }

            if (isDoubleTap && !isSecondCanceled) {
                onDoubleTap(secondUp.position)
            }
        }
    }
    onGestureEnd()
}

private suspend fun AwaitPointerEventScope.forEachPointerEventUntilReleased(
    onCancel: () -> Unit,
    action: (event: PointerEvent, isTouchSlopPast: Boolean) -> Boolean,
) {
    val touchSlop = TouchSlop(viewConfiguration.touchSlop)
    do {
        val mainEvent = awaitPointerEvent(pass = PointerEventPass.Main)
        if (mainEvent.changes.fastAny { it.isConsumed }) {
            onCancel()
            break
        }

        val isTouchSlopPast = touchSlop.isPast(mainEvent)
        val canContinue = action(mainEvent, isTouchSlopPast)
        if (!canContinue) {
            break
        }
        if (isTouchSlopPast) {
            continue
        }

        val finalEvent = awaitPointerEvent(pass = PointerEventPass.Final)
        if (finalEvent.changes.fastAny { it.isConsumed }) {
            onCancel()
            break
        }
    } while (mainEvent.changes.fastAny { it.pressed })
}

private suspend fun AwaitPointerEventScope.awaitSecondDown(
    firstUp: PointerInputChange
): PointerInputChange? = withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
    val minUptime = firstUp.uptimeMillis + viewConfiguration.doubleTapMinTimeMillis
    var change: PointerInputChange
    do {
        change = awaitFirstDown()
    } while (change.uptimeMillis < minUptime)
    change
}

private fun PointerEvent.consumePositionChanges() {
    changes.fastForEach {
        if (it.positionChanged()) {
            it.consume()
        }
    }
}

private class TouchSlop(private val threshold: Float) {
    private var pan = Offset.Zero
    private var _isPast = false

    fun isPast(event: PointerEvent): Boolean {
        if (_isPast) {
            return true
        }

        if (event.changes.size > 1) {
            _isPast = true
        } else {
            pan += event.calculatePan()
            _isPast = pan.getDistance() > threshold
        }

        return _isPast
    }
}

enum class ScrollGesturePropagation {
    ContentEdge,
    NotZoomed,
}

fun Modifier.zoomable(
    zoomState: ZoomState,
    zoomEnabled: Boolean = true,
    enableOneFingerZoom: Boolean = true,
    scrollGesturePropagation: ScrollGesturePropagation = ScrollGesturePropagation.ContentEdge,
    onTap: (position: Offset) -> Unit = {},
    onDoubleTap: suspend (position: Offset) -> Unit = { position -> if (zoomEnabled) zoomState.toggleScale(2.5f, position) },
): Modifier = this then ZoomableElement(
    zoomState = zoomState,
    zoomEnabled = zoomEnabled,
    enableOneFingerZoom = enableOneFingerZoom,
    snapBackEnabled = false,
    scrollGesturePropagation = scrollGesturePropagation,
    onTap = onTap,
    onDoubleTap = onDoubleTap,
)

private data class ZoomableElement(
    val zoomState: ZoomState,
    val zoomEnabled: Boolean,
    val enableOneFingerZoom: Boolean,
    val snapBackEnabled: Boolean,
    val scrollGesturePropagation: ScrollGesturePropagation,
    val onTap: (position: Offset) -> Unit,
    val onDoubleTap: suspend (position: Offset) -> Unit,
): ModifierNodeElement<ZoomableNode>() {
    override fun create(): ZoomableNode = ZoomableNode(
        zoomState,
        zoomEnabled,
        enableOneFingerZoom,
        snapBackEnabled,
        scrollGesturePropagation,
        onTap,
        onDoubleTap,
    )

    override fun update(node: ZoomableNode) {
        node.update(
            zoomState,
            zoomEnabled,
            enableOneFingerZoom,
            snapBackEnabled,
            scrollGesturePropagation,
            onTap,
            onDoubleTap,
        )
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "zoomable"
        properties["zoomState"] = zoomState
        properties["zoomEnabled"] = zoomEnabled
        properties["enableOneFingerZoom"] = enableOneFingerZoom
        properties["snapBackEnabled"] = snapBackEnabled
        properties["scrollGesturePropagation"] = scrollGesturePropagation
        properties["onTap"] = onTap
        properties["onDoubleTap"] = onDoubleTap
    }
}

private class ZoomableNode(
    var zoomState: ZoomState,
    var zoomEnabled: Boolean,
    var enableOneFingerZoom: Boolean,
    var snapBackEnabled: Boolean,
    var scrollGesturePropagation: ScrollGesturePropagation,
    var onTap: (position: Offset) -> Unit,
    var onDoubleTap: suspend (position: Offset) -> Unit,
): PointerInputModifierNode, LayoutModifierNode, DelegatingNode() {
    var measuredSize = Size.Zero

    fun update(
        zoomState: ZoomState,
        zoomEnabled: Boolean,
        enableOneFingerZoom: Boolean,
        snapBackEnabled: Boolean,
        scrollGesturePropagation: ScrollGesturePropagation,
        onTap: (position: Offset) -> Unit,
        onDoubleTap: suspend (position: Offset) -> Unit,
    ) {
        if (this.zoomState != zoomState) {
            zoomState.setLayoutSize(measuredSize)
            this.zoomState = zoomState
        }
        this.zoomEnabled = zoomEnabled
        this.enableOneFingerZoom = enableOneFingerZoom
        this.scrollGesturePropagation = scrollGesturePropagation
        this.snapBackEnabled = snapBackEnabled
        this.onTap = onTap
        this.onDoubleTap = onDoubleTap
    }

    val pointerInputNode = delegate(SuspendingPointerInputModifierNode {
        detectTransformGestures(
            cancelIfZoomCanceled = snapBackEnabled,
            onGestureStart = {
                resetConsumeGesture()
                zoomState.startGesture()
            },
            canConsumeGesture = { pan, zoom ->
                zoomEnabled && canConsumeGesture(pan, zoom)
            },
            onGesture = { centroid, pan, zoom, timeMillis ->
                if (zoomEnabled) {
                    coroutineScope.launch {
                        zoomState.applyGesture(
                            pan = pan,
                            zoom = zoom,
                            position = centroid,
                            timeMillis = timeMillis,
                        )
                    }
                }
            },
            onGestureEnd = {
                coroutineScope.launch {
                    if (snapBackEnabled || zoomState.scale < 1f) {
                        zoomState.changeScale(1f, Offset.Zero)
                    } else {
                        zoomState.startFling()
                    }
                }
            },
            onTap = onTap,
            onDoubleTap = { position ->
                coroutineScope.launch {
                    onDoubleTap(position)
                }
            },
            enableOneFingerZoom = enableOneFingerZoom,
        )
    })

    private var consumeGesture: Boolean? = null

    private fun resetConsumeGesture() {
        consumeGesture = null
    }

    private fun canConsumeGesture(pan: Offset, zoom: Float): Boolean {
        val currentValue = consumeGesture
        if (currentValue != null) {
            return currentValue
        }

        val newValue = when {
            zoom != 1f -> true
            zoomState.scale == 1f -> false
            scrollGesturePropagation == ScrollGesturePropagation.NotZoomed -> true
            else -> zoomState.willChangeOffset(pan)
        }
        consumeGesture = newValue
        return newValue
    }

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize
    ) {
        pointerInputNode.onPointerEvent(pointerEvent, pass, bounds)
    }

    override fun onCancelPointerInput() {
        pointerInputNode.onCancelPointerInput()
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        measuredSize = IntSize(placeable.measuredWidth, placeable.measuredHeight).toSize()
        zoomState.setLayoutSize(measuredSize)
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(x = 0, y = 0) {
                scaleX = zoomState.scale
                scaleY = zoomState.scale
                translationX = zoomState.offsetX
                translationY = zoomState.offsetY
            }
        }
    }
}

suspend fun ZoomState.toggleScale(
    targetScale: Float,
    position: Offset,
    animationSpec: AnimationSpec<Float> = spring(),
) {
    val newScale = if (scale == 1f) targetScale else 1f
    changeScale(newScale, position, animationSpec)
}
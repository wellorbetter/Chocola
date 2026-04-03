@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.sosauce.chocola.presentation.screens.playing.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.sosauce.chocola.R
import com.sosauce.chocola.data.datastore.rememberArtworkShape
import com.sosauce.chocola.data.datastore.rememberCarousel
import com.sosauce.chocola.data.states.MusicState
import com.sosauce.chocola.domain.actions.PlayerActions
import com.sosauce.chocola.utils.ImageUtils
import com.sosauce.chocola.utils.ignoreParentPadding
import com.sosauce.chocola.utils.toShape
import kotlin.math.absoluteValue
import kotlinx.coroutines.flow.distinctUntilChanged

private const val CAROUSEL_PAGE_SPACING_DP = 10
private const val CAROUSEL_MIN_SCALE = 0.85f
private const val CAROUSEL_MIN_ALPHA = 0.5f
private val MUSIC_NOTE_ICON_SIZE = 110.dp

@Composable
fun Artwork(
    pagerModifier: Modifier = Modifier,
    musicState: MusicState,
    onHandlePlayerActions: (PlayerActions) -> Unit,
) {
    val context = LocalContext.current
    val useCarousel by rememberCarousel()
    var artworkShape by rememberArtworkShape()

    if (useCarousel) {
        val pagerState = rememberPagerState(
            initialPage = musicState.mediaIndex,
            pageCount = { musicState.loadedMedias.size }
        )

        var isProgrammaticScroll by remember { mutableStateOf(false) }

        LaunchedEffect(musicState.mediaIndex) {
            if (!pagerState.isScrollInProgress &&
                pagerState.currentPage != musicState.mediaIndex
            ) {
                isProgrammaticScroll = true
                pagerState.animateScrollToPage(musicState.mediaIndex)
                isProgrammaticScroll = false
            }
        }

        val currentMediaIndex by rememberUpdatedState(musicState.mediaIndex)
        val currentShuffle by rememberUpdatedState(musicState.shuffle)
        val currentTrackCount by rememberUpdatedState(musicState.loadedMedias.size)

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.settledPage }
                .distinctUntilChanged()
                .collect { settledPage ->
                    if (currentTrackCount == 0) return@collect
                    val safeIndex = settledPage.coerceIn(0, currentTrackCount - 1)
                    if (isProgrammaticScroll) return@collect
                    if (safeIndex != currentMediaIndex) {
                        if (currentShuffle) {
                            if (safeIndex > currentMediaIndex) {
                                onHandlePlayerActions(PlayerActions.SeekToNextMusic)
                            } else {
                                onHandlePlayerActions(PlayerActions.SeekToPreviousMusic)
                            }
                        } else {
                            onHandlePlayerActions(PlayerActions.SeekToMusicIndex(safeIndex))
                        }
                    }
                }
        }

        HorizontalPager(
            state = pagerState,
            modifier = pagerModifier
                .ignoreParentPadding()
                .aspectRatio(1f)
                .wrapContentSize()
                .fillMaxSize(),
            pageSpacing = CAROUSEL_PAGE_SPACING_DP.dp,
            beyondViewportPageCount = 1
        ) { page ->
            val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            ).absoluteValue

            val scale = lerp(start = CAROUSEL_MIN_SCALE, stop = 1.0f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
            val alpha = lerp(start = CAROUSEL_MIN_ALPHA, stop = 1.0f, fraction = 1f - pageOffset.coerceIn(0f, 1f))

            val image = rememberAsyncImagePainter(
                ImageUtils.imageRequester(musicState.loadedMedias[page].artUri, context)
            )

            ArtworkImage(
                painter = image,
                modifier = Modifier
                    .aspectRatio(1f)
                    .wrapContentSize()
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
            )
        }

    } else {
        val image =
            rememberAsyncImagePainter(ImageUtils.imageRequester(musicState.track.artUri, context))

        ArtworkImage(
            painter = image,
            modifier = Modifier
                .aspectRatio(1f)
                .wrapContentSize()
                .fillMaxSize()
                .clip(artworkShape.toShape())
        )
    }
}

@Composable
private fun ArtworkImage(painter: AsyncImagePainter, modifier: Modifier) {
    val imageState by painter.state.collectAsStateWithLifecycle()
    when (imageState) {
        is AsyncImagePainter.State.Error -> ErrorImage()
        else -> Image(
            painter = painter,
            contentDescription = stringResource(R.string.artwork),
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ErrorImage() {
    var artworkShape by rememberArtworkShape()

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .wrapContentSize()
            .fillMaxSize()
            .clip(artworkShape.toShape())
            .background(MaterialTheme.colorScheme.surfaceContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.music_note_rounded),
            contentDescription = null,
            modifier = Modifier.size(MUSIC_NOTE_ICON_SIZE),
            tint = contentColorFor(MaterialTheme.colorScheme.surfaceContainer)
        )
    }
}

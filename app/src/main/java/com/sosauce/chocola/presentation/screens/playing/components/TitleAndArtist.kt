@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.chocola.presentation.screens.playing.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.sosauce.chocola.data.states.MusicState


private const val TRACK_ANIM_DURATION = 350

@Composable
fun TitleAndArtist(
    titleModifier: Modifier = Modifier,
    musicState: MusicState
) {
    AnimatedContent(
        targetState = musicState.track,
        transitionSpec = {
            (slideInVertically(
                animationSpec = tween(durationMillis = TRACK_ANIM_DURATION, easing = EaseInOutCubic),
                initialOffsetY = { fullHeight -> fullHeight / 3 }
            ) + fadeIn(
                animationSpec = tween(durationMillis = TRACK_ANIM_DURATION, easing = EaseInOutCubic)
            )) togetherWith (slideOutVertically(
                animationSpec = tween(durationMillis = TRACK_ANIM_DURATION, easing = EaseInOutCubic),
                targetOffsetY = { fullHeight -> -fullHeight / 3 }
            ) + fadeOut(
                animationSpec = tween(durationMillis = TRACK_ANIM_DURATION, easing = EaseInOutCubic)
            ))
        },
        contentKey = { it.mediaId },
        label = "TitleAndArtistTransition"
    ) { track ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = track.title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMediumEmphasized,
                fontWeight = FontWeight.ExtraBold,
                modifier = titleModifier
                    .fillMaxWidth()
                    .basicMarquee()
            )
            Text(
                text = track.artist,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleLargeEmphasized,
                modifier = Modifier.basicMarquee()
            )
        }
    }
}

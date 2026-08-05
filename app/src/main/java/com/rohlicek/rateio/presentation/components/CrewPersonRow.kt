package com.rohlicek.rateio.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rohlicek.rateio.data.remote.tmdb.TmdbCrewMember


@Composable
fun CrewPersonRow(
    modifier: Modifier = Modifier,
    departmentFilter: String,
    people: List<TmdbCrewMember>?,
    onPersonClick: ((Int) -> Unit)? = null,
) {
    if (people.isNullOrEmpty()) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .weight(1f),
                //.padding(top = 16.dp),
            text = departmentFilter,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
        )

        LazyRow(
            modifier = Modifier
                .weight(2f)
                .fillMaxWidth()
                .height(155.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentPadding = PaddingValues(end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(people.take(15), key = { it.creditId }) { member ->
                PersonCard(
                    name = member.name,
                    position = member.job,
                    profilePath = member.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
                    onClick = {
                        onPersonClick?.invoke(member.id)
                    },
                    width = 75.dp,
                    height = 90.dp,
                )
            }
        }
    }
}
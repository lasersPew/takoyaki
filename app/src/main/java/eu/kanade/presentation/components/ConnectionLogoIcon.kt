// AM (CONNECTIONS) -->
package eu.kanade.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.data.connections.BaseConnection
import tachiyomi.presentation.core.util.clickableNoIndication

@Composable
fun ConnectionsLogoIcon(
    service: BaseConnection,
    onClick: (() -> Unit)? = null,
) {
    val modifier = if (onClick != null) {
        Modifier.clickableNoIndication(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .background(color = Color(service.getLogoColor()), shape = MaterialTheme.shapes.medium)
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(service.getLogo()),
            contentDescription = service.name,
        )
    }
}
// <-- AM (CONNECTIONS)

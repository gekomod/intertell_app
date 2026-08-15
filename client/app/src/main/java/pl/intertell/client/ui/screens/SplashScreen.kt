package pl.intertell.client.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import pl.intertell.client.R
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellType

private val SplashNavy = Color(0xFF0B1B3F)

/** Shown briefly on cold start, before the login screen. */
@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashNavy)
            .padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.logo_intertell),
                contentDescription = "Intertell",
                contentScale = ContentScale.Fit,
                modifier = Modifier.width(170.dp),
            )
            Text(
                "SZYBKI INTERNET ŚWIATŁOWODOWY",
                style = IntertellType.monoSmall,
                color = IntertellColors.White.copy(alpha = 0.55f),
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row {
                Text("Intertell ", style = IntertellType.headline, color = IntertellColors.White)
                Text("- BOK", style = IntertellType.headline, color = IntertellColors.Accent)
            }
            Text(
                "Twoje konto. Wszystko pod kontrolą.",
                style = IntertellType.body,
                color = IntertellColors.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 6.dp),
            )
            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(IntertellColors.White),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.mascot_client),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(136.dp),
                )
            }
            Text(
                "Ładowanie…",
                style = IntertellType.bodySmall,
                color = IntertellColors.White.copy(alpha = 0.55f),
                modifier = Modifier.padding(top = 22.dp),
            )
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(200.dp),
                color = IntertellColors.Accent,
                trackColor = IntertellColors.White.copy(alpha = 0.15f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SplashFeature(Icons.Default.Description, "Faktury", "i płatności")
            FeatureDivider()
            SplashFeature(Icons.Default.SupportAgent, "Zgłoszenia", "24/7")
            FeatureDivider()
            SplashFeature(Icons.Default.Wifi, "Twoje", "usługi")
        }
    }
}

@Composable
private fun SplashFeature(icon: ImageVector, line1: String, line2: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = IntertellColors.Accent, modifier = Modifier.size(22.dp))
        Text(line1, style = IntertellType.bodySmall, color = IntertellColors.White.copy(alpha = 0.7f), modifier = Modifier.padding(top = 6.dp))
        Text(line2, style = IntertellType.bodySmall, color = IntertellColors.White.copy(alpha = 0.7f))
    }
}

@Composable
private fun FeatureDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .padding(vertical = 4.dp)
            .background(IntertellColors.White.copy(alpha = 0.12f)),
    )
}

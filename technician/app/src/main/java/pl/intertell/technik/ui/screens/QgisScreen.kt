package pl.intertell.technik.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import pl.intertell.technik.BuildConfig
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

private const val QFIELD_PACKAGE = "ch.opengis.qfield"
private const val INFRASTRUCTURE_SOURCE_ID = "infrastructure"

// Roughly the middle of Poland — a reasonable default before the technician
// pans to their own area; there's no server-computed bounding box to fit to
// (see server's internal/qfield), so this is a fixed starting view, not
// centered on the actual data.
private val POLAND_CENTER = LatLng(52.06, 19.48)
private const val POLAND_ZOOM = 6.0

@Composable
fun QgisScreen(viewModel: TechnicianViewModel) {
    val context = LocalContext.current
    val geoJson by viewModel.infrastructureGeoJson.collectAsState()
    val loading by viewModel.infrastructureLoading.collectAsState()
    val error by viewModel.infrastructureError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        Text("QGIS", style = IntertellType.headline, color = IntertellColors.TextPrimary)
        Text(
            "Mapa infrastruktury sieciowej — przebiegi światłowodów, szafki i punkty dystrybucyjne.",
            style = IntertellType.body,
            color = IntertellColors.Text6,
            modifier = Modifier.padding(top = 6.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(IntertellColors.ScreenBackground),
            contentAlignment = Alignment.Center,
        ) {
            when {
                geoJson != null -> InfrastructureMap(geoJsonFile = geoJson!!, modifier = Modifier.fillMaxSize())
                loading -> CircularProgressIndicator(color = IntertellColors.Accent)
                error != null -> Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Mapa sieci jest niedostępna.",
                        style = IntertellType.bodyBold,
                        color = IntertellColors.TextPrimary,
                    )
                    Text(
                        error ?: "",
                        style = IntertellType.bodySmall,
                        color = IntertellColors.Text45,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                else -> CircularProgressIndicator(color = IntertellColors.Accent)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { openQField(context) }
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("Otwórz w aplikacji QField →", style = IntertellType.bodyBold, color = IntertellColors.Accent)
        }
    }
}

@Composable
private fun InfrastructureMap(geoJsonFile: File, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember {
        MapView(context).apply { onCreate(null) }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier) { view ->
        view.getMapAsync { map ->
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(POLAND_CENTER, POLAND_ZOOM))
            val styleUrl = "https://api.maptiler.com/maps/streets-v2/style.json?key=${BuildConfig.MAPTILER_API_KEY}"
            map.setStyle(Style.Builder().fromUri(styleUrl)) { style ->
                if (style.getSource(INFRASTRUCTURE_SOURCE_ID) != null) return@setStyle
                // File-URI constructor — MapLibre parses the GeoJSON off the
                // downloaded file itself, so it's never materialized as a
                // Java String on the way in (see ApiClient.getToFile).
                style.addSource(GeoJsonSource(INFRASTRUCTURE_SOURCE_ID, geoJsonFile.toURI()))
                style.addLayer(
                    LineLayer("infrastructure-lines", INFRASTRUCTURE_SOURCE_ID)
                        // GeoPackage exports from QGIS are almost always
                        // Multi*-typed even for single-part features, so a
                        // filter on bare "LineString" alone silently matches
                        // nothing — the whole infrastructure layer rendered
                        // invisibly for that reason.
                        .withFilter(
                            Expression.any(
                                Expression.eq(Expression.geometryType(), "LineString"),
                                Expression.eq(Expression.geometryType(), "MultiLineString"),
                            ),
                        )
                        .withProperties(
                            PropertyFactory.lineColor(Color.parseColor("#0E86C4")),
                            PropertyFactory.lineWidth(3f),
                        ),
                )
                style.addLayer(
                    CircleLayer("infrastructure-points", INFRASTRUCTURE_SOURCE_ID)
                        .withFilter(
                            Expression.any(
                                Expression.eq(Expression.geometryType(), "Point"),
                                Expression.eq(Expression.geometryType(), "MultiPoint"),
                            ),
                        )
                        .withProperties(
                            PropertyFactory.circleColor(Color.parseColor("#D93B1E")),
                            PropertyFactory.circleRadius(6f),
                            PropertyFactory.circleStrokeColor(Color.WHITE),
                            PropertyFactory.circleStrokeWidth(2f),
                        ),
                )
            }
        }
    }
}

private fun openQField(context: Context) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(QFIELD_PACKAGE)
    if (launchIntent != null) {
        context.startActivity(launchIntent)
        return
    }
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$QFIELD_PACKAGE")))
    } catch (e: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$QFIELD_PACKAGE")))
    }
}

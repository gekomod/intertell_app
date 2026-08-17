package pl.intertell.technik.ui.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import pl.intertell.technik.BuildConfig
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.data.InfrastructureMap
import pl.intertell.technik.data.LayerStyle
import pl.intertell.technik.data.LayerUnit
import pl.intertell.technik.data.computeLayerUnits
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

private const val QFIELD_PACKAGE = "ch.opengis.qfield"
private const val INFRASTRUCTURE_SOURCE_ID = "infrastructure"
// Fallback ids used only when the server hasn't sent any layer/style info
// at all (see the units.isEmpty() branch below) — every feature in one pair
// of layers, same as the very first version of this screen.
private const val FALLBACK_LINE_LAYER_ID = "infrastructure-lines"
private const val FALLBACK_POINT_LAYER_ID = "infrastructure-points"

// Roughly the middle of Poland — a reasonable default before the technician
// pans to their own area or a GPS fix arrives; there's no server-computed
// bounding box to fit to (see server's internal/qfield).
private val POLAND_CENTER = LatLng(52.06, 19.48)
private const val POLAND_ZOOM = 6.0

// Cycled by name (hashed) so anything without a recognized QGIS style still
// gets a stable, distinct color rather than one flat default.
private val LAYER_PALETTE = listOf(
    "#0E86C4", "#D93B1E", "#2E9E5B", "#F2A93B",
    "#8B5CF6", "#EC4899", "#14B8A6", "#F97316",
)

private fun colorForLayer(seed: String): Int =
    Color.parseColor(LAYER_PALETTE[(seed.hashCode() and Int.MAX_VALUE) % LAYER_PALETTE.size])

private fun safeParseColor(hex: String?, fallback: Int): Int =
    if (hex.isNullOrBlank()) fallback else runCatching { Color.parseColor(hex) }.getOrDefault(fallback)

/** The resolved color for one menu row / map layer — a category's own color if it has one, else the table's. */
private fun unitColor(unit: LayerUnit, styles: Map<String, LayerStyle>): Int {
    val style = styles[unit.table]
    val hex = if (unit.categoryValue != null) style?.categories?.get(unit.categoryValue) ?: style?.default else style?.default
    return safeParseColor(hex, colorForLayer(unit.key))
}

// GeoPackage table names generated by QField carry a trailing UUID (e.g.
// "..._947c5a8f_c98c_46c2_9b1d_adc6ebbc6a01") that's meaningless to a
// technician — stripped for display only, the raw table name is still what
// gets toggled/matched everywhere else.
private val TABLE_UUID_SUFFIX = Regex("_[0-9a-fA-F]{8}_[0-9a-fA-F]{4}_[0-9a-fA-F]{4}_[0-9a-fA-F]{4}_[0-9a-fA-F]{12}$")

private fun displayLayerName(table: String): String =
    TABLE_UUID_SUFFIX.replace(table, "")
        .split("_")
        .filter { it.isNotBlank() }
        .joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

@Composable
fun QgisScreen(viewModel: TechnicianViewModel) {
    val context = LocalContext.current
    val infrastructure by viewModel.infrastructureGeoJson.collectAsState()
    val loading by viewModel.infrastructureLoading.collectAsState()
    val error by viewModel.infrastructureError.collectAsState()
    val visibleLayers by viewModel.visibleInfrastructureLayers.collectAsState()
    val styles by viewModel.infrastructureStyles.collectAsState()

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
                infrastructure != null -> InfrastructureMapView(
                    infrastructure = infrastructure!!,
                    visibleLayers = visibleLayers,
                    styles = styles,
                    onToggleLayer = viewModel::toggleInfrastructureLayer,
                    modifier = Modifier.fillMaxSize(),
                )
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
private fun InfrastructureMapView(
    infrastructure: InfrastructureMap,
    visibleLayers: Set<String>,
    styles: Map<String, LayerStyle>,
    onToggleLayer: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember {
        MapView(context).apply { onCreate(null) }
    }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var showLayersMenu by remember { mutableStateOf(false) }
    // One toggleable "unit" per menu row — a whole table, or (for a table
    // QGIS categorizes, e.g. cable capacity) one row per category, matching
    // how QGIS/QField's own legend breaks such a layer down.
    val units = remember(infrastructure.layers, styles) { computeLayerUnits(infrastructure.layers, styles) }

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

    Box(modifier = modifier) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize()) { view ->
            view.getMapAsync { map ->
                mapLibreMap = map
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(POLAND_CENTER, POLAND_ZOOM))
                val styleUrl = "https://api.maptiler.com/maps/streets-v2/style.json?key=${BuildConfig.MAPTILER_API_KEY}"
                map.setStyle(Style.Builder().fromUri(styleUrl)) { style ->
                    if (style.getSource(INFRASTRUCTURE_SOURCE_ID) != null) return@setStyle
                    // The file:// URI constructor silently loaded nothing on
                    // real devices (MapLibre's native file source appears not
                    // to handle it reliably) — reading the text ourselves and
                    // using the String constructor is the same approach the
                    // pre-OOM-fix code used, and is safe now that the backend
                    // trims the payload to a couple MB (see internal/qfield).
                    style.addSource(GeoJsonSource(INFRASTRUCTURE_SOURCE_ID, infrastructure.file.readText()))

                    if (units.isEmpty()) {
                        // Old server with no layer/style info at all — one
                        // pair of layers for everything, hashed colors,
                        // always visible (no menu is shown for this case).
                        style.addLayer(
                            LineLayer(FALLBACK_LINE_LAYER_ID, INFRASTRUCTURE_SOURCE_ID)
                                .withFilter(lineGeometryFilter())
                                .withProperties(PropertyFactory.lineColor(Color.parseColor(LAYER_PALETTE[0])), PropertyFactory.lineWidth(3f)),
                        )
                        style.addLayer(
                            CircleLayer(FALLBACK_POINT_LAYER_ID, INFRASTRUCTURE_SOURCE_ID)
                                .withFilter(pointGeometryFilter())
                                .withProperties(
                                    PropertyFactory.circleColor(Color.parseColor(LAYER_PALETTE[1])),
                                    PropertyFactory.circleRadius(6f),
                                    PropertyFactory.circleStrokeColor(Color.WHITE),
                                    PropertyFactory.circleStrokeWidth(2f),
                                ),
                        )
                    } else {
                        // One real MapLibre layer per unit (not one shared
                        // layer with a rebuilt filter) — visibility is then
                        // just a property flip per layer, which is simple
                        // and reliably reactive, unlike re-deriving a
                        // combined "in visible set" filter expression.
                        units.forEach { unit ->
                            val color = unitColor(unit, styles)
                            val visibility = if (unit.key in visibleLayers) Property.VISIBLE else Property.NONE
                            val baseFilter = unitBaseFilter(unit, styles)
                            style.addLayer(
                                LineLayer(lineLayerId(unit), INFRASTRUCTURE_SOURCE_ID)
                                    .withFilter(Expression.all(lineGeometryFilter(), baseFilter))
                                    .withProperties(
                                        PropertyFactory.lineColor(color),
                                        PropertyFactory.lineWidth(3f),
                                        PropertyFactory.visibility(visibility),
                                    ),
                            )
                            style.addLayer(
                                CircleLayer(pointLayerId(unit), INFRASTRUCTURE_SOURCE_ID)
                                    .withFilter(Expression.all(pointGeometryFilter(), baseFilter))
                                    .withProperties(
                                        PropertyFactory.circleColor(color),
                                        PropertyFactory.circleRadius(6f),
                                        PropertyFactory.circleStrokeColor(Color.WHITE),
                                        PropertyFactory.circleStrokeWidth(2f),
                                        PropertyFactory.visibility(visibility),
                                    ),
                            )
                        }
                    }

                    // Permission was already requested at app startup
                    // (MainActivity.askForLocationPermission) — this just
                    // skips the indicator if it was denied, same as
                    // elsewhere in the app.
                    if (hasLocationPermission(context)) {
                        map.locationComponent.apply {
                            activateLocationComponent(LocationComponentActivationOptions.builder(context, style).build())
                            isLocationComponentEnabled = true
                            renderMode = RenderMode.COMPASS
                            cameraMode = CameraMode.NONE
                        }
                        lastKnownLocation(context)?.let { location ->
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 14.0))
                        }
                    }
                }
            }
        }

        // Live visibility updates when a checkbox is toggled — the
        // layers themselves are only ever created once (see the
        // style.getSource guard above), this just flips each one's
        // visibility property, which MapLibre always picks up immediately.
        LaunchedEffect(visibleLayers, mapLibreMap, units) {
            val style = mapLibreMap?.style ?: return@LaunchedEffect
            units.forEach { unit ->
                val visibility = if (unit.key in visibleLayers) Property.VISIBLE else Property.NONE
                style.getLayer(lineLayerId(unit))?.setProperties(PropertyFactory.visibility(visibility))
                style.getLayer(pointLayerId(unit))?.setProperties(PropertyFactory.visibility(visibility))
            }
        }

        if (units.isNotEmpty()) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) {
                IconButton(
                    onClick = { showLayersMenu = !showLayersMenu },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(IntertellColors.ScreenBackground),
                ) {
                    Icon(Icons.Filled.Layers, contentDescription = "Warstwy mapy", tint = IntertellColors.Accent)
                }
                if (showLayersMenu) {
                    val grouped = units.groupBy { it.table }
                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 48.dp)
                            .width(240.dp)
                            .heightIn(max = 360.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(IntertellColors.ScreenBackground)
                            .padding(8.dp),
                    ) {
                        infrastructure.layers.forEach { table ->
                            val tableUnits = grouped[table] ?: return@forEach
                            val onlyUnit = tableUnits.singleOrNull()
                            if (onlyUnit != null && onlyUnit.categoryValue == null) {
                                item(key = onlyUnit.key) {
                                    LayerMenuRow(
                                        label = displayLayerName(table),
                                        checked = onlyUnit.key in visibleLayers,
                                        color = unitColor(onlyUnit, styles),
                                        onToggle = { onToggleLayer(onlyUnit.key) },
                                    )
                                }
                            } else {
                                item(key = "$table-header") {
                                    Text(
                                        displayLayerName(table),
                                        style = IntertellType.bodyBold,
                                        color = IntertellColors.TextPrimary,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                                    )
                                }
                                items(tableUnits, key = { it.key }) { unit ->
                                    LayerMenuRow(
                                        label = unit.categoryValue ?: "?",
                                        checked = unit.key in visibleLayers,
                                        color = unitColor(unit, styles),
                                        onToggle = { onToggleLayer(unit.key) },
                                        indent = true,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LayerMenuRow(
    label: String,
    checked: Boolean,
    color: Int,
    onToggle: () -> Unit,
    indent: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(start = if (indent) 16.dp else 0.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = ComposeColor(color)),
        )
        Text(label, style = IntertellType.bodySmall, color = IntertellColors.TextPrimary)
    }
}

private fun lineLayerId(unit: LayerUnit) = "line-${unit.key}"
private fun pointLayerId(unit: LayerUnit) = "point-${unit.key}"

/** Matches this unit's table, and its category value too when it has one — geometry-type filtering is applied separately (see lineGeometryFilter/pointGeometryFilter). */
private fun unitBaseFilter(unit: LayerUnit, styles: Map<String, LayerStyle>): Expression {
    val layerFilter = Expression.eq(Expression.get("layer"), unit.table)
    val field = styles[unit.table]?.field
    return if (unit.categoryValue != null && field != null) {
        Expression.all(layerFilter, Expression.eq(Expression.get(field), unit.categoryValue))
    } else {
        layerFilter
    }
}

// GeoPackage exports from QGIS are almost always Multi*-typed even for
// single-part features, so a filter on bare "LineString"/"Point" alone
// silently matches nothing.
private fun lineGeometryFilter(): Expression = Expression.any(
    Expression.eq(Expression.geometryType(), "LineString"),
    Expression.eq(Expression.geometryType(), "MultiLineString"),
)

private fun pointGeometryFilter(): Expression = Expression.any(
    Expression.eq(Expression.geometryType(), "Point"),
    Expression.eq(Expression.geometryType(), "MultiPoint"),
)

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun lastKnownLocation(context: Context): android.location.Location? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    return try {
        locationManager.getProviders(true)
            .mapNotNull { locationManager.getLastKnownLocation(it) }
            .maxByOrNull { it.time }
    } catch (e: SecurityException) {
        null
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

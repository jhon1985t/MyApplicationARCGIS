package com.jhonjto.co.myapplicationarcgis

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.mapping.view.LocationDisplay.DataSourceStatusChangedEvent
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol


class MainActivity : AppCompatActivity() {

    lateinit var mMapView: MapView
    lateinit var mFeatureLayer: FeatureLayer
    val itemID = "2e4b3df6ba4b44969a3bc9827de746b3"
    val portal = Portal("http://www.arcgis.com")
    lateinit var mGraphicsOverlay: GraphicsOverlay
    lateinit var mLocationDisplay: LocationDisplay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // *** ADD ***
        mMapView = findViewById(R.id.mapView)
        setupMap()
        setupLocationDisplay()
        addTrailheadsLayer()
        createGraphics()
    }

    private fun setupMap() {
        // *** ADD ***
        ArcGISRuntimeEnvironment.setLicense(resources.getString(R.string.arcgis_license_key))

        val basemapType = Basemap.Type.TOPOGRAPHIC
        val latitude = 34.0270
        val longitude = -118.8050
        val levelOfDetail = 13
        val map = ArcGISMap(basemapType, latitude, longitude, levelOfDetail)
        mMapView.map = map
        addLayer(map)
    }

    private fun addTrailheadsLayer() {
        val url = "https://services3.arcgis.com/GVgbJbqm8hXASVYi/arcgis/rest/services/Trailheads/FeatureServer/"
        val serviceFeatureTable = ServiceFeatureTable(url)
        val featureLayer = FeatureLayer(serviceFeatureTable)
        val map = mMapView.map
        map.operationalLayers.add(featureLayer)
    }

    private fun addLayer(map: ArcGISMap) {
        val portalItem = PortalItem(portal, itemID)
        mFeatureLayer = FeatureLayer(portalItem, 0)
        mFeatureLayer.addDoneLoadingListener {
            if (mFeatureLayer.loadStatus == LoadStatus.LOADED) {
                map.operationalLayers.add(mFeatureLayer)
            }
        }
        mFeatureLayer.loadAsync()
    }

    private fun setupLocationDisplay() {
        mLocationDisplay = mMapView.locationDisplay

        /* ** ADD ** */
        mLocationDisplay.addDataSourceStatusChangedListener { dataSourceStatusChangedEvent: DataSourceStatusChangedEvent ->
            if (dataSourceStatusChangedEvent.isStarted || dataSourceStatusChangedEvent.error == null) {
                return@addDataSourceStatusChangedListener
            }
            val requestPermissionsCode = 2
            val requestPermissions = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            if (!(ContextCompat.checkSelfPermission(this@MainActivity, requestPermissions[0]) == PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(this@MainActivity, requestPermissions[1]) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this@MainActivity, requestPermissions, requestPermissionsCode)
            } else {
                val message = String.format("Error in DataSourceStatusChangedListener: %s",
                        dataSourceStatusChangedEvent.source.locationDataSource.error.message)
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            }
        }
        /* ** ADD ** */
        mLocationDisplay.autoPanMode = LocationDisplay.AutoPanMode.COMPASS_NAVIGATION
        mLocationDisplay.startAsync()
    }

    private fun createGraphicsOverlay() {
    mGraphicsOverlay = GraphicsOverlay()
    mMapView.graphicsOverlays.add(mGraphicsOverlay);
    }

    private fun createPointGraphics() {
        val point = Point(-123.1886, 49.3379, SpatialReferences.getWgs84())
        val pointSymbol =
            SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.rgb(226, 119, 40), 10.0f)
        pointSymbol.outline = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 2.0f)
        val pointGraphic = Graphic(point, pointSymbol)
        mGraphicsOverlay.graphics.add(pointGraphic)
    }

    private fun createGraphics() {
        createGraphicsOverlay()
        createPointGraphics()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mLocationDisplay.startAsync()
        } else {
            Toast.makeText(this@MainActivity, "resources.getString(R.string.location_permission_denied)", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        mMapView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mMapView.resume()
    }

    override fun onDestroy() {
        mMapView.dispose()
        super.onDestroy()
    }
}

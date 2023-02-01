package com.example.kotlinmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.NetworkOnMainThreadException
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kotlinmap.databinding.ActivityMapsBinding
import com.example.models.Coordinate
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.text.DecimalFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
    LocationListener,
    GoogleApiClient.OnConnectionFailedListener {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val REQUEST_LOCATION_PERMISSION = 1

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var ggApiClient: GoogleApiClient
    private lateinit var location: Location
    private lateinit var locationRequest: LocationRequest
    private val database = Firebase.database
    private val storage = Firebase.storage

    private var trackerId: String = ""
    private var trackerName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //getLastLocation()

        trackerId = intent.getStringExtra("tracking_uid").toString();
        trackerName = intent.getStringExtra("tracker_name").toString()

        //  an vao nut xem vi tri
        binding.btnXemViTri.text = "Xem vị trí của " + trackerName
        binding.btnXemViTri.setOnClickListener {

            // lay vi tri cua ng kia -> update len UI
            var ref = database.getReference("Locations").child(trackerId)

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var location = snapshot.getValue(Coordinate::class.java)
                    if (location != null) {
                        map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(location.lat,
                            location.long)))
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.lat,
                            location.long), 15f));
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var location = snapshot.getValue(Coordinate::class.java)
                    if (location != null) {
                        map.clear()
                        zoomToLocation(LatLng(location.lat, location.long),
                            trackerId,
                            "Vị trí của ${trackerName}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
            binding.btnXemChiTiet.visibility = View.VISIBLE
        }

        binding.btnXemChiTiet.setOnClickListener {
            openBottomSheetDialog()
        }
    }

    private fun openBottomSheetDialog() {
        val view = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
        var btSheetDialog = BottomSheetDialog(this)
        btSheetDialog.setContentView(view)
        btSheetDialog.show()
        setUpDataForBottomSheet(view)
    }

    private fun setUpDataForBottomSheet(view: View) {
        val tit1: TextView;
        var tit2: TextView
        var lat1: TextView;
        var long1: TextView;
        var location1: TextView
        var lat2: TextView;
        var long2: TextView;
        var location2: TextView
        var distance: TextView

        tit1 = view.findViewById(R.id.txt_my_tit)
        tit2 = view.findViewById(R.id.txt_tit_2)
        tit1.text = "Vi tri cua ban"; tit2.text = "Vi tri cua ${trackerName}"

        distance = view.findViewById(R.id.txt_distance_sheet)

        // cap nhat theo realtime vi tri hien tai cua 2 nguoi + tinh khoang cach 2 toa do
        val ref = database.getReference("Locations")
        // cap nhat vi tri cho nguoi 1
        Firebase.auth.currentUser?.let { ref.child(it.uid) }
            ?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var coordinate = snapshot.getValue(Coordinate::class.java)
                    lat1 = view.findViewById(R.id.txt_lat_1)
                    long1 = view.findViewById(R.id.txt_long_1)
                    location1 = view.findViewById(R.id.txt_location_1)
                    if (coordinate != null) {
                        lat1.text = "Vi do: " + coordinate.lat.toString()
                        long1.text = "Kinh do: " + coordinate.long.toString()
                        try {
                            location1.text =
                                "Dia chi: " + getAddressByCoordinate(coordinate.lat, coordinate.long)
                        } catch(e: NetworkOnMainThreadException) {
                            e.stackTrace
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        // cap nhat vi tri cho nguoi 2
        database.getReference("Locations/${trackerId}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var coordinate = snapshot.getValue(Coordinate::class.java)
                    lat2 = view.findViewById(R.id.txt_lat_2)
                    long2 = view.findViewById(R.id.txt_long_2)
                    location2 = view.findViewById(R.id.txt_location_2)
                    if (coordinate != null) {
                        lat2.text = "Vi do: " + coordinate.lat.toString()
                        long2.text = "Kinh do: " + coordinate.long.toString()
                        try {
                            location2.text =
                                "Dia chi: " + getAddressByCoordinate(coordinate.lat, coordinate.long)
                        } catch (e: NetworkOnMainThreadException) {
                            e.stackTrace
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        // tinh khoang cach 2 nguoi
        Firebase.auth.currentUser?.let {
            database.getReference("Locations").child(it.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var myLocation = snapshot.getValue(Coordinate::class.java)
                        var myLat = LatLng(myLocation?.lat ?: 0.0, myLocation?.long ?: 0.0)
                        database.getReference("Locations").child(trackerId)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    var otherLocation = snapshot.getValue(Coordinate::class.java)
                                    var otherLat = LatLng(otherLocation?.lat ?: 0.0,
                                        otherLocation?.long ?: 0.0)
                                    var dis = CalculationByDistance(myLat, otherLat)
                                    distance.text =
                                        "Khoang cach dia ly: " + String.format("%.2f", dis) + " km"
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }

    private fun getAddressByCoordinate(latitude: Double, longitude: Double): String {
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(this, Locale.getDefault())

        addresses = geocoder.getFromLocation(latitude,
            longitude,
            1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        val address: String = addresses[0].getAddressLine(0)
        val city: String = addresses[0].getLocality()
        val state: String = addresses[0].getAdminArea()
        val country: String = addresses[0].getCountryName()
        val knownName: String = addresses[0].getFeatureName() // Only if available else return NULL
        return address
    }

    override fun onResume() {
        super.onResume()
        //startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        // stopLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        buildGoogleApiClient()
        map.isMyLocationEnabled = true

    }

    protected fun buildGoogleApiClient() {
        ggApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        ggApiClient.connect()
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }


    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    override fun onConnected(p0: Bundle?) {
        locationRequest = LocationRequest()
        locationRequest.setInterval(10000)
        locationRequest.setFastestInterval(5000)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(ggApiClient, locationRequest, this)
    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onLocationChanged(p0: Location) {
        location = p0
        var latLng = LatLng(p0.latitude, p0.longitude)

        //Log.d("lc", latLng.latitude.toString())

        zoomToLocation(latLng, Firebase.auth.currentUser?.uid ?: "", "Vị trí của bạn")

        // save my location into firebase
        var ref = database.getReference("Locations").child(Firebase.auth.currentUser?.uid ?: "")
        var coordinate = Coordinate(p0.latitude, p0.longitude)
        ref.setValue(coordinate)
    }

    private fun zoomToLocation(latLng: LatLng, uid: String, text: String) {
        map.clear()
        //map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f))

        val storageRef = storage.getReference("avatar").child(uid)
        val localFile = File.createTempFile("img_marker", "jpeg")
        storageRef.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            val bitmapForMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false)

            map.addMarker(MarkerOptions().position(latLng).title(text)
                .icon(BitmapDescriptorFactory.fromBitmap(getCroppedBitmap(bitmapForMarker))))
        }
    }

    fun getCroppedBitmap(bitmap: Bitmap): Bitmap? {
        val output = Bitmap.createBitmap(bitmap.width,
            bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        paint.setAntiAlias(true)
        canvas.drawARGB(0, 0, 0, 0)
        paint.setColor(color)
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle((bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
            (bitmap.width / 2).toFloat(), paint)
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        canvas.drawBitmap(bitmap, rect, rect, paint)
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output
    }

    fun CalculationByDistance(StartP: LatLng, EndP: LatLng): Double {
        val Radius = 6371 // radius of earth in Km
        val lat1 = StartP.latitude
        val lat2 = EndP.latitude
        val lon1 = StartP.longitude
        val lon2 = EndP.longitude
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = (Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + (Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2)))
        val c = 2 * Math.asin(Math.sqrt(a))
        val valueResult = Radius * c
        val km = valueResult / 1
        val newFormat = DecimalFormat("####")
        val kmInDec: Int = Integer.valueOf(newFormat.format(km))
        val meter = valueResult % 1000
        val meterInDec: Int = Integer.valueOf(newFormat.format(meter))
        return Radius * c
    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }


}
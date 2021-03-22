package app.sosapp.sos.sosapp.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import app.sosapp.sos.sosapp.data.SOSAppSharedPrefs
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import java.util.*
import app.sosapp.sos.sosapp.R
import com.google.android.gms.common.api.ResolvableApiException

class LocationUtil(private val mActivity: Activity) {

    companion object {
        private val TAG by lazy { "LocationUtil" }

        const val PERMISSION_REQUEST_CODE_LOCATION = 1006
        val locPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

        fun hasLocationPermissions(mContext: Context): Boolean {
            for (permission in locPermissions) {
                if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED)
                    return false
            }
            return true
        }

        fun isLocationServiceEnabled(mContext: Context): Boolean{
            kotlin.runCatching {
                (mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager?)?.let { manager->
                    return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                }
            }.onFailure {
                Log.e(TAG, "isLocServiceEnabled Exc $it")
            }
            return false
        }

    }

    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mActivity)


    fun checkAndRequestGPS(onComplete: (Exception?) -> Unit){
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        mActivity.setFirebaseAnalyticsLogEvent("CHECK_REQUEST_GPS", bundleOf("Status" to "requesting"))

        LocationServices.getSettingsClient(mActivity)
            .checkLocationSettings(builder.build()).addOnCompleteListener {
                try {
                    val response: LocationSettingsResponse = it.getResult(ApiException::class.java)
                    onComplete(null)
                    mActivity.setFirebaseAnalyticsLogEvent("CHECK_REQUEST_GPS", bundleOf("Status" to "requested"))
                }catch (e: ApiException){
                    Log.e(TAG, "checkAndRequestGPS Exc $e")
                    when(e.statusCode){
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                val resolvable: ResolvableApiException = e as ResolvableApiException
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(mActivity, LocationRequest.PRIORITY_HIGH_ACCURACY)
                                mActivity.setFirebaseAnalyticsLogEvent("CHECK_REQUEST_GPS", bundleOf("Status" to "requested"))
                            } catch (e: IntentSender.SendIntentException) { /*Ignore*/
                            } catch (e: Exception) {
                                onComplete(e)
                                e.reportException("CHECK_REQUEST_GPS Error")
                                mActivity.setFirebaseAnalyticsLogEvent("CHECK_REQUEST_GPS", bundleOf("Status" to "error"))
                            }
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            onComplete(e)
                            e.reportException("CHECK_REQUEST_GPS Error")
                            mActivity.setFirebaseAnalyticsLogEvent("CHECK_REQUEST_GPS", bundleOf("Status" to "error"))
                        }
                    }
                }
            }
    }

    fun getSOSMessage(mPrefs: SOSAppSharedPrefs, onComplete: (String?) -> Unit){
        var msg = mPrefs.sosMessage?:SOSAppRes.getString(R.string.msg_default_sos_msg)
        msg += "\n"
        mActivity.setFirebaseAnalyticsLogEvent("GET_SOS_MSG", bundleOf("Status" to "getting"))
        getLatLon { location->
            mActivity.setFirebaseAnalyticsLogEvent("GET_SOS_MSG", bundleOf("Status" to if (location == null)"location_null" else "Got_MSG"))
            if (location == null) {
                mActivity.toast(SOSAppRes.getString(R.string.err_msg_location_fetch_failed))
                onComplete(SOSAppRes.getString(R.string.err_msg_location_fetch_failed))
            } else {
                if (mPrefs.sosAddressOn) {
                    getAddressFromLatLon(location.latitude, location.longitude) { address ->
                        if (!address.isNullOrBlank()) {
                            msg += address
                            msg += "\n\n"
                        }
                        msg += "https://maps.google.com/maps?daddr=${location.latitude},${location.longitude}"
                        onComplete(msg)
                    }
                }else {
                    msg += "\n\nhttps://maps.google.com/maps?daddr=${location.latitude},${location.longitude}"
                    onComplete(msg)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLatLon(onLatLonFound: (Location?) -> Unit){
        kotlin.runCatching {
            fusedLocationClient.lastLocation.addOnCompleteListener {
                Log.d(TAG, "getLastLocation OnComplete Result -> ${it.result} | Exc -> ${it.exception?.message}")
                if (it.isSuccessful && it.result != null){
                    onLatLonFound(it.result)
                }else{
                    onLatLonFound(null)
                }
            }.addOnFailureListener {
                Log.e(TAG, "getLastLocation OnFailure $it")
                onLatLonFound(null)
            }
        }.onFailure {
            Log.e(TAG, "getLastLocation Exc $it")
            it.reportException("getLatLon")
            onLatLonFound(null)
        }
    }

    fun getAddressFromLatLon(latitude: Double, longitude: Double, onAddressFound: (String?) -> Unit) {
        val geoCoder = Geocoder(mActivity, Locale.getDefault())
        kotlin.runCatching {
            Log.d("LocalityLatLng", "$latitude,$longitude")
            val addresses = geoCoder.getFromLocation(latitude, longitude, 5)
            if (!addresses.isNullOrEmpty()) {
                val address = filterAddress(addresses)
                Log.d(TAG, "Address Obj : $address")
                Log.d(TAG, "LocalityCity : ${address.locality}")
                Log.d(TAG, "SubLocalityCity : ${address.subLocality}")
                Log.d(TAG, "AdminSubAdminArea : ${address.adminArea} | ${address.subAdminArea}")

                onAddressFound(address.getAddressLine(0))
            }
        }.onFailure {
            it.reportException("getAddressFromLatLon")
            Log.e(TAG, "GeoCoder Exc $it")
        }
    }

    private fun filterAddress(addresses: List<Address>): Address{
        addresses.filter {
            !it.getAddressLine(0).contains("Unnamed Road", true)
        }.also {unNamedRemoved->
            if (unNamedRemoved.isNullOrEmpty()) return addresses[0]
            else {
                kotlin.runCatching {
                    unNamedRemoved.filter {
                        it.getAddressLine(0) != null
                    }.also {finalFilter->
                        if (finalFilter.isNullOrEmpty()) unNamedRemoved[0]
                        else {
                            finalFilter.maxByOrNull { it.getAddressLine(0).length }?:finalFilter[0]
                        }
                    }
                }.onFailure {
                    it.reportException("filterAddress")
                    return addresses[0]
                }
            }
        }
        return addresses[0]
    }

}
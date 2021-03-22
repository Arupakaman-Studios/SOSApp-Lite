package app.sosapp.sos.sosapp.utils

import android.annotation.TargetApi
import android.content.Context
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.os.bundleOf
import app.sosapp.sos.sosapp.R


class SOSFlashUtil(private val mContext: Context) {

    companion object{
        private val TAG by lazy { "SOSFlashUtil" }

    }

    private var flashStat = 0
    private var getStat = 2
    private var isFlashOn = false
    private var isWidgetFlash = false

    @Suppress("DEPRECATION")
    private var mCamera: Camera? = null
    private var mCameraManager: CameraManager? = null

    private var mHandler: Handler? = null
    private var mRunnable: Runnable? = null


    fun startSOSFlash(){
        releaseCamera()
        isFlashOn = false
        mHandler = Handler(Looper.getMainLooper())

        val offMethod: Function<Unit>
        val onMethod: Function<Unit>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            onMethod = ::turnOnFlash23
            offMethod = ::turnOffFlash23
        } else{
            onMethod = ::turnOnFlash
            offMethod = ::turnOffFlash
        }
        mRunnable = Runnable {
            isFlashOn = if (isFlashOn){
                offMethod()
                false
            }else{
                onMethod()
                true
            }
            mHandler?.postDelayed(mRunnable!!, 500)
        }
        mRunnable?.run()
        mContext.setFirebaseAnalyticsLogEvent("SOS_Flash_Toggled", bundleOf("Flash" to "Turned"))
    }

    fun stopFlash(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) turnOffFlash23()
        else turnOffFlash()
        releaseCamera()
        removeHandlerCall()
    }

    @Suppress("DEPRECATION")
    private fun turnOnFlash() {
        if (mCamera == null) {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
        }
        if (mCamera == null) {
            mContext.toast(SOSAppRes.getString(R.string.err_msg_general))
            FirebaseReporterUtil.reportException("turnOnFlash mCamera == null")
            removeHandlerCall()
            return
        }
        mCamera?.apply {
            val p: Camera.Parameters = parameters
            p.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            parameters = p
            startPreview()
        }
        Log.d(TAG, "Flash Normal ")
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun turnOnFlash23() {
        kotlin.runCatching {
            mCameraManager = mContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = getCamId() // Usually front camera is at 0 position.
            if (mCameraManager != null) {
                mCameraManager?.setTorchMode(cameraId, true)
            }
        }.onFailure {
            mContext.toast(SOSAppRes.getString(R.string.err_msg_general))
            it.reportException("turnOnFlash23")
        }
        Log.v(TAG, "Flash Normal 23")
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun turnOffFlash23() {
        try {
            val cameraId = getCamId()
            mCameraManager = mContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            if (mCameraManager != null) {
                mCameraManager?.setTorchMode(cameraId, false)
            }
        } catch (e: CameraAccessException) {
            mContext.toast(SOSAppRes.getString(R.string.err_msg_general))
            e.reportException("turnOffFlash23")
        }
    }

    @Suppress("DEPRECATION")
    private fun turnOffFlash() {
        if (mCamera == null) {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
        }
        if (mCamera == null) {
            mContext.toast(SOSAppRes.getString(R.string.err_msg_general))
            FirebaseReporterUtil.reportException("turnOffFlash mCamera == null")
            removeHandlerCall()
            return
        }
        mCamera?.apply {
            val p: Camera.Parameters = parameters
            p.flashMode = Camera.Parameters.FLASH_MODE_OFF
            parameters = p
            stopPreview()
        }
    }

    @Suppress("DEPRECATION")
    private fun releaseCamera() {
        mCamera?.apply {
            val param: Camera.Parameters = parameters
            param.flashMode = Camera.Parameters.FLASH_MODE_OFF
            parameters = param
            stopPreview()
            release()
            mCamera = null
        }
    }

    private fun removeHandlerCall() {
        if (mHandler != null) {
            kotlin.runCatching {
                mRunnable?.let {
                    mHandler?.removeCallbacks(it)
                }
            }.onFailure {
                it.reportException("removeHandlerCall")
            }
            mHandler = null
            mRunnable = null
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getCamId(): String {
        val manager = (mContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager?)?: return "0"
        kotlin.runCatching {
            Log.d(TAG, "getCamId -> ${manager.cameraIdList}")
            return manager.cameraIdList[0]?:"0"
        }.onFailure {
            Log.e(TAG, "getCamId Exc -> $it")
            it.reportException("getCamId")
        }
        return "0"
    }

}
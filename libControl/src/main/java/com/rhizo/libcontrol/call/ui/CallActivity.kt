package com.rhizo.libcontrol.call.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.rhizo.libcontrol.R
import com.rhizo.libcontrol.call.webrtc.WebrtcManager
import com.rhizo.libcontrol.databinding.ActivityCallBinding
import com.rhizo.libcontrol.util.ClickExt.setOnSafeClick
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

class CallActivity : AppCompatActivity() {
    private val ALL_PERMISSIONS_CODE = 1000
    private lateinit var mBinding: ActivityCallBinding
    private var mWebrtcManager: WebrtcManager? = null
    private var mRemoteSurfaceView: SurfaceViewRenderer? = null
    private var mLocalSurfaceView: SurfaceViewRenderer? = null
    private var mCallIn = false
    private var mPowerManager: PowerManager? = null
    private var mWakeLock: PowerManager.WakeLock? = null

    companion object {
        const val ACTION_PREPARED = "action_prepared"
    }

    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_call)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mPowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager?
        mWakeLock = mPowerManager?.newWakeLock(PowerManager.FULL_WAKE_LOCK, "CallActivity")

        mRemoteSurfaceView = SurfaceViewRenderer(applicationContext)
        mLocalSurfaceView = SurfaceViewRenderer(applicationContext)
        mBinding.flVideo.addView(mRemoteSurfaceView)
        mBinding.flVideoLocal.addView(mLocalSurfaceView)

        mBinding.btnDecline.setOnSafeClick {
            mWebrtcManager?.doHangup()
            window.decorView.postDelayed({
                finish()
            }, 1000)
        }

        mWebrtcManager = WebrtcManager(applicationContext)
        mWebrtcManager?.setEventUICallback(mEventUICallBack)

        turnOnScreen()

        //请求视频通话所需权限
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                ),
                ALL_PERMISSIONS_CODE
            )
        } else {
            start()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ALL_PERMISSIONS_CODE
            && grantResults.size == 2
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
            && grantResults[1] == PackageManager.PERMISSION_GRANTED
        ) {
            start()
        } else {
            finish()
        }
    }

    /**
     * 开始接听
     */
    private fun start() {
        mWebrtcManager?.init(mLocalSurfaceView, mRemoteSurfaceView)
        window.decorView.postDelayed({
            sendBroadcast(Intent(ACTION_PREPARED))
        }, 1000)
        window.decorView.postDelayed({
            if (!mCallIn) {
                finish()
            }
        }, 8000)
    }

    /**
     * 点亮
     */
    private fun turnOnScreen() {
        mWakeLock?.acquire(10 * 60 * 1000)
    }

    /**
     * 熄灭
     */
    private fun turnOffScreen() {
        if (mWakeLock != null && mWakeLock!!.isHeld) {
            mWakeLock?.release()
        }
    }

    private val mEventUICallBack: WebrtcManager.EventUICallBack =
        object : WebrtcManager.EventUICallBack {

            override fun callIn(senderIp: String?) {
                runOnUiThread {
                    mCallIn = true
                    mWebrtcManager?.doAnswer()
                }
            }

            override fun showVideo(videoTrack: VideoTrack) {
                runOnUiThread {
                    try {
                        videoTrack.addSink(mRemoteSurfaceView)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun receiveHangup() {
                runOnUiThread {
                    mLocalSurfaceView?.clearImage()
                    mRemoteSurfaceView?.clearImage()
                    finish()
                }
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        mRemoteSurfaceView?.let {
            it.pauseVideo()
            it.release()
        }
        mLocalSurfaceView?.let {
            it.pauseVideo()
            it.release()
        }
        mBinding.flVideo.removeAllViews()
        mBinding.flVideoLocal.removeAllViews()
        turnOffScreen()
    }

}
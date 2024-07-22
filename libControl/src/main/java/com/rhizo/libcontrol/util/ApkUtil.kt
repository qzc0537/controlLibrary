package com.rhizo.libcontrol.util

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

object ApkUtil {
    val APK_DIR = "apk"
    private var mApplication: Application? = null
    private var mDownloading = false
    private val mExecutor = Executors.newSingleThreadExecutor()
    private val mHandler = Handler(Looper.getMainLooper())
    private var mLastUpdateTime = 0L
    private var mApkDownloadListener: ApkDownloadListener? = null

    fun setApplication(context: Application) {
        mApplication = context
    }

    private fun checkApplication() {
        if (mApplication == null) {
            throw RuntimeException("mApplication is null")
        }
    }

    fun isDownloading(): Boolean {
        return mDownloading
    }

    /**
     * 下载apk
     */
    fun downloadApk(version: String, apkUrl: String, listener: ApkDownloadListener) {
        checkApplication()
        if (mDownloading) {
            return
        }
        if (apkUrl.isEmpty()) {
            return
        }
        mDownloading = true
        mApkDownloadListener = listener
        mApkDownloadListener?.onApkDownloadStart()

        mExecutor.execute {
            val tempFile = File(getApkDir(), "temp_$version.apk")
            val destFile = getApkFile(version)
            if (!tempFile.exists()) {
                tempFile.createNewFile()
            } else {
                tempFile.delete()
            }
            try {
                val url = URL(apkUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.requestMethod = "GET"
                if (connection.responseCode != 200) {
                    mDownloading = false
                    return@execute
                }
                val contentLength = connection.contentLength
                val inputStream = connection.inputStream
                val outputStream = FileOutputStream(tempFile)
                var len: Int
                var readLen = 0
                val buffer = ByteArray(2048)
                while (inputStream.read(buffer).also { len = it } != -1) {
                    outputStream.write(buffer, 0, len)
                    readLen += len
                    val progress = (readLen * 100L / contentLength).toInt()
                    if (System.currentTimeMillis() - mLastUpdateTime >= 1000) {
                        mLastUpdateTime = System.currentTimeMillis()
                        mHandler.post {
                            if (progress > 0) {
                                mApkDownloadListener?.onApkDownloadProgress(progress)
                            }
                        }
                    }
                }
                outputStream.close()
                inputStream.close()
                tempFile.renameTo(destFile)
                tempFile.delete()
                mHandler.post {
                    mApkDownloadListener?.onApkDownloadSuccess(destFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                tempFile.delete()
                mHandler.post {
                    mApkDownloadListener?.onApkDownloadFailed(e.message ?: "")
                }
            } finally {
                mDownloading = false
            }
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        mApkDownloadListener = null
    }

    /**
     * apk安装
     */
    fun installApk(file: File) {
        checkApplication()
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val uri =
                FileProvider.getUriForFile(
                    mApplication!!.applicationContext,
                    mApplication!!.applicationContext.packageName + ".fileProvider",
                    file
                )
            intent.setDataAndType(
                uri,
                "application/vnd.android.package-archive"
            )
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
        }
        mApplication?.startActivity(intent)
    }

    /**
     * 获取apk目录
     */
    fun getApkDir(): File {
        checkApplication()
        val apkDir = File(mApplication!!.filesDir, APK_DIR)
        if (!apkDir.exists()) {
            apkDir.mkdir()
        }
        return apkDir
    }

    /**
     * 获取apk文件
     */
    fun getApkFile(version: String): File {
        checkApplication()
        val apkDir = File(mApplication!!.filesDir, APK_DIR)
        if (!apkDir.exists()) {
            apkDir.mkdir()
        }
        return File(apkDir, "$version.apk")
    }

    /**
     * 删除apk
     */
    fun deleteApk(currentVersionName: String) {
        checkApplication()
        val dir = getApkDir()
        val files = dir.listFiles() ?: emptyArray()
        for (item in files) {
            if (item.name == "$currentVersionName.apk") {
                item.delete()
                break
            }
        }
    }

    interface ApkDownloadListener {
        fun onApkDownloadStart()
        fun onApkDownloadProgress(progress: Int)
        fun onApkDownloadSuccess(file: File)
        fun onApkDownloadFailed(msg: String)
    }

}
package com.rhizo.libcontrol

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.permissionx.guolindev.PermissionX
import com.rhizo.bluetooth.bean.BluetoothMessage
import com.rhizo.bluetooth.bean.WifiBean
import com.rhizo.bluetooth.bluetooth.BluetoothServer
import com.rhizo.bluetooth.bluetooth.OnBluetoothUICallback
import com.rhizo.bluetooth.util.ConnectWifiUtils
import com.rhizo.common.util.AppUtil
import com.rhizo.common.util.GsonUtil
import com.rhizo.common.util.TimeUtil
import com.rhizo.libcontrol.ui.CallActivity
import com.rhizo.libcontrol.bean.RegisterCloudBean
import com.rhizo.libcontrol.bean.VersionBean
import com.rhizo.libcontrol.util.CLog
import com.rhizo.libcontrol.util.MessageUtil
import com.rhizo.libcontrol.util.RobotUtil
import com.rhizo.libentity.BaseMessage
import com.rhizo.libentity.BatteryBean
import com.rhizo.libentity.BeWithMeStatus
import com.rhizo.libentity.ChassisSerialBean
import com.rhizo.libentity.DeleteLocation
import com.rhizo.libentity.DeviceInit
import com.rhizo.libentity.FinishMapping
import com.rhizo.libentity.GotoLocation
import com.rhizo.libentity.GotoLocationStatus
import com.rhizo.libentity.PositionBean
import com.rhizo.libentity.ResetMap
import com.rhizo.libentity.SaveLocation
import com.rhizo.libentity.SkidJoy
import com.rhizo.libentity.TiltAngle
import com.rhizo.libentity.TurnBy
import com.rhizo.libentity.UpdateLocation
import com.rhizo.libentity.UpdateLocationResult
import com.rhizo.libentity.UpdateMapName
import com.rhizo.libtcp.netty.NettyTcpClientUICallback
import com.rhizo.libtcp.netty.NettyTcpServer
import com.rhizo.libtcp.netty.NettyTcpServerUICallback
import com.rhizo.libtcp.netty.TcpConstants
import com.rhizo.libcontrol.netty.TcpManager
import com.robotemi.sdk.BatteryData
import com.robotemi.sdk.Robot
import com.robotemi.sdk.constants.Page
import com.robotemi.sdk.listeners.OnBatteryStatusChangedListener
import com.robotemi.sdk.listeners.OnBeWithMeStatusChangedListener
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener
import com.robotemi.sdk.navigation.listener.OnCurrentPositionChangedListener
import com.robotemi.sdk.navigation.model.Position
import com.robotemi.sdk.permission.Permission
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.util.concurrent.Executors


class ControlPlugin(private val mContext: AppCompatActivity) : ConnectWifiUtils.WifiConnectCallback,
    OnBluetoothUICallback, NettyTcpServerUICallback,
    OnBeWithMeStatusChangedListener, OnBatteryStatusChangedListener,
    OnGoToLocationStatusChangedListener, OnCurrentPositionChangedListener {
    private val TAG = ControlPlugin::class.java.simpleName
    private val mBluetoothAdapter: BluetoothAdapter by lazy { BluetoothAdapter.getDefaultAdapter() }
    private var mWifiManager: WifiManager? = null
    private var mConnectWifiUtils: ConnectWifiUtils? = null
    private var mWifiBean: WifiBean? = null
    private var mConnecting = false
    private var mBluetoothDevice: BluetoothDevice? = null
    private var mPositionBean: PositionBean? = null
    private var mRobotSerialNumber = ""
    private var mAndroidVersion = ""
    private var mRobotxVersion = ""
    private val mThreadPool = Executors.newFixedThreadPool(3)
    private val CODE_GET_WIFI_INFO = 1
    private val CODE_LISTEN_BLUETOOTH_ON = 2
    private var mSp: SharedPreferences? = null


    private val mHandler = Handler(Looper.getMainLooper()) {
        when (it.what) {
            CODE_GET_WIFI_INFO -> {
                val wifiBean = getWifiBean(mContext)
                sendWifiInfoToClient(wifiBean, true)
            }

            CODE_LISTEN_BLUETOOTH_ON -> {
                whetherBluetoothOn()
            }
        }
        true
    }

    /**
     * 初始化
     */
    fun init(androidVersion: String) {
        mAndroidVersion = androidVersion
        if (isBluetoothEnable()) {
            whetherBluetoothOn()
        } else {
            showOpenBluetoothDialog()
            mHandler.sendEmptyMessageDelayed(CODE_LISTEN_BLUETOOTH_ON, 2000)
        }
    }

    /**
     * 初始化
     */
    private fun innerInit() {
        mSp = mContext.getSharedPreferences("ControlPlugin", Context.MODE_PRIVATE)
        mWifiManager =
            mContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        mConnectWifiUtils = ConnectWifiUtils(mContext)
        mConnectWifiUtils?.setWifiConnectCallback(this)
        if (mWifiManager?.isWifiEnabled == false) {
            mWifiManager?.setWifiEnabled(true)
        }

        //蓝牙相关权限
        BluetoothServer.getInstance().addCallback(this)
        setDiscoverableTimeout()
        initAppPermission()

        //开启Tcp服务
        if (AppUtil.getNetworkType(mContext) == AppUtil.TYPE_WIFI) {
            val wifiBean = getWifiBean(mContext)
            TcpManager.startServer(wifiBean.ipAddress, this)
        }

        //连接服务器
//        TcpManager.startCloudClient(
//            "dev.tc-etc.cn",
//            7789,
//            mCloudTcpClientUICallback
//        )

        //视频通话准备完成广播
        mContext.registerReceiver(mCallReceiver, IntentFilter(CallActivity.ACTION_PREPARED))

        //temi 监听
        Robot.getInstance().addOnBeWithMeStatusChangedListener(this)
        Robot.getInstance().addOnBatteryStatusChangedListener(this)
        Robot.getInstance().addOnGoToLocationStatusChangedListener(this)
        Robot.getInstance().addOnCurrentPositionChangedListener(this)
    }

    /**
     * 蓝牙是否打开
     */
    private fun whetherBluetoothOn() {
        if (isBluetoothEnable()) {
            mHandler.removeMessages(CODE_LISTEN_BLUETOOTH_ON)
            mContext.window.decorView.postDelayed({
                mRobotxVersion = Robot.getInstance().roboxVersion
                mRobotSerialNumber = Robot.getInstance().serialNumber ?: ""
                innerInit()
            }, 1000)
        } else {
            mHandler.sendEmptyMessageDelayed(CODE_LISTEN_BLUETOOTH_ON, 2000)
        }
    }

    /**
     * 注册到服务器的内容
     */
    private fun registerCloudJson(
        androidVersion: String,
        baseVersion: String,
        hardwareVersion: String = "1"
    ): String {
        val timezone = TimeUtil.getTimeZoneShort()
        val time = TimeUtil.getTime_yyyy_MM_dd_hh_mm_ss()
        val versionBean = VersionBean(
            androidVersion,
            baseVersion,
            hardwareVersion
        )
        val versionJson = GsonUtil.toJson(versionBean)
        val registerCloudBean = RegisterCloudBean(
            "000002",
            "30303030",
            "11213141516171819111",
            mRobotSerialNumber,
            timezone,
            time,
            versionJson
        )
        val json = GsonUtil.toJson(registerCloudBean)
        CLog.d(TAG, "registerCloudJson:$json")
        return json
    }

    private val mCloudTcpClientUICallback = object : NettyTcpClientUICallback {
        override fun onTcpConnected() {
            super.onTcpConnected()
            CLog.d(TAG, "startCloudClient->onTcpConnected")
            TcpManager.sendCloudClientMessage(
                TcpConstants.CloudCommand.REGISTER_CLOUD,
                registerCloudJson(mAndroidVersion, mRobotxVersion)
            )
        }

        override fun onTcpDisConnected() {
            super.onTcpDisConnected()
            Log.e(TAG, "startCloudClient->onTcpConnected")
        }

        override fun onTcpError(message: String) {
            super.onTcpError(message)
            Log.e(TAG, "startCloudClient->onTcpConnected")
        }

        override fun onTcpMessage(command: Int, json: String) {
            super.onTcpMessage(command, json)
            CLog.d(TAG, "startCloudClient->onTcpMessage:command:$command json:$json")
        }
    }

    private val mCallReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: ""
            if (action == CallActivity.ACTION_PREPARED) {
                MessageUtil.sendVideoCall(mRobotSerialNumber)
            }
        }

    }

    /**
     * 蓝牙是否打开
     */
    private fun isBluetoothEnable(): Boolean {
        return mBluetoothAdapter.isEnabled
    }

    /**
     * 打开蓝牙对话框
     */
    private fun showOpenBluetoothDialog() {
        AlertDialog.Builder(mContext)
            .setCancelable(false)
            .setMessage(R.string.lib_control_please_turn_on_bluetooth)
            .setNegativeButton(R.string.lib_control_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.lib_control_to_open) { dialog, _ ->
                dialog.dismiss()
//                BluetoothUtil.openBluetoothSettings(mContext)
                Robot.getInstance().startPage(Page.SETTINGS)
            }.show()
    }

    /**
     * 申请应用所需相关权限
     */
    private fun initAppPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionX.init(mContext)
                .permissions(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        startBluetoothServer()
                    }
                }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PermissionX.init(mContext)
                    .permissions(
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    .request { allGranted, grantedList, deniedList ->
                        if (allGranted) {
                            startBluetoothServer()
                        }
                    }
            } else {
                startBluetoothServer()
            }
        }
    }

    /**
     * 开启蓝牙服务
     */
    private fun startBluetoothServer() {
        BluetoothServer.getInstance().start()
        requestTemiAllPermission()
    }

    /**
     * 设置可发现超时时间
     */
    private fun setDiscoverableTimeout(timeout: Int = 100) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        try {
            val setDiscoverableTimeout =
                BluetoothAdapter::class.java.getMethod(
                    "setDiscoverableTimeout",
                    Int::class.javaPrimitiveType
                )
            setDiscoverableTimeout.isAccessible = true
            val setScanMode =
                BluetoothAdapter::class.java.getMethod(
                    "setScanMode",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                )
            setScanMode.isAccessible = true
            setDiscoverableTimeout.invoke(adapter, timeout)
            setScanMode.invoke(
                adapter,
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE,
                timeout
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setDiscoverableTimeout: " + e.message)
        }
    }

    /**
     * 获取Temi所有权限
     */
    private fun requestTemiAllPermission() {
        val permissions: MutableList<Permission> = ArrayList()
        for (permission in Permission.values()) {
            if (Robot.getInstance().checkSelfPermission(permission) == Permission.GRANTED) {
                continue
            }
            permissions.add(permission)
        }
        val size = permissions.size
        if (size > 0) {
            Robot.getInstance().requestPermissions(permissions, 0)
        }
    }

    /**
     * 扫描wifi
     * 注意：必须扫描后才能调用连接方法，不管是api连接还是adb命令连接！
     */
    private fun scanWifi() {
        CLog.d(TAG, "扫描wifi")
        PermissionX.init(mContext)
            .permissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE
            )
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    mWifiManager?.startScan()
                }
            }
    }

    /**
     * 获取WiFi信息
     */
    @SuppressLint("MissingPermission")
    private fun getWifiBean(context: Context): WifiBean {
        val wifiManager =
            context.applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        var ssid = wifiInfo.ssid
        if (ssid.contains("unknown")) {
            val configuredNetworks = wifiManager.configuredNetworks
            for (configuration in configuredNetworks) {
                if (configuration.networkId == wifiInfo.networkId) {
                    ssid = configuration.SSID
                    break
                }
            }
        }
        val ip: Int = wifiInfo.getIpAddress()
        val ipAddress = String.format(
            "%d.%d.%d.%d",
            ip and 0xff,
            ip shr 8 and 0xff,
            ip shr 16 and 0xff,
            ip shr 24 and 0xff
        )
        val bean = if (mWifiBean != null) mWifiBean else WifiBean()
        bean?.apply {
            this.ssid = ssid.replace("\"", "")
            this.ipAddress = ipAddress
            this.serialNo = mRobotSerialNumber
            this.level = wifiInfo.rssi
            this.port = TcpConstants.IP.SERVER_PORT
        }
        return bean!!
    }

    /**
     * 发送WiFi信息给客户端
     */
    @SuppressLint("MissingPermission")
    private fun sendWifiInfoToClient(wifiBean: WifiBean, startServer: Boolean = false) {
        if (wifiBean.isCorrectIp && wifiBean.ssid != WifiBean.INVALID_SSID) {
            MessageUtil.sendWifiInfo(mRobotSerialNumber, wifiBean, mBluetoothDevice)
            if (startServer) {
                TcpManager.startServer(wifiBean.ipAddress, this)
            }
        } else {
            mHandler.sendEmptyMessageDelayed(CODE_GET_WIFI_INFO, 1000)
        }
    }

    /**
     * 获取地图数据
     */
    private fun sendMap() {
        mThreadPool.execute {
            val model = Robot.getInstance().getMapData()
            MessageUtil.sendMap(mRobotSerialNumber, model)
        }
    }

    private val mWifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent == null) {
                return
            }
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                if (ActivityCompat.checkSelfPermission(
                        mContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }

                val scanResults = mWifiManager?.scanResults ?: emptyList()
                for (scanResult in scanResults) {
                    //SSID:GDRJ BSSID:44:32:62:58:2a:24 capabilities:[WPA2-PSK-CCMP][RSN-PSK-CCMP][ESS] level:-44
                    val ssid = scanResult.SSID
                    val bssid = scanResult.BSSID
                    val capabilities = scanResult.capabilities
                    val level = scanResult.level
//                    Log.d(
//                        TAG,
//                        "ssid:$ssid capabilities:$capabilities level:$level ssid2:${mWifiBean?.ssid}"
//                    )
                    mWifiBean?.let {
                        if (!TextUtils.isEmpty(ssid)
                            && !TextUtils.isEmpty(it.ssid)
                            && !TextUtils.isEmpty(it.password)
                            && ssid == it.ssid
                            && !mConnecting
                        ) {
                            CLog.d(TAG, "连接WiFi")
                            mConnecting = true
                            unregisterReceiver()
                            val wifiBean = WifiBean().apply {
                                this.ssid = ssid
                                this.capabilities = capabilities
                                this.level = level
                                this.password = it.password
                            }
                            mConnectWifiUtils?.connectWifi(wifiBean)
                        }
                    }
                }
            }
        }
    }

    private fun unregisterReceiver() {
        try {
            mContext.unregisterReceiver(mWifiScanReceiver)
        } catch (e: Exception) {
            //ignore
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        unregisterReceiver()
        TcpManager.removeServerCallback().stopServer(false)
        TcpManager.removeCloudClientCallback().stopCloudClient(false)
        BluetoothServer.getInstance().removeCallback(this)?.stop()
        mConnectWifiUtils?.unregisterReceiver()
        mHandler.removeCallbacksAndMessages(null)
        mContext.unregisterReceiver(mCallReceiver)

        Robot.getInstance().removeOnBeWithMeStatusChangedListener(this)
        Robot.getInstance().removeOnBatteryStatusChangedListener(this)
        Robot.getInstance().removeOnGoToLocationStatusChangedListener(this)
        Robot.getInstance().removeOnCurrentPositionChangedListener(this)
    }

    override fun onBluetoothConnected(device: BluetoothDevice) {
        super.onBluetoothConnected(device)
        CLog.d(TAG, "蓝牙客户端已连接")
        mBluetoothDevice = device
        var ssid = ""
        val hasWifi = AppUtil.getNetworkType(mContext) == AppUtil.TYPE_WIFI
        if (hasWifi) {
            val wifiBean = getWifiBean(mContext)
            sendWifiInfoToClient(wifiBean)
            ssid = wifiBean.ssid
        }
        mHandler.postDelayed({
            if (ssid != WifiBean.INVALID_SSID) {
                MessageUtil.sendDeviceInit(
                    mRobotSerialNumber,
                    DeviceInit(hasWifi, ssid).also { it.serialNo = mRobotSerialNumber },
                    mBluetoothDevice
                )
            }
        }, 500)
    }

    override fun onBluetoothDisConnected() {
        super.onBluetoothDisConnected()
        Log.e(TAG, "蓝牙客户端已断开")
        mBluetoothDevice = null
    }

    override fun onBluetoothMessage(message: String) {
        super.onBluetoothMessage(message)
        CLog.d(TAG, "onBluetoothMessage:${message}")
        val bluetoothMessage = GsonUtil.fromJson<BluetoothMessage>(message) ?: return
        handleMessage(bluetoothMessage.code, bluetoothMessage.json)
    }

    override fun onWifiConnectSuccess() {
        mConnecting = false
        TcpManager.stopServer(false)
        mHandler.sendEmptyMessageDelayed(CODE_GET_WIFI_INFO, 1000)
    }

    override fun onWifiConnectFailure() {
        mConnecting = false
        AlertDialog.Builder(mContext)
            .setMessage(R.string.lib_control_wifi_connection_failed)
            .setNegativeButton(R.string.lib_control_cancel) { dialog, _ ->
                dialog.dismiss()
            }.setNegativeButton(R.string.lib_control_confirm) { dialog, _ ->
                dialog.dismiss()
                mWifiBean?.let {
                    mConnecting = true
                    mConnectWifiUtils?.connectWifi(it)
                }
            }.show()
    }

    override fun onWifiConnectLog(msg: String?) {
    }

    override fun onTcpConnected() {
        sendBatteryData()
    }

    override fun onTcpDisConnected() {
    }

    override fun onTcpMessage(command: Int, json: String) {
        handleMessage(command, json)
    }

    /**
     * 发送电量信息
     */
    private fun sendBatteryData() {
        val batteryData = Robot.getInstance().batteryData
        CLog.d(TAG, "batteryData: ${GsonUtil.toJson(batteryData)}")
        batteryData?.let {
            val bean = BatteryBean(
                it.level,
                it.isCharging,
                false
            )
            MessageUtil.sendBatteryData(mRobotSerialNumber, bean, mBluetoothDevice)
        }
    }

    /**
     * 处理手机客户端发来的消息
     */
    private fun handleMessage(command: Int, json: String) {
        val baseMessage = GsonUtil.fromJson<BaseMessage>(json) ?: return
        val serialNo = baseMessage.serialNo
        val bodyJson = baseMessage.bodyJson
        CLog.d(TAG, "handleMessage->command:$command json:$json")
        when (command) {
            TcpConstants.LocalCommand.CODE_CONNECT_WIFI -> {
                mConnecting = false
                mContext.registerReceiver(
                    mWifiScanReceiver,
                    IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                )
                mWifiBean = GsonUtil.fromJson(bodyJson)
                scanWifi()
            }

            TcpConstants.LocalCommand.CODE_SCAN_MAP -> {
                RobotUtil.startMapPage()
            }

            TcpConstants.LocalCommand.CODE_BE_WITH_ME -> {
                RobotUtil.beWithMe()
            }

            TcpConstants.LocalCommand.CODE_STOP_MOVEMENT -> {
                RobotUtil.stopMovement()
            }

            TcpConstants.LocalCommand.CODE_GET_MAP_DATA -> {
                sendMap()
            }

            TcpConstants.LocalCommand.CODE_MOVE -> {
                GsonUtil.fromJson<SkidJoy>(bodyJson)?.let {
                    RobotUtil.skidJoy(it)
                }
            }

            TcpConstants.LocalCommand.CODE_TURN_BY -> {
                GsonUtil.fromJson<TurnBy>(bodyJson)?.let {
                    RobotUtil.turnBy(it)
                }
            }

            TcpConstants.LocalCommand.CODE_TILT_ANGLE -> {
                GsonUtil.fromJson<TiltAngle>(bodyJson)?.let {
                    RobotUtil.tiltAngle(it)
                }
            }

            TcpConstants.LocalCommand.CODE_SAVE_LOCATION -> {
                GsonUtil.fromJson<SaveLocation>(bodyJson)?.let {
                    val res = RobotUtil.saveLocation(it)
                    it.result = res
                    MessageUtil.sendSaveLocation(mRobotSerialNumber, it, mBluetoothDevice)
                    if (res) {
                        sendMap()
                    }
                }
            }

            TcpConstants.LocalCommand.CODE_DELETE_LOCATION -> {
                GsonUtil.fromJson<DeleteLocation>(bodyJson)?.let {
                    val res = RobotUtil.deleteLocation(it)
                    it.result = res
                    MessageUtil.sendDeleteLocation(mRobotSerialNumber, it, mBluetoothDevice)
                }
            }

            TcpConstants.LocalCommand.CODE_UPDATE_MAP_NAME -> {
                GsonUtil.fromJson<UpdateMapName>(bodyJson)?.let {
                    val value = RobotUtil.updateMapName(it.mapName)
                    it.result = value
                    MessageUtil.sendUpdateMapName(mRobotSerialNumber, it, mBluetoothDevice)
                }
            }

            TcpConstants.LocalCommand.CODE_BATTERY_CHANGED -> {
                sendBatteryData()
            }

            TcpConstants.LocalCommand.CODE_GOTO_HOME_BASE -> {
                RobotUtil.gotoHomeBase()
            }

            TcpConstants.LocalCommand.CODE_GOTO_LOCATION -> {
                GsonUtil.fromJson<GotoLocation>(bodyJson)?.let {
                    RobotUtil.gotoLocation(it)
                }
            }

            TcpConstants.LocalCommand.CODE_UPDATE_LOCATION -> {
                GsonUtil.fromJson<UpdateLocation>(bodyJson)?.let {
                    mThreadPool.execute {
                        val res = RobotUtil.updateLocation(it)
                        MessageUtil.sendUpdateLocation(
                            mRobotSerialNumber,
                            UpdateLocationResult(res),
                            mBluetoothDevice
                        )
                    }
                }
            }

            TcpConstants.LocalCommand.CODE_NEW_PATH -> {
                //todo
            }

            TcpConstants.LocalCommand.CODE_NEW_PATH -> {
                //todo
            }

            TcpConstants.LocalCommand.CODE_NEW_ELECTRONIC_FENCE -> {
                //todo
            }

            TcpConstants.LocalCommand.CODE_NEW_ELECTRONIC_FENCE -> {
                //todo
            }

            TcpConstants.LocalCommand.CODE_RESET_MAP -> {
                mThreadPool.execute {
                    GsonUtil.fromJson<ResetMap>(bodyJson)?.let {
                        val res = RobotUtil.resetMap(it.allFloor)
                        it.result = res
                        MessageUtil.sendResetMap(mRobotSerialNumber, it, mBluetoothDevice)
                    }
                }
            }

            TcpConstants.LocalCommand.CODE_FINISH_MAPPING -> {
                mThreadPool.execute {
                    GsonUtil.fromJson<FinishMapping>(bodyJson)?.let {
                        val res = RobotUtil.finishMapping(it.mapName)
                        it.result = res
                        MessageUtil.sendFinishMapping(mRobotSerialNumber, it, mBluetoothDevice)
                    }
                }
            }

            TcpConstants.LocalCommand.CODE_SET_HOME_BASE -> {
                mThreadPool.execute {
                    GsonUtil.fromJson<UpdateLocation>(bodyJson)?.let {
                        RobotUtil.updateLocation(it)
                    }
                }
            }

            TcpConstants.LocalCommand.CODE_INTERRUPT_SEND_DATA -> {
                NettyTcpServer.setInterruptSendData(true)
            }

            TcpConstants.LocalCommand.CODE_GET_CHASSIS_SERIAL_NUMBER -> {
                mHandler.postDelayed({
                    val serialNumber = Robot.getInstance().serialNumber
                    if (!serialNumber.isNullOrEmpty()) {
                        val chassisSerialBean = ChassisSerialBean(serialNumber)
                        MessageUtil.sendTemiSerialNumber(
                            mRobotSerialNumber,
                            chassisSerialBean,
                            mBluetoothDevice
                        )
                    }
                }, 1000)
            }

            TcpConstants.LocalCommand.CODE_VIDEO_CALL -> {
                mContext.startActivity(Intent(mContext, CallActivity::class.java))
//                val cmd = "adb root; adb wait-for-device; adb shell ${mContext.packageName}/com.rhizo.libcontrol.ui.CallActivity"
//                executeAdb(cmd)
//                executeAdb("am start -n ${mContext.packageName}/com.rhizo.libcontrol.ui.CallActivity")
            }

            TcpConstants.LocalCommand.CODE_GET_CURRENT_FLOOR->{
                RobotUtil.getCurrentFloor()?.let {
                    MessageUtil.sendCurrentFloor(mRobotSerialNumber, it)
                }
            }

        }
    }

    /**
     * 执行adb命令
     */
    private fun executeAdb(command: String) {
        val sb = StringBuilder()
        try {
            // 执行adb devices命令
            val process = Runtime.getRuntime().exec("sh")
            val outputStream = DataOutputStream(process.outputStream)
            outputStream.writeBytes(command + "\n")
            outputStream.flush()
            outputStream.writeBytes("exit\n")
            outputStream.flush()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            // 等待命令执行完成
            process.waitFor()
            // 关闭流
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
            CLog.d(TAG, "executeAdb->${e.message}")
        }
    }

    override fun onBeWithMeStatusChanged(status: String) {
        val beWithMeStatus = BeWithMeStatus(status)
        MessageUtil.sendBeWithMeStatus(mRobotSerialNumber, beWithMeStatus, mBluetoothDevice)
    }

    override fun onBatteryStatusChanged(batteryData: BatteryData?) {
        sendBatteryData()
    }

    override fun onGoToLocationStatusChanged(
        location: String,
        status: String,
        descriptionId: Int,
        description: String
    ) {
        //{"description":"未知","descriptionId":0,"location":"a","status":"start"}
        Log.d(
            TAG,
            "onGoToLocationStatusChanged location:$location status:$status descriptionId:$descriptionId description:$description"
        )
        if (status == GotoLocationStatus.START || status == GotoLocationStatus.COMPLETE) {
            val bean = GotoLocationStatus(location, status, descriptionId, description)
            MessageUtil.sendGotoLocationStatus(mRobotSerialNumber, bean, mBluetoothDevice)
        }
    }

    override fun onCurrentPositionChanged(position: Position) {
        mPositionBean = PositionBean(position.x, position.y, position.yaw, position.tiltAngle)
        MessageUtil.sendCurrentPosition(mRobotSerialNumber, mPositionBean!!, mBluetoothDevice)
    }

}
package com.rhizo.controllibrary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rhizo.libcontrol.ControlPlugin

class MainActivity : AppCompatActivity() {
    private var mControlPlugin: ControlPlugin? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //初始化
        mControlPlugin = ControlPlugin(this)
        mControlPlugin?.init("1.0")
    }

    override fun onDestroy() {
        super.onDestroy()
        mControlPlugin?.release()
    }
}

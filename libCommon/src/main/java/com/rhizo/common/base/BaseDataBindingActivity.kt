package com.rhizo.common.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


abstract open class BaseDataBindingActivity<VB : ViewDataBinding, VM : ViewModel>(@LayoutRes private val resId: Int) :
    BaseActivity() {
    protected lateinit var mBinding: VB
    protected lateinit var mViewModel: VM
    protected var mPageNum = 1

    abstract fun viewModelClass(): Class<VM>

    protected open fun initView(savedInstanceState: Bundle?) {}
    protected open fun initView() {}
    protected open fun initData() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        /*val decorView = window.decorView
        val flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = flags*/

        mBinding = DataBindingUtil.setContentView(this, resId)
        mViewModel = ViewModelProvider(this)[viewModelClass()]

        initView(savedInstanceState)
        initView()
        initData()
    }

}
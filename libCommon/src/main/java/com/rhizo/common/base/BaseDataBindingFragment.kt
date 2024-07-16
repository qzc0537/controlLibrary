package com.rhizo.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

abstract class BaseDataBindingFragment<VB : ViewDataBinding, VM : ViewModel>(@LayoutRes private val resId: Int) :
    BaseFragment() {
    protected lateinit var mBinding: VB
    protected lateinit var mViewModel: VM
    protected var mInitialize = false
    protected var mPageNum = 1

    abstract fun viewModelClass(): Class<VM>

    protected open fun initView() {}
    protected open fun initData() {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, resId, container, false)
        mViewModel = ViewModelProvider(this)[viewModelClass()]
        mInitialize = true
        initView()
        initData()
        return mBinding.root
    }

}
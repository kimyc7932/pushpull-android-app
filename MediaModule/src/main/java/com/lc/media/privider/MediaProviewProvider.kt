package com.lc.media.privider

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.alibaba.android.arouter.facade.annotation.Route
import com.lc.media.DeviceOnlineMediaPlayActivityNew
import com.lechange.common.login.LoginManager
import com.mm.android.mobilecommon.pps.cont.MethodConst
import com.mm.android.mobilecommon.route.IPreviewProvider
import com.mm.android.mobilecommon.route.RoutePathManager

@Route(path = RoutePathManager.ActivityPath.MEDIA_PREVIEW_PROVIDER)
class MediaProviewProvider : IPreviewProvider {

    private var context: Context? = null

    override fun gotoPrevew(deviceDetail: String) {
        val bundle = Bundle()
        bundle.putString(MethodConst.ParamConst.deviceDetail, deviceDetail)
        val intent = Intent(context, DeviceOnlineMediaPlayActivityNew::class.java)
        intent.putExtras(bundle)
        if (context !is Activity) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context?.startActivity(intent)
    }

    override fun addDevices(params: String) {
        val ret = LoginManager.getInstance().addDevices(params)
        Log.e("addDevices", "ret:$ret")
    }

    override fun init(context: Context?) {
        this.context = context
    }
}
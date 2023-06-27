package com.opensdk.devicedetail.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mm.android.mobilecommon.entity.device.DeviceDetailListData
import com.mm.android.mobilecommon.pps.cont.CAPABILITY
import com.mm.android.mobilecommon.pps.cont.MethodConst
import com.mm.android.mobilecommon.pps.model.CameraStatusVO
import com.mm.android.mobilecommon.pps.service.DeviceCameraStatusService
import com.mm.android.mobilecommon.pps.service.IDeviceCameraStatus
import com.mm.android.mobilecommon.pps.utils.DeviceAbilityHelper
import com.mm.android.mobilecommon.pps.utils.Log
import com.mm.android.mobilecommon.utils.DialogUtils
import com.mm.android.mobilecommon.utils.LogUtil
import com.mm.android.mobilecommon.utils.PreferencesHelper
import com.opensdk.devicedetail.R
import com.opensdk.devicedetail.callback.IGetDeviceInfoCallBack
import com.opensdk.devicedetail.dialog.CommonDialog
import com.opensdk.devicedetail.entity.DeviceAlarmStatusData
import com.opensdk.devicedetail.entity.DeviceChannelInfoData
import com.opensdk.devicedetail.manager.DetailInstanceManager
import com.opensdk.devicedetail.tools.GsonUtils

class DeviceDetailDeploymentFragment : Fragment(), IGetDeviceInfoCallBack.IDeviceChannelInfoCallBack,
    View.OnClickListener, IGetDeviceInfoCallBack.IDeviceAlarmStatusCallBack {
    private var deviceListBean: DeviceDetailListData.ResponseData.DeviceListBean? = null
    private var deviceDetailActivity: DeviceDetailActivity? = null
    private var ivMoveCheck: ImageView? = null
    private var alarmStatus = 0
    private var ivMotionCheck: ImageView? = null
    private var ivAbsoundCheck: ImageView? = null
    private var ivRecordCheck: ImageView? = null
    private var rlSmartMotion: RelativeLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deviceDetailActivity = activity as DeviceDetailActivity?
        deviceDetailActivity!!.llOperate.visibility = View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_device_detail_deployment_new, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        deviceDetailActivity = activity as DeviceDetailActivity?
//        deviceDetailActivity!!.tvTitle.text = resources.getString(R.string.lc_demo_device_deployment_title)
        deviceDetailActivity!!.tvTitle.text = ""
        if (arguments == null) {
            return
        }
        val deviceListStr = requireArguments().getString(MethodConst.ParamConst.deviceDetail)
        if (TextUtils.isEmpty(deviceListStr)) {
            return
        }
        deviceListBean = GsonUtils.fromJson(deviceListStr, DeviceDetailListData.ResponseData.DeviceListBean::class.java)
        if (deviceListBean == null) {
            return
        }
        rlSmartMotion = view.findViewById<RelativeLayout>(R.id.rl_smart_motion)
        ivMotionCheck = view.findViewById<ImageView>(R.id.iv_motion_check)
        ivAbsoundCheck = view.findViewById<ImageView>(R.id.iv_absound_check)

        initData()
    }

    private fun initView(view: View) {
        ivMoveCheck = view.findViewById(R.id.iv_move_check)
        ivMoveCheck?.setOnClickListener(this)

        ivRecordCheck = view.findViewById<ImageView>(R.id.iv_record_check)
        ivRecordCheck?.setOnClickListener(this)

        // 布防时间段设置
//        view.findViewById<View>(R.id.rl_deployment_setting_time).setOnClickListener(this)
    }

    private fun initData() {
        //获取动检状态/设备详情
        DialogUtils.show(activity)

        val deviceId = deviceListBean!!.deviceId
        val channelId: String =
            deviceListBean!!.channelList.get(deviceListBean!!.checkedChannel).channelId

        val deviceDetailService = DetailInstanceManager.newInstance().deviceDetailService
        val deviceChannelInfoData = DeviceChannelInfoData()
        deviceChannelInfoData.data.deviceId = deviceListBean!!.deviceId
        deviceChannelInfoData.data.channelId = deviceListBean!!.channelList[deviceListBean!!.checkedChannel].channelId
        deviceDetailService.bindDeviceChannelInfo(deviceChannelInfoData, this)

        /** 장치의 움직임감지 상태값 조회 **/
        /** 장치의 움직임감지 상태값 조회  */
        val alarmCameraStatusVO: CameraStatusVO =
            DeviceAbilityHelper.createCameraStatusVO(deviceId, channelId)
        alarmCameraStatusVO.setEnableType(CAPABILITY.MOTION_DETECT)
        alarmCameraStatusVO.setTargetIV(ivMoveCheck)
        getDeviceCameraStatus(alarmCameraStatusVO)

        /** 장치의 녹화저장 상태값 조회 **/
        /** 장치의 녹화저장 상태값 조회  */
        val recordCameraStatusVO: CameraStatusVO =
            DeviceAbilityHelper.createCameraStatusVO(deviceId, channelId)
        recordCameraStatusVO.setEnableType(CAPABILITY.LOCAL_RECORD)
        recordCameraStatusVO.setTargetIV(ivRecordCheck)
        getDeviceCameraStatus(recordCameraStatusVO)

        /** 이상음감지 조회 **/
        /** 이상음감지 조회  */
        val abSoundCameraStatusVO: CameraStatusVO =
            DeviceAbilityHelper.createCameraStatusVO(deviceId, channelId)
        abSoundCameraStatusVO.setEnableType(CAPABILITY.ABNORMAL_SOUND)
        abSoundCameraStatusVO.setTargetIV(ivAbsoundCheck)
        getDeviceCameraStatus(abSoundCameraStatusVO)


        ivRecordCheck!!.setOnClickListener {
            val recordStatusVO = DeviceAbilityHelper.createCameraStatusVO(deviceId, channelId)
            setLocalRecoard(recordStatusVO)
        }

        ivAbsoundCheck!!.setOnClickListener {
            val recordStatusVO = DeviceAbilityHelper.createCameraStatusVO(deviceId, channelId)
            setAbnormalSound(recordStatusVO)
        }

        /** 스마트트랙 기능이 있으면 ... **/
        /** 스마트트랙 기능이 있으면 ...  */
        if (deviceListBean!!.ability.toLowerCase().contains(CAPABILITY.SMART_TRACK.toLowerCase())) {
            rlSmartMotion!!.visibility = View.VISIBLE
            /** 장치의 스마트트랙 상태값 조회  */
            val cameraStatusVO = DeviceAbilityHelper.createCameraStatusVO(deviceId, channelId)
            cameraStatusVO.enableType = CAPABILITY.SMART_TRACK
            cameraStatusVO.targetIV = ivMotionCheck
            getDeviceCameraStatus(cameraStatusVO)
            ivMotionCheck!!.setOnClickListener {
                val smartTrackCameraStatusVO =
                    DeviceAbilityHelper.createCameraStatusVO(deviceId, channelId)
                setSmartTrack(smartTrackCameraStatusVO)
            }
        }
    }


    fun setDeviceStatus(cameraStatusVO: CameraStatusVO?, callback: IDeviceCameraStatus.CommonCallback?) {
        DeviceCameraStatusService.getInstance().setDeviceCameraStatus(cameraStatusVO, callback)
    }

    fun setAbnormalSound(cameraStatusVO: CameraStatusVO) {
        DialogUtils.show(activity)
        val key =
            cameraStatusVO.channelId + "_" + cameraStatusVO.deviceId + "_" + CAPABILITY.ABNORMAL_SOUND
        var value = PreferencesHelper.getInstance(context).getBoolean(key, false)
        value = !value
        cameraStatusVO.enableType = CAPABILITY.ABNORMAL_SOUND
        cameraStatusVO.isEnableValue = value
        cameraStatusVO.targetIV = ivAbsoundCheck
        cameraStatusVO.saveKey = key
        val finalValue = value
        setDeviceStatus(cameraStatusVO, object : IDeviceCameraStatus.CommonCallback {
            override fun onSuccess(response: String?) {
                val key = cameraStatusVO.saveKey
                PreferencesHelper.getInstance(context)[key] = finalValue
                drawImageView(cameraStatusVO.targetIV, finalValue)
                DialogUtils.dismiss()
            }

            override fun onError(throwable: Throwable?) {
                DialogUtils.dismiss()
            }
        })
    }

    fun setLocalRecoard(cameraStatusVO: CameraStatusVO) {
        DialogUtils.show(activity)
        val key =
            cameraStatusVO.channelId + "_" + cameraStatusVO.deviceId + "_" + CAPABILITY.LOCAL_RECORD
        var recordValue = PreferencesHelper.getInstance(context).getBoolean(key, false)
        recordValue = !recordValue
        cameraStatusVO.enableType = CAPABILITY.LOCAL_RECORD
        cameraStatusVO.isEnableValue = recordValue
        cameraStatusVO.targetIV = ivRecordCheck
        cameraStatusVO.saveKey = key
        val finalRecordValue = recordValue
        setDeviceStatus(cameraStatusVO, object : IDeviceCameraStatus.CommonCallback {
            override fun onSuccess(response: String?) {
                val key = cameraStatusVO.saveKey
                PreferencesHelper.getInstance(context)[key] = finalRecordValue
                drawImageView(cameraStatusVO.targetIV, finalRecordValue)
                DialogUtils.dismiss()
            }

            override fun onError(throwable: Throwable?) {
                DialogUtils.dismiss()
            }
        })
    }

    fun setSmartTrack(cameraStatusVO: CameraStatusVO) {
        DialogUtils.show(activity)
        val key =
            cameraStatusVO.channelId + "_" + cameraStatusVO.deviceId + "_" + CAPABILITY.SMART_TRACK
        var motionCheckValue = PreferencesHelper.getInstance(context).getBoolean(key, false)
        motionCheckValue = !motionCheckValue
        cameraStatusVO.enableType = CAPABILITY.SMART_TRACK
        cameraStatusVO.isEnableValue = motionCheckValue
        cameraStatusVO.targetIV = ivMotionCheck
        cameraStatusVO.saveKey = key
        val finalMotionCheckValue = motionCheckValue
        setDeviceStatus(cameraStatusVO, object : IDeviceCameraStatus.CommonCallback {
            override fun onSuccess(response: String?) {
                val key = cameraStatusVO.saveKey
                PreferencesHelper.getInstance(context)[key] = finalMotionCheckValue
                drawImageView(cameraStatusVO.targetIV, finalMotionCheckValue)
                DialogUtils.dismiss()
                /** 스마트트랙은 알림설정이 켜져 있어야 하기 때문에 알림설정 켜기  */
                if (finalMotionCheckValue) {
                    val deviceDetailService = DetailInstanceManager.newInstance().deviceDetailService
                    deviceDetailService.setDeviceCameraStatus(
                        deviceListBean!!.deviceId,
                        deviceListBean!!.channelList.get(deviceListBean!!.checkedChannel).channelId,
                        "motionDetect",
                        true
                    )
                    //                    deviceRecordService.setDeviceCameraStatus(deviceListBean.deviceId,
//                            deviceListBean.channels.get(deviceListBean.checkedChannel).channelId,"localRecord", true);
                    drawImageView(ivMoveCheck, true) // 읽어온 기능의 기본값 셋팅
                }
            }

            override fun onError(throwable: Throwable?) {
                DialogUtils.dismiss()
            }
        })
    }

    private fun getDeviceCameraStatus(cameraStatusVO: CameraStatusVO) {
        DialogUtils.show(activity)
        DeviceCameraStatusService.getInstance()
            .getDeviceCameraStatus(cameraStatusVO, object : IDeviceCameraStatus.CommonCallback {
                override fun onSuccess(response: String?) {
                    if (response == null || response == "") {
                        DialogUtils.dismiss()
                        return
                    }
                    val resultJson = Gson().fromJson(response, JsonObject::class.java)
                    val enableType = resultJson["enableType"].asString
                    val status = resultJson["status"].asString
                    try {
                        Log.d(
                            TAG,
                            "getDeviceCameraStatus.response.enableType:::$enableType ==> $status"
                        )
                    } catch (e: Exception) {
                    }
                    if (enableType != null && enableType != "") {
                        Log.d(
                            TAG,
                            "getDeviceCameraStatus.response.enableType:::$enableType ==> $status"
                        )
                        var b = false
                        if (status.equals("on", ignoreCase = true)) b = true
                        val key =
                            cameraStatusVO.channelId + "_" + cameraStatusVO.deviceId + "_" + enableType
                        PreferencesHelper.getInstance(context).set(key, b) // 장비에서 읽어온 값으로 변수 값 셋팅.
                        drawImageView(cameraStatusVO.targetIV, b) // 읽어온 기능의 기본값 셋팅
                    }
                    DialogUtils.dismiss()
                }

                override fun onError(throwable: Throwable?) {
                    DialogUtils.dismiss()
                }
            })
    }

    private fun drawImageView(imageView: ImageView?, b: Boolean) {
        if (imageView == null) return
        if (b) {
            imageView.setImageDrawable(resources.getDrawable(R.mipmap.lc_demo_switch_on))
        } else {
            imageView.setImageDrawable(resources.getDrawable(R.mipmap.lc_demo_switch_off))
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun deviceChannelInfo(result: DeviceChannelInfoData.Response) {
        if (!isAdded) {
            return
        }
        DialogUtils.dismiss()
        alarmStatus = result.data.alarmStatus
        if (result.data.alarmStatus == 1) {
            ivMoveCheck!!.setImageDrawable(resources.getDrawable(R.drawable.lc_demo_switch_on))
        } else {
            ivMoveCheck!!.setImageDrawable(resources.getDrawable(R.drawable.lc_demo_switch_off))
        }
    }

    override fun deviceAlarmStatus(result: Boolean) {
        if (!isAdded) {
            return
        }
        DialogUtils.dismiss()
        alarmStatus = if (alarmStatus == 1) 0 else 1
        if (alarmStatus == 1) {
            ivMoveCheck!!.setImageDrawable(resources.getDrawable(R.drawable.lc_demo_switch_on))
        } else {
            ivMoveCheck!!.setImageDrawable(resources.getDrawable(R.drawable.lc_demo_switch_off))
        }
    }

    override fun onError(throwable: Throwable) {
        if (!isAdded) {
            return
        }
        DialogUtils.dismiss()
        LogUtil.errorLog(TAG, "error", throwable)
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.iv_move_check) {
            if (alarmStatus == 0) {
                val dialog = CommonDialog.Builder(activity).setTitle(R.string.device_manage_notice)
                        .setMessage(R.string.device_manager_default_defence_period_tip)
                        .setCheckBoxEnable(true) { dialog, which, isChecked -> changeNeverRemindStatus(isChecked) }
                        .setCancelButton(R.string.deploy_cancel, null)
                        .setConfirmButton(R.string.common_i_know) { dialog, which, isChecked -> changeAlarmStatus() }
                        .create()
                dialog.show(childFragmentManager, null)
            } else {
                val preferences = activity?.getSharedPreferences("SHARED_InitInfo", 0)
                val neverRemind = preferences?.getBoolean("NeverRemind" + deviceListBean?.deviceId, false)
                if (neverRemind == false) {
                    val dialog = CommonDialog.Builder(activity).setTitle(R.string.device_manage_notice)
                            .setMessage(R.string.device_manager_no_alarm_video_message_tip)
                            .setCheckBoxEnable(true) { dialog, which, isChecked -> changeNeverRemindStatus(isChecked) }
                            .setCancelButton(R.string.deploy_cancel, null)
                            .setConfirmButton(R.string.common_i_know) { dialog, which, isChecked -> changeAlarmStatus() }
                            .create()
                    dialog.show(childFragmentManager, null)
                } else {
                    changeAlarmStatus()
                }
            }
        }
//        else if (id == R.id.rl_deployment_setting_time) {
//            gotoDeploymentSettingTimePage()
//        }
    }

    /**
     * change never remind status
     *
     * 切换记住提醒状态
     */
    private fun changeNeverRemindStatus(checked: Boolean) {
        val preferences = activity?.getSharedPreferences("SHARED_InitInfo", 0)
        preferences?.edit()?.putBoolean("NeverRemind" + deviceListBean?.deviceId, checked)?.apply()
    }

    /**
     * change alarm status
     *
     * 切换动检状态
     */
    private fun changeAlarmStatus(){
        //设置动检状态
        DialogUtils.show(activity)
        val deviceDetailService = DetailInstanceManager.newInstance().deviceDetailService
        val deviceAlarmStatusData = DeviceAlarmStatusData()
        deviceAlarmStatusData.data.deviceId = deviceListBean!!.deviceId
        deviceAlarmStatusData.data.channelId =
                deviceListBean!!.channelList[deviceListBean!!.checkedChannel].channelId
        //现在是开启状态，则关闭，反之
        deviceAlarmStatusData.data.enable = if (alarmStatus == 1) false else true
        deviceDetailService.modifyDeviceAlarmStatus(deviceAlarmStatusData, this)
    }


    /**
     * goto deployment setting time page
     *
     * 跳转布防时间设置界面
     */
    private fun gotoDeploymentSettingTimePage() {
        if (activity == null) {
            return
        }
        val i = Intent(activity, DeploymentSettingTimeActivity::class.java)
        i.putExtra(MethodConst.ParamConst.deviceDetail, requireArguments().getString(MethodConst.ParamConst.deviceDetail))
        startActivity(i)
    }

    companion object {
        private val TAG = DeviceDetailDeploymentFragment::class.java.simpleName
        @JvmStatic
        fun newInstance(): DeviceDetailDeploymentFragment {
            return DeviceDetailDeploymentFragment()
        }
    }
}
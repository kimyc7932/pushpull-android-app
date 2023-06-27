package com.mm.android.mobilecommon.pps.cont;

public class MethodConst {

    public static String QUERY_DEVICEDETAIL_PAGE = "listDeviceDetailsByPage";
    public static String METHOD_FRAME_REVERSE_STATUS = "frameReverseStatus";
    public static String METHOD_MODIFY_FRAME_REVERSE_STATUS = "modifyFrameReverseStatus";
    public static String METHOD_SET_DEVICE_CAMERA_STATUS = "setDeviceCameraStatus";
    public static String METHOD_GET_DEVICE_CAMERA_STATUS = "getDeviceCameraStatus";
    public static String METHOD_GET_DEVICE_TIME = "getDeviceTime";
    public static String METHOD_CALIBRATION_DEVICE_TIME = "calibrationDeviceTime";
    public static String METHOD_DEVICE_SD_CARD_STATUS = "deviceSdcardStatus";
    public static String METHOD_RECOVER_SD_CARD = "recoverSDCard";
    public static String METHOD_DEVICE_STORAGE = "deviceStorage";
    public static String METHOD_TIMEZONE_CONFIG_BY_DAY = "timeZoneConfigByDay";
    public static String METHOD_TIMEZONE_QUERY_BY_DAY = "timeZoneQueryByDay";
    public static String METHOD_TIMEZONE_CONFIG_BY_WEEK = "timeZoneConfigByWeek";
    public static String METHOD_TIMEZONE_QUERY_BY_WEEK = "timeZoneQueryByWeek";
    public static String METHOD_GET_ALARM_MESSAGE = "getAlarmMessage";

    public static String METHOD_DEVICE_BASE_LIST = "deviceBaseList";
    public static String METHOD_DEVICE_OPEN_LIST = "deviceOpenList";
    public static String METHOD_DEVICE_OPEN_DETAIL_LIST = "deviceOpenDetailList";
    public static String METHOD_DEVICE_BASE_DETAIL_LIST = "deviceBaseDetailList";
    public static String METHOD_DEVICE_UN_BIND = "unBindDevice";
    public static String METHOD_DEVICE_VERSION_LIST = "deviceVersionList";
    public static String METHOD_DEVICE_MODIFY_NAME = "modifyDeviceName";
    public static String METHOD_DEVICE_CHANNEL_INFO = "bindDeviceChannelInfo";
    public static String METHOD_DEVICE_MODIFY_ALARM_STATUS = "modifyDeviceAlarmStatus";
    public static String METHOD_DEVICE_UPDATE = "upgradeDevice";
    public static String METHOD_GET_CLOUND_RECORDS = "getCloudRecords";
    public static String METHOD_QUERY_LOCAL_RECORD = "queryLocalRecords";
    public static String METHOD_CONTROL_MOVE_PTZ = "controlMovePTZ";
    public static String METHOD_QUERY_CLOUND_RECORDS = "queryCloudRecords";
    public static String METHOD_DELETE_CLOUND_RECORDS = "deleteCloudRecords";
    public static String LIST_SUB_ACCOUNT = "listSubAccount";
    public static String LIST_SUB_ACCOUNT_DEVICE = "listSubAccountDevice";
    public static String SUB_ACCOUNT_DEVICE_LIST = "subAccountDeviceList";
    public static String DELETE_DEVICE_PERMISSION = "deleteDevicePermission";
    public static String SD_STATUE_QUERY = "deviceSdcardStatus";
    public static String GET_DEVICE_CLOUD = "getDeviceCloud";
    public static String METHOD_SET_DEVICE_STATUS = "setDeviceCameraStatus";

    public static String METHOD_LIST_DEVICE_DETAIL_BATCH = "listDeviceDetailsByIds";

    public static String GET_STORE_INFO = "deviceStorage";
    public static String RECOVER_SDCARD = "recoverSDCard";
    public static String DEVICE_SD_CARD_STATUS = "deviceSdcardStatus";
    public static String SET_CLOUD_STORAGE = "setAllStorageStrategy";

    public interface ParamConst {
        String filePath = "filePath";
        public String mCameraDatas = "mCameraDatas";
        public String deviceDetail = "deviceDetail";
        public String recordType = "recordType";
        public String recordData = "recordsData";
        public int recordTypeLocal = 2;
        public int recordTypeCloud = 1;
        public String fromList = "list";
    }

}

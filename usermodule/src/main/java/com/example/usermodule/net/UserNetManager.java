package com.example.usermodule.net;

import android.os.Handler;
import android.os.Message;

import com.example.usermodule.api.IAccountUserDataCallBack;
import com.example.usermodule.api.PpsUserApiManager;
import com.example.usermodule.api.UserApiManager;
import com.mm.android.mobilecommon.AppConsume.BusinessException;
import com.mm.android.mobilecommon.AppConsume.BusinessRunnable;
import com.mm.android.mobilecommon.base.LCBusinessHandler;
import com.mm.android.mobilecommon.businesstip.BusinessErrorTip;
import com.mm.android.mobilecommon.businesstip.HandleMessageCode;
import com.mm.android.mobilecommon.pps.manager.PpsDeviceApiManager;

import java.util.Map;

public class UserNetManager {

    public static class Holder{
        private final static UserNetManager mInstance = new UserNetManager();
    }

    public static UserNetManager getInstance(){
        return Holder.mInstance;
    }

    /** updateUserInfo **/
    public void updateUserInfo(final Map<String, Object> paramMap, final IAccountUserDataCallBack callBack){
        final LCBusinessHandler handler = new LCBusinessHandler() {
            @Override
            public void handleBusiness(Message msg) {
                if (callBack == null) {
                    return;
                }
                if (msg.what == HandleMessageCode.HMC_SUCCESS) {
                    //成功
                    callBack.onSuccess((String) msg.obj);
                } else {
                    //失败
                    callBack.onError(BusinessErrorTip.throwError(msg));
                }
            }
        };
        new BusinessRunnable(handler) {
            @Override
            public void doBusiness() throws BusinessException {
                try {
                    updateUserInfo(handler, paramMap);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void updateUserInfo(Handler handler, Map<String, Object> paramMap)throws BusinessException{
        String result = PpsUserApiManager.updateUserInfo(paramMap);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }


    /** version check **/
    public void getNewestVersion(final String uid, final IDeviceOperationCallback callBack){
        final LCBusinessHandler handler = new LCBusinessHandler() {
            @Override
            public void handleBusiness(Message msg) {
                if (callBack == null) {
                    return;
                }
                if (msg.what == HandleMessageCode.HMC_SUCCESS) {
                    //成功
                    callBack.onSuccess((String) msg.obj);
                } else {
                    //失败
                    callBack.onError(BusinessErrorTip.throwError(msg));
                }
            }
        };
        new BusinessRunnable(handler) {
            @Override
            public void doBusiness() throws BusinessException {
                try {
                    getNewestVersion(handler, uid);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void getNewestVersion(Handler handler,String uid)throws BusinessException{
        String result = PpsDeviceApiManager.getNewestVersion(uid);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }

    public void getOpenIdByAccount(final String userName, final IUserDataCallBack callBack){
        final LCBusinessHandler handler = new LCBusinessHandler() {
            @Override
            public void handleBusiness(Message msg) {
                if (callBack == null) {
                    return;
                }
                if (msg.what == HandleMessageCode.HMC_SUCCESS) {
                    //成功
                    callBack.onCallBackOpenId((String) msg.obj);
                } else {
                    //失败
                    callBack.onError(BusinessErrorTip.throwError(msg));
                }
            }
        };
        new BusinessRunnable(handler) {
            @Override
            public void doBusiness() throws BusinessException {
                try {
                    getOpenIdByAccountMethod(handler, userName);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void getOpenIdByAccountMethod(Handler handler,String userName)throws BusinessException{
        String result = UserApiManager.getUserOpenIdByAccout(userName);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }

    public void subAccountToken(final String openId, final IUserDataCallBack callBack){
        final LCBusinessHandler handler = new LCBusinessHandler() {
            @Override
            public void handleBusiness(Message msg) {
                if (callBack == null) {
                    return;
                }
                if (msg.what == HandleMessageCode.HMC_SUCCESS) {
                    //成功
                    callBack.onCallBackOpenId((String) msg.obj);
                } else {
                    //失败
                    callBack.onError(BusinessErrorTip.throwError(msg));
                }
            }
        };
        new BusinessRunnable(handler) {
            @Override
            public void doBusiness() throws BusinessException {
                try {
                    subAccountTokenDeal(handler, openId);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void subAccountTokenDeal(Handler handler,String openId)throws BusinessException{
        String result = UserApiManager.getSubAccountToken(openId);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }


    public void createAccountToken(final String userName, final IUserDataCallBack callBack){
        final LCBusinessHandler handler = new LCBusinessHandler() {
            @Override
            public void handleBusiness(Message msg) {
                if (callBack == null) {
                    return;
                }
                if (msg.what == HandleMessageCode.HMC_SUCCESS) {
                    //成功
                    callBack.onCallBackOpenId((String) msg.obj);
                } else {
                    //失败
                    callBack.onError(BusinessErrorTip.throwError(msg));
                }
            }
        };
        new BusinessRunnable(handler) {
            @Override
            public void doBusiness() throws BusinessException {
                try {
                    createSubAccount(handler, userName);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void createSubAccount(Handler handler,String suerName) throws  BusinessException{
        String result =UserApiManager.createSubAccountToken(suerName);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }

    public void deleteAccountToken(final String openId, final IAccountUserDataCallBack callBack){
        final LCBusinessHandler handler = new LCBusinessHandler() {
            @Override
            public void handleBusiness(Message msg) {
                if (callBack == null) {
                    return;
                }

                if (msg.what == HandleMessageCode.HMC_SUCCESS) {
                    //成功
                    callBack.onSuccess((String) msg.obj);
                } else {
                    //失败
                    callBack.onError(BusinessErrorTip.throwError(msg));
                }
            }
        };

        new BusinessRunnable(handler) {
            @Override
            public void doBusiness() throws BusinessException {
                try {
                    deleteSubAccount(handler, openId);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void deleteSubAccount(Handler handler,String openId) throws  BusinessException{
        String result =UserApiManager.deleteSubAccountToken(openId);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }

}

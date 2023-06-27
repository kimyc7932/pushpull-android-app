package com.opensdk.devicedetail.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.mm.android.mobilecommon.entity.device.DeviceDetailListData;
import com.mm.android.mobilecommon.pps.ACONST;
import com.mm.android.mobilecommon.pps.cont.MethodConst;
import com.mm.android.mobilecommon.pps.manager.TimeZoneManager;
import com.mm.android.mobilecommon.pps.model.TimeZoneInfo;
import com.mm.android.mobilecommon.pps.service.DeviceTimezoneService;
import com.mm.android.mobilecommon.pps.service.IDeviceOperationCallback;
import com.mm.android.mobilecommon.pps.utils.Log;
import com.mm.android.mobilecommon.utils.PreferencesHelper;
import com.opensdk.devicedetail.R;
import com.opensdk.devicedetail.callback.IGetDeviceInfoCallBack;

import java.util.ArrayList;
import java.util.List;

public class DeviceDetailTimeZoneFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = DeviceDetailTimeZoneFragment.class.getSimpleName();
    private Bundle arguments;
    private DeviceDetailListData.ResponseData.DeviceListBean deviceListBean;
    private IGetDeviceInfoCallBack.ITimeZoneInfo timeZoneListener;
    private DeviceDetailActivity deviceDetailActivity;
    private SearchView svSearch;
    private String fromWhere;
    private ListView lvTimeZone;

    public static DeviceDetailTimeZoneFragment newInstance() {
        DeviceDetailTimeZoneFragment fragment = new DeviceDetailTimeZoneFragment();
        return fragment;
    }

    public void setTimeZoneListener(IGetDeviceInfoCallBack.ITimeZoneInfo timeZoneListener) {
        this.timeZoneListener = timeZoneListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arguments = getArguments();
        if (arguments == null) {
            return;
        }
        deviceListBean = (DeviceDetailListData.ResponseData.DeviceListBean) arguments.getSerializable(MethodConst.ParamConst.deviceDetail);
        //不为空 列表页跳转
        fromWhere = arguments.getString(MethodConst.ParamConst.fromList);
        if (deviceListBean == null) {
            return;
        }
        deviceDetailActivity = (DeviceDetailActivity) getActivity();
        deviceDetailActivity.llOperate.setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_timezone, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DeviceDetailActivity deviceDetailActivity = (DeviceDetailActivity) getActivity();
        deviceDetailActivity.tvTitle.setText(getResources().getString(R.string.lc_timezone_text));
        initView(view);
    }

    private void initView(View view) {
        lvTimeZone = view.findViewById(R.id.lv_timezone);
        svSearch = view.findViewById(R.id.sv_search);

        final List<TimeZoneInfo> timeZoneInfoList = TimeZoneManager.getInstance().getTimeZoneInfoList(view.getContext());
        final ArrayAdapter<TimeZoneInfo> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_single_choice, timeZoneInfoList);
        lvTimeZone.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvTimeZone.setAdapter(adapter);

        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<TimeZoneInfo> filterList = new ArrayList<>();
//                for(int i=0; i<timeZoneInfoList.size(); i++) {
//                    TimeZoneInfo timeZoneInfo = timeZoneInfoList.get(i);
//                    if(timeZoneInfo.getCity().toLowerCase().contains(newText.toLowerCase())) {
//                        filterList.add(timeZoneInfo);
//                    }
//                } // end for

                adapter.getFilter().filter(newText);

                return false;
            }
        });


        lvTimeZone.setAdapter(adapter);
        lvTimeZone.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                System.out.println("onitemclick=============================== " + adapter.getItem(position).getCity());

                final TimeZoneInfo timeZoneInfo = adapter.getItem(position);

                DeviceTimezoneService.getInstance().setTimezone(deviceListBean.deviceId, timeZoneInfo, new IDeviceOperationCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Log.d(TAG, "setTimezone.onSuccess -----------------------------------");
                        Log.d(TAG, "setTimezone : " + response);
                        Log.d(TAG, "setTimezone.onSuccess -----------------------------------");

                        if(response == null || response.equals("")) return;
                        if (timeZoneListener != null) {
                            PreferencesHelper.getInstance(getContext()).set(deviceListBean.deviceId+"_"+ ACONST.TIME_ZONE, timeZoneInfo.getUtc()+" "+timeZoneInfo.getCity());
                            PreferencesHelper.getInstance(getContext()).set(deviceListBean.deviceId+"_"+ACONST.UTC_TIME, timeZoneInfo.getUtc());

                            timeZoneListener.timeZoneInfo(response);
                        }
                        Toast.makeText(getContext(), R.string.lc_timezone_setup_finish, Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStack();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.d(TAG, "setTimezone.onError -----------------------------------");
                        Log.d(TAG, "setTimezone : "+ throwable.getMessage());
                        Log.d(TAG, "setTimezone.onError -----------------------------------");
                    }
                });
            };
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        if (getActivity() == null || getActivity().getSupportFragmentManager() == null) {
            return;
        }
        DeviceDetailModifyNameFragment fragment = DeviceDetailModifyNameFragment.newInstance();
        fragment.setArguments(arguments);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.hide(this).add(R.id.fr_content, fragment).addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }

}

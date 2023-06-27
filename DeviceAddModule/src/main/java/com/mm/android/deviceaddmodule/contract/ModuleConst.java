package com.mm.android.deviceaddmodule.contract;

import java.util.HashMap;
import java.util.Map;

public class ModuleConst {
    public static String LANG_TYPE;

    public static Map<String, String> changeIntroductionMap = new HashMap<String, String>();
    static {
        changeIntroductionMap.put("SoftAPModeGuidingStepOneIntroduction_KR", "장비의 표시등이 파란색으로 깜박일 때 [다음] 버튼을 누르면 장치가 AP 구성 모드로 들어갑니다. 시간이 오래 소요될경우 reset 버튼을 10초이상 누른 후 다시 시도해 주세요.");
        changeIntroductionMap.put("SoftAPModeGuidingStepOneIntroduction_EN", "Press [Next] button when the device indicator light flashing blue, then the device will enter AP configuration mode. If it takes a long time, try again after pressing the reset button for more than 10 seconds.");
    }

}

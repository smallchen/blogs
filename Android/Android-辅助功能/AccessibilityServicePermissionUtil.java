import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

/**
 * open permission automated
 */
public class PermissionUtil {

    private static final String TAG = "PermissionUtil";
    public static final String ACCESSIBILITY_REFERENCE = "com.jokin.demo/com.jokin.demo.service.MarkAccessibility";
    public static final String ACCESSIBILITY_ENABLE = "1";
    public static final String ACCESSIBILITY_DISENABLE = "0";


    /**
     * confirm accessibility service is opened
     *
     * @param context
     * @param accessibilityReference
     * @return
     */
    public static boolean isMarkAccessibilitySettingOn(Context context, String accessibilityReference) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            Log.e(TAG, "***ACCESSIBILITY IS ENABLED***");
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

            if (settingValue == null) {
                return false;
            }

            TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
            splitter.setString(settingValue);
            while (splitter.hasNext()) {
                String accessibilityService = splitter.next();
                if (accessibilityService.equalsIgnoreCase(accessibilityReference)) {
                    NLog.e(TAG, "We've found the correct setting - accessibility is switched on!");
                    return true;
                }
            }
        } else {
            Log.e(TAG, "***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }


    /**
     * enable Accessibility By Value
     *
     * @param context
     * @param accessibilityReference
     * @param value
     */
    public static void enableAccessibilityByValue(Context context, String accessibilityReference, String value) {
        Settings.Secure.putString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, accessibilityReference);
        Settings.Secure.putString(context.getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED, value);
    }


    /**
     * check AccessibilityService and open
     * @param context
     */
    public static void checkAccessibilityServiceAndOpen(Context context) {
        if (!isMarkAccessibilitySettingOn(context.getApplicationContext()
                , ACCESSIBILITY_REFERENCE)) {
            Log.d(TAG, "Accessibility Service 没有开启，进行开启操作");
            enableAccessibilityByValue(context.getApplicationContext()
                    , ACCESSIBILITY_REFERENCE, ACCESSIBILITY_ENABLE);
        }
    }

    /**
     *  check accessibility is working
     */
    public static boolean checkAccessibilityIsWorking(Context context) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> serviceInfos = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : serviceInfos) {
            String id = info.getId();
            if (id.contains("MarkAccessibility")) {
                return true;
            }
        }
        return false;
    }
}

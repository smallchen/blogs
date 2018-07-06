## How to change android system locale


Calling below code could change the device’s locale settings.

        IActivityManager am = ActivityManagerNative.getDefault();
        Configuration config;
        try {
            config = am.getConfiguration();
            config.locale = Locale.CHINA;
            am.updateConfiguration(config);
            //Trigger the dirty bit for the Settings Provider.  
           BackupManager.dataChanged("com.android.providers.settings");
        } catch (Exception e) {
            e.printStackTrace();
        }

However, when you input above code in the Eclipse or Motodev Studio of android, it shows error message which says could not find the class IActivityManager and ActivityManagerNative.

IActivityManager and ActivityManagerNative are two android internal classes which is not included in the android sdk build path.

Two resolve this issue, there are two ways: Using Class Reflection to access the API or Using fake class in the build path.
Using Class Reflection to access the API


        try {
            Class<?> activityManagerNative = Class.forName("android.app.ActivityManagerNative");                
            Object am=activityManagerNative.getMethod("getDefault").invoke(activityManagerNative);
            Object config=am.getClass().getMethod("getConfiguration").invoke(am);
            config.getClass().getDeclaredField("locale").set(config, Locale.US);
            config.getClass().getDeclaredField("userSetLocale").setBoolean(config, true);

            am.getClass().getMethod("updateConfiguration",android.content.res.Configuration.class).invoke(am,config);

        }catch (Exception e) {
            e.printStackTrace();
        }

Using fake class in the build path

To bypass the error in the development tool, you could create two files in the package “android.app”:

package android.app;

import android.content.res.Configuration;
import android.os.RemoteException;

public interface IActivityManager {
        public abstract Configuration getConfiguration () throws RemoteException;
        public abstract void updateConfiguration (Configuration configuration) throws RemoteException;
}

And

package android.app;

public abstract class ActivityManagerNative implements IActivityManager {

        public static IActivityManager getDefault(){
                return null;
        }   

}

Then, the application will be compiled. It runs properly when it is installed on the phone.
Posted 23rd May 2012 by Bin Liu

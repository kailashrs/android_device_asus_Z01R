/*
* Copyright (C) 2016 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.omnirom.device;

import android.app.ActivityManagerNative;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.IAudioService;
import android.media.AudioManager;
import android.media.session.MediaSessionLegacyHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.HapticFeedbackConstants;
import android.view.WindowManagerGlobal;

import com.android.internal.os.DeviceKeyHandler;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.Gravity;
import android.widget.Toast;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.aosip.aosipUtils;
import com.android.internal.statusbar.IStatusBarService;

public class KeyHandler implements DeviceKeyHandler {

    private static final String TAG = "KeyHandler";
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_SENSOR = true;

    protected static final int GESTURE_REQUEST = 1;
    private static final int GESTURE_WAKELOCK_DURATION = 1000;

    private static final int KEY_GESTURE_C = 46;
    private static final int KEY_GESTURE_E = 18;
    private static final int KEY_GESTURE_L = 38;
    private static final int KEY_GESTURE_M = 50;
    private static final int KEY_GESTURE_O = 24;
    private static final int KEY_GESTURE_S = 31;
    private static final int KEY_GESTURE_V = 47;
    private static final int KEY_GESTURE_W = 17;
    private static final int KEY_GESTURE_Z = 44;
    private static final int KEY_GESTURE_DOWN_SWIPE = 261;
    private static final int KEY_GESTURE_LEFT_SWIPE = 262;
    private static final int KEY_GESTURE_RIGHT_SWIPE = 263;
    private static final int KEY_SWIPEUP_GESTURE = 260;

    private static final int KEY_DOUBLE_TAP = 143;
    private static final int KEY_HOME = 102;
    private static final int KEY_BACK = 158;
    private static final int KEY_RECENTS = 580;

    private static final int MIN_PULSE_INTERVAL_MS = 2500;
    private static final String DOZE_INTENT = "com.android.systemui.doze.pulse";
    private static final int HANDWAVE_MAX_DELTA_MS = 1000;
    private static final int POCKET_MIN_DELTA_MS = 5000;
    private static final int FP_GESTURE_LONG_PRESS = 188;

    private static final String GOODIX_CONTROL_PATH = "/sys/devices/platform/soc/0.goodix_gf5228/proximity_state";
    
    private static final int[] sSupportedGestures = new int[]{
    };

    private static final int[] sProxiCheckedGestures = new int[]{
    };

    protected final Context mContext;
    private final PowerManager mPowerManager;
    private EventHandler mEventHandler;
    private WakeLock mGestureWakeLock;
    private Handler mHandler = new Handler();
    private SettingsObserver mSettingsObserver;
    private static boolean mButtonDisabled;
    private final NotificationManager mNoMan;
    private final AudioManager mAudioManager;
    private SensorManager mSensorManager;
    private boolean mProxyIsNear;
    private boolean mUseProxiCheck;
    private boolean mProxyWasNear;
    private long mProxySensorTimestamp;
    private boolean mUseWaveCheck;
    private Sensor mPocketSensor;
    private boolean mUsePocketCheck;
    private boolean mFPcheck;
    private boolean mDispOn;
    private boolean isFpgesture;
    private boolean isOPCameraAvail;
    private boolean mRestoreUser;

    private SensorEventListener mProximitySensor = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mProxyIsNear = event.values[0] < mPocketSensor.getMaximumRange();
            if (DEBUG_SENSOR) Log.i(TAG, "mProxyIsNear = " + mProxyIsNear + " mProxyWasNear = " + mProxyWasNear);
            if (mUseProxiCheck) {
                if (Utils.fileWritable(GOODIX_CONTROL_PATH)) {
                    Utils.writeValue(GOODIX_CONTROL_PATH, mProxyIsNear ? "1" : "0");
                }
            }
            if (mUseWaveCheck || mUsePocketCheck) {
                if (mProxyWasNear && !mProxyIsNear) {
                    long delta = SystemClock.elapsedRealtime() - mProxySensorTimestamp;
                    if (DEBUG_SENSOR) Log.i(TAG, "delta = " + delta);
                    if (mUseWaveCheck && delta < HANDWAVE_MAX_DELTA_MS) {
                        launchDozePulse();
                    }
                    if (mUsePocketCheck && delta > POCKET_MIN_DELTA_MS) {
                        launchDozePulse();
                    }
                }
                mProxySensorTimestamp = SystemClock.elapsedRealtime();
                mProxyWasNear = mProxyIsNear;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(
                    Settings.System.DEVICE_PROXI_CHECK_ENABLED),
                    false, this);
            mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(
                    Settings.System.DEVICE_FEATURE_SETTINGS),
                    false, this);
            update();
            updateDozeSettings();
        }

        @Override
        public void onChange(boolean selfChange) {
            update();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(
                    Settings.System.DEVICE_FEATURE_SETTINGS))){
                updateDozeSettings();
                return;
            }
            update();
        }

        public void update() {
            mUseProxiCheck = Settings.System.getIntForUser(
                    mContext.getContentResolver(), Settings.System.DEVICE_PROXI_CHECK_ENABLED, 1,
                    UserHandle.USER_CURRENT) == 1;
        }
    }

    private BroadcastReceiver mSystemStateReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                 mDispOn = true;
                 onDisplayOn();
             } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                 mDispOn = false;
                 onDisplayOff();
             } else if (intent.getAction().equals(Intent.ACTION_USER_SWITCHED)) {
                int userId = intent.getIntExtra(Intent.EXTRA_USER_HANDLE, UserHandle.USER_NULL);
                if (userId == UserHandle.USER_SYSTEM && mRestoreUser) {
                    if (DEBUG) Log.i(TAG, "ACTION_USER_SWITCHED to system");
                    Startup.restoreAfterUserSwitch(context);
                } else {
                    mRestoreUser = true;
                }
             }
         }
    };

    public KeyHandler(Context context) {
        mContext = context;
        mDispOn = true;
        mEventHandler = new EventHandler();
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mGestureWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "GestureWakeLock");
        mSettingsObserver = new SettingsObserver(mHandler);
        mSettingsObserver.observe();
        mNoMan = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mPocketSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        IntentFilter systemStateFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        systemStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        systemStateFilter.addAction(Intent.ACTION_USER_SWITCHED);
        mContext.registerReceiver(mSystemStateReceiver, systemStateFilter);
    }

    private class EventHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        }
    }

    @Override
    public boolean handleKeyEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_UP) {
            return false;
        }

        isFpgesture = false;

        if (DEBUG) Log.i(TAG, "nav_code= " + event.getScanCode());
        int fpcode = event.getScanCode();
        mFPcheck = canHandleKeyEvent(event);
        String value = getGestureValueForFPScanCode(fpcode);
        if (mFPcheck && mDispOn && !TextUtils.isEmpty(value) && !value.equals(AppSelectListPreference.DISABLED_ENTRY)){
            isFpgesture = true;
            if (!launchSpecialActions(value) && !isCameraLaunchEvent(event)) {
                    Intent intent = createIntent(value);
                    if (DEBUG) Log.i(TAG, "intent = " + intent);
                    mContext.startActivity(intent);
            }
        }
        return isFpgesture;
    }

    @Override
    public boolean canHandleKeyEvent(KeyEvent event) {
        return ArrayUtils.contains(sSupportedGestures, event.getScanCode());
    }

    @Override
    public boolean isDisabledKeyEvent(KeyEvent event) {
        boolean isProxyCheckRequired = mUseProxiCheck &&
                ArrayUtils.contains(sProxiCheckedGestures, event.getScanCode());
        if (mProxyIsNear && isProxyCheckRequired) {
            if (DEBUG) Log.i(TAG, "isDisabledKeyEvent: blocked by proxi sensor - scanCode=" + event.getScanCode());
            return true;
        }
        return false;
    }

    @Override
    public boolean isCameraLaunchEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_UP) {
            return false;
        }
        if (mFPcheck) {
            String value = getGestureValueForFPScanCode(event.getScanCode());
            return !TextUtils.isEmpty(value) && value.equals(AppSelectListPreference.CAMERA_ENTRY);
        } else {
            String value = getGestureValueForScanCode(event.getScanCode());
            return !TextUtils.isEmpty(value) && value.equals(AppSelectListPreference.CAMERA_ENTRY);
        }
    }

    @Override
    public boolean isWakeEvent(KeyEvent event){
        if (event.getAction() != KeyEvent.ACTION_UP) {
            return false;
        }
        if (event.getScanCode() == KEY_SWIPEUP_GESTURE) {
            return true;
        }
        String value = getGestureValueForScanCode(event.getScanCode());
        if (!TextUtils.isEmpty(value) && value.equals(AppSelectListPreference.WAKE_ENTRY)) {
            if (DEBUG) Log.i(TAG, "isWakeEvent " + event.getScanCode() + value);
            return true;
        }
        return event.getScanCode() == KEY_DOUBLE_TAP;
    }

    @Override
    public Intent isActivityLaunchEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_UP) {
            return null;
        }
        String value = getGestureValueForScanCode(event.getScanCode());
        if (!TextUtils.isEmpty(value) && !value.equals(AppSelectListPreference.DISABLED_ENTRY)) {
            if (DEBUG) Log.i(TAG, "isActivityLaunchEvent " + event.getScanCode() + value);
            if (!launchSpecialActions(value)) {
                Intent intent = createIntent(value);
                return intent;
            }
        }
        return null;
    }

    private IAudioService getAudioService() {
        IAudioService audioService = IAudioService.Stub
                .asInterface(ServiceManager.checkService(Context.AUDIO_SERVICE));
        if (audioService == null) {
            Log.w(TAG, "Unable to find IAudioService interface.");
        }
        return audioService;
    }

    boolean isMusicActive() {
        return mAudioManager.isMusicActive();
    }

    private void dispatchMediaKeyWithWakeLockToAudioService(int keycode) {
        if (ActivityManagerNative.isSystemReady()) {
            IAudioService audioService = getAudioService();
            if (audioService != null) {
                KeyEvent event = new KeyEvent(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN,
                        keycode, 0);
                dispatchMediaKeyEventUnderWakelock(event);
                event = KeyEvent.changeAction(event, KeyEvent.ACTION_UP);
                dispatchMediaKeyEventUnderWakelock(event);
            }
        }
    }

    private void dispatchMediaKeyEventUnderWakelock(KeyEvent event) {
        if (ActivityManagerNative.isSystemReady()) {
            MediaSessionLegacyHelper.getHelper(mContext).sendMediaButtonEvent(event, true);
        }
    }

    private void onDisplayOn() {
        if (DEBUG) Log.i(TAG, "Display on");
        if (enableProxiSensor()) {
            mSensorManager.unregisterListener(mProximitySensor, mPocketSensor);
            enableGoodix();
        }
    }

    private void enableGoodix() {
        if (Utils.fileWritable(GOODIX_CONTROL_PATH)) {
            Utils.writeValue(GOODIX_CONTROL_PATH, "0");
        }
    }
    
    private void onDisplayOff() {
        if (DEBUG) Log.i(TAG, "Display off");
        if (enableProxiSensor()) {
            mProxyWasNear = false;
            mSensorManager.registerListener(mProximitySensor, mPocketSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
            mProxySensorTimestamp = SystemClock.elapsedRealtime();
        }
    }

    private Intent createIntent(String value) {
        ComponentName componentName = ComponentName.unflattenFromString(value);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.setComponent(componentName);
        return intent;
    }

    private boolean launchSpecialActions(String value) {
        if (value.equals(AppSelectListPreference.TORCH_ENTRY)) {
            mGestureWakeLock.acquire(GESTURE_WAKELOCK_DURATION);
            IStatusBarService service = getStatusBarService();
            if (service != null) {
                try {
                    service.toggleCameraFlash();
                } catch (RemoteException e) {
                    // do nothing.
                }
            }
            return true;
        } else if (value.equals(AppSelectListPreference.MUSIC_PLAY_ENTRY)) {
            mGestureWakeLock.acquire(GESTURE_WAKELOCK_DURATION);
            dispatchMediaKeyWithWakeLockToAudioService(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
            return true;
        } else if (value.equals(AppSelectListPreference.MUSIC_NEXT_ENTRY)) {
            if (isMusicActive()) {
                mGestureWakeLock.acquire(GESTURE_WAKELOCK_DURATION);
                dispatchMediaKeyWithWakeLockToAudioService(KeyEvent.KEYCODE_MEDIA_NEXT);
            }
            return true;
        } else if (value.equals(AppSelectListPreference.MUSIC_PREV_ENTRY)) {
            if (isMusicActive()) {
                mGestureWakeLock.acquire(GESTURE_WAKELOCK_DURATION);
                dispatchMediaKeyWithWakeLockToAudioService(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
            }
            return true;
        } else if (value.equals(AppSelectListPreference.VOLUME_UP_ENTRY)) {
            mAudioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_RAISE,AudioManager.USE_DEFAULT_STREAM_TYPE,AudioManager.FLAG_SHOW_UI);
            return true;
        } else if (value.equals(AppSelectListPreference.VOLUME_DOWN_ENTRY)) {
            mAudioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_LOWER,AudioManager.USE_DEFAULT_STREAM_TYPE,AudioManager.FLAG_SHOW_UI);
            return true;
        } else if (value.equals(AppSelectListPreference.DOZE_PULSE_ENTRY)) {
            launchDozePulse();
            return true;
        } else if (value.equals(AppSelectListPreference.BROWSE_SCROLL_DOWN_ENTRY)) {
            aosipUtils.sendKeycode(KeyEvent.KEYCODE_PAGE_DOWN);
            return true;
        } else if (value.equals(AppSelectListPreference.BROWSE_SCROLL_UP_ENTRY)) {
            aosipUtils.sendKeycode(KeyEvent.KEYCODE_PAGE_UP);
            return true;
        } else if (value.equals(AppSelectListPreference.NAVIGATE_BACK_ENTRY)) {
            aosipUtils.sendKeycode(KeyEvent.KEYCODE_BACK);
            return true;
        } else if (value.equals(AppSelectListPreference.NAVIGATE_HOME_ENTRY)) {
            aosipUtils.sendKeycode(KeyEvent.KEYCODE_HOME);
            return true;
        } else if (value.equals(AppSelectListPreference.NAVIGATE_RECENT_ENTRY)) {
            aosipUtils.sendKeycode(KeyEvent.KEYCODE_APP_SWITCH);
            return true;
        }
        return false;
    }

    private String getGestureValueForScanCode(int scanCode) {
        switch(scanCode) {
            case KEY_GESTURE_C:
                return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_0, UserHandle.USER_CURRENT);
            case KEY_GESTURE_E:
                return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_1, UserHandle.USER_CURRENT);
            case KEY_GESTURE_L:
                return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_2, UserHandle.USER_CURRENT);
            case KEY_GESTURE_M:
                return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_3, UserHandle.USER_CURRENT);
            case KEY_GESTURE_O:
                return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_4, UserHandle.USER_CURRENT);
            case KEY_GESTURE_S:
                return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_5, UserHandle.USER_CURRENT);
            case KEY_GESTURE_V:
                return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_6, UserHandle.USER_CURRENT);
            case KEY_GESTURE_W:
                return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_7, UserHandle.USER_CURRENT);
            case KEY_GESTURE_Z:
                return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_8, UserHandle.USER_CURRENT);
            case KEY_GESTURE_DOWN_SWIPE:
                return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_9, UserHandle.USER_CURRENT);
            case KEY_GESTURE_LEFT_SWIPE:
                return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_10, UserHandle.USER_CURRENT);
            case KEY_GESTURE_RIGHT_SWIPE:
                return Settings.System.getStringForUser(mContext.getContentResolver(),
                    GestureSettings.DEVICE_GESTURE_MAPPING_11, UserHandle.USER_CURRENT);
        }
        return null;
    }

    private String getGestureValueForFPScanCode(int scanCode) {
        return null;
    }

    private void launchDozePulse() {
        if (DEBUG) Log.i(TAG, "Doze pulse");
        mContext.sendBroadcastAsUser(new Intent(DOZE_INTENT),
                new UserHandle(UserHandle.USER_CURRENT));
    }

    private boolean enableProxiSensor() {
        return mUsePocketCheck || mUseWaveCheck || mUseProxiCheck;
    }

    private void updateDozeSettings() {
        String value = Settings.System.getStringForUser(mContext.getContentResolver(),
                    Settings.System.DEVICE_FEATURE_SETTINGS,
                    UserHandle.USER_CURRENT);
        if (DEBUG) Log.i(TAG, "Doze settings = " + value);
        if (!TextUtils.isEmpty(value)) {
            String[] parts = value.split(":");
            mUseWaveCheck = Boolean.valueOf(parts[0]);
            mUsePocketCheck = Boolean.valueOf(parts[1]);
        }
    }

    protected static Sensor getSensor(SensorManager sm, String type) {
        for (Sensor sensor : sm.getSensorList(Sensor.TYPE_ALL)) {
            if (type.equals(sensor.getStringType())) {
                return sensor;
            }
        }
        return null;
    }

    IStatusBarService getStatusBarService() {
        return IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
    }
}

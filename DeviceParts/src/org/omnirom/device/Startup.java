/*
* Copyright (C) 2013 The OmniROM Project
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

public class Startup extends BroadcastReceiver {
    private static final String TAG = "Startup";

    private static void restore(String file, boolean enabled) {
        if (file == null) {
            return;
        }
        Utils.writeValue(file, enabled ? "1" : "0");
    }

    @Override
    public void onReceive(final Context context, final Intent bootintent) {
        restoreAfterUserSwitch(context);
    }

    public static void restoreAfterUserSwitch(Context context) {
        Log.e(TAG, "restoreAfterUserSwitch called");
        // C Gesture
        String mapping = GestureSettings.DEVICE_GESTURE_MAPPING_0;
        String value = Settings.System.getString(context.getContentResolver(), mapping);
        if (TextUtils.isEmpty(value)) {
            value = AppSelectListPreference.MUSIC_PLAY_ENTRY;
            Settings.System.putString(context.getContentResolver(), mapping, value);
        }
        boolean enabled = !value.equals(AppSelectListPreference.DISABLED_ENTRY);
        restore(GestureSettings.getGestureFile(GestureSettings.KEY_C_APP), enabled);

        // E Gesture
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_1;
        value = Settings.System.getString(context.getContentResolver(), mapping);
        if (TextUtils.isEmpty(value)) {
            value = AppSelectListPreference.CAMERA_ENTRY;
            Settings.System.putString(context.getContentResolver(), mapping, value);
        }
        enabled = !value.equals(AppSelectListPreference.DISABLED_ENTRY);
        restore(GestureSettings.getGestureFile(GestureSettings.KEY_E_APP), enabled);

        // L Gesture
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_2;
        value = Settings.System.getString(context.getContentResolver(), mapping);
        if (TextUtils.isEmpty(value)) {
            value = AppSelectListPreference.CAMERA_ENTRY;
            Settings.System.putString(context.getContentResolver(), mapping, value);
        }
        enabled = !value.equals(AppSelectListPreference.DISABLED_ENTRY);
        restore(GestureSettings.getGestureFile(GestureSettings.KEY_L_APP), enabled);

        // M Gesture
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_3;
        value = Settings.System.getString(context.getContentResolver(), mapping);
        if (TextUtils.isEmpty(value)) {
            value = AppSelectListPreference.CAMERA_ENTRY;
            Settings.System.putString(context.getContentResolver(), mapping, value);
        }
        enabled = !value.equals(AppSelectListPreference.DISABLED_ENTRY);
        restore(GestureSettings.getGestureFile(GestureSettings.KEY_M_APP), enabled);

        // O Gesture
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_4;
        value = Settings.System.getString(context.getContentResolver(), mapping);
        if (TextUtils.isEmpty(value)) {
            value = AppSelectListPreference.CAMERA_ENTRY;
            Settings.System.putString(context.getContentResolver(), mapping, value);
        }
        enabled = !value.equals(AppSelectListPreference.DISABLED_ENTRY);
        restore(GestureSettings.getGestureFile(GestureSettings.KEY_O_APP), enabled);

        // S Gesture
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_5;
        if (TextUtils.isEmpty(value)) {
            value = AppSelectListPreference.TORCH_ENTRY;
            Settings.System.putString(context.getContentResolver(), mapping, value);
        }
        value = Settings.System.getString(context.getContentResolver(), mapping);
        enabled = !TextUtils.isEmpty(value) && !value.equals(AppSelectListPreference.DISABLED_ENTRY);
        restore(GestureSettings.getGestureFile(GestureSettings.KEY_S_APP), enabled);

        // V Geture
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_6;
        value = Settings.System.getString(context.getContentResolver(), mapping);
        if (TextUtils.isEmpty(value)) {
            value = AppSelectListPreference.TORCH_ENTRY;
            Settings.System.putString(context.getContentResolver(), mapping, value);
        }
        enabled = !value.equals(AppSelectListPreference.DISABLED_ENTRY);
        restore(GestureSettings.getGestureFile(GestureSettings.KEY_V_APP), enabled);

        // W Gesture
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_7;
        value = Settings.System.getString(context.getContentResolver(), mapping);
        if (TextUtils.isEmpty(value)) {
            value = AppSelectListPreference.MUSIC_PREV_ENTRY;
            Settings.System.putString(context.getContentResolver(), mapping, value);
        }
        enabled = !value.equals(AppSelectListPreference.DISABLED_ENTRY);
        restore(GestureSettings.getGestureFile(GestureSettings.KEY_W_APP), enabled);

        // Z Gesture
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_8;
        value = Settings.System.getString(context.getContentResolver(), mapping);
        if (TextUtils.isEmpty(value)) {
            value = AppSelectListPreference.MUSIC_NEXT_ENTRY;
            Settings.System.putString(context.getContentResolver(), mapping, value);
        }
        enabled = !value.equals(AppSelectListPreference.DISABLED_ENTRY);
        restore(GestureSettings.getGestureFile(GestureSettings.KEY_Z_APP), enabled);

        // DOWN Gesture
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_9;
        if (TextUtils.isEmpty(value)) {
            value = AppSelectListPreference.TORCH_ENTRY;
            Settings.System.putString(context.getContentResolver(), mapping, value);
        }
        value = Settings.System.getString(context.getContentResolver(), mapping);
        enabled = !TextUtils.isEmpty(value) && !value.equals(AppSelectListPreference.DISABLED_ENTRY);
        restore(GestureSettings.getGestureFile(GestureSettings.KEY_DOWN_APP), enabled);

        // LEFT Gesture
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_10;
        if (TextUtils.isEmpty(value)) {
            value = AppSelectListPreference.MUSIC_PREV_ENTRY;
            Settings.System.putString(context.getContentResolver(), mapping, value);
        }
        value = Settings.System.getString(context.getContentResolver(), mapping);
        enabled = !TextUtils.isEmpty(value) && !value.equals(AppSelectListPreference.DISABLED_ENTRY);
        restore(GestureSettings.getGestureFile(GestureSettings.KEY_LEFT_SWIPE_APP), enabled);

        // RIGHT Gesture
        mapping = GestureSettings.DEVICE_GESTURE_MAPPING_11;
        if (TextUtils.isEmpty(value)) {
            value = AppSelectListPreference.MUSIC_NEXT_ENTRY;
            Settings.System.putString(context.getContentResolver(), mapping, value);
        }
        value = Settings.System.getString(context.getContentResolver(), mapping);
        enabled = !TextUtils.isEmpty(value) && !value.equals(AppSelectListPreference.DISABLED_ENTRY);
        restore(GestureSettings.getGestureFile(GestureSettings.KEY_RIGHT_SWIPE_APP), enabled);

        enabled = Settings.System.getInt(context.getContentResolver(), DeviceSettings.SETTINGS_GLOVE_KEY, 0) != 0;
        restore(DeviceSettings.getFile(), enabled);

        enabled = Settings.System.getInt(context.getContentResolver(), GestureSettings.SETTINGS_GESTURE_KEY, 0) != 0;
        restore(GestureSettings.getFile(), enabled);
    }
}

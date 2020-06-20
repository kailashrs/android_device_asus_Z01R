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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.os.Bundle;

import android.app.ActionBar;
import org.omnirom.device.SeekBarPreferenceCSV;
import org.omnirom.device.SeekBarPreferenceHue;
import org.omnirom.device.SeekBarPreferenceRGB;

public class DisplayCalibration extends PreferenceActivity implements
        OnPreferenceChangeListener {

    public static final String KEY_KCAL_ENABLED = "kcal_enabled";
    public static final String KEY_KCAL_RED = "kcal_red";
    public static final String KEY_KCAL_GREEN = "kcal_green";
    public static final String KEY_KCAL_BLUE = "kcal_blue";
    public static final String KEY_KCAL_CONTRAST = "kcal_contrast";
    public static final String KEY_KCAL_HUE = "kcal_hue";
    public static final String KEY_KCAL_SATURATION = "kcal_saturation";
    public static final String KEY_KCAL_VALUE = "kcal_value";

    private SeekBarPreferenceRGB mKcalRed;
    private SeekBarPreferenceRGB mKcalBlue;
    private SeekBarPreferenceRGB mKcalGreen;
    private SeekBarPreferenceCSV mKcalContrast;
    private SeekBarPreferenceHue mKcalHue;
    private SeekBarPreferenceCSV mKcalSaturation;
    private SeekBarPreferenceCSV mKcalValue;
    private SharedPreferences mPrefs;
    private SwitchPreference mKcalEnabled;

    private static final String COLOR_FILE_RED = "/sys/module/msm_drm/parameters/kcal_red";
    private static final String COLOR_FILE_GREEN = "/sys/module/msm_drm/parameters/kcal_green";
    private static final String COLOR_FILE_BLUE = "/sys/module/msm_drm/parameters/kcal_blue";
    private static final String COLOR_FILE_CONTRAST = "/sys/module/msm_drm/parameters/kcal_cont";
    private static final String COLOR_FILE_HUE = "/sys/module/msm_drm/parameters/kcal_hue";
    private static final String COLOR_FILE_SATURATION = "/sys/module/msm_drm/parameters/kcal_sat";
    private static final String COLOR_FILE_VALUE = "/sys/module/msm_drm/parameters/kcal_val";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.display_cal);

        ImageView imageView = (ImageView) findViewById(R.id.calibration_pic);
        imageView.setImageResource(R.drawable.calibration_png);

        addPreferencesFromResource(R.xml.display_calibration);

        mKcalEnabled = (SwitchPreference) findPreference(KEY_KCAL_ENABLED);
        mKcalEnabled.setChecked(mPrefs.getBoolean(DisplayCalibration.KEY_KCAL_ENABLED, false));
        mKcalEnabled.setOnPreferenceChangeListener(this);

        mKcalRed = (SeekBarPreferenceRGB) findPreference(KEY_KCAL_RED);
        mKcalRed.setInitValue(mPrefs.getInt(KEY_KCAL_RED, 256));
        mKcalRed.setOnPreferenceChangeListener(this);

        mKcalGreen = (SeekBarPreferenceRGB) findPreference(KEY_KCAL_GREEN);
        mKcalGreen.setInitValue(mPrefs.getInt(KEY_KCAL_GREEN, 256));
        mKcalGreen.setOnPreferenceChangeListener(this);

        mKcalBlue = (SeekBarPreferenceRGB) findPreference(KEY_KCAL_BLUE);
        mKcalBlue.setInitValue(mPrefs.getInt(KEY_KCAL_BLUE, 256));
        mKcalBlue.setOnPreferenceChangeListener(this);

        mKcalContrast = (SeekBarPreferenceCSV) findPreference(KEY_KCAL_CONTRAST);
        mKcalContrast.setInitValue(mPrefs.getInt(KEY_KCAL_CONTRAST, 383));
        mKcalContrast.setOnPreferenceChangeListener(this);

        mKcalHue = (SeekBarPreferenceHue) findPreference(KEY_KCAL_HUE);
        mKcalHue.setInitValue(mPrefs.getInt(KEY_KCAL_HUE, 1536));
        mKcalHue.setOnPreferenceChangeListener(this);

        mKcalSaturation = (SeekBarPreferenceCSV) findPreference(KEY_KCAL_SATURATION);
        mKcalSaturation.setInitValue(mPrefs.getInt(KEY_KCAL_SATURATION, 383));
        mKcalSaturation.setOnPreferenceChangeListener(this);

        mKcalValue = (SeekBarPreferenceCSV) findPreference(KEY_KCAL_VALUE);
        mKcalValue.setInitValue(mPrefs.getInt(KEY_KCAL_VALUE, 383));
        mKcalValue.setOnPreferenceChangeListener(this);

    }

    private boolean isSupported(String file) {
        return Utils.fileWritable(file);
    }

    public static void restore(Context context) {
       boolean storeEnabled = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(DisplayCalibration.KEY_KCAL_ENABLED, false);
       if (storeEnabled) {
           int storedRed = PreferenceManager
                   .getDefaultSharedPreferences(context).getInt(DisplayCalibration.KEY_KCAL_RED, 256);
           int storedGreen = PreferenceManager
                   .getDefaultSharedPreferences(context).getInt(DisplayCalibration.KEY_KCAL_GREEN, 256);
           int storedBlue = PreferenceManager
                   .getDefaultSharedPreferences(context).getInt(DisplayCalibration.KEY_KCAL_BLUE, 256);
           int storedContrast = PreferenceManager
                   .getDefaultSharedPreferences(context).getInt(DisplayCalibration.KEY_KCAL_CONTRAST, 383);
           int storedHue = PreferenceManager
                   .getDefaultSharedPreferences(context).getInt(DisplayCalibration.KEY_KCAL_HUE, 1536);
           int storedSaturation = PreferenceManager
                   .getDefaultSharedPreferences(context).getInt(DisplayCalibration.KEY_KCAL_SATURATION, 383);
           int storedValue = PreferenceManager
                   .getDefaultSharedPreferences(context).getInt(DisplayCalibration.KEY_KCAL_VALUE, 383);
           Utils.writeValue(COLOR_FILE_RED, String.valueOf(storedRed));
           Utils.writeValue(COLOR_FILE_GREEN, String.valueOf(storedGreen));
           Utils.writeValue(COLOR_FILE_BLUE, String.valueOf(storedBlue));
           Utils.writeValue(COLOR_FILE_CONTRAST, String.valueOf(storedContrast));
           Utils.writeValue(COLOR_FILE_HUE, String.valueOf(storedHue));
           Utils.writeValue(COLOR_FILE_SATURATION, String.valueOf(storedSaturation));
           Utils.writeValue(COLOR_FILE_VALUE, String.valueOf(storedValue));
       }
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mKcalEnabled) {
            Boolean enabled = (Boolean) newValue;
            mPrefs.edit().putBoolean(KEY_KCAL_ENABLED, enabled).commit();
            String mRed = String.valueOf(mPrefs.getInt(KEY_KCAL_RED, 256));
            String mGreen = String.valueOf(mPrefs.getInt(KEY_KCAL_GREEN, 256));
            String mBlue = String.valueOf(mPrefs.getInt(KEY_KCAL_BLUE, 256));
            String mContrast = String.valueOf(mPrefs.getInt(KEY_KCAL_CONTRAST, 383));
            String mHue = String.valueOf(mPrefs.getInt(KEY_KCAL_HUE, 1536));
            String mSaturation = String.valueOf(mPrefs.getInt(KEY_KCAL_SATURATION, 383));
            String mValue = String.valueOf(mPrefs.getInt(KEY_KCAL_VALUE, 383));
            Utils.writeValue(COLOR_FILE_RED, mRed);
            Utils.writeValue(COLOR_FILE_GREEN, mGreen);
            Utils.writeValue(COLOR_FILE_BLUE, mBlue);
            Utils.writeValue(COLOR_FILE_CONTRAST, mContrast);
            Utils.writeValue(COLOR_FILE_HUE, mHue);
            Utils.writeValue(COLOR_FILE_SATURATION, mSaturation);
            Utils.writeValue(COLOR_FILE_VALUE, mValue);
            return true;
        } else if (preference == mKcalRed) {
            float val = Float.parseFloat((String) newValue);
            mPrefs.edit().putInt(KEY_KCAL_RED, (int) val).commit();
            String strVal = (String) newValue;
            Utils.writeValue(COLOR_FILE_RED, strVal);
            return true;
        } else if (preference == mKcalGreen) {
            float val = Float.parseFloat((String) newValue);
            mPrefs.edit().putInt(KEY_KCAL_GREEN, (int) val).commit();
            String strVal = (String) newValue;
            Utils.writeValue(COLOR_FILE_GREEN, strVal);
            return true;
        } else if (preference == mKcalBlue) {
            float val = Float.parseFloat((String) newValue);
            mPrefs.edit().putInt(KEY_KCAL_BLUE, (int) val).commit();
            String strVal = (String) newValue;
            Utils.writeValue(COLOR_FILE_BLUE, strVal);
            return true;
        } else if (preference == mKcalContrast) {
            float val = Float.parseFloat((String) newValue);
            mPrefs.edit().putInt(KEY_KCAL_CONTRAST, (int) val).commit();
            String strVal = (String) newValue;
            Utils.writeValue(COLOR_FILE_CONTRAST, strVal);
            return true;
        } else if (preference == mKcalHue) {
            float val = Float.parseFloat((String) newValue);
            mPrefs.edit().putInt(KEY_KCAL_HUE, (int) val).commit();
            String strVal = (String) newValue;
            Utils.writeValue(COLOR_FILE_HUE, strVal);
            return true;
        } else if (preference == mKcalSaturation) {
            float val = Float.parseFloat((String) newValue);
            mPrefs.edit().putInt(KEY_KCAL_SATURATION, (int) val).commit();
            String strVal = (String) newValue;
            Utils.writeValue(COLOR_FILE_SATURATION, strVal);
            return true;
        } else if (preference == mKcalValue) {
            float val = Float.parseFloat((String) newValue);
            mPrefs.edit().putInt(KEY_KCAL_VALUE, (int) val).commit();
            String strVal = (String) newValue;
            Utils.writeValue(COLOR_FILE_VALUE, strVal);
            return true;
        }
        return false;
    }
}

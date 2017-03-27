/*
 * Copyright (C) 2016 Cosmic-OS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cosmic.settings.fragments;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.widget.LockPatternUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.cosmic.settings.utils.Utils;
import com.cosmic.settings.preferences.SystemSettingSwitchPreference;
import android.provider.Settings;

public class LockScreenSettings extends SettingsPreferenceFragment {

    private static final int MY_USER_ID = UserHandle.myUserId();

    private static final String LS_OPTIONS_CAT = "lockscreen_options";
    private static final String LS_SECURE_CAT = "lockscreen_secure_options";

    private static final String KEYGUARD_TORCH = "keyguard_toggle_torch";
    private static final String FINGERPRINT_VIB = "fingerprint_success_vib";
    private static final String FP_UNLOCK_KEYSTORE = "fp_unlock_keystore";
    private static final String FP_MAX_FAILED_ATTEMPTS = "fp_max_failed_attempts";
    private ListPreference maxFailedAttempts;

    private FingerprintManager mFingerprintManager;

    private SystemSettingSwitchPreference mLsTorch;
    private SystemSettingSwitchPreference mFingerprintVib;
    private SystemSettingSwitchPreference mFpKeystore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreen_settings);
        PreferenceScreen prefScreen = getPreferenceScreen();
        final LockPatternUtils lockPatternUtils = new LockPatternUtils(getActivity());

        PreferenceCategory optionsCategory = (PreferenceCategory) findPreference(LS_OPTIONS_CAT);
        PreferenceCategory secureCategory = (PreferenceCategory) findPreference(LS_SECURE_CAT);

        mLsTorch = (SystemSettingSwitchPreference) findPreference(KEYGUARD_TORCH);
        if (!Utils.deviceSupportsFlashLight(getActivity())) {
            optionsCategory.removePreference(mLsTorch);
        }

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintVib = (SystemSettingSwitchPreference) findPreference(FINGERPRINT_VIB);
        mFpKeystore = (SystemSettingSwitchPreference) findPreference(FP_UNLOCK_KEYSTORE);
        if (!mFingerprintManager.isHardwareDetected()) {
            secureCategory.removePreference(mFingerprintVib);
            secureCategory.removePreference(mFpKeystore);
        }

        if (!lockPatternUtils.isSecure(MY_USER_ID)) {
            prefScreen.removePreference(secureCategory);
        }

        // fp max failed attempts
        maxFailedAttempts = (ListPreference) findPreference(FP_MAX_FAILED_ATTEMPTS);
        int set = Settings.System.getIntForUser(resolver,
                Settings.System.FP_MAX_FAILED_ATTEMPTS, 5, UserHandle.USER_CURRENT);
        maxFailedAttempts.setValue(String.valueOf(set));
        maxFailedAttempts.setSummary(maxFailedAttempts.getEntry());
        maxFailedAttempts.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
         if (preference == maxFailedAttempts) {
            int set = Integer.valueOf((String) objValue);
            int index = maxFailedAttempts.findIndexOfValue((String) objValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.FP_MAX_FAILED_ATTEMPTS, set, UserHandle.USER_CURRENT);
            maxFailedAttempts.setSummary(maxFailedAttempts.getEntries()[index]);
            return true;
        }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.GALAXY;
    }
}

package com.kci.utils;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.kci.smsreceiver.R;

public class SettingsActivity extends PreferenceActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}

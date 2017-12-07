package ca.nicksalt.appdoc;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.View;

import java.util.zip.Inflater;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class PreferencesActivity extends PreferenceFragment {
    boolean notification, autoDownload;
    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        // Get current preferences and set them
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final SwitchPreference notifications = (SwitchPreference)findPreference("notifications");
        notification = prefs.getBoolean("notifications", true);
        notifications.setDefaultValue(notification);
        //Change preference
        notifications.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Log.d("AppDoc", "notification");
                SharedPreferences.Editor editor = prefs.edit();
                notification = !notification;
                editor.putBoolean("notifications", notification);
                editor.apply();
                return true;
            }
        });
        final SwitchPreference download = (SwitchPreference)findPreference("autoDownload");
        autoDownload = prefs.getBoolean("download", false);
        download.setDefaultValue(autoDownload);
        download.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Log.d("AppDoc", "download");
                SharedPreferences.Editor editor = prefs.edit();
                autoDownload = !autoDownload;
                editor.putBoolean("download", autoDownload);
                editor.apply();
                return true;
            }
        });
    }


}
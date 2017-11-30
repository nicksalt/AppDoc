package ca.nicksalt.appdoc;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

/**
 * Created by jaxbl on 2017-11-30.
 */

public class PreferenceActivity extends AppCompatPreferenceActivity{

    private final static String TAG = "PreferenceAcitivity";

    public PreferenceActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new LocationFragment()).commit();
    }

    public static class LocationFragment extends PreferenceFragment {

        private final static String TAG = "LocationFragment";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_general);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


       return true;
    }
}

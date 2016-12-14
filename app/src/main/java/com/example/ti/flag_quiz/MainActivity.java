package com.example.ti.flag_quiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static final String CHOICES ="pref_numberOfChoices";
    public static final String REGIONS ="pref_regionsToInclude";
    private boolean phoneDevice = true; // to force the phone into portrait mode
    private boolean preferencesChanged = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false); // to set default prefrence values

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(preferencesChangeListener);

        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK; // to determine screen size
        // to check if device is a tablet
        if(screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||screenSize==Configuration.SCREENLAYOUT_SIZE_XLARGE)
            phoneDevice = false;

        // if device is a phone restrict it to portrait view only

        if(phoneDevice){
            setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            );
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        if (preferencesChanged) {
            MainActivityFragment quizFragment =
                    (MainActivityFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.quizfragment);

            quizFragment.updateGuessRows(PreferenceManager
                    .getDefaultSharedPreferences(this));
            quizFragment.updateRegions(
                    PreferenceManager.getDefaultSharedPreferences(this));

            quizFragment.resetQuiz();
            preferencesChanged = false;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        int orientation = getResources().getConfiguration().orientation;

        if(orientation == Configuration.ORIENTATION_PORTRAIT){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;}
        else
            return false;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent pIntent =  new Intent(this,SettingsActivity.class);
        startActivity(pIntent);

        return super.onOptionsItemSelected(item);
    }
     // Anonymous inner class that implements OnSharedPreferenceChangeListner
    private OnSharedPreferenceChangeListener preferencesChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            preferencesChanged  =true;
            MainActivityFragment quizFragment = (MainActivityFragment)getSupportFragmentManager().findFragmentById(R.id.qiuzFragment);

            if(key.equals(CHOICES)){ // if number of choices to display is changed
                try{
                quizFragment.updateGuessRows(sharedPreferences);
                quizFragment.resetQuiz();}

                catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
            else if(key.equals(REGIONS)){ // check if the region changes
                Set<String> regions =  sharedPreferences.getStringSet(REGIONS , null);

                if (regions != null && regions.size()>0){

                    try{
                    quizFragment.updateRegions(sharedPreferences);
                        quizFragment.resetQuiz();}
                    catch (NullPointerException e){
                        e.printStackTrace();
                    }

                }
                else {
                    //user must set North america as a default region

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    try {

                        regions.add(getString(R.string.default_region));}

                    catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    editor.putStringSet(REGIONS, regions);
                    editor.apply();
                    Toast.makeText(MainActivity.this, R.string.default_region_message, Toast.LENGTH_SHORT).show();
                }
            }

            Toast.makeText(MainActivity.this, R.string.restarting_quiz,Toast.LENGTH_SHORT).show();


        }
    };
}

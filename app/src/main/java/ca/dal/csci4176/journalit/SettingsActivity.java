package ca.dal.csci4176.journalit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ToggleButton;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    private Toolbar mToolbar;
    private ToggleButton location;
    private CheckBox note, task, mood, step, caffeine, water;
    private Button button;
    private Prefs mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mPrefs = new Prefs(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        location = (ToggleButton) findViewById(R.id.tb);
        note = (CheckBox) findViewById(R.id.note);
        task = (CheckBox) findViewById(R.id.task);
        mood = (CheckBox) findViewById(R.id.mood);
        step = (CheckBox) findViewById(R.id.step);
        caffeine = (CheckBox) findViewById(R.id.caffeine);
        water = (CheckBox) findViewById(R.id.water);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        mToolbar.setTitle("Settings");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        note.setChecked(mPrefs.doShowNotes());
        task.setChecked(mPrefs.doShowTasks());
        mood.setChecked(mPrefs.doShowMood());
        caffeine.setChecked(mPrefs.doShowCaffeine());
        water.setChecked(mPrefs.doShowWater());
        step.setChecked(mPrefs.doShowSteps());
        location.setChecked(mPrefs.isLocationEnabled());
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button) {
            mPrefs.setShowNotes(note.isChecked());
            mPrefs.setShowTasks(task.isChecked());
            mPrefs.setShowMood(mood.isChecked());
            mPrefs.setShowCaffeine(caffeine.isChecked());
            mPrefs.setShowWater(water.isChecked());
            mPrefs.setShowsteps(step.isChecked());
            mPrefs.setLocationEnabled(location.isChecked());
            finish();
        }
    }
}
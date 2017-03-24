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


/**
 * Created by WZ on 2017/3/23.
 */

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    private Toolbar mToolbar;
    private ToggleButton location;
    private CheckBox note, task;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        location = (ToggleButton) findViewById(R.id.tb);
        note = (CheckBox) findViewById(R.id.note);
        task = (CheckBox) findViewById(R.id.task);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        mToolbar.setTitle("SettingsActivity");
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
        note.setChecked(load("note", true));
        task.setChecked(load("task", true));
        location.setChecked(load("location", true));
    }

    private void save(String s, final boolean isChecked) {
        SharedPreferences sharedPreferences = getSharedPreferences("BooleanValue", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(s, isChecked);
        editor.commit();
    }

    private boolean load(String s, boolean defaultVal) {
        SharedPreferences sharedPreferences = getSharedPreferences("BooleanValue", 0);
        return sharedPreferences.getBoolean(s, defaultVal);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button) {
            save("note", note.isChecked());
            save("task", task.isChecked());
            save("location", location.isChecked());
            Intent i = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(i);
        }
    }
}
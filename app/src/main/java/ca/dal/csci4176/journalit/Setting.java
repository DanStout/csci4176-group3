package ca.dal.csci4176.journalit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.CheckBox;
import android.widget.ToggleButton;


/**
 * Created by WZ on 2017/3/23.
 */

public class Setting extends AppCompatActivity {

    private Toolbar mToolbar;
    private ToggleButton location;
    private CheckBox note, task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        location = (ToggleButton) findViewById(R.id.tb);
        note = (CheckBox) findViewById(R.id.note);
        task = (CheckBox) findViewById(R.id.task);
        mToolbar.setTitle("Setting");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



    }

    @Override
    public void onPause() {
        super.onPause();
        save("note", note.isChecked());
        save("task", task.isChecked());
        save("location", location.isChecked());
    }

    @Override
    public void onResume() {
        super.onResume();
        note.setChecked(load("note"));
        task.setChecked(load("task"));
        location.setChecked(load("location"));
    }

    private void save(String s, final boolean isChecked) {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(s, isChecked);
        editor.commit();
    }

    private boolean load(String s) {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(s, false);
    }

    /*@Override
    public void onBackPressed() {
        Intent data = new Intent();
        // add data to Intent
        setResult(Activity.RESULT_OK, data);
        super.onBackPressed();
    }*/
}
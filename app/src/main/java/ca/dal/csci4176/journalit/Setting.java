package ca.dal.csci4176.journalit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

public class Setting extends AppCompatActivity implements View.OnClickListener{

    private Toolbar mToolbar;
    private ToggleButton location;
    private CheckBox note, task;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        location = (ToggleButton) findViewById(R.id.tb);
        note = (CheckBox) findViewById(R.id.note);
        task = (CheckBox) findViewById(R.id.task);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        mToolbar.setTitle("Setting");
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
        note.setChecked(load("note"));
        task.setChecked(load("task"));
        location.setChecked(load("location"));
    }

    private void save(String s, final boolean isChecked) {
        SharedPreferences sharedPreferences = getSharedPreferences("BooleanValue", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(s, isChecked);
        editor.commit();
    }

    private boolean load(String s) {
        SharedPreferences sharedPreferences = getSharedPreferences("BooleanValue", 0);
        return sharedPreferences.getBoolean(s, false);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button) {
            save("note", note.isChecked());
            save("task", task.isChecked());
            save("location", location.isChecked());
            Intent i = new Intent(Setting.this, MainActivity.class);
            startActivity(i);
        }
    }
}
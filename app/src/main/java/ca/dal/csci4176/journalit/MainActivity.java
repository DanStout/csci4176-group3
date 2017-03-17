package ca.dal.csci4176.journalit;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import ca.dal.csci4176.journalit.models.DailyEntry;
import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity
{
    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        RecyclerView rv = (RecyclerView) findViewById(R.id.cardList);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(v -> Toast.makeText(MainActivity.this, "hi", Toast.LENGTH_SHORT).show());

        Realm realm = Realm.getDefaultInstance();
        RealmResults<DailyEntry> entries = realm.where(DailyEntry.class).findAll();
        RVAdapter adapter = new RVAdapter(this, entries);
        rv.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_edit:
                Timber.d("Edit");
                break;
            case R.id.action_share:
                Timber.d("Share");
                break;
            case R.id.action_settings:
                Timber.d("Settings");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}

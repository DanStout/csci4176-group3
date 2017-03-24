package ca.dal.csci4176.journalit;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.threeten.bp.LocalDate;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.dal.csci4176.journalit.models.BulletItem;
import ca.dal.csci4176.journalit.models.CheckboxItem;
import ca.dal.csci4176.journalit.models.DailyEntry;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity
{
    @BindView(R.id.main_fab)
    FloatingActionButton mFab;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.main_entry_list)
    RecyclerView mRecycler;

    private DailyEntry mToday;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);


        mRecycler.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(llm);

        mFab.setOnClickListener(v -> openTodayEntry());

        Realm realm = Realm.getDefaultInstance();

        RealmResults<DailyEntry> entries = realm
                .where(DailyEntry.class)
                .findAllSorted("key", Sort.DESCENDING);

        RVAdapter adapter = new RVAdapter(this, entries);
        mRecycler.setAdapter(adapter);

        mToday = realm
                .where(DailyEntry.class)
                .equalTo("key", DailyEntry.getKeyOfToday())
                .findFirst();

        if (mToday == null)
        {
            mToday = new DailyEntry();
            mToday.setDate(LocalDate.now());

            BulletItem note = new BulletItem("");
            CheckboxItem task = new CheckboxItem("", false);

            realm.beginTransaction();
            mToday = realm.copyToRealm(mToday);
            mToday.getNotes().add(note);
            mToday.getTasks().add(task);
            realm.commitTransaction();
        }
    }

    private void openTodayEntry()
    {
        startActivity(DailyEntryActivity.getIntent(this, mToday));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_edit:
                Timber.d("Edit");
                break;
            case R.id.menu_share:
                Timber.d("Share");
                break;
            case R.id.menu_settings:
                Timber.d("Settings");
                Intent i = new Intent(MainActivity.this, Setting.class);
                startActivity(i);
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

package ca.dal.csci4176.journalit;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nononsenseapps.filepicker.FilePickerActivity;

import org.threeten.bp.LocalDate;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.dal.csci4176.journalit.models.BulletItem;
import ca.dal.csci4176.journalit.models.CheckboxItem;
import ca.dal.csci4176.journalit.models.DailyEntry;
import ca.dal.csci4176.journalit.service.LocationGetter;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity
{
    private static final int REQ_EXPORT_NEW_FILE = 0;

    @BindView(R.id.main_fab)
    FloatingActionButton mFab;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.main_entry_list)
    RecyclerView mRecycler;

    private Random rand = new Random();
    private LocationGetter mLocGetter;
    private DailyEntry mToday;
    private Realm mRealm;
    private Prefs mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mPrefs = new Prefs(this);
        mLocGetter = new LocationGetter(this);
        setSupportActionBar(mToolbar);

        checkGoogleSignin();

        mRecycler.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(llm);

        mFab.setOnClickListener(v -> openTodayEntry());

        mRealm = Realm.getDefaultInstance();

        RealmResults<DailyEntry> entries = getEntries();

        RVAdapter adapter = new RVAdapter(this, entries);
        mRecycler.setAdapter(adapter);

        mToday = mRealm
                .where(DailyEntry.class)
                .equalTo("key", DailyEntry.getKeyOfToday())
                .findFirst();

        if (mToday == null)
        {
            mToday = createAndSaveEntryForToday();
        }
    }

    private DailyEntry createAndSaveEntryForToday()
    {
        DailyEntry ent = new DailyEntry();
        ent.setDate(LocalDate.now());

        BulletItem note = new BulletItem("");
        CheckboxItem task = new CheckboxItem("", false);

        mRealm.beginTransaction();
        ent = mRealm.copyToRealm(ent);
        ent.getNotes().add(note);
        ent.getTasks().add(task);
        mRealm.commitTransaction();

        if (mPrefs.isLocationEnabled())
        {
            saveCreationLocationForEntry(ent);
        }

        return ent;
    }

    private RealmResults<DailyEntry> getEntries()
    {
        return mRealm
                .where(DailyEntry.class)
                .findAllSorted("key", Sort.DESCENDING);
    }

    private void saveCreationLocationForEntry(DailyEntry ent)
    {
        mLocGetter.findLocation(location ->
        {
            if (mRealm.isInTransaction())
            {
                Timber.d("Cannot save location: already in transaction");
                return;
            }

            mRealm.executeTransaction(r ->
            {
                ent.setLongitude(location.getLongitude());
                ent.setLatitude(location.getLatitude());
            });
        });
    }

    /**
     * Make the user sign in so we can get the fitness data
     */
    private void checkGoogleSignin()
    {
        if (mPrefs.isSignedIn())
        {
            connectGoogleApiClient();
        }
        else if (!mPrefs.didUserDeclineGoogleAcc())
        {
            new MaterialDialog.Builder(this)
                    .content(R.string.explain_google_acc)
                    .positiveText(R.string.yes)
                    .negativeText(R.string.no)
                    .onPositive((dialog, which) -> connectGoogleApiClient())
                    .onNegative((dialog, which) -> mPrefs.setDidUserDeclineGoogleAcc(true))
                    .show();
        }
    }

    private void connectGoogleApiClient()
    {
        GoogleApiClient client = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .enableAutoManage(this, res ->
                {
                    Timber.d("Connection failed: %s", res);
                    mPrefs.setSignedIn(false);
                })
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks()
                {
                    @Override
                    public void onConnected(@Nullable Bundle bundle)
                    {
                        mPrefs.setSignedIn(true);
                    }

                    @Override
                    public void onConnectionSuspended(int i)
                    {

                    }
                })
                .build();
        client.connect();
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
            case R.id.menu_export:
                chooseExportDataFile();
                break;
            case R.id.menu_demo:
                Timber.d("Demo");
                addDemoEntries();
                break;
            case R.id.menu_settings:
                Timber.d("Settings");
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void chooseExportDataFile()
    {
        Intent i = new Intent(this, FilePickerActivity.class);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_NEW_FILE);
        startActivityForResult(i, REQ_EXPORT_NEW_FILE);
    }

    private void exportDataToFile(Uri contentUri)
    {

        try (
                OutputStream out = getContentResolver().openOutputStream(contentUri);
                PrintWriter writer = new PrintWriter(out))
        {
            RealmResults<DailyEntry> entries = getEntries();
            Gson g = new GsonBuilder().setPrettyPrinting().create();
            String text = g.toJson(mRealm.copyFromRealm(entries));
            writer.write(text);
            Toast.makeText(this, R.string.export_success, Toast.LENGTH_LONG).show();
        }
        catch (IOException e)
        {
            Timber.w(e, "Failed to export data");
            Toast.makeText(this, R.string.export_failed, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQ_EXPORT_NEW_FILE:
                if (resultCode != Activity.RESULT_OK) return;
                exportDataToFile(data.getData());
                return;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void addDemoEntries()
    {
        LocalDate day = LocalDate.now();
        mRealm.beginTransaction();
        for (int i = 0; i < 7; i++)
        {
            DailyEntry ent = new DailyEntry();
            ent.setDate(day);
            day = day.minusDays(1);

            ent = mRealm.copyToRealmOrUpdate(ent);


            BulletItem note1 = new BulletItem("I drank 1500 litres of coffee today");
            LatLng note1loc = getRandomDemoLocation();
            note1.setEntryLat(note1loc.latitude);
            note1.setEntryLong(note1loc.longitude);

            BulletItem note2 = new BulletItem("I saw my nemesis on the bus");
            LatLng note2loc = getRandomDemoLocation();
            note2.setEntryLat(note2loc.latitude);
            note2.setEntryLong(note2loc.longitude);

            CheckboxItem task1 = new CheckboxItem("Take out the garbage", Math.random() < 0.5);
            CheckboxItem task2 = new CheckboxItem("Learn to speak Korean", Math.random() < 0.5);

            ent.getNotes().add(note1);
            ent.getNotes().add(note2);

            ent.getTasks().add(task1);
            ent.getTasks().add(task2);

            LatLng loc = getRandomDemoLocation();
            ent.setLatitude(loc.latitude);
            ent.setLongitude(loc.longitude);
        }
        mRealm.commitTransaction();
    }

    private LatLng getRandomDemoLocation()
    {
        double std = 0.001;
        double lat = rand.nextGaussian() * std + 44.637401;
        double lng = rand.nextGaussian() * std - 63.587379;
        return new LatLng(lat, lng);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mRealm.close();
    }
}

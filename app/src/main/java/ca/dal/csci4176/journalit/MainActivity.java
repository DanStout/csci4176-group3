package ca.dal.csci4176.journalit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.nononsenseapps.filepicker.FilePickerActivity;

import org.threeten.bp.LocalDate;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

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
    private static final int REQ_EXPORT_NEW_FILE = 0;

    @BindView(R.id.main_fab)
    FloatingActionButton mFab;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.main_entry_list)
    RecyclerView mRecycler;

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
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria gpsConf = new Criteria();
        gpsConf.setAccuracy(Criteria.ACCURACY_FINE);
        gpsConf.setPowerRequirement(Criteria.POWER_MEDIUM);
        gpsConf.setAltitudeRequired(false);
        gpsConf.setSpeedRequired(false);
        gpsConf.setBearingRequired(false);
        gpsConf.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);

        LocationListener locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                Log.d("Location Changed.", location.toString());
                mRealm.beginTransaction();
                ent.setLatitude(location.getLatitude());
                ent.setLongitude(location.getLongitude());
                mRealm.commitTransaction();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras)
            {
                Log.d("Status Changed.", String.valueOf(status));
            }

            @Override
            public void onProviderEnabled(String provider)
            {
                Log.d("Provider Enabled", provider);
            }

            @Override
            public void onProviderDisabled(String provider)
            {
                Log.d("Provider Disabled", provider);
            }
        };

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener()
                {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response)
                    {
                        Timber.d("Permission granted");
                        //noinspection MissingPermission
                        locationManager.requestSingleUpdate(gpsConf, locationListener, null);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response)
                    {
                        Timber.d("Permission denied");
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token)
                    {
                        Timber.d("Showing rationale..");
                        new MaterialDialog.Builder(MainActivity.this)
                                .content(R.string.rationale_location)
                                .positiveText(R.string.OK)
                                .negativeText(R.string.no_thanks)
                                .onPositive((dialog, which) ->
                                {
                                    Timber.d("Continuing with permission request");
                                    token.continuePermissionRequest();
                                })
                                .onNegative((dialog, which) ->
                                {
                                    Timber.d("Cancelling permission request");
                                    token.cancelPermissionRequest();
                                })
                                .show();
                    }
                })
                .check();
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

        try(
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
            BulletItem note2 = new BulletItem("I saw my nemesis on the bus");

            CheckboxItem task1 = new CheckboxItem("Take out the garbage", false);
            CheckboxItem task2 = new CheckboxItem("Learn to speak Korean", false);

            ent.getNotes().add(note1);
            ent.getNotes().add(note2);

            ent.getTasks().add(task1);
            ent.getTasks().add(task2);
        }
        mRealm.commitTransaction();
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

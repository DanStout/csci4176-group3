package ca.dal.csci4176.journalit;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jmedeisis.draglinearlayout.DragLinearLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.dal.csci4176.journalit.models.BulletItem;
import ca.dal.csci4176.journalit.models.CheckboxItem;
import ca.dal.csci4176.journalit.models.DailyEntry;
import ca.dal.csci4176.journalit.models.Mood;
import ca.dal.csci4176.journalit.models.MoodItem;
import ca.dal.csci4176.journalit.utils.BitmapUtils;
import ca.dal.csci4176.journalit.utils.ViewUtils;
import ca.dal.csci4176.journalit.views.BulletItemView;
import ca.dal.csci4176.journalit.views.CheckboxItemView;
import io.realm.Realm;
import timber.log.Timber;

import static android.location.LocationManager.*;

public class DailyEntryActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final DateTimeFormatter mImgFileNameDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd_hhmmssSSS");
    private static final String EXTRA_ENTRY_ID = "entry_id";
    private static final int REQ_TAKE_PHOTO = 1;

    private DailyEntry mEntry;
    private Realm mRealm;
    private File mPhotoFile;
    private Prefs mPrefs;
    private Drawable mBackDraw;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.entry_notes_container)
    DragLinearLayout mNoteCont;

    @BindView(R.id.entry_notes)
    TextView mNote;

    @BindView(R.id.entry_tasks_container)
    DragLinearLayout mTaskCont;

    @BindView(R.id.entry_tasks)
    TextView mTask;

    @BindView(R.id.mood_title)
    TextView mMoodTitle;

    @BindView(R.id.mood_spinner)
    Spinner mMoodSpinner;

    @BindView(R.id.entry_step_container)
    LinearLayout mStepCont;

    @BindView(R.id.entry_no_photo_container)
    LinearLayout mNoPhotoCont;

    @BindView(R.id.entry_photo)
    ImageView mPhoto;

    @BindView(R.id.entry_steps)
    TextView mTxtSteps;

    @BindView(R.id.caffeine)
    View mCaffeine;

    @BindView(R.id.water)
    View mWater;

    SupportMapFragment mMap;

    @OnClick(R.id.entry_no_photo_container)
    public void takePhoto() {
        Intent in = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            mPhotoFile = createImageFile();
        } catch (IOException e) {
            Timber.w(e, "Failed to create temp image file");
            Toast.makeText(this, R.string.photo_failed, Toast.LENGTH_LONG).show();
            return;
        }

        Uri imgUri = FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID + ".provider", mPhotoFile);
        in.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        ClipData clip = ClipData.newUri(getContentResolver(), "Photo", imgUri);
        in.setClipData(clip);
        in.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        if (in.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(in, REQ_TAKE_PHOTO);
        } else {
            Timber.d("Unable to find activity to take photo");
        }
    }

    private File createImageFile() throws IOException {
        String imgName = "img_" + LocalDateTime.now().format(mImgFileNameDateFormat) + "_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imgName, ".jpg", dir);
    }

    public static Intent getIntent(Context ctx, DailyEntry entry) {
        Intent in = new Intent(ctx, DailyEntryActivity.class);
        in.putExtra(EXTRA_ENTRY_ID, entry.getKey());
        return in;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_entry);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mPrefs = new Prefs(this);

        mRealm = Realm.getDefaultInstance();

        // If we don't make a copy of the BG drawable, the UI will go all crazy when we reuse it
        mBackDraw = getWindow().getDecorView().getBackground().getConstantState().newDrawable();

        long id = getIntent().getLongExtra(EXTRA_ENTRY_ID, -1);
        if (id == -1) {
            Timber.w("Extra %s not found!", EXTRA_ENTRY_ID);
            finish();
            return;
        }

        mEntry = mRealm
                .where(DailyEntry.class)
                .equalTo("key", id)
                .findFirst();

        Timber.d("Found entry: %s", mEntry);
        setTitle(mEntry.getDateFormatted());
        setPhotoFromEntry();
        mTxtSteps.setText(String.valueOf(mEntry.getSteps()));

        mMap = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMap.getMapAsync(this);

        ((TextView) mCaffeine.findViewById(R.id.title)).setText("Caffeine Servings: ");
        ((EditText) mCaffeine.findViewById(R.id.num_val)).setText(String.valueOf(mEntry.getCaffeine()));

        ((TextView) mWater.findViewById(R.id.title)).setText("Water Servings: ");
        ((EditText) mWater.findViewById(R.id.num_val)).setText(String.valueOf(mEntry.getWater()));

        Timber.d("Found mood: %s", mEntry.getMood());

        Spinner mood_spinner = (Spinner) findViewById(R.id.mood_spinner);
        ArrayAdapter<Mood> mood_adapter = new ArrayAdapter<Mood>(this, android.R.layout.simple_spinner_item, Mood.values());
        mood_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mood_spinner.setAdapter(mood_adapter);

        if (mEntry.getMood() != null) {
            int index = mood_adapter.getPosition(mEntry.getMood().getEnum());
            mood_spinner.setSelection(index);
        }

        mood_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Mood mood = (Mood) parent.getItemAtPosition(position);

                mRealm.beginTransaction();
                MoodItem mItem = mRealm.createObject(MoodItem.class);
                mItem.saveEnum(mood);
                mEntry.setMood(mItem);
                mRealm.commitTransaction();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mEntry.getNotes().addChangeListener((col, changeSet) ->
        {
            for (int pos : changeSet.getInsertions()) {
                Timber.d("Bullet inserted at %d", pos);
                BulletItem item = col.get(pos);
                addBulletItem(item, pos, true);
            }

            for (int pos : changeSet.getDeletions()) {
                Timber.d("Bullet %d removed", pos);
                mNoteCont.removeViewAt(pos);
                int next = pos == 0 ? pos + 1 : pos - 1;
                mNoteCont.getChildAt(next).requestFocus();
            }

            for (int pos : changeSet.getChanges()) {
                BulletItemView v = (BulletItemView) mNoteCont.getChildAt(pos);
                v.updateFromItem();
                Timber.d("Bullet item %d changed", pos);
            }
        });

        for (int i = 0; i < mEntry.getNotes().size(); i++) {
            BulletItem item = mEntry.getNotes().get(i);
            addBulletItem(item, i, false);
        }

        mNoteCont.setOnViewSwapListener((firstView, firstPosition, secondView, secondPosition) ->
        {
            Timber.d("Checkbox moved: %d -> %d", firstPosition, secondPosition);
            mRealm.executeTransaction(r -> mEntry.getNotes().move(firstPosition, secondPosition));
        });

        mEntry.getTasks().addChangeListener((col, changeSet) ->
        {
            for (int pos : changeSet.getInsertions()) {
                Timber.d("Checkbox inserted at %d", pos);
                CheckboxItem item = col.get(pos);
                addCheckboxItem(item, pos, true);
            }

            for (int pos : changeSet.getDeletions()) {
                Timber.d("Checkbox %d deleted", pos);
                mTaskCont.removeViewAt(pos);
                int next = pos == 0 ? pos + 1 : pos - 1;
                mTaskCont.getChildAt(next).requestFocus();
            }

            for (int pos : changeSet.getChanges()) {
                Timber.d("Checkbox item %d changed", pos);
                CheckboxItemView v = (CheckboxItemView) mTaskCont.getChildAt(pos);
                v.updateFromItem();
            }
        });

        for (int i = 0; i < mEntry.getTasks().size(); i++) {
            CheckboxItem item = mEntry.getTasks().get(i);
            addCheckboxItem(item, i, false);
        }

        mTaskCont.setOnViewSwapListener((firstView, firstPosition, secondView, secondPosition) ->
        {
            Timber.d("Checkbox moved: %d -> %d", firstPosition, secondPosition);
            mRealm.executeTransaction(r -> mEntry.getTasks().move(firstPosition, secondPosition));
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();

        EditText cafe_val = (EditText) mCaffeine.findViewById(R.id.num_val);
        int cafe = Integer.parseInt(String.valueOf(cafe_val.getText()));

        EditText water_val = (EditText) mWater.findViewById(R.id.num_val);
        int water = Integer.parseInt(String.valueOf(water_val.getText()));

        mRealm.beginTransaction();
        mEntry.setCaffeine(cafe);
        mEntry.setWater(water);
        mRealm.commitTransaction();
    }

    private void addCheckboxItem(CheckboxItem item, int pos, boolean doFocus)
    {
        CheckboxItemView bul = new CheckboxItemView(mRealm, this);
        bul.setBackground(mBackDraw);
        bul.bindToItem(item);
        mTaskCont.addDragView(bul, bul.getMoveHandle(), pos);

        if (doFocus) {
            bul.requestFocus();
        }

        bul.setEnterListener(selectionStart ->
        {
            Timber.d("Enter pressed in checkbox item with selection start %d", selectionStart);

            String fullText = item.getText();
            String newText = fullText.substring(0, selectionStart);
            String selText = fullText.substring(selectionStart);

            int loc = mEntry.getTasks().indexOf(item) + 1;

            mRealm.beginTransaction();
            item.setText(newText);
            CheckboxItem newItem = mRealm.createObject(CheckboxItem.class);
            newItem.setText(selText);
            mEntry.getTasks().add(loc, newItem);
            mRealm.commitTransaction();
        });

        bul.setDeleteListener(() ->
        {
            int idx = mEntry.getTasks().indexOf(item);
            if (idx > 0) {
                CheckboxItem prev = mEntry.getTasks().get(idx - 1);
                String ptxt = prev.getText();
                String itxt = item.getText();
                String result = ptxt.isEmpty() ? itxt : itxt.isEmpty() ? ptxt : ptxt + " " + itxt;

                mRealm.executeTransaction(r ->
                {
                    prev.setText(result);
                    item.deleteFromRealm();
                });
            }
        });
    }

    private void addBulletItem(BulletItem item, int position, boolean doFocus) {
        BulletItemView bul = new BulletItemView(mRealm, this);
        bul.setBackground(mBackDraw);
        bul.bindToItem(item);
        mNoteCont.addDragView(bul, bul.getMoveHandle(), position);
        if (doFocus) {
            bul.requestFocus();
        }

        bul.setEnterListener(selectionStart ->
        {
            Timber.d("Enter pressed in bullet item with selection start %d", selectionStart);

            String fullText = item.getText();
            String newText = fullText.substring(0, selectionStart);
            String selText = fullText.substring(selectionStart);

            int loc = mEntry.getNotes().indexOf(item) + 1;

            if (mPrefs.isLocationEnabled()) {
                mRealm.beginTransaction();
                item.setText(newText);
                BulletItem newItem = mRealm.createObject(BulletItem.class);
                newItem = mRealm.createObject(BulletItem.class);
                newItem.setText(selText);
                pullLocationOnce(newItem, loc);
                mRealm.commitTransaction();
            } else {
                mRealm.beginTransaction();
                item.setText(newText);
                BulletItem newItem = mRealm.createObject(BulletItem.class);
                newItem = mRealm.createObject(BulletItem.class);
                newItem.setText(selText);
                mEntry.getNotes().add(loc, newItem);
                mRealm.commitTransaction();
            }

        });

        bul.setDeleteListener(() ->
        {
            int idx = mEntry.getNotes().indexOf(item);
            if (idx > 0) {
                BulletItem prev = mEntry.getNotes().get(idx - 1);
                String ptxt = prev.getText();
                String itxt = item.getText();
                String result = ptxt.isEmpty() ? itxt : itxt.isEmpty() ? ptxt : ptxt + " " + itxt;

                mRealm.executeTransaction(r ->
                {
                    prev.setText(result);
                    item.deleteFromRealm();
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_TAKE_PHOTO && resultCode == RESULT_OK) {
            Timber.d("File path: %s", mPhotoFile.getPath());
            mRealm.executeTransaction(r -> mEntry.setPhotoPath(mPhotoFile.getPath()));
            setPhotoFromEntry();
        }
    }

    /**
     * Displays photo at given path
     * Do not call within a realm transaction
     */
    private void setPhotoFromEntry()
    {
        String photoPath = mEntry.getPhotoPath();
        if (photoPath == null)
        {
            Timber.d("No photo path saved");
            return;
        }

        // We can't use the dimensions of the actual imageview since it's invisible now
        ViewUtils.getDimensions(mNoPhotoCont, (width, height) ->
        {
            Bitmap img = BitmapUtils.decodeSubsampledBitmap(photoPath, width, height);

            if (img == null)
            {
                Timber.d("Photo path saved, but no longer valid: removing path");
                if (mRealm.isInTransaction())
                {
                    Timber.d("Cannot remove path; already inside transaction");
                    return;
                }

                mRealm.executeTransaction(r -> mEntry.setPhotoPath(null));
                return;
            }

            mPhoto.setImageBitmap(img);
            mNoPhotoCont.setVisibility(View.GONE);
            mPhoto.setVisibility(View.VISIBLE);
            mPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // TODO: If we add listeners outside of DailyEntryActivity this could be problematic
        mEntry.getNotes().removeAllChangeListeners();
        mEntry.getTasks().removeAllChangeListeners();

        mRealm.close();
    }


    @Override
    public void onResume() {
        super.onResume();

        if (mPrefs.doShowNotes()) {
            mNoteCont.setVisibility(View.VISIBLE);
            mNote.setVisibility(View.VISIBLE);
        } else {
            mNoteCont.setVisibility(View.GONE);
            mNote.setVisibility(View.GONE);
        }

        if (mPrefs.doShowTasks()) {
            mTaskCont.setVisibility(View.VISIBLE);
            mTask.setVisibility(View.VISIBLE);
        } else {
            mTaskCont.setVisibility(View.GONE);
            mTask.setVisibility(View.GONE);
        }

        if (mPrefs.doShowMood()) {
            mMoodTitle.setVisibility(View.VISIBLE);
            mMoodSpinner.setVisibility(View.VISIBLE);
        } else {
            mMoodTitle.setVisibility(View.GONE);
            mMoodSpinner.setVisibility(View.GONE);
        }

        if (mPrefs.doShowCaffeine())
        {
            mCaffeine.setVisibility(View.VISIBLE);
        }
        else
        {
            mCaffeine.setVisibility(View.GONE);
        }

        if (mPrefs.doShowWater())
        {
            mWater.setVisibility(View.VISIBLE);
        }
        else
        {
            mWater.setVisibility(View.GONE);
        }

        if (mPrefs.doShowSteps())
        {
            mStepCont.setVisibility(View.VISIBLE);
        } else {
            mStepCont.setVisibility(View.GONE);
        }

        Timber.d("Location enabled: %s, Has Location: %s", mPrefs.isLocationEnabled(), mEntry.hasLocation());
        if (mPrefs.isLocationEnabled() && mEntry.hasLocation()) {
            mMap.getView().setVisibility(View.VISIBLE);
        } else {
            mMap.getView().setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        LatLng loc;
        for (int i = 0; i < mEntry.getNotes().size(); i++)
        {
            BulletItem hold = mEntry.getNotes().get(i);
            if ((!Double.isNaN(hold.getEntryLat()) || !Double.isNaN(hold.getEntryLong())) && !hold.getText().equals("")) {
                loc = new LatLng(hold.getEntryLat(), hold.getEntryLong());
                googleMap.addMarker(new MarkerOptions().position(loc)
                        .title(hold.getText()));
            }
        }

        loc = new LatLng(mEntry.getLatitude(), mEntry.getLongitude());
        CameraUpdate cUp = CameraUpdateFactory.newLatLngZoom(loc, 16);

        googleMap.addMarker(new MarkerOptions().position(loc)
                .title(mEntry.getDateFormatted()));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        googleMap.animateCamera(cUp);
    }

    private void pullLocationOnce(BulletItem newItem, int loc) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria gpsConf = new Criteria();
        gpsConf.setAccuracy(Criteria.ACCURACY_FINE);
        gpsConf.setPowerRequirement(Criteria.POWER_MEDIUM);
        gpsConf.setAltitudeRequired(false);
        gpsConf.setSpeedRequired(false);
        gpsConf.setBearingRequired(false);
        gpsConf.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        Timber.d("Called.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(GPS_PROVIDER);
            //The location may not move between creating an entry, and adding a note
            //Want to ensure locations are as accurate as possible
            if (location != null) {
                newItem.setEntryLong(location.getLongitude());
                newItem.setEntryLat(location.getLatitude());
            }
        }

        LocationListener locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                //If the location has changed since we created the entry, we'll update the note's location
                Timber.d("Location Changed: %s.", location);
                mRealm.beginTransaction();
                newItem.setEntryLat(location.getLatitude());
                newItem.setEntryLong(location.getLongitude());
                mEntry.getNotes().add(loc, newItem);
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
                        new MaterialDialog.Builder(DailyEntryActivity.this)
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


    public void incrementValue(View view)
    {
        View parent = (View) view.getParent();
        EditText val = (EditText) parent.findViewById(R.id.num_val);
        val.setText(String.valueOf(Integer.parseInt(String.valueOf(val.getText())) + 1));
    }

    public void decrementValue(View view)
    {
        View parent = (View) view.getParent();
        EditText val = (EditText) parent.findViewById(R.id.num_val);
        int n = Integer.parseInt(String.valueOf(val.getText()));
        if (n > 0)
        {
            val.setText(String.valueOf(Integer.parseInt(String.valueOf(val.getText())) - 1));
        }
    }
}

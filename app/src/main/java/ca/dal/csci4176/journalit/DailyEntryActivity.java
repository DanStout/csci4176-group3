package ca.dal.csci4176.journalit;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jmedeisis.draglinearlayout.DragLinearLayout;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.dal.csci4176.journalit.models.BulletItem;
import ca.dal.csci4176.journalit.models.CheckboxItem;
import ca.dal.csci4176.journalit.models.DailyEntry;
import ca.dal.csci4176.journalit.models.Mood;
import ca.dal.csci4176.journalit.models.MoodItem;
import ca.dal.csci4176.journalit.service.LocationGetter;
import ca.dal.csci4176.journalit.utils.BitmapUtils;
import ca.dal.csci4176.journalit.utils.ViewUtils;
import ca.dal.csci4176.journalit.views.BulletItemView;
import ca.dal.csci4176.journalit.views.CheckboxItemView;
import io.realm.Realm;
import timber.log.Timber;

public class DailyEntryActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private static final DateTimeFormatter mImgFileNameDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd_hhmmssSSS");
    private static final String EXTRA_ENTRY_ID = "entry_id";
    private static final int REQ_TAKE_PHOTO = 1;

    private DailyEntry mEntry;
    private Realm mRealm;
    private File mPhotoFile;
    private Prefs mPrefs;
    private Drawable mBackDraw;
    private LocationGetter mLocGetter;
    private SupportMapFragment mMapFrag;

    /**
     * Notes currently displayed on map, plus one for the entry
     */
    private Map<BulletItem, Marker> mPins = new HashMap<>();

    /**
     * Fake empty bullet item used to show the daily entry's location
     */
    private BulletItem mEntryItem = new BulletItem();

    @BindView(R.id.daily_scrollview)
    ScrollView mScrollView;

    @BindView(R.id.map_hack)
    ImageView mMapHackLayout;

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

    @OnClick(R.id.entry_no_photo_container)
    public void takePhoto()
    {
        Intent in = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try
        {
            mPhotoFile = createImageFile();
        }
        catch (IOException e)
        {
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

        if (in.resolveActivity(getPackageManager()) != null)
        {
            startActivityForResult(in, REQ_TAKE_PHOTO);
        }
        else
        {
            Timber.d("Unable to find activity to take photo");
        }
    }

    public static Intent getIntent(Context ctx, DailyEntry entry)
    {
        Intent in = new Intent(ctx, DailyEntryActivity.class);
        in.putExtra(EXTRA_ENTRY_ID, entry.getKey());
        return in;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_entry);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mLocGetter = new LocationGetter(this);
        mPrefs = new Prefs(this);

        mRealm = Realm.getDefaultInstance();

        // If we don't make a copy of the BG drawable, the UI will go all crazy when we reuse it
        mBackDraw = getWindow().getDecorView().getBackground().getConstantState().newDrawable();

        long id = getIntent().getLongExtra(EXTRA_ENTRY_ID, -1);
        if (id == -1)
        {
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
        displayPhotoFromEntry();
        mTxtSteps.setText(String.valueOf(mEntry.getSteps()));

        mMapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFrag.getMapAsync(this);

        setupOtherSections();
        setupNotesAndTasks();
        enableMapHack();
    }

    /**
     * Adapted from http://stackoverflow.com/a/17317176/2513761
     * Author: Laksh
     * Created: 2013-06-13
     * Accessed: 2017-04-03
     */
    private void enableMapHack()
    {
        mMapHackLayout.setOnTouchListener((v, event) ->
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    mScrollView.requestDisallowInterceptTouchEvent(true);
                    return false;
                case MotionEvent.ACTION_UP:
                    mScrollView.requestDisallowInterceptTouchEvent(false);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    mScrollView.requestDisallowInterceptTouchEvent(true);
                    return false;
                default:
                    return true;
            }
        });
    }

    private void setupOtherSections()
    {
        ((TextView) mCaffeine.findViewById(R.id.title)).setText("Caffeine Servings: ");
        ((EditText) mCaffeine.findViewById(R.id.num_val)).setText(String.valueOf(mEntry.getCaffeine()));

        ((TextView) mWater.findViewById(R.id.title)).setText("Water Servings: ");
        ((EditText) mWater.findViewById(R.id.num_val)).setText(String.valueOf(mEntry.getWater()));

        Timber.d("Found mood: %s", mEntry.getMood());

        Spinner mood_spinner = (Spinner) findViewById(R.id.mood_spinner);
        ArrayAdapter<Mood> mood_adapter = new ArrayAdapter<Mood>(this, android.R.layout.simple_spinner_item, Mood.values());
        mood_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mood_spinner.setAdapter(mood_adapter);

        if (mEntry.getMood() != null)
        {
            int index = mood_adapter.getPosition(mEntry.getMood().getEnum());
            mood_spinner.setSelection(index);
        }

        mood_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                Mood mood = (Mood) parent.getItemAtPosition(position);

                mRealm.beginTransaction();
                MoodItem mItem = mRealm.createObject(MoodItem.class);
                mItem.saveEnum(mood);
                mEntry.setMood(mItem);
                mRealm.commitTransaction();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });
    }

    private void setupNotesAndTasks()
    {
        mEntry.getNotes().addChangeListener((col, changeSet) ->
        {
            for (int pos : changeSet.getInsertions())
            {
                Timber.d("Bullet inserted at %d", pos);
                BulletItem item = col.get(pos);
                addBulletItem(item, pos, true);
            }

            for (int pos : changeSet.getDeletions())
            {
                Timber.d("Bullet %d removed", pos);
                mNoteCont.removeViewAt(pos);
                int next = pos == 0 ? pos + 1 : pos - 1;
                mNoteCont.getChildAt(next).requestFocus();
            }

            if (changeSet.getInsertions().length > 0 || changeSet.getDeletions().length > 0)
            {
                // We have to clear pins immediately before continuing to changeset, because removed bulletitems will be invalid
                clearPins();
                mMapFrag.getMapAsync(this::updatePins);
            }

            for (int pos : changeSet.getChanges())
            {
                Timber.d("Bullet item %d changed", pos);
                BulletItemView v = (BulletItemView) mNoteCont.getChildAt(pos);
                v.updateFromItem();

                BulletItem item = col.get(pos);
                Marker mark = mPins.get(item);
                if (mark != null)
                {
                    mark.setTitle(item.getMarkerText());

                    // This will force it to update the displayed text
                    if (mark.isInfoWindowShown())
                    {
                        mark.showInfoWindow();
                    }
                }
            }
        });

        for (int i = 0; i < mEntry.getNotes().size(); i++)
        {
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
            for (int pos : changeSet.getInsertions())
            {
                Timber.d("Checkbox inserted at %d", pos);
                CheckboxItem item = col.get(pos);
                addCheckboxItem(item, pos, true);
            }

            for (int pos : changeSet.getDeletions())
            {
                Timber.d("Checkbox %d deleted", pos);
                mTaskCont.removeViewAt(pos);
                int next = pos == 0 ? pos + 1 : pos - 1;
                mTaskCont.getChildAt(next).requestFocus();
            }

            for (int pos : changeSet.getChanges())
            {
                Timber.d("Checkbox item %d changed", pos);
                CheckboxItemView v = (CheckboxItemView) mTaskCont.getChildAt(pos);
                v.updateFromItem();
            }
        });

        for (int i = 0; i < mEntry.getTasks().size(); i++)
        {
            CheckboxItem item = mEntry.getTasks().get(i);
            addCheckboxItem(item, i, false);
        }

        mTaskCont.setOnViewSwapListener((firstView, firstPosition, secondView, secondPosition) ->
        {
            Timber.d("Checkbox moved: %d -> %d", firstPosition, secondPosition);
            mRealm.executeTransaction(r -> mEntry.getTasks().move(firstPosition, secondPosition));
        });
    }

    private File createImageFile() throws IOException
    {
        String imgName = "img_" + LocalDateTime.now().format(mImgFileNameDateFormat) + "_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imgName, ".jpg", dir);
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

        if (doFocus)
        {
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
            if (idx > 0)
            {
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

    private void addBulletItem(BulletItem item, int position, boolean doFocus)
    {
        BulletItemView bul = new BulletItemView(mRealm, this);
        bul.setBackground(mBackDraw);
        bul.bindToItem(item);
        mNoteCont.addDragView(bul, bul.getMoveHandle(), position);
        if (doFocus)
        {
            bul.requestFocus();
        }

        bul.setEnterListener(selectionStart ->
        {
            Timber.d("Enter pressed in bullet item with selection start %d", selectionStart);

            String fullText = item.getText();
            String newText = fullText.substring(0, selectionStart);
            String selText = fullText.substring(selectionStart);

            int loc = mEntry.getNotes().indexOf(item) + 1;

            mRealm.beginTransaction();
            item.setText(newText);
            BulletItem newItem = mRealm.createObject(BulletItem.class);
            newItem = mRealm.createObject(BulletItem.class);
            newItem.setText(selText);
            mEntry.getNotes().add(loc, newItem);
            mRealm.commitTransaction();

            saveLocationForNote(newItem);
        });

        bul.setDeleteListener(() ->
        {
            int idx = mEntry.getNotes().indexOf(item);
            if (idx > 0)
            {
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

    private void saveLocationForNote(BulletItem newItem)
    {
        mLocGetter.findLocation(location ->
        {
            if (mRealm.isInTransaction())
            {
                Timber.d("Cannot save location for note!");
                return;
            }

            mRealm.executeTransaction(r ->
            {
                newItem.setEntryLat(location.getLatitude());
                newItem.setEntryLong(location.getLongitude());
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQ_TAKE_PHOTO && resultCode == RESULT_OK)
        {
            Timber.d("File path: %s", mPhotoFile.getPath());
            mRealm.executeTransaction(r -> mEntry.setPhotoPath(mPhotoFile.getPath()));
            displayPhotoFromEntry();
        }
    }

    /**
     * Displays photo at given path
     * Do not call within a realm transaction
     */
    private void displayPhotoFromEntry()
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
    protected void onDestroy()
    {
        super.onDestroy();

        // TODO: If we add listeners outside of DailyEntryActivity this could be problematic
        mEntry.getNotes().removeAllChangeListeners();
        mEntry.getTasks().removeAllChangeListeners();

        mRealm.close();
    }


    @Override
    public void onResume()
    {
        super.onResume();

        if (mPrefs.doShowNotes())
        {
            mNoteCont.setVisibility(View.VISIBLE);
            mNote.setVisibility(View.VISIBLE);
        }
        else
        {
            mNoteCont.setVisibility(View.GONE);
            mNote.setVisibility(View.GONE);
        }

        if (mPrefs.doShowTasks())
        {
            mTaskCont.setVisibility(View.VISIBLE);
            mTask.setVisibility(View.VISIBLE);
        }
        else
        {
            mTaskCont.setVisibility(View.GONE);
            mTask.setVisibility(View.GONE);
        }

        if (mPrefs.doShowMood())
        {
            mMoodTitle.setVisibility(View.VISIBLE);
            mMoodSpinner.setVisibility(View.VISIBLE);
        }
        else
        {
            mMoodTitle.setVisibility(View.GONE);
            mMoodSpinner.setVisibility(View.GONE);
        }

        mCaffeine.setVisibility(mPrefs.doShowCaffeine() ? View.VISIBLE : View.GONE);
        mWater.setVisibility(mPrefs.doShowWater() ? View.VISIBLE : View.GONE);
        mStepCont.setVisibility(mPrefs.doShowSteps() ? View.VISIBLE : View.GONE);

        Timber.d("Location enabled: %s, Has Location: %s", mPrefs.isLocationEnabled(), mEntry.hasLocation());
        int vis = mPrefs.isLocationEnabled() && mEntry.hasLocation() ? View.VISIBLE : View.GONE;
        mMapFrag.getView().setVisibility(vis);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        // We have to wait for the map to load, because you can't zoom to include a set of points until the map has been layed out
        googleMap.setOnMapLoadedCallback(() -> updatePins(googleMap));
    }

    private void clearPins()
    {
        for (Marker mark : mPins.values())
        {
            mark.remove();
        }
        mPins.clear();
    }

    private void updatePins(GoogleMap map)
    {
        for (BulletItem item : mEntry.getNotes())
        {
            if (item.getEntryLat() == 0 || item.getEntryLong() == 0)
            {
                continue;
            }


            MarkerOptions opts = new MarkerOptions()
                    .position(new LatLng(item.getEntryLat(), item.getEntryLong()))
                    .title(item.getMarkerText());
            Marker mark = map.addMarker(opts);
            mPins.put(item, mark);
        }

        LatLng entryLoc = new LatLng(mEntry.getLatitude(), mEntry.getLongitude());
        if (mEntry.hasLocation())
        {
            MarkerOptions opt = new MarkerOptions()
                    .position(entryLoc)
                    .title(mEntry.getDateFormatted());
            Marker mark = map.addMarker(opt);
            mPins.put(mEntryItem, mark);
        }

        CameraUpdate update;

        if (mPins.size() > 1)
        {
            LatLngBounds.Builder bldr = new LatLngBounds.Builder();
            for (Marker mark : mPins.values())
            {
                bldr.include(mark.getPosition());
            }
            LatLngBounds bounds = bldr.build();
            update = CameraUpdateFactory.newLatLngBounds(bounds, 100);
        }
        else
        {
            update = CameraUpdateFactory.newLatLngZoom(entryLoc, 16);
        }

        map.animateCamera(update);
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

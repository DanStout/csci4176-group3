package ca.dal.csci4176.journalit;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import ca.dal.csci4176.journalit.views.BulletItemView;
import ca.dal.csci4176.journalit.views.CheckboxItemView;
import io.realm.Realm;
import timber.log.Timber;

public class DailyEntryActivity extends AppCompatActivity
{
    private static final DateTimeFormatter mImgFileNameDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd_hhmmssSSS");
    private static final String EXTRA_ENTRY_ID = "entry_id";
    private static final int REQ_TAKE_PHOTO = 1;

    private DailyEntry mEntry;
    private Realm mRealm;
    private File mPhotoFile;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.entry_notes_container)
    LinearLayout mNoteCont;

    @BindView(R.id.entry_tasks_container)
    LinearLayout mTaskCont;

    @BindView(R.id.entry_no_photo_container)
    LinearLayout mNoPhotoCont;

    @BindView(R.id.entry_photo)
    ImageView mPhoto;

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

        Uri imgUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", mPhotoFile);
        in.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        ClipData clip = ClipData.newUri(getContentResolver(), "Photo", imgUri);
        in.setClipData(clip);
        in.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        if (in.resolveActivity(getPackageManager()) != null)
        {
            startActivityForResult(in, REQ_TAKE_PHOTO);
        }
    }

    private File createImageFile() throws IOException
    {
        String imgName = "img_" + LocalDateTime.now().format(mImgFileNameDateFormat) + "_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imgName, ".jpg", dir);
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

        mRealm = Realm.getDefaultInstance();

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

        Bitmap photo = BitmapFactory.decodeFile(mEntry.getPhotoPath());
        if (photo != null)
        {
            setPhoto(photo);
        }

        mEntry.getNotes().addChangeListener((col, changeSet) ->
        {
            for (int pos : changeSet.getInsertions())
            {
                BulletItem item = col.get(pos);
                addBulletItem(item, pos, true);
            }

            for (int pos : changeSet.getDeletions())
            {
                mNoteCont.removeViewAt(pos);
                int next = pos == 0 ? pos + 1 : pos - 1;
                mNoteCont.getChildAt(next).requestFocus();
            }

            for(int pos : changeSet.getChanges())
            {
                BulletItemView v = (BulletItemView) mNoteCont.getChildAt(pos);
                v.updateFromItem();
                Timber.d("Bullet item %d changed", pos);
            }
        });

        for (int i = 0; i < mEntry.getNotes().size(); i++)
        {
            BulletItem item = mEntry.getNotes().get(i);
            addBulletItem(item, i, false);
        }

        mEntry.getTasks().addChangeListener((col, changeSet) ->
        {
            for (int pos : changeSet.getInsertions())
            {
                CheckboxItem item = col.get(pos);
                addCheckboxItem(item, pos, true);
            }

            for (int pos : changeSet.getDeletions())
            {
                mTaskCont.removeViewAt(pos);
                int next = pos == 0 ? pos + 1 : pos - 1;
                mTaskCont.getChildAt(next).requestFocus();
            }

            for(int pos : changeSet.getChanges())
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
    }

    private void addCheckboxItem(CheckboxItem item, int pos, boolean doFocus)
    {
        CheckboxItemView bul = new CheckboxItemView(mRealm, this);
        bul.bindToItem(item);
        mTaskCont.addView(bul, pos);
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
        bul.bindToItem(item);
        mNoteCont.addView(bul, position);
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
            newItem.setText(selText);
            mEntry.getNotes().add(loc, newItem);
            mRealm.commitTransaction();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQ_TAKE_PHOTO && resultCode == RESULT_OK)
        {
            Timber.d("File path: %s", mPhotoFile.getPath());
            mRealm.executeTransaction(r -> mEntry.setPhotoPath(mPhotoFile.getPath()));
            Bitmap img = BitmapFactory.decodeFile(mPhotoFile.getPath());
            setPhoto(img);
        }
    }

    private void setPhoto(Bitmap bitmap)
    {
        mPhoto.setImageBitmap(bitmap);
        mNoPhotoCont.setVisibility(View.GONE);
        mPhoto.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mRealm.close();
    }
}

package ca.dal.csci4176.journalit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.dal.csci4176.journalit.models.BulletItem;
import ca.dal.csci4176.journalit.models.CheckboxItem;
import ca.dal.csci4176.journalit.models.DailyEntry;
import ca.dal.csci4176.journalit.views.BulletItemView;
import ca.dal.csci4176.journalit.views.CheckboxItemView;
import io.realm.Realm;
import timber.log.Timber;

public class DailyEntryActivity extends AppCompatActivity
{
    private static final String EXTRA_ENTRY_ID = "entry_id";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.entry_notes_container)
    LinearLayout mNoteCont;

    @BindView(R.id.entry_tasks_container)
    LinearLayout mTaskCont;

    private DailyEntry mEntry;

    private Realm mRealm;

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
        }

        mEntry = mRealm
                .where(DailyEntry.class)
                .equalTo("key", id)
                .findFirst();

        Timber.d("Found entry: %s", mEntry);
        setTitle(mEntry.getDateFormatted());

        for (BulletItem item : mEntry.getNotes())
        {
            BulletItemView bul = new BulletItemView(mRealm, this);
            bul.bindToItem(item);
            mNoteCont.addView(bul);

            bul.setEnterListener((selectionStart) -> handleBulletItemEnter(item, selectionStart));
            bul.setDeleteListener(() ->
            {
                if (mNoteCont.indexOfChild(bul) > 0)
                {
                    mNoteCont.removeView(bul);
                    mRealm.executeTransaction(r -> item.deleteFromRealm());
                }
            });
        }

        for (CheckboxItem item : mEntry.getTasks())
        {
            CheckboxItemView chk = new CheckboxItemView(mRealm, this);
            chk.bindToItem(item);
            mTaskCont.addView(chk);

            chk.setEnterListener((selStart -> handleCheckboxItemEnter(item, selStart)));
            chk.setDeleteListener(() ->
            {
                if (mTaskCont.indexOfChild(chk) > 0)
                {
                    mTaskCont.removeView(chk);
                    mRealm.executeTransaction(r -> item.deleteFromRealm());
                }
            });
        }
    }

    private void handleCheckboxItemEnter(CheckboxItem item, int selStart)
    {
        Timber.d("Enter pressed in checkbox item with selection start %d", selStart);

        String fullText = item.getText();
        String newText = fullText.substring(0, selStart);
        String selText = fullText.substring(selStart);

        int loc = mEntry.getTasks().indexOf(item) + 1;

        mRealm.beginTransaction();

        item.setText(newText);
        CheckboxItem newItem = mRealm.createObject(CheckboxItem.class);
        newItem.setText(selText);
        mEntry.getTasks().add(loc, newItem);

        mRealm.commitTransaction();

        CheckboxItemView newBul = new CheckboxItemView(mRealm, this);
        newBul.bindToItem(newItem);
        mTaskCont.addView(newBul, loc);
        newBul.requestFocus();

        newBul.setEnterListener(sel -> handleCheckboxItemEnter(newItem, sel));

        newBul.setDeleteListener(() ->
        {
            if (mTaskCont.indexOfChild(newBul) > 0)
            {
                mTaskCont.removeView(newBul);
                mRealm.executeTransaction(r -> item.deleteFromRealm());
            }
        });
    }

    private void handleBulletItemEnter(BulletItem item, int selectionStart)
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

        BulletItemView newBul = new BulletItemView(mRealm, this);
        newBul.bindToItem(newItem);
        mNoteCont.addView(newBul, loc);
        newBul.requestFocus();

        newBul.setEnterListener(sel -> handleBulletItemEnter(newItem, sel));

        newBul.setDeleteListener(() ->
        {
            if (mNoteCont.indexOfChild(newBul) > 0)
            {
                mNoteCont.removeView(newBul);
                mRealm.executeTransaction(r -> item.deleteFromRealm());
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mRealm.close();
    }
}

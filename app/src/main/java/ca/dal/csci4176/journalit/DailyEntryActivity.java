package ca.dal.csci4176.journalit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

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

    private Realm mRealm;
    private List<CheckboxItemView> mCheckboxes = new ArrayList<>();
    private List<BulletItemView> mBulletItemViews = new ArrayList<>();

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

        DailyEntry entry = mRealm
                .where(DailyEntry.class)
                .equalTo("key", id)
                .findFirst();

        Timber.d("Found entry: %s", entry);
        setTitle(entry.getDateFormatted());

        for (BulletItem item : entry.getNotes())
        {
            BulletItemView bul = new BulletItemView(this);
            bul.bindToItem(item);
            mNoteCont.addView(bul);
            mBulletItemViews.add(bul);
        }

        for (CheckboxItem item : entry.getTasks())
        {
            CheckboxItemView chk = new CheckboxItemView(this);
            chk.bindToItem(item);
            mTaskCont.addView(chk);
            mCheckboxes.add(chk);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        mRealm.executeTransaction(r ->
        {
            for(CheckboxItemView v : mCheckboxes)
            {
                v.updateItem();
            }

            for(BulletItemView v : mBulletItemViews)
            {
                v.updateItem();
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

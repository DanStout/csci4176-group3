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

        mEntry.getNotes().addChangeListener((col, changeSet) ->
        {
            int[] inserts = changeSet.getInsertions();

            for (int pos : inserts)
            {
                BulletItem item = col.get(pos);
                addBulletItem(item, pos, true);
            }

            int[] deletes = changeSet.getDeletions();
            for (int pos : deletes)
            {
                mNoteCont.removeViewAt(pos);
                int next = pos == 0 ? pos + 1 : pos - 1;
                mNoteCont.getChildAt(next).requestFocus();
            }
        });

        for (int i = 0; i < mEntry.getNotes().size(); i++)
        {
            BulletItem item = mEntry.getNotes().get(i);
            addBulletItem(item, i, false);
        }

        mEntry.getTasks().addChangeListener((col, changeSet) ->
        {
            int[] inserts = changeSet.getInsertions();
            for (int pos : inserts)
            {
                CheckboxItem item = col.get(pos);
                addCheckboxItem(item, pos, true);
            }

            int[] deletes = changeSet.getDeletions();
            for (int pos : deletes)
            {
                mTaskCont.removeViewAt(pos);
                int next = pos == 0 ? pos + 1 : pos - 1;
                mTaskCont.getChildAt(next).requestFocus();
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
    protected void onDestroy()
    {
        super.onDestroy();
        mRealm.close();
    }
}

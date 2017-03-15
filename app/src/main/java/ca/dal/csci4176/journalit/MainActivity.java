package ca.dal.csci4176.journalit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.threeten.bp.LocalDate;

import ca.dal.csci4176.journalit.models.Entry;
import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Entry ent = new Entry();
        ent.setDate(LocalDate.now());
        ent.setText("This is the entry text");

        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(r -> r.copyToRealm(ent));

        RealmResults<Entry> entries = realm
                .where(Entry.class)
                .findAll();

        Timber.d("Found %d entries", entries.size());

        for (Entry e : entries)
        {
            Timber.d("%s", e);
        }

    }
}

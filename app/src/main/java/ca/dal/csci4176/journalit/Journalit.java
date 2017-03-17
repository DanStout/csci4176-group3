package ca.dal.csci4176.journalit;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

import ca.dal.csci4176.journalit.models.DailyEntry;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import timber.log.Timber;

public class Journalit extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        Realm.init(this);
        AndroidThreeTen.init(this);

        if (BuildConfig.DEBUG)
        {
            Timber.plant(new Timber.DebugTree());
            Timber.d("Timber Debug Logging Enabled");
        }

        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm.setDefaultConfiguration(config);

        Realm realm = Realm.getDefaultInstance();

        List<DailyEntry> entries = new ArrayList<>();
        LocalDate day = LocalDate.now();
        for(int i = 0; i < 7; i++)
        {
            DailyEntry ent = new DailyEntry();
            ent.setDate(day);
            ent.setText(String.format("This is the entry text for %s", day));
            day = day.minusDays(1);
            entries.add(ent);
        }

        realm.executeTransaction(r -> r.deleteAll());
        realm.executeTransaction(r -> r.copyToRealm(entries));
    }
}

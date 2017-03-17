package ca.dal.csci4176.journalit;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;

import ca.dal.csci4176.journalit.models.BulletItem;
import ca.dal.csci4176.journalit.models.CheckboxItem;
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
        Realm.deleteRealm(config);
        Realm realm = Realm.getDefaultInstance();



        realm.beginTransaction();

        LocalDate day = LocalDate.now();
        for (int i = 0; i < 7; i++)
        {
            DailyEntry ent = new DailyEntry();
            ent.setDate(day);
            ent.setText(String.format("This is the entry text for %s", day));
            day = day.minusDays(1);

            ent = realm.copyToRealmOrUpdate(ent);

            BulletItem note1 = new BulletItem("I drank 1500 litres of coffee today");
            BulletItem note2 = new BulletItem("I saw my nemesis on the bus");

            CheckboxItem task1 = new CheckboxItem("Take out the garbage", false);
            CheckboxItem task2 = new CheckboxItem("Learn to speak Korean", false);

            ent.getNotes().add(note1);
            ent.getNotes().add(note2);

            ent.getTasks().add(task1);
            ent.getTasks().add(task2);
        }

        realm.commitTransaction();

    }
}

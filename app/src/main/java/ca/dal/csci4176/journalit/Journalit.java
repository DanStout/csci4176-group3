package ca.dal.csci4176.journalit;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

import ca.dal.csci4176.journalit.service.DailyStepService;
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
//        Realm.deleteRealm(config);

        DailyStepService.enableChecking(this);
    }
}

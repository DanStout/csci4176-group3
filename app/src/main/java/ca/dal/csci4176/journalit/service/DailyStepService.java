package ca.dal.csci4176.journalit.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DailyTotalResult;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;

import java.util.concurrent.TimeUnit;

import ca.dal.csci4176.journalit.models.DailyEntry;
import io.realm.Realm;
import timber.log.Timber;

public class DailyStepService extends IntentService
{
    public DailyStepService()
    {
        super(DailyStepService.class.getSimpleName());
    }

    public static void enableChecking(Context ctx)
    {
        Intent in = new Intent(ctx, DailyStepService.class);

        // 0 for request code
        PendingIntent pin = PendingIntent.getService(ctx, 0, in, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        LocalDateTime elevenFiftyNine = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT.minusMinutes(1));
        long millisTriggerAt = elevenFiftyNine.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        mgr.setInexactRepeating(AlarmManager.RTC, millisTriggerAt, AlarmManager.INTERVAL_DAY, pin);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        GoogleApiClient client = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .build();


        Realm realm = Realm.getDefaultInstance();
        try
        {
            ConnectionResult result = client.blockingConnect(15, TimeUnit.SECONDS);
            if (!result.isSuccess())
            {
                Timber.d("BlockingConnect failed: '%s'", result.getErrorMessage());
                return;
            }

            DailyEntry today = realm.where(DailyEntry.class).equalTo("key", DailyEntry.getKeyOfToday()).findFirst();
            Timber.d("Found today's entry: %s", today);
            if (today == null)
            {
                Timber.w("Today is null!");
                return;
            }

            PendingResult<DailyTotalResult> pend = Fitness.HistoryApi.readDailyTotal(client, DataType.TYPE_STEP_COUNT_DELTA);
            pend.setResultCallback(dailyTotalResult ->
            {
                if (!dailyTotalResult.getStatus().isSuccess())
                {
                    Timber.d("readDailyTotal request failed");
                    return;
                }

                DataSet set = dailyTotalResult.getTotal();
                int steps = set.isEmpty() ? 0 : set.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                Timber.d("Saving %d steps for today's entry", steps);
                realm.executeTransaction(r -> today.setSteps(steps));
            });
        }
        finally
        {
            client.disconnect();
            realm.close();
        }
    }
}

package ca.dal.csci4176.journalit.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Just in case the user restarts the phone but doesn't open Journalit, we still want to perform the daily step check
 */
public class StartupBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        DailyStepService.enableChecking(context);
    }
}

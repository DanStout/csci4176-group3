package ca.dal.csci4176.journalit;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs
{
    private static final String PREF_USER_DECLINED_GOOGLE_ACC = "userdeclinedgoogacc";
    private static final String PREF_SIGNED_IN = "signedin";
    private static final String PREF_ENABLE_LOCATION = "location";
    private static final String PREF_SHOW_NOTES = "shownotes";
    private static final String PREF_SHOW_TASKS = "showtasks";
    private static final String PREF_SHOW_MOOD = "showmood";
    private static final String PREF_SHOW_STEPS = "showsteps";
    private static final String PREF_SHOW_CAFFEINE = "showcaffeine";
    private static final String PREF_SHOW_WATER = "showwater";

    private final SharedPreferences mPrefs;

    public Prefs(Context ctx)
    {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public boolean didUserDeclineGoogleAcc()
    {
        return mPrefs.getBoolean(PREF_USER_DECLINED_GOOGLE_ACC, false);
    }

    public void setDidUserDeclineGoogleAcc(boolean value)
    {
        mPrefs.edit().putBoolean(PREF_USER_DECLINED_GOOGLE_ACC, value).apply();
    }

    public boolean isSignedIn()
    {
        return mPrefs.getBoolean(PREF_SIGNED_IN, false);
    }

    public void setSignedIn(boolean value)
    {
        mPrefs.edit().putBoolean(PREF_SIGNED_IN, value).apply();
    }


    public boolean isLocationEnabled()
    {
        return mPrefs.getBoolean(PREF_ENABLE_LOCATION, true);
    }

    public void setLocationEnabled(boolean isEnabled)
    {
        mPrefs.edit().putBoolean(PREF_ENABLE_LOCATION, isEnabled).apply();
    }

    public boolean doShowTasks()
    {
        return mPrefs.getBoolean(PREF_SHOW_TASKS, true);
    }

    public boolean doShowNotes()
    {
        return mPrefs.getBoolean(PREF_SHOW_NOTES, true);
    }

    public void setShowTasks(boolean doShow)
    {
        mPrefs.edit().putBoolean(PREF_SHOW_TASKS, doShow).apply();
    }

    public void setShowNotes(boolean doShow)
    {
        mPrefs.edit().putBoolean(PREF_SHOW_NOTES, doShow).apply();
    }

    public boolean doShowSteps()
    {
        return mPrefs.getBoolean(PREF_SHOW_STEPS, true);
    }

    public void setShowsteps(boolean doShow)
    {
        mPrefs.edit().putBoolean(PREF_SHOW_STEPS, doShow).apply();
    }

    public boolean doShowMood()
    {
        return mPrefs.getBoolean(PREF_SHOW_MOOD, true);
    }

    public void setShowMood(boolean doShow)
    {
        mPrefs.edit().putBoolean(PREF_SHOW_MOOD, doShow).apply();
    }

    public boolean doShowCaffeine()
    {
        return mPrefs.getBoolean(PREF_SHOW_CAFFEINE, true);
    }

    public void setShowCaffeine(boolean doShow)
    {
        mPrefs.edit().putBoolean(PREF_SHOW_CAFFEINE, doShow).apply();
    }

    public boolean doShowWater()
    {
        return mPrefs.getBoolean(PREF_SHOW_WATER, true);
    }

    public void setShowWater(boolean doShow)
    {
        mPrefs.edit().putBoolean(PREF_SHOW_WATER, doShow).apply();
    }
}

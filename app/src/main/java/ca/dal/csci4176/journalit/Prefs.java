package ca.dal.csci4176.journalit;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs
{
    private static final String PREF_USER_DECLINED_GOOGLE_ACC = "userdeclinedgoogacc";
    private static final String PREF_SIGNED_IN = "signedin";
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
}

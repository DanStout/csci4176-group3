package ca.dal.csci4176.journalit.service;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.afollestad.materialdialogs.MaterialDialog;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import ca.dal.csci4176.journalit.R;
import timber.log.Timber;

public class LocationGetter
{
    private final Activity mCtx;
    private LocationManager mLocMgr;
    private Criteria mCriteria;

    public LocationGetter(Activity ctx)
    {
        mCtx = ctx;
        mLocMgr = (LocationManager) mCtx.getSystemService(Context.LOCATION_SERVICE);
        mCriteria = new Criteria();
        mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        mCriteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        mCriteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);

    }

    public interface LocationFoundCallback
    {
        void onLocationFound(Location location);
    }

    /**
     * Execute a callback once a location has been found. Callback might never execute if the user
     * declines permission or the location does not get updated for some reason
     */
    public void findLocation(LocationFoundCallback callback)
    {
        Dexter.withActivity(mCtx)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener()
                {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response)
                    {
                        Timber.d("Permission granted");
                        //noinspection MissingPermission
                        findLocationWithPermission(callback);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response)
                    {
                        Timber.d("Permission denied");
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token)
                    {
                        Timber.d("Showing rationale..");
                        new MaterialDialog.Builder(mCtx)
                                .content(R.string.rationale_location)
                                .positiveText(R.string.OK)
                                .negativeText(R.string.no_thanks)
                                .onPositive((dialog, which) ->
                                {
                                    Timber.d("Continuing with permission request");
                                    token.continuePermissionRequest();
                                })
                                .onNegative((dialog, which) ->
                                {
                                    Timber.d("Cancelling permission request");
                                    token.cancelPermissionRequest();
                                })
                                .show();
                    }
                })
                .check();
    }

    private void findLocationWithPermission(LocationFoundCallback callback)
    {
        // First we try to see if we already know the location
        Location lastLoc = mLocMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLoc == null)
        {
            lastLoc = mLocMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (lastLoc != null)
        {
            callback.onLocationFound(lastLoc);
            return;
        }

        // Otherwise we'll fetch it

        LocationListener locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                Timber.d("Location Changed to: %s", location);
                callback.onLocationFound(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras)
            {
                Timber.d("Status Changed to: %d", status);
            }

            @Override
            public void onProviderEnabled(String provider)
            {
                Timber.d("Provider enabled: %s", provider);
            }

            @Override
            public void onProviderDisabled(String provider)
            {
                Timber.d("Provider disabled: %s", provider);
            }
        };

        mLocMgr.requestSingleUpdate(mCriteria, locationListener, null);
    }
}

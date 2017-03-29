package ca.dal.csci4176.journalit.models;

import io.realm.RealmObject;
import timber.log.Timber;
import com.google.android.gms.maps.model.LatLng;

public class BulletItem extends RealmObject implements TextItem
{
    private String text;
    private double latitude, longitude;

    public BulletItem(String text)
    {
        setText(text);
    }

    public BulletItem(String text, LatLng loc)
    {
        setText(text);
        setEntryLat(loc.latitude);
        setEntryLong(loc.longitude);
    }

    /**
     * Required by Realm
     */
    public BulletItem()
    {

    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        textLastChangedAt = System.nanoTime();
        Timber.d("TextLastChangedAt: %s", textLastChangedAt);
        this.text = text;
    }

    public double getEntryLat() {
        return latitude;
    }

    public void setEntryLat(double lat) {
        this.latitude = lat;
        Timber.d("Latitude changed: %s", lat);
    }

    public double getEntryLong() {
        return longitude;
    }

    public void setEntryLong(double lon) {
        this.longitude = lon;
        Timber.d("Longitude changed: %s", lon);
    }

    /**
     * Returns the System.nanotime(); value that the setText() was last called at
     */
    private long textLastChangedAt;

    public long getTextLastChangedAt()
    {
        return textLastChangedAt;
    }

    @Override
    public String toString()
    {
        return "BulletItem{" +
                "text='" + text + '\'' +
                '}';
    }
}
package ca.dal.csci4176.journalit.models;

import ca.dal.csci4176.journalit.utils.DateUtils;
import io.realm.RealmObject;
import timber.log.Timber;

import com.google.android.gms.maps.model.LatLng;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

public class BulletItem extends RealmObject implements TextItem
{
    private static final DateTimeFormatter mEntryTimeFormat = DateTimeFormatter.ofPattern("hh:mm a");
    private String text;
    private double latitude, longitude;
    private long createdAt;

    public BulletItem(String text)
    {
        this();
        setText(text);
    }

    public BulletItem(String text, LatLng loc)
    {
        this();
        setText(text);
        setEntryLat(loc.latitude);
        setEntryLong(loc.longitude);
    }

    /**
     * Required by Realm
     */
    public BulletItem()
    {
        this.createdAt = System.currentTimeMillis();
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

    public double getEntryLat()
    {
        return latitude;
    }

    public void setEntryLat(double lat)
    {
        this.latitude = lat;
        Timber.d("Latitude changed: %s", lat);
    }

    public double getEntryLong()
    {
        return longitude;
    }

    public String getMarkerText()
    {
        LocalTime created = DateUtils.toLocalTime(getCreatedAt());
        String time = mEntryTimeFormat.format(created);
        return time + ": " + getText();
    }

    public long getCreatedAt()
    {
        return createdAt;
    }

    public void setEntryLong(double lon)
    {
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
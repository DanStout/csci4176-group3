package ca.dal.csci4176.journalit.models;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Date;

import ca.dal.csci4176.journalit.utils.DateUtils;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class DailyEntry extends RealmObject
{
    private static DateTimeFormatter sDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Required
    private Date date;

    @PrimaryKey
    private long key;

    private String photoPath;

    private RealmList<BulletItem> notes;

    private RealmList<CheckboxItem> tasks;

    public static long getKeyOfToday()
    {
        LocalDate now = LocalDate.now();
        Date nowDate = DateUtils.toDate(now);
        return getKey(nowDate);
    }

    private static long getKey(Date date)
    {
        return date.getTime();
    }

    public RealmList<BulletItem> getNotes()
    {
        return notes;
    }

    public String getPhotoPath()
    {
        return photoPath;
    }

    public void setPhotoPath(String photoPath)
    {
        this.photoPath = photoPath;
    }

    public RealmList<CheckboxItem> getTasks()
    {
        return tasks;
    }

    public LocalDate getDate()
    {
        return DateUtils.toLocalDate(date);
    }

    public String getDateFormatted()
    {
        return getDate().format(sDateFormat);
    }

    public long getKey()
    {
        return key;
    }

    public void setDate(LocalDate date)
    {
        this.date = DateUtils.toDate(date);
        this.key = getKey(this.date);
    }

    @Override
    public String toString()
    {
        return "DailyEntry{" +
                "date=" + date +
                ", key=" + key +
                ", notes=" + notes +
                ", tasks=" + tasks +
                '}';
    }
}

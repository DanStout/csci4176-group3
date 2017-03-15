package ca.dal.csci4176.journalit.models;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Entry extends RealmObject
{
    @Required
    private Date date;

    @PrimaryKey
    private long key;

    private String text;

    public LocalDate getDate()
    {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public long getKey()
    {
        return key;
    }

    public void setDate(LocalDate date)
    {
        this.date = new Date(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        this.key = this.date.getTime();
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    @Override
    public String toString()
    {
        return "Entry{" +
                "date=" + date +
                ", text='" + text + '\'' +
                '}';
    }
}

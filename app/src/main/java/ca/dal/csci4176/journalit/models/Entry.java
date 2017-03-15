package ca.dal.csci4176.journalit.models;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Required;

public class Entry extends RealmObject
{
    @Required
    private Date date;

    private String text;

    public LocalDate getDate()
    {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public void setDate(LocalDate date)
    {
        this.date = new Date(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }
}

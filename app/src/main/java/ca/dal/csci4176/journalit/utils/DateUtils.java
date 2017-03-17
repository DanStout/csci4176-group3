package ca.dal.csci4176.journalit.utils;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.Date;

public class DateUtils
{
    public static Date toDate(LocalDate date)
    {
        return new Date(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    public static LocalDate toLocalDate(Date date)
    {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }


}

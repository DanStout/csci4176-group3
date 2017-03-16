package ca.dal.csci4176.journalit;

import java.util.Calendar;

/**
 * Created by WZ on 2017/3/16.
 */

public class Card {
    String date;
    String note1;
    String note2;
    int photoId;

    Card(String note1, String note2, int photoId) {
        Calendar c = Calendar.getInstance();
        this.date = "" + c.get(Calendar.YEAR) + " " + c.get(Calendar.MONTH) + " " + c.get(Calendar.DATE);
        this.note1 = note1;
        this.note2 = note2;
        this.photoId = photoId;
    }
}

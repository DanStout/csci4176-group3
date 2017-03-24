package ca.dal.csci4176.journalit.models;

import io.realm.RealmObject;


public class MoodItem extends RealmObject {

    private String enumDescription;

    public void saveEnum(Mood val) {
        this.enumDescription = val.toString();
    }

    public Mood getEnum() {
        return (enumDescription != null) ? Mood.valueOf(enumDescription) : null;
    }
}

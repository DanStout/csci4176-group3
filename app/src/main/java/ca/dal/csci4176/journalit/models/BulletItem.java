package ca.dal.csci4176.journalit.models;

import io.realm.RealmObject;

public class BulletItem extends RealmObject
{
    public BulletItem(String text)
    {
        setText(text);
    }

    /**
     * Required by Realm
     */
    public BulletItem()
    {

    }

    private String text;

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
        return "BulletItem{" +
                "text='" + text + '\'' +
                '}';
    }
}

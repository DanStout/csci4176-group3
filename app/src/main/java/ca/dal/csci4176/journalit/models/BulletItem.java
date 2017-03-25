package ca.dal.csci4176.journalit.models;

import io.realm.RealmObject;
import timber.log.Timber;

public class BulletItem extends RealmObject implements TextItem
{
    private String text;

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

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        textLastChangedAt = System.nanoTime();
        Timber.d("TextLastChangedAt: %d", textLastChangedAt);
        this.text = text;
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

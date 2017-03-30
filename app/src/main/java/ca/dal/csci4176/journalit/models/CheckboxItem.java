package ca.dal.csci4176.journalit.models;

import io.realm.RealmObject;
import timber.log.Timber;

public class CheckboxItem extends RealmObject implements TextItem
{
    private String text;
    private boolean isChecked;

    public CheckboxItem(String text, boolean isChecked)
    {
        setText(text);
        this.isChecked = isChecked;
    }

    /**
     * Required by Realm
     */
    public CheckboxItem()
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

    public boolean isChecked()
    {
        return isChecked;
    }

    public void setChecked(boolean checked)
    {
        isChecked = checked;
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
        return "CheckboxItem{" +
                "text='" + text + '\'' +
                ", isChecked=" + isChecked +
                '}';
    }
}

package ca.dal.csci4176.journalit.models;

import io.realm.RealmObject;

public class CheckboxItem extends RealmObject
{
    private String text;
    private boolean isChecked;

    public CheckboxItem(String text, boolean isChecked)
    {
        this.text = text;
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

    @Override
    public String toString()
    {
        return "CheckboxItem{" +
                "text='" + text + '\'' +
                ", isChecked=" + isChecked +
                '}';
    }
}

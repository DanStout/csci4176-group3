package ca.dal.csci4176.journalit.views;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.dal.csci4176.journalit.R;
import ca.dal.csci4176.journalit.models.CheckboxItem;
import io.realm.Realm;
import timber.log.Timber;

public class CheckboxItemView extends BaseItemView<CheckboxItem>
{
    @BindView(R.id.checkbox_item_checkbox)
    CheckBox mCheck;

    @BindView(R.id.item_move)
    ImageView mMove;

    private CheckboxItem mItem;
    private Realm mRealm;

    public CheckboxItemView(Realm realm, Context context)
    {
        super(context, R.layout.checkbox_item, R.id.checkbox_item_edit);
        mRealm = realm;
    }

    protected void init()
    {
        ButterKnife.bind(this);
        mCheck.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            mRealm.executeTransaction(r -> mItem.setChecked(isChecked));
            updateStrikethrough(isChecked);
        });
    }

    public View getMoveHandle()
    {
        return mMove;
    }

    @Override
    protected void saveText(String txt)
    {
        if (!mItem.isValid())
        {
            return;
        }

        Timber.d("Saving text to checkbox item: '%s'", txt);
        mRealm.executeTransaction(r -> mItem.setText(txt));
    }

    private void updateStrikethrough(boolean isChecked)
    {
        int flags = mEditTxt.getPaintFlags();
        int strike = Paint.STRIKE_THRU_TEXT_FLAG;

        if (isChecked)
        {
            mEditTxt.setPaintFlags(flags | strike);
        }
        else
        {
            mEditTxt.setPaintFlags(flags & ~strike);
        }
    }

    @Override
    public void bindToItem(CheckboxItem item)
    {
        Timber.d("Binding to checkbox item");
        mItem = item;
        updateFromItem();
    }

    @Override
    public void updateFromItem()
    {
        Timber.d("Updating checkbox item");
        if (!mItem.isValid())
        {
            return;
        }

        mCheck.setChecked(mItem.isChecked());
        updateStrikethrough(mItem.isChecked());

        String saved = mItem.getText();
        String ui = mEditTxt.getText().toString();
        boolean isSame = saved.equals(ui);
        boolean savedValueChangedMoreRecently = mItem.getTextLastChangedAt() > mEditTextLastChangedAt;
        Timber.d("TextLastChangedAt: %d, EditTextLastChangedAt: %d", mItem.getTextLastChangedAt(), mEditTextLastChangedAt);
        Timber.d("Checkbox item changed. Saved text: '%s', UI text: '%s', is same: %s, saved changed more recently: %s", saved, ui, isSame, savedValueChangedMoreRecently);

        if (!isSame && savedValueChangedMoreRecently)
        {
            Timber.d("Setting text!");
            mEditTxt.setText(saved);
        }
    }

    /**
     * By default, the checkbox will get the focus. Instead we want the EditText to be focused
     */
    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect)
    {
        mEditTxt.requestFocus();
        return true;
    }
}

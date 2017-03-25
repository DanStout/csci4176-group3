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
    protected void saveText()
    {
        if (!mItem.isValid())
        {
            return;
        }

        String txt = mEditTxt.getText().toString();
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
        if (!mItem.isValid())
        {
            return;
        }

        Timber.d("Updating checkbox item");
        mCheck.setChecked(mItem.isChecked());

        if (!mEditTxt.getText().toString().equals(mItem.getText()))
        {
            callingSetText = true;
            mEditTxt.setText(mItem.getText());
            callingSetText = false;
        }

        updateStrikethrough(mItem.isChecked());
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

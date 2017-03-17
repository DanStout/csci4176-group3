package ca.dal.csci4176.journalit.views;

import android.content.Context;
import android.graphics.Paint;
import android.widget.CheckBox;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.dal.csci4176.journalit.R;
import ca.dal.csci4176.journalit.models.CheckboxItem;
import io.realm.Realm;

public class CheckboxItemView extends BaseItemView<CheckboxItem>
{
    @BindView(R.id.checkbox_item_checkbox)
    CheckBox mCheck;

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

    @Override
    protected void saveText()
    {
        if (!mItem.isValid())
        {
            return;
        }

        mRealm.executeTransaction(r -> mItem.setText(mEditTxt.getText().toString()));
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

    public void bindToItem(CheckboxItem item)
    {
        mItem = item;
        updateForItem();
        mItem.addChangeListener(element -> updateForItem());
    }

    private void updateForItem()
    {
        if (!mItem.isValid())
        {
            return;
        }

        mCheck.setChecked(mItem.isChecked());

        if (!mEditTxt.getText().toString().equals(mItem.getText()))
        {
            callingSetText = true;
            mEditTxt.setText(mItem.getText());
            callingSetText = false;
        }

        updateStrikethrough(mItem.isChecked());
    }
}

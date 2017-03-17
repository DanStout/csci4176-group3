package ca.dal.csci4176.journalit.views;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.dal.csci4176.journalit.R;
import ca.dal.csci4176.journalit.models.CheckboxItem;

public class CheckboxItemView extends LinearLayout
{
    @BindView(R.id.checkbox_item_edit)
    EditText mEditTxt;

    @BindView(R.id.checkbox_item_checkbox)
    CheckBox mCheck;

    private CheckboxItem mItem;

    public CheckboxItemView(Context context)
    {
        super(context);
        init();
    }

    public CheckboxItemView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public CheckboxItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        inflate(getContext(), R.layout.checkbox_item, this);
        ButterKnife.bind(this);

        mCheck.setOnCheckedChangeListener((buttonView, isChecked) -> updateStrikethrough(isChecked));
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
        mCheck.setChecked(item.isChecked());
        mEditTxt.setText(item.getText());
        updateStrikethrough(item.isChecked());
        mItem = item;
    }

    /**
     * May only be called within a Realm transaction!
     */
    public void updateItem()
    {
        mItem.setChecked(mCheck.isChecked());
        mItem.setText(mEditTxt.getText().toString());
    }
}

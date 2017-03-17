package ca.dal.csci4176.journalit.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.RelativeLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.dal.csci4176.journalit.R;
import ca.dal.csci4176.journalit.models.BulletItem;

public class BulletItemView extends RelativeLayout
{
    @BindView(R.id.bullet_item_edit)
    EditText mEditTxt;

    private BulletItem mItem;

    public BulletItemView(Context context)
    {
        super(context);
        init();
    }

    public BulletItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public BulletItemView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        inflate(getContext(), R.layout.bullet_item, this);
        ButterKnife.bind(this);
    }

    public void bindToItem(BulletItem item)
    {
        mEditTxt.setText(item.getText());
        mItem = item;
    }

    /**
     * May only be called within a Realm transaction!
     */
    public void updateItem()
    {
        mItem.setText(mEditTxt.getText().toString());
    }
}

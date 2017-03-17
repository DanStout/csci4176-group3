package ca.dal.csci4176.journalit.views;

import android.content.Context;

import butterknife.ButterKnife;
import ca.dal.csci4176.journalit.R;
import ca.dal.csci4176.journalit.models.BulletItem;
import io.realm.Realm;

public class BulletItemView extends BaseItemView<BulletItem>
{
    private BulletItem mItem;
    private Realm mRealm;

    public BulletItemView(Realm realm, Context context)
    {
        super(context, R.layout.bullet_item, R.id.bullet_item_edit);
        mRealm = realm;
    }

    protected void init()
    {
        ButterKnife.bind(this);
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

    public void bindToItem(BulletItem item)
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

        if (!mEditTxt.getText().toString().equals(mItem.getText()))
        {
            callingSetText = true;
            mEditTxt.setText(mItem.getText());
            callingSetText = false;
        }
    }
}

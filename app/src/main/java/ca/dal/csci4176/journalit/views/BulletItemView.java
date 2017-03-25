package ca.dal.csci4176.journalit.views;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.dal.csci4176.journalit.R;
import ca.dal.csci4176.journalit.models.BulletItem;
import io.realm.Realm;
import timber.log.Timber;

public class BulletItemView extends BaseItemView<BulletItem>
{
    private BulletItem mItem;
    private Realm mRealm;

    @BindView(R.id.item_move)
    ImageView mMove;

    public BulletItemView(Realm realm, Context context)
    {
        super(context, R.layout.bullet_item, R.id.bullet_item_edit);
        mRealm = realm;
    }

    protected void init()
    {
        ButterKnife.bind(this);
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

        Timber.d("Saving text to bullet item: '%s'", txt);
        mRealm.executeTransaction(r -> mItem.setText(txt));
    }

    @Override
    public void bindToItem(BulletItem item)
    {
        mItem = item;
        updateFromItem();
    }

    @Override
    public void updateFromItem()
    {
        Timber.d("Updating bullet item");
        if (!mItem.isValid())
        {
            return;
        }

        String saved = mItem.getText();
        String ui = mEditTxt.getText().toString();
        boolean isSame = saved.equals(ui);
        boolean savedValueChangedMoreRecently = mItem.getTextLastChangedAt() > mEditTextLastChangedAt;
        Timber.d("TextLastChangedAt: %d, EditTextLastChangedAt: %d", mItem.getTextLastChangedAt(), mEditTextLastChangedAt);
        Timber.d("Bullet item changed. Saved text: '%s', UI text: '%s', is same: %s, saved changed more recently: %s", saved, ui, isSame, savedValueChangedMoreRecently);

        if (!isSame && savedValueChangedMoreRecently)
        {
            Timber.d("Setting text!");
            mEditTxt.setText(saved);
        }
    }
}

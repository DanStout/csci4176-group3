package ca.dal.csci4176.journalit.views;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.RelativeLayout;

import io.realm.RealmObject;
import timber.log.Timber;

public abstract class BaseItemView<T extends RealmObject> extends RelativeLayout
{
    protected EditText mEditTxt;
    protected boolean callingSetText;

    private EnterListener mEnterListener;
    private DeleteListener mDeleteListener;

    public interface EnterListener
    {
        void onEnterPressed(int selectionStart);
    }

    public interface DeleteListener
    {
        void onDeleteWhileEmpty();
    }

    public void setDeleteListener(DeleteListener listener)
    {
        mDeleteListener = listener;
    }

    public void setEnterListener(EnterListener listener)
    {
        mEnterListener = listener;
    }

    public BaseItemView(Context context, @LayoutRes int layoutRes, @IdRes int editTextRes)
    {
        super(context);

        inflate(getContext(), layoutRes, this);

        mEditTxt = (EditText) findViewById(editTextRes);

        mEditTxt.setOnKeyListener((v, keyCode, event) ->
        {
            if (event.getAction() == KeyEvent.ACTION_DOWN)
            {
                switch (keyCode)
                {
                    case KeyEvent.KEYCODE_ENTER:
                        if (mEnterListener != null)
                        {
                            saveText();
                            mEnterListener.onEnterPressed(mEditTxt.getSelectionStart());
                        }
                        return true;
                    case KeyEvent.KEYCODE_DEL:
                        if (mDeleteListener != null && mEditTxt.getText().length() == 0)
                        {
                            mDeleteListener.onDeleteWhileEmpty();
                            return true;
                        }
                        return false;
                    default:
                        return false;
                }
            }
            return false;
        });

        mEditTxt.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                Timber.d("Text changed to: %s", s.toString());

                if (!callingSetText)
                {
                    saveText();
                }
            }
        });

        init();
    }

    public abstract void bindToItem(T item);

    protected abstract void init();

    protected abstract void saveText();
}

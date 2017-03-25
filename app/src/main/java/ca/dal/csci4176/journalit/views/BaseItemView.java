package ca.dal.csci4176.journalit.views;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.RealmObject;
import timber.log.Timber;

public abstract class BaseItemView<T extends RealmObject> extends LinearLayout
{
    protected EditText mEditTxt;

    /**
     * If no changes happen to the EditText after this time, its value will be saved to realm
     **/
    private static final int TEXT_SAVE_DELAY_MILLIS = 200;

    protected long mEditTextLastChangedAt;

    private EnterListener mEnterListener;
    private DeleteListener mDeleteListener;

    public interface EnterListener
    {
        void onEnterPressed(int selectionStart);
    }

    public interface DeleteListener
    {
        void onDeleteAtStart();
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

        /*
         * Potential issue: the keylistener appears to not be called for some virtual keyboards?
         */
        mEditTxt.setOnKeyListener((v, keyCode, event) ->
        {
            Timber.d("Key Pressed: %d", keyCode);
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL &&
                    mDeleteListener != null && mEditTxt.getSelectionStart() == 0)
            {
                saveText(mEditTxt.getText().toString());
                mDeleteListener.onDeleteAtStart();
                return true;
            }
            return false;
        });

        RxTextView.afterTextChangeEvents(mEditTxt)
                .debounce(TEXT_SAVE_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(evt ->
                {
                    Timber.d("Debounced after text changed: saving text ");
                    saveText(evt.editable().toString());
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
                mEditTextLastChangedAt = Math.max(mEditTextLastChangedAt, System.nanoTime());
                Timber.d("EditTextLastChangedAt: %d", mEditTextLastChangedAt);
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                Timber.d("Text changed to: '%s'", s.toString());
                int i = s.toString().indexOf('\n');
                if (i != -1)
                {
                    s.delete(i, i + 1);
                    if (mEnterListener != null)
                    {
                        saveText(s.toString());
                        mEnterListener.onEnterPressed(mEditTxt.getSelectionStart());
                    }
                }
            }
        });

        init();
    }

    public abstract void bindToItem(T item);

    protected abstract void init();

    /**
     * Update the UI based on the contents of the Realm object
     */
    public abstract void updateFromItem();

    /**
     * Save the contents of the EditText to Realm
     */
    protected abstract void saveText(String text);
}

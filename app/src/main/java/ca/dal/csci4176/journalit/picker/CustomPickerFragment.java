package ca.dal.csci4176.journalit.picker;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerFragment;

import ca.dal.csci4176.journalit.R;
import ca.dal.csci4176.journalit.utils.StringUtils;

public class CustomPickerFragment extends FilePickerFragment
{
    @Override
    public void onClickOk(@NonNull View view)
    {
        if (mode == MODE_NEW_FILE)
        {
            if (StringUtils.isWhitespace(super.getNewFileName()))
            {
                Toast.makeText(getActivity(), R.string.error_empty_filename, Toast.LENGTH_LONG).show();
                return;
            }
        }
        super.onClickOk(view);
    }

    @NonNull
    @Override
    protected String getNewFileName()
    {
        String name = super.getNewFileName();
        return name.indexOf('.') != -1 ? name : name + ".txt";
    }
}

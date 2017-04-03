package ca.dal.csci4176.journalit.utils;

import android.view.View;
import android.view.ViewTreeObserver;

import timber.log.Timber;

public class ViewUtils
{
    public interface OnMeasuredCallback
    {
        void onMeasured(int width, int height);
    }

    public interface OnLayoutCallback
    {
        void onLayout();
    }

    public static void executeOnLayout(View view, OnLayoutCallback callback)
    {
        if (view.isLaidOut())
        {
            callback.onLayout();
        }
        else
        {
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
            {
                @Override
                public void onGlobalLayout()
                {
                    callback.onLayout();
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
    }

    /**
     * Execute a callback when the dimensions of a view are known
     */
    public static void getDimensions(View view, OnMeasuredCallback func)
    {
        int width = view.getWidth();
        int height = view.getHeight();
        if (width != 0 && height != 0)
        {
            func.onMeasured(width, height);
            return;
        }

        Timber.d("View has a zero dimension: adding OnPreDrawListener");
        executeOnLayout(view, () -> func.onMeasured(view.getMeasuredWidth(), view.getMeasuredHeight()));
    }
}

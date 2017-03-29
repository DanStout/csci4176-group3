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

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                Timber.d("OnGlobalLayout");
                func.onMeasured(view.getMeasuredWidth(), view.getMeasuredHeight());
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }
}

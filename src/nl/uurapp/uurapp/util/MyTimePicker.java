package nl.uurapp.uurapp.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.TimePicker;


// http://stackoverflow.com/questions/8847171/android-timepicker-wheel-style-not-responding-correctly-to-flick-gestures-insi


public class MyTimePicker extends TimePicker {

	public MyTimePicker(Context context) {
		super(context);
	}
	

    public MyTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
	    if (ev.getActionMasked() == MotionEvent.ACTION_DOWN)
	    {
	        ViewParent p = getParent();
	        if (p != null)
	            p.requestDisallowInterceptTouchEvent(true);
	    }

	    return false;
	}
}

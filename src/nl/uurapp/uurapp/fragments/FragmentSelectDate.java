package nl.uurapp.uurapp.fragments;

import java.util.Calendar;

import nl.uurapp.uurapp.ApplicationEx;
import nl.uurapp.uurapp.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.DatePicker;

public class FragmentSelectDate extends Fragment {

	private FragmentEventsListener listener;
	private DatePicker datePicker;
	private ApplicationEx mApp;
	private CountDownTimer waitTimer;
	private long timerInterval = 500;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_select_date, container,
				false);

		// get controls
		datePicker = (DatePicker) view.findViewById(R.id.datePicker1);
		// Create OnDateChangedListener

		// Set DatePicker to Selection
		mApp = ApplicationEx.getContext();
		Calendar mDate = Calendar.getInstance();
		mDate.setTime(mApp.getSelection().getDate());
		datePicker.init(mDate.get(Calendar.YEAR), mDate.get(Calendar.MONTH),
				mDate.get(Calendar.DAY_OF_MONTH), new OnDateChangedListener() {

					@Override
					public void onDateChanged(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {

						// On Changed data, save selected data and update
						// time-rows.
						Log.i(ApplicationEx.TAG, "FragmentSelectDate: onDateChanged()");
						Calendar pickedDate = Calendar.getInstance();
						pickedDate.set(year, monthOfYear, dayOfMonth);
						mApp.getSelection().setDate(pickedDate.getTime());
						if (waitTimer != null) {
							waitTimer.cancel();
							waitTimer.start();
						}
					}
				});

		return view;
	}

	// Interface with Activity
	public interface FragmentEventsListener {
		public void DetailChanged(Boolean toMaster);

		public void Alert(String text);

		public void Toast(String text);
	}

	public void onPause() {
		Log.i(ApplicationEx.TAG, "FragmentSelectDate onPause()");
		if (waitTimer != null) {
			waitTimer.cancel();
			waitTimer = null;
			Log.i(ApplicationEx.TAG,
					"FragmentSelectDate waitTimer.cancel() and set to null");
		}
		super.onPause();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof FragmentEventsListener) {
			listener = (FragmentEventsListener) activity;
		} else {
			throw new ClassCastException(
					activity.toString()
							+ " must implemenet FragmentSelectDate.FragmentEventsListener");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(ApplicationEx.TAG, "FragmentSelectDate onResume()");
		// Init waittimer, is set to null in onPause()
		// Set waittimer, to wait 1 second before updating timerow
		waitTimer = new CountDownTimer(timerInterval, timerInterval) {

			public void onTick(long millisUntilFinished) {
				// never called (0)
			}

			public void onFinish() {
				// After timerInterval-milliseconds finish
				// Toast.makeText(TimeList.this, "Timer Finish.",
				// Toast.LENGTH_SHORT).show();
				Log.i(ApplicationEx.TAG, "FragmentSelectDate: Timer!");
				listener.DetailChanged(false);
			}
		};
		updateDate();
	}

	public void updateDate() {
		// Get addresses
		Log.i(ApplicationEx.TAG, "FragmentSelectDate: updateDate()");
		Calendar mDate = Calendar.getInstance();
		mDate.setTime(mApp.getSelection().getDate());
		datePicker.updateDate(mDate.get(Calendar.YEAR),
				mDate.get(Calendar.MONTH), mDate.get(Calendar.DAY_OF_MONTH));
	}

}

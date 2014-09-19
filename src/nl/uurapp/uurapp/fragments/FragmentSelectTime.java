package nl.uurapp.uurapp.fragments;

import java.util.Calendar;

import nl.uurapp.uurapp.ApplicationEx;
import nl.uurapp.uurapp.R;
import nl.uurapp.uurapp.util.MyTimePicker;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

public class FragmentSelectTime extends Fragment {

	static final int modeUndecided = 0;
	static final int modeEnterStartEnd = 1;
	static final int modeEnterHours = 2;

	private int timeEntryMode;
	private MyTimePicker tpStart;
	private MyTimePicker tpEnd;
	private MyTimePicker tpHours;

	private TextView tvTitleHours;
	private boolean mIgnorePickerEvent;
	private ApplicationEx mApp;
	private FragmentEventsListener listener;
	private CountDownTimer waitTimer;
	private long timerInterval = 500;
	private Boolean timerUpdateNotHuman;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_select_time, container,
				false);

		mApp = ApplicationEx.getContext();
		timerUpdateNotHuman = false;

		// get controls
		tvTitleHours = (TextView) view.findViewById(R.id.textViewTitleHours);

		// Set the timepicker to 24H in stead of AM/PM
		tpStart = (MyTimePicker) view.findViewById(R.id.timePickerStart);
		tpEnd = (MyTimePicker) view.findViewById(R.id.timePickerEnd);
		tpHours = (MyTimePicker) view.findViewById(R.id.timePickerHours);

		tpStart.setIs24HourView(Boolean.TRUE);
		tpEnd.setIs24HourView(Boolean.TRUE);
		tpHours.setIs24HourView(Boolean.TRUE);

		// Set listener for tpStart
		tpStart.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				// -- Begin
				// Interval 5 minutes,
				// http://stackoverflow.com/questions/2580216/android-timepicker-minutes-to-15
				if (mIgnorePickerEvent)
					return;
				if (minute % ApplicationEx.TIME_PICKER_INTERVAL != 0) {
					int minuteFloor = minute
							- (minute % ApplicationEx.TIME_PICKER_INTERVAL);
					minute = minuteFloor
							+ (minute == minuteFloor + 1 ? ApplicationEx.TIME_PICKER_INTERVAL
									: 0);
					if (minute == 60)
						minute = 0;
					mIgnorePickerEvent = true;
					tpStart.setCurrentMinute(minute);
					mIgnorePickerEvent = false;

				}
				// Interval 5 minutes
				// -- End

				if (timerUpdateNotHuman)
					return;

				if (timeEntryMode == modeUndecided)
					timeEntryMode = modeEnterStartEnd;

				if (waitTimer != null) {
					waitTimer.cancel();
					waitTimer.start();
					Log.i(ApplicationEx.TAG, "Timer restart..tpStart");
				}

			}
		});
		tpEnd.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {

				// -- Begin
				// Interval 5 minutes,
				// http://stackoverflow.com/questions/2580216/android-timepicker-minutes-to-15
				if (mIgnorePickerEvent)
					return;
				if (minute % ApplicationEx.TIME_PICKER_INTERVAL != 0) {
					int minuteFloor = minute
							- (minute % ApplicationEx.TIME_PICKER_INTERVAL);
					minute = minuteFloor
							+ (minute == minuteFloor + 1 ? ApplicationEx.TIME_PICKER_INTERVAL
									: 0);
					if (minute == 60)
						minute = 0;
					mIgnorePickerEvent = true;
					tpEnd.setCurrentMinute(minute);
					mIgnorePickerEvent = false;
				}
				// Interval 5 minutes
				// -- End
				if (timerUpdateNotHuman)
					return;

				if (timeEntryMode == modeUndecided)
					timeEntryMode = modeEnterStartEnd;

				if (waitTimer != null) {
					waitTimer.cancel();
					waitTimer.start();
					Log.i(ApplicationEx.TAG, "Timer restart..tpEnd");
				}
			}
		});
		// Set listener for tpHours
		tpHours.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				// -- Begin
				// Interval 5 minutes,
				// http://stackoverflow.com/questions/2580216/android-timepicker-minutes-to-15
				if (mIgnorePickerEvent)
					return;
				if (minute % ApplicationEx.TIME_PICKER_INTERVAL != 0) {
					int minuteFloor = minute
							- (minute % ApplicationEx.TIME_PICKER_INTERVAL);
					minute = minuteFloor
							+ (minute == minuteFloor + 1 ? ApplicationEx.TIME_PICKER_INTERVAL
									: 0);
					if (minute == 60)
						minute = 0;
					mIgnorePickerEvent = true;
					tpHours.setCurrentMinute(minute);
					mIgnorePickerEvent = false;
				}
				// Interval 5 minutes
				// -- End
				if (timerUpdateNotHuman)
					return;

				if (timeEntryMode == modeUndecided)
					timeEntryMode = modeEnterHours;

				if (waitTimer != null) {
					waitTimer.cancel();
					waitTimer.start();
					Log.i(ApplicationEx.TAG, "Timer restart..tpHours");
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

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof FragmentEventsListener) {
			listener = (FragmentEventsListener) activity;
		} else {
			throw new ClassCastException(
					activity.toString()
							+ " must implemenet FragmentSelectTime.FragmentEventsListener");
		}
	}

	public void UpdateSelection() {
		// Store start, end and hours
		// Get time
		Log.i(ApplicationEx.TAG, "UpdateSelection()");
		if (timeEntryMode == modeEnterStartEnd) {
			mApp.getSelection().setStartTime(tpStart.getCurrentHour(),
					tpStart.getCurrentMinute());
			mApp.getSelection().setEndTime(tpEnd.getCurrentHour(),
					tpEnd.getCurrentMinute());
		} else {
			mApp.getSelection().setStartTime("");
			mApp.getSelection().setEndTime("");
		}

		mApp.getSelection().setHours(tpHours.getCurrentHour(),
				tpHours.getCurrentMinute());
	}

	// On activity resume update controls according to timeentry-modus
	public void onResume() {
		super.onResume();
		Log.i(ApplicationEx.TAG, "FragmentSelectTime onResume()");

		// Initialise waittimer (is set to null in onPause)
		// Set waittimer, to wait 1 second before updating Selection
		waitTimer = new CountDownTimer(timerInterval, timerInterval) {

			public void onTick(long millisUntilFinished) {
				// never called (0)
			}

			public void onFinish() {
				Log.i(ApplicationEx.TAG, "FragmentSelectTime: Timer!");
				SetTimeEntryMode();
				if (timeEntryMode == modeEnterStartEnd)
					CalculateAndSetHours();
				timerUpdateNotHuman = false;
				UpdateSelection();
				listener.DetailChanged(false);
			}
		};

		SetTimePickerToSelection();
		timeEntryMode = modeUndecided;
		mIgnorePickerEvent = false;

	}

	public void onPause() {
		Log.i(ApplicationEx.TAG, "FragmentSelectTime onPause()");
		if (waitTimer != null) {
			waitTimer.cancel();
			waitTimer = null;
			Log.i(ApplicationEx.TAG,
					"FragmentSelectTime waitTimer.cancel() and set to null");
		}
		super.onPause();
	}

	private void SetTimePickerToSelection() {
		// Set timepickers to current selection
		timerUpdateNotHuman = true;
		Log.i(ApplicationEx.TAG, "SetTimePickerToSelection()");
		if (mApp.getSelection().getStartTime() != null) {
			tpStart.setCurrentHour(mApp.getSelection().getStartTime()
					.get(Calendar.HOUR_OF_DAY));
			tpStart.setCurrentMinute(mApp.getSelection().getStartTime()
					.get(Calendar.MINUTE));
		}
		if (mApp.getSelection().getEndTime() != null) {
			tpEnd.setCurrentHour(mApp.getSelection().getEndTime()
					.get(Calendar.HOUR_OF_DAY));
			tpEnd.setCurrentMinute(mApp.getSelection().getEndTime()
					.get(Calendar.MINUTE));
		}

		tpHours.setCurrentHour(mApp.getSelection().getHours_Hour());
		tpHours.setCurrentMinute(mApp.getSelection().getHours_Minute());
		timerUpdateNotHuman = false;
	}

	private void SetTimeEntryMode() {
		Log.i(ApplicationEx.TAG, "SetTimeEntryMode()");
		switch (timeEntryMode) {
		case modeUndecided:
			Log.i(ApplicationEx.TAG, "SetTimeEntryMode() = modeUndecided");
			tpStart.setEnabled(true);
			tpEnd.setEnabled(true);
			tpHours.setEnabled(true);
			break;
		case modeEnterStartEnd:
			Log.i(ApplicationEx.TAG, "SetTimeEntryMode() = modeEnterStartEnd");
			tpStart.setEnabled(true);
			tpEnd.setEnabled(true);
			tpHours.setEnabled(false);
			break;
		case modeEnterHours:
			Log.i(ApplicationEx.TAG, "SetTimeEntryMode() = modeEnterHours");
			tpStart.setEnabled(false);
			tpEnd.setEnabled(false);
			tpHours.setEnabled(true);
			break;
		}
	}

	private void CalculateAndSetHours() {
		timerUpdateNotHuman = true;

		if (timeEntryMode == modeEnterHours)
			return;
		Log.i(ApplicationEx.TAG, "CalculateAndSetHours()");

		int startHour = tpStart.getCurrentHour();
		int startMinute = tpStart.getCurrentMinute();
		int endHour = tpEnd.getCurrentHour();
		int endMinute = tpEnd.getCurrentMinute();

		if ((endHour * 60 + endMinute) < (startHour * 60 + startMinute)) {
			// show drawable:
			// http://stackoverflow.com/questions/6931900/setting-drawableleft-in-a-textview-problem
			tvTitleHours.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					R.drawable.moon, 0);
		} else {
			// show drawable
			tvTitleHours.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		mApp.getSelection()
				.setHours(startHour, startMinute, endHour, endMinute);

		int hours = mApp.getSelection().getHours_Hour();
		int minutes = mApp.getSelection().getHours_Minute();

		tpHours.setCurrentHour(hours);
		tpHours.setCurrentMinute(minutes);
		tpHours.setEnabled(false);
		timerUpdateNotHuman = false;
	}
}

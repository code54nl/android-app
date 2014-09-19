package nl.uurapp.uurapp.fragments;

import nl.uurapp.uurapp.ApplicationEx;
import nl.uurapp.uurapp.R;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class FragmentEnterRemarks extends Fragment {

	private FragmentEventsListener listener;
	private EditText etRemarks;
	private ApplicationEx mApp;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_enter_remarks,
				container, false);

		mApp = ApplicationEx.getContext();
		// Cancel if no organization selected

		// get controls
		etRemarks = (EditText) view.findViewById(R.id.editTextRemarks);
		etRemarks.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (listener != null) {
					String text = s.toString();

					mApp.getSelection().setRemarks(text);
					listener.DetailChanged(false);
				}

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

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
							+ " must implemenet FragmentEnterRemarks.FragmentEventsListener");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		updateDescription();
	}

	private void updateDescription() {
		// Get addresses
		ApplicationEx mApp = ApplicationEx.getContext();
		etRemarks.setText(mApp.getSelection().getRemarks());
	}

}

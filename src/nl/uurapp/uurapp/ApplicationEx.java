package nl.uurapp.uurapp;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.app.Application;
import android.content.SharedPreferences;

public class ApplicationEx extends Application {

	// Used to keep track of user-selections and application-state

	// 'constants'
	public static final String PREF_FILE_NAME = "Uurapp";
	public static final String TAG = "UAPP";
	public static final String APIVersion = "2";
	public static final String Referral = "1272";
	public static final Long savedTimeRowsBeforeReview = (long) 10;
	public static final String dictionary_RESULT = "result";
	public static final String result_UNSUPPORTED_API_VERSION = "api_unsupported_api_version";
	public static final String result_SUCCESS = "success";
	public static final String result_NOT_LOGGED_ON = "not_logged_on";
	public static final String result_FAILED = "failed";
	public static final String result_LOGIN_PROCESS_FAILED = "login_process_failed";
	public static final String result_LOGIN_NOT_APPROVED = "login_not_approved";
	public static final String result_LOGIN_LOCKED_OUT = "login_login_locked_out";
	public static final String result_LOGIN_INVALID = "login_invalid";
	public static final String result_LOGIN_TOKEN_EXPIRED = "login_token_expired";
	public static final String result_PASSWORD_NOT_COMPLEX = "password_not_complex";
	public static final String result_USERNAME_NOT_AVAILABLE = "username_not_available";
	public static final String result_INVALID_EMAIL_ADDRESS = "invalid_email_address";
	public static final int TIME_PICKER_INTERVAL = 5;

	// public static final String apiURL =
	// public static final String apiURL =
	// "http://erpdebug.code54.nl/API/jsonapi.ashx"; // debug version
	public static final String apiURL = "https://app.uurapp.nl/API/jsonapi.ashx"; // production
																					// version

	public static String pad0Left(String s, int n) {
		return String.format("%1$" + n + "s", s).replace(' ', '0');
	}

	public static class Selection {
		// holds users current selection of organization, starttime, etc.
		private String timeRowID;
		private Date date;
		private Calendar startTime; // null if no starttime used
		private Calendar endTime; // null if no endtime used
		private BigDecimal hours;
		private String organizationID;
		private String organizationName;
		private String addressID;
		private String addressName;
		private String activityID;
		private String activityName;
		private String projectPhaseID;
		private String projectPhaseName;
		private String remarks;
		private String description;

		public void Reset() // start new selection
		{
			setDate(new Date());
			startTime = getCurrentRoundedTime();
			endTime = getCurrentRoundedTime();
			setHours(new BigDecimal(0));
			setOrganizationID("");			
			setProjectPhaseID("");
			setProjectPhaseName("");
			setActivityID("");
			setActivityName("");
			setRemarks("");
			setDescription("");
			setAddressID("");
			setTimeRowID("");
			setOrganizationName("");
			setAddressName("");
		}

		public void NewTimeRow() // Partly reset selection to prepare for new
									// timerow
		{
			// if endtime is set, make this the new starttime
			// and set endtime to now
			if (endTime != null) {
				setStartTime(endTime.get(Calendar.HOUR_OF_DAY),
						endTime.get(Calendar.MINUTE));
				Calendar nowTime = Calendar.getInstance();
				setEndTime(nowTime.get(Calendar.HOUR_OF_DAY),
						nowTime.get(Calendar.MINUTE));
			} else
				startTime = null;

			setDate(new Date());
			setHours(new BigDecimal(0));
			setOrganizationID("");
			setOrganizationName("");
			// setActivityID(""); - keep last used activity
			setProjectPhaseID("");
			setRemarks("");
			setDescription("");
			setAddressID("");
			setTimeRowID("");
		}

		public String getOrganizationName() {
			return organizationName;
		}

		public void setOrganizationName(String organizationName) {
			this.organizationName = organizationName;
		}

		private void setHours(BigDecimal bigDecimal) {
			this.hours = bigDecimal;
		}

		public BigDecimal getHours() {
			return hours;
		}

		public int getHours_Hour() {
			return hours.divideToIntegralValue(new BigDecimal(1)).intValue();
		}

		public int getHours_Minute() {
			BigDecimal minutesInHour = new BigDecimal(60);
			BigDecimal remainder = hours.remainder(new BigDecimal(1));
			return minutesInHour.multiply(remainder).intValue();
		}

		public void setHours(String hours) throws ParseException {
			this.hours = new BigDecimal(hours);
		}

		public void setHours(int hour, int minute) {
			BigDecimal bdHour = new BigDecimal(hour);
			BigDecimal bdMinute = new BigDecimal(minute);
			this.hours = bdHour.add(bdMinute.divide(new BigDecimal(60), 2,
					BigDecimal.ROUND_HALF_UP));
		}

		public void setHours(int startHour, int startMinute, int endHour,
				int endMinute) {
			BigDecimal bdStartHour = new BigDecimal(startHour);
			BigDecimal bdStartMinute = new BigDecimal(startMinute);
			BigDecimal bdStart = bdStartHour.add(bdStartMinute.divide(
					new BigDecimal(60), 2, BigDecimal.ROUND_HALF_UP));

			BigDecimal bdEndHour = new BigDecimal(endHour);
			BigDecimal bdEndMinute = new BigDecimal(endMinute);
			BigDecimal bdEnd = bdEndHour.add(bdEndMinute.divide(new BigDecimal(
					60), 2, BigDecimal.ROUND_HALF_UP));

			this.hours = bdEnd.subtract(bdStart);
			if (this.hours.compareTo(new BigDecimal(0)) == -1)
				this.hours = new BigDecimal(24).add(this.hours); // remember,
																	// this is a
																	// substraction
																	// because
																	// hours is
																	// negative
		}

		public Date getDate() {
			return date;
		}

		public String getDateString() {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
			String dateStr = formatter.format(getDate());
			return dateStr;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public void setDate(String dateStr) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
			try {
				setDate(formatter.parse(dateStr));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private Calendar getCurrentRoundedTime() {
			Calendar now = Calendar.getInstance();
			// Round minutes
			// Interval 5 minutes,
			// http://stackoverflow.com/questions/2580216/android-timepicker-minutes-to-15
			int minute = now.get(Calendar.MINUTE);
			if (minute % TIME_PICKER_INTERVAL != 0) {
				int minuteFloor = minute - (minute % TIME_PICKER_INTERVAL);
				minute = minuteFloor
						+ (minute == minuteFloor + 1 ? TIME_PICKER_INTERVAL : 0);
				if (minute == 60)
					minute = 0;
				now.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
						now.get(Calendar.DAY_OF_MONTH),
						now.get(Calendar.HOUR_OF_DAY), minute);
			}
			return now;
		}

		public Calendar getStartTime() {
			return startTime;
		}

		public String getStartTimeString() {
			if (getStartTime() == null)
				return "";
			Date time = getStartTime().getTime();
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
			String timeStr = formatter.format(time);
			return timeStr;
		}

		public void setStartTime(String startTime) {
			if (startTime.equals(""))
				this.startTime = null;
			else
				try {
					SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
					Date dtStart = formatter.parse(startTime);
					Calendar clStart = Calendar.getInstance();
					clStart.setTime(dtStart);
					setStartTime(clStart);
				} catch (Exception e) {
					e.printStackTrace();
				}
		}

		private void setStartTime(Calendar instance) {
			this.startTime = instance;
		}

		public void setStartTime(int Hours, int Minutes) {
			Calendar cal = Calendar.getInstance();
			cal.set(0, 0, 0, Hours, Minutes);
			this.startTime = cal;
		}

		public Calendar getEndTime() {
			return endTime;
		}

		public String getEndTimeString() {
			if (getEndTime() == null)
				return "";
			Date time = getEndTime().getTime();
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
			String timeStr = formatter.format(time);
			return timeStr;
		}

		public void setEndTime(String endTime) {
			if (endTime.equals(""))
				this.endTime = null;
			else
				try {
					SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
					Date dtEnd = formatter.parse(endTime);
					Calendar clEnd = Calendar.getInstance();
					clEnd.setTime(dtEnd);
					setEndTime(clEnd);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		public void setEndTime(int Hours, int Minutes) {
			Calendar cal = Calendar.getInstance();
			cal.set(0, 0, 0, Hours, Minutes);
			this.endTime = cal;
		}

		private void setEndTime(Calendar instance) {
			this.endTime = instance;

		}

		public Selection() {
			setDate(new Date());
		}

		public String getOrganizationID() {
			return organizationID;
		}

		public void setOrganizationID(String organizationID) {
			this.organizationID = organizationID;
		}

		public String getActivityID() {
			return activityID;
		}

		public void setActivityID(String activityID) {
			this.activityID = activityID;
		}

		public String getProjectPhaseID() {
			return projectPhaseID;
		}

		public void setProjectPhaseID(String projectPhaseID) {
			this.projectPhaseID = projectPhaseID;
		}

		public String getRemarks() {
			return remarks;
		}

		public void setRemarks(String remarks) {
			this.remarks = remarks;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getAddressID() {
			return addressID;
		}

		public void setAddressID(String addressID) {
			this.addressID = addressID;
		}

		public String getTimeRowID() {
			return timeRowID;
		}

		public void setTimeRowID(String timeRowID) {
			this.timeRowID = timeRowID;
		}

		public String getAddressName() {
			return addressName;
		}

		public void setAddressName(String addressName) {
			this.addressName = addressName;
		}

		public String getProjectPhaseName() {
			return projectPhaseName;
		}

		public void setProjectPhaseName(String projectPhaseName) {
			this.projectPhaseName = projectPhaseName;
		}

		public String getActivityName() {
			return activityName;
		}

		public void setActivityName(String activityName) {
			this.activityName = activityName;
		}
	}

	public static class Address {
		public Address(String id, String description) {
			setId(id);
			setDescription(description);
		}

		private String id;
		private String description;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

	public static class ProjectPhase {
		public ProjectPhase(String id, String description) {
			setId(id);
			setDescription(description);
		}

		private String id;
		private String description;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

	public static class Activities {
		public Activities(String id, String description) {
			setId(id);
			setDescription(description);
		}

		private String id;
		private String description;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

	public static class Organization {
		private String id;
		private String name;

		public Organization(String id, String name) {
			this.setId(id);
			this.setName(name);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}

	public List<Organization> organizationList; // holds the organization-list
												// (for selection)
	public List<TimeRow> timeRowList; // holds the timerows (for edit/delete)
	public List<Address> addressList; // holds the addresses for current
										// organization (for selection)
	public List<ProjectPhase> projectPhaseList; // holds the projectphases for
												// current organization (for
												// selection)
	public List<Activities> activityList; // holds the activity-list (for
											// selection)

	private String username;
	private String token;
	private boolean autologon;
	private Long timerowsavings_until_review;
	private static ApplicationEx _instance;
	private Selection selection;

	public static class TimeRow implements Comparable<TimeRow> {
		private String id;
		private String startTime;
		private String endTime;
		private String hours;
		private String activityID;
		private String activityAbbrv;
		private String activityDescription;
		private String organizationID;
		private String organizationAbbrv;
		private String organizationName;
		private String projectPhaseAbbrv;
		private String projectPhaseName;
		private String projectAbbrv;
		private String projectName;
		private String projectPhaseID;
		private String remarks;
		private String description;
		private Boolean declared;

		public TimeRow(String id, String startTime, String endTime,
				String hours, String activityID, String activityAbbrv,
				String activityDescription, String organizationID,
				String organizationAbbrv, String organizationName,
				String projectPhaseID, String projectPhaseAbbrv,
				String projectPhaseName, String projectAbbrv,
				String projectName, String remarks, String description,
				Boolean declared) {
			this.setId(id);
			this.setStartTime(startTime);
			this.setEndTime(endTime);
			this.setHours(hours);
			this.setActivityID(activityID);
			this.setActivityAbbrv(activityAbbrv);
			this.setActivityDescription(activityDescription);
			this.setOrganizationID(organizationID);
			this.setOrganizationAbbrv(organizationAbbrv);
			this.setOrganizationName(organizationName);
			this.setProjectPhaseAbbrv(projectPhaseAbbrv);
			this.setProjectPhaseID(projectPhaseID);
			this.setProjectPhaseName(projectPhaseName);
			this.setRemarks(remarks);
			this.setDescription(description);
			this.setDeclared(declared);
			this.setProjectAbbrv(projectAbbrv);
			this.setProjectName(projectName);
		}

		public String getActivityDescription() {
			return activityDescription;
		}

		public void setActivityDescription(String activityDescription) {
			this.activityDescription = activityDescription;
		}

		public String getOrganizationName() {
			return organizationName;
		}

		public void setOrganizationName(String organizationName) {
			this.organizationName = organizationName;
		}

		public String getProjectPhaseName() {
			return projectPhaseName;
		}

		public void setProjectPhaseName(String projectPhaseName) {
			this.projectPhaseName = projectPhaseName;
		}

		public String getProjectName() {
			return projectName;
		}

		public void setProjectName(String projectName) {
			this.projectName = projectName;
		}

		public String getStartTime() {
			return startTime;
		}

		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}

		public String getEndTime() {
			return endTime;
		}

		public void setEndTime(String endTime) {
			this.endTime = endTime;
		}

		public String getOrganizationAbbrv() {
			return organizationAbbrv;
		}

		public void setOrganizationAbbrv(String organizationAbbrv) {
			this.organizationAbbrv = organizationAbbrv;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getHours() {
			return hours;
		}

		public void setHours(String hours) {
			this.hours = hours;
		}

		public String getActivityID() {
			return activityID;
		}

		public void setActivityID(String activityID) {
			this.activityID = activityID;
		}

		public String getActivityAbbrv() {
			return activityAbbrv;
		}

		public void setActivityAbbrv(String activityAbbrv) {
			this.activityAbbrv = activityAbbrv;
		}

		public String getOrganizationID() {
			return organizationID;
		}

		public void setOrganizationID(String organizationID) {
			this.organizationID = organizationID;
		}

		public String getRemarks() {
			return remarks;
		}

		public void setRemarks(String remarks) {
			this.remarks = remarks;
		}

		public String getProjectPhaseAbbrv() {
			return projectPhaseAbbrv;
		}

		public void setProjectPhaseAbbrv(String projectPhaseAbbrv) {
			this.projectPhaseAbbrv = projectPhaseAbbrv;
		}

		public String getProjectPhaseID() {
			return projectPhaseID;
		}

		public void setProjectPhaseID(String projectPhaseID) {
			this.projectPhaseID = projectPhaseID;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Boolean getDeclared() {
			return declared;
		}

		public void setDeclared(Boolean declared) {
			this.declared = declared;
		}

		@Override
		public int compareTo(TimeRow arg0) {
			try {
				if ((this.startTime != "") && (arg0.startTime != "")) {
					SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
					Date dtStart1 = formatter.parse(this.startTime);
					Date dtStart2 = formatter.parse(arg0.startTime);
					return (dtStart1.compareTo(dtStart2));
				} else
					return 0;
			} catch (Exception e) {
				return 0;
			}
		}

		public String getProjectAbbrv() {
			return projectAbbrv;
		}

		public void setProjectAbbrv(String projectAbbrv) {
			this.projectAbbrv = projectAbbrv;
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String password) {
		this.token = password;
	}

	public static ApplicationEx getContext() {
		return _instance;
	}

	public void Reset() // start new session (i.e. on logoff)
	{
		setUsername("");
		setToken("");
		organizationList = new ArrayList<Organization>();
		timeRowList = new ArrayList<TimeRow>();
		addressList = new ArrayList<Address>();
		projectPhaseList = new ArrayList<ProjectPhase>();
		activityList = new ArrayList<Activities>();
		selection.Reset();
	}

	public ApplicationEx() {
		super();
		_instance = this;
		organizationList = new ArrayList<Organization>();
		timeRowList = new ArrayList<TimeRow>();
		addressList = new ArrayList<Address>();
		projectPhaseList = new ArrayList<ProjectPhase>();
		activityList = new ArrayList<Activities>();
		selection = new Selection();
	}

	public Selection getSelection() {
		return selection;
	}

	public boolean isAutologon() {
		return autologon;
	}

	public void setAutologon(boolean autologon) {
		this.autologon = autologon;
	}

	public Long getTimerowsavings_until_review() {
		return timerowsavings_until_review;
	}

	public void setTimerowsavings_until_review(Long timerowsavings_until_review) {
		this.timerowsavings_until_review = timerowsavings_until_review;

		// save username, not token or password
		SharedPreferences preferences = getSharedPreferences(
				ApplicationEx.PREF_FILE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong("timerowsavings_until_review",
				timerowsavings_until_review);
		editor.commit();
	}
}
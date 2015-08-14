package il.org.rimon_edu.calendarofthesoul.calendar;

import il.org.rimon_edu.calendarofthesoul.R;
import il.org.rimon_edu.calendarofthesoul.easter.EasterDates;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SoulCalendar {

	/* Shared preferences */
	private static final String APP_PREFS_TIMESTAMP	= "pref_timestamp";
	private static final String APP_PREFS_WEEK 		= "pref_week";
	private static final int    ILLEGAL_WEEK		= 52;
	private static final int    NUM_WEEKS			= 52;
	
	private static final int    timestampValid		= 30*60*1000;	/* 30 Minutes represented in milliseconds */
    private Long				timestamp 			= null;			/* Stored as System.currentTimeMillis() */
    private int    				weekToShow ;						/* Stored as number (0-51) */
    private boolean				needUpdate			= true;			/* Indicates that the week number requires re-calculation */
    private Calendar			lastShownDate		= null;			/* Shows the last date that the user requested to show */
	
	private Context				context				= null;
	private SharedPreferences	settings			= null;
	private EasterDates			easters				= null;
    private static final String	MODULE_NAME			= "SoulCalendar.Calendar";
    private static final boolean LOGGING			= false;
    
    public static final int		WINTER				= 0;
    public static final int		SPRING				= 1;
    public static final int		SUMMER				= 2;
    public static final int		AUTUMN				= 3;

    
	public SoulCalendar(Context c, SharedPreferences p) {
		this.context = c;
		this.settings = p;
		
		this.easters = new EasterDates(c);
		this.lastShownDate = Calendar.getInstance();
		this.lastShownDate.setTimeInMillis(now());
		
		restoreSharedPreferences();
		calculateWeekToShow(now());
		
	}
	
	/* Returns values from 1..52 */
	public int getWeek() {
		return this.weekToShow+1;
	}
	
	public int getNumWeeks() {
		return NUM_WEEKS;
	}
	
	/* Receives values from 1..52 */
	public void setWeek(int week) {
		setTimeStamp();
		if (1>week || week>NUM_WEEKS)
		{
			if(LOGGING) Log.e(MODULE_NAME, new String("Week # ").concat(Integer.valueOf(week).toString()).concat(" is not in the range of 1-52"));
			return;
		}
		this.weekToShow = week-1;
		if(LOGGING) Log.v(MODULE_NAME, new String("Setting weekToShow to ").concat(Integer.valueOf(this.weekToShow).toString()));
	}
	
	public String getVerse() {
		setNeedToUpdate(false);
		return this.context.getResources().getStringArray(R.array.verses)[weekToShow];
	}
	
	public boolean getNeedToUpdate() {
		return this.needUpdate;
	}
	
	private void setNeedToUpdate(boolean need) {
		this.needUpdate = need;
	}
	
	public void update(boolean force) {
		setTimeStamp();
		if (force) {
			setNeedToUpdate(true);
		}
		calculateWeekToShow(now());
	}
	
	public void setCorrespondingWeek() {
		setTimeStamp();
		if(LOGGING) Log.v(MODULE_NAME, "Setting corresponding week");
		setWeek(Math.abs(NUM_WEEKS - this.weekToShow));
	}
	
	public void saveSharedPreferences(SharedPreferences.Editor editor) {
    	if(LOGGING) Log.v(MODULE_NAME, "saveSharedPreferences() called");
    	editor.putLong(APP_PREFS_TIMESTAMP, timestamp);
    	editor.putInt(APP_PREFS_WEEK, weekToShow);
	}
	
	public void restoreSharedPreferences() {
		long now = now();
		this.timestamp	= settings.getLong(APP_PREFS_TIMESTAMP, now);
		this.weekToShow = settings.getInt(APP_PREFS_WEEK, ILLEGAL_WEEK);
		
		if (! isTimeStampValid() || (timestamp == now)) {
			setNeedToUpdate(true);
		}
		else {
			setNeedToUpdate(false);
		}		
		setTimeStamp();
	}
	
	public void setDate(Calendar date) {
		setTimeStamp();
		if(LOGGING) Log.v(MODULE_NAME, new String("Setting date to ").concat(date.toString()));
		this.needUpdate = true;
		this.lastShownDate = date;
		calculateWeekToShow(date.getTimeInMillis());
	}
	
	public Calendar getDate() {
		setTimeStamp();
		return this.lastShownDate;
	}
	
	public int getSeason()
	{
		if (weekToShow < 11)
			return SPRING;
		if (weekToShow < 22)
			return SUMMER;
		if (weekToShow < 38)
			return AUTUMN;
		return WINTER;
	}
	
	private long now() {
		return System.currentTimeMillis();
	}

	private void setTimeStamp() {
		this.timestamp = now();
	}
	
	String longToString(long l)
	{
		return Long.valueOf(l).toString();
	}
	
	private boolean isTimeStampValid() {
		
		long now = now();
		if ( (now - timestamp) > timestampValid) {
			return false;
		}
		return true; 		
	}
	
	/*
	 * return -1 if a < b
	 * return  0 if a = b
	 * return  1 if a > b
	 */
	private int compareCalendars(Calendar a, Calendar b)
	{
		if (a.get(Calendar.YEAR) < b.get(Calendar.YEAR)) {
			return -1;
		}
		
		if (a.get(Calendar.YEAR) == b.get(Calendar.YEAR)) {
			if (a.get(Calendar.MONTH) < b.get(Calendar.MONTH)) {
				return -1;
			}
			
			if (a.get(Calendar.MONTH) == b.get(Calendar.MONTH)) {
				if (a.get(Calendar.DAY_OF_MONTH) < b.get(Calendar.DAY_OF_MONTH)) {
					return -1;
				}
			
				if (a.get(Calendar.DAY_OF_MONTH) == b.get(Calendar.DAY_OF_MONTH)) {
					return 0;
				}
			}
		}
		return 1;	
	}
	
	private int calculateWeekNumber(Calendar first, Calendar last, Calendar date, int maxNumWeeks)
	{
		int week = 0;
		
		// Since the concept of a week is flexible in the calendar, we first have to calculate how many
		// days there are in each week
		int daysInWeek = last.get(Calendar.DAY_OF_YEAR) - first.get(Calendar.DAY_OF_YEAR);
		if (0 > daysInWeek) {
			daysInWeek += first.getActualMaximum(Calendar.DAY_OF_YEAR);
		}
		daysInWeek = (int)Math.ceil((double)daysInWeek/maxNumWeeks);
		
		// Increment 'first' by a "week" and so long as 'date' is not before that, continue doing so 
		first.add(Calendar.DAY_OF_YEAR, daysInWeek);
		while (1 != compareCalendars(first, date)) {
			week++;
			first.add(Calendar.DAY_OF_YEAR, daysInWeek);
		}
		
		// Handle the case where the last "week" has one extra day
		if (week == maxNumWeeks) {
			week--;
		}
		return week;	
	}
	
	
    private void calculateWeekToShow(long millis) {
    	if (this.needUpdate) {  
    		int week;
    		Calendar dayToShow = Calendar.getInstance();
    		dayToShow.setTimeInMillis(millis);
    		  		    		
    		int easterYear = dayToShow.get(Calendar.YEAR);
    		
    		if (easterYear < easters.getFirstYear() ||
    			easterYear > easters.getLastYear()) {
    			if(LOGGING) Log.e(MODULE_NAME, new String(Integer.valueOf(easterYear).toString().concat(" is not a leagl year for this app")));
    			return;
    		}
    		    		    		
    		Date now = dayToShow.getTime();
    		Date easterBefore = easters.getEasterByYear(easterYear);    		
    		if (0 < easterBefore.compareTo(now) ) {
    			easterYear--;
    			easterBefore = easters.getEasterByYear(easterYear);
    		}
    		
    		if(LOGGING) Log.v(MODULE_NAME, new String("Relevant Easters are ").concat(easterBefore.toString()).concat("and ").concat(this.easters.getEasterByYear(easterYear+1).toString()));
    		Date easterAfter = easters.getEasterByYear(easterYear+1);    		
    		
    		/* There are 3 "zones" for the calendar
    		 * 1. The time between easterBefore and 22 June (linked to St. John's Tide) for verses 1-11	 (11 verses)
    		 * 2. The time between 23 June and 28 December (linked to Christmas) 		for verses 12-38 (27 verses)
    		 * 3. The time between 29 December and easterAfter 							for verses 39-52 (14 verses)
    		 * 
    		 * For the dates in zones 1, 3 - the "week" can fluctuate in its number of days
    		 * in order to accommodate for the amount of time. Weeks can be 5, 6, 7 or 8 days.
    		 */    		
    		Calendar stJohnTide = Calendar.getInstance();
    		stJohnTide.set(easterYear, Calendar.JUNE, 23);
    		
    		Calendar xmas = Calendar.getInstance();
    		xmas.set(easterYear, Calendar.DECEMBER, 29);

    		// see http://stackoverflow.com/questions/4608470/why-dec-31-2010-returns-1-as-week-of-year
    		xmas.setMinimalDaysInFirstWeek(7);
    		dayToShow.setMinimalDaysInFirstWeek(7);
    		stJohnTide.setMinimalDaysInFirstWeek(7);
    		    		
    		int zone = compareCalendars(dayToShow, stJohnTide);
    		
    		if (-1 == zone) {			// zone 1
    			Calendar easter = Calendar.getInstance();
    			easter.setTime(easterBefore);
    			week = 0;				// first week in zone 1
    			week += calculateWeekNumber(easter, stJohnTide, dayToShow, 11);
    		}
    		else {
    			zone = compareCalendars(dayToShow, xmas);
    			
    			if (-1 != zone) {		// zone 3
        			Calendar easter = Calendar.getInstance();
        			easter.setTime(easterAfter);
    				week = 38;			// first week in zone 3
        			week += calculateWeekNumber(xmas, easter, dayToShow, 14);
    			}
    			else {					// zone 2
    				week = 11;			// first week in zone 2
    				week += calculateWeekNumber(stJohnTide, xmas, dayToShow, 27);
    				//week += ((dayToShow.get(Calendar.DAY_OF_YEAR) - stJohnTide.get(Calendar.DAY_OF_YEAR)) / 7);
    				//week = 37;			// last week in zone 2
    	    		//week -= xmas.get(Calendar.WEEK_OF_YEAR);
    				//week += dayToShow.get(Calendar.WEEK_OF_YEAR);

    			}
    		} 		
        	setWeek(week+1);
    	}
	}

}

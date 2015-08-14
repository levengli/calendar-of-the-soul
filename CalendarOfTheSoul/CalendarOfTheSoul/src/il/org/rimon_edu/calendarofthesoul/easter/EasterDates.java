package il.org.rimon_edu.calendarofthesoul.easter;

import il.org.rimon_edu.calendarofthesoul.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.util.Log;

public class EasterDates {
    private final int			firstYearOfCalendar = 1912;
	private Context				context				= null;
	private static final int    maxEasterDates		= 1000;
    private static List<Date> 	easterDates 		= new ArrayList<Date>(maxEasterDates);
    private static final String	EASTER_DATE_FORMAT	= "dd MMM yyyy";
    private static final String	MODULE_NAME			= "SoulCalendar.EasterDates";
    private static final boolean LOGGING			= false;
    
    public EasterDates(Context c) {
    	this.context = c;
        try { 
        	populateEasterDates(); 
    	}
        catch(Exception e) {
        	if(LOGGING) Log.e(MODULE_NAME, "Failed to read and parse the dates for Easter", e);
        	e.printStackTrace();
    	}
   	
    }
    
    public int getFirstYear() {
    	return this.firstYearOfCalendar;
    }
    
    public int getLastYear() {
    	return this.firstYearOfCalendar + easterDates.toArray().length - 1;
    }

    
    public Date getEasterByYear(int year) {
    	if (year < firstYearOfCalendar ||
    		year > firstYearOfCalendar + easterDates.toArray().length) {
    		if(LOGGING) Log.w(MODULE_NAME, "Year not supporeted, defaulted to first supported year");
    		year = firstYearOfCalendar;
    	}
    	year -= firstYearOfCalendar;
    	return easterDates.get(year);
    }

    private void populateEasterDates() throws Exception {
    	InputStream	rawEaster			= this.context.getResources().openRawResource(R.raw.easters);
        BufferedReader 		reader		= new BufferedReader(new InputStreamReader(rawEaster, "utf8"));
        SimpleDateFormat 	dateFormat 	= new SimpleDateFormat(EASTER_DATE_FORMAT, new Locale("en"));
        String				line;
        String[] 			split;
        
        while((line = reader.readLine())!= null)
        {
        	split = line.split(", ");
        	for (int i=0 ; i<split.length ; i++)
        	{
	        	easterDates.add((Date)dateFormat.parse(split[i]));
        	}
        }           
     }
}

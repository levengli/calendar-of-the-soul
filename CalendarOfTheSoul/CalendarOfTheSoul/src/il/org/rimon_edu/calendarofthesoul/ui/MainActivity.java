package il.org.rimon_edu.calendarofthesoul.ui;

import il.org.rimon_edu.calendarofthesoul.R;
import il.org.rimon_edu.calendarofthesoul.calendar.SoulCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TreeMap;

import numberPickerOverride.NumberPickerOverride;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends Activity implements ZoomTextViewCallback {
	    
	private static final String APP_PREFS 			= "calendarOfTheSoul_prefs";
	private static final String APP_PREFS_LANG 		= "pref_lang";
    private static final String APP_PREFS_TEXT_SIZE = "verse_size";
    private static String 		lang 				= null;			/* Stored as a 2 letter representation, e.g. en, he, fr */

    /* Other global variables */
    private static final String	MODULE_NAME			= "SoulCalendar.UI";
    private static final boolean LOGGING			= false;
            
    private SoulCalendar		soulCalendar		= null;
	private SharedPreferences	settings			= null;
	
	/* Menu items */
	private static final int 	CHANGE_TO_CURRENT_WEEK 			= 0;
	private static final int 	CHANGE_TO_CORRESPONDING_WEEK 	= 1;
	private static final int 	USER_SELECT_WEEK_NUM			= 2;
	private static final int 	USER_SELECT_DATE 				= 3;
	private static final int 	CHANGE_LANGUAGE 				= 4;
	private static final int    SHARE_APP						= 5;
	private static final int 	SHOW_ABOUT 						= 6;
	private static final int 	QUIT 							= 7;
		
	private NumberPickerOverride numberPicker = null;

	
	/**************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {    	    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
		this.settings = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
		this.soulCalendar = new SoulCalendar(getBaseContext(), this.settings);
       
		numberPicker = new NumberPickerOverride(this);
    	numberPicker.setMinNum(1);
    	numberPicker.setMaxNum(this.soulCalendar.getNumWeeks());
    	numberPicker.setSubmitListener(okListener);
    	
    	
        restoreAppState();      
        switchAppLanguage();
        updateVerse();
        ((ImageButton)findViewById(R.id.side_menu)).setOnClickListener(mainMenuButtonOnClickListener);
        this.onPause();        // force saving the current settings
        
        ((ZoomTextView)findViewById(R.id.verse)).callbacks = (ZoomTextViewCallback) this;
    } 
    

    @Override
    protected void onPause() {
    	if(LOGGING) Log.v(MODULE_NAME, "onPause() called");
    	super.onPause();
    	/* Save the SharedPreferences */
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putString(APP_PREFS_LANG, lang);
    	editor.putFloat(APP_PREFS_TEXT_SIZE, ((ZoomTextView)findViewById(R.id.verse)).getTextSize());
    	this.soulCalendar.saveSharedPreferences(editor);
    	editor.commit();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	restoreAppState();
        updateVerse();    		
    }
    
    
    private void switchAppLanguage() {
    	
    	if(LOGGING) Log.v(MODULE_NAME, lang);
    	
    	/* Update the app to use the new language */
        Configuration config = getBaseContext().getResources().getConfiguration();
        final Locale locale  = new Locale(lang, config.locale.getCountry(), config.locale.getVariant());
    	
    	Locale.setDefault(locale);
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        
        LinearLayout		header 			= (LinearLayout)findViewById(R.id.header);
        View				spacer			= findViewById(R.id.spacer);
    	TextView	 		week_text  		= (TextView)findViewById(R.id.week_text);
    	ImageButton			side_menu		= (ImageButton)findViewById(R.id.side_menu);
    	
    	if (header.getChildCount() > 0)
    		header.removeAllViews();
    	
    	if (getString(R.string.dir).equals("RTL")) {
    		header.addView(side_menu);
    		header.addView(spacer);
    		header.addView(week_text);
    	}
    	else {
    		header.addView(week_text);
    		header.addView(spacer);
    		header.addView(side_menu);
    	}
    	
    	setTitle(getString(R.string.title));
    	numberPicker.setTitle(getString(R.string.select_week));
    	
    }
    
    
    private MainActivity getSoulCalendarUi() {
    	return this;
    }

	private void updateVerse() {
    	TextView week_text = (TextView)findViewById(R.id.week_text);
    	String toShow = getText(R.string.week).toString().concat(" ").concat(Integer.valueOf(this.soulCalendar.getWeek()).toString());
    	week_text.setText(toShow);
    	
    	TextView verse = (TextView) findViewById(R.id.verse);
    	//Configuration config = getBaseContext().getResources().getConfiguration();
    	//verse.setText("Locale="+config.locale.toString());
    	verse.setText(this.soulCalendar.getVerse());
    	
    	verse.setBackgroundResource(0);
    	switch (this.soulCalendar.getSeason())
    	{
    	case SoulCalendar.WINTER:
    		verse.setBackgroundResource(R.drawable.winter);
    		break;
    	case SoulCalendar.SPRING:
    		verse.setBackgroundResource(R.drawable.spring);
    		break;
    	case SoulCalendar.SUMMER:
    		verse.setBackgroundResource(R.drawable.summer);
    		break;
    	case SoulCalendar.AUTUMN:
    		verse.setBackgroundResource(R.drawable.autumn);
    		break;
    	}
    }
      

    /* Will return true if the text requires updating */
    private void restoreAppState() { 
		lang = settings.getString(APP_PREFS_LANG, getString(R.string.default_lang));
		float textSize = settings.getFloat(APP_PREFS_TEXT_SIZE, 
				((ZoomTextView)findViewById(R.id.verse)).getDefaultTextSize());
		((ZoomTextView)findViewById(R.id.verse)).setTextSize(textSize);
		
		this.soulCalendar.restoreSharedPreferences();
		this.soulCalendar.update(false);
    }
        
    
    /***** Menus and Listeners ****/  
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
        	showMainMenu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    

    private void showLanguageMenu() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    	builder.setTitle("");
    	
    	final TreeMap<String, String>supportedLocales = new TreeMap<String, String>();
    	supportedLocales.put("English", "en");
    	supportedLocales.put("עברית", "iw");
    	supportedLocales.put("Deutsch", "de");
    	supportedLocales.put("Català", "ca");
    	supportedLocales.put("Español", "es");
    	
   	
    	final String[] languages = new String[supportedLocales.size()]; 
    	supportedLocales.keySet().toArray(languages);
    	builder.setItems(languages,  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	lang = supportedLocales.get(languages[which]);
            	switchAppLanguage();
                updateVerse();
            }
    	});
    	
    	AlertDialog changeLang = builder.create();
    	changeLang.show();
    }    
    
    private void showAboutMenu() {
    	
    	AlertDialog.Builder builder 	= new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AboutAlertDialog));
    	LayoutInflater 		inflater	= getLayoutInflater();
    	View 				about_menu 	= inflater.inflate(R.layout.about, null);

    	builder.setTitle(getString(R.string.about));	
    	builder.setView(about_menu);
    	builder.setInverseBackgroundForced(true);
    	    	
        TextView version = (TextView)about_menu.findViewById(R.id.about_version_num);
        try {
        	String version_num = getString(R.string.version).concat(getBaseContext().getPackageManager().getPackageInfo(getBaseContext().getPackageName(), 0).versionName);
        	version.setText(version_num);
        }
        catch (Exception e) {
        	if(LOGGING) Log.e(MODULE_NAME, "Failed to get the version", e);
        	e.printStackTrace();
        	return;         	
        }
    	
        TextView source = (TextView)about_menu.findViewById(R.id.about_source);
        source.setText(Html.fromHtml(getString(R.string.source)));
        
        TextView author = (TextView)about_menu.findViewById(R.id.about_author);
        author.setText(Html.fromHtml(getString(R.string.app_author)));

        AlertDialog about = builder.create();
    	about.setCanceledOnTouchOutside(true);
    	about.show();
        source.setMovementMethod(LinkMovementMethod.getInstance());
        author.setMovementMethod(LinkMovementMethod.getInstance());
    }
    
    
    /*** Listeners ***/
    OnClickListener mainMenuButtonOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	showMainMenu();
        }
    };
    
    DialogInterface.OnClickListener mainMenuOptionsOnClickListener = new DialogInterface.OnClickListener() {
    	@Override
    	public void onClick(DialogInterface dialog, int which) {
    		MainActivity self = getSoulCalendarUi();
    		switch (which)
    		{
    		case CHANGE_TO_CURRENT_WEEK:
    			if(LOGGING) Log.v(MODULE_NAME, "User selcetd CHANGE_TO_CURRENT_WEEK");
    			self.soulCalendar.update(true);
    			break;
    		case CHANGE_TO_CORRESPONDING_WEEK:
    			if(LOGGING) Log.v(MODULE_NAME, "User selcetd CHANGE_TO_CORRESPONDING_WEEK");
    			self.soulCalendar.setCorrespondingWeek();
    			break;
    		case USER_SELECT_WEEK_NUM:
    			if(LOGGING) Log.v(MODULE_NAME, "User selcetd USER_SELECT_WEEK_NUM");
				allowUserToChooseWeek();
    			break;
    		case USER_SELECT_DATE:
    			if(LOGGING) Log.v(MODULE_NAME, "User selcetd USER_SELECT_DATE");
    			allowUserToChooseDate();
    			break;
    		case CHANGE_LANGUAGE:
    			if(LOGGING) Log.v(MODULE_NAME, "User selcetd CHANGE_LANGUAGE");
    			showLanguageMenu();
    			break;
    		case SHOW_ABOUT:
    			if(LOGGING) Log.v(MODULE_NAME, "User selcetd SHOW_ABOUT");
    			showAboutMenu();
    			break;
    		case SHARE_APP:
    			if(LOGGING) Log.v(MODULE_NAME, "User selcetd SHARE_APP");
		        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		        sharingIntent.setType("text/plain");
		        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.title));
		        String msg = getResources().getString(R.string.reccomend) + ". \n" + getResources().getString(R.string.link);
		        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, msg);
		        startActivity(Intent.createChooser(sharingIntent,"Share using"));
    		case QUIT:
    			if(LOGGING) Log.v(MODULE_NAME, "User selcetd QUIT");
    			self.onPause();
    			self.finish();
    			break;
			default:
				if(LOGGING) Log.e(MODULE_NAME, "User selcetd unknown options " + Integer.valueOf(which).toString());
				return;
    		}
    		self.updateVerse();
    	}
    };
    
    private void showMainMenu() {
    	if(LOGGING) Log.v(MODULE_NAME, "Main Menu button was clicked");
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    	builder.setTitle("");
		
		ArrayList<String> menu_options = new ArrayList<String>();
		menu_options.add(CHANGE_TO_CURRENT_WEEK, 	   getString(R.string.current_week));
		menu_options.add(CHANGE_TO_CORRESPONDING_WEEK, getString(R.string.corresponding_week));
		menu_options.add(USER_SELECT_WEEK_NUM,		   getString(R.string.select_week));
		menu_options.add(USER_SELECT_DATE, 			   getString(R.string.set_date));
		menu_options.add(CHANGE_LANGUAGE, 			   getString(R.string.change_lang));
		menu_options.add(SHARE_APP, 			   	   getString(R.string.share));
		menu_options.add(SHOW_ABOUT, 			   	   getString(R.string.about));
		menu_options.add(QUIT, 					   	   getString(R.string.quit));
		
		String[] menu_entries = new String[menu_options.size()];
		menu_options.toArray(menu_entries);
		
    	builder.setItems(menu_entries,  mainMenuOptionsOnClickListener);
    	AlertDialog menu = builder.create();
    	menu.show();
    	
    }
    
    private void allowUserToChooseWeek() {
    	numberPicker.setValue(this.soulCalendar.getWeek());
    	numberPicker.show();
    }
    
    private void allowUserToChooseDate() {
		Calendar date = this.soulCalendar.getDate();
    	DatePickerDialog datePicker = new DatePickerDialog(this, userDateSetListener, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
    	datePicker.setTitle(getString(R.string.set_date));
    	datePicker.show();
    }
    
    DatePickerDialog.OnDateSetListener userDateSetListener = new DatePickerDialog.OnDateSetListener() {
    	@Override
    	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
    		MainActivity self = getSoulCalendarUi();
    		Calendar date = Calendar.getInstance();
    		date.set(Calendar.YEAR, year);
    		date.set(Calendar.MONTH, monthOfYear);
    		date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    		self.soulCalendar.setDate(date);
    		self.updateVerse();
    	}
    };
 
	private ImageButton.OnClickListener okListener = new ImageButton.OnClickListener() {
		@Override
		public void onClick(View v) {
			numberPicker.dismiss();
	    	soulCalendar.setWeek(numberPicker.getValue());
	    	updateVerse();
		}
	};
	
	
	public void onFlingCB(int dir)
	{
		int week;
		boolean ltr = getString(R.string.dir).equalsIgnoreCase("LTR");
		if ( (ltr && ZoomTextViewCallback.LEFT == dir) || (!ltr && ZoomTextViewCallback.RIGHT == dir) ) {
			week = incrementWeek();
		}
		else {
			week = reduceWeek();
		}
		soulCalendar.setWeek(week);
		updateVerse();

	}
	
	private int reduceWeek() {
		int week = soulCalendar.getWeek() - 1;
		if (week < 1) {
			week = soulCalendar.getNumWeeks();
		}
		return week;
	}
	
	private int incrementWeek() {
		int week = soulCalendar.getWeek() + 1;
		if (week > soulCalendar.getNumWeeks()) {
			week = 1;
		}
		return week;
	}
}

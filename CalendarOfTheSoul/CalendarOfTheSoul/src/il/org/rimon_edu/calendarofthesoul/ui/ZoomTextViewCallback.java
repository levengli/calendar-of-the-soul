package il.org.rimon_edu.calendarofthesoul.ui;


public interface ZoomTextViewCallback {
	
	public static final int LEFT	= 0;
	public static final int RIGHT	= 1;

	public void onFlingCB(int dir);
	
}

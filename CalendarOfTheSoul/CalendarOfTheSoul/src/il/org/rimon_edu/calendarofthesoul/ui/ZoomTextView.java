package il.org.rimon_edu.calendarofthesoul.ui;

import il.org.rimon_edu.calendarofthesoul.R;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;

public class ZoomTextView extends TextView implements ZoomTextViewCallback {
	
	private static final String	 MODULE_NAME		= "ZoomTextView";
	private static final boolean LOGGING			= false;
	
	private final float				defaultTextSize;
	private float				 	currentTextSize;
	private final GestureDetector  	gestureDetector;
	
	public ZoomTextViewCallback 	callbacks		= this;	
	private float					origDist		= 0;
	private boolean 				scrollDetected = false; 

	// Constructor
	public ZoomTextView(Context context) {
		this(context, null);
	}
	
	// Constructor
	public ZoomTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.zoom_text_view, null);
		
		currentTextSize = getTextSize();
		defaultTextSize = currentTextSize;
		
		gestureDetector = new GestureDetector(simpleGestureListener);

	}
		
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(LOGGING) Log.v(MODULE_NAME, "onTouchEvent()");
	
		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
			if(LOGGING) Log.v(MODULE_NAME, "onTouchEvent(MotionEvent.ACTION_POINTER_DOWN)");
			origDist = distance(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1));
		}
		
		boolean ret = gestureDetector.onTouchEvent(event);
		
		if (scrollDetected && ret) {
			setMovementMethod(new ScrollingMovementMethod());
		}
		
		return ret;

	}
	
	@Override
	public void setTextSize(float size) {
		
		if (defaultTextSize > size) {
			if(LOGGING) Log.w(MODULE_NAME, "User tried setting " + size + " which is too small, overriding to "+defaultTextSize);
			size = defaultTextSize;
		} else if (size > 5 * defaultTextSize) {
			if(LOGGING) Log.w(MODULE_NAME, "User tried setting " + size + " which is too large, overriding to "+ 5*defaultTextSize);
			size = 5*defaultTextSize;
		}
		currentTextSize = size;
		if(LOGGING) Log.v(MODULE_NAME, "setTextSize(" + size + ")");
		super.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
	}

	
	public float getDefaultTextSize() {
		return defaultTextSize;
	}

	private float distance(float x, float y) {
		return (float) Math.sqrt(x * x + y * y);
	}
	
	

	private GestureDetector.OnGestureListener simpleGestureListener = new GestureDetector.OnGestureListener() {
	
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if(LOGGING) Log.v(MODULE_NAME, "onFling()");

			final ViewConfiguration vc = ViewConfiguration.get(getContext());
			final int swipeMinDistance = vc.getScaledTouchSlop();

			if (e2.getPointerCount() == 1 && !scrollDetected) {
				try {
			        // right to left swipe
					if(e1.getX() - e2.getX() > swipeMinDistance) {
						if(LOGGING) Log.v(MODULE_NAME, "onFling(left)");
						callbacks.onFlingCB(LEFT);
					}  
					else if (e2.getX() - e1.getX() > swipeMinDistance) {
						if(LOGGING) Log.v(MODULE_NAME, "onFling(right)");
						callbacks.onFlingCB(RIGHT);
					}
					else {
						if(LOGGING) Log.v(MODULE_NAME, "You gotta fling faster");
						return false;
					}
				} catch (Exception e) {
			    // nothing
			    }		
				
				return true;
			}
			scrollDetected = false;
			return false;
		
		}
	

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			if(LOGGING) Log.v(MODULE_NAME, "onScroll()");
				
			if (e2.getPointerCount() == 2) {
				
				float x, y;
				float newDist;
				
				x = e2.getX(0) - e2.getX(1);
				y = e2.getY(0) - e2.getY(1);
				newDist = distance(x, y);
								
                float scale = newDist / origDist;
				if (LOGGING) Log.v(MODULE_NAME, "origDist="+origDist+", newDist="+newDist);
				if (LOGGING) Log.v(MODULE_NAME, "currentTextSize="+currentTextSize+", scale="+scale);

				setTextSize(currentTextSize * scale);	
				origDist = newDist;
				scrollDetected = true;
				return true;
			}
            return false;            
		}
		
		
		
		@Override
		public void onLongPress(MotionEvent event) {
			if(LOGGING) Log.v(MODULE_NAME, "onLongPress()");
		}
		
		@Override
		public boolean onDown(MotionEvent event) {
			if(LOGGING) Log.v(MODULE_NAME, "onDown()");
			return true;
		}
		
		@Override
		public void onShowPress(MotionEvent event) {
			if(LOGGING) Log.v(MODULE_NAME, "onShowPress()");
		}
		
		@Override
		public boolean onSingleTapUp(MotionEvent event) {
			if(LOGGING) Log.v(MODULE_NAME, "onSingleTapUp()");
			return true;
		}
	};
	
	
	// Default callbacks
	@Override
	public void onFlingCB(int dir) {
		if(LOGGING) Log.v(MODULE_NAME, "Default onFlingCB() called");
	}
	
	
}

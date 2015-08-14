package numberPickerOverride;

import il.org.rimon_edu.numberpicker.R;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

public class NumberPickerOverride extends View {

	private int 				minVal;
	private int 				maxVal;
	private int 				previous;
	private Dialog				dialog = null;
	final static private String MODULE_NAME = "NUMVER_PICKER_OVERRIDE";
	
	
	public NumberPickerOverride(Context context) {
		super(context);
		this.minVal = 0xFFFFFFFF;
		this.maxVal = 0x7FFFFFFF;
		this.dialog = new Dialog(context);
		
		
		dialog.setCanceledOnTouchOutside(false);
		dialog.setContentView(R.layout.number_picker);
		dialog.findViewById(R.id.up).setOnClickListener(upListener);
		dialog.findViewById(R.id.down).setOnClickListener(downListener);
		dialog.findViewById(R.id.ok).setOnClickListener(cancelListener);	// default
		dialog.findViewById(R.id.cancel).setOnClickListener(cancelListener);	
		updateDisplay(0);
	}
		
		
	public int getMinNum() {
		return minVal;
	}

	public void setMinNum(int minNum) {
		this.minVal = minNum;
	}

	public int getMaxNum() {
		return maxVal;
	}

	public void setMaxNum(int maxNum) {
		this.maxVal = maxNum;
	}

	public int getValue() {
		EditText v = (EditText)dialog.findViewById(R.id.val);
		String str = v.getText().toString();
		return Integer.valueOf(str);
	}
	
	public void setTitle(String title) {
		this.dialog.setTitle(title);
	}

	public void setValue(int selectedNumber) {
		if (selectedNumber >= getMinNum() && selectedNumber <= getMaxNum()) {
			updateDisplay(selectedNumber);
		}
		else
		{
			Log.e(MODULE_NAME, new String("Illegal value ").concat(Integer.valueOf(selectedNumber).toString()));
		}
	}
	
	private void updateDisplay(int newVal) {
		EditText val = (EditText)this.dialog.findViewById(R.id.val);
		val.setText(Integer.valueOf(newVal).toString());
	}
		
	public void show() {
		previous = getValue();
		this.dialog.show();
	}
	
	public void dismiss() {
		this.dialog.dismiss();
	}
	
	public void setSubmitListener(ImageButton.OnClickListener listener) {
		dialog.findViewById(R.id.ok).setOnClickListener(listener);
	}
		
	private ImageButton.OnClickListener upListener = new ImageButton.OnClickListener() {
		@Override
		public void onClick(View v) {
			int tempValue = getValue();
			if (++tempValue > getMaxNum())
				tempValue = getMinNum();
			updateDisplay(tempValue);
		}
	};
	
	private ImageButton.OnClickListener downListener = new ImageButton.OnClickListener() {
		@Override
		public void onClick(View v) {
			int tempValue = getValue();
			if (--tempValue < getMinNum())
				tempValue = getMaxNum();
			updateDisplay(tempValue);
		}
	};
	
	private ImageButton.OnClickListener cancelListener = new ImageButton.OnClickListener() {
		@Override
		public void onClick(View v) {
			updateDisplay(previous);
			dialog.dismiss();
		}
	};

}

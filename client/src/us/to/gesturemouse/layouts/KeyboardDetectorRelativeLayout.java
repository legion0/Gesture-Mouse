package us.to.gesturemouse.layouts;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

public class KeyboardDetectorRelativeLayout extends RelativeLayout {

	public interface IKeyboardChanged {
		void onKeyboardShown();

		void onKeyboardHidden();
	}

	private ArrayList<IKeyboardChanged> keyboardListener = new ArrayList<IKeyboardChanged>();
	private Context context;

	public KeyboardDetectorRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	public KeyboardDetectorRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public KeyboardDetectorRelativeLayout(Context context) {
		super(context);
		this.context = context;
	}

	public void inflate(int layout) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(layout, this);
	}

	public void addKeyboardStateChangedListener(IKeyboardChanged listener) {
		keyboardListener.add(listener);
	}

	public void removeKeyboardStateChangedListener(IKeyboardChanged listener) {
		keyboardListener.remove(listener);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
		final int actualHeight = getHeight();

		if (actualHeight > proposedheight) {
			notifyKeyboardShown();
		} else if (actualHeight < proposedheight) {
			notifyKeyboardHidden();
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	private void notifyKeyboardHidden() {
		for (IKeyboardChanged listener : keyboardListener) {
			listener.onKeyboardHidden();
		}
	}

	private void notifyKeyboardShown() {
		for (IKeyboardChanged listener : keyboardListener) {
			listener.onKeyboardShown();
		}
	}

}
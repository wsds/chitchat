package com.open.chitchat.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class InputMethodManagerUtils {

	private InputMethodManager mInputMethodManager;

	@SuppressWarnings("static-access")
	public InputMethodManagerUtils(Context content) {
		mInputMethodManager = (InputMethodManager) content.getSystemService(content.INPUT_METHOD_SERVICE);
	}

	public void show(EditText editText) {
		editText.requestFocus();
		mInputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
	}

	public void hide(EditText editText) {
		mInputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}
}

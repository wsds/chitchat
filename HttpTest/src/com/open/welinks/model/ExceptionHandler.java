package com.open.welinks.model;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;

import com.open.lib.MyLog;

public class ExceptionHandler implements UncaughtExceptionHandler {
	public String tag = "ExceptionHandler";
	public MyLog log = new MyLog(tag, true);

	// ************************Exception System*************************
	public static String printStackTrace(Context context, Exception e) {
		StackTraceElement[] parentStack = null;
		String indent = "";
		StringBuffer err = new StringBuffer();
		err.append(toString2(e, context));
		err.append("\n");
		StackTraceElement[] stack = e.getStackTrace();
		if (stack != null) {
			int duplicates = parentStack != null ? countDuplicates(stack, parentStack) : 0;
			for (int i = 0; i < stack.length - duplicates; i++) {
				err.append(indent);
				err.append("\tat ");
				err.append(stack[i].toString());
				err.append("\n");
			}

			if (duplicates > 0) {
				err.append(indent);
				err.append("\t... ");
				err.append(Integer.toString(duplicates));
				err.append(" more\n");
			}
		}

		Throwable cause = e.getCause();
		if (cause != null) {
			err.append(indent);
			err.append("Caused by: ");
			printStackTrace(context, e);
		}
		return err.toString();
	}

	public static String toString2(Exception e, Context context) {
		String msg = e.getLocalizedMessage();
		String name = context.getClass().getName();
		if (msg == null) {
			return name;
		}
		return name + ": " + msg;
	}

	private static int countDuplicates(StackTraceElement[] currentStack, StackTraceElement[] parentStack) {
		int duplicates = 0;
		int parentIndex = parentStack.length;
		for (int i = currentStack.length; --i >= 0 && --parentIndex >= 0;) {
			StackTraceElement parentFrame = parentStack[parentIndex];
			if (parentFrame.equals(currentStack[i])) {
				duplicates++;
			} else {
				break;
			}
		}
		return duplicates;
	}
	// ****** **** **** *** ***** ********** ********* ************ ********* ************

	@Override
	public void uncaughtException(Thread arg0, Throwable arg1) {
		// TODO Auto-generated method stub
		
	}
}

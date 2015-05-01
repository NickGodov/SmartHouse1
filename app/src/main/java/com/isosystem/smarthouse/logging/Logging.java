package com.isosystem.smarthouse.logging;

import android.text.TextUtils;

import com.isosystem.smarthouse.Globals;

import java.text.SimpleDateFormat;

public final class Logging {


    public static void v(String msg) {
    	// Логи пишутся только в режиме отладки
    	if (Globals.DEBUG_MODE) {	
    		SimpleDateFormat s = new SimpleDateFormat("HH:mm:ss");
    		String format = s.format(System.currentTimeMillis());
    		
            android.util.Log.v(Globals.LOG_TAG, getLocation() + "[" + format + "]" + msg);
    	}
    }
    
    private static String getLocation() {
        final String className = Logging.class.getName();
        final StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        boolean found = false;

        for (StackTraceElement trace : traces) {
            try {
                if (found) {
                    if (!trace.getClassName().startsWith(className)) {
                        Class<?> clazz = Class.forName(trace.getClassName());
                        return "[" + getClassName(clazz) + ":" + trace.getMethodName() + ":" + trace.getLineNumber() + "]: ";
                    }
                }
                else if (trace.getClassName().startsWith(className)) {
                    found = true;
                }
            }
            catch (ClassNotFoundException e) {
            }
        }
        return "[]: ";
    }

    private static String getClassName(Class<?> clazz) {
        if (clazz != null) {
            if (!TextUtils.isEmpty(clazz.getSimpleName())) {
                return clazz.getSimpleName();
            }
            return getClassName(clazz.getEnclosingClass());
        }
        return "";
    }
}
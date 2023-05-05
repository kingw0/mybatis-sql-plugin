package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.spi.LoggerFactoryBinder;

import java.util.Iterator;

public class StaticLoggerBinder implements LoggerFactoryBinder {
    private static StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return new ILoggerFactory() {
            @Override
            public Logger getLogger(String s) {
                return new Logger() {
                    private void logPrint(String level, Marker marker, Throwable throwable, String format,
                                          Object... args) {
                        String info = String.format(format.replaceAll("\\{\\}", "%s"), args);

                        String markerStr = "";

                        if (marker != null) {
                            StringBuilder markerBuilder = new StringBuilder();

                            Iterator<Marker> iter = marker.iterator();

                            while (iter.hasNext()) {
                                markerBuilder.append(iter.next().getName()).append(',');
                                iter.remove();
                            }

                            markerStr = markerBuilder.toString();
                        }

                        System.out.println(String.format("%s - %s - %s", level, markerStr, info));

                        if (throwable != null) {
                            throwable.printStackTrace();
                        }
                    }

                    @Override
                    public String getName() {
                        return "TestConsole";
                    }

                    @Override
                    public boolean isTraceEnabled() {
                        return true;
                    }

                    @Override
                    public void trace(String s) {
                        logPrint("trace", null, null, s);
                    }

                    @Override
                    public void trace(String s, Object o) {
                        logPrint("trace", null, null, s, o);
                    }

                    @Override
                    public void trace(String s, Object o, Object o1) {
                        logPrint("trace", null, null, s, o, o1);
                    }

                    @Override
                    public void trace(String s, Object... objects) {
                        logPrint("trace", null, null, s, objects);
                    }

                    @Override
                    public void trace(String s, Throwable throwable) {
                        logPrint("trace", null, throwable, s);
                    }

                    @Override
                    public boolean isTraceEnabled(Marker marker) {
                        return true;
                    }

                    @Override
                    public void trace(Marker marker, String s) {
                        logPrint("trace", marker, null, s);
                    }

                    @Override
                    public void trace(Marker marker, String s, Object o) {
                        logPrint("trace", marker, null, s, o);
                    }

                    @Override
                    public void trace(Marker marker, String s, Object o, Object o1) {
                        logPrint("trace", marker, null, s, o, o1);
                    }

                    @Override
                    public void trace(Marker marker, String s, Object... objects) {
                        logPrint("trace", marker, null, s, objects);
                    }

                    @Override
                    public void trace(Marker marker, String s, Throwable throwable) {
                        logPrint("trace", marker, throwable, s);
                    }

                    @Override
                    public boolean isInfoEnabled() {
                        return false;
                    }

                    @Override
                    public void info(String s) {
                        logPrint("info", null, null, s);
                    }

                    @Override
                    public void info(String s, Object o) {
                        logPrint("info", null, null, s, o);
                    }

                    @Override
                    public void info(String s, Object o, Object o1) {
                        logPrint("info", null, null, s, o, o1);
                    }

                    @Override
                    public void info(String s, Object... objects) {
                        logPrint("info", null, null, s, objects);
                    }

                    @Override
                    public void info(String s, Throwable throwable) {
                        logPrint("info", null, throwable, s);
                    }

                    @Override
                    public boolean isInfoEnabled(Marker marker) {
                        return true;
                    }

                    @Override
                    public void info(Marker marker, String s) {
                        logPrint("info", marker, null, s);
                    }

                    @Override
                    public void info(Marker marker, String s, Object o) {
                        logPrint("info", marker, null, s, o);
                    }

                    @Override
                    public void info(Marker marker, String s, Object o, Object o1) {
                        logPrint("info", marker, null, s, o, o1);
                    }

                    @Override
                    public void info(Marker marker, String s, Object... objects) {
                        logPrint("info", marker, null, s, objects);
                    }

                    @Override
                    public void info(Marker marker, String s, Throwable throwable) {
                        logPrint("info", marker, throwable, s);
                    }

                    @Override
                    public boolean isWarnEnabled() {
                        return false;
                    }

                    @Override
                    public void warn(String s) {
                        logPrint("warn", null, null, s);
                    }

                    @Override
                    public void warn(String s, Object o) {
                        logPrint("warn", null, null, s, o);
                    }

                    @Override
                    public void warn(String s, Object o, Object o1) {
                        logPrint("warn", null, null, s, o, o1);
                    }

                    @Override
                    public void warn(String s, Object... objects) {
                        logPrint("warn", null, null, s, objects);
                    }

                    @Override
                    public void warn(String s, Throwable throwable) {
                        logPrint("warn", null, throwable, s);
                    }

                    @Override
                    public boolean isWarnEnabled(Marker marker) {
                        return true;
                    }

                    @Override
                    public void warn(Marker marker, String s) {
                        logPrint("warn", marker, null, s);
                    }

                    @Override
                    public void warn(Marker marker, String s, Object o) {
                        logPrint("warn", marker, null, s, o);
                    }

                    @Override
                    public void warn(Marker marker, String s, Object o, Object o1) {
                        logPrint("warn", marker, null, s, o, o1);
                    }

                    @Override
                    public void warn(Marker marker, String s, Object... objects) {
                        logPrint("warn", marker, null, s, objects);
                    }

                    @Override
                    public void warn(Marker marker, String s, Throwable throwable) {
                        logPrint("warn", marker, throwable, s);
                    }

                    @Override
                    public boolean isErrorEnabled() {
                        return false;
                    }

                    @Override
                    public void error(String s) {
                        logPrint("error", null, null, s);
                    }

                    @Override
                    public void error(String s, Object o) {
                        logPrint("error", null, null, s, o);
                    }

                    @Override
                    public void error(String s, Object o, Object o1) {
                        logPrint("error", null, null, s, o, o1);
                    }

                    @Override
                    public void error(String s, Object... objects) {
                        logPrint("error", null, null, s, objects);
                    }

                    @Override
                    public void error(String s, Throwable throwable) {
                        logPrint("error", null, throwable, s);
                    }

                    @Override
                    public boolean isErrorEnabled(Marker marker) {
                        return true;
                    }

                    @Override
                    public void error(Marker marker, String s) {
                        logPrint("error", marker, null, s);
                    }

                    @Override
                    public void error(Marker marker, String s, Object o) {
                        logPrint("error", marker, null, s, o);
                    }

                    @Override
                    public void error(Marker marker, String s, Object o, Object o1) {
                        logPrint("error", marker, null, s, o, o1);
                    }

                    @Override
                    public void error(Marker marker, String s, Object... objects) {
                        logPrint("error", marker, null, s, objects);
                    }

                    @Override
                    public void error(Marker marker, String s, Throwable throwable) {
                        logPrint("error", marker, throwable, s);
                    }

                    @Override
                    public boolean isDebugEnabled() {
                        return false;
                    }

                    @Override
                    public void debug(String s) {
                        logPrint("debug", null, null, s);
                    }

                    @Override
                    public void debug(String s, Object o) {
                        logPrint("debug", null, null, s, o);
                    }

                    @Override
                    public void debug(String s, Object o, Object o1) {
                        logPrint("debug", null, null, s, o, o1);
                    }

                    @Override
                    public void debug(String s, Object... objects) {
                        logPrint("debug", null, null, s, objects);
                    }

                    @Override
                    public void debug(String s, Throwable throwable) {
                        logPrint("debug", null, throwable, s);
                    }

                    @Override
                    public boolean isDebugEnabled(Marker marker) {
                        return true;
                    }

                    @Override
                    public void debug(Marker marker, String s) {
                        logPrint("debug", marker, null, s);
                    }

                    @Override
                    public void debug(Marker marker, String s, Object o) {
                        logPrint("debug", marker, null, s, o);
                    }

                    @Override
                    public void debug(Marker marker, String s, Object o, Object o1) {
                        logPrint("debug", marker, null, s, o, o1);
                    }

                    @Override
                    public void debug(Marker marker, String s, Object... objects) {
                        logPrint("debug", marker, null, s, objects);
                    }

                    @Override
                    public void debug(Marker marker, String s, Throwable throwable) {
                        logPrint("debug", marker, throwable, s);
                    }
                };
            }
        };
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return null;
    }
}

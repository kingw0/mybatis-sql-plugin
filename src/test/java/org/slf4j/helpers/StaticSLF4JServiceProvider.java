package org.slf4j.helpers;

import org.slf4j.ILoggerFactory;
import org.slf4j.helpers.NOP_FallbackServiceProvider;
import org.slf4j.impl.StaticLoggerBinder;
import org.slf4j.spi.LoggerFactoryBinder;

public class StaticSLF4JServiceProvider extends NOP_FallbackServiceProvider {
    LoggerFactoryBinder BINDER = StaticLoggerBinder.getSingleton();

    @Override
    public ILoggerFactory getLoggerFactory() {
        return BINDER.getLoggerFactory();
    }
}

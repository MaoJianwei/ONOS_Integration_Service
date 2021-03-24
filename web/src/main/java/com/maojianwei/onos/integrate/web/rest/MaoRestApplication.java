package com.maojianwei.onos.integrate.web.rest;

import org.onlab.rest.AbstractWebApplication;

import java.util.Set;


public class MaoRestApplication extends AbstractWebApplication {
    @Override
    public Set<Class<?>> getClasses() {
        return getClasses(MaoRestApi.class);
    }
}

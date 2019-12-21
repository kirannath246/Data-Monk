package com.knowledge.datasource.commons;

import java.util.Set;

import com.knowledge.datasource.data.ExtDataProcessor;

public interface ExternalDataSource {


    public void openDataSource() throws Exception;

    public void closeDataSource() throws Exception;

    public void query(String dataSelectQuery, ExtDataProcessor edp) throws Exception;

    public Set<ExternalObject> getObjects() throws Exception;

    public Set<ExternalObjectAttribute> getObjectAttribute(String objectName) throws Exception;

    boolean testConnection();
}

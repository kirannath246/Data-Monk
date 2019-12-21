package com.knowledge.datasource;

import java.util.Properties;

import com.knowledge.datasource.commons.ExternalDataSource;
import com.knowledge.datasource.salesforce.ExternalSalesForceSource;



public class ExternalDataSourceFactory {

    public ExternalDataSource getExternalDataSource(Properties properties) {

        return new ExternalSalesForceSource(properties);
    }

}

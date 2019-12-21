package com.knowledge.data_source.test;

import java.text.SimpleDateFormat;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import com.knowledge.datasource.commons.ConnectionParameters;
import com.knowledge.datasource.data.ExtDataProcessor;
import com.knowledge.datasource.data.ExternalDataSet;
import com.knowledge.datasource.salesforce.ExternalSalesForceSource;

public class SalesForceSourceTest {

    static final Properties properties = new Properties();

    static final ExternalSalesForceSource salesforce = new ExternalSalesForceSource(properties);

    static final String previousDateString = "1970-01-01T00:00:00.000Z";

    static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @BeforeClass
    public static void beforeClass() {

        properties.setProperty(ConnectionParameters.PASSWORD, "pass");
        properties.setProperty(ConnectionParameters.USERNAME, "user");
        properties.setProperty(ConnectionParameters.CONSUMER_KEY, "3MVG9pe2TCoA1Pf4kQ18pwNWfVWFoBA2ff0rlm87EuHpuErjjRUAnpJqdsCOdSzG2tFYkzvwH6U5cRSGfy8JC");
        properties.setProperty(ConnectionParameters.CONSUMER_SECRET, "6852858376433491005");
        properties.setProperty(ConnectionParameters.SECURITY_TOKEN, "XyrleGTVAFQLN1xhA1zH1Y0CF");
        properties.setProperty(ConnectionParameters.SALES_FORCE_URL, "https://login.salesforce.com");
        properties.setProperty(ConnectionParameters.SALES_FORCE_VERSION, "44.0");

    }

    @Test
    public void salesforceAttributeListing() throws Exception {
        System.out.println(salesforce.getObjectAttribute("Account"));
    }

    @Test
    public void salesforceObjectlisting() throws Exception {
        System.out.println(salesforce.getObjects());
        System.out.println(salesforce.getObjects().size());
    }

    @Test
    public void salesforceQueryExecution() throws Exception {

        salesforce.query("SELECT Id FROM Account", new ExtDataProcessor() {

            @Override
            public void start() {
            }

            @Override
            public void processRow(ExternalDataSet data) throws Exception {
                for (int i = 0; i < data.getColumnCount(); i++) {
                    if (i == 4) {
                        System.out.print(format.parse(data.getString(i)));
                    } else {
                        System.out.print(data.getString(i) + ";");
                    }
                }
                System.out.print("\n");
            }

            @Override
            public void finish() {
            }
        });
    }

    @Test
    public void connectionTest() {
        System.out.println(salesforce.testConnection());
    }
}


package com.knowledge.datasource.salesforce;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.knowledge.datasource.commons.ConnectionParameters;
import com.knowledge.datasource.commons.ExternalAttributeType;
import com.knowledge.datasource.commons.ExternalDataSource;
import com.knowledge.datasource.commons.ExternalObject;
import com.knowledge.datasource.commons.ExternalObjectAttribute;
import com.knowledge.datasource.data.ExtDataProcessor;
import com.knowledge.datasource.data.SalesforceDataSet;
import com.sforce.async.AsyncApiException;
import com.sforce.async.BulkConnection;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * The ExternalSalesForceSource class
 *
 * @author kiran
 *
 */
@Slf4j
public class ExternalSalesForceSource implements ExternalDataSource {

    private final Properties properties;

    private PartnerConnection partnerConnection;

    private BulkConnection connection;

    private Set<ExternalObject> objects;

    private static final Map<String, Set<ExternalObjectAttribute>> attributeListCache = new HashMap<>();

    private int pkChunkingSize = 10000;

    boolean isPkChunkingEnabled = true;

    private static final int MAX_PK_CHUNKING_SIZE = 250000;

    public ExternalSalesForceSource(Properties connectionProperties) {
        properties = connectionProperties;
    }

    public ExternalSalesForceSource(Properties connectionProperties, boolean isPkChunkingEnabled, int pkChunkingSize) {
        properties = connectionProperties;
        this.isPkChunkingEnabled = isPkChunkingEnabled;
        this.pkChunkingSize = pkChunkingSize > MAX_PK_CHUNKING_SIZE ? MAX_PK_CHUNKING_SIZE : pkChunkingSize;

    }

    @Override
    public void openDataSource() throws Exception {
        connection = getBulkConnection(properties.getProperty(ConnectionParameters.USERNAME),
                properties.getProperty(ConnectionParameters.PASSWORD) + properties.getProperty(ConnectionParameters.SECURITY_TOKEN));
        if (isPkChunkingEnabled) {
            connection.addHeader("Sforce-Enable-PKChunking", "chunkSize=" + pkChunkingSize);
        }
    }

    @Override
    public void closeDataSource() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void query(String dataSelectQuery, ExtDataProcessor edp) throws Exception {
        dataSelectQuery = dataSelectQuery.toLowerCase();
        openDataSource();
        edp.start();

        String object;

        final int split = dataSelectQuery.indexOf("from");

        object = dataSelectQuery.substring(split + 5);
        if (object.indexOf(" ") != -1) {
            object = object.substring(0, object.indexOf(" "));
        }
        final List<String> fieldList = new ArrayList<>(Arrays.asList(dataSelectQuery.substring(dataSelectQuery.indexOf("select") + 6, dataSelectQuery.indexOf("from")).trim().split(",")));
        processQuery(object, dataSelectQuery, fieldList, edp);
        edp.finish();

    }

    @Override
    public Set<ExternalObject> getObjects() throws Exception {
        openDataSource();
        objects = new HashSet<>();
        final DescribeGlobalSObjectResult[] sobjects = partnerConnection.describeGlobal().getSobjects();
        for (final DescribeGlobalSObjectResult sobject : sobjects) {
            final ExternalObject eObject = new ExternalObject();
            eObject.setObjectName(sobject.getName());
            eObject.setType("object");
            objects.add(eObject);
        }
        return objects;
    }

    @Override
    public Set<ExternalObjectAttribute> getObjectAttribute(String objectName) throws Exception {
        if (attributeListCache.containsKey(objectName)) {
            return attributeListCache.get(objectName);
        } else {
            openDataSource();
            final DescribeSObjectResult result = partnerConnection.describeSObject(objectName);
            final Field[] fields = result.getFields();
            final Set<ExternalObjectAttribute> attributeList = new HashSet<>();
            for (final Field field : fields) {
                final ExternalObjectAttribute attributeObj = new ExternalObjectAttribute();
                attributeObj.setAttributeName(field.getName());
                attributeObj.setType(getAttributeType(field.getType().toString()));
                attributeList.add(attributeObj);

            }
            if (!attributeListCache.containsKey(objectName)) {
                attributeListCache.put(objectName, attributeList);
            }
            return attributeList;
        }

    }

    private ExternalAttributeType getAttributeType(String type) {
        switch (type) {
        case "id":
        case "string":
        case "address":
        case "reference":
        case "picklist":
        case "textarea":
        case "url":
        case "phone":
        case "location":
            return ExternalAttributeType.STRING;

        case "int":
        case "double":
            return ExternalAttributeType.NUMBER;

        case "datetime":
        case "date":
            return ExternalAttributeType.DATE;

        case "boolean":
            return ExternalAttributeType.BOOLEAN;

        default:
            return null;

        }

    }

    @Override
    public boolean testConnection() {
        try {
            openDataSource();
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Creates a Bulk API job and query batches for a String.
     *
     * @param edp
     */
    public void processQuery(String sobjectType, String query, List<String> fieldList, ExtDataProcessor edp) throws AsyncApiException, ConnectionException, IOException, InterruptedException {

        QueryResult qr = partnerConnection.query(query);

        boolean done = false;
        int loopCount = 0;
        // Loop through the batches of returned results
        while (!done) {
            System.out.println("Records in results set " + loopCount++ + " - ");
            final SObject[] records = qr.getRecords();
            // Process the query results

            for (final SObject sobj : records) {
                final List<String> fields = new ArrayList<>();
                for (final String field : fieldList) {
                    sobj.getField(field);
                    fields.add(field);
                }
                final SalesforceDataSet dataset = new SalesforceDataSet(fields);
                try {
                    edp.processRow(dataset);
                } catch (final Exception e) {
                    log.error("ExternalSalesForceSource|checkResults|ExternalDataProcessor|ProcessRow failed for record :" + fields, e);
                }

            }
            if (qr.isDone()) {
                done = true;
            } else {
                qr = partnerConnection.queryMore(qr.getQueryLocator());
            }

        }
        partnerConnection.logout();

    }

    /**
     * Create the BulkConnection used to call Bulk API operations.
     */
    private BulkConnection getBulkConnection(String userName, String password) throws ConnectionException, AsyncApiException {
        final ConnectorConfig partnerConfig = new ConnectorConfig();
        partnerConfig.setUsername(userName);
        partnerConfig.setPassword(password);
        partnerConfig.setAuthEndpoint("https://login.salesforce.com/services/Soap/u/47.0");
        // Creating the connection automatically handles login and stores
        // the session in partnerConfig
        partnerConnection = new PartnerConnection(partnerConfig);
        // When PartnerConnection is instantiated, a login is implicitly
        // executed and, if successful,
        // a valid session is stored in the ConnectorConfig instance.
        // Use this key to initialize a BulkConnection:
        final ConnectorConfig config = new ConnectorConfig();
        config.setSessionId(partnerConfig.getSessionId());
        // The endpoint for the Bulk API service is the same as for the normal
        // SOAP uri until the /Soap/ part. From here it's '/async/versionNumber'
        final String soapEndpoint = partnerConfig.getServiceEndpoint();
        final String apiVersion = "47.0";
        final String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/" + apiVersion;
        config.setRestEndpoint(restEndpoint);
        // This should only be false when doing debugging.
        config.setCompression(true);
        // Set this to true to see HTTP requests and responses on stdout
        config.setTraceMessage(false);
        return new BulkConnection(config);
    }

}

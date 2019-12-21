
package com.knowledge.datasource.data;

public interface ExtDataProcessor {

    void start() throws Exception;

    void processRow(ExternalDataSet data) throws Exception;

    void finish() throws Exception;

}

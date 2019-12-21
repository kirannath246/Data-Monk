
package com.knowledge.datasource.data;

import java.util.List;

/**
 * The SalesforceDataSet class
 *
 * @author kiran
 *
 */
public class SalesforceDataSet implements ExternalDataSet {

    private final List<String> rowFields;

    public SalesforceDataSet(List<String> rowFields) {

        this.rowFields = rowFields;

    }

    @Override
    public boolean hasNext() throws Exception {
        return false;
    }

    @Override
    public int getColumnCount() throws Exception {

        return rowFields.size();
    }

    @Override
    public String getString(int position) throws Exception {
        return rowFields.get(position);
    }

    @Override
    public Integer getInteger(int position) throws Exception {
        return Integer.parseInt(rowFields.get(position));
    }

    @Override
    public Long getLong(int position) throws Exception {
        return Long.parseLong(rowFields.get(position));
    }

    @Override
    public Double getDouble(int position) throws Exception {
        return Double.parseDouble(rowFields.get(position));
    }

}

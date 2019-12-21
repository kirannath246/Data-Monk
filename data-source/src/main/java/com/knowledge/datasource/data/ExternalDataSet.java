
package com.knowledge.datasource.data;

public interface ExternalDataSet {

    public boolean hasNext() throws Exception;

    public int getColumnCount() throws Exception;

    public String getString(int position) throws Exception;

    public Integer getInteger(int position) throws Exception;

    public Long getLong(int position) throws Exception;

    public Double getDouble(int position) throws Exception;

}

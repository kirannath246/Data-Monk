package com.knowledge.datasource.commons;

import lombok.Data;

@Data
public class ExternalObject implements Comparable<ExternalObject> {

    private String objectName;

    private String type;

    @Override
    public int compareTo(ExternalObject o) {
        return this.objectName.compareTo(o.objectName);
    }

}
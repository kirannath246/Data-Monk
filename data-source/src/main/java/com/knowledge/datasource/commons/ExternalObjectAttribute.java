
package com.knowledge.datasource.commons;

import lombok.Data;

@Data
public class ExternalObjectAttribute implements Comparable<ExternalObjectAttribute> {

    private String attributeName;

    private ExternalAttributeType type;

    @Override
    public int compareTo(ExternalObjectAttribute o) {
        return attributeName.compareTo(o.attributeName);
    }

}

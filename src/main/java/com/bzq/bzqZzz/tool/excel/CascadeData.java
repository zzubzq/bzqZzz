package com.bzq.bzqZzz.tool.excel;

import java.io.Serializable;
import java.util.List;

/**
 * @author bai
 * @date 2019-04-12 10:26
 */
public class CascadeData implements Serializable {

    public CascadeData() {
    }

    public CascadeData(String key) {
        this.key = key;
    }

    private static final long serialVersionUID = 1L;

    private String key;

    private List<String> valueList;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getValueList() {
        return valueList;
    }

    public void setValueList(List<String> valueList) {
        this.valueList = valueList;
    }
}

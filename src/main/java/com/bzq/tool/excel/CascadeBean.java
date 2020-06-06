package com.bzq.tool.excel;

import java.io.Serializable;
import java.util.List;

/**
 * @author zzubzq
 * 级联关系描述bean
 */
public class CascadeBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String keyField;

    private String valueField;

    private List<CascadeData> cascadeDataList;

    public String getKeyField() {
        return keyField;
    }

    public void setKeyField(String keyField) {
        this.keyField = keyField;
    }

    public String getValueField() {
        return valueField;
    }

    public void setValueField(String valueField) {
        this.valueField = valueField;
    }

    public List<CascadeData> getCascadeDataList() {
        return cascadeDataList;
    }

    public void setCascadeDataList(List<CascadeData> cascadeDataList) {
        this.cascadeDataList = cascadeDataList;
    }
}

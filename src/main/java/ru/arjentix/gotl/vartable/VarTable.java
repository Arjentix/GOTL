package ru.arjentix.gotl.vartable;

import java.util.HashMap;

public class VarTable {

    private class VarData {
        public String type;
        public String value;

        public VarData(String type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    private HashMap<String, VarData> hashMap;

    public VarTable() {
        hashMap = new HashMap<>();
    }

    public void add(String var, String value) {
        hashMap.put(var, new VarData("int", value));
    }

    public void add(String var, String type, String value) {
        hashMap.put(var, new VarData(type, value));
    }

    public String getType(String var) {
        return hashMap.get(var).type;
    }

    public String getValue(String var) {
        return hashMap.get(var).value;
    }

    public void setType(String var, String type) {
        hashMap.get(var).type = type;
    }

    public void setValue(String var, String value) {
        hashMap.get(var).value = value;
    }
    
}
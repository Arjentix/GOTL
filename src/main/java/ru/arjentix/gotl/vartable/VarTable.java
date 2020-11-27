package ru.arjentix.gotl.vartable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VarTable {

  public static class VarData {
    public String type;
    public Object value;

    public VarData(String type, Object value) {
      this.type = type;
      this.value = value;
    }

    public String toString() {
      return "{" + type + ", " + value + "}";
    }
  }

  private HashMap<String, VarData> hashMap;
  private static VarTable instance;

  private VarTable() {
    hashMap = new HashMap<>();
  }

  public static VarTable getInstance() {
    if (instance == null) {
      instance = new VarTable();
    }

    return instance;
  }

  public void add(String var, Object value) {
    hashMap.put(var, new VarData("int", value));
  }

  public void add(String var, String type, Object value) {
    hashMap.put(var, new VarData(type, value));
  }

  public void remove(String var) {
    hashMap.remove(var);
  }

  public boolean contains(String var) {
    return hashMap.containsKey(var);
  }

  public String getType(String var) {
    return hashMap.get(var).type;
  }

  public Object getValue(String var) {
    return hashMap.get(var).value;
  }

  public void setType(String var, String type) {
    hashMap.get(var).type = type;
  }

  public void setValue(String var, Object value) {
    hashMap.get(var).value = value;
  }

  public Set<String> keySet() {
    return hashMap.keySet();
  }

  public Map<String, VarData> getData() {
    return hashMap;
  }

  public void setData(Map<String, VarData> data) {
    hashMap = new HashMap<>(data);
  }

  public void clear() {
    hashMap.clear();
  }

  public String toString() {
    StringBuilder builder = new StringBuilder("{");
    boolean first = true;

    for (Map.Entry<String, VarData> entry : hashMap.entrySet()) {
      if (!first) {
        builder.append(", ");
      }
      builder.append("[" + entry.getKey() + " : " + entry.getValue() + "]");
      first = false;
    }
    builder.append("}");

    return builder.toString();
  }

}

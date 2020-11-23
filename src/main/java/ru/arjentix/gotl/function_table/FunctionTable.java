package ru.arjentix.gotl.function_table;

import java.util.HashMap;
import java.util.Map;

public class FunctionTable extends HashMap<String, Function> {
  private static FunctionTable instance;

  private FunctionTable() {
    super();
  }

  public static FunctionTable getInstance() {
    if (instance == null) {
      instance = new FunctionTable();
    }

    return instance;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("{");
    boolean first = true;

    for (Map.Entry<String, Function> entry : super.entrySet()) {
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

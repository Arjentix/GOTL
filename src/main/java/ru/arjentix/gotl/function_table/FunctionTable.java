package ru.arjentix.gotl.function_table;

import java.util.HashMap;

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
}

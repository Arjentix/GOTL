package ru.arjentix.gotl.type_table;

import ru.arjentix.gotl.type_table.Method;

import java.util.HashMap;
import java.util.List;

public class TypeTable extends HashMap<String, List<Method>> {
  private static TypeTable instance;

  private TypeTable() {
    super();
  }

  public static TypeTable getInstance() {
    if (instance == null) {
      instance = new TypeTable();
    }

    return instance;
  }
}

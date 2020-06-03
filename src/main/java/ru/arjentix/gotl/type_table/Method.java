package ru.arjentix.gotl.type_table;

import java.util.List;
import java.util.function.BiFunction;

public class Method {
    private String name;
    private List<String> paramTypes;
    private String returnType;
    private BiFunction<Object, Object, Object> implementation;

    public Method(String name, List<String> paramTypes, String returnType,
                  BiFunction<Object, Object, Object> implementation) {
      this.name = name;
      this.paramTypes = paramTypes;
      this.returnType = returnType;
      this.implementation = implementation;
    }

    public String getName() {
        return name;
    }

    public List<String> getParamTypes() {
        return paramTypes;
    }

    public String getReturnType() {
      return returnType;
    }

    public Object invoke(Object arg0, Object arg1) {
        return implementation.apply(arg0, arg1);
    }
}
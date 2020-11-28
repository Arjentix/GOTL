package ru.arjentix.gotl;

import ru.arjentix.gotl.exception.GotlTokenizeException;
import ru.arjentix.gotl.exception.LangParseException;
import ru.arjentix.gotl.function_table.Function;
import ru.arjentix.gotl.function_table.FunctionTable;
import ru.arjentix.gotl.cacher.Cacher;
import ru.arjentix.gotl.exception.ExecuteException;
import ru.arjentix.gotl.lexer.Lexer;
import ru.arjentix.gotl.parser.Parser;
import ru.arjentix.gotl.vartable.VarTable;
import ru.arjentix.gotl.vartable.VarTable.VarData;
import ru.arjentix.gotl.type_table.*;
import ru.arjentix.gotl.types.GotlHashMap;
import ru.arjentix.gotl.types.GotlList;
import ru.arjentix.gotl.rpn_translator.RpnTranslator;
import ru.arjentix.gotl.rpn_interpreter.RpnInterpreter;
import ru.arjentix.gotl.token.Token;
import ru.arjentix.gotl.triad_optimizer.TriadOptimizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GotlUI {

  private static class Arguments {
    public boolean noCache;
    public String filename;

    public Arguments() {
      noCache = false;
      filename = "";
    }
  }

  private static List<Token> mainRpn;

  public static void main(String[] args) throws IOException, GotlTokenizeException, LangParseException, ExecuteException {
    Arguments arguments = parseArgs(args);

    String rawInput = Files.readString(Paths.get(arguments.filename));

    System.out.println("<----- Interpretation info ----->");

    Lexer lexer = new Lexer(rawInput);
    List<Token> tokens = lexer.getTokens();
    System.out.println("\nTokens: " + tokens + "\n");

    int programHash = tokens.hashCode();
    System.out.println("Program hash: " + programHash);
    Cacher cacher = new Cacher(programHash, arguments.filename);

    if (!arguments.noCache && cacher.findCache()) {
      System.out.println("Found cache in " + cacher.getCacheFilename());
      mainRpn = cacher.getRpn();
      cacher.configureVarTable();
      cacher.configureFunctionTable();
    }
    else {
      Parser parser = new Parser(tokens);
      parser.lang();

      RpnTranslator translator = new RpnTranslator(lexer.getTokens());
      mainRpn = translator.getRpn();
      System.out.println("Reverse Polish Notation: " + mainRpn + "\n");
      System.out.println("Table of variables: " + VarTable.getInstance() + "\n");
      System.out.println("Function table: " + FunctionTable.getInstance() + "\n");

      optimizeCode();

      cacher.writeCache(programHash, mainRpn);
    }
    System.out.println("\nOptimized Reverse Polish Notation: " + mainRpn + "\n");
    System.out.println("New table of variables: " + VarTable.getInstance() + "\n");
    System.out.println("New function table: " + FunctionTable.getInstance() + "\n");

    configureTypeTable();
    RpnInterpreter rpnInterpreter = new RpnInterpreter(mainRpn);

    System.out.println("<----- Program output ----->");
    rpnInterpreter.interpret();
    System.out.println();
  }

  private static Arguments parseArgs(String[] args) {
    Arguments arguments = new Arguments();

    if (args.length < 1) {
      System.err.println("Usage: GotlUI [--no-cache] <filename>");
      System.exit(-1);
    }

    arguments.filename = args[0];
    if (args[0].equals("--no-cache")) {
      arguments.noCache = true;
      arguments.filename = args[1];
    }

    return arguments;
  }

  private static void optimizeCode() {
    // Optimizing main code
    System.out.println("Optimizing main code:");
    TriadOptimizer optimizer = new TriadOptimizer(mainRpn);
    optimizer.optimize();

    Map<String, VarData> varTableDataCopy = new HashMap<>();
    varTableDataCopy.putAll(VarTable.getInstance().getData());
    // Optimizing functions code
    for (Map.Entry<String, Function> entry : FunctionTable.getInstance().entrySet()) {
      System.out.println("Optimizing " + entry.getKey() + " function:");
      VarTable.getInstance().setData(entry.getValue().getVarTableData());
      TriadOptimizer functionOptimizer = new TriadOptimizer(entry.getValue().getBody());
      functionOptimizer.optimize();
    }
    VarTable.getInstance().setData(varTableDataCopy);

    clearVarTable();
  }

  private static void configureTypeTable() {
    initList();
    initMap();
  }

  private static void initList() {
    TypeTable.getInstance().put("list", new ArrayList<>(Arrays.asList(
        new Method(".add", new ArrayList<>(Arrays.asList("Object")), "", (arg0, arg1) -> {
          GotlList list = (GotlList) arg0;
          ArrayList<Object> argsList = (ArrayList<Object>) arg1;
          list.add(argsList.get(0));

          return null;
        }),
        new Method(".insert", new ArrayList<>(Arrays.asList("int", "Object")), "", (arg0, arg1) -> {
          GotlList list = (GotlList) arg0;
          ArrayList<Object> argsList = (ArrayList<Object>) arg1;
          list.insert((int) argsList.get(0), argsList.get(1));

          return null;
        }),
        new Method(".get", new ArrayList<>(Arrays.asList("int")), "Object", (arg0, arg1) -> {
          GotlList list = (GotlList) arg0;
          ArrayList<Object> argsList = (ArrayList<Object>) arg1;
          return list.get((int) argsList.get(0));
        }),
        new Method(".remove", new ArrayList<>(Arrays.asList("int")), "", (arg0, arg1) -> {
          GotlList list = (GotlList) arg0;
          ArrayList<Object> argsList = (ArrayList<Object>) arg1;
          list.remove((int) argsList.get(0));

          return null;
        }),
        new Method(".size", new ArrayList<>(), "int", (arg0, arg1) -> {
          GotlList list = (GotlList) arg0;
          return list.size();
        }),
        new Method(".isEmpty", new ArrayList<>(), "int", (arg0, arg1) -> {
          GotlList list = (GotlList) arg0;
          return list.isEmpty() ? 1 : 0;
        }),
        new Method(".clear", new ArrayList<>(), "", (arg0, arg1) -> {
          GotlList list = (GotlList) arg0;
          list.clear();

          return null;
        })
    )));
  }

  private static void initMap() {
    TypeTable.getInstance().put("map", new ArrayList<>(Arrays.asList(
        new Method(".put", new ArrayList<>(Arrays.asList("Object", "Object")), "", (arg0, arg1) -> {
          GotlHashMap hashMap = (GotlHashMap) arg0;
          ArrayList<Object> argsList = (ArrayList<Object>) arg1;
          hashMap.put(argsList.get(0), argsList.get(1));

          return null;
        }),
        new Method(".get", new ArrayList<>(Arrays.asList("Object")), "Object", (arg0, arg1) -> {
          GotlHashMap hashMap = (GotlHashMap) arg0;
          ArrayList<Object> argsList = (ArrayList<Object>) arg1;
          return hashMap.get(argsList.get(0));
        }),
        new Method(".remove", new ArrayList<>(Arrays.asList("Object")), "", (arg0, arg1) -> {
          GotlHashMap hashMap = (GotlHashMap) arg0;
          ArrayList<Object> argsList = (ArrayList<Object>) arg1;
          hashMap.remove(argsList.get(0));

          return null;
        }),
        new Method(".size", new ArrayList<>(), "int", (arg0, arg1) -> {
          GotlHashMap hashMap = (GotlHashMap) arg0;
          return hashMap.size();
        }),
        new Method(".clear", new ArrayList<>(), "", (arg0, arg1) -> {
          GotlHashMap hashMap = (GotlHashMap) arg0;
          hashMap.clear();

          return null;
        })
    )));

  }

  private static void clearVarTable() {
    Iterator<String> it = VarTable.getInstance().keySet().iterator();
    while (it.hasNext()) {
      if (it.next().charAt(0) != '_') {
        it.remove();
      }
    }
  }
}

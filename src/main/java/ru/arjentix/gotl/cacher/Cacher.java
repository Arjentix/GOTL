package ru.arjentix.gotl.cacher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ru.arjentix.gotl.function_table.Function;
import ru.arjentix.gotl.function_table.FunctionTable;
import ru.arjentix.gotl.token.Token;
import ru.arjentix.gotl.vartable.VarTable;
import ru.arjentix.gotl.vartable.VarTable.VarData;

public class Cacher {
  private int hash;
  private Path cacheFile;
  private final String tokenPattern = "[A-Z_]+\\s+:\\s+\"((\"[^\"]*\")|[^\"]*)\"";
  private final String functionPattern = "@[A-Za-z0-9_]+";
  private final String functionVariablePattern = functionPattern;
  private final String spacePattern = "\\s+";
  private final String argsPattern = "\\bargs\\b" + spacePattern + "\\=" + spacePattern + "\\[((" + functionVariablePattern + ")?|(" + functionVariablePattern + "(," + spacePattern + functionVariablePattern + ")*))\\]";
  private final String bodyPattern = "\\bbody\\b" + spacePattern + "\\=" + spacePattern + "\\[((" + tokenPattern + ")?|(" + tokenPattern + "(," + spacePattern + tokenPattern + ")*))\\]";
  private final String varDataPattern = "[A-Za-z0-9_]+\\=\\{\\bint\\b,\\s+[0-9]+\\}";
  private final String varTableDataPattern = "\\bvarTableData\\b" + spacePattern + "\\=" + spacePattern + "\\{((" + varDataPattern + ")?|(" + varDataPattern + "(," + spacePattern + varDataPattern + ")*))\\}";

  public Cacher(int hash, String filename) {
    this.hash = hash;
    filename = filename + ".cache";
    this.cacheFile = Paths.get(System.getProperty("java.io.tmpdir"), "gotl", filename);
  }

  public boolean findCache() throws IOException {
    if (!Files.exists(cacheFile)) {
      return false;
    }

    String content = Files.readString(cacheFile);
    try (Scanner scanner = new Scanner(content)) {
      int readedHash = scanner.nextInt();
      if (hash != readedHash) {
        return false;
      }
    }

    return true;
  }

  public String getCacheFilename() {
    return cacheFile.toString();
  }

  public List<Token> getRpn() throws IOException {
    List<Token> rpn = new ArrayList<>();
    boolean endOfRpn = false;

    String allData = Files.readString(cacheFile);
    String rpnStr = allData.split("\n\n")[1];

    rpnStr = rpnStr.substring(1, rpnStr.length() - 1);

    Matcher matcher = Pattern.compile(tokenPattern).matcher(rpnStr);
    while (matcher.find()) {
      rpn.add(Token.fromString(matcher.group()));
    }

    return rpn;
  }

  public void configureVarTable() throws IOException {
    String allData = Files.readString(cacheFile);
    String varTableStr = allData.split("\n\n")[2];
    
    if (varTableStr.length() == 2) {
      return;
    }

    varTableStr = varTableStr.substring(1, varTableStr.length() - 2);

    Matcher matcher = Pattern.compile("[A-Za-z0-9_]+\\s+:\\s+\\{\\bint\\b,\\s+[0-9]+\\}").matcher(varTableStr);
    while (matcher.find()) {
      String[] parts = matcher.group().split("\\s+:\\s+");
      String varName = parts[0];

      String[] typeAndValue = parts[1].split(",\\s+");
      String value = typeAndValue[1].substring(0, typeAndValue[1].length() - 1);

      VarTable.getInstance().add(varName, "int", Integer.parseInt(value));
    }
  }

  public void configureFunctionTable() throws IOException {
    String allData = Files.readString(cacheFile);
    String functionTableStr = allData.split("\n\n")[3];
    
    functionTableStr = functionTableStr.substring(1, functionTableStr.length() - 2);

    Matcher matcher = Pattern.compile(functionPattern + spacePattern + "\\:" + spacePattern + argsPattern + "," + spacePattern + bodyPattern + "," + spacePattern + varTableDataPattern).matcher(functionTableStr);
    while (matcher.find()) {
      String functionData = matcher.group();

      String functionName = extractFunctionName(functionData);
      List<String> args = extractArgs(functionData);
      List<Token> body = extractBody(functionData);
      Map<String, VarData> varTableData = extractVarTableData(functionData);

      FunctionTable.getInstance().put(functionName, new Function(args, body, varTableData));
    }
  }

  private String extractFunctionName(String functionData) {
    Matcher functionNameMatcher = Pattern.compile(functionPattern).matcher(functionData);
    functionNameMatcher.find();
    return functionNameMatcher.group();
  }

  private List<String> extractArgs(String functionData) {
    List<String> args = new ArrayList<>();

    Matcher argsMatcher = Pattern.compile(argsPattern).matcher(functionData);
    argsMatcher.find();
    String argsStr = argsMatcher.group();

    Matcher argMatcher = Pattern.compile(functionVariablePattern).matcher(argsStr);
    while (argMatcher.find()) {
      args.add(argMatcher.group());
    }

    return args;
  }

  private List<Token> extractBody(String functionData) {
    List<Token> body = new ArrayList<>();

    Matcher bodyMatcher = Pattern.compile(bodyPattern).matcher(functionData);
    bodyMatcher.find();
    String bodyStr = bodyMatcher.group();

    Matcher tokenMatcher = Pattern.compile(tokenPattern).matcher(bodyStr);
    while (tokenMatcher.find()) {
      body.add(Token.fromString(tokenMatcher.group()));
    }

    return body;
  }

  private Map<String, VarData> extractVarTableData(String functionData) {
    Map<String, VarData> varTableData = new HashMap<>();

    Matcher varTableDataMatcher = Pattern.compile(varTableDataPattern).matcher(functionData);
    varTableDataMatcher.find();
    String varTableDataStr = varTableDataMatcher.group();

    Matcher varDataMatcher = Pattern.compile(varDataPattern).matcher(varTableDataStr);
    while (varDataMatcher.find()) {
      String varDataStr = varDataMatcher.group();
      String parts[] = varDataStr.split("=");
      String varName = parts[0];

      String[] typeAndValue = parts[1].split(",\\s+");
      String value = typeAndValue[1].substring(0, typeAndValue[1].length() - 1);

      varTableData.put(varName, new VarData("int", Integer.parseInt(value)));
    }

    return varTableData;
  }

  public void writeCache(int hash, List<Token> rpn) throws IOException {
    File file = new File(cacheFile.toString());
    File parent = file.getParentFile();
    if (!parent.exists() && !parent.mkdirs()) {
        throw new IllegalStateException("Couldn't create dir: " + parent);
    }
    final String delim = "\n\n";
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.toString()))) {
      writer.write(Integer.toString(hash) + delim + rpn + delim + VarTable.getInstance() +
                   delim + FunctionTable.getInstance());
    }
  }
}

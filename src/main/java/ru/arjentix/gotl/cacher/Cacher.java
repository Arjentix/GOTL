package ru.arjentix.gotl.cacher;

import java.util.ArrayList;
import java.util.List;
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

import ru.arjentix.gotl.token.Token;
import ru.arjentix.gotl.vartable.VarTable;

public class Cacher {
  private int hash;
  private Path cacheFile;

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

  public List<Token> getRpn() throws IOException {
    List<Token> rpn = new ArrayList<>();
    boolean endOfRpn = false;

    String allData = Files.readString(cacheFile);
    String rpnStr = allData.split("\n\n")[1];

    rpnStr = rpnStr.substring(1, rpnStr.length() - 1);

    Matcher matcher = Pattern.compile("[A-Z_]+\\s+:\\s+\"((\"[^\"]*\")|[^\"]*)\"").matcher(rpnStr);
    while (matcher.find()) {
      rpn.add(Token.fromString(matcher.group()));
    }

    return rpn;
  }

  public void configureVarTable() throws IOException {
    String allData = Files.readString(cacheFile);
    String varTableStr = allData.split("\n\n")[2];
    
    varTableStr = varTableStr.substring(1, varTableStr.length() - 2);

    Matcher matcher = Pattern.compile("[A-Za-z0-9_]+\\s+:\\s+\\bint\\b,\\s+[0-9]+").matcher(varTableStr);
    while (matcher.find()) {
      String[] parts = matcher.group().split("\\s+:\\s+");
      String varName = parts[0];

      String[] typeAndValue = parts[1].split(",\\s+");
      String value = typeAndValue[1];

      VarTable.getInstance().add(varName, "int", Integer.parseInt(value));
    }
  }

  public void writeCache(int hash, List<Token> rpn) throws IOException {
    File file = new File(cacheFile.toString());
    File parent = file.getParentFile();
    if (!parent.exists() && !parent.mkdirs()) {
        throw new IllegalStateException("Couldn't create dir: " + parent);
    }
    final String delim = "\n\n";
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.toString()))) {
      writer.write(Integer.toString(hash) + delim + rpn + delim + VarTable.getInstance());
    }
  }
}

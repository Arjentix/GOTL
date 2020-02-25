package ru.arjentix.gotl;

import ru.arjentix.gotl.exception.GotlTokenizeException;
import ru.arjentix.gotl.lexer.Lexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class GotlUI {
  public static void main(String[] args) throws IOException, GotlTokenizeException{
    System.out.println("Start GOTL UI");

    if (args.length < 1) {
      System.err.println("Usage: GotlUI <filename>");
      System.exit(-1);
    }

    String rawInput = Files.readString(Paths.get(args[0]));

    Lexer lexer = new Lexer(rawInput);
    System.out.println(lexer.getTokens());

    // Parser parser = new Parser(lexer.getTokens());
    // parser.lang();
  }
}

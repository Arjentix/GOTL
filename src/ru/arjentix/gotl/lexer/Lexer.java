package ru.arjentix.gotl.lexer;

import ru.arjentix.gotl.token.Token;
import ru.arjentix.gotl.exception.GotlTokenizeException;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Lexer {
  private final String rawInput;

  public Lexer(String rawInput) {
    this.rawInput = rawInput;
    System.out.println("Create lexer with input string:\n\"" + this.rawInput + "\"");
  }

  public List<Token> getTokens() throws GotlTokenizeException {
    List<Token> tokens = new ArrayList<Token>();
    int lineCounter = 0;

    for (String line : getLines(rawInput)) {
      try {
        ++lineCounter;

        // While line contains something (except spaces)
        while (line.matches("(\\s*\\S+\\s*)+")) {
          line = line.trim();

          // Assigning first value in enum
          LexemType relevantLexemType = LexemType.values()[0];

          // Modifying original regex pattern for our purpose
          String real_regex = "^(" + relevantLexemType.getPattern().pattern() + ")";
          Matcher matcher = Pattern.compile(real_regex).matcher(line);

          // Finding relevant lexeme type or throwing exception
          while (!matcher.find()) {
            relevantLexemType = getNextLexemType(relevantLexemType);
            real_regex = "^(" + relevantLexemType.getPattern().pattern() + ")";
            matcher.usePattern(Pattern.compile(real_regex));
          }
          // Relevant lexeme type was founded

          String value = matcher.group(0);
          tokens.add(new Token(relevantLexemType, value));
    
          // Replacing first founded value with spaces
          line = matcher.replaceFirst("");
        }
      }
      catch (IndexOutOfBoundsException ex) {
        Scanner scanner = new Scanner(line);
        String unknownSymbol = scanner.next();
        scanner.close();

        throw new GotlTokenizeException(
          "Unknown symbol at line " + lineCounter + " : " + unknownSymbol
        );
      }
    }

    return tokens;
  }

  private LexemType getNextLexemType(LexemType lexemType) throws IndexOutOfBoundsException {
    int curPos = lexemType.ordinal();
    LexemType[] lexemTypes = LexemType.values();

    if (curPos >= lexemTypes.length) {
      throw new IndexOutOfBoundsException();
    }

    return lexemTypes[curPos + 1];
  }

  private List<String> getLines(String str) {
    Scanner scanner = new Scanner(str);
    List<String> lines = new ArrayList<String>();

    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      lines.add(line);
    }

    scanner.close();
    return lines;
  }
}
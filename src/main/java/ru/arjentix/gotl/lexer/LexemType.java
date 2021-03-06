package ru.arjentix.gotl.lexer;

import java.util.regex.Pattern;

public enum LexemType {

  VALAR_MORGHULIS("Valar Morghulis", -1),
  VALAR_DOHAERIS("Valar Dohaeris", -1),
  DAENERIS("Daeneris\\:", -1),
  TYRION("Tyrion\\:", -1),
  CERCEI("Cercei\\:", -1),
  ARYA("Arya\\:", -1),
  RGLOR("Rglor:", -1),
  BRAN("Bran:", -1),
  JON("Jon\\:", 0),
  YGRITTE("Ygritte\\:", 0),
  COMMA(",", -1),
  TYPE("int|str|list|map", 2),
  LOGIC_OP("==|>=|<=|<|>", 2),
  ASSIGN_OP("=", 2),
  INPUT_OUTPUT_OP("--", 2),
  DIGIT("0|([1-9][0-9]*)", 0),
  PLUS_MINUS("\\+|\\-", 3),
  MULT_DIV("\\*|\\/", 4),
  CONST_STRING("\"[^\"]*\"", 0),
  IF_KW("if", 5),
  WHILE_KW("while", 5),
  RETURN_KW("return", 5),
  SEMICOLON("\\;", 10),
  OPEN_PARENTH("\\(", 1),
  CLOSE_PARENTH("\\)", 1),
  OPEN_BRACKET("\\{", 1),
  CLOSE_BRACKET("\\}", 1),
  FUNCTION("\\@[a-zA-Z]+", 5),
  METHOD("\\.[a-zA-Z]+", 5),
  VAR("[a-zA-Z]+", 0),
  // Types for RPN
  FALSE_TRANSITION("", 10),
  UNCONDITIONAL_TRANSITION("", 10),
  INPUT_OP("", 10),
  NEW_THREAD("", 4),
  OUTPUT_OP("", 2),
  OUTPUT_NEWLINE("", 2),
  EMPTY_LEXEME("", -1);

  private Pattern pattern;
  private int priority;

  LexemType(String regexp, int priority) {
    this.pattern = Pattern.compile(regexp);
    this.priority = priority;
  }

  public Pattern getPattern() {
    return this.pattern;
  }

  public int getPriority() {
    return this.priority;
  }
}

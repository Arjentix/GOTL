package ru.arjentix.gotl.lexer;

import java.util.regex.Pattern;

public enum LexemType {

  VALAR_MORGHULIS("Valar Morghulis", -1),
  VALAR_DOHAERIS("Valar Dohaeris", -1),
  DAENERIS("Daeneris\\:", -1),
  TYRION("Tyrion\\:", -1),
  CERCEI("Cercei\\:", -1),
  ARYA("Arya\\:", -1),
  JON("Jon\\:", -1),
  YGRITTE("Ygritte\\:", -1),
  ASSIGN_OP("=", 2),
  INPUT_OUTPUT_OP("--", 2),
  DIGIT("0|([1-9][0-9]*)", 0),
  PLUS_MINUS("\\+|\\-", 4),
  MULT_DIV("\\*|\\/", 3),
  LOGIC_OP(">|<|==|>=|<=", 2),
  CONST_STRING("\".*\"", 0),
  IF_KW("if", 5),
  WHILE_KW("while", 5),
  SEMICOLON("\\;", -1),
  OPEN_PARANTH("\\(", 1),
  CLOSE_PARANTH("\\)", 1),
  OPEN_BRACKET("\\{", 1),
  CLOSE_BRACKET("\\}", 1),
  VAR("[a-zA-z]+", 0);

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

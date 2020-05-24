package ru.arjentix.gotl.lexer;

import java.util.regex.Pattern;

public enum LexemType {

  VALAR_MORGHULIS("Valar Morghulis"),
  VALAR_DOHAERIS("Valar Dohaeris"),
  DAENERIS("Daeneris\\:"),
  TYRION("Tyrion\\:"),
  CERCEI("Cercei\\:"),
  ARYA("Arya\\:"),
  JON("Jon\\:"),
  YGRITTE("Ygritte\\:"),
  ASSIGN_OP("="),
  INPUT_OUTPUT_OP("--"),
  DIGIT("0|([1-9][0-9]*)"),
  OP("\\+|\\-|\\*|\\/"),
  LOGIC_OP(">|<|==|>=|<="),
  CONST_STRING("\".*\""),
  IF_KW("if"),
  WHILE_KW("while"),
  SEMICOLON("\\;"),
  OPEN_PARANTH("\\("),
  CLOSE_PARANTH("\\)"),
  OPEN_BRACKET("\\{"),
  CLOSE_BRACKET("\\}"),
  VAR("[a-zA-z]+");

  private Pattern pattern;

  LexemType(String regexp) {
    this.pattern = Pattern.compile(regexp);
  }

  public Pattern getPattern() {
    return this.pattern;
  }
}

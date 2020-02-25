package ru.arjentix.gotl.token;

import ru.arjentix.gotl.lexer.LexemType;

public class Token {
  private LexemType type;
  private String value;

  public Token(LexemType type, String value) {
    this.type = type;
    this.value = value;
  }

  public LexemType getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  public String toString() {
    return this.getType() + " : \"" + this.getValue() + "\"";
  }
}

package ru.arjentix.gotl.token;

import java.util.Scanner;

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

  @Override
  public int hashCode() {
    final int a = type.hashCode();
    final int b = value.hashCode();
    final int x = 31;

    return a * x * x + b;
  }

  public String toString() {
    return this.getType() + " : \"" + this.getValue() + "\"";
  }

  public static Token fromString(String str) {
    String[] typeAndValue = str.split("\\s+:\\s+");
    LexemType type = LexemType.valueOf(typeAndValue[0]);

    String value = typeAndValue[1];
    if (value.length() > 2) {
      value = value.substring(1, value.length() - 1);
    }
    else {
      value = "";
    }
    
    return new Token(type, value);
  }
}

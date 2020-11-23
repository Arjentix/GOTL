package ru.arjentix.gotl.function_table;

import java.util.List;

import ru.arjentix.gotl.token.Token;

public class Function {
  private int argsCount;
  private List<Token> body;

  public Function(int argsCount, List<Token> body) {
    this.argsCount = argsCount;
    this.body = body;
  }

  public int getArgsCount() {
    return argsCount;
  }

  public List<Token> getBody() {
    return body;
  }

  public String toString() {
    return "args count = " + argsCount + ", body = " + body;
  }

}

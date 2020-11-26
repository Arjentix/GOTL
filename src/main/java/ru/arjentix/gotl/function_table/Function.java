package ru.arjentix.gotl.function_table;

import java.util.List;

import ru.arjentix.gotl.token.Token;

public class Function {
  private List<String> args;
  private List<Token> body;

  public Function(List<String> args, List<Token> body) {
    this.args = args;
    this.body = body;
  }

  public List<String> getArgs() {
    return args;
  }

  public List<Token> getBody() {
    return body;
  }

  public String toString() {
    return "args = " + args + ", body = " + body;
  }

}

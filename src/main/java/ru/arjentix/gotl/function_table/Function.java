package ru.arjentix.gotl.function_table;

import java.util.List;

import ru.arjentix.gotl.token.Token;

public class Function {
  private int argsCount;
  private List<Token> rpn;

  public Function(int argsCount, List<Token> rpn) {
    this.argsCount = argsCount;
    this.rpn = rpn;
  }

  public int getArgsCount() {
    return argsCount;
  }

  public List<Token> getRpn() {
    return rpn;
  }

}

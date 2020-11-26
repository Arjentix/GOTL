package ru.arjentix.gotl.triad_optimizer.triad;

import java.util.ArrayList;
import java.util.List;

import ru.arjentix.gotl.triad_optimizer.triad.argument.*;
import ru.arjentix.gotl.exception.ExecuteException;
import ru.arjentix.gotl.exception.NotImplementedException;
import ru.arjentix.gotl.lexer.LexemType;
import ru.arjentix.gotl.rpn_interpreter.StackMachine;
import ru.arjentix.gotl.token.Token;
import ru.arjentix.gotl.type_table.TypeTable;

public class Triad implements Tokenizable {
  private TriadArgument first;
  private TriadArgument second;
  private Token operation;
  private int startPos;
  private int endPos;
  private boolean changed;
  private int evaluationRes;

  public Triad(TriadArgument first, TriadArgument second, Token operation,
              int startPos, int endPos) {
    this.first = first;
    this.second = second;
    this.operation = operation;
    this.startPos = startPos;
    this.endPos = endPos;
    this.changed = true;
    this.evaluationRes = 0;
  }

  public int evaluate() throws ExecuteException, NotImplementedException {
    if (changed) {
      try {
        List<Token> rpn = first.tokenize();
        rpn.addAll(second.tokenize());
        rpn.add(operation);
        StackMachine stackMachine = new StackMachine(rpn);
        stackMachine.execute();
        evaluationRes = Integer.parseInt(stackMachine.getContext().stack.peek().getValue());
        changed = false;
      }
      catch (NotImplementedException e) {
        //...
      }
    }
    return evaluationRes;
  }

  public TriadArgument getFirst() {
    return first;
  }

  public void setFirst(TriadArgument first) {
    this.first = first;
    changed = true;
  }

  public TriadArgument getSecond() {
    return second;
  }

  public void setSecond(TriadArgument second) throws NotImplementedException {
    this.second = second;
    changed = true;
  }

  public Token getOperation() throws NotImplementedException {
    return operation;
  }

  public void setOperation(Token operation) throws NotImplementedException {
    this.operation = operation;
    changed = true;
  }

  public int getStartPos() {
    return startPos;
  }

  public int getEndPos() {
    return endPos;
  }

  public void setStartPos(int startPos) {
    this.startPos = startPos;
  }

  public void setEndPos(int endPos) {
    this.endPos = endPos;
  }

  public List<Token> tokenize() throws NotImplementedException {
    List<Token> tokens = new ArrayList<>();
    try {
      int res = evaluate();
      tokens.add(new Token(LexemType.DIGIT, Integer.toString(res)));
    }
    catch (ExecuteException e) {
      tokens.addAll(first.tokenize());
      tokens.addAll(second.tokenize());
      tokens.add(operation);
    }

    return tokens;
  }

  @Override
  public int hashCode() {
    final int a = first.hashCode();
    final int b = second.hashCode();
    final int c = operation.hashCode();
    final int d = startPos;
    final int e = endPos;
    final int x = 111;

    return (int) (a*Math.pow(x, 4) + b*Math.pow(x, 3) + c*Math.pow(x, 2) + d*x + e);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Triad other = (Triad) obj;

    return (first.equals(other.first) && second.equals(other.second) &&
            operation.equals(other.operation) && startPos == other.startPos && endPos == other.endPos);
  }

  public String toString() {
    return "(" + first + ", " + second + ")" + operation.getValue();
  }
}

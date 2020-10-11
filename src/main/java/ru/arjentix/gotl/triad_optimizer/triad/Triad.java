package ru.arjentix.gotl.triad_optimizer.triad;

import java.util.Arrays;
import java.util.List;

import ru.arjentix.gotl.exception.ExecuteException;
import ru.arjentix.gotl.exception.NotImplementedException;
import ru.arjentix.gotl.stack_machine.StackMachine;
import ru.arjentix.gotl.token.Token;
import ru.arjentix.gotl.type_table.TypeTable;

public class Triad {
  private TriadArgument first;
  private TriadArgument second;
  private Token operation;
  private int pos;
  private boolean changed;
  private int evaluationRes;

  public Triad(TriadArgument first, TriadArgument second, Token operation, int pos) {
    this.first = first;
    this.second = second;
    this.operation = operation;
    this.pos = pos;
    this.changed = true;
    this.evaluationRes = 0;
  }

  public int evaluate() throws ExecuteException {
    if (changed) {
      List<Token> rpn = Arrays.asList(first.toToken(), second.toToken(), operation);
      StackMachine stackMachine = new StackMachine(rpn, new TypeTable());
      stackMachine.execute();
      evaluationRes =  Integer.parseInt(stackMachine.getStack().peek().getValue());
      changed = false;
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

  public int getPos() {
    return pos;
  }

  public void setPos(int pos) {
    this.pos = pos;
  }

  public String toString() {
    return "(" + first + ", " + second + ")" + operation.getValue();
  }
}

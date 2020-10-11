package ru.arjentix.gotl.triad_optimizer.triad;

import ru.arjentix.gotl.exception.NotImplementedException;
import ru.arjentix.gotl.token.Token;

public class DegenerateTriad extends Triad {
  public DegenerateTriad(Digit first, int pos) {
    super(first, new Digit(0), null, pos);
  }

  @Override
  public int evaluate() {
    return ((Digit)getFirst()).getValue();
  }

  @Override
  public void setSecond(TriadArgument second) throws NotImplementedException {
    throw new NotImplementedException("Can't set second argument of Degenerate Triad");
  }

  @Override
  public Token getOperation() throws NotImplementedException {
    throw new NotImplementedException("Can't get operation of Degenerate Triad");
  }

  @Override
  public void setOperation(Token operation) throws NotImplementedException {
    throw new NotImplementedException("Can't set operation of Degenerate Triad");
  }

  @Override
  public String toString() {
    return "C(" + ((Digit)getFirst()).getValue() + ", " + ((Digit)getSecond()).getValue() + ")";
  }
}

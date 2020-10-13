package ru.arjentix.gotl.triad_optimizer.triad;

import java.util.List;

import ru.arjentix.gotl.triad_optimizer.triad.argument.*;
import ru.arjentix.gotl.exception.NotImplementedException;
import ru.arjentix.gotl.token.Token;

public class DegenerateTriad extends Triad {
  public DegenerateTriad(Digit first, int startPos, int endPos) {
    super(first, new Digit(0), null, startPos, endPos);
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
  public List<Token> tokenize() throws NotImplementedException {
    throw new NotImplementedException("Can't tokenize Degenerate Triad");
  }

  @Override
  public String toString() {
    return "C(" + ((Digit)getFirst()).getValue() + ", " + ((Digit)getSecond()).getValue() + ")";
  }
}

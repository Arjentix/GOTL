package ru.arjentix.gotl.triad_optimizer.triad;

import ru.arjentix.gotl.exception.ExecuteException;
import ru.arjentix.gotl.exception.NotImplementedException;
import ru.arjentix.gotl.token.Token;
import ru.arjentix.gotl.triad_optimizer.triad.argument.Digit;
import ru.arjentix.gotl.triad_optimizer.triad.argument.TriadArgument;

public class SameTriad extends Triad {
  private Triad oldTriad;
  private int sameTriadNumber;

  public SameTriad(Triad oldTriad, int sameTriadNumber) {
    super(new Digit(sameTriadNumber), new Digit(0), null, oldTriad.getStartPos(),
          oldTriad.getEndPos());
    this.oldTriad = oldTriad;
    this.sameTriadNumber = sameTriadNumber;
  }

  public int getSameTriadNumber() {
    return sameTriadNumber;
  }
  
  @Override
  public int evaluate() throws ExecuteException {
    return oldTriad.evaluate();
  }

  @Override
  public void setSecond(TriadArgument second) throws NotImplementedException {
    throw new NotImplementedException("Can't set second argument of Same Triad");
  }

  @Override
  public Token getOperation() throws NotImplementedException {
    throw new NotImplementedException("Can't get operation of Same Triad");
  }

  @Override
  public void setOperation(Token operation) throws NotImplementedException {
    throw new NotImplementedException("Can't set operation of Same Triad");
  }

  @Override
  public int hashCode() {
    final int a = oldTriad.hashCode();
    final int b = sameTriadNumber;
    final int x = 111;

    return a * x + b;
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
    SameTriad other = (SameTriad) obj;

    return (sameTriadNumber == other.sameTriadNumber);
  }

  @Override
  public String toString() {
    return "SAME(" + sameTriadNumber + ", 0)";
  }
}

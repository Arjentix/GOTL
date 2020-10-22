package ru.arjentix.gotl.triad_optimizer.triad.argument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.arjentix.gotl.lexer.LexemType;
import ru.arjentix.gotl.token.Token;

public class Digit extends TriadArgument {
    private int value;

    public Digit(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public List<Token> tokenize() {
        return new ArrayList<>(Arrays.asList(new Token(LexemType.DIGIT, Integer.toString(value))));
    }
    
    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public int hashCode() {
      return value;
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
      Digit other = (Digit) obj;

      return value == other.value;
    }
}

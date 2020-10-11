package ru.arjentix.gotl.triad_optimizer.triad;

import java.util.List;

import ru.arjentix.gotl.exception.ExecuteException;
import ru.arjentix.gotl.lexer.LexemType;
import ru.arjentix.gotl.token.Token;

public class TriadRef implements TriadArgument {
    private List<Triad> triads;
    private int index;

    public TriadRef(List<Triad> triads, int index) {
        this.triads = triads;
        this.index = index;
    }

    @Override
    public int getValue() throws ExecuteException {
        return triads.get(index).evaluate();
    }

    @Override
    public Token toToken() throws ExecuteException {
        return new Token(LexemType.DIGIT, Integer.toString(getValue()));
    }
    
    @Override
    public String toString() {
        return "^" + Integer.toString(index);
    }
}

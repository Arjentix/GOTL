package ru.arjentix.gotl.triad_optimizer.triad.argument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.arjentix.gotl.triad_optimizer.triad.Triad;
import ru.arjentix.gotl.exception.ExecuteException;
import ru.arjentix.gotl.exception.NotImplementedException;
import ru.arjentix.gotl.lexer.LexemType;
import ru.arjentix.gotl.token.Token;

public class TriadRef extends TriadArgument {
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
    public List<Token> tokenize() throws NotImplementedException {
        try {
            Token token = new Token(LexemType.DIGIT, Integer.toString(getValue()));
            return new ArrayList<>(Arrays.asList(token));
        }
        catch (ExecuteException e) {
            return triads.get(index).tokenize();
        }
    }
    
    @Override
    public String toString() {
        return "^" + Integer.toString(index);
    }
}

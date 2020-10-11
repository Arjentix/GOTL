package ru.arjentix.gotl.triad_optimizer.triad;

import ru.arjentix.gotl.lexer.LexemType;
import ru.arjentix.gotl.token.Token;

public class Digit implements TriadArgument {
    private int value;

    public Digit(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public Token toToken() {
        return new Token(LexemType.DIGIT, Integer.toString(value));
    }
    
    @Override
    public String toString() {
        return Integer.toString(value);
    }
}

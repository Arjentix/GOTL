package ru.arjentix.gotl.triad_optimizer.triad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.arjentix.gotl.lexer.LexemType;
import ru.arjentix.gotl.token.Token;
import ru.arjentix.gotl.vartable.VarTable;

public class Variable extends TriadArgument {
    String name;

    public Variable(String name) {
        this.name = name;
    }

    @Override
    public int getValue() {
        return (int) VarTable.getInstance().getValue(name);
    }

    @Override
    public List<Token> tokenize() {
        return new ArrayList<>(Arrays.asList(new Token(LexemType.VAR, name)));
    }
    
    @Override
    public String toString() {
        return name;
    }
}

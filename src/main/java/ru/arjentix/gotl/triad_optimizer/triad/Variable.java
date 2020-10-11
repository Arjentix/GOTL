package ru.arjentix.gotl.triad_optimizer.triad;

import javax.lang.model.element.VariableElement;

import ru.arjentix.gotl.lexer.LexemType;
import ru.arjentix.gotl.token.Token;
import ru.arjentix.gotl.vartable.VarTable;

public class Variable implements TriadArgument {
    String name;

    public Variable(String name) {
        this.name = name;
    }

    @Override
    public int getValue() {
        return (int) VarTable.getInstance().getValue(name);
    }

    @Override
    public Token toToken() {
        return new Token(LexemType.VAR, name);
    }
    
    @Override
    public String toString() {
        return name;
    }
}

package ru.arjentix.gotl.triad_optimizer.triad;

import ru.arjentix.gotl.exception.ExecuteException;
import ru.arjentix.gotl.token.Token;

public interface TriadArgument {
    public int getValue() throws ExecuteException;

    public Token toToken() throws ExecuteException;

    public String toString();
}

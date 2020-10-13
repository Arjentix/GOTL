package ru.arjentix.gotl.triad_optimizer.triad;

import java.util.List;

import ru.arjentix.gotl.exception.ExecuteException;
import ru.arjentix.gotl.token.Token;

public abstract class TriadArgument implements Tokenizable {
    public abstract int getValue() throws ExecuteException;

    public abstract String toString();
}

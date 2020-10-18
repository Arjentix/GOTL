package ru.arjentix.gotl.triad_optimizer.triad.argument;

import ru.arjentix.gotl.exception.ExecuteException;
import ru.arjentix.gotl.triad_optimizer.triad.Tokenizable;

public abstract class TriadArgument implements Tokenizable {
    public abstract int getValue() throws ExecuteException;

    public abstract String toString();
}
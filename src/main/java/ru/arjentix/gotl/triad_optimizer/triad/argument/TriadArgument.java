package ru.arjentix.gotl.triad_optimizer.triad.argument;

import ru.arjentix.gotl.exception.ExecuteException;
import ru.arjentix.gotl.exception.NotImplementedException;
import ru.arjentix.gotl.triad_optimizer.triad.Tokenizable;

public abstract class TriadArgument implements Tokenizable {
    public abstract int getValue() throws ExecuteException, NotImplementedException;

    public abstract String toString();

    public abstract int hashCode();

    public abstract boolean equals(Object obj);
}

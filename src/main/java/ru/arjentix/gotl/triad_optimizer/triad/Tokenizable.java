package ru.arjentix.gotl.triad_optimizer.triad;

import java.util.List;

import ru.arjentix.gotl.exception.NotImplementedException;
import ru.arjentix.gotl.token.Token;

public interface Tokenizable {
    List<Token> tokenize() throws NotImplementedException;
}

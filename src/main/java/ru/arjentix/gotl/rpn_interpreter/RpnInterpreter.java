package ru.arjentix.gotl.rpn_interpreter;

import java.util.List;

import ru.arjentix.gotl.exception.ExecuteException;
import ru.arjentix.gotl.token.Token;

public class RpnInterpreter {
    private List<Token> rpnList;
    private StackMachine stackMachine;

    public RpnInterpreter(List<Token> rpnList) {
        this.rpnList = rpnList;
        this.stackMachine = new StackMachine(this.rpnList);
    }

    public void interpret() throws ExecuteException {
        stackMachine.execute();
    }
}

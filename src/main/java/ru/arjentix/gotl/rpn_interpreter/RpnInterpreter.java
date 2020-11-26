package ru.arjentix.gotl.rpn_interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;

import ru.arjentix.gotl.exception.ExecuteException;
import ru.arjentix.gotl.function_table.Function;
import ru.arjentix.gotl.function_table.FunctionTable;
import ru.arjentix.gotl.lexer.LexemType;
import ru.arjentix.gotl.rpn_interpreter.StackMachine.Context;
import ru.arjentix.gotl.rpn_interpreter.StackMachine.State;
import ru.arjentix.gotl.token.Token;
import ru.arjentix.gotl.vartable.VarTable;
import ru.arjentix.gotl.vartable.VarTable.VarData;

public class RpnInterpreter {
  private List<Token> rpnList;
  private StackMachine stackMachine;
  private Stack<Context> contextStack;

  public RpnInterpreter(List<Token> rpnList) {
    this.rpnList = rpnList;
    this.stackMachine = new StackMachine(this.rpnList);
    this.contextStack = new Stack<>();
  }

  public void interpret() throws ExecuteException {
    stackMachine.execute();

    State state = stackMachine.getState();
    while (state != State.NORMAL) {
      switch (stackMachine.getState()) {
        case NORMAL:
          break;
        case FUNCTION_CALL:
          switchContext();
          stackMachine.execute();
          break;
        case FUNCTION_END:
        case RETURN_CALL:
          switchContextBack();
          stackMachine.setState(State.NORMAL);
          stackMachine.execute();
          break;
      }
      state = stackMachine.getState();
    }
  }

  private void switchContext() {
    Context oldContext = stackMachine.getContext();
    String funcName = oldContext.rpnList.get(oldContext.pos).getValue();
    Function function = FunctionTable.getInstance().get(funcName);

    Map<String, VarTable.VarData> varTableData = new HashMap<>();
    ListIterator<String> it = function.getArgs().listIterator(function.getArgs().size());
    Stack<Token> stackCopy = new Stack<>();
    stackCopy.addAll(oldContext.stack);
    while (it.hasPrevious()) {
      varTableData.put(it.previous(), getVarData(stackCopy.pop()));
    }

    contextStack.push(oldContext);
    Context context = new Context();
    context.pos = 0;
    context.newLine = true;
    context.stack = new Stack<Token>();
    context.rpnList = FunctionTable.getInstance().get(funcName).getBody();
    context.varTableData = varTableData;

    stackMachine.setContext(context);
  }

  private VarData getVarData(Token token) {
    VarData varData = new VarData("int", 0);

    LexemType type = token.getType();
    switch (type) {
      case VAR:
        varData.type = VarTable.getInstance().getType(token.getValue());
        varData.value = VarTable.getInstance().getValue(token.getValue());
        break;
      case DIGIT:
        varData.type = "int";
        varData.value = Integer.parseInt(token.getValue());
        break;
      case CONST_STRING:
        varData.type = "str";
        varData.value = token.getValue();
        break;
      default:
        break;
    }

    return varData;
  }

  private void switchContextBack() {
    if (contextStack.empty()) {
      return;
    }

    Context context = contextStack.pop();
    Context curContext = stackMachine.getContext();
    // If was return
    if (curContext.pos < curContext.rpnList.size() &&
       curContext.rpnList.get(curContext.pos).getType() == LexemType.RETURN_KW) {
      String funcName = context.rpnList.get(context.pos).getValue();
      Function function = FunctionTable.getInstance().get(funcName);
      Stack<Token> curStack = curContext.stack;

      if (!curStack.empty()) {
        // Substitution a return value instead of function call
        for (int i = 0; i < function.getArgs().size() + 1; ++i) {
          context.rpnList.remove(context.pos);
          --context.pos;
        }
        ++context.pos;

        Token returnToken = curStack.pop();
        if (returnToken.getType() == LexemType.VAR) {
          context.varTableData.put(returnToken.getValue(), curContext.varTableData.get(returnToken.getValue()));
        }
        context.rpnList.add(context.pos, returnToken);
      }
    }
    else {
      ++context.pos;
    }

    stackMachine.setContext(context);
  }
}

package ru.arjentix.gotl.rpn_interpreter;

import java.util.ArrayList;
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
  private class ThreadData {
    public Stack<Context> contextStack;
    public int functionNesting;
    State state;

    public ThreadData() {
      contextStack = new Stack<>();
      functionNesting = 0;
      state = State.NORMAL;
    }

    public ThreadData(Stack<Context> contextStack, int functionNesting, State state) {
      this.contextStack = contextStack;
      this.functionNesting = functionNesting;
      this.state = state;
    }
  }

  private StackMachine stackMachine;
  private List<ThreadData> threads;

  public RpnInterpreter(List<Token> rpnList) {
    this.stackMachine = new StackMachine(rpnList);
    this.threads = new ArrayList<>();

    Context context = new Context();
    context.rpnList = rpnList;

    Stack<Context> contextStack = new Stack<>();
    contextStack.push(context);
    threads.add(new ThreadData(contextStack, 0, State.NORMAL)); // Adding context for main thread
  }

  public void interpret() throws ExecuteException {
    boolean endCondition = false;

    while (!endCondition) {
      endCondition = true;

      for (int i = 0; i < threads.size(); ++i) {
        ThreadData threadData = threads.get(i);
        State state = executeThread(threadData);

        // End if all threads finished with END state
        endCondition = endCondition && (state == State.END);
      }
    }
  }

  private State executeThread(ThreadData threadData) throws ExecuteException {
    final int countOfOperations = 3;

    if (threadData.contextStack.empty()) {
      return State.END;
    }

    stackMachine.setContext(threadData.contextStack.pop());
    stackMachine.setState(threadData.state);
    stackMachine.execute(countOfOperations);

    switch (stackMachine.getState()) {
      case NORMAL:
      case FUNCTION_EXECUTION:
        threadData.contextStack.push(stackMachine.getContext());
        threadData.state = stackMachine.getState();
        break;
      case END:
        break;
      case FUNCTION_CALL:
        ++threadData.functionNesting;
        switchContext(threadData.contextStack);
        threadData.state = State.FUNCTION_EXECUTION;
        break;
      case FUNCTION_END:
      case RETURN_CALL:
        --threadData.functionNesting;
        switchContextBack(threadData.contextStack);
        threadData.state = (threadData.functionNesting == 0 ? State.NORMAL : State.FUNCTION_EXECUTION);
        break;
      case NEW_THREAD_CALL:
        addNewThread(threadData.contextStack);
        break;
    }

    return stackMachine.getState();
  }

  // n - count of tokens to go back to find function name
  private void switchContext(Stack<Context> contextStack) {
    Context oldContext = stackMachine.getContext();
    String funcName = oldContext.rpnList.get(oldContext.pos).getValue();
    Function function = FunctionTable.getInstance().get(funcName);

    Map<String, VarTable.VarData> varTableData = new HashMap<>();
    varTableData.putAll(function.getVarTableData());
    ListIterator<String> it = function.getArgs().listIterator(function.getArgs().size());
    Stack<Token> stackCopy = new Stack<>();
    stackCopy.addAll(oldContext.stack);
    while (it.hasPrevious()) {
      String tmp = it.previous();
      varTableData.put(tmp, getVarData(stackCopy.pop()));
    }

    contextStack.push(oldContext);

    Context context = new Context();
    context.rpnList = function.getBody();
    context.varTableData = varTableData;
    contextStack.push(context);
  }

  private void addNewThread(Stack<Context> contextStack) {
    Context curContext = stackMachine.getContext();

    String funcName = curContext.rpnList.get(curContext.pos - 1).getValue();
    Function function = FunctionTable.getInstance().get(funcName);

    Context context = new Context();
    context.rpnList = function.getBody();
    context.varTableData = function.getVarTableData();

    Stack<Context> newThreadContextStack = new Stack<>();
    newThreadContextStack.push(context);

    threads.add(new ThreadData(newThreadContextStack, 0, State.NORMAL));

    ++curContext.pos;
    contextStack.push(curContext);
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
        varData.value = token.getValue().substring(1, token.getValue().length() - 1);
        break;
      default:
        break;
    }

    return varData;
  }

  private void switchContextBack(Stack<Context> contextStack) {
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

    contextStack.push(context);
  }
}

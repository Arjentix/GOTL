package ru.arjentix.gotl.rpn_interpreter;

import ru.arjentix.gotl.lexer.LexemType;
import ru.arjentix.gotl.token.Token;
import ru.arjentix.gotl.vartable.VarTable;
import ru.arjentix.gotl.type_table.Method;
import ru.arjentix.gotl.type_table.TypeTable;
import ru.arjentix.gotl.types.GotlHashMap;
import ru.arjentix.gotl.types.GotlList;
import ru.arjentix.gotl.exception.ExecuteException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

public class StackMachine {

  public enum State {
    NORMAL,
    END,
    FUNCTION_CALL,
    FUNCTION_EXECUTION,
    FUNCTION_END,
    RETURN_CALL,
    NEW_THREAD_CALL;
  }

  public static class Context {
    public List<Token> rpnList;
    public int pos;
    public Stack<Token> stack;
    public boolean newLine;
    public Map<String, VarTable.VarData> varTableData;

    public Context() {
      this.pos = 0;
      this.newLine = true;
      this.stack = new Stack<>();
      this.rpnList = new ArrayList<>();
      this.varTableData = VarTable.getInstance().getData();
    }
  }

  private Context context;
  private State state;

  public StackMachine(List<Token> rpnList) {
    this.context = new Context();
    this.context.rpnList = rpnList;
    this.state = State.NORMAL;
  }

  public Context getContext() {
    this.context.varTableData = VarTable.getInstance().getData();
    return this.context;
  }

  public void setContext(Context context) {
    this.context = context;
    VarTable.getInstance().setData(this.context.varTableData);
  }

  public State getState() {
    return this.state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public void execute() throws ExecuteException {
    execute(context.rpnList.size());
  }

  public void execute(int n) throws ExecuteException {
    for (int i = 0;
         (i < n) && (context.pos < context.rpnList.size());
         ++i, ++context.pos) {
      Token curToken = context.rpnList.get(context.pos);
      LexemType curType = curToken.getType();
      String curValue = curToken.getValue();

      if (curType == LexemType.FUNCTION) {
        // If next is new thread call
        if ((context.pos != context.rpnList.size() - 1) &&
            (context.rpnList.get(context.pos + 1).getType() == LexemType.NEW_THREAD)) {
          continue;
        }

        state = State.FUNCTION_CALL;
        return;
      }
      if (curType == LexemType.NEW_THREAD) {
        state = State.NEW_THREAD_CALL;
        return;
      }
      if (curType == LexemType.RETURN_KW) {
        state = State.RETURN_CALL;
        return;
      }

      if (curType == LexemType.VAR ||
          curType == LexemType.DIGIT ||
          curType == LexemType.CONST_STRING ||
          curType == LexemType.TYPE) {
        context.stack.push(curToken);
      }
      else {
        switch (curType) {
        case ASSIGN_OP:
          assign();
          break;
        case METHOD:
          method(curValue);
          break;
        case PLUS_MINUS:
          if (curValue.equals("+")) {
            plus();
          }
          else {
            minus();
          }
          break;
        case MULT_DIV:
          if (curValue.equals("*")) {
            mult();
          }
          else {
            div();
          }
          break;
        case LOGIC_OP:
          if (curValue.equals(">")) {
            greaterThan();
          }
          else if (curValue.equals("<")) {
            lessThan();
          }
          else if (curValue.equals("==")) {
            equal();
          }
          else if (curValue.equals(">=")) {
            greaterOrEqual();
          }
          else if (curValue.equals("<=")) {
            lessOrEqual();
          }
          break;
        case INPUT_OP:
          input();
          break;
        case OUTPUT_OP:
          output();
          break;
        case OUTPUT_NEWLINE:
          context.newLine = true;
          break;
        case FALSE_TRANSITION:
          falseTransition();
          break;
        case UNCONDITIONAL_TRANSITION:
          unconditionalTransition();
          break;
        default:
          throw new ExecuteException("Unexpected token " + curType + ": " +
                                     curValue + " during execution");
        }
      }
    }

    if ((context.pos == context.rpnList.size()) &&
        (state == State.FUNCTION_EXECUTION)) {
      state = State.FUNCTION_END;
    }
    else if ((context.pos == context.rpnList.size()) &&
             (state == State.NORMAL)) {
      state = State.END;
    }
  }

  private void checkForVarOfDigit(Token token) throws ExecuteException {
    if (token.getType() != LexemType.VAR &&
        token.getType() != LexemType.DIGIT) {
      throw new ExecuteException("Expected variable or digit, but got " +
                                 token.getType() + ": " + token.getValue());
    }
  }

  private void checkForVar(Token token) throws ExecuteException {
    if (token.getType() != LexemType.VAR) {
      throw new ExecuteException("Expected variable, but got " +
                                 token.getType() + ": " +
                                 token.getValue());
    }
  }

  private void assign() throws ExecuteException {
    Token value = context.stack.pop();
    Token variable = context.stack.pop();

    Object realValue = null;
    String type = null;

    checkForVar(variable);

    if (value.getType() == LexemType.VAR) {
      type = VarTable.getInstance().getType(value.getValue());
      realValue = VarTable.getInstance().getValue(value.getValue());
    }
    else if (value.getType() == LexemType.TYPE) {
      if (value.getValue().equals("int")) {
        realValue = (Integer) 0;
      }
      if (value.getValue().equals("str")) {
        realValue = (String) "";
      }
      if (value.getValue().equals("list")) {
        realValue = new GotlList();
      }
      if (value.getValue().equals("map")) {
        realValue = new GotlHashMap();
      }
      type = value.getValue(); 
    }
    else if (value.getType() == LexemType.DIGIT) {
      type = "int";
      realValue = Integer.parseInt(value.getValue());
    }
    else if (value.getType() == LexemType.CONST_STRING) {
      type = "str";
      realValue = value.getValue();
    }


    VarTable.getInstance().add(variable.getValue(), type, realValue);
  }

  private Method findMethod(String name, List<Method> methods) {
    Method m = null;
    for (Method methodItem : methods) {
      if (methodItem.getName().equals(name)) {
        m = methodItem;
        break;
      }
    }

    return m;
  }

  private void wrongArgType(String name, String varType, String paramType,
                            String argType) throws ExecuteException {
    throw new ExecuteException("Method " + name + " of type " + varType +
                               " accepts argument of type " + paramType +
                               ", but got " + argType);
  }

  private void method(String name) throws ExecuteException {
    Token variable = context.stack.pop();
    checkForVar(variable);
    String varType = VarTable.getInstance().getType(variable.getValue());

    Method realMethod = findMethod(name, TypeTable.getInstance().get(varType));

    List<Object> args = new ArrayList<>();
    List<String> paramTypes = realMethod.getParamTypes(); 
    Collections.reverse(paramTypes);

    for (String paramType : paramTypes) {
      Token arg = context.stack.pop();
      String argType = null;
      Object value = null;

      if (arg.getType() == LexemType.VAR) {
        argType = VarTable.getInstance().getType(arg.getValue());
        value = VarTable.getInstance().getValue(arg.getValue());
      }
      else {
        if (arg.getType() == LexemType.DIGIT) {
          argType = "int";
          value = Integer.parseInt(arg.getValue());
        }
        else if (arg.getType() == LexemType.CONST_STRING) {
          argType = "str";
          value = arg.getValue();
        }
      }

      if (!paramType.equals("Object") && !argType.equals(paramType)) {
        wrongArgType(name, varType, paramType, argType);
      }

      args.add(value);
    }
    Collections.reverse(args);

    Object res = realMethod.invoke(VarTable.getInstance().getValue(variable.getValue()), args);

    String returnType = realMethod.getReturnType();
    if (returnType.equals("Object")) {
      if (res.getClass() == Integer.class) {
        returnType = "int";
      }
      else if (res.getClass() == String.class) {
        returnType = "str";
      }
      else if (res.getClass() == GotlList.class) {
        returnType = "list";
      }
      else if (res.getClass() == GotlHashMap.class) {
        returnType = "map";
      }
    }
    if (returnType.equals("int")) {
      context.stack.push(new Token(LexemType.DIGIT, Integer.toString((Integer) res)));
    }
    if (returnType.equals("str")) {
      context.stack.push(new Token(LexemType.CONST_STRING, (String) res));
    }
    if (returnType.equals("list")) {
      context.stack.push(new Token(LexemType.DIGIT, ((GotlList) res).toString()));
    }
    if (returnType.equals("map")) {
      context.stack.push(new Token(LexemType.CONST_STRING, ((GotlHashMap) res).toString()));
    }
  }

  private int tokenToInt(Token token) throws ExecuteException {
      checkForVarOfDigit(token);

      int res;
      if (token.getType() == LexemType.VAR) {
        try {
          res = (Integer) VarTable.getInstance().getValue(token.getValue());
        }
        catch (NullPointerException e) {
          throw new ExecuteException("Variable " + token.getValue() + " wasn't defined");
        }
      }
      else {
        res = Integer.parseInt(token.getValue());
      }

      return res;
  }

  private void plus() throws ExecuteException {
    int rhsValue = tokenToInt(context.stack.pop());
    int lhsValue = tokenToInt(context.stack.pop());

    context.stack.push(new Token(LexemType.DIGIT, Integer.toString(lhsValue +
                                                           rhsValue)));
  }

  private void minus() throws ExecuteException {
    int rhsValue = tokenToInt(context.stack.pop());
    int lhsValue = tokenToInt(context.stack.pop());

    context.stack.push(new Token(LexemType.DIGIT, Integer.toString(lhsValue -
                                                           rhsValue)));
  }

  private void mult() throws ExecuteException {
    int rhsValue = tokenToInt(context.stack.pop());
    int lhsValue = tokenToInt(context.stack.pop());

    context.stack.push(new Token(LexemType.DIGIT, Integer.toString(lhsValue *
                                                           rhsValue)));
  }

  private void div() throws ExecuteException {
    int rhsValue = tokenToInt(context.stack.pop());
    int lhsValue = tokenToInt(context.stack.pop());

    context.stack.push(new Token(LexemType.DIGIT, Integer.toString(lhsValue /
                                                           rhsValue)));
  }

  private void greaterThan() throws ExecuteException {
    int rhsValue = tokenToInt(context.stack.pop());
    int lhsValue = tokenToInt(context.stack.pop());

    context.stack.push(new Token(LexemType.DIGIT, Integer.toString(lhsValue >
                                                           rhsValue ? 1 : 0)));
  }

  private void lessThan() throws ExecuteException {
    int rhsValue = tokenToInt(context.stack.pop());
    int lhsValue = tokenToInt(context.stack.pop());

    context.stack.push(new Token(LexemType.DIGIT, Integer.toString(lhsValue <
                                                           rhsValue ? 1 : 0)));
  }

  private void greaterOrEqual() throws ExecuteException {
    int rhsValue = tokenToInt(context.stack.pop());
    int lhsValue = tokenToInt(context.stack.pop());

    context.stack.push(new Token(LexemType.DIGIT, Integer.toString(lhsValue >=
                                                           rhsValue ? 1 : 0)));
  }

  private void lessOrEqual() throws ExecuteException {
    int rhsValue = tokenToInt(context.stack.pop());
    int lhsValue = tokenToInt(context.stack.pop());

    context.stack.push(new Token(LexemType.DIGIT, Integer.toString(lhsValue <=
                                                           rhsValue ? 1 : 0)));
  }

  private void equal() throws ExecuteException {
    int rhsValue = tokenToInt(context.stack.pop());
    int lhsValue = tokenToInt(context.stack.pop());

    context.stack.push(new Token(LexemType.DIGIT, Integer.toString(lhsValue ==
                                                           rhsValue ? 1 : 0)));
  }

  private void input() throws ExecuteException {
    Token token = context.stack.pop();
    checkForVar(token);
    Scanner scanner = new Scanner(System.in);

    System.out.print("\nYgritte: -- ");

    String str = scanner.nextLine();
    this.context.newLine = false;
    String type = "int";
    try {
      Integer.parseInt(str);
    }
    catch (NumberFormatException ex) {
      type = "str";
    }

    if (type.equals("int")) {
      VarTable.getInstance().add(token.getValue(), "int", Integer.parseInt(str));
    }
    else if (type.equals("str")) {
      VarTable.getInstance().add(token.getValue(), "str", str);
    }
  }

  private void output() throws ExecuteException {
    Token token = context.stack.pop();

    String str = "";
    if (token.getType() == LexemType.VAR) {
      String type = VarTable.getInstance().getType(token.getValue());
      Object value = VarTable.getInstance().getValue(token.getValue());
      if (type.equals("int")) {
        str = Integer.toString((Integer) value);
      }
      if (type.equals("str")) {
        str = (String) value;
      }
      if (type.equals("list")) {
        str = ((GotlList) value).toString();
      }
      if (type.equals("map")) {
        str = ((GotlHashMap) value).toString();
      }
    }
    else if (token.getType() == LexemType.DIGIT) {
      str = token.getValue();
    }
    else if (token.getType() == LexemType.CONST_STRING) {
      str = token.getValue();
      str = str.substring(1, str.length() - 1);
    }
    else {
      throw new ExecuteException("Expected variable, digit or const string but got " +
                                 token.getType() + ": " +
                                 token.getValue());
    }

    if (context.newLine) {
      System.out.print("\nJon: -- ");
    }
    System.out.print(str);

    context.newLine = false;
  }

  private void falseTransition() throws ExecuteException {
    Token pointer = context.stack.pop();
    Token condition = context.stack.pop();

    checkForVar(pointer);

    int conditionValue = tokenToInt(condition);
    if (conditionValue <= 0) {
      // -1 because where is ++context.pos in cycle
      context.pos = tokenToInt(pointer) - 1;
    }
  }

  private void unconditionalTransition() throws ExecuteException {
    Token pointer = context.stack.pop();
    if (pointer.getType() != LexemType.VAR) {
      throw new ExecuteException("Expected variable, but got " +
                                 pointer.getType() + ": " +
                                 pointer.getValue());
    }

    // -1 because where is ++context.pos in cycle
    context.pos = tokenToInt(pointer) - 1;
  }

}

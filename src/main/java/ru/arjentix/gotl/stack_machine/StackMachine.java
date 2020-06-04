package ru.arjentix.gotl.stack_machine;

import ru.arjentix.gotl.lexer.LexemType;
import ru.arjentix.gotl.token.Token;
import ru.arjentix.gotl.vartable.VarTable;
import ru.arjentix.gotl.type_table.Method;
import ru.arjentix.gotl.type_table.TypeTable;
import ru.arjentix.gotl.types.GotlList;
import ru.arjentix.gotl.exception.ExecuteException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class StackMachine {

  private List<Token> rpnList;
  private VarTable varTable;
  private TypeTable typeTable;
  private int pos;
  private Stack<Token> stack;
  private boolean wasOutput;

  public StackMachine(List<Token> rpnList, VarTable varTable, TypeTable typeTable) {
    this.rpnList = rpnList;
    this.varTable = varTable;
    this.typeTable = typeTable;
    pos = 0;
    stack = new Stack<>();
    wasOutput = false;
  }

  public void execute() throws ExecuteException {
    for (; pos < rpnList.size(); ++pos) {
      Token curToken = rpnList.get(pos);
      LexemType curType = curToken.getType();
      String curValue = curToken.getValue();

      // System.out.println("Token: " + curToken);
      // System.out.println("Stack: " + stack);
      // System.out.println("VarTable: " + varTable + "\n");

      if (curType == LexemType.VAR ||
          curType == LexemType.DIGIT ||
          curType == LexemType.CONST_STRING ||
          curType == LexemType.TYPE) {
        stack.push(curToken);
      }
      else {
        if (curType != LexemType.OUTPUT_OP) {
          wasOutput = false;
        }
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
    Token value = stack.pop();
    Token variable = stack.pop();

    Object realValue = null;
    String type = null;

    checkForVar(variable);

    if (value.getType() == LexemType.VAR) {
      type = varTable.getType(variable.getValue());
      realValue = varTable.getValue(variable.getValue());
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


    varTable.add(variable.getValue(), type, realValue);
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
    Token variable = stack.pop();
    checkForVar(variable);
    String varType = varTable.getType(variable.getValue());

    Method realMethod = findMethod(name, typeTable.get(varType));

    List<Object> args = new ArrayList<>();
    List<String> paramTypes = realMethod.getParamTypes(); 
    Collections.reverse(paramTypes);

    for (String paramType : paramTypes) {
      Token arg = stack.pop();
      String argType = null;
      String argValue = null;
      Object value = null;

      if (arg.getType() == LexemType.VAR) {
        argType = varTable.getType(arg.getValue());
        value = varTable.getValue(arg.getValue());
      }
      else {
        if (arg.getType() == LexemType.DIGIT) {
          argType = "int";
          argValue = arg.getValue();
        }
        else if (arg.getType() == LexemType.CONST_STRING) {
          argType = "str";
          argValue = arg.getValue();
        }

        if (paramType.equals("int")) {
          value = Integer.parseInt(argValue);
        }
        if (paramType.equals("str")) {
          value = argValue;
        }
      }

      if (!argType.equals(paramType)) {
        wrongArgType(name, varType, paramType, argType);
      }

      args.add(value);
    }

    Object res = realMethod.invoke(varTable.getValue(variable.getValue()), args);

    String returnType = realMethod.getReturnType();
    if (returnType.equals("int")) {
      stack.push(new Token(LexemType.DIGIT, Integer.toString((Integer) res)));
    }
    if (returnType.equals("str")) {
      stack.push(new Token(LexemType.CONST_STRING, (String) res));
    }
  }

  private int tokenToInt(Token token) throws ExecuteException {
    checkForVarOfDigit(token);

    int res;
    if (token.getType() == LexemType.VAR) {
      res = (Integer) varTable.getValue(token.getValue());
    }
    else {
      res = Integer.parseInt(token.getValue());
    }

    return res;
  }

  private void plus() throws ExecuteException {
    int rhsValue = tokenToInt(stack.pop());
    int lhsValue = tokenToInt(stack.pop());

    stack.push(new Token(LexemType.DIGIT, Integer.toString(lhsValue +
                                                           rhsValue)));
  }

  private void minus() throws ExecuteException {
    int rhsValue = tokenToInt(stack.pop());
    int lhsValue = tokenToInt(stack.pop());

    stack.push(new Token(LexemType.DIGIT, Integer.toString(lhsValue -
                                                           rhsValue)));
  }

  private void mult() throws ExecuteException {
    int rhsValue = tokenToInt(stack.pop());
    int lhsValue = tokenToInt(stack.pop());

    stack.push(new Token(LexemType.DIGIT, Integer.toString(lhsValue *
                                                           rhsValue)));
  }

  private void div() throws ExecuteException {
    int rhsValue = tokenToInt(stack.pop());
    int lhsValue = tokenToInt(stack.pop());

    stack.push(new Token(LexemType.DIGIT, Integer.toString(lhsValue /
                                                           rhsValue)));
  }

  private void greaterThan() throws ExecuteException {
    int rhsValue = tokenToInt(stack.pop());
    int lhsValue = tokenToInt(stack.pop());

    stack.push(new Token(LexemType.DIGIT, Integer.toString(lhsValue >
                                                           rhsValue ? 1 : 0)));
  }

  private void lessThan() throws ExecuteException {
    int rhsValue = tokenToInt(stack.pop());
    int lhsValue = tokenToInt(stack.pop());

    stack.push(new Token(LexemType.DIGIT, Integer.toString(lhsValue <
                                                           rhsValue ? 1 : 0)));
  }

  private void greaterOrEqual() throws ExecuteException {
    int rhsValue = tokenToInt(stack.pop());
    int lhsValue = tokenToInt(stack.pop());

    stack.push(new Token(LexemType.DIGIT, Integer.toString(lhsValue >=
                                                           rhsValue ? 1 : 0)));
  }

  private void lessOrEqual() throws ExecuteException {
    int rhsValue = tokenToInt(stack.pop());
    int lhsValue = tokenToInt(stack.pop());

    stack.push(new Token(LexemType.DIGIT, Integer.toString(lhsValue <=
                                                           rhsValue ? 1 : 0)));
  }

  private void equal() throws ExecuteException {
    int rhsValue = tokenToInt(stack.pop());
    int lhsValue = tokenToInt(stack.pop());

    stack.push(new Token(LexemType.DIGIT, Integer.toString(lhsValue ==
                                                           rhsValue ? 1 : 0)));
  }

  private void input() throws ExecuteException {
    Token token = stack.pop();
    checkForVar(token);
    Scanner scanner = new Scanner(System.in);

    System.out.print("Ygritte: -- ");

    String str = scanner.next();
    String type = "int";
    if (varTable.contains(token.getValue())) {
      type = varTable.getType(token.getValue());
    }
    if (type.equals("int")) {
      varTable.add(token.getValue(), "int", Integer.parseInt(str));
    }
    else if (type.equals("str")) {
      varTable.add(token.getValue(), "str", str);
    }
  }

  private void output() throws ExecuteException {
    Token token = stack.pop();

    String str = "";
    if (token.getType() == LexemType.VAR) {
      String type = varTable.getType(token.getValue());
      Object value = varTable.getValue(token.getValue());
      if (type.equals("int")) {
        str = Integer.toString((Integer) value);
      }
      if (type.equals("str")) {
        str = (String) value;
      }
      if (type.equals("list")) {
        str = ((GotlList) value).toString();
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

    if (wasOutput) {
      System.out.print(str);
    }
    else {
      System.out.print("Jon: -- " + str);
    }
    if ((pos + 2 >= rpnList.size()) ||
        ((pos + 2 < rpnList.size()) && rpnList.get(pos + 2).getType() != LexemType.OUTPUT_OP)) {
      System.out.println();
    }

    wasOutput = true;
  }

  private void falseTransition() throws ExecuteException {
    Token pointer = stack.pop();
    Token condition = stack.pop();

    checkForVar(pointer);

    int conditionValue = tokenToInt(condition);
    if (conditionValue <= 0) {
      // -1 because where is ++pos in cycle
      pos = tokenToInt(pointer) - 1;
    }
  }

  private void unconditionalTransition() throws ExecuteException {
    Token pointer = stack.pop();
    if (pointer.getType() != LexemType.VAR) {
      throw new ExecuteException("Expected variable, but got " +
                                 pointer.getType() + ": " +
                                 pointer.getValue());
    }

    // -1 because where is ++pos in cycle
    pos = tokenToInt(pointer) - 1;
  }

}

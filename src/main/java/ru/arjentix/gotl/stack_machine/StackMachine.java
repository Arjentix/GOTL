package ru.arjentix.gotl.stack_machine;

import ru.arjentix.gotl.lexer.LexemType;
import ru.arjentix.gotl.token.Token;
import ru.arjentix.gotl.vartable.VarTable;
import ru.arjentix.gotl.exception.ExecuteException;

import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class StackMachine {

  private List<Token> rpnList;
  private VarTable varTable;
  private int pos;
  private Stack<Token> stack;
  private boolean wasOutput;

  public StackMachine(List<Token> rpnList, VarTable varTable) {
    this.rpnList = rpnList;
    this.varTable = varTable;
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
          curType == LexemType.CONST_STRING) {
        stack.push(curToken);
      }
      else {
        if (curType != LexemType.OUTPUT_OP) {
          wasOutput = false;
        }
        switch (curType) {
        case TYPE:
          if (curValue.equals("int")) {
            intFoo();
          }
          if (curValue.equals("str")) {
            str();
          }
          if (curValue.equals("list")) {
            list();
          }
          break;
        case ASSIGN_OP:
          assign();
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

  private void intFoo() throws ExecuteException {
    Token variable = stack.pop();

    checkForVar(variable);

    varTable.add(variable.getValue(), "int", "0");
  }

  private void str() throws ExecuteException {
    Token variable = stack.pop();

    checkForVar(variable);

    varTable.add(variable.getValue(), "str", "");
  }

  private void list() throws ExecuteException {
    Token variable = stack.pop();

    checkForVar(variable);

    varTable.add(variable.getValue(), "list", "");
  }

  private void assign() throws ExecuteException {
    Token value = stack.pop();
    Token variable = stack.pop();

    checkForVar(variable);
    checkForVarOfDigit(value);

    varTable.add(variable.getValue(), value.getValue());
  }

  private int tokenToInt(Token token) throws ExecuteException {
    checkForVarOfDigit(token);

    int res;
    if (token.getType() == LexemType.VAR) {
      res = Integer.parseInt(varTable.getValue(token.getValue()));
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

  private void input() {
    Token token = stack.pop();
    Scanner scanner = new Scanner(System.in);

    System.out.print("Ygritte: -- ");
    varTable.add(token.getValue(), Integer.toString(scanner.nextInt()));
  }

  private void output() throws ExecuteException {
    Token token = stack.pop();

    String str = "";
    if (token.getType() == LexemType.VAR) {
      str = varTable.getValue(token.getValue());
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
      pos = Integer.parseInt(varTable.getValue(pointer.getValue())) - 1;
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
    pos = Integer.parseInt(varTable.getValue(pointer.getValue())) - 1;
  }

}

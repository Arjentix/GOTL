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

  public StackMachine(List<Token> rpnList, VarTable varTable) {
    this.rpnList = rpnList;
    this.varTable = varTable;
    pos = 0;
    stack = new Stack<>();
  }

  public void execute() throws ExecuteException {
    for (; pos < rpnList.size(); ++pos) {
      Token curToken = rpnList.get(pos);
      LexemType curType = curToken.getType();
      String curValue = curToken.getValue();

      if (curType == LexemType.VAR ||
          curType == LexemType.DIGIT ||
          curType == LexemType.CONST_STRING) {
        stack.push(curToken);
      }
      else {
        switch (curType) {
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

  private void assign() throws ExecuteException {
    Token value = stack.pop();
    Token variable = stack.pop();
    if (variable.getType() != LexemType.VAR) {
      throw new ExecuteException("Expected variable, but got " +
                                 variable.getType() + ": " +
                                 variable.getValue());
    }
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

    varTable.add(token.getValue(), Integer.toString(scanner.nextInt()));
  }

  private void output() throws ExecuteException {
    Token token = stack.pop();

    String str = "";
    if (token.getType() == LexemType.VAR) {
      str = varTable.getValue(token.getValue());
    }
    else if (token.getType() == LexemType.DIGIT ||
             token.getType() == LexemType.CONST_STRING) {
      str = token.getValue();
    }
    else {
      throw new ExecuteException("Expected variable, digit or const string but got " +
                                 token.getType() + ": " +
                                 token.getValue());
    }

    System.out.println(str);
  }

  private void falseTransition() throws ExecuteException {
    Token pointer = stack.pop();
    Token condition = stack.pop();

    if (pointer.getType() != LexemType.VAR) {
      throw new ExecuteException("Expected variable, but got " +
                                 pointer.getType() + ": " +
                                 pointer.getValue());
    }

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

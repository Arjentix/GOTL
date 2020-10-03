package ru.arjentix.gotl.rpn_translator;

import ru.arjentix.gotl.lexer.LexemType;
import ru.arjentix.gotl.token.Token;
import ru.arjentix.gotl.vartable.VarTable;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

public class RpnTranslator {
  private List<Token> tokens;
  private VarTable varTable;

  private void fromStackToList(Stack<Token> stack, List<Token> list) {
    while (!stack.empty()) {
      list.add(stack.pop());
    }
  }

  public RpnTranslator(List<Token> tokens, VarTable varTable) {
    this.tokens = tokens;
    this.varTable = varTable;
  }

  public List<Token> getRpn() {
    List<Token> rpnList = new ArrayList<>();
    Stack<Token> stack = new Stack<>();
    Stack<LexemType> exprWithTransitions = new Stack<>();
    Stack<Integer> whileKwPositions = new Stack<>();
    Stack<Token> varsWithMethodCalled = new Stack<>();
    Stack<Token> methodCalled = new Stack<>();
    int transitionNumber = 0;
    boolean wasInput = false; // true -- was "Jon" token, false -- was "Ygritte" token

    for (Token curToken : tokens) {
      LexemType curType = curToken.getType();

      // Skipping tokens with priority < 0
      if (curType.getPriority() < 0) {
        continue;
      }

      // Processing METHOD
      if (curType == LexemType.METHOD) {
        varsWithMethodCalled.push(rpnList.remove(rpnList.size() - 1));
        methodCalled.push(curToken);
        continue;
      }

      // Processing JON
      if (curType == LexemType.JON) {
          wasInput = false;
          rpnList.add(new Token(LexemType.OUTPUT_NEWLINE, "\n"));
          continue;
      }

      // Processing YGRITTE
      if (curType == LexemType.YGRITTE) {
          wasInput = true;
          continue;
      }

      // Processing INPUT_OUTPUT_OP
      if (curType == LexemType.INPUT_OUTPUT_OP) {
        fromStackToList(stack, rpnList);
        if (wasInput) {
          stack.add(new Token(LexemType.INPUT_OP, "--"));
        }
        else {
          stack.add(new Token(LexemType.OUTPUT_OP, "--"));
        }
        continue;
      }

      // Processing variables, digits and strings
      if (curType == LexemType.VAR || curType == LexemType.DIGIT || 
          curType == LexemType.CONST_STRING ||
          curType == LexemType.TYPE) {
        rpnList.add(curToken);
        continue;
      }

      // Processing open paranth
      if (curType == LexemType.OPEN_PARENTH) {
        stack.push(curToken);
        continue;
      }

      // Processing semicolon
      if (curType == LexemType.SEMICOLON) {
        fromStackToList(stack, rpnList);
        continue;
      }

      // Processing close paranth
      if (curType == LexemType.CLOSE_PARENTH) {
        if (!methodCalled.empty()) {
          rpnList.add(varsWithMethodCalled.pop());
          rpnList.add(methodCalled.pop());
        }
        Token top = stack.pop();
        while (top.getType() != LexemType.OPEN_PARENTH) {
          rpnList.add(top);
          top = stack.pop();
        }

        continue;
      }

      // Processing open bracket
      if (curType == LexemType.OPEN_BRACKET) {
        // Inserting false transition
        if (!exprWithTransitions.empty()) {
            rpnList.add(
              new Token(LexemType.VAR,
                        "_p" + Integer.toString(++transitionNumber)
              )
            );
            rpnList.add(new Token(LexemType.FALSE_TRANSITION, "!F"));
        }

        continue;
      }

      // Processing close bracket
      if (curType == LexemType.CLOSE_BRACKET) {
        // Setting transition variable
        if (!exprWithTransitions.empty()) {
          int falseTransitionPointer = rpnList.size();
          int oldTransitionNumber = transitionNumber;

          if (exprWithTransitions.lastElement() == LexemType.WHILE_KW) {
            falseTransitionPointer += 2; // To skip unconditional transition

            String transVar = "_p" +
                              Integer.toString(++transitionNumber);
            rpnList.add(new Token(LexemType.VAR, transVar));
            rpnList.add(new Token(LexemType.UNCONDITIONAL_TRANSITION, "!"));
            varTable.add(transVar, "int",
                         whileKwPositions.pop());
          }
          // Adding pointer for false transition
          varTable.add("_p" + Integer.toString(oldTransitionNumber), "int",
                       falseTransitionPointer);
          exprWithTransitions.pop();
        }

        continue;
      }

      // Processing if and while
      if (curType == LexemType.IF_KW || curType == LexemType.WHILE_KW) {
        exprWithTransitions.push(curType);
        if (curType == LexemType.WHILE_KW) {
            whileKwPositions.push(rpnList.size());
        }
        continue;
      }

      // Processing other types
      if (stack.empty() ||
          stack.peek().getType().getPriority() < curType.getPriority()) {
          stack.push(curToken);
      }
      else {
        Token top = stack.peek();
        while (!stack.empty() &&
                top.getType().getPriority() >= curType.getPriority()) {
          rpnList.add(top);
          stack.pop();
          top = stack.peek();
        }
        if (stack.empty() && top != null) { // Adding last element
          rpnList.add(top);
        }

        stack.push(curToken);
      }
    }

    fromStackToList(stack, rpnList);

    return rpnList;
  }

}

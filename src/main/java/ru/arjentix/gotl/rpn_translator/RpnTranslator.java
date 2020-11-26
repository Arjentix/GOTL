package ru.arjentix.gotl.rpn_translator;

import ru.arjentix.gotl.function_table.Function;
import ru.arjentix.gotl.function_table.FunctionTable;
import ru.arjentix.gotl.lexer.LexemType;
import ru.arjentix.gotl.token.Token;
import ru.arjentix.gotl.vartable.VarTable;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

public class RpnTranslator {
  private List<Token> tokens;

  private void fromStackToList(Stack<Token> stack, List<Token> list) {
    while (!stack.empty()) {
      list.add(stack.pop());
    }
  }

  public RpnTranslator(List<Token> tokens) {
    this.tokens = tokens;
  }

  private int extractFunction(int pos) {
    String funcName = tokens.get(pos + 1).getValue();

    List<String> args = new ArrayList<>();
    pos = extractFunctionArgs(pos, funcName, args);

    List<Token> funcBody = new ArrayList<>();
    pos = extractFunctionBody(pos, funcName, funcBody);

    FunctionTable.getInstance().put(funcName, new Function(args, funcBody));

    return pos - 1;
  }

  private int extractFunctionArgs(int pos, String funcName, List<String> args) {
    for (pos += 3; tokens.get(pos).getType() != LexemType.CLOSE_PARENTH; ++pos) {
      Token curToken = tokens.get(pos);
      if (curToken.getType() == LexemType.VAR) {
        args.add(constructVariableName(funcName, curToken.getValue()));
      }
    }

    return pos;
  }

  private int extractFunctionBody(int pos, String funcName, List<Token> funcBody) {
    int unclosedBracketsCount = 1;
    for (pos += 2; unclosedBracketsCount != 0; ++pos) {
      Token curToken = tokens.get(pos);
      LexemType curType = curToken.getType();

      if (curType == LexemType.OPEN_BRACKET) {
        ++unclosedBracketsCount;
      }
      else if (curType == LexemType.CLOSE_BRACKET) {
        --unclosedBracketsCount;
      }
      else if (curType == LexemType.VAR) {
        curToken.setValue(constructVariableName(funcName, curToken.getValue()));
      }

      funcBody.add(curToken);
    }
    funcBody.remove(funcBody.size() - 1); // Removing last bracket

    return pos;
  }

  private String constructVariableName(String funcName, String varName) {
    return funcName + "_" + varName;
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

    for (int i = 0; i < tokens.size(); ++i) {
      Token curToken = tokens.get(i);
      LexemType curType = curToken.getType();

      if (curType == LexemType.RGLOR) {
        i = extractFunction(i);
        continue;
      }

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

      // Processing open parenth
      if (curType == LexemType.OPEN_PARENTH) {
        stack.push(curToken);
        continue;
      }

      // Processing semicolon
      if (curType == LexemType.SEMICOLON) {
        fromStackToList(stack, rpnList);
        continue;
      }

      // Processing close parenth
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
            VarTable.getInstance().add(transVar, "int",
                         whileKwPositions.pop());
          }
          // Adding pointer for false transition
          VarTable.getInstance().add("_p" + Integer.toString(oldTransitionNumber), "int",
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

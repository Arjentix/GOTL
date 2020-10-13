package ru.arjentix.gotl.triad_optimizer;

import ru.arjentix.gotl.triad_optimizer.triad.*;
import ru.arjentix.gotl.triad_optimizer.triad.argument.*;
import ru.arjentix.gotl.exception.ExecuteException;
import ru.arjentix.gotl.exception.NotImplementedException;
import ru.arjentix.gotl.lexer.LexemType;
import ru.arjentix.gotl.token.Token;
import ru.arjentix.gotl.vartable.VarTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TriadOptimizer {
  private List<Token> rpn;

  private class WrapInt {
    int value;

    public WrapInt(int value) {
      this.value = value;
    }
  }

  public TriadOptimizer(List<Token> rpn) {
    this.rpn = rpn;
  }

  public List<Token> optimize() {
    List<Triad> triads = findTriads();
    System.out.println("Triads: " + triads + "\n");

    optimizeTriads(triads);
    System.out.println("Optimized triads: " + triads + "\n");

    replaceTriads(triads);

    adjustTransitions();
    rpn.removeIf(token -> (token.getType() == LexemType.EMPTY_LEXEME));

    return rpn;
  }

  private List<Triad> findTriads() {
    List<Triad> triads = new ArrayList<>();

    for (int i = 0; i < rpn.size(); ++i) {
      Token curToken = rpn.get(i);
      if (curToken.getType() == LexemType.FALSE_TRANSITION) {
        // Jumping to the false transition
        i = ((int) VarTable.getInstance().getValue(rpn.get(i - 1).getValue())) - 1;
        continue;
      }

      if (curToken.getType() == LexemType.ASSIGN_OP) {
        buildTriads(triads, new WrapInt(i));
      }
    }

    return triads;
  }

  private Triad buildTriads(List<Triad> triads, WrapInt pos) {
    int endPos = pos.value;
    Token operation = rpn.get(pos.value);
    Stack<TriadArgument> args = new Stack<>();

    for (int i = 0; i < 2; ++i) {
      Token curToken = rpn.get(--pos.value);
      if (curToken.getType() == LexemType.DIGIT ||
          curToken.getType() == LexemType.VAR) {
        args.push(buildArgument(curToken));
      }
      else {
        buildTriads(triads, pos);
        args.push(new TriadRef(triads, triads.size() - 1));
      }
    }

    TriadArgument firstArg = args.pop();
    TriadArgument secondArg = args.pop();
    Triad triad = new Triad(firstArg, secondArg, operation, pos.value, endPos);
    triads.add(triad);
    return triad;
  }

  private TriadArgument buildArgument(Token token) {
    if (token.getType() == LexemType.DIGIT) {
      return new Digit(Integer.parseInt(token.getValue()));
    }

    return new Variable(token.getValue());
  }

  private void optimizeTriads(List<Triad> triads) {
    for (int i = 0; i < triads.size(); ++i) {
      Triad triad = triads.get(i);
      try {
        if (triad.getOperation().getType() == LexemType.ASSIGN_OP) {
          optimizeAssignment(triads, i);
        }
        else {
          optimizeEvaluation(triads, i);
        }
      }
      catch (NotImplementedException ex) {
        //...
      }
    }
  }

  private void optimizeEvaluation(List<Triad> triads, int index) {
    try {
      Triad triad = triads.get(index);
      int res = triad.evaluate();
      Triad degTriad = new DegenerateTriad(new Digit(res), triad.getStartPos(),
                                           triad.getEndPos());
      triads.set(index, degTriad);
    }
    catch (ExecuteException e) {
      //...
    }
  }

  private void optimizeAssignment(List<Triad> triads, int index) {
    try {
      Triad triad = triads.get(index);
      int digit = 0;
      if (triad.getSecond() instanceof Digit) {
        digit = triad.getSecond().getValue();
        VarTable.getInstance().add(triad.getFirst().toString(), digit);
        return;
      }

      if (triad.getSecond() instanceof TriadRef) {
        digit = triad.getSecond().getValue();
      }
      else if (triad.getSecond() instanceof Variable) {
        String varName = triad.getSecond().toString();
        if (!VarTable.getInstance().contains(varName)) {
          return;
        }
        digit = (int) VarTable.getInstance().getValue(varName);
      }
      Digit digitArg = new Digit(digit);
      triad.setSecond(digitArg);
      triads.set(index, triad);
      VarTable.getInstance().add(triad.getFirst().toString(), digit);
    }
    catch (NotImplementedException | ExecuteException e) {
      //...
    }
  }

  private void replaceTriads(List<Triad> triads) {
    for (Triad triad : triads) {
      try {
        if (triad.getOperation().getType() == LexemType.ASSIGN_OP) {

          for (int i = triad.getEndPos() - 1; i > triad.getStartPos(); --i) {
            rpn.set(i, new Token(LexemType.EMPTY_LEXEME, ""));
          }

          List<Token> secondArgumentTokens = triad.getSecond().tokenize();
          for (int i = 0; i < secondArgumentTokens.size(); ++i) {
            rpn.set(triad.getStartPos() + 1 + i, secondArgumentTokens.get(i));
          }
        }
      }
      catch (NotImplementedException e) {
        //...
      }
    }
  }

  private void adjustTransitions() {
    for (int i = 0; i < rpn.size(); ++i) {
      if (rpn.get(i).getType() == LexemType.UNCONDITIONAL_TRANSITION ||
          rpn.get(i).getType() == LexemType.FALSE_TRANSITION) {
        String transitionVarName = rpn.get(i - 1).getValue();
        int transitionVarValue = (int) VarTable.getInstance()
                                               .getValue(transitionVarName);
        transitionVarValue -= getEmptyLexemCountBeforeIndex(transitionVarValue);
        VarTable.getInstance().setValue(transitionVarName, transitionVarValue);
      }
    }
  }

  private int getEmptyLexemCountBeforeIndex(int index) {
    int counter = 0;
    for (int i = 0; i < index; ++i) {
      if (rpn.get(i).getType() == LexemType.EMPTY_LEXEME) {
        ++counter;
      }
    }

    return counter;
  }
}

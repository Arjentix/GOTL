package ru.arjentix.gotl.triad_optimizer;

import ru.arjentix.gotl.triad_optimizer.triad.*;
import ru.arjentix.gotl.triad_optimizer.triad.argument.*;
import ru.arjentix.gotl.exception.ExecuteException;
import ru.arjentix.gotl.exception.NotImplementedException;
import ru.arjentix.gotl.lexer.LexemType;
import ru.arjentix.gotl.token.Token;
import ru.arjentix.gotl.vartable.VarTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    excludeUnnecessaryOperations(triads);
    System.out.println("Triads without unnecessary operations: " + triads + "\n");

    replaceTriads(triads);

    adjustTransitions();
    rpn.removeIf(token -> (token.getType() == LexemType.EMPTY_LEXEME));

    return rpn;
  }

  private List<Triad> findTriads() {
    List<Triad> triads = new ArrayList<>();
    boolean inIfOrLoop = false;
    int jumpPos = 0;

    for (int i = 0; i < rpn.size(); ++i) {
      Token curToken = rpn.get(i);
      if (curToken.getType() == LexemType.FALSE_TRANSITION) {
        if (!inIfOrLoop) {
          jumpPos = ((int) VarTable.getInstance().getValue(rpn.get(i - 1).getValue()));
        }
        inIfOrLoop = true;
      }
      if (i == jumpPos) {
        inIfOrLoop = false;
      }

      if (curToken.getType() == LexemType.ASSIGN_OP) {
        if (!inIfOrLoop) {
          buildTriads(triads, new WrapInt(i));
        }
        else {
          List<Triad> tmpTriads = new ArrayList<>();
          List<String> foundVars = new ArrayList<>();
          buildTriads(tmpTriads, new WrapInt(i));
          for (Triad tmpTriad : tmpTriads) {
            try {
              if ((tmpTriad.getOperation().getType() == LexemType.ASSIGN_OP) &&
                  (!foundVars.contains(tmpTriad.getFirst().toString()))) {
                triads.add(new DeleteIfExistTriad((Variable)tmpTriad.getFirst()));
                foundVars.add(tmpTriad.getFirst().toString());
              }
            }
            catch (NotImplementedException e) {
              // ...
            }
          }
        }
      }
    }

    return triads;
  }

  private void buildTriads(List<Triad> triads, WrapInt pos) {
    int endPos = pos.value;
    Token operation = rpn.get(pos.value);
    Stack<TriadArgument> args = new Stack<>();

    for (int i = 0; i < 2; ++i) {
      Token curToken = rpn.get(--pos.value);
      LexemType curType = curToken.getType();
      if (curType == LexemType.DIGIT ||
          curType == LexemType.VAR) {
        args.push(buildArgument(curToken));
      }
      else if (curType == LexemType.PLUS_MINUS ||
               curType == LexemType.MULT_DIV ||
               curType == LexemType.LOGIC_OP) {
        buildTriads(triads, pos);
        args.push(new TriadRef(triads, triads.size() - 1));
      }
      else {
        return;
      }
    }

    TriadArgument firstArg = args.pop();
    TriadArgument secondArg = args.pop();
    Triad triad = new Triad(firstArg, secondArg, operation, pos.value, endPos);
    triads.add(triad);
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
        if (triad instanceof DeleteIfExistTriad) {
          VarTable.getInstance().remove(triad.getFirst().toString());
        }
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
    catch (ExecuteException | NotImplementedException e) {
      //...
    }
  }

  private void optimizeAssignment(List<Triad> triads, int index) {
    Triad triad = triads.get(index);
    try {
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
          VarTable.getInstance().remove(triad.getFirst().toString());
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
      VarTable.getInstance().remove(triad.getFirst().toString());
    }
  }

  private void excludeUnnecessaryOperations(List<Triad> triads) {
    Map<TriadArgument, Integer> numbersOfDependencies = buildNumbersOfDependencies(triads);

    replaceWithSameTriads(triads, numbersOfDependencies);
    System.out.println("Triads after excluding algorithm: " + triads + "\n");

    fixRefsToTriad(triads);
    triads.removeIf(triad -> (triad instanceof SameTriad));
  }

  private Map<TriadArgument, Integer> buildNumbersOfDependencies(List<Triad> triads) {
    Map<TriadArgument, Integer> numbersOfDependencies = new HashMap<>();

    for (int i = 0; i < triads.size(); ++i) {
      Triad curTriad = triads.get(i);
      if (curTriad instanceof DegenerateTriad) {
        continue;
      }
      int firstNumber = getNumberOfDependencies(numbersOfDependencies,
                                                curTriad.getFirst());
      int secondNumber = getNumberOfDependencies(numbersOfDependencies,
                                                 curTriad.getSecond());
      numbersOfDependencies.put(new TriadRef(triads, i), Math.max(firstNumber, secondNumber) + 1);
      
      try {
        if (curTriad.getOperation().getType() == LexemType.ASSIGN_OP) {
          numbersOfDependencies.put(curTriad.getFirst(), i + 1);
        }
      }
      catch (NotImplementedException ex) {
        //...
      }
    }

    return numbersOfDependencies;
  }

  private int getNumberOfDependencies(
    Map<TriadArgument, Integer> numbersOfDependencies,
    TriadArgument arg) {
    if (arg instanceof Digit) {
      return 0;
    }

    Integer number = numbersOfDependencies.get(arg);
    if (number == null) {
      numbersOfDependencies.put(arg, 0);
      number = 0;
    }

    return number.intValue();
  }

  private void replaceWithSameTriads(List<Triad> triads, Map<TriadArgument, Integer> numbersOfDependencies) {
    for (int i = 0; i < triads.size(); ++i) {
      Triad iTriad = triads.get(i);
      if (iTriad instanceof DegenerateTriad || iTriad instanceof SameTriad) {
        continue;
      }
      for (int j = i + 1; j < triads.size(); ++j) {
        Triad jTriad = triads.get(j);
        if (jTriad instanceof DegenerateTriad || jTriad instanceof SameTriad) {
          continue;
        }
        try {

          if (iTriad.getFirst().equals(jTriad.getFirst()) &&
              iTriad.getSecond().equals(jTriad.getSecond()) &&
              iTriad.getOperation().equals(jTriad.getOperation()) &&
              (numbersOfDependencies.get(new TriadRef(triads, i)).compareTo(
               numbersOfDependencies.get(new TriadRef(triads, j))) == 0)) {
            triads.set(j, new SameTriad(jTriad, i));
          }
        }
        catch (NotImplementedException ex) {
          //...
        }
      }
    }
  }

  private void fixRefsToTriad(List<Triad> triads) {
    for (int i = 0; i < triads.size(); ++i) {
      try {
        Triad curTriad = triads.get(i);
        curTriad.setFirst(getFixedTriadArgument(triads, curTriad.getFirst()));
        curTriad.setSecond(getFixedTriadArgument(triads, curTriad.getSecond()));
        triads.set(i, curTriad);
      }
      catch (NotImplementedException ex) {
        //...
      }
    }
  }

  private TriadArgument getFixedTriadArgument(List<Triad> triads, TriadArgument arg) {
    if (arg instanceof TriadRef) {
      TriadRef triadRef = (TriadRef) arg;
      if (triads.get(triadRef.getIndex()) instanceof SameTriad) {
        SameTriad sameTriad = (SameTriad) triads.get(triadRef.getIndex());
        triadRef.setIndex(sameTriad.getSameTriadNumber());
      }

      int index = triadRef.getIndex();
      triadRef.setIndex(index - getSameTriadCountBeforeIndex(triads, index));

      return triadRef;
    }

    return arg;
  }

  private int getSameTriadCountBeforeIndex(List<Triad> triads, int index) {
    int counter = 0;
    for (int i = 0; i < index; ++i) {
      if (triads.get(i) instanceof SameTriad) {
        ++counter;
      }
    }

    return counter;
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

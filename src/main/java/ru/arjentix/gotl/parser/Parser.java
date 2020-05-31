package ru.arjentix.gotl.parser;

import ru.arjentix.gotl.token.Token;
import ru.arjentix.gotl.lexer.LexemType;
import ru.arjentix.gotl.exception.LangParseException;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.Collections;
import java.util.Comparator;
import java.lang.Object;

public class Parser {

  private class ParseResult {
    public boolean success;
    public int depth;
    public String error_mes;

    public ParseResult() {
      this.success = true;
      this.depth = 0;
      this.error_mes = "";
    }

    public ParseResult(boolean success, int depth, String error_mes) {
      this.success = success;
      this.depth = depth;
      this.error_mes = error_mes;
    }
  }

  private final List<Token> tokens;
  private int pos = -1;
  private ParseResult most_depth_error_res;

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  public void lang() throws LangParseException {
    List<Function<Object, ParseResult>> expressions = new ArrayList<>();
    expressions.add((arg0) -> {return valarMorghulis();});
    expressions.add((arg0) -> {
      return plusOperation((arg) -> {return expr();});
    });
    expressions.add((arg0) -> {return valarDohaeris();});

    ParseResult res = andOperation(expressions);
    if (!res.success) {
      throw new LangParseException(most_depth_error_res.error_mes);
    }
  }

  private ParseResult valarMorghulis() {
    return matchToken(match(), LexemType.VALAR_MORGHULIS);
  }

  private ParseResult valarDohaeris() {
    return matchToken(match(), LexemType.VALAR_DOHAERIS);
  }

  private ParseResult expr() {
    List<Function<Object, ParseResult>> expressions = new ArrayList<>();
    expressions.add((arg) -> {return assignExpr();});
    // expressions.add((arg) -> {return methodCall();});
    expressions.add((arg) -> {return condExpr();});
    expressions.add((arg) -> {return whileExpr();});
    expressions.add((arg) -> {return inputExpr();});
    expressions.add((arg) -> {return outputExpr();});

    return orOperation(expressions);
  }

  private ParseResult assignExpr() {
    List<Function<Object, ParseResult>> expressions = new ArrayList<>();
    expressions.add((arg0) -> {return daeneris();});
    expressions.add((arg0) -> {return var();});
    expressions.add((arg0) -> {return assignOp();});
    expressions.add((arg0) -> {return valueExpr();});
    expressions.add((arg0) -> {return semicolon();});

    return andOperation(expressions);
  }

  private ParseResult var() {
    return matchToken(match(), LexemType.VAR);
  }

  private ParseResult daeneris() {
    return matchToken(match(), LexemType.DAENERIS);
  }

  private ParseResult assignOp() {
    return matchToken(match(), LexemType.ASSIGN_OP);
  }

//   private ParseResult methodCall() {
//     List<Function<Object, ParseResult>> expressions = new ArrayList<>();
//     expressions.add((arg0) -> {return var();});
//     expressions.add((arg0) -> {return dot();});
//     expressions.add((arg0) -> {return method();});
//     expressions.add((arg0) -> {return openParanth();});
//     expressions.add((arg0) -> {return questionMarkOperation(argList());});
//     expressions.add((arg0) -> {return closeParanth();});

//     return andOperation(expressions);
//   }

//   private ParseResult argList() {
//     List<Function<Object, ParseResult>> expressions = new ArrayList<>();
//     expressions.add((arg0) -> {return valueExpr();});
//     expressions.add((arg0) -> {
//         List<Function<Object, ParseResult>> starExpressions = new ArrayList<>();
//         starExpressions.add((arg1) -> {return comma();});
//         starExpressions.add((arg1) -> {return valueExpr();});
//         return starOperation(starExpressions);
//     });

//     return andOperation(expressions);
//   }

  private ParseResult valueExpr() {
    List<Function<Object, ParseResult>> expressions = new ArrayList<>();
    expressions.add((arg0) -> {return value();});
    expressions.add((arg0) -> {return arithmExpr();});
    // expressions.add((arg0) -> {return methodCall();});

    return orOperation(expressions);
  }

  private ParseResult value() {
    List<Function<Object, ParseResult>> expressions = new ArrayList<>();
    expressions.add((arg0) -> {return type();});
    expressions.add((arg0) -> {return var();});
    expressions.add((arg0) -> {return digit();});
    expressions.add((arg0) -> {return constString();});

    return orOperation(expressions);
  }

  private ParseResult type() {
    return matchToken(match(), LexemType.TYPE);
  }

  private ParseResult digit() {
    return matchToken(match(), LexemType.DIGIT);
  }

  private ParseResult arithmExpr() {
    List<Function<Object, ParseResult>> expressions = new ArrayList<>();
    expressions.add((arg0) -> {return tyrion();});
    expressions.add((arg0) -> {return arithmBody();});

    return andOperation(expressions);
  }

  private ParseResult arithmBody() {
    List<Function<Object, ParseResult>> expressions = new ArrayList<>();
    expressions.add((arg0) -> {return questionMarkOperation((arg1) -> {return openParanth();});});
    expressions.add((arg0) -> {return valueExpr();});
    expressions.add((arg0) -> {
      List<Function<Object, ParseResult>> andExpressions = new ArrayList<>();
      andExpressions.add((arg) -> {return op();});
      andExpressions.add((arg) -> {return arithmBody();});
      return starOperation((arg) -> {return andOperation(andExpressions);});
    });
    expressions.add((arg0) -> {return questionMarkOperation((arg1) -> {return closeParanth();});});

    return andOperation(expressions);
  }
  

  private ParseResult op() {
    List<Function<Object, ParseResult>> expressions = new ArrayList<>();
    expressions.add((arg0) -> {return plusMinus();});
    expressions.add((arg0) -> {return multDiv();});

    return orOperation(expressions);
  }

  private ParseResult plusMinus() {
      return matchToken(match(), LexemType.PLUS_MINUS);
  }

  private ParseResult multDiv() {
      return matchToken(match(), LexemType.MULT_DIV);
  }

  private ParseResult tyrion() {
    return matchToken(match(), LexemType.TYRION);
  }

  private ParseResult condExpr() {
    List<Function<Object, ParseResult>> expressions = new ArrayList<>();
    expressions.add((arg0) -> {return cercei();});
    expressions.add((arg0) -> {return cond_head();});
    expressions.add((arg0) -> {return body();});

    return andOperation(expressions);
  }

  private ParseResult cercei() {
    return matchToken(match(), LexemType.CERCEI);
  }

  private ParseResult cond_head() {
    List<Function<Object, ParseResult>> expressions = new ArrayList<>();
    expressions.add((arg0) -> {return ifKeyword();});
    expressions.add((arg0) -> {return logicalHead();});

    return andOperation(expressions);
  }

  private ParseResult ifKeyword() {
    return matchToken(match(), LexemType.IF_KW);
  }

  private ParseResult logicalHead() {
    List<Function<Object, ParseResult>> expressions = new ArrayList<>();
    expressions.add((arg0) -> {return openParanth();});
    expressions.add((arg0) -> {return logicalExpr();});
    expressions.add((arg0) -> {return closeParanth();});

    return andOperation(expressions);
  }

  private ParseResult openParanth() {
    return matchToken(match(), LexemType.OPEN_PARANTH);
  }

  private ParseResult logicalExpr() {
    List<Function<Object, ParseResult>> expressions = new ArrayList<>();
    expressions.add((arg0) -> {return value();});
    expressions.add((arg0) -> {return logicOp();});
    expressions.add((arg0) -> {return value();});

    return andOperation(expressions);
  }

  private ParseResult logicOp() {
    return matchToken(match(), LexemType.LOGIC_OP);
  }

  private ParseResult closeParanth() {
    return matchToken(match(), LexemType.CLOSE_PARANTH);
  }

  private ParseResult body() {
    List<Function<Object, ParseResult>> expressions = new ArrayList<>();
    expressions.add((arg0) -> {return openBracket();});
    expressions.add((arg0) -> {
      return plusOperation((arg) -> {return expr();});
    });
    expressions.add((arg0) -> {return closeBracket();});

    return andOperation(expressions);
  }

  private ParseResult openBracket() {
    return matchToken(match(), LexemType.OPEN_BRACKET);
  }

  private ParseResult closeBracket() {
    return matchToken(match(), LexemType.CLOSE_BRACKET);
  }

  private ParseResult whileExpr() {
    List<Function<Object, ParseResult>> expressions = new ArrayList<>();
    expressions.add((arg0) -> {return arya();});
    expressions.add((arg0) -> {return whileHead();});
    expressions.add((arg0) -> {return body();});

    return andOperation(expressions);
  }

  private ParseResult arya() {
    return matchToken(match(), LexemType.ARYA);
  }

  private ParseResult whileHead() {
    List<Function<Object, ParseResult>> expressions = new ArrayList<>();
    expressions.add((arg0) -> {return whileKeyword();});
    expressions.add((arg0) -> {return logicalHead();});

    return andOperation(expressions);
  }

  private ParseResult whileKeyword() {
    return matchToken(match(), LexemType.WHILE_KW);
  }

  private ParseResult inputExpr() {
    List<Function<Object, ParseResult>> expressions = new ArrayList<>();
    expressions.add((arg0) -> {return ygritte();});
    expressions.add((arg0) -> {
      return plusOperation((arg) -> {
        List<Function<Object, ParseResult>> and_expressions = new ArrayList<>();
        and_expressions.add((ar) -> {return inputOutputOp();});
        and_expressions.add((ar) -> {return var();});
        return andOperation(and_expressions);
      });
    });
    expressions.add((arg0) -> {return semicolon();});

    return andOperation(expressions);
  }

  private ParseResult ygritte() {
    return matchToken(match(), LexemType.YGRITTE);
  }

  private ParseResult inputOutputOp() {
    return matchToken(match(), LexemType.INPUT_OUTPUT_OP);
  }

  private ParseResult outputExpr() {
    List<Function<Object, ParseResult>> expressions = new ArrayList<>();
    expressions.add((arg0) -> {return jonSnow();});
    expressions.add((arg0) -> {
      return plusOperation((arg) -> {
        List<Function<Object, ParseResult>> and_expressions = new ArrayList<>();
        and_expressions.add((ar) -> {return inputOutputOp();});
        and_expressions.add((ar) -> {return outputValue();});
        return andOperation(and_expressions);
      });
    });
    expressions.add((arg0) -> {return semicolon();});

    return andOperation(expressions);
  }

  private ParseResult jonSnow() {
    return matchToken(match(), LexemType.JON);
  }

  private ParseResult outputValue() {
    List<Function<Object, ParseResult>> expressions = new ArrayList<>();
    expressions.add((arg0) -> {return value();});
    expressions.add((arg0) -> {return constString();});

    return orOperation(expressions);
  }

  private ParseResult constString() {
    return matchToken(match(), LexemType.CONST_STRING);
  }

  private ParseResult semicolon() {
    return matchToken(match(), LexemType.SEMICOLON);
  }

  private ParseResult orOperation(List<Function<Object, ParseResult>> expressions) {
    List<ParseResult> results = new ArrayList<>();

    for (Function<Object, ParseResult> func : expressions) {
      ParseResult cur_res = func.apply(null);
      if (cur_res.success) {
        // System.out.println(new Throwable().fillInStackTrace().getStackTrace()[1].getMethodName() + "::OR::Matched: " + func);
        return cur_res;
      }
      else {
        // System.out.println(new Throwable().fillInStackTrace().getStackTrace()[1].getMethodName() + "::OR::Not matched: " + func);
        results.add(cur_res);
        // if func isn't the last
        if (!func.equals(expressions.get(expressions.size() - 1))) {
          back(cur_res.depth);
        }
      }
    }

    // If no one expression matches then store the most depth result
    most_depth_error_res = Collections.max(results, new Comparator<ParseResult>() {
      public int compare(ParseResult left, ParseResult right) {
        if (left.depth > right.depth) {
          return 1;
        }
        if (right.depth > left.depth) {
          return -1;
        }

        return 0;
      }
    });

    return results.get(results.size() - 1);
  }

  private ParseResult andOperation(List<Function<Object, ParseResult>> expressions) {
    List<ParseResult> results = new ArrayList<>();
    for (Function<Object, ParseResult> func : expressions) {
      ParseResult cur_res = func.apply(null);
      if (results.size() != 0) {
        cur_res.depth += results.get(results.size() - 1).depth;
      }
  
      if (!cur_res.success) {
        // System.out.println(new Throwable().fillInStackTrace().getStackTrace()[1].getMethodName() + "::AND::Not matched: " + func);
        return cur_res;
      }

      // System.out.println(new Throwable().fillInStackTrace().getStackTrace()[1].getMethodName() + "::AND::Matched: " + func);
      results.add(cur_res);
    }

    return results.get(results.size() - 1);
  }

  private ParseResult starOperation(Function<Object, ParseResult> expression) {
    ParseResult curRes = new ParseResult();
    int depthSum = 0;

    while(curRes.success) {
      curRes = expression.apply(null);
      depthSum += curRes.depth;
    }

    back(curRes.depth);
    curRes.depth = depthSum - curRes.depth;
    curRes.success = true;
    return curRes;
  }

  private ParseResult plusOperation(Function<Object, ParseResult> expression) {
    ParseResult cur_res = new ParseResult();
    int depth_sum = 0;
    int counter = -1;

    while(cur_res.success) {
      ++counter;
      cur_res = expression.apply(null);
      depth_sum += cur_res.depth;
    }

    if (counter < 1) {
      back(depth_sum);
      return cur_res;
    }

    back(cur_res.depth);
    cur_res.depth = depth_sum - cur_res.depth;
    cur_res.success = true;
    return cur_res;
  }

  private ParseResult questionMarkOperation(Function<Object, ParseResult> expression) {
    ParseResult curRes = expression.apply(null);
    if (!curRes.success) {
        back(curRes.depth);
    }

    curRes.success = true;
    return curRes;
  }

  private Token match() {
    return tokens.get(++pos);
  }

  private void back(int step) {
    pos -= step;
  }

  private ParseResult matchToken(Token token, LexemType type) {
    if (!token.getType().equals(type)) {
      return new ParseResult(false, 1, type + " expected, but " +
                                       token.getType().name() + ": " +
                                       token.getValue() + " found");
    }

    return new ParseResult(true, 1, "");
  }
};

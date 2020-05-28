package ru.arjentix.gotl;

import ru.arjentix.gotl.exception.GotlTokenizeException;
import ru.arjentix.gotl.exception.LangParseException;
import ru.arjentix.gotl.exception.ExecuteException;
import ru.arjentix.gotl.lexer.Lexer;
import ru.arjentix.gotl.parser.Parser;
import ru.arjentix.gotl.vartable.VarTable;
import ru.arjentix.gotl.rpn_translator.RpnTranslator;
import ru.arjentix.gotl.stack_machine.StackMachine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class GotlUI {
  public static void main(String[] args) throws IOException, GotlTokenizeException, LangParseException, ExecuteException {
    System.out.println("Start GOTL UI");

    if (args.length < 1) {
      System.err.println("Usage: GotlUI <filename>");
      System.exit(-1);
    }

    String rawInput = Files.readString(Paths.get(args[0]));

    System.out.println("<----- Iterpretation info ----->");

    Lexer lexer = new Lexer(rawInput);
    System.out.println("\nTokens: " + lexer.getTokens() + "\n");

    Parser parser = new Parser(lexer.getTokens());
    parser.lang();

    VarTable varTable = new VarTable();

    RpnTranslator translator = new RpnTranslator(lexer.getTokens(), varTable);
    System.out.println("Reverse Polish Notation: " + translator.getRpn() + "\n");
    System.out.println("Table of variables: " + varTable + "\n");

    StackMachine stackMachine = new StackMachine(translator.getRpn(), varTable);

    System.out.println("<----- Program output ----->");
    stackMachine.execute();
  }
}

package ru.arjentix.gotl;

import ru.arjentix.gotl.exception.GotlTokenizeException;
import ru.arjentix.gotl.exception.LangParseException;
import ru.arjentix.gotl.exception.ExecuteException;
import ru.arjentix.gotl.lexer.Lexer;
import ru.arjentix.gotl.parser.Parser;
import ru.arjentix.gotl.vartable.VarTable;
import ru.arjentix.gotl.type_table.*;
import ru.arjentix.gotl.rpn_translator.RpnTranslator;
import ru.arjentix.gotl.stack_machine.StackMachine;
import ru.arjentix.gotl.list.GotlList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;


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

    TypeTable typeTable = new TypeTable();

    // List type initialization
    typeTable.put("list", new ArrayList<Method>(Arrays.asList(new Method[] {
        new Method(".add", new ArrayList<String>(){{add("int");}}, "", (arg0, arg1) -> {
            GotlList list = (GotlList) arg0;
            ArrayList<Integer> argsList = (ArrayList<Integer>) arg1;
            list.add(argsList.get(0));

            return null;
        }),
        new Method(".insert", new ArrayList<String>(){{add("int"); add("int");}}, "", (arg0, arg1) -> {
            GotlList list = (GotlList) arg0;
            ArrayList<Integer> argsList = (ArrayList<Integer>) arg1;
            int realArg0 = argsList.get(0);
            int realArg1 = argsList.get(1);
            list.insert(realArg0, realArg1);

            return null;
        }),
        new Method(".get", new ArrayList<String>(){{add("int");}}, "int", (arg0, arg1) -> {
            GotlList list = (GotlList) arg0;
            ArrayList<Integer> argsList = (ArrayList<Integer>) arg1;
            return list.get(argsList.get(0));
        }),
        new Method(".remove", new ArrayList<String>(){{add("int");}}, "", (arg0, arg1) -> {
            GotlList list = (GotlList) arg0;
            ArrayList<Integer> argsList = (ArrayList<Integer>) arg1;
            list.remove(argsList.get(0));

            return null;
        }),
        new Method(".size", new ArrayList<String>(), "int", (arg0, arg1) -> {
            GotlList list = (GotlList) arg0;
            return list.size();
        }),
        new Method(".isEmpty", new ArrayList<String>(), "int", (arg0, arg1) -> {
            GotlList list = (GotlList) arg0;
            return list.isEmpty() ? 1 : 0;
        })
    })));

    RpnTranslator translator = new RpnTranslator(lexer.getTokens(), varTable);
    System.out.println("Reverse Polish Notation: " + translator.getRpn() + "\n");
    System.out.println("Table of variables: " + varTable + "\n");

    StackMachine stackMachine = new StackMachine(translator.getRpn(), varTable, typeTable);

    System.out.println("<----- Program output ----->");
    stackMachine.execute();
  }
}

package utils;

import java.util.ArrayList;

public class Rule {
    Symbol leftPart;
    ArrayList<Symbol> rightPart;

    public Rule(Symbol left, ArrayList<Symbol> right) {
        this.leftPart = left;
        if (right.size() < 1) {
            System.out.println("Trying to make a rule with no right part!");
            System.exit(3);
        } else {
            this.rightPart = right;
        }
    }

    public static boolean isToEmpty(Rule r) {
        return ((r.rightPart.size() == 1) && (r.rightPart.get(0).type.equals("empty")));
    }

    public static boolean isToTerm(Rule r) {
        return ((r.rightPart.size() == 1) && (r.rightPart.get(0).type.equals("term")));
    }

    public static boolean isToTermNterm(Rule r) {
        return ((r.rightPart.size() == 2) && (r.rightPart.get(0).type.equals("term")) && (r.rightPart.get(1).type.equals("nonterm")));
    }
}

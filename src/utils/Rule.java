package utils;

import java.util.ArrayList;
import java.util.stream.Collectors;

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

    @Override
    public String toString() {
        return "Rule: {leftPart=" + leftPart.name + ", rightPart=[" + rightPart.stream().map((s) -> s.name).collect(Collectors.joining(", ")) +
                "]}";
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

    public static boolean isToNtermNterm(Rule r) {
        return ((r.rightPart.size() == 2) && (r.rightPart.get(0).type.equals("nonterm")) && (r.rightPart.get(1).type.equals("nonterm")));
    }

    public static boolean hasTermAndNotToTerm(Rule r) {
        return ((r.rightPart.size() > 1) && r.rightPart.stream().anyMatch(rp -> rp.type.equals("term")));
    }

    public static boolean isToNterms(Rule r) {
        return ((r.rightPart.size() > 1) && r.rightPart.stream().allMatch(s -> s.type.equals("nonterm")));
    }

    public static ArrayList<String> getTerminalNames(Rule r) {
        ArrayList<String> names = new ArrayList<>();
        for (Symbol s : r.rightPart) {
            if ((s.type.equals("term")) && (!names.contains(s.name))) names.add(s.name);
        }
        return names;
    }
}

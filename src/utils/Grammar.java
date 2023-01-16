package utils;

import java.util.ArrayList;

public class Grammar {
    ArrayList<Symbol> nonterminals;
    ArrayList<Symbol> terminals;
    ArrayList<Rule> rules;
    Symbol startingSymbol;

    public Grammar(ArrayList<Symbol> nts, ArrayList<Symbol> ts, ArrayList<Rule> rs, Symbol stS) {
        nonterminals = nts;
        terminals = ts;
        rules = rs;
        startingSymbol = stS;
    }

    public Grammar() {
        nonterminals = new ArrayList<>();
        terminals = new ArrayList<>();
        rules = new ArrayList<>();
        startingSymbol = null;
    }

/*    public boolean isDuplicate(Rule r1, Rule r2) {
        boolean isDup = true;
        if ((rules.indexOf(r1) != rules.indexOf(r2)) && (r1.rightPart.size() == r2.rightPart.size()) && (r1.leftPart.name.equals(r2.leftPart.name))) {
            for (int i = 0; (i < r1.rightPart.size()); i++) {
                if (!(r1.rightPart.get(i).name.equals(r2.rightPart.get(i).name))) {
                    isDup = false;
                    break;
                }
            }
        }
        return isDup;
    }

    public void removeDuplicates() {
        for (Rule r1 : new ArrayList<>(rules)) {
            for (Rule r2 : new ArrayList<>(rules)) {
                if (isDuplicate(r1, r2)) rules.remove(r2);
            }
        }
    }*/

    public static String getString(Grammar g) {
        StringBuilder s = new StringBuilder("Grammar{nonterminals=");
        for (Symbol nt : g.nonterminals) s.append(nt.name).append(" ");
        s.append(", terminals=");
        for (Symbol t: g.terminals) s.append(t.name).append(" ");
        s.append("\nrules=");
        for (Rule rl : g.rules) {
            s.append("(");
            s.append(rl.leftPart.name);
            s.append(" -> ");
            for (Symbol rp : rl.rightPart) {
                s.append(rp.name).append(" ");
            }
            s.append(')');
        }
        s.append("\nstartingSymbol=");
        s.append(g.startingSymbol.name).append('}');
        return s.toString();
    }
}

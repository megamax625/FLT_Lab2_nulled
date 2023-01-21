package utils;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Intersection extends Grammar {
    ArrayList<Three> nonterminals;
    ArrayList<IntersectedRule> terminalRules;
    ArrayList<IntersectedRule> rules;
    ArrayList<Symbol> terminals;
    Three startingSymbol;
    public Intersection(CNF cnf, NFA auto, boolean debug) {
        super();
        this.nonterminals = new ArrayList<>();
        this.terminalRules = new ArrayList<>();
        this.rules = new ArrayList<>();
        this.terminals = new ArrayList<>();
        this.buildIntersection(cnf, auto, debug);
    }

    public void buildIntersection(CNF cnf, NFA auto, boolean debug) {
        ArrayList<NFA.State> states = new ArrayList<>(auto.states);
        ArrayList<NFA.State> finalStates = new ArrayList<>(auto.finalStates);
        if (finalStates.size() != 1) {
            DebugPrint("Automaton has <> 1 final state!", debug);
            System.exit(10);
        }
        NFA.State finalState = finalStates.get(0);
        ArrayList<Symbol> nonterminalsCNF = new ArrayList<>(cnf.nonterminals);
        ArrayList<Rule> CNFRules = new ArrayList<>(cnf.rules);
        ArrayList<NFA.Transition> autoTransitions = new ArrayList<>(auto.transitions);
        this.nonterminals = new ArrayList<>();
        this.startingSymbol = new Three(new Symbol(auto.startState.symbol.name), new Symbol(cnf.startingSymbol), new Symbol(finalState.symbol));
        nonterminals.add(startingSymbol);
        DebugPrint("New starting nonterminal:" + startingSymbol, debug);
        this.buildTerminalRules(CNFRules, autoTransitions, debug);
        DebugPrint(this.toString(), debug);
        this.buildNonTerminalRules(CNFRules, autoTransitions, states, debug);
        DebugPrint(this.toString(), debug);
        DebugPrint("Number of rules: " + rules.size(), debug);
        int oldRuleSize = rules.size();
        do {
            oldRuleSize = rules.size();
            this.RemoveUnreachableNonterminals(debug);
            DebugPrint(this.toString(), debug);
            this.removeUnproducingNonterminals(debug);
            DebugPrint(this.toString(), debug);
            int newRuleSize = rules.size();
            // второй раз сразу потому что могут удалиться правила
            if (oldRuleSize != newRuleSize) {
                this.RemoveUnreachableNonterminals(debug);
                DebugPrint(this.toString(), debug);
                int newNewRuleSize = nonterminals.size();
                if (newNewRuleSize != newRuleSize) {
                    this.removeUnproducingNonterminals(debug);
                    DebugPrint(this.toString(), debug);
                }
            }
        } while (!(rules.size() == oldRuleSize));
        System.out.println("Number of rules: " + rules.size());
        System.out.println(this.toString());
    }

    public void buildTerminalRules(ArrayList<Rule> CNFRules, ArrayList<NFA.Transition> autoTransitions, boolean debug) {
        DebugPrint("Adding terminal rules", debug);
        for (Rule r : CNFRules) {
            if (Rule.isToTerm(r)) {
                Symbol term = new Symbol(r.rightPart.get(0));
                if (terminals.stream().noneMatch(t -> (t.name.equals(term.name)))) terminals.add(term);
                Symbol nonterm = new Symbol(r.leftPart.name);
                for (NFA.Transition tr : autoTransitions) {
                    if (tr.alphabetic.name.equals(term.name)) {
                        Three newNonterm = new Three(new Symbol(tr.left.symbol.name), nonterm, new Symbol(tr.right.symbol.name));
                        ArrayList<Symbol> rightPart = new ArrayList<>();
                        rightPart.add(term);
                        ArrayList<Three> RP = new ArrayList<>();
                        for (Symbol s : rightPart) {
                            RP.add(new Three(new Symbol(), new Symbol(s), new Symbol()));
                        }
                        IntersectedRule newRule = new IntersectedRule(newNonterm, RP, debug);
                        DebugPrint("Made new rule " + newRule, debug);
                        this.rules.add(newRule);
                        this.terminalRules.add(newRule);
                        if (this.nonterminals.stream().noneMatch(nt -> ((nt.start.name.equals(newNonterm.start.name)) &&
                                        (nt.end.name.equals(newNonterm.end.name) &&
                                        (nt.nonterm.name.equals(newNonterm.nonterm.name)))))) this.nonterminals.add(newNonterm);
                    }
                }
            }
        }
    }

    public void buildNonTerminalRules(ArrayList<Rule> CNFRules, ArrayList<NFA.Transition> autoTransitions, ArrayList<NFA.State> states, boolean debug) {
        DebugPrint("Adding nonterminal rules", debug);
        DebugPrint("Transitions: " + autoTransitions.stream().map((s) -> s.toString()).collect(Collectors.joining(", ")), debug);
        for (Rule r : CNFRules) {
            if (Rule.isToNtermNterm(r)) {
                Symbol right1 = r.rightPart.get(0);
                Symbol right2 = r.rightPart.get(1);
                DebugPrint("Starting to build nonterminal rules for " + r, debug);
                for (NFA.State p : states) {
                    for (NFA.State q : states) {
                        if (autoTransitions.stream().anyMatch(t -> (t.left.symbol.name.equals(p.symbol.name) && t.right.symbol.name.equals(q.symbol.name)))) {
                            Three left = new Three(new Symbol(p.symbol), new Symbol(r.leftPart), new Symbol(q.symbol));
                            if (CheckForProducing(left, CNFRules, debug)) {
                                for (NFA.State qi : states) {
                                    if (autoTransitions.stream().anyMatch(tr ->
                                            (tr.left.symbol.name.equals(p.symbol.name) && tr.right.symbol.name.equals(qi.symbol.name)))
                                    && autoTransitions.stream().anyMatch(tran ->
                                            (tran.left.symbol.name.equals(qi.symbol.name) && tran.right.symbol.name.equals(q.symbol.name)))) {
                                        Three RPleft = new Three(new Symbol(p.symbol), new Symbol(right1), new Symbol(qi.symbol));
                                        if (CheckForProducing(RPleft, CNFRules, debug)) {
                                            if (this.nonterminals.stream().noneMatch(nt -> ((nt.start.name.equals(RPleft.start.name)) &&
                                                    (nt.end.name.equals(RPleft.end.name) &&
                                                            (nt.nonterm.name.equals(RPleft.nonterm.name)))))) this.nonterminals.add(RPleft);
                                            Three RPright = new Three(new Symbol(qi.symbol), new Symbol(right2), new Symbol(q.symbol));
                                            if (CheckForProducing(RPright, CNFRules, debug)) {
                                                if (this.nonterminals.stream().noneMatch(nt -> ((nt.start.name.equals(RPright.start.name)) &&
                                                        (nt.end.name.equals(RPright.end.name) &&
                                                                (nt.nonterm.name.equals(RPright.nonterm.name)))))) this.nonterminals.add(RPright);
                                                if (RPleft.name.equals(left.name) && terminalRules.stream().noneMatch(tr -> (tr.leftPart.name.equals(left.name)))) {
                                                    DebugPrint("Got self-recursive nonterminal " + RPleft.name, debug);
                                                    continue;
                                                }
                                                if (RPright.name.equals(left.name) && terminalRules.stream().noneMatch(tr -> (tr.leftPart.name.equals(left.name)))) {
                                                    DebugPrint("Got self-recursive nonterminal " + RPright.name, debug);
                                                    continue;
                                                }
                                                ArrayList<Three> RP = new ArrayList<>();
                                                RP.add(RPleft);
                                                RP.add(RPright);
                                                IntersectedRule newRule = new IntersectedRule(left, RP, debug);
                                                DebugPrint("Got new Rule: " + newRule, debug);
                                                rules.add(newRule);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean CheckForProducing(Three left, ArrayList<Rule> CNFRules, boolean debug) {
        boolean isNotOnlyToTerm = false;
        boolean isNotProducing = true;
        for (Rule r2 : CNFRules) {
            if ((r2.leftPart.name.equals(left.nonterm.name)) && (!(Rule.isToTerm(r2) || Rule.isToEmpty(r2)))) {
                isNotOnlyToTerm = true;
                isNotProducing = false;
                //DebugPrint("For " + left + " nonterm " + r.leftPart.name + " rewrites not only to term: " + r2.toString(), debug);
                break;
            }
        }
        if (!isNotOnlyToTerm) {
            for (IntersectedRule termR : terminalRules) {
                //DebugPrint(termR.leftPart.name + " !=? " + left.name, debug);
                if (termR.leftPart.name.equals(left.name)) {
                    DebugPrint("Nonterm " + left.nonterm.name + " rewrites only to terms, but has a terminal rule for " + left, debug);
                    isNotProducing = false;
                    break;
                }
            }
        }
        if (!isNotProducing) {
            return true;
        } else {
            DebugPrint("Rule with nonterminal " + left + " omitted because it isn't producing", debug);
            return false;
        }
    }

    public void RemoveUnreachableNonterminals(boolean debug) {
        for (Three nt : nonterminals) {
            nt.reachable = false;
        }
        int countReachable = 1;
        int countToCompare = 1;
        startingSymbol.reachable = true;
        countReachable += markReachables(startingSymbol);
        DebugPrint("CountR: " + countReachable + ", CountTC: " + countToCompare + "\n", debug);
        while (countReachable != countToCompare) {
            countToCompare = countReachable;
            countReachable += markReachables(startingSymbol);
        }
        DebugPrint("Current nonterminals: " + nonterminals.stream().map((s) -> s.name + " reach:" + s.reachable).collect(Collectors.joining(", ")), debug);
        for (IntersectedRule r : new ArrayList<>(rules)) {
            if (terminalRules.contains(r)) {
                for (Three nt : nonterminals) {
                    if (nt.name.equals(r.leftPart.name) && !(nt.reachable)) {
                        rules.remove(r);
                        terminalRules.remove(r);
                        break;
                    }
                }
            } else {
                for (Three rpt : r.rightPart) {
                    if (nonterminals.stream().noneMatch((nt) -> (nt.name.equals(rpt.name))))
                        rules.remove(r);
                }
                for (Three nt : nonterminals) {
                    if (nt.name.equals(r.leftPart.name) && !(nt.reachable)) rules.remove(r);
                }
            }
        }
        nonterminals.removeIf(nt -> !nt.reachable);
    }

    public void removeUnproducingNonterminals(boolean debug) {
        for (Three nt : nonterminals) {
            nt.producing = false;
        }
        int countProducing = 0;
        int countToCompare = 0;
        for (IntersectedRule r : rules) {
            DebugPrint("Checking rule " + r, debug);
            if ((r.rightPart.size() == 1)) {
                DebugPrint("Marking producing of " + r, debug);
                for (Three nt : nonterminals) {
                    if (nt.name.equals(r.leftPart.name)) {
                        nt.producing = true;
                        countProducing++;
                        break;
                    }
                }
            }
        }
        countProducing += markProducing();
        DebugPrint("CountP: " + countProducing + ", CountTC: " + countToCompare + "\n", debug);
        do {
            countToCompare = countProducing;
            countProducing += markProducing();
        } while (countProducing != countToCompare);
        DebugPrint("Current nonterminals: " + nonterminals.stream().map((s) -> s.name + " prod:" + s.producing).collect(Collectors.joining(", ")), debug);
        for (IntersectedRule r : new ArrayList<>(rules)) {
            for (Three nt : nonterminals) {
                if (nt.name.equals(r.leftPart.name) && !nt.producing) rules.remove(r);
            }
        }
        nonterminals.removeIf(nt -> !nt.producing);
    }

    public int markReachables(Three s) {
        int added = 0;
        for (IntersectedRule ir : rules) {
            if (ir.leftPart.name.equals(s.name)) {
                for (Three rpt : ir.rightPart) {
                    for (Three nt : nonterminals) {
                        if ((nt.name.equals(rpt.name)) && !(nt.reachable)) {
                                added++;
                                nt.reachable = true;
                                added += markReachables(nt);
                                break;
                            }
                        }
                    }
                }
            }
        return added;
    }

    public int markProducing() {
        int added = 0;
        for (IntersectedRule ir : rules) {
            for (Three nt : nonterminals) {
                if (nt.name.equals(ir.leftPart.name)) {
                    if (!(nt.producing)) {
                        boolean prod = true;
                        for (Three rpt : ir.rightPart) {
                            for (Three nt2 : nonterminals) {
                                if ((rpt.name.equals(nt2.name)) && !(nt2.producing)) {
                                    prod = false;
                                    break;
                                }
                            }
                        }
                        if (prod) {
                            added++;
                            nt.producing = true;
                        }
                    }
                }
            }
        }
        return added;
    }

    @Override
    public String toString() {
        return "Intersection{" +
                "startingSymbol=" + startingSymbol +
                "\nnonterminals=" + nonterminals +
                "\nrules=" + rules +
                "\nterminals=" + terminals +
                '}';
    }

    public static class Three {
        public Symbol start;
        public Symbol nonterm;
        public Symbol end;
        public String name;
        public boolean reachable;
        public boolean producing;
        public Three(Symbol start, Symbol nonterm, Symbol end) {
            this.start = start;
            this.nonterm = nonterm;
            this.end = end;
            if ((start.name == null) || (end.name == null)) this.name = nonterm.name;
            else this.name = "<" + start.name + ", " + nonterm.name + ", " + end.name + ">";
        }

        public boolean equals(Three t) {
            return (this.start.name.equals(t.start.name)) && (this.nonterm.name.equals(t.nonterm.name)) && (this.end.name.equals(t.end.name));
        }

        @Override
        public String toString() {
            if ((start == null) || (end == null)) return nonterm.name;
            else return "<" +
                    start.name +
                    "," + nonterm.name +
                    "," + end.name +
                    '>';
        }
    }

    public static class IntersectedRule extends Rule {
        Three leftPart;
        ArrayList<Three> rightPart;
        public IntersectedRule(Three left, ArrayList<Three> right, boolean debug) {
            super();
            this.leftPart = left;
            this.rightPart = right;
        }


        @Override
        public String toString() {
            return "{" + leftPart +
                    " -> " + rightPart.stream().map((s) -> s.name).collect(Collectors.joining(", ")) +
                    '}';
        }
    }

    public static void DebugPrint(String str, boolean debug) {
        if (debug) System.out.println(str);
    }
}

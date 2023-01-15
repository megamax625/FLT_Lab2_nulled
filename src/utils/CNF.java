package utils;

import java.util.ArrayList;

public class CNF extends Grammar{

    public CNF(Grammar CFG, boolean debug) {
        super(CFG.nonterminals, CFG.terminals, CFG.rules, CFG.startingSymbol);
        this.ConvertToCNF(debug);
    }

    public void ConvertToCNF(boolean debug) {
        if (this.isCNF(debug)) return;
        else {
            this.RemoveEps(debug);
            this.RemoveChainRules(debug);
            this.AddGuardNterms(debug);
            this.BreakRulesBy2Nterms(debug);
        }
    }

    public boolean isCNF(boolean debug) {
        DebugPrint("Checking input for being CNF", debug);
        for (Rule r : rules) {
            if (!((Rule.isToNtermNterm(r)) | (Rule.isToTerm(r)) | (Rule.isToEmpty(r) && r.leftPart.equals(startingSymbol)))) {
                DebugPrint(r.toString() + " does not fit CNF definition!\nStarting conversion", debug);
                break;
            }
        }
        return false;
    }

    public void RemoveEps(boolean debug) {
        ArrayList<String> nullable = new ArrayList<>();
        for (Rule r : rules) {
            if (Rule.isToEmpty(r) && !(nullable.contains(r.leftPart.name))) {
                DebugPrint("Inserting nonterminal " + r.leftPart + " into nullable", debug);
                nullable.add(r.leftPart.name);
            }
        }
        int count;
        ArrayList<Rule> rulesSubset = new ArrayList<>(rules);
        rulesSubset.removeIf(Rule::isToEmpty);
        do {
            count = nullable.size();
            for (Rule r : new ArrayList<>(rulesSubset)) {
                //DebugPrint("Checking rule for nullableness: " + r, debug);
                //r.rightPart.forEach(n -> DebugPrint("nonterm " + n + " nullability: " + nullable.contains(n.name), debug));
                if (!nullable.contains(r.leftPart.name) && (r.rightPart.stream().allMatch(nt -> nullable.contains(nt.name)))) {
                    DebugPrint("Inserting nonterminal " + r.leftPart + " into nullable", debug);
                    nullable.add(r.leftPart.name);
                    rulesSubset.remove(r);
                }
            }
        } while (nullable.size() != count);
        if (nullable.contains(startingSymbol.name)) {
            Symbol newStart = new Symbol(startingSymbol.name.replace("]", "_0]"), startingSymbol.type);
            ArrayList<Symbol> rightPart = new ArrayList<>();
            rightPart.add(startingSymbol);
            rules.add(0, new Rule(newStart, rightPart));
            rightPart = new ArrayList<>();
            rightPart.add(new Symbol("ε"));
            rules.add(1, new Rule(newStart, rightPart));
            startingSymbol = newStart;
            DebugPrint("Added new starting symbol to CFG", debug);
        }
        ArrayList<Rule> rulesContainer = new ArrayList<>(rules);
        for (Rule r : rules) {
            if ((r.rightPart.size() > 1) && (r.rightPart.stream().anyMatch(nt -> nullable.contains(nt.name)))) {
                DebugPrint("Adding omitting rules for rule " + r.toString(), debug);
                ArrayList<Symbol> currentNullables = new ArrayList<>();
                for (Symbol s : r.rightPart) {
                    if (nullable.contains(s.name)) currentNullables.add(s);
                }
                int nullablesInRule = currentNullables.size();
                int ruleVariatons = nullablesInRule * nullablesInRule;
                ArrayList<Rule> newRules = new ArrayList<>();
                newRules.add(r);
                rulesContainer.remove(r);
                for (int i = ruleVariatons - 2; i >= 0; i--) {
                    Symbol left = r.leftPart;
                    ArrayList<Symbol> right = new ArrayList<>();
                    int pos = 0;
                    for (Symbol s : r.rightPart) {
                        if ((!s.type.equals("nonterm")) || !nullable.contains(s.name)) {
                            right.add(s);
                        } else {
                            if ((i & (int)Math.pow(2, pos)) != 0) {
                                //DebugPrint(i + " & " + (int)Math.pow(2,pos) + " = " + (i & (int)Math.pow(2, pos)), debug);
                                right.add(s);
                            }
                            pos += 1;
                        }
                    }
                    if (right.isEmpty()) right.add(new Symbol("ε"));
                    Rule newRule = new Rule(left, right);
                    boolean equal = false;
                    for (Rule nr : newRules) {
                        if (nr.rightPart.size() != newRule.rightPart.size()) continue;
                        //DebugPrint("Comparing " + nr + " and " + newRule, debug);
                        boolean stillEqual = true;
                        for (int j = 0; j < nr.rightPart.size(); j++) {
                            if (!nr.rightPart.get(j).name.equals(newRule.rightPart.get(j).name)) {
                                stillEqual = false;
                                break;
                            }
                        }
                        if (stillEqual) {
                            equal = true;
                            break;
                        }
                    }
                    if (!(equal)) {
                        DebugPrint("Adding " + newRule + ", index of " + Integer.toBinaryString(i), debug);
                        newRules.add(newRule);
                    }
                }
                rulesContainer.addAll(newRules);
            }
        }
        ArrayList<Rule> noEmptyRules = new ArrayList<>(rulesContainer);
        noEmptyRules.removeIf(r -> ((Rule.isToEmpty(r)) && (!r.leftPart.equals(startingSymbol))));
        rules = noEmptyRules;
        for (int i = 0; i < rules.size(); i++) {
            for (int j = i + 1; j < rules.size(); j++) {
                Rule r1 = rules.get(i);
                Rule r2 = rules.get(j);
                if (r1.leftPart.name.equals(r2.leftPart.name) && (r1.rightPart.size() == r2.rightPart.size())) {
                    for (int k = 0; k < r1.rightPart.size(); k++) {
                        if (!r1.rightPart.get(k).name.equals(r2.rightPart.get(k).name)) {
                            break;
                        }
                        rules.remove(j);
                    }
                }
            }
        }
        DebugPrint(CNF.getString(this), debug);
    }

    public void RemoveChainRules(boolean debug) {
        ArrayList<Rule> chainRules = new ArrayList<>(rules);
        chainRules.removeIf(r -> ((r.rightPart.size() != 1) || (!r.rightPart.get(0).type.equals("nonterm"))));
        DebugPrint("Chain rules: " + chainRules.toString(), debug);
        for (Rule chr : chainRules) {
            Symbol remover = chr.leftPart;
            Symbol toRemove = chr.rightPart.get(0);
            nonterminals.removeIf(nt -> nt.name.equals(toRemove.name));
            DebugPrint("Removed nonterm " + toRemove + " from nonterminals", debug);
            for (Rule r : new ArrayList<>(rules)) {
                if (r.leftPart.name.equals(toRemove.name)) {
                    r.leftPart = remover;
                    DebugPrint(toRemove + " to " + remover + " rewrote rule as " + r, debug);
                }
                if (r.equals(chr)) {
                    rules.remove(chr);
                    DebugPrint("Removing " + chr, debug);
                }
            }
        }
        DebugPrint(CNF.getString(this), debug);
    }

    public void AddGuardNterms(boolean debug) {
        ArrayList<Rule> rulesWithNotSingleTerm = new ArrayList<>(rules);
        rulesWithNotSingleTerm.removeIf(r -> !Rule.hasTermAndNotToTerm(r));
        DebugPrint("Rules that need guarding nonterminals: " + rulesWithNotSingleTerm, debug);
        ArrayList<String> guardedAlphabet = new ArrayList<>();
        for (Rule gr : rulesWithNotSingleTerm) {
            for (Symbol s : gr.rightPart) {
                if ((s.type.equals("term")) && (!guardedAlphabet.contains(s.name))) guardedAlphabet.add(s.name);
            }
        }
        DebugPrint("List of terminals that need guarding: " + guardedAlphabet, debug);
        for (String name : guardedAlphabet) {
            String symName = "[G_" + name + "]";
            boolean nameCollision = false;
            for (Symbol nt : nonterminals) {
                if (nt.name.equals(symName)) {
                    nameCollision = true;
                    break;
                }
            }
            boolean quit = false;
            for (Rule r : rules) {
                if ((r.rightPart.size() == 1) && (r.rightPart.get(0).name.equals(name))) {
                    DebugPrint("No need to guard terminal " + name + " cause he already has a guarding nonterminal " + r.leftPart.name, debug);
                    for (Rule r2 : rules) {
                        if ((r != r2) && (Rule.hasTermAndNotToTerm(r2) && (Rule.getTerminalNames(r2).contains(name)))) {
                            ArrayList<Symbol> newRP = new ArrayList<>();
                            for (Symbol s : r2.rightPart) {
                                if ((s.type.equals("term") && (s.name.equals(name)))) {
                                    newRP.add(new Symbol(symName));
                                } else {
                                    newRP.add(s);
                                }
                            }
                            r2.rightPart = newRP;
                        }
                    }
                    quit = true;
                    break;
                }
            }
            if (quit) continue;
            while (nameCollision) {
                symName = symName.substring(0, symName.length()-1) + "_" + name + "]";
                nameCollision = false;
                for (Symbol nt : nonterminals) {
                    if (nt.name.equals(symName)) {
                        nameCollision = true;
                        break;
                    }
                }
            }
            Symbol left = new Symbol(symName);
            Symbol right = new Symbol(name);
            ArrayList<Symbol> rightPart = new ArrayList<>();
            rightPart.add(right);
            Rule newRule = new Rule(left, rightPart);
            DebugPrint("Made new guarding " + newRule, debug);
            Symbol guarded = new Symbol(symName);
            nonterminals.add(guarded);
            for (Rule r : rules) {
                if (rulesWithNotSingleTerm.contains(r)) {
                    ArrayList<Symbol> newRP = new ArrayList<>();
                    for (Symbol s : r.rightPart) {
                        if (!s.name.equals(name)) newRP.add(s);
                        else newRP.add(guarded);
                    }
                    r.rightPart = newRP;
                }
            }
            rules.add(newRule);
        }
        DebugPrint(CNF.getString(this), debug);
    }

    public void BreakRulesBy2Nterms(boolean debug) {
        ArrayList<Rule> breakableRules = new ArrayList<>(rules);
        breakableRules.removeIf(r -> !Rule.isToNterms(r));
        rules.removeIf(r -> Rule.isToNterms(r));
        ArrayList<Rule> brokenRules = new ArrayList<>();
        for (Rule breakingBarriers : new ArrayList<>(breakableRules)) {
            while (!breakingBarriers.rightPart.isEmpty()) {
                divideRules(breakingBarriers, brokenRules, debug);
            }
        }
        DebugPrint("New rules:" + brokenRules, debug);
        rules.addAll(brokenRules);
        DebugPrint(CNF.getString(this), debug);
    }

    public void divideRules(Rule r, ArrayList<Rule> brb,boolean debug) {
        if (r.rightPart.size() == 2) {
            DebugPrint("Got to final partition of " + r, debug);
            rules.add(new Rule(r.leftPart, r.rightPart));
            r.rightPart = new ArrayList<>();
        }
        else {
            //DebugPrint("Partitioning " + r, debug);
            Symbol left = r.leftPart;
            Symbol r1 = r.rightPart.get(0);
            String r2Name = r1.name.substring(0, r1.name.length() - 1) + "f]";
            //DebugPrint("Potential new name for nonterm before checks:" + r2Name, debug);
            boolean nameCollision = false;
            for (Symbol nt : nonterminals) {
                if (nt.name.equals(r2Name)) {
                    nameCollision = true;
                    r2Name = r2Name.substring(0, r2Name.length() - 1) + "f]";
                    break;
                }
            }
            while (nameCollision) {
                nameCollision = false;
                for (Symbol nt : nonterminals) {
                    if (nt.name.equals(r2Name)) {
                        nameCollision = true;
                        r2Name = r2Name.substring(0, r2Name.length() - 1) + "f]";
                        break;
                    }
                }
            }
            //DebugPrint("Getting new nontern with name " + r2Name, debug);
            ArrayList<Symbol> newRP = new ArrayList<>();
            newRP.add(r1);
            Symbol r2 = new Symbol(r2Name);
            nonterminals.add(r2);
            newRP.add(r2);
            Rule newRule = new Rule(left, newRP);
            brb.add(newRule);
            //DebugPrint(nonterminals.toString(), debug);
            DebugPrint("Made new " + newRule, debug);
            r.rightPart.remove(0);
            r.leftPart = r2;
        }
    }

    public static void DebugPrint(String str, boolean debug) {
        if (debug) System.out.println(str);
    }

}
package utils;

import java.util.ArrayList;

public class NFA {
    ArrayList<State> states;
    ArrayList<State> finalStates;
    ArrayList<Symbol> alphabet;
    State startState;
    ArrayList<Transition> transitions;

    public NFA(ArrayList<State> sts, ArrayList<State> fSts, ArrayList<Symbol> alph, State stSym, ArrayList<Transition> tr) {
        this.states = sts;
        this.finalStates = fSts;
        this.alphabet = alph;
        this.startState = stSym;
        this.transitions = tr;
    }

    public static class Transition {
        public State left;
        public Symbol alphabetic;
        public State right;

        public Transition(State left, Symbol alph, State right) {
            this.left = left;
            this.alphabetic = alph;
            this.right = right;
        }

        @Override
        public String toString() {
            return "{" + left.symbol.name +
                    ", " + alphabetic.name +
                    ", " + right.symbol.name +
                    '}';
        }
    }

    public static class State {
        Symbol symbol;
        boolean reachable;
        boolean producing;

        public State(Symbol name) {
            this.symbol = name;
            this.reachable = false;
            this.producing = false;
        }

        public State(Symbol name, boolean reachable, boolean producing) {
            this.symbol = name;
            this.reachable = reachable;
            this.producing = producing;
        }

    }

    public static NFA RGtoAutomaton(Grammar rg, boolean debug) {
        ArrayList<State> states = new ArrayList<>();
        for (Symbol nterm : rg.nonterminals) {
            states.add(new State(nterm));
        }
        ArrayList<State> finalStates = new ArrayList<>();
        ArrayList<Symbol> alphabet = rg.terminals;
        State startState = getStateBySymbol(rg.startingSymbol, states);
        ArrayList<Transition> transitions = new ArrayList<>();

        State new_final = new State(new Symbol("[S_final]", "nonterm"));
        states.add(new_final);
        finalStates.add(new_final);

        for (Rule r : rg.rules) {
            if (Rule.isToEmpty(r)) {
                boolean alreadyFinal = false;
                for (State fs : finalStates) {
                    if (fs.symbol.name.equals(r.leftPart.name)) {
                        alreadyFinal = true;
                        break;
                    }
                }
                if (!alreadyFinal) finalStates.add(getStateBySymbol((r.leftPart), states));
            } else if (Rule.isToTerm(r)) {
                transitions.add(new Transition(getStateBySymbol((r.leftPart), states), r.rightPart.get(0), new_final));
            } else if (Rule.isToTermNterm(r)) {
                transitions.add(new Transition(getStateBySymbol((r.leftPart), states), r.rightPart.get(0), getStateBySymbol((r.rightPart.get(1)), states)));
            } else {
                System.out.println("Incorrect rule detected in conversion from RG to NFA: " + r.leftPart.name + " -> " + r.rightPart.get(0).name + "...");
            }
        }
        // удаляем недостижимые нетерминалы
        startState.reachable = true;
        int countReachable = 1;
        int countToCompare = 1;
        countReachable += markReachables(startState, transitions);
        DebugPrint("CountR: " + countReachable + ", CountTC: " + countToCompare + "\n", debug);
        while (countReachable != countToCompare) {
            countToCompare = countReachable;
            countReachable += markReachables(startState, transitions);
        }
        states.removeIf(st -> !st.reachable);
        finalStates.removeIf(fSt -> !fSt.reachable);
        transitions.removeIf(tr -> !(tr.right.reachable));
        StringBuilder dPrint = new StringBuilder("NFA:{States:[");
        if (debug) {
            for (State st : states) {
                dPrint.append(st.symbol.name).append(" ");
            }
            dPrint.append("]; finalStates:[");
            for (State fst : finalStates) {
                dPrint.append(fst.symbol.name).append(" ");
            }
            dPrint.append("]; alphabet:[");
            for (Symbol al : alphabet) {
                dPrint.append(al.name).append(" ");
            }
            dPrint.append("]; startState: ").append(startState.symbol.name).append("; transitions: [");
            for (Transition tr : transitions) {
                dPrint.append("<").append(tr.left.symbol.name).append(",").append(tr.alphabetic.name).append(",").append(tr.right.symbol.name).append(">; ");
            }
            dPrint.append("]}\n");
        }
        DebugPrint(dPrint.toString(), debug);
        // удаляем непорождающие терминалы
        ArrayList<Transition> reversedTransitions = new ArrayList<>();
        for (Transition tr : transitions) {
            reversedTransitions.add(new Transition(tr.right, tr.alphabetic, tr.left));
        }
        State newReversedReachState = new State(new Symbol("S_REV", "nonterm"));
        for (State fs : finalStates) {
            reversedTransitions.add(new Transition(newReversedReachState, new Symbol("dummy", "term"), fs));
        }
        newReversedReachState.producing = true;
        int countProducing = 1;
        countToCompare = 1;
        countProducing += markProducers(newReversedReachState, reversedTransitions);
        DebugPrint("CountPR: " + countProducing + ", CountTC: " + countToCompare + "\n", debug);
        while (countProducing != countToCompare) {
            countToCompare = countProducing;
            countProducing += markProducers(newReversedReachState, reversedTransitions);
        }
        states.removeIf(st -> !st.producing);
        finalStates.removeIf(fSt -> !fSt.producing);
        transitions.removeIf(tran -> !states.contains(tran.right));
        alphabet.removeIf(alpha -> !hasVertexWithTerminal(alpha, transitions));
        dPrint = new StringBuilder("NFA:{States:[");
        if (debug) {
            for (State st : states) {
                dPrint.append(st.symbol.name).append(" ");
            }
            dPrint.append("]; finalStates:[");
            for (State fst : finalStates) {
                dPrint.append(fst.symbol.name).append(" ");
            }
            dPrint.append("]; alphabet:[");
            for (Symbol al : alphabet) {
                dPrint.append(al.name).append(" ");
            }
            dPrint.append("]; startState: ").append(startState.symbol.name).append("; transitions: [");
            for (Transition tr : transitions) {
                dPrint.append("<").append(tr.left.symbol.name).append(",").append(tr.alphabetic.name).append(",").append(tr.right.symbol.name).append(">; ");
            }
            dPrint.append("]}\n");
        }
        DebugPrint(dPrint.toString(), debug);
        return new NFA(states, finalStates, alphabet, startState, transitions);
    }

    public static int markReachables(State s, ArrayList<Transition> transitions) {
        int added = 0;
        for (Transition tr : transitions) {
            if ((tr.left.symbol == s.symbol) && !(tr.right.reachable)) {
                added++;
                tr.right.reachable = true;
                added += markReachables(tr.right, transitions);
            }
        }
        return added;
    }

    public static int markProducers(State s, ArrayList<Transition> transitions) {
        int added = 0;
        for (Transition tr : transitions) {
            if ((tr.left.symbol == s.symbol) && !(tr.right.producing)) {
                added++;
                tr.right.producing = true;
                added += markProducers(tr.right, transitions);
            }
        }
        return added;
    }

    public static State getStateBySymbol(Symbol s, ArrayList<State> states) {
        for (State st : states) {
            if (s.name.equals(st.symbol.name)) return st;
        }
        System.out.println("Couldn't find the right state! " + s.name + " state not found!");
        System.exit(8);
        return new State(new Symbol("0"));
    }

    public static boolean hasVertexWithTerminal(Symbol a, ArrayList<Transition> tran) {
        boolean res = false;
        for (Transition tr : tran) {
            if (tr.alphabetic.equals(a)) {
                res = true;
                break;
            }
        }
        return res;
    }

    public static void DebugPrint(String str, boolean debug) {
        if (debug) System.out.print(str);
    }
}

package com.googlecode.jvmvm.tests.interpretable.third;

import java.util.*;
import java.util.Map.Entry;

/**
 * Write a function to check if the given chemical equation is balanced or not?
 * Ex: Cu + S = CuS
 * 2H2 + 02 = 2H2O
 *
 * @author ksjeyabarani
 */
public class ChemicalEquation {

    private String LHS;

    private String RHS;

    private String equation;

    private String[] equationSides;

    private List<ChemicalFormula> lhsParts;

    private List<ChemicalFormula> rhsParts;

    private Map<String, Integer> lhsTotalChemicalCountmap;

    private Map<String, Integer> rhsTotalChemicalCountmap;

    private boolean isBalanced;

    public ChemicalEquation(String equation) throws Exception {

        super();

        this.equation = equation;

        if ((null != equation) && (!equation.equals(""))) {

            equationSides = equation.split("=");

            if (equationSides.length != 2)
                throw new Exception("This is not a valid equation");

            LHS = equationSides[0];
            RHS = equationSides[1];

            lhsParts = getParts(LHS);
            rhsParts = getParts(RHS);

        }

        this.equationSides = this.getEquationSides();

        List<ChemicalFormula> lhsParts = this.getParts(this.equationSides[0]);
        List<ChemicalFormula> rhsParts = this.getParts(this.equationSides[1]);

        this.lhsTotalChemicalCountmap = totalChemicalCountForTheGivenSide(lhsParts);
        this.rhsTotalChemicalCountmap = totalChemicalCountForTheGivenSide(rhsParts);

        this.isBalanced = true;

        if (lhsTotalChemicalCountmap.size() != rhsTotalChemicalCountmap.size())
            this.isBalanced = false;


        Iterator<Entry<String, Integer>> iterator = this.lhsTotalChemicalCountmap.entrySet().iterator();

        while (iterator.hasNext()) {

            Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) iterator.next();

            if (this.lhsTotalChemicalCountmap.get(pairs.getKey()) != this.rhsTotalChemicalCountmap.get(pairs.getKey())) {
                this.isBalanced = false;
            }

        }


    }

    public List<ChemicalFormula> getParts(String equationSide) throws Exception {

        String[] parts;

        if ((null != equationSide) && (!equationSide.equals(""))) {

            parts = equationSide.split("-");

            List<ChemicalFormula> list = new LinkedList<ChemicalFormula>();

            for (int i = 0; i < parts.length; i++) {
                list.add(new ChemicalFormula(parts[i]));
            }

            return list;
        }

        return null;
    }

    public Map<String, Integer> totalChemicalCountForTheGivenSide(List<ChemicalFormula> parts) {

        Map<String, Integer> givenSideChemicalCount = new HashMap<String, Integer>();

        if ((null != parts) && (!parts.isEmpty())) {

            for (int i = 0; i < parts.size(); i++) {

                ChemicalFormula formula = parts.get(i);
                int count = formula.getCount();

                Map<String, Integer> map = formula.elementCountMap;

                Iterator<Entry<String, Integer>> iterator = map.entrySet().iterator();

                while (iterator.hasNext()) {

                    Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) iterator.next();

                    if (null == givenSideChemicalCount.get(pairs.getKey())) {
                        String ch = (String) pairs.getKey();
                        int totalCount = count * (Integer) pairs.getValue();
                        givenSideChemicalCount.put(ch, totalCount);
                    } else {
                        String ch = (String) pairs.getKey();
                        int totalCount = (count * (Integer) pairs.getValue()) + givenSideChemicalCount.get(pairs.getKey());
                        givenSideChemicalCount.put(ch, totalCount);
                    }

                }
            }

        } else {
            givenSideChemicalCount = null;
        }

        return givenSideChemicalCount;

    }

    public Map<String, Integer> getLhsTotalChemicalCountmap() {
        return lhsTotalChemicalCountmap;
    }

    public void setLhsTotalChemicalCountmap(
            Map<String, Integer> lhsTotalChemicalCountmap) {
        this.lhsTotalChemicalCountmap = lhsTotalChemicalCountmap;
    }

    public Map<String, Integer> getRhsTotalChemicalCountmap() {
        return rhsTotalChemicalCountmap;
    }

    public void setRhsTotalChemicalCountmap(
            Map<String, Integer> rhsTotalChemicalCountmap) {
        this.rhsTotalChemicalCountmap = rhsTotalChemicalCountmap;
    }

    public boolean isBalanced() throws Exception {
        return this.isBalanced;
    }

    public String getLHS() {
        return LHS;
    }

    public void setLHS(String lHS) {
        LHS = lHS;
    }

    public String getRHS() {
        return RHS;
    }

    public void setRHS(String rHS) {
        RHS = rHS;
    }

    public String[] getEquationSides() {
        return equationSides;
    }

    public void setEquationSides(String[] equationSides) {
        this.equationSides = equationSides;
    }

    public String getEquation() {
        return equation;
    }

    public void setEquation(String equation) {
        this.equation = equation;
    }


    @Override
    public String toString() {
        return "ChemicalEquation [equation=" + equation + ", equationSides="
                + Arrays.toString(equationSides) + ", lhsParts=" + lhsParts
                + ", rhsParts=" + rhsParts + ", lhsTotalChemicalCountmap="
                + lhsTotalChemicalCountmap + ", rhsTotalChemicalCountmap="
                + rhsTotalChemicalCountmap + ", isBalanced=" + isBalanced + "]";
    }

    private static class ChemicalFormula {

        String formula;
        int count;
        Map<String, Integer> elementCountMap = new HashMap<String, Integer>();

        public String getFormula() {
            return formula;
        }

        public void setFormula(String formula) {
            this.formula = formula;
        }

        public Map<String, Integer> getElementCountMap() {
            return elementCountMap;
        }

        public void setElementCountMap(Map<String, Integer> elementCount) {
            this.elementCountMap = elementCount;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }


        public ChemicalFormula(String formula) throws Exception {

            if ((null != formula) && (!formula.equals(""))) {

                this.formula = formula;

                formula = formula.trim();

                int formulaSize = formula.length();

                boolean notADigit = false;
                int x = 0;

                // get count
                if (Character.isDigit(Character.valueOf(formula.charAt(0)))) {

                    x = 1;
                    StringBuilder countStringBuilder = new StringBuilder("" + Character.valueOf(formula.charAt(0)));

                    while (true) {
                        if (notADigit || (x >= formulaSize)) {
                            break;
                        }

                        if (Character.isDigit(Character.valueOf(formula.charAt(x)))) {
                            countStringBuilder.append("" + Character.valueOf(formula.charAt(x)));
                            x++;
                        } else {
                            notADigit = true;
                        }

                    }
                    count = Integer.parseInt(countStringBuilder.toString());
                }

                String key = "";
                Integer value = null;
                boolean previousWasASymbol = false;

                while (x < formulaSize) {

                    value = null;

                    if (Character.isDigit(Character.valueOf(formula.charAt(x)))) {

                        StringBuilder countStringBuilder = new StringBuilder("");

                        while ((x < formulaSize) && (Character.isDigit(Character.valueOf(formula.charAt(x))))) {
                            countStringBuilder.append("" + Character.valueOf(formula.charAt(x)));
                            x++;
                        }

                        value = Integer.parseInt(countStringBuilder.toString());
                        elementCountMap.put(key, value);

                    } else {

                        key = "";

                        StringBuilder elementStringBuilder = null;
                        elementStringBuilder = new StringBuilder("");


                        if (Character.isUpperCase(Character.valueOf(formula.charAt(x)))) {

                            elementStringBuilder.append("" + Character.valueOf(formula.charAt(x)));
                            x++;

                            while ((x < formulaSize) && (Character.isLowerCase(Character.valueOf(formula.charAt(x))))) {
                                elementStringBuilder.append(Character.valueOf(formula.charAt(x)));
                                x++;
                            }

                        }
                        key = elementStringBuilder.toString();
                        previousWasASymbol = true;

                    }
                    if (!(key.equals("") || (null == key)) && (null != value)) {
                        elementCountMap.put(key, value);
                    }
                    if (previousWasASymbol && (null == value)) {
                        elementCountMap.put(key, new Integer(1));
                    }

                }
            }

            if (count == 0) {
                count = 1;
            }

        }

        @Override
        public String toString() {
            return "ChemicalFormula [formula=" + formula + ", count=" + count
                    + ", elementCountMap=" + elementCountMap + "]";
        }

    }

    public static String run() throws Exception {
        ChemicalEquation equation = new ChemicalEquation("NaOH-H2SO4=NaSHO4-H2O");
        return equation + " => " + equation.isBalanced;
    }


}

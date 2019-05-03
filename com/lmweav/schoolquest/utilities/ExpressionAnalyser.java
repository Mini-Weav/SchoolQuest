package com.lmweav.schoolquest.utilities;

import com.lmweav.schoolquest.characters.NPC;
import com.lmweav.schoolquest.items.Item;

import java.util.ArrayList;
import java.util.Arrays;

import static com.lmweav.schoolquest.Game.GAME;
import static com.lmweav.schoolquest.Constants.*;

/*
 * School Quest: ExpressionAnalyser
 * This class is a small expression analyser for reading text files that are used in the game.
 *
 * Methods in this class tokenise, parse and analyse the supplied data.
 *
 * @author Luke Weaver
 * @version 1.0.8
 * @since 2019-05-02
 */
public class ExpressionAnalyser {

    private static ArrayList<String> numericVariables = new ArrayList<>();
    private static ArrayList<String> booleanVariables = new ArrayList<>();
    private static ArrayList<String> inequalityOperators = new ArrayList<>();
    private static ArrayList<String> booleanOperators = new ArrayList<>();

    static {
        numericVariables.add("FP0");
        numericVariables.add("FP1");
        numericVariables.add("FP2");
        numericVariables.add("FP3");
        numericVariables.add("FP4");

        numericVariables.add("GP0");
        numericVariables.add("GP1");
        numericVariables.add("GP2");
        numericVariables.add("GP3");
        numericVariables.add("GP4");

        numericVariables.add("ITEM");

        numericVariables.add("TIME");
        numericVariables.add("RESPONSE");
        numericVariables.add("ID");
        numericVariables.add("CONDITION");

        numericVariables.add("PLAYER_X");
        numericVariables.add("PLAYER_Y");

        booleanVariables.add("TRUE");
        booleanVariables.add("FALSE");

        booleanVariables.add("SPOKEN");

        booleanVariables.add("GIVE");

        booleanVariables.add("KEY0");
        booleanVariables.add("KEY1");
        booleanVariables.add("KEY2");
        booleanVariables.add("KEY3");
        booleanVariables.add("KEY4");
        booleanVariables.add("KEY5");

        booleanVariables.add("GF");
        booleanVariables.add("HAS_GF");
        booleanVariables.add("PLAN");
        booleanVariables.add("HEIST");
        booleanVariables.add("WON");
        booleanVariables.add("SUSPENDED");
        booleanVariables.add("MINIGAME");

        booleanOperators.add("AND");
        booleanOperators.add("OR");
        booleanOperators.add("NOT");

        inequalityOperators.add("==");
        inequalityOperators.add("!=");
        inequalityOperators.add(">");
        inequalityOperators.add("<");
        inequalityOperators.add(">=");
        inequalityOperators.add("<=");
    }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    private static boolean isInteger(String token) {
        try {
            Integer.parseInt(token);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private static boolean isOperator(String token) {
        return booleanOperators.contains(token) || inequalityOperators.contains(token);
    }

    private static boolean isVariable(String token) {
        return booleanVariables.contains(token) || numericVariables.contains(token);
    }

    private static boolean isValid(String token) {
        return isVariable(token) || isOperator(token) || isInteger(token);
    }

    private static ArrayList<String> tokenise(String expression) {
        String[] tokens = expression.split("\\s+");
        for (String token : tokens) {
            if (!isValid(token)) {
                throw new IllegalStateException(); }
        }
        return new ArrayList<>(Arrays.asList(tokens));
    }

    private static void splitExpressions(ArrayList<String> tokens,
                                         ArrayList<ArrayList<String>> exps,
                                         ArrayList<String> boolOps) {
        int i = 0;
        exps.add(new ArrayList<String>());
        for (String token : tokens) {
            if (booleanOperators.contains(token) && !token.equals("NOT")) {
                boolOps.add(token);
                exps.add(new ArrayList<String>());
                i++;
            } else { exps.get(i).add(token); }
        }
    }

    private static Object setVariable(String token, NPC npc) {
        switch (token) {
            case "FP0":
                return GAME.getFriendScore(0);
            case "FP1":
                return GAME.getFriendScore(1);
            case "FP2":
                return GAME.getFriendScore(2);
            case "FP3":
                return GAME.getFriendScore(3);
            case "FP4":
                return GAME.getFriendScore(4);

            case "GP0":
                return GAME.getGradeScore(0);
            case "GP1":
                return GAME.getGradeScore(1);
            case "GP2":
                return GAME.getGradeScore(2);
            case "GP3":
                return GAME.getGradeScore(3);
            case "GP4":
                return GAME.getGradeScore(4);

            case "PLAYER_X":
                return GAME.getPlayer().getX();
            case "PLAYER_Y":
                return GAME.getPlayer().getY();

            case "KEY0":
                return GAME.hasItem(Item.getItem(KEY0_INDEX));
            case "KEY1":
                return GAME.hasItem(Item.getItem(KEY1_INDEX));
            case "KEY2":
                return GAME.hasItem(Item.getItem(KEY2_INDEX));
            case "KEY3":
                return GAME.hasItem(Item.getItem(KEY3_INDEX));
            case "KEY4":
                return GAME.hasItem(Item.getItem(KEY4_INDEX));
            case "KEY5":
                return GAME.hasItem(Item.getItem(KEY5_INDEX));

            case "GIVE":
                return npc.canGive() && GAME.getPlayer().getResponseIndex() != 3;

            case "ITEM":
                return npc.getItemResponseIndex();

            case "TIME":
                return GAME.getTime();
            case "RESPONSE":
                return GAME.getPlayer().getResponseIndex();
            case "ID":
                return npc.getId();
            case "CONDITION":
                return GAME.getPlayer().getCondition();

            case "TRUE":
                return true;
            case "FALSE":
                return false;

            case "GF":
                return GAME.getGfIndex() == npc.getId();
            case "HAS_GF":
                return GAME.getGfIndex() >= 0;
            case "PLAN":
                return GAME.getProgressDataStructure().hasHeistPlan();
            case "HEIST":
                return GAME.hasItem(Item.getItem(KEY0_INDEX)) &&
                        GAME.hasItem(Item.getItem(KEY3_INDEX)) &&
                        GAME.hasItem(Item.getItem(KEY4_INDEX));
            case "WON":
                return GAME.getProgressDataStructure().hasWonHeist();
            case "SUSPENDED":
                return GAME.getProgressDataStructure().hasLostHeist();

            case "MINIGAME":
                return GAME.getMiniGame() != null;

            case "SPOKEN":
                return GAME.spokenTo(npc);

            default:
                if (isInteger(token)) {
                    return Integer.parseInt(token);
                }
                throw new IllegalStateException();
        }
    }

    private static boolean evaluateExpression(ArrayList<String> expression, NPC npc) {
        boolean not = false;
        Object var1, var2;
        int i = 0, operatorIndex;

        if (expression.get(i).equals("NOT")) {
            not = true;
            i++;
        }
        if (not && !booleanVariables.contains(expression.get(i))) {
            throw new IllegalStateException();
        }

        var1 = setVariable(expression.get(i), npc);
        i++;
        if (var1 instanceof Boolean) {
            if (not) { return !((Boolean) var1);}
            return (Boolean) var1;
        }
        if (!inequalityOperators.contains(expression.get(i))) {
            throw new IllegalStateException();
        }
        operatorIndex = i;
        i++;

        if (expression.get(i).equals("NOT")) {
            throw new IllegalStateException();
        }

        var2 = setVariable(expression.get(i), npc);

        if (var2 instanceof Boolean) {
            throw new IllegalStateException();
        }

        switch (expression.get(operatorIndex)) {
            case "==":
                return var1 == var2;
            case "!=":
                return var1 != var2;
            case ">":
                return (Integer) var1 > (Integer) var2;
            case "<":
                return (Integer) var1 < (Integer) var2;
            case ">=":
                return (Integer) var1 >= (Integer) var2;
            case "<=":
                return (Integer) var1 <= (Integer) var2;

            default:
                throw new IllegalStateException();
        }
    }

    public static boolean analyse(String expression, NPC npc) {
        ArrayList<String> tokens = tokenise(expression);
        ArrayList<Boolean> evals = new ArrayList<>();

        ArrayList<ArrayList<String>> exps = new ArrayList<>();
        ArrayList<String> boolOps = new ArrayList<>();

        boolean result;


        splitExpressions(tokens, exps, boolOps);

        for (ArrayList<String> exp : exps) {
            evals.add(evaluateExpression(exp, npc));
        }

        if (boolOps.size() == 0) { return evals.get(0); }

        switch (boolOps.get(0)) {
            case "AND":
                result = evals.get(0) && evals.get(1);
                break;
            case "OR":
                result = evals.get(0) || evals.get(1);
                break;

            default:
                throw new IllegalStateException();

        }

        for (int i = 2; i < evals.size(); i++) {
            switch (boolOps.get(i - 1)) {
                case "AND":
                    result = result && evals.get(i);
                    break;
                case "OR":
                    result = result || evals.get(i);
                    break;

                default:
                    throw new IllegalStateException();
            }
        }

        return result;
    }
}

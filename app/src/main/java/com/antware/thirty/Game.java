package com.antware.thirty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Game {

    private final static int MAX_ROUNDS = 10;
    private final static int MAX_THROWS = 3;

    private int round = 1;
    private int score = 0;
    private int diceThrowsLeft;

    private Die[] dice = new Die[6];
    private Combination[] combs = new Combination[10];
    private Combination pickedComb;
    private boolean isCombPicked;

    private int[] roundScores = new int[MAX_ROUNDS];

    public void initGame() {
        round = 1;
        score = 0;
        diceThrowsLeft = MAX_THROWS;
        isCombPicked = false;
        for (int i = 0; i < dice.length; i++) {
            dice[i] = new Die();
        }
        for (int i = 0; i < combs.length; i++)
            combs[i] = new Combination(i);
    }

    public void throwDice() {
        for (Die die : dice) {
            if (!die.isPicked())
                die.throwDie();
        }
        Set<Set<List<Die>>> allDicePartitions = Combination.getPartitions(new ArrayList<>(Arrays.asList(dice)));
        for (Combination comb : combs) {
            comb.computePoints(allDicePartitions, dice);
        }
        diceThrowsLeft--;
        if (diceThrowsLeft == 0) {
            if (pickedComb != null)
                score += pickedComb.getPoints();
        }
        else if (diceThrowsLeft < 0) {
            roundScores[round - 1] = pickedComb == null ? 0 : pickedComb.getPoints();
            round++;
            diceThrowsLeft += MAX_THROWS;
            isCombPicked = false;
            pickedComb = null;
        }

    }

    public int getThrowsLeft() {
        return diceThrowsLeft;
    }

    public int getScore() {
        return score;
    }

    public int getRound() {
        return round;
    }

    public int getDieFace(int i) {
        return dice[i].getFace();
    }

    public int getCombPoints(int i) {
        return combs[i].getPoints();
    }

    public boolean isCombPicked() {
        return isCombPicked;
    }

    public void setPickedComb(int cardNum) {
        pickedComb = combs[cardNum];
        pickedComb.setPickedComb(true);
        isCombPicked = true;

        if (diceThrowsLeft == 0)
            score += pickedComb.getPoints();
    }

    public int getMaxThrows() {
        return MAX_THROWS;
    }

    public boolean isDiePicked(int index) {
        return dice[index].isPicked();
    }

    public void setDiePicked(int index, boolean isPicked) {
        dice[index].setPicked(isPicked);
    }

    public int getMaxRounds() {
        return MAX_ROUNDS;
    }
}

package com.antware.thirty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Game {

    private final static int NUM_ROUNDS = 10;
    private final static int MAX_THROWS = 3;

    private int round = 1;
    private int score = 0;
    private int diceThrowsLeft;

    private Die[] dice = new Die[6];
    private Combination[] combs = new Combination[10];
    private Combination pickedComb;

    private int[] scorePerRound = new int[NUM_ROUNDS];
    private int[] combPerRound = new int[NUM_ROUNDS];

    public void initGame() {
        round = 1;
        score = 0;
        diceThrowsLeft = MAX_THROWS;
        pickedComb = null;
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

        if (diceThrowsLeft == 0 && pickedComb != null)
            setRoundScore();
        else if (diceThrowsLeft < 0)
            initNextRound();
    }

    private void initNextRound() {
        round++;
        diceThrowsLeft += MAX_THROWS;
        pickedComb = null;
        scorePerRound[round - 1] = 0;
    }

    private void setRoundScore() {
        score += pickedComb.getPoints();
        scorePerRound[round - 1] = pickedComb.getPoints();
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
        return pickedComb != null;
    }

    public void setPickedComb(int cardNum) {
        pickedComb = combs[cardNum];
        pickedComb.setPickedComb(true);
        combPerRound[round - 1] = pickedComb.getNameAsInt();

        if (diceThrowsLeft == 0)
            setRoundScore();
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
        return NUM_ROUNDS;
    }

    public int[] getScorePerRound() {
        return scorePerRound;
    }

    public int[] getCombPerRound() {
        return combPerRound;
    }

    public void setRound(int round) {
        this.round = round;
    }
}

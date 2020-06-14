package com.antware.thirty;

public class Game {

    private final static int MAX_ROUNDS = 10;
    private final static int MAX_THROWS = 3;

    private int round;
    private int score;
    private int diceThrowsLeft;

    private Die[] dice = new Die[6];
    private Combination[] combs = new Combination[10];
    private Combination pickedComb;
    private boolean isCombPicked;

    public void initGame() {
        round = 0;
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
            die.throwDie();
        }
        if (--diceThrowsLeft == 0) {
            round++;
            diceThrowsLeft = MAX_THROWS;
            isCombPicked = false;
        }
        for (Combination comb : combs) {
            comb.computePoints(dice);
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
    }

    public int getMaxThrows() {
        return MAX_THROWS;
    }
}

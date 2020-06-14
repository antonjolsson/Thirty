package com.antware.thirty;

public class Game {

    private final static int MAX_ROUNDS = 10;
    private final static int MAX_THROWS = 3;

    private int round;
    private int score;
    private int diceThrowsLeft;

    private Die[] dices = new Die[6];
    private Combination[] combs = new Combination[10];

    public void initGame() {
        round = 0;
        score = 0;
        diceThrowsLeft = MAX_THROWS;
        for (Die die : dices)
            die = new Die();
        for (int i = 0; i < combs.length; i++)
            combs[i] = new Combination(i);
    }


    public Combination[] getCombs() {
        return combs;
    }

    public void throwDice() {

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
}

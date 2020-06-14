package com.antware.thirty;

public class Game {

    private final static int MAX_ROUNDS = 10;
    private final static int MAX_THROWS = 3;

    private int round;
    private int score;
    private int diceThrowsLeft;

    private Die[] dice = new Die[6];
    private Combination[] combs = new Combination[10];

    public void initGame() {
        round = 0;
        score = 0;
        diceThrowsLeft = MAX_THROWS;
        for (int i = 0; i < dice.length; i++) {
            dice[i] = new Die();
        }
        for (int i = 0; i < combs.length; i++)
            combs[i] = new Combination(i);
    }


    public Combination[] getCombs() {
        return combs;
    }

    public void throwDice() {
        for (Die die : dice) {
            die.throwDie();
        }
        if (--diceThrowsLeft == 0) {
            round++;
            diceThrowsLeft = MAX_THROWS;
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
}

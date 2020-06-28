package com.antware.thirty;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Game implements Parcelable {

    private final static int NUM_ROUNDS = 2;
    private final static int MAX_THROWS = 3;

    private int round = 1;
    private int score;
    private int diceThrowsLeft;

    private Die[] dice = new Die[6];
    private Combination[] combs = new Combination[10];
    private Combination combPickedThisRound;

    private int[] scorePerRound = new int[NUM_ROUNDS];
    private int[] combPerRound = new int[NUM_ROUNDS];

    public Game() {}

    protected Game(Parcel in) {
        round = in.readInt();
        score = in.readInt();
        diceThrowsLeft = in.readInt();

        restoreDice(in);

        initCombinations();
        int pickCombNum = in.readInt();
        if (pickCombNum >= 0) setCombPicked(pickCombNum);
        computeCombPoints();

        scorePerRound = in.createIntArray();
        combPerRound = in.createIntArray();
    }

    private void restoreDice(Parcel in) {
        int[] dieFaces = new int[dice.length];
        boolean[] pickedDice = new boolean[dice.length];
        in.readIntArray(dieFaces);
        in.readBooleanArray(pickedDice);
        initDice(dieFaces);
        for (int i = 0; i < dice.length; i++) {
            dice[i].setPicked(pickedDice[i]);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(round);
        dest.writeInt(score);
        dest.writeInt(diceThrowsLeft);

        dest.writeIntArray(getDieFaces());
        dest.writeBooleanArray(getPickedDice());
        dest.writeInt(combPickedThisRound != null ? combPickedThisRound.getOrderNumber() : -1);

        dest.writeIntArray(scorePerRound);
        dest.writeIntArray(combPerRound);
    }

    private boolean[] getPickedDice() {
        boolean[] pickedDice = new boolean[dice.length];
        for (int i = 0; i < dice.length; i++) {
            pickedDice[i] = dice[i].isPicked();
        }
        return pickedDice;
    }

    private int[] getDieFaces() {
        int[] dieFaces = new int[6];
        for (int i = 0; i < dieFaces.length; i++) {
            dieFaces[i] = dice[i].getFace();
        }
        return dieFaces;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Game> CREATOR = new Creator<Game>() {
        @Override
        public Game createFromParcel(Parcel in) {
            return new Game(in);
        }

        @Override
        public Game[] newArray(int size) {
            return new Game[size];
        }
    };

    public void initGame() {
        round = 1;
        score = 0;
        diceThrowsLeft = MAX_THROWS;
        combPickedThisRound = null;
        initDice(null);
        initCombinations();
    }

    private void initCombinations() {
        for (int i = 0; i < combs.length; i++)
            combs[i] = new Combination(i);
    }

    private void initDice(int[] faces) {
        for (int i = 0; i < dice.length; i++) {
            if (faces != null)
                dice[i] = new Die(faces[i]);
            else dice[i] = new Die();
        }
    }

    public void throwDice() {
        for (Die die : dice) {
            if (!die.isPicked())
                die.throwDie();
        }
        computeCombPoints();
        diceThrowsLeft--;

        if (diceThrowsLeft == 0 && combPickedThisRound != null)
            setRoundScore();
        else if (diceThrowsLeft < 0)
            initNextRound();
    }

    private void computeCombPoints() {
        Set<Set<List<Die>>> allDicePartitions = Combination.getPartitions(new ArrayList<>(Arrays.asList(dice)));
        for (Combination comb : combs) {
            if (comb != combPickedThisRound && comb.hasBeenPicked())
                continue;
            comb.computePoints(allDicePartitions, dice);
        }
    }

    private void initNextRound() {
        round++;
        diceThrowsLeft += MAX_THROWS;
        combPickedThisRound = null;
        scorePerRound[round - 1] = 0;
    }

    private void setRoundScore() {
        score += combPickedThisRound.getPoints();
        scorePerRound[round - 1] = combPickedThisRound.getPoints();
        combPickedThisRound.setFinalPoints();
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

    public boolean isAnyCombPickedThisRound() {
        return combPickedThisRound != null;
    }

    public void setCombPicked(int cardNum) {
        combPickedThisRound = combs[cardNum];
        combPickedThisRound.setPickedComb(true);
        combPerRound[round - 1] = combPickedThisRound.getNameAsInt();

        if (diceThrowsLeft == 0)
            setRoundScore();
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

    public boolean isCombPicked(int comb) {
        return combs[comb].hasBeenPicked();
    }

    public int getFinalCombPoints(int i) {
        return combs[i].getFinalPoints();
    }
}

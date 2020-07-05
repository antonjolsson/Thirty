package com.antware.thirty;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Class for handling pure game logic.
 * @author Anton J Olsson
 */
public class Game implements Parcelable {

    private final static int NUM_ROUNDS = 10;
    private final static int MAX_ROLLS = 3;

    private int round = 1;
    private int score;
    private int diceRollsLeft;

    private Die[] dice = new Die[6];
    private Combination[] combs = new Combination[10];
    private Combination combPickedThisRound;

    private int[] scorePerRound;
    private int[] combPerRound;

    /**
     * Constructs a new Game.
     */
    public Game() {}

    /**
     * Constructor used to restore the current game state after being destroyed.
     * @param in The game state as a Parcel.
     */
    protected Game(Parcel in) {
        round = in.readInt();
        score = in.readInt();
        diceRollsLeft = in.readInt();

        restoreDice(in);

        initCombinations();
        int pickCombNum = in.readInt();
        if (pickCombNum >= 0) setCombPicked(pickCombNum);

        scorePerRound = in.createIntArray();
        combPerRound = in.createIntArray();
        for (int i = 0; i < round; i++) {
            assert combPerRound != null;
            int orderNum = Combination.getOrderNum(combPerRound[i]);
            if (orderNum < 0) continue;
            combs[orderNum].setPickedComb(true);
            combs[orderNum].setPoints(scorePerRound[i]);
        }

        computeCombPoints();
    }

    // Restore current dice set from saved state

    /**
     * Restores the current dice set from saved state.
     * @param in The game state as a Parcel.
     */
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

    /**
     * Saves current game state as a Parcel.
     * @param dest The Parcel to write to.
     * @param flags Additional flags about how the object should be written
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(round);
        dest.writeInt(score);
        dest.writeInt(diceRollsLeft);

        dest.writeIntArray(getDieFaces());
        dest.writeBooleanArray(getPickedDice());
        dest.writeInt(combPickedThisRound != null ? combPickedThisRound.getOrderNumber() : -1);

        dest.writeIntArray(scorePerRound);
        dest.writeIntArray(combPerRound);
    }

    /**
     * Returns all dice having been picked this round.
     * @return the picked dice
     */
    private boolean[] getPickedDice() {
        boolean[] pickedDice = new boolean[dice.length];
        for (int i = 0; i < dice.length; i++) {
            pickedDice[i] = dice[i].isPicked();
        }
        return pickedDice;
    }

    /**
     * Returns all die faces as an int[].
     * @return all die faces
     */
    private int[] getDieFaces() {
        int[] dieFaces = new int[6];
        for (int i = 0; i < dieFaces.length; i++) {
            dieFaces[i] = dice[i].getFace();
        }
        return dieFaces;
    }

    /**
     * Code necessary for implementing Parcelable.
     * <a href>https://developer.android.com/reference/android/os/Parcelable</a>
     */
    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Game> CREATOR = new Creator<Game>() {
        /**
         * Code necessary for implementing Parcelable.
         * <a href>https://developer.android.com/reference/android/os/Parcelable.Creator</a>
         */
        @Override
        public Game createFromParcel(Parcel in) {
            return new Game(in);
        }

        /**
         * Code necessary for implementing Parcelable.
         * <a href>https://developer.android.com/reference/android/os/Parcelable.Creator</a>
         */
        @Override
        public Game[] newArray(int size) {
            return new Game[size];
        }
    };

    /**
     * Initializes the game state; sets all members to their initial value/state.
     */
    public void initGame() {
        round = 1;
        score = 0;
        diceRollsLeft = MAX_ROLLS;
        combPickedThisRound = null;
        scorePerRound = new int[NUM_ROUNDS];
        combPerRound = new int[NUM_ROUNDS];
        initDice(null);
        initCombinations();
    }

    /**
     * Creates new combinations and puts them in the array of combinations.
     */
    private void initCombinations() {
        for (int i = 0; i < combs.length; i++)
            combs[i] = new Combination(i);
    }

    /**
     * Creates new dice and puts them in dice.
     * @param faces the faces of the dice, if called to restore game state
     */
    private void initDice(int[] faces) {
        for (int i = 0; i < dice.length; i++) {
            if (faces != null)
                dice[i] = new Die(faces[i]);
            else dice[i] = new Die();
        }
    }

    /**
     * Reduces the number of diceRollsLeft, rolls dice, computes new points for each
     * combination. Depending on state, possibly also sets the score of this round or inits a new round.
     */
    public void rollDice() {
        diceRollsLeft--;

        for (Die die : dice) {
            if (!die.isPicked())
                die.rollDie();
        }
        computeCombPoints();

        if (diceRollsLeft == 0 && combPickedThisRound != null)
            setRoundScore();
        else if (diceRollsLeft < 0)
            initNextRound();
    }

    /**
     * Computes max points of all non-picked combinations (and possibly of a combination picked this
     * round, if rolls left).
     */
    private void computeCombPoints() {
        Set<Set<List<Die>>> allDicePartitions = Combination.getPartitions(new ArrayList<>(Arrays.asList(dice)));
        for (Combination comb : combs) {
            if (comb != combPickedThisRound && comb.isPicked())
                continue;
            comb.computePoints(allDicePartitions, dice);
        }
    }

    /**
     * Increases current round number, resets dice-rolls left and sets round score to 0.
     */
    private void initNextRound() {
        round++;
        diceRollsLeft += MAX_ROLLS;
        scorePerRound[round - 1] = 0;
    }

    /**
     * Adds round-score to total score and total score/round tally and sets combPickedThisRound
     * to null.
     */
    private void setRoundScore() {
        score += combPickedThisRound.getPoints();
        scorePerRound[round - 1] = combPickedThisRound.getPoints();
        combPickedThisRound = null;
    }

    /**
     * Returns the number of dice-rolls left this round.
     * @return the number of dice-rolls left
     */
    public int getRollsLeft() {
        return diceRollsLeft;
    }

    /**
     * Returns the current game score.
     * @return the game score
     */
    public int getScore() {
        return score;
    }

    /**
     * Returns the current round.
     * @return the current round
     */
    public int getRound() {
        return round;
    }

    /**
     * Returns the face of a die of a given index.
     * @param i the index of the die in dice
     * @return the face of the die
     */
    public int getDieFace(int i) {
        return dice[i].getFace();
    }

    /**
     * Returns the current points of a combination of a given index.
     * @param i the index of the combination in the array of combinations
     * @return the current points of the combination
     */
    public int getCombPoints(int i) {
        return combs[i].getPoints();
    }

    /**
     * Returns whether any combination has been picked this round.
     * @return whether any combination has been picked
     */
    public boolean isAnyCombPickedThisRound() {
        return combPerRound[round - 1] > 0;
    }

    /**
     * Marks a combination as picked. If no dice-rolls left, also sets the round's score.
     * @param cardNum the index of the combination in combs
     */
    public void setCombPicked(int cardNum) {
        combPickedThisRound = combs[cardNum];
        combPickedThisRound.setPickedComb(true);
        combPerRound[round - 1] = combPickedThisRound.getNameAsInt();

        if (diceRollsLeft == 0)
            setRoundScore();
    }

    /**
     * Returns whether a given die has been picked.
     * @param index the index of the die in dice
     * @return whether the die has been picked
     */
    public boolean isDiePicked(int index) {
        return dice[index].isPicked();
    }

    /**
     * Marks a given die as picked or not.
     * @param index the index of the die
     * @param isPicked whether the die should be marked as picked or not
     */
    public void setDiePicked(int index, boolean isPicked) {
        dice[index].setPicked(isPicked);
    }

    /**
     * Returns the number of rounds in the game.
     * @return the number of rounds in the game
     */
    public int getMaxRounds() {
        return NUM_ROUNDS;
    }

    /**
     * Returns the score for each round.
     * @return the score for each round
     */
    public int[] getScorePerRound() {
        return scorePerRound;
    }

    /**
     * Returns the picked combinations for each round.
     * @return the picked combinations
     */
    public int[] getCombPerRound() {
        return combPerRound;
    }

    /**
     * Returns whether a given combination has been picked.
     * @param comb the index of the combination
     * @return whether the combination has been picked
     */
    public boolean isCombPicked(int comb) {
        return combs[comb].isPicked();
    }

}

package com.antware.thirty;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class representing a point-combination (Low, 3, 5, ... , 12)
 * @author Anton J Olsson
 */
public class Combination implements Cloneable{

    /**
     * Returns all possible partitions of a List of Die.
     * @param dice the List of Die of which to return partitions
     * @return A Set containing all partitions; a partition is represented by a Set, containing parts
     * in the form of List's
     */
    public static Set<Set<List<Die>>> getPartitions(List<Die> dice){
        if (dice.size() == 0 || dice.size() == 1) {
            Set<Set<List<Die>>> partitions = new HashSet<>();
            Set<List<Die>> partition = new HashSet<>();
            partition.add(dice);
            partitions.add(partition);
            return partitions;
        }
        else {
            Set<Set<List<Die>>> partitions = getPartitions(dice.subList(0, dice.size() - 1));
            Set<Set<List<Die>>> partitionsCopy = new HashSet<>();
            Die lastDie = dice.get(dice.size() - 1);
            for (Set<List<Die>> partition : partitions) {
                Set<List<Die>> partitionCopy = getCopy(partition);
                partitionCopy.add(Collections.singletonList(lastDie));
                partitionsCopy.add(partitionCopy);

                for (List<Die> part : partition) {
                    Set<List<Die>> partitionCopy2 = getCopy(partition);
                    List<Die> partCopy = new ArrayList<>(part);
                    partCopy.add(lastDie);
                    partitionCopy2.remove(part);
                    partitionCopy2.add(partCopy);
                    partitionsCopy.add(partitionCopy2);
                }

            }
            return partitionsCopy;
        }
    }

    /**
     * Copies a <code>Set of <code>List's of <code>Die, representing a partition of dice.
     * @param partition the partition to be copied
     * @return A copy of the partition
     */
    private static Set<List<Die>> getCopy(Set<List<Die>> partition) {
        Set<List<Die>> partitionCopy = new HashSet<>();
        for (List<Die> list : partition) {
            List<Die> newList = new ArrayList<>(list);
            partitionCopy.add(newList);
        }
        return partitionCopy;
    }

    /**
     * Returns the 0-indexed order number of a Combination (starting from Low).
     * @param nameAsInt name of the combination as an <code>int
     * @return The 0-indexed order number
     */
    public static int getOrderNum(int nameAsInt) {
        return nameAsInt == 1 ? 0 : nameAsInt - LOWEST_NUM_VALUE + 1;
    }

    private final static int LOWEST_NUM_VALUE = 4;
    private final static int HIGHEST_NUM_VALUE = 12;

    private enum CombName {LOW, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, ELEVEN, TWELVE}
    CombName[] combNames = CombName.values();

    private CombName name;

    /**
     * If <code>Combination has been picked and its max points added to total score; final max points. Else,
     * max points of current dice.
     */
    private int points = 0;
    /**
     * Representing whether this <code>Combination has been picked or not.
     */
    private boolean isPicked;

    /**
     * Constructs a new <code>Combination given an index number. Sets <code>isPicked to <code>false as default.
     * @param combNum the index number from which the <code>Combination sets its <code>name.
     */
    Combination(int combNum) {
        name = combNames[combNum];
        isPicked = false;
    }

    /**
     * Sets the points this <code>Combination yields.
     * @param points the number of points
     */
    public void setPoints(int points) {
        this.points = points;
    }

    /**
     * Returns the number of points this <code>Combination yields.
     * @return The number of points
     */
    public int getPoints() {
        return points;
    }

    /**
     * Returns whether this <code>Combination has been picked yet or not.
     * @return the value of <code>isPicked
     */
    public boolean isPicked() {
        return isPicked;
    }

    /**
     * Sets whether this <code>Combination has been picked yet or not.
     * @param isPicked whether this <code>Combination has been picked or not
     */
    public void setPickedComb(boolean isPicked) {
        this.isPicked = isPicked;
    }

    /**
     * Computes max points for this combination from all possible dice partitions.
     * @param allPartitions all possible partitions of the current dice set
     * @param dice the current dice set, used to compute points for <code>Combination LOW.
     */
    public void computePoints(Set<Set<List<Die>>> allPartitions, Die[] dice) {
        points = 0;
        if (name == CombName.LOW){
            addLowFaces(dice);
            return;
        }
        for (Set<List<Die>> partition : allPartitions) {
            int combPoints = 0;
            for (List<Die> part : partition) {
                int sum = getDiceSum(part);
                if (sum >= LOWEST_NUM_VALUE && sum <= HIGHEST_NUM_VALUE)
                    if (combNames[sum - LOWEST_NUM_VALUE + 1] == name)
                        combPoints += sum;
            }
            if (combPoints > points)
                points = combPoints;
        }
    }

    /**
     * Returns the sum of faces of a <code>List of <code>Die.
     * @param part the <code>List of <List>Die.</List>
     * @return The sum
     */
    private int getDiceSum(List<Die> part) {
        int sum = 0;
        for (Die die : part) {
            sum += die.getFace();
        }
        return sum;
    }

    // Get the sum of all dice with values < 4

    /**
     * Adds the sum of all dice with values less than 4 to <code>points.
     * @param dice the current dice set
     */
    private void addLowFaces(Die[] dice) {
        for (Die die : dice) {
            if (die.getFace() < LOWEST_NUM_VALUE)
                points += die.getFace();
        }
    }

    /**
     * Necessary to make this class <code>Cloneable.
     * @return A clone of an object
     * @throws CloneNotSupportedException
     */
    @NotNull
    public Object clone() throws
            CloneNotSupportedException
    {
        return super.clone();
    }

    /**
     * Returns name of this object as an int. LOW equals 1, other combinations
     * are represented by their name in numeric form.
     * @return name as an int.
     */
    public int getNameAsInt() {
        if (name == CombName.LOW) return 1;
        else return getOrderNumber() + LOWEST_NUM_VALUE - 1;
    }

    // Get the 0-indexed order number of this combination

    /**
     *
     * @return Returns the 0-indexed order number of this combination, as ordered in CombName.
     */
    public int getOrderNumber() {
        for (int i = 0; i < combNames.length; i++) {
            if (combNames[i] == name)
                return i;
        }
        return -1;
    }

}

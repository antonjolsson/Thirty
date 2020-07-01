package com.antware.thirty;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Class representing a point combination (Low, 4, 5... etc)
public class Combination implements Cloneable{

    // Get all possible partitions of the current dice set
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

    // Copy a partition
    private static Set<List<Die>> getCopy(Set<List<Die>> partition) {
        Set<List<Die>> partitionCopy = new HashSet<>();
        for (List<Die> list : partition) {
            List<Die> newList = new ArrayList<>(list);
            partitionCopy.add(newList);
        }
        return partitionCopy;
    }

    // Get the 0-indexed order number of a combination (starting from Low)
    public static int getOrderNum(int nameAsInt) {
        return nameAsInt == 1 ? 0 : nameAsInt - LOWEST_NUM_VALUE + 1;
    }

    private final static int LOWEST_NUM_VALUE = 4;
    private final static int HIGHEST_NUM_VALUE = 12;

    private enum CombName {LOW, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, ELEVEN, TWELVE}
    CombName[] combNames = CombName.values();

    private CombName name;
    // If combination has been picked and its max points added to total score; final max points. Else,
    // max points of current dice.
    private int points = 0;
    private boolean isPicked; // Has this combination been picked?

    Combination(int combNum) {
        name = combNames[combNum];
        isPicked = false;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }

    public boolean isPicked() {
        return isPicked;
    }

    public void setPickedComb(boolean isPicked) {
        this.isPicked = isPicked;
    }

    // Compute max points for this combination from all possible dice partitions
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

    private int getDiceSum(List<Die> part) {
        int sum = 0;
        for (Die die : part) {
            sum += die.getFace();
        }
        return sum;
    }

    // Get the sum of all dice with values < 4
    private void addLowFaces(Die[] dice) {
        for (Die die : dice) {
            if (die.getFace() < LOWEST_NUM_VALUE)
                points += die.getFace();
        }
    }

    @NotNull
    public Object clone() throws
            CloneNotSupportedException
    {
        return super.clone();
    }

    public int getNameAsInt() {
        if (name == CombName.LOW) return 1;
        else return getOrderNumber() + LOWEST_NUM_VALUE - 1;
    }

    // Get the 0-indexed order number of this combination
    public int getOrderNumber() {
        for (int i = 0; i < combNames.length; i++) {
            if (combNames[i] == name)
                return i;
        }
        return -1;
    }

}

package com.antware.thirty;

import java.util.ArrayList;
import java.util.List;

public class Combination {

    private enum CombName {LOW, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, ELEVEN, TWELVE}

    private CombName name;
    private int points = 0;
    private boolean isPicked;

    public int getPoints() {
        return points;
    }

    public boolean isPicked() {
        return isPicked;
    }

    public void setPickedComb(boolean isPicked) {
        this.isPicked = isPicked;
    }
    public void computePoints(Die[] dice) {
        points = 0;
        List<List<Die>> diceCombs = getDiceCombs(dice);
        for (List<Die> diceComb : diceCombs) {
            int combPoints = 0;
            for (Die die : diceComb) {
                combPoints += die.getFace();
            }
            if (combPoints > points)
                points = combPoints;
        }
    }

    private List<List<Die>> getDiceCombs(Die[] dice) {
        List<List<Die>> diceCombs = new ArrayList<>();
        if (name == CombName.LOW) {
            getLowFaces(dice, diceCombs);
        }
        else getExactCombs(dice, diceCombs);
        return diceCombs;
    }

    private void getExactCombs(Die[] dice, List<List<Die>> diceCombs) {

    }

    private void getLowFaces(Die[] dice, List<List<Die>> diceCombs) {
        List<Die> lowFaces = new ArrayList<>();
        for (Die die : dice) {
            if (die.getFace() < 4)
                lowFaces.add(die);
        }
        diceCombs.add(lowFaces);
    }


    Combination(int combNum) {
        CombName[] combNames = CombName.values();
        name = combNames[combNum];
        isPicked = false;
    }

}

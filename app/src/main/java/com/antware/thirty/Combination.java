package com.antware.thirty;

public class Combination {

    private CombName name;
    private int points = 0;
    private boolean isPicked;

    public int getPoints() {
        return points;
    }

    public boolean isPicked() {
        return isPicked;
    }

    private enum CombName {LOW, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, ELEVEN, TWELVE}
    private CombName[] combNames = CombName.values();

    Combination(int combNum) {
        name = combNames[combNum];
    }

}

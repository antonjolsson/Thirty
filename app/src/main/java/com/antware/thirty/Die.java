package com.antware.thirty;

import java.util.Random;

// Class representing a die
public class Die {

    Random random = new Random();
    // Value between 1 and 6
    private int face = 0;
    // Is the die picked so it won't be re-throwed?
    private boolean isPicked;

    Die() {
        setFace(0);
    }

    Die(int face) {
        this.face = face;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public void throwDie() {
        face = random.nextInt(6) + 1;
    }

    public int getFace() {
        return face;
    }

    public boolean isPicked() {
        return isPicked;
    }

    public void setPicked(boolean isPicked) {
        this.isPicked = isPicked;
    }
}

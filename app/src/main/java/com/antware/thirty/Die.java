package com.antware.thirty;

import java.util.Random;

public class Die {

    Random random = new Random();
    private int face = 0;
    private boolean isPicked;

    Die() {
        setFace(0);
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

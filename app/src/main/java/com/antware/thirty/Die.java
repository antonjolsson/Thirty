package com.antware.thirty;

import java.util.Random;

/**
 * Class representing a six-faced die (plural: dice).
 * @author Anton J Olsson
 */
public class Die {

    Random random = new Random();
    // Value between 1 and 6
    private int face = 0;
    // Is the die picked so it won't be re-rolled?
    private boolean isPicked;

    /**
     * Constructs a new <code>Die.</code> Sets face to 0 as default.
     */
    Die() {
        setFace(0);
    }

    /**
     * Constructs a new <code>Die.</code>
     * @param face the initial face of the die
     */
    Die(int face) {
        this.face = face;
    }

    /**
     * Sets the face of the die.
     * @param face the new face of the die.
     */
    public void setFace(int face) {
        this.face = face;
    }

    /**
     * Sets the face to a random number 1 - 6.
     */
    public void rollDie() {
        face = random.nextInt(6) + 1;
    }

    /**
     * Returns the face.
     * @return the current face
     */
    public int getFace() {
        return face;
    }

    /**
     * Returns whether this die has been picked in the current round.
     * @return whether this die has been picked
     */
    public boolean isPicked() {
        return isPicked;
    }

    /**
     * Sets whether this die has been picked in the current round.
     * @param isPicked whether this die has been picked
     */
    public void setPicked(boolean isPicked) {
        this.isPicked = isPicked;
    }
}

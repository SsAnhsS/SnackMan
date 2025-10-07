package de.hsrm.mi.swt.snackman.entities.mobileObjects;

import java.util.Random;

public enum ScriptGhostDifficulty {
    EASY, DIFFICULT;

    /**
     * Gets a random script ghost difficulty
     *
     * @return random script ghost difficulty
     */
    public static ScriptGhostDifficulty getRandomScriptGhostDifficulty() {
        ScriptGhostDifficulty[] difficulties = {EASY, DIFFICULT};
        Random random = new Random();
        int randomIndex = random.nextInt(difficulties.length);
        return difficulties[randomIndex];
    }

    /**
     * Converts a string into a difficulty level for the script ghosts.
     *
     * @param difficulty to be converted
     * @return the difficulty as enum type
     */
    public static ScriptGhostDifficulty getScriptGhostDifficulty(String difficulty) {
        if (difficulty.equals("DIFFICULT")) {
            return ScriptGhostDifficulty.DIFFICULT;
        }
        return ScriptGhostDifficulty.EASY;
    }
}

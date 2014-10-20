/********************
 * Minesweeper.java *
 ********************
 **
 ** So much for Asimov's Laws. They're actually trying to kill
 ** you now. Not to be alarmist, but the floor is littered
 ** with mines. Rushing for the exit blindly may be unwise.
 ** I need you alive, after all.
 **
 ** If only there was some way you could track the positions
 ** of the mines...
 **
 **/

package com.googlecode.jvmvm.ui.levels.level_05;

import java.awt.*;

public class Minesweeper extends Level {
    private int getRandomInt(int min, int max) {
        return (int) Math.floor(Math.random() * (max - min + 1)) + min;
    }

    @Override
    public void startLevel(Map map) {
/*START_OF_START_LEVEL*/
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                map.setSquareColor(x, y, new Color(1f, 0f, 0f));
            }
        }

        map.placePlayer(map.getWidth() - 5, 5);

        for (int i = 0; i < 75; i++) {
            int x = getRandomInt(0, map.getWidth() - 1);
            int y = getRandomInt(0, map.getHeight() - 1);
            if ((x != 2 || y != map.getHeight() - 1)
                    && (x != map.getWidth() - 5 || y != 5)) {
                // don't place mine over exit or player!
                map.placeObject(x, y, "mine");
/*BEGIN_EDITABLE*/

/*END_EDITABLE*/
            }
        }

        map.placeObject(2, map.getHeight() - 1, "exit");
/*END_OF_START_LEVEL*/
    }

    @Override
    public boolean validateLevel(Map map) {
        map.validateAtLeastXObjects(40, "mine");
        map.validateExactlyXManyObjects(1, "exit");
        return true;
    }
}

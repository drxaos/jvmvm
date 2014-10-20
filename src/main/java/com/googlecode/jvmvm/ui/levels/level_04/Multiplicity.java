/*********************
 * Multiplicity.java *
 *********************
 **
 ** Out of one cell and into another. They're not giving you
 ** very much to work with here, either. Ah, well.
 **
 ** Level filenames can be hints, by the way. Have I
 ** mentioned that before?
 **
 ** No more cells after this one. I promise.
 **
 **/

package com.googlecode.jvmvm.ui.levels.level_04;

public class Multiplicity extends Level {
    @Override
    public void startLevel(Map map) {
/*START_OF_START_LEVEL*/

        map.placePlayer(map.getWidth() - 5, map.getHeight() - 4);

        for (int y = 7; y <= map.getHeight() - 3; y++) {
            map.placeObject(7, y, "block");
            map.placeObject(map.getWidth() - 3, y, "block");
        }
/*BEGIN_EDITABLE*/

/*END_EDITABLE*/
        for (int x = 7; x <= map.getWidth() - 3; x++) {
            map.placeObject(x, 7, "block");
            map.placeObject(x, map.getHeight() - 3, "block");
        }

        map.placeObject(map.getWidth() - 5, 5, "exit");
/*END_OF_START_LEVEL*/
    }
}
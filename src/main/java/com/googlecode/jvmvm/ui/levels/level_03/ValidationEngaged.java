/************************
 * validationEngaged.js *
 ************************
 **
 ** They're really on to us now! The validateLevel function
 ** has been activated to enforce constraints on what you can
 ** do. In this case, you're not allowed to remove any blocks.
 **
 ** They're doing all they can to keep you here. But you
 ** can still outsmart them.
 **
 **/

package com.googlecode.jvmvm.ui.levels.level_03;

public class ValidationEngaged extends Level {
    public void startLevel(Map map) {
/*START_OF_START_LEVEL*/
        map.placePlayer(map.getWidth() - 7, map.getHeight() - 5);
/*BEGIN_EDITABLE*/

        for (int y = 10; y <= map.getHeight() - 3; y++) {
            map.placeObject(5, y, "block");
            map.placeObject(map.getWidth() - 5, y, "block");
        }

        for (int x = 5; x <= map.getWidth() - 5; x++) {
            map.placeObject(x, 10, "block");
            map.placeObject(x, map.getHeight() - 3, "block");
        }
/*END_EDITABLE*/

        map.placeObject(7, 5, "exit");
/*END_OF_START_LEVEL*/
    }

    public boolean validateLevel(Map map) {
        int numBlocks = 2 * (map.getHeight() - 13) + 2 * (map.getWidth() - 10);

        map.validateAtLeastXObjects(numBlocks, "block");
        map.validateExactlyXManyObjects(1, "exit");
        return true;
    }
}

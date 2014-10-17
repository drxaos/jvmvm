/**
 **********************
 * TheLongWayOut.java *
 **********************
 **
 ** Well, it looks like they're on to us. The path isn't as
 ** clear as I thought it'd be. But no matter - four clever
 ** characters should be enough to erase all their tricks.
 **
 **/

package com.googlecode.jvmvm.ui.levels.level_02;

public class TheLongWayOut extends Level {

    public void startLevel(final Map map) {
/*START_OF_START_LEVEL*/
        map.placePlayer(7, 5);

        Maze maze = new Maze(map.getWidth(), map.getHeight());
/*BEGIN_EDITABLE*/

/*END_EDITABLE*/
        maze.create(new Maze.Callback() {

            @Override
            public void cell(int x, int y, int mapValue) {
                // don't write maze over player
                if (map.getPlayer().atLocation(x, y)) {
                    return;
                } else if (mapValue == 1) { // 0 is empty space 1 is wall
                    map.placeObject(x, y, "block");
                } else {
                    map.placeObject(x, y, "empty");
                }
            }
        });

        map.placeObject(map.getWidth() - 4, map.getHeight() - 4, "block");
        map.placeObject(map.getWidth() - 6, map.getHeight() - 4, "block");
        map.placeObject(map.getWidth() - 5, map.getHeight() - 5, "block");
        map.placeObject(map.getWidth() - 5, map.getHeight() - 3, "block");
/*BEGIN_EDITABLE*/

/*END_EDITABLE*/
        map.placeObject(map.getWidth() - 5, map.getHeight() - 4, "exit");
/*END_OF_START_LEVEL*/
    }
}

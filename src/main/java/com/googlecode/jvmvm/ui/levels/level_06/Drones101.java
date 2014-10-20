/******************
 * Drones101.java *
 ******************
 **
 ** Do you remember, my dear Professor, a certain introductory
 ** computational rationality class you taught long ago? Assignment
 ** #2, behavior functions of autonomous agents? I remember that one
 ** fondly - but attack drones are so much easier to reason about
 ** when they're not staring you in the face, I would imagine!
 **
 **/

package com.googlecode.jvmvm.ui.levels.level_06;

import java.awt.*;

public class Drones101 extends Level {
    private int getRandomInt(int min, int max) {
        return (int) Math.floor(Math.random() * (max - min + 1)) + min;
    }

    private void moveToward(Object obj, String type) {
        Point target = obj.findNearest(type);
        int leftDist = obj.getX() - target.x;
        int upDist = obj.getY() - target.y;

        String direction;
        if (upDist == 0 && leftDist == 0) {
            return;
        }
        if (upDist > 0 && upDist >= leftDist) {
            direction = "up";
        } else if (upDist < 0 && upDist < leftDist) {
            direction = "down";
        } else if (leftDist > 0 && leftDist >= upDist) {
            direction = "left";
        } else {
            direction = "right";
        }

        if (obj.canMove(direction)) {
            obj.move(direction);
        }
    }

    @Override
    public void startLevel(Map map) {
/*START_OF_START_LEVEL*/

        map.defineObject("attackDrone", new Definition() {
            {
                type = "dynamic";
                symbol = 'd';
                color = Color.RED;
            }

            @Override
            public void onCollision(Player player) {
                player.killedBy("an attack drone");
            }

            @Override
            public void behavior(Object me) {
                moveToward(me, "player");
            }
        });


        map.placePlayer(1, 1);
        map.placeObject(map.getWidth() - 2, 12, "attackDrone");
        map.placeObject(map.getWidth() - 1, 12, "exit");

        map.placeObject(map.getWidth() - 1, 11, "block");
        map.placeObject(map.getWidth() - 2, 11, "block");
        map.placeObject(map.getWidth() - 1, 13, "block");
        map.placeObject(map.getWidth() - 2, 13, "block");
/*BEGIN_EDITABLE*/

/*END_EDITABLE*/
/*END_OF_START_LEVEL*/
    }

    @Override
    public boolean validateLevel(Map map) {
        map.validateExactlyXManyObjects(1, "exit");
        return true;
    }
}

/**
 ************************
 * FordingTheRiver.java *
 ************************
 **
 ** And there's the river. Fortunately, I was prepared for this.
 ** See the raft on the other side?
 **
 ** Everything is going according to plan.
 **
 */

package com.googlecode.jvmvm.ui.levels.level_09;


import java.awt.*;

public class FordingTheRiver extends Level {

    String raftDirection = "down";

    @Override
    public void startLevel(final Map map) {
/*START_OF_START_LEVEL*/


        map.placePlayer(map.getWidth() - 1, map.getHeight() - 1);
        Player player = map.getPlayer();

        map.defineObject("raft", new Definition() {
            {
                type = "dynamic";
                symbol = '▓';
                color = new Color(0x44,0x22,0x00);
                transport = true; // (prevents player from drowning in water)
            }

            @Override
            public void behavior(Object me) {
                me.move(raftDirection);
            }
        });

        map.defineObject("water", new Definition() {
            {
                symbol = '░';
                color = new Color(0x44,0x44,0xff);
            }

            @Override
            public void onCollision(Player player) {
                player.killedBy("drowning in deep dark water");
            }
        });

        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 5; y < 15; y++) {
                map.placeObject(x, y, "water");
            }
        }

        map.placeObject(20, 5, "raft");
        map.placeObject(0, 2, "exit");
        map.placeObject(0, 1, "block");
        map.placeObject(1, 1, "block");
        map.placeObject(0, 3, "block");
        map.placeObject(1, 3, "block");

/*BEGIN_EDITABLE*/



/*END_EDITABLE*/
/*END_OF_START_LEVEL*/
    }

    @Override
    public boolean validateLevel(Map map) {
        map.validateExactlyXManyObjects(1, "exit");
        map.validateExactlyXManyObjects(1, "raft");
        return true;
    }
}

/***************
 * Colors.java *
 ***************
 **
 ** You're almost at the exit. You just need to get past this
 ** color lock.
 **
 ** Changing your environment is no longer enough. You must
 ** learn to change yourself. I've sent you a little something
 ** that should help with that.
 **
 **/

package com.googlecode.jvmvm.ui.levels.level_07;

import java.awt.*;

public class Colors extends Level {

    @Override
    public void startLevel(final Map map) {
/*START_OF_START_LEVEL*/
        map.placePlayer(0, 12);

        map.placeObject(5, 12, "phone");

        // The function phone lets you call arbitrary functions,
        // as defined by player.setPhoneCallback() below.
        // The function phone callback is bound to Q or Ctrl-6.
        map.getPlayer().setPhoneCallback(new PhoneCallback() {
            @Override
            public void callback() {
/*BEGIN_EDITABLE*/
                Player player = map.getPlayer();

                player.setColor(new Color(1f, 0f, 0f));





/*END_EDITABLE*/
            }
        });


        map.defineObject("redLock", new Definition() {
            {
                symbol = '☒';
                color = Color.RED;
            }

            @Override
            public boolean impassable(Player player, String type, Object object) {
                return !player.getColor().equals(object.getColor());
            }
        });

        map.defineObject("greenLock", new Definition() {
            {
                symbol = '☒';
                color = Color.GREEN;
            }

            @Override
            public boolean impassable(Player player, String type, Object object) {
                return !player.getColor().equals(object.getColor());
            }
        });

        map.defineObject("yellowLock", new Definition() {
            {
                symbol = '☒';
                color = Color.YELLOW;
            }

            @Override
            public boolean impassable(Player player, String type, Object object) {
                return !player.getColor().equals(object.getColor());
            }
        });

        for (int x = 20; x <= 40; x++) {
            map.placeObject(x, 11, "block");
            map.placeObject(x, 13, "block");
        }
        map.placeObject(22, 12, "greenLock");
        map.placeObject(25, 12, "redLock");
        map.placeObject(28, 12, "yellowLock");
        map.placeObject(31, 12, "greenLock");
        map.placeObject(34, 12, "redLock");
        map.placeObject(37, 12, "yellowLock");
        map.placeObject(40, 12, "exit");
        for (int y = 0; y < map.getHeight(); y++) {
            if (y != 12) {
                map.placeObject(40, y, "block");
            }
            for (int x = 41; x < map.getWidth(); x++) {
                map.setSquareColor(x, y, new Color(0, 0x88, 0));
            }
        }
/*END_OF_START_LEVEL*/
    }

    @Override
    public boolean validateLevel(Map map) {
        map.validateExactlyXManyObjects(1, "exit");
        return true;
    }

    @Override
    public boolean onExit(Map map) {
        if (!map.getPlayer().hasItem("phone")) {
            map.writeStatus("We need the phone!");
            return false;
        } else {
            return true;
        }
    }
}

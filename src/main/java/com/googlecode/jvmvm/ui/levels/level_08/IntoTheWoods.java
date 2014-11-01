/*********************
 * IntoTheWoods.java *
 *********************
 **
 ** Ah, you're out of the woods now. Or into the woods, as the
 ** case may be.
 **
 ** So take a deep breath, relax, and remember what you're here
 ** for in the first place.
 **
 ** I've traced its signal and the Algorithm is nearby. You'll
 ** need to go through the forest and across the river, and
 ** you'll reach the fortress where it's kept. Their defences
 ** are light, and we should be able to catch them off-guard.
 **
 **/

package com.googlecode.jvmvm.ui.levels.level_08;


import java.util.HashMap;

public class IntoTheWoods extends Level {

    @Override
    public void startLevel(final Map map) {
/*START_OF_START_LEVEL*/
        // NOTE: In this level alone, map.placeObject is allowed to
        //overwrite existing objects.

        map.displayChapter("Chapter 2\nRaiders of the Lost Algorithm");

        map.placePlayer(2, map.getHeight() - 1);

        HashMap<String, Function> functionList = new HashMap<String, Function>();

        functionList.put("fortresses", new Function() {

            int genRandomValue(String direction) {
                if (direction.equals("height")) {
                    return (int) Math.floor(Math.random() * (map.getHeight() - 3));
                } else if (direction.equals("width")) {
                    return (int) Math.floor(Math.random() * (map.getWidth() + 1));
                }
                return 0;
            }

            @Override
            public void call() {
                int x = genRandomValue("width");
                int y = genRandomValue("height");

                for (int i = x - 2; i < x + 2; i++) {
                    map.placeObject(i, y - 2, "block");
                }
                for (int i = x - 2; i < x + 2; i++) {
                    map.placeObject(i, y + 2, "block");
                }

                for (int j = y - 2; j < y + 2; j++) {
                    map.placeObject(x - 2, j, "block");
                }

                for (int j = y - 2; j < y + 2; j++) {
                    map.placeObject(x + 2, j, "block");
                }
            }
        });

        functionList.put("generateForest", new Function() {
            @Override
            public void call() {
                for (int i = 0; i < map.getWidth(); i++) {
                    for (int j = 0; j < map.getHeight(); j++) {

                        // initialize to empty if the square contains a forest already
                        if (map.getObjectTypeAt(i, j).equals("tree")) {
                            // remove existing forest
                            map.placeObject(i, j, "empty");
                        }

                        if (map.getPlayer().atLocation(i, j) ||
                                map.getObjectTypeAt(i, j).equals("block") ||
                                map.getObjectTypeAt(i, j).equals("exit")) {
                            continue;
                        }

                        double rv = Math.random();
                        if (rv < 0.45) {
                            map.placeObject(i, j, "tree");
                        }
                    }
                }
            }
        });

        functionList.put("movePlayerToExit", new Function() {
            @Override
            public void call() {
                map.writeStatus("Permission denied.");
            }
        });

        functionList.put("pleaseMovePlayerToExit", new Function() {
            @Override
            public void call() {
                map.writeStatus("I don't think so.");
            }
        });

        functionList.put("movePlayerToExitDamnit", new Function() {
            @Override
            public void call() {
                map.writeStatus("So, how 'bout them <LOCAL SPORTS TEAM>?");
            }
        });

        // generate forest
        functionList.get("generateForest").call();

        // generate fortresses
        functionList.get("fortresses").call();
        functionList.get("fortresses").call();
        functionList.get("fortresses").call();
        functionList.get("fortresses").call();

        map.getPlayer().setPhoneCallback(functionList.get("/*BEGIN_INLINE*/movePlayerToExit"/*END_INLINE*/));

        map.placeObject(map.getWidth() - 1, map.getHeight() - 1, "exit");
/*END_OF_START_LEVEL*/
    }

    @Override
    public boolean validateLevel(Map map) {
        map.validateAtLeastXObjects(100, "tree");
        map.validateExactlyXManyObjects(1, "exit");
        return true;
    }
}

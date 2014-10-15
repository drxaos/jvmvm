package com.googlecode.jvmvm.ui.levels.level_01.internal;

import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.ui.levels.level_01.Player;

public class DefinitionExecutor {
    Definition definition;
    Project project;

    public DefinitionExecutor(Definition definition, Project project) {
        this.definition = definition;
        this.project = project;
    }

    public void onCollision(Player player) {
        project.setupVM(Bootstrap.class.getName(), "onCollision", null, new Class[]{Definition.class, Player.class}, new Object[]{definition, player}).run(2000);
    }

    public void onPickUp(Player player) {
        project.setupVM(Bootstrap.class.getName(), "onPickUp", null, new Class[]{Definition.class, Player.class}, new Object[]{definition, player}).run(2000);
    }

    public void behavior(Me me) {
        definition.behavior(me);
    }

    public void onDrop() {
        definition.onDrop();
    }

}

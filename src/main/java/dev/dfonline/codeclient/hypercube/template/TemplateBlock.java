package dev.dfonline.codeclient.hypercube.template;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TemplateBlock {
    /**
     * "block" | "bracket"
     */
    public String id;
    /**
     * "open" | "close"
     */
    @Nullable public String direct;
    @Nullable public String block;
    @Nullable public String data;
    @Nullable public String action;

    public enum Block {
        CALL_FUNC("CALL FUNCTION",true),
        CONTROL("CONTROL",true),
        ELSE("ELSE",false),
        ENTITY_ACTION("ENTITY ACTION",true),
        ENTITY_EVENT("ENTITY EVENT",true),
        EVENT("PLAYER EVENT",true),
        FUNC("FUNCTION",true),
        GAME_ACTION("GAME ACTION",true),
        IF_ENTITY("IF PLAYER",false),
        IF_GAME("IF GAME",false),
        IF_PLAYER("IF PLAYER",false),
        IF_VAR("IF VARIABLE",false),
        PLAYER_ACTION("PLAYER ACTION",true),
        PROCESS("PROCESS",true),
        REPEAT("REPEAT",false),
        SELECT_OBJ("SELECT OBJECT",true),
        SET_VAR("SET VARIABLE",true),
        START_PROCESS("START PROCESS",true);

        public final String name; // TODO: check I got all these names right.
        public final boolean hasStone;

        Block(String name, boolean hasStone) {
            this.name = name;
            this.hasStone = hasStone;
        }

    }
    public int getLength() {
        return id.equals("block")
                ? Block.valueOf(block.toUpperCase()).hasStone ? 2 : 1
                : Objects.equals(direct, "open") ? 1 : 2;
    }
}

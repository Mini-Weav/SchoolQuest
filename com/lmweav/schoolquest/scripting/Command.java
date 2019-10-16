package com.lmweav.schoolquest.scripting;

import com.lmweav.schoolquest.characters.GameCharacter;

/*
 * School Quest: Command
 * This abstract class is used to define script behaviours.
 *
 * This behaviour is defined in the execute method, with the reset method used to set the
 * object to its initial state.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
abstract class Command {

    int lineIndex;
    boolean finished = false;

    GameCharacter actor;

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/
    abstract void execute();

    abstract void reset();
}

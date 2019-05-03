package com.lmweav.schoolquest.scripting;

import com.lmweav.schoolquest.characters.GameCharacter;

/*
 * School Quest: WaitCommand
 *
 * This class is a script command that waits until a game character's command at the supplied
 * index is finished.
 *
 * @author Luke Weaver
 * @version 1.0.8
 * @since 2019-05-02
 */
public class WaitCommand extends Command {

    private Script script;

    private boolean timer;

    private GameCharacter target;
    private int index;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    WaitCommand(Script script, GameCharacter target, int index) {
        this.script = script;

        timer = false;
        this.target = target;
        this.index = index;
    }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    @Override
    public void execute() {
        if (!timer) {
            finished = script.isCommandFinished(target, index);
        }
    }

    @Override
    public void reset() { finished = false; }
}

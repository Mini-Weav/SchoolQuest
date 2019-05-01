package com.lmweav.schoolquest.scripting;

import com.lmweav.schoolquest.R;

import static com.lmweav.schoolquest.Game.GAME;

/*
 * School Quest: JingleCommand
 *
 * This class is a script command that plays a specified jingle audio.
 *
 * @author Luke Weaver
 * @version 1.0.5
 * @since 2019-04-21
 */
public class JingleCommand extends Command {

    private int jingle;
    private boolean started;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    JingleCommand(String jingle) {
        switch (jingle) {
            case "item":
                this.jingle = R.raw._jingle_get_item;
                break;
            default:
                this.jingle = -1;
                break;
        }
    }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    @Override
    public void execute() {
        if (started) { finished = true; }

        if (jingle > -1 && !started) {
            started = true;
            GAME.playJingle(jingle);
        }
    }

    @Override
    public void reset() {
        started = false;
        finished = false;
    }

}

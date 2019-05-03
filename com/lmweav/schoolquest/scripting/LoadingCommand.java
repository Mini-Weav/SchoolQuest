package com.lmweav.schoolquest.scripting;

import static com.lmweav.schoolquest.Game.GAME;

/*
 * School Quest: LoadingCommand
 *
 * This class is a script command that enables/disables the loading screen view.
 *
 * @author Luke Weaver
 * @version 1.0.8
 * @since 2019-05-02
 */
public class LoadingCommand extends Command {
    private boolean loading;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    LoadingCommand(boolean loading) {
        this.loading = loading;
    }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    @Override
    public void execute() {
        GAME.setLoadingScreen(loading, -1);
        finished = true;
    }

    @Override
    public void reset() { finished = false; }
}

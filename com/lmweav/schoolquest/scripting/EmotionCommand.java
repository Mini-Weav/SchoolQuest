package com.lmweav.schoolquest.scripting;

import com.lmweav.schoolquest.characters.Emotion;
import com.lmweav.schoolquest.characters.GameCharacter;

/*
 * School Quest: EmotionCommand
 *
 * This class is a script command that gives a game character an emotion object.
 *
 * @author Luke Weaver
 * @version 1.0.5
 * @since 2019-04-21
 */
class EmotionCommand extends Command {

    private int emotionId;

    private boolean started;

    private GameCharacter actor;
    private Script script;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    EmotionCommand(GameCharacter actor, String emotion, Script script) {
        this.actor = actor;
        emotionId = Emotion.getEmotionIndex(emotion);

        this.script = script;
    }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    @Override
    void execute() {
        if (!started) {
            actor.setEmotion(emotionId);
            started = true;
        }
        else if (actor.getEmotion() == null || script.uiFinished()) {
            actor.setEmotion(-1);
            finished = true;
        }

    }

    @Override
    void reset() {
        finished = false;
        started = false;
        actor.setEmotion(-1);
    }
}

package com.lmweav.schoolquest;


import java.io.Serializable;

/*
 * School Quest: GameProgressDataStructure
 * This class holds all data relating to event triggers in the game.
 *
 * Methods in this class are predominantly getters and setters. The Serializable interface is
 * implemented to allow this data to read when loading a game save.
 *
 * @author Luke Weaver
 * @version 1.0.5
 * @since 2019-04-15
 */
public class GameProgressDataStructure implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean hasHeistPlan = false;
    private boolean startedHeist = false;
    private boolean enteredStaffRoom = false;
    private boolean hackedPC = false;
    private boolean wonHeist = false;

    private boolean lostHeist = false;

    private boolean catchNPCInteraction = false;
    private boolean catchInteractiveTile = false;

    private int timeBeforeHeist;

    private String catchNPCInteractionText = null;
    private String catchInteractiveTileText = null;

    private boolean[] madeCraft = new boolean[4];
    private boolean[] madeSnack = new boolean[4];

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public boolean hasHeistPlan() { return hasHeistPlan; }
    public void setHeistPlan() { hasHeistPlan = true; }

    boolean hasStartedHeist() { return startedHeist; }
    void setStartedHeist() { startedHeist= true; }

    boolean hasEnteredStaffRoom() { return enteredStaffRoom; }
    public void setEnteredStaffRoom() { enteredStaffRoom = true; }

    boolean hasHackedPC() { return hackedPC; }
    void setHackedPC() { hackedPC = true; }

    public boolean hasWonHeist() { return wonHeist; }
    void setWonHeist() { wonHeist = true; }

    public boolean hasLostHeist() { return lostHeist; }
    public void setLostHeist() { lostHeist = true; }

    boolean isCatchNPCInteraction() { return catchNPCInteraction; }
    void setCatchNPCInteraction() { catchNPCInteraction = true; }
    void resetCatchNPCInteraction() { catchNPCInteraction = false; }

    public boolean isCatchInteractiveTile() { return catchInteractiveTile; }
    void setCatchInteractiveTile() { catchInteractiveTile = true; }
    void resetCatchInteractiveTile() { catchInteractiveTile = false; }

    public int getTimeBeforeHeist() { return timeBeforeHeist; }
    void setTimeBeforeHeist(int time) { timeBeforeHeist = time; }

    String getCatchNPCInteractionText() { return catchNPCInteractionText; }
    void setCatchNPCInteractionText(String text) { catchNPCInteractionText = text; }

    public String getCatchInteractiveTileText() { return catchInteractiveTileText; }
    void setCatchInteractiveTileText(String text) { catchInteractiveTileText = text; }


    public boolean isMadeCraftD() { return madeCraft[0]; }
    public boolean isMadeCraftC() { return madeCraft[1]; }
    public boolean isMadeCraftB() { return madeCraft[2]; }
    public boolean isMadeCraftA() { return madeCraft[3]; }
    public void setMadeCraft(int index) { madeCraft[index] = true; }

    public boolean isMadeSnackD() { return madeSnack[0]; }
    public boolean isMadeSnackC() { return madeSnack[1]; }
    public boolean isMadeSnackB() { return madeSnack[2]; }
    public boolean isMadeSnackA() { return madeSnack[3]; }
    public void setMadeSnack(int index) { madeSnack[index] = true; }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    void NGPlus() {
        hasHeistPlan = false;
        startedHeist = false;
        enteredStaffRoom = false;
        hackedPC = false;
        wonHeist = false;
        lostHeist = false;
        catchNPCInteraction = false;
        catchInteractiveTile = false;
        catchNPCInteractionText = null;
        catchInteractiveTileText = null;
    }
}

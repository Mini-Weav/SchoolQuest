package com.lmweav.schoolquest.characters;

import android.util.Pair;

import java.util.HashMap;

/*
 * School Quest: NPCDataStructure
 * This class is a data structure for a NPC's external resource ids.
 *
 * @author Luke Weaver
 * @version 1.0.8
 * @since 2019-05-02
 */
public class NPCDataStructure {

    private int npcId;
    private HashMap<String, Integer> imgIds = new HashMap<>();
    private int txtId;
    private int shopId;
    private int textBoxImgId;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    @SafeVarargs
    NPCDataStructure(int npcId, int imgId, int txtId, int shopId, int textBoxImgId,
                            Pair<String, Integer>... altIds) {
        this.npcId = npcId;
        this.txtId = txtId;
        this.shopId = shopId;
        this.textBoxImgId = textBoxImgId;

        imgIds.put("default", imgId);

        for (Pair<String, Integer> altId : altIds) {
            imgIds.put(altId.first, altId.second);
        }
    }

    public NPCDataStructure(NPCDataStructure data) {
        this.npcId = data.npcId;
        this.imgIds = data.imgIds;
        this.txtId = data.txtId;
        this.shopId = data.shopId;
        this.textBoxImgId = data.textBoxImgId;
    }

    /*--------------------------------------------------------
    Getters and Setters
    --------------------------------------------------------*/

    public int getNpcId() { return npcId; }
    public void setNpcId(int npcId) { this.npcId = npcId; }

    int getImgId() { return imgIds.get("default"); }

    int getTxtId() { return txtId; }

    int getShopId() { return shopId; }

    int getTextBoxImgId() { return textBoxImgId; }

    HashMap<String, Integer> getImgIds() { return imgIds; }

}

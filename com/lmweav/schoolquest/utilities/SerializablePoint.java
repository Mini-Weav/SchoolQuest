package com.lmweav.schoolquest.utilities;

import java.io.Serializable;

/*
 * School Quest: SerializablePoint
 * This class is a serializable implementation of a 2D point of integers.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class SerializablePoint implements Serializable {

    private static final long serialVersionUID = 1L;

    public int x;
    public int y;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/
    public SerializablePoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

}

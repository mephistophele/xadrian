/*
 * $Id: FactorySize.java 740 2009-02-28 10:39:18Z k $
 * Copyright (C) 2009 Klaus Reimer <k@ailis.de>
 * See LICENSE.txt file for licensing information.
 */

package de.ailis.xadrian.data;


/**
 * Factory sizes
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision: 740 $
 */

public enum Suns
{
    /** 0% */
    P0("0 %", 0, 76),

    /** 100% */
    P100("100 %", 100, 118),

    /** 150% */
    P150("150 %", 150, 106),

    /** 200% */
    P200("200 %", 200, 98),

    /** 300% or more */
    P300(">= 300 %", 300, 90);

    /** The suns caption text */
    private String text;

    /** The power in percent */
    private int percent;

    /** The cycle */
    private int cycle;


    /**
     * Constructor
     * 
     * @param text
     *            The caption text
     * @param percent
     *            The power in percent
     * @param cycle
     *            The cycle
     */

    private Suns(final String text, final int percent, final int cycle)
    {
        this.text = text;
        this.percent = percent;
        this.cycle = cycle;
    }


    /**
     * Returns the index.
     * 
     * @return The index
     */

    @Override
    public String toString()
    {
        return this.text;
    }


    /**
     * Returns the suns with the specified percent value. If not found then 0 is
     * returned.
     * 
     * @param percent
     *            The percent value
     * @return The suns
     */

    public static Suns valueOf(final int percent)
    {
        for (final Suns suns : Suns.values())
        {
            if (suns.getPercent() == percent) return suns;
        }
        
        // If sun power is higher then 300 then return this sun power
        if (percent > 300) return P300;
                        
        // No match
        return null;
    }


    /**
     * Returns the sun power in percent.
     * 
     * @return The sun power in percent
     */

    public int getPercent()
    {
        return this.percent;
    }


    /**
     * Returns the cycle.
     * 
     * @return The cycle
     */

    public int getCycle()
    {
        return this.cycle;
    }
}
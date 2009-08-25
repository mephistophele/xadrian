/*
 * $Id: ClipboardProvider.java 788 2009-03-08 14:14:51Z k $
 * Copyright (C) 2009 Klaus Reimer <k@ailis.de>
 * See LICENSE.txt file for licensing information.
 */

package de.ailis.xadrian.interfaces;

import de.ailis.xadrian.data.Sector;


/**
 * This interface is implemented by all components which contain a sector the
 * user can change.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision: 788 $
 */

public interface SectorProvider
{
    /**
     * Sets the sector.
     * 
     * @param sector
     *            The sector to set
     */

    public void setSector(Sector sector);


    /**
     * Returns the sector.
     * 
     * @return Return the sector
     */

    public Sector getSector();


    /**
     * Checks if the sector can be changed.
     * 
     * @return True if sector can be changed, false if not
     */

    public boolean canChangeSector();
}
/*
 * Copyright (C) 2010-2012 Klaus Reimer <k@ailis.de>
 * See LICENSE.TXT for licensing information.
 */
package de.ailis.xadrian.data;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import de.ailis.xadrian.data.factories.GameFactory;
import de.ailis.xadrian.data.factories.SectorFactory;

/**
 * Tests Sector class.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
public class SectorTest
{
    /** The sector factory */
    private static SectorFactory sectorFactory;

    /**
     * Initializes the test
     */

    @BeforeClass
    public static void init()
    {
        Game game = GameFactory.getInstance().getGame("x3tc");
        sectorFactory = game.getSectorFactory();
        System.setProperty("xadrian.config", "false");
    }

    /**
     * Checks the getDistance() method.
     */
    @Test
    public void testGetDistance()
    {
        // Check distance between Kingdom End and Atreus' Clouds
        assertEquals(5, sectorFactory.getSector(0, 0).getDistance(
            sectorFactory.getSector(3, 2)));

        // Check distance between Friar's Retreat and Menelaus' Frontier
        assertEquals(16, sectorFactory.getSector(9, 14).getDistance(
            sectorFactory.getSector(3, 0)));

        // Check distance between Friar's Retreat and a Khaak sector 931 (Must
        // fail with -1 because Khaak sector 931 can't be reached without a jump
        // drive)
        assertEquals(-1, sectorFactory.getSector(9, 14).getDistance(
            sectorFactory.getSector(16, 11)));
    }
    
    @Test
    public void testGetAsteroids(){
        assertEquals(4, sectorFactory.getSector(0,0).getAsteroids().length);
        assertEquals(0, sectorFactory.getSector(11, 7).getAsteroids().length);
    }
    
    @Test
    public void testGetIceAsteroids(){
        Asteroid[] asteroids = sectorFactory.getSector(14, 6).getIceAsteroids();
        assertEquals(1, asteroids.length);
        assertEquals(17, asteroids[0].getYield());
    }
}

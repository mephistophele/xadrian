/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ailis.xadrian.models;

import de.ailis.xadrian.data.factories.GameFactory;
import de.ailis.xadrian.data.factories.SectorFactory;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author david
 */
public class AsteroidSelectionModelTest {
    
    @Test
    public void testSetSector(){
        AsteroidSelectionModel model = new AsteroidSelectionModel();
        SectorFactory factory = new SectorFactory(GameFactory.getInstance().getDefaultGame());
        assertNull(model.getSector());
        model.setSector(factory.getSector(0, 0));
        assertNotNull(model.getSector());
    }
    
}

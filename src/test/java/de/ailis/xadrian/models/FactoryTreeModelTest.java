/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ailis.xadrian.models;

import de.ailis.xadrian.data.factories.GameFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author david
 */
public class FactoryTreeModelTest {
    
    @Test
    public void testGetChildCount(){
        FactoryTreeModel factory = new FactoryTreeModel(GameFactory.getInstance().getDefaultGame());
        assertEquals(3, factory.getChildCount(factory.getRoot()));
    }
    
}

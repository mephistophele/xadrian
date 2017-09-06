/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ailis.xadrian.data;

import de.ailis.xadrian.data.factories.FactoryFactory;
import de.ailis.xadrian.data.factories.GameFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author david
 */
public class ShoppingListTest {
    
    @Test
    public void testAddItem(){
        ShoppingList list = new ShoppingList(null, 1);
        Factory factory = new FactoryFactory(GameFactory.getInstance().getDefaultGame()).getFactory(1);
        list.addItem(new ShoppingListItem(factory, 3, null, 0));
        assertEquals(5, list.getTotalQuantity());
        assertEquals(2, list.getKitQuantity());
        assertEquals(1, list.getKitQuantityBuilt());
        assertEquals(1, list.getKitQuantityLeft());
        assertEquals(1, list.getItems().size());
        list.addItem(new ShoppingListItem(factory, 2, null, 5));
        assertEquals(9, list.getTotalQuantity());
        assertEquals(4, list.getKitQuantity());
        assertEquals(3, list.getKitQuantityLeft());
        assertEquals(6, list.getTotalQuantityBuilt());
        assertEquals(3, list.getTotalQuantityLeft());
        assertEquals(1, list.getItems().size());
    }
    
}

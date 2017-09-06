/*
 * Copyright (C) 2010-2012 Klaus Reimer <k@ailis.de>
 * See LICENSE.TXT for licensing information.
 */
package de.ailis.xadrian.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The shopping list.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
public class ShoppingList implements Serializable
{
    /** Serial version UID */
    private static final long serialVersionUID = -2174714813369451947L;

    /** The shopping list items */
    private final List<ShoppingListItem> items = new ArrayList<>();

    /** The number of built kits */
    private int kitQuantityBuilt = 0;

    /** The sector with the nearest shipyard */
    private final Sector nearestKitSellingSector;

    /**
     * Constructor.
     *
     * @param nearestKitSellingSector
     *            The nearest sector where the player can buy complex kits.
     *            Can be null if not known.
     * @param kitsBuilt
     *            The number of built kits
     */
    public ShoppingList(final Sector nearestKitSellingSector, final int kitsBuilt)
    {
        this.nearestKitSellingSector = nearestKitSellingSector;
        this.kitQuantityBuilt = kitsBuilt;
    }

    /**
     * Adds a new shopping list item to the list.
     *
     * @param item
     *            The shopping list item to add
     */
    public void addItem(final ShoppingListItem item)
    {
        int i, max;

        // Try to update an existing list item first
        for (i = 0, max = this.items.size(); i < max; i++)
        {
            final ShoppingListItem oldItem = this.items.get(i);
            if (oldItem.getFactory().equals(item.getFactory()))
            {
                this.items.set(i, new ShoppingListItem(item.getFactory(), item
                        .getQuantity()
                        + oldItem.getQuantity(), item.getNearestManufacturer(),
                        item.getQuantityBuilt()));
                return;
            }
        }

        // Add the new item
        this.items.add(item);
        Collections.sort(this.items);
    }

    /**
     * Returns the shopping list items.
     *
     * @return The shopping list items
     */
    public List<ShoppingListItem> getItems()
    {
        return Collections.unmodifiableList(this.items);
    }

    /**
     * Returns the total volume.
     *
     * @return The total volume
     */
    public long getTotalVolume()
    {
        return this.items.stream().mapToLong(item -> item.getTotalVolume()).sum() + getTotalKitVolume();
    }
    
    /**
     * Returns the total rest volume.
     * 
     * @return The total rest volume.
     */
    public long getTotalRestVolume()
    {
        long volume = getRestKitVolume();
        volume = this.items.stream().map((item) -> item.getRestVolume()).reduce(volume, (accumulator, _item) -> accumulator + _item);
        return volume;
    }

    public int getFactoryTotalQuantity(){
        return this.items.stream().mapToInt(item -> item.getQuantity()).sum();
    }
    
    /**
     * Returns the total quantity.
     *
     * @return The total quantity
     */
    public int getTotalQuantity()
    {
        return getFactoryTotalQuantity() + getKitQuantity();
    }

    /**
     * Returns the total quantity of built factories and kits.
     *
     * @return The total quantity
     */
    public int getTotalQuantityBuilt()
    {
        return this.items.stream().mapToInt(item -> item.getQuantityBuilt()).sum() + getKitQuantityBuilt();
    }

    /**
     * Returns the total quantity of built factories and kits left.
     *
     * @return The total quantity left
     */
    public int getTotalQuantityLeft()
    {
        return getTotalQuantity() - getTotalQuantityBuilt();
    }

    /**
     * Returns the total price.
     *
     * @return The total price
     */
    public long getTotalPrice()
    {
        return this.items.stream().mapToLong(item -> item.getTotalPrice()).sum() + getTotalKitPrice();
    }

    /**
     * Returns the total rest price.
     * 
     * @return The total rest price.
     */
    public long getTotalRestPrice()
    {
        long price = getRestKitPrice();
        price = this.items.stream().map((item) -> item.getRestPrice()).reduce(price, (accumulator, _item) -> accumulator + _item);
        return price;
    }
    /**
     * Returns the kit quantity.
     *
     * @return The kit quantity
     */
    public int getKitQuantity()
    {
        return Math.max(0, this.getFactoryTotalQuantity() - 1);
    }

    /**
     * Returns the number of built kits.
     *
     * @return The number built kits
     */
    public int getKitQuantityBuilt()
    {
        return this.kitQuantityBuilt;
    }

    /**
     * Returns the number of kits left.
     *
     * @return The number kits left
     */
    public int getKitQuantityLeft()
    {
        return getKitQuantity() - getKitQuantityBuilt();
    }

    /**
     * Returns the single kit price
     *
     * @return The single kit price
     */
    public int getKitPrice()
    {
        return Complex.KIT_PRICE;
    }

    /**
     * Returns the total kit price.
     *
     * @return The total kit price
     */
    public long getTotalKitPrice()
    {
        return ((long) getKitPrice()) * getKitQuantity();
    }

    /**
     * Returns the nearest shipyard where the player can buy complex
     * construction kits. Can be null if no shipyard is available.
     *
     * @return The nearest shipyard or null if none
     */
    public Sector getNearestKitSellingSector()
    {
        return this.nearestKitSellingSector;
    }

    /**
     * Returns the kit volume.
     *
     * @return The kit volume
     */
    public int getKitVolume()
    {
        return Complex.KIT_VOLUME;
    }

    /**
     * Returns the total kit volume.
     *
     * @return The total kit volume
     */
    public long getTotalKitVolume()
    {
        return ((long) getKitVolume()) * getKitQuantity();
    }
    
    /**
     * Returns the rest volume of all kits which are not already built.
     * 
     * @return The rest kit volume.
     */
    public long getRestKitVolume()
    {
        return ((long) getKitVolume()) * getKitQuantityLeft();
    }

    /**
     * Returns the rest price of all kits which are not already built.
     * 
     * @return The rest kit price.
     */
    public long getRestKitPrice()
    {
        return ((long) getKitPrice()) * getKitQuantityLeft();
    }
}

/*
 * Copyright (C) 2010-2012 Klaus Reimer <k@ailis.de>
 * See LICENSE.TXT for licensing information.
 */

package de.ailis.xadrian.data;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import de.ailis.xadrian.support.Config;
import de.ailis.xadrian.support.I18N;
import de.ailis.xadrian.support.ReverseIntegerComparator;

/**
 * A sector.
 * 
 * @author Klaus Reimer (k@ailis.de)
 */
public class Sector implements Serializable, Comparable<Sector>
{
    /** Serial version UID */
    private static final long serialVersionUID = -8004624270181949305L;

    /** The game this sector belongs to. */
    protected final Game game;

    /** The sector id */
    protected final String id;

    /** The X position in the universe */
    private final int x;

    /** The Y position in the universe */
    private final int y;

    /** The race this sector belongs to */
    private final Race race;

    /** The number of planets in the sector */
    private final int planets;

    /** The suns */
    private final Sun suns;

    /** If this is a core sector or not */
    private final boolean core;

    /** The id of the sector which can be reached through the north gate */
    private final String northId;

    /** The id of the sector which can be reached through the north gate */
    private final String southId;

    /** The id of the sector which can be reached through the north gate */
    private final String westId;

    /** The id of the sector which can be reached through the north gate */
    private final String eastId;

    /** The message id */
    private final String messageId;

    /** If this sector has a shipyard or not */
    private final boolean shipyard;

    /** Array with asteroids in this sector */
    private final Asteroid[] asteroids;

    /**
     * Constructor
     * 
     * @param game
     *            The game this sector belongs to. Must not be null.
     * @param id
     *            The sector id
     * @param x
     *            The X position in the universe
     * @param y
     *            The Y position in the universe
     * @param race
     *            The race this sector belongs to
     * @param planets
     *            The number of planets in the sector
     * @param suns
     *            The suns
     * @param core
     *            If this is a core sector or not
     * @param shipyard
     *            If this sector has a shipyard or not
     * @param northId
     *            The id of the sector behind the north gate
     * @param eastId
     *            The id of the sector behind the east gate
     * @param southId
     *            The id of the sector behind the south gate
     * @param westId
     *            The id of the sector behind the west gate
     * @param asteroids
     *            Array with asteroids in this sector. May be null for
     *            special sectors which have a dynamic asteroids list (Like
     *            the player sector in X3TC).
     */
    public Sector(final Game game, final String id, final int x, final int y,
        final Race race, final int planets, final Sun suns, final boolean core,
        final boolean shipyard, final String northId, final String eastId,
        final String southId, final String westId, final Asteroid[] asteroids)
    {
        if (game == null)
            throw new IllegalArgumentException("game must be set");
        this.game = game;
        this.id = id;
        this.messageId = "sector." + id;
        this.x = x;
        this.y = y;
        this.race = race;
        this.planets = planets;
        this.shipyard = shipyard;
        this.core = core;
        this.suns = suns;
        this.northId = northId;
        this.southId = southId;
        this.westId = westId;
        this.eastId = eastId;
        this.asteroids = asteroids == null ? null : asteroids.clone();
    }

    /**
     * Return the sector id.
     * 
     * @return The sector id
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * Returns the name.
     * 
     * @return The name
     */
    public String getName()
    {        
        if (this.game.isX3AP() && "sec-12-4".equals(this.id))
        {
            return I18N.getString(this.game, this.messageId + "-" 
                + Config.getInstance().getX3APPlayerSector()); 
        }
        return I18N.getString(this.game, this.messageId);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(this.id).toHashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;
        final Sector other = (Sector) obj;
        return new EqualsBuilder().append(this.id, other.id).isEquals();
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final Sector o)
    {
        return getName().compareTo(o.getName());
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getName();
    }

    /**
     * Returns the x position in the universe.
     * 
     * @return The x position in the universe
     */
    public int getX()
    {
        return this.x;
    }

    /**
     * Returns the y position in the universe.
     * 
     * @return The y position in the universe
     */
    public int getY()
    {
        return this.y;
    }

    /**
     * Returns the race to which this sector belongs.
     * 
     * @return The race to which this sector belongs
     */
    public Race getRace()
    {
        return this.race;
    }

    /**
     * Returns the number of planets.
     * 
     * @return The number of planets
     */
    public int getPlanets()
    {
        return this.planets;
    }

    /**
     * Returns the suns.
     * 
     * @return The suns
     */
    public Sun getSuns()
    {
        return this.suns;
    }

    /**
     * Checks if this sector is a core sector or not.
     * 
     * @return True if core sector, false if not
     */
    public boolean isCore()
    {
        return this.core;
    }

    /**
     * Checks if this sector has a shipyard or not.
     * 
     * @return True if sector has a shipyard, false if not
     */
    public boolean hasShipyard()
    {
        return this.shipyard;
    }

    /**
     * Returns the sector behind the north gate.
     * 
     * @return The sector behind the north gate
     */
    public Sector getNorth()
    {
        return this.game.getSectorFactory().getSector(this.northId);
    }

    /**
     * Returns the sector behind the south gate.
     * 
     * @return The sector behind the south gate
     */
    public Sector getSouth()
    {
        return this.game.getSectorFactory().getSector(this.southId);
    }

    /**
     * Returns the sector behind the west gate.
     * 
     * @return The sector behind the west gate
     */
    public Sector getWest()
    {
        return this.game.getSectorFactory().getSector(this.westId);
    }

    /**
     * Returns the sector behind the east gate.
     * 
     * @return The sector behind the east gate
     */
    public Sector getEast()
    {
        return this.game.getSectorFactory().getSector(this.eastId);
    }

    /**
     * Returns the size of the sector. This size is chosen so all objects in it
     * fit into this sector size. So this size can be used as the width and
     * height and depth to display the sector on the screen.
     * 
     * @return The sector size
     */
    public int getSize()
    {
        int maxX = 0;
        int maxY = 0;
        int maxZ = 0;

        for (final Asteroid asteroid : getAsteroids())
        {
            maxX = Math.max(maxX, Math.abs(asteroid.getX()));
            maxY = Math.max(maxY, Math.abs(asteroid.getY()));
            maxZ = Math.max(maxZ, Math.abs(asteroid.getZ()));
        }

        return Math.max(50000,
            Math.max(Math.max(maxX, maxY), maxZ) * 2 + 10000);
    }

    /**
     * Returns the distance from this sector to the specified sector. If the
     * destination sector is unreachable (because it can't be reached via gates)
     * then -1 is returned.
     * 
     * @param dest
     *            The destination sector
     * @return The distance. -1 if destination sector is unreachable
     */
    public int getDistance(final Sector dest)
    {
        final Set<Sector> seen = new HashSet<Sector>();
        Set<Sector> todo = new HashSet<Sector>();

        // We begin with the current sector and a distance of 0
        todo.add(this);
        int distance = 0;

        // This loop is repeated until no more TODOs are present. If the
        // target sector is reached then the loop is exited with a return
        while (todo.size() > 0)
        {
            final Set<Sector> newTodo = new HashSet<Sector>();
            for (final Sector sector : todo)
            {
                // When we have reached our destination sector then return
                // the distance traveled so far
                if (dest.equals(sector)) return distance;

                // Mark this sector as already seen
                seen.add(sector);

                // Add the sectors which can be reached from the current sector
                // to the new TODOs list if they are not already seen
                Sector tmp = sector.getNorth();
                if (tmp != null && !seen.contains(tmp)) newTodo.add(tmp);
                tmp = sector.getEast();
                if (tmp != null && !seen.contains(tmp)) newTodo.add(tmp);
                tmp = sector.getWest();
                if (tmp != null && !seen.contains(tmp)) newTodo.add(tmp);
                tmp = sector.getSouth();
                if (tmp != null && !seen.contains(tmp)) newTodo.add(tmp);
            }

            // In the next run process our new TODOs list
            todo = newTodo;

            // Increment the distance
            distance++;
        }

        // No more TODOs and target sector was not reached
        return -1;
    }

    /**
     * Returns the nearest sector with a shipyard which sells complex
     * construction kits. This method honors the ignored races the player
     * has configured. If no shipyard was found then null is returned.
     * 
     * @return The nearest sector with a shipyard selling complex construction
     *         kits or null if none.
     */
    public Sector getNearestKitSellingSector()
    {
        final Config config = Config.getInstance();
        final Set<Sector> seen = new HashSet<Sector>();
        Set<Sector> todo = new HashSet<Sector>();

        // We begin with the current sector
        todo.add(this);

        // This loop is repeated until no more TODOs are present. If the
        // target sector is reached then the loop is exited with a return
        while (todo.size() > 0)
        {
            final Set<Sector> newTodo = new HashSet<Sector>();
            for (final Sector sector : todo)
            {
                // When we have reached a sector with a shipyard and the player
                // can buy complex construction kits from it then return it.
                if (sector.hasShipyard() 
                    && !config.isRaceIgnored(sector.getRace())
                    && !sector.getRace().getId().equals("xenon")                   
                    && (!sector.getRace().getId().equals("terran") 
                        || !this.game.isX3TC())) return sector;

                // Mark this sector as already seen
                seen.add(sector);

                // Add the sectors which can be reached from the current sector
                // to the new TODOs list if they are not already seen
                Sector tmp = sector.getNorth();
                if (tmp != null && !seen.contains(tmp)) newTodo.add(tmp);
                tmp = sector.getEast();
                if (tmp != null && !seen.contains(tmp)) newTodo.add(tmp);
                tmp = sector.getWest();
                if (tmp != null && !seen.contains(tmp)) newTodo.add(tmp);
                tmp = sector.getSouth();
                if (tmp != null && !seen.contains(tmp)) newTodo.add(tmp);
            }

            // In the next run process our new TODOs list
            todo = newTodo;
        }

        // No more TODOs and no shipyard was found
        return null;
    }

    /**
     * Returns the array with asteroids.
     * 
     * @return The array with asteroids.
     */
    public Asteroid[] getAsteroids()
    {
        return this.asteroids.clone();
    }

    /**
     * Returns the silicon asteroids of this sector.
     * 
     * @return The silicon asteroids
     */
    public Asteroid[] getSiliconAsteroids()
    {
        final SortedSet<Asteroid> localAsteroids = new TreeSet<Asteroid>();
        for (final Asteroid asteroid : getAsteroids())
        {
            if (asteroid.getWare().isSiliconWafers())
            {
                localAsteroids.add(asteroid);
            }
        }
        return localAsteroids.toArray(new Asteroid[localAsteroids.size()]);
    }

    /**
     * Returns the ore asteroids of this sector.
     * 
     * @return The ore asteroids
     */
    public Asteroid[] getOreAsteroids()
    {
        final SortedSet<Asteroid> localAsteroids = new TreeSet<Asteroid>();
        for (final Asteroid asteroid : getAsteroids())
        {
            if (asteroid.getWare().isOre())
            {
                localAsteroids.add(asteroid);
            }
        }
        return localAsteroids.toArray(new Asteroid[localAsteroids.size()]);
    }

    /**
     * Returns the ice asteroids of this sector.
     * 
     * @return The ice asteroids
     */
    public Asteroid[] getIceAsteroids()
    {
        final SortedSet<Asteroid> localAsteroids = new TreeSet<Asteroid>();
        for (final Asteroid asteroid : getAsteroids())
        {
            if (asteroid.getWare().isIce())
            {
                localAsteroids.add(asteroid);
            }
        }
        return localAsteroids.toArray(new Asteroid[localAsteroids.size()]);
    }

    /**
     * Returns the nividium asteroids of this sector.
     * 
     * @return The nividium asteroids
     */
    public Asteroid[] getNividiumAsteroids()
    {
        final SortedSet<Asteroid> localAsteroids = new TreeSet<Asteroid>();
        for (final Asteroid asteroid : getAsteroids())
        {
            if (asteroid.getWare().isNividium())
            {
                localAsteroids.add(asteroid);
            }
        }
        return localAsteroids.toArray(new Asteroid[localAsteroids.size()]);
    }

    /**
     * Returns the total silicon yield of this sector.
     * 
     * @return The total silicon yield
     */
    public int getTotalSiliconYield()
    {
        int yield = 0;
        for (final Asteroid asteroid : getAsteroids())
        {
            if (asteroid.getWare().isSiliconWafers())
            {
                yield += asteroid.getYield();
            }
        }
        return yield;
    }

    /**
     * Returns the total ore yield of this sector.
     * 
     * @return The total ore yield
     */
    public int getTotalOreYield()
    {
        int yield = 0;
        for (final Asteroid asteroid : getAsteroids())
        {
            if (asteroid.getWare().isOre())
            {
                yield += asteroid.getYield();
            }
        }
        return yield;
    }

    /**
     * Returns the total nividium yield of this sector.
     * 
     * @return The total nividium yield
     */
    public int getTotalNividiumYield()
    {
        int yield = 0;
        for (final Asteroid asteroid : getAsteroids())
        {
            if (asteroid.getWare().isNividium())
            {
                yield += asteroid.getYield();
            }
        }
        return yield;
    }

    /**
     * Returns the total ice yield of this sector.
     * 
     * @return The total ice yield
     */
    public int getTotalIceYield()
    {
        int yield = 0;
        for (final Asteroid asteroid : getAsteroids())
        {
            if (asteroid.getWare().isIce())
            {
                yield += asteroid.getYield();
            }
        }
        return yield;
    }

    /**
     * Returns the silicon color of this sector. The brighter the more silicon
     * is available.
     * 
     * @return The silicon color
     */
    public Color getSiliconColor()
    {
        final int max = this.game.getSectorFactory().getMaxSiliconYield();
        final int cur = getTotalSiliconYield();
        if (cur == 0) return Color.BLACK;
        final int intensity = Math.min(200, 200 * cur / max) + 55;
        return new Color(0, intensity, intensity);
    }

    /**
     * Returns the ore color of this sector. The brighter the more ore is
     * available.
     * 
     * @return The ore color
     */
    public Color getOreColor()
    {
        final int max = this.game.getSectorFactory().getMaxOreYield();
        final int cur = getTotalOreYield();
        if (cur == 0) return Color.BLACK;
        final int intensity = Math.min(200, 200 * cur / max) + 55;
        return new Color(0, intensity, intensity);
    }

    /**
     * Returns the nividium color of this sector. The brighter the more ore is
     * available.
     * 
     * @return The nividium color
     */
    public Color getNividiumColor()
    {
        final int max = this.game.getSectorFactory().getMaxNividiumYield();
        final int cur = getTotalNividiumYield();
        if (cur == 0) return Color.BLACK;
        final int intensity = Math.min(200, 200 * cur / max) + 55;
        return new Color(0, intensity, intensity);
    }

    /**
     * Returns the ice color of this sector. The brighter the more ore is
     * available.
     * 
     * @return The ice color
     */
    public Color getIceColor()
    {
        final int max = this.game.getSectorFactory().getMaxIceYield();
        final int cur = getTotalIceYield();
        if (cur == 0) return Color.BLACK;
        final int intensity = Math.min(200, 200 * cur / max) + 55;
        return new Color(0, intensity, intensity);
    }

    /**
     * Returns the yield map. This map has the yield as key and the number of
     * asteroids with this yield as value.
     * 
     * @param wareId
     *            The id of the asteroid ware to search for
     * @return The yield map
     */
    public SortedMap<Integer, Integer> getYieldsMap(final String wareId)
    {
        final SortedMap<Integer, Integer> yields =
            new TreeMap<Integer, Integer>(new ReverseIntegerComparator());

        // Iterate over all asteroids
        for (final Asteroid asteroid : getAsteroids())
        {
            // If this asteroid is not of the searched type then ignore it
            if (!wareId.equals(asteroid.getWare().getId())) continue;

            // Update the yield in the yields map
            final int yield = asteroid.getYield();
            Integer oldQuantity = yields.get(yield);
            if (oldQuantity == null) oldQuantity = 0;
            yields.put(yield, oldQuantity + 1);
        }

        // Return the yields map
        return yields;
    }

    /**
     * Returns a list with the yields of the specified asteroid ware.
     * 
     * @param wareId
     *            The id of the asteroid ware
     * @return The list with the yields. Can be empty. Never null.
     */
    public List<Integer> getYields(final String wareId)
    {
        final List<Integer> yields = new ArrayList<Integer>();

        // Iterate over all asteroids
        for (final Asteroid asteroid : getAsteroids())
        {
            // If this asteroid is not of the searched type then ignore it
            if (!wareId.equals(asteroid.getWare().getId())) continue;

            yields.add(asteroid.getYield());
        }
        Collections.sort(yields, new ReverseIntegerComparator());
        return yields;
    }

    /**
     * Checks if the specified asteroid is in the sector.
     * 
     * @param asteroid
     *            The asteroid to check
     * @return True if asteroid is in sector, false if not
     */
    public boolean hasAsteroid(final Asteroid asteroid)
    {
        for (final Asteroid sectorAsteroid : getAsteroids())
            if (sectorAsteroid.equals(asteroid)) return true;
        return false;
    }

    /**
     * Returns the game this sector belongs to.
     * 
     * @return The game. Never null.
     */
    public Game getGame()
    {
        return this.game;
    }
}

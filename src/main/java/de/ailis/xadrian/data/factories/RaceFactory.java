/*
 * Copyright (C) 2010-2012 Klaus Reimer <k@ailis.de>
 * See LICENSE.txt file for licensing information.
 */
package de.ailis.xadrian.data.factories;

import java.awt.Color;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import de.ailis.xadrian.Main;
import de.ailis.xadrian.data.Game;
import de.ailis.xadrian.data.Race;
import de.ailis.xadrian.exceptions.DataException;
import de.ailis.xadrian.frames.SplashFrame;

/**
 * Factory for Race objects.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
public class RaceFactory implements Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The game for which this factory is responsible. */
    private final Game game;

    /** The race map (for quick ID navigation) */
    private final Map<String, Race> raceMap = new HashMap<>();

    /** The races (sorted) */
    private final SortedSet<Race> races = new TreeSet<>();

    /**
     * Constructor.
     *
     * @param game
     *            The game for which this factory is responsible.
     */
    public RaceFactory(final Game game)
    {
        this.game = game;
        readData();
        SplashFrame.advanceProgress();
    }

    /**
     * Reads the data from the XML file.
     */
    private void readData()
    {
        String gameId = this.game.getId();
        URL url = Main.class.getResource("/" + gameId + "/races.xml");
        if (url == null)
            url = Main.class.getResource("data/" + gameId + "/races.xml");
        final SAXReader reader = new SAXReader();
        try
        {
            final Document document = reader.read(url);
            for (final Object item : document.getRootElement().elements("race"))
            {
                final Element element = (Element) item;
                final String id = element.attributeValue("id");
                final String colorStr = element.attributeValue("color");
                final Color color = new Color(Integer.parseInt(colorStr
                    .substring(1), 16));
                final Race race = new Race(this.game, id, color);
                this.races.add(race);
                this.raceMap.put(id, race);
            }
        }
        catch (final DocumentException e)
        {
            throw new DataException("Unable to read XML file: " + e, e);
        }
    }

    /**
     * Returns all races.
     *
     * @return The races
     */
    public SortedSet<Race> getRaces()
    {
        return Collections.unmodifiableSortedSet(this.races);
    }

    /**
     * Returns all races which have buyable factories.
     *
     * @return The races which habe buyable factories.
     */
    public Collection<Race> getManufacturerRaces()
    {
        final Collection<Race> localRaces = new ArrayList<>();
        final FactoryFactory factory = this.game.getFactoryFactory();
        this.races.stream().filter((race) -> (factory.getFactories(race).size() > 0)).forEach((race) -> {
            localRaces.add(race);
        });
        return localRaces;
    }

    /**
     * Returns the race with the specified id or null if not found.
     *
     * @param id
     *            The race id
     * @return The race or null if not found
     */
    public Race getRace(final String id)
    {
        return this.raceMap.get(id);
    }

    /**
     * Returns the number of races.
     *
     * @return The number of races
     */
    public int countRaces()
    {
        return this.races.size();
    }
}

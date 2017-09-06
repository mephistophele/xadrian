/*
 * Copyright (C) 2010-2012 Klaus Reimer <k@ailis.de>
 * See LICENSE.TXT for licensing information.
 */

package de.ailis.xadrian.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.xadrian.data.factories.FactoryFactory;
import de.ailis.xadrian.data.factories.GameFactory;
import de.ailis.xadrian.data.factories.RaceFactory;
import de.ailis.xadrian.data.factories.SectorFactory;
import de.ailis.xadrian.data.factories.SunFactory;
import de.ailis.xadrian.data.factories.WareFactory;
import de.ailis.xadrian.dialogs.SetYieldsDialog;
import de.ailis.xadrian.exceptions.DataException;
import de.ailis.xadrian.exceptions.GameNotFoundException;
import de.ailis.xadrian.exceptions.TemplateCodeException;
import de.ailis.xadrian.interfaces.GameProvider;
import de.ailis.xadrian.support.Config;
import de.ailis.xadrian.support.DynaByteInputStream;
import de.ailis.xadrian.support.DynaByteOutputStream;
import de.ailis.xadrian.support.I18N;
import de.ailis.xadrian.support.ModalDialog.Result;
import de.ailis.xadrian.support.MultiCollection;

/**
 * A complex
 *
 * @author Klaus Reimer (k@ailis.de)
 */
public class Complex implements Serializable, GameProvider
{
    /** Serial version UID */
    private static final long serialVersionUID = 2128684141345704703L;

    /** The logger */
    private static final Log log = LogFactory.getLog(Complex.class);

    /** The single price of a complex construction kit */
    public static final int KIT_PRICE = 259696;

    /** The volume of a complex construction kit */
    public static final int KIT_VOLUME = 4250;

    /** The complex counter for the complex name generator */
    private static int complexCounter = 0;

    /** The game this complex belongs to. */
    private final Game game;

    /** The complex name */
    private String name;

    /** The factories in this complex */
    private final List<ComplexFactory> factories;

    /** Automatically added factories in this complex */
    private final List<ComplexFactory> autoFactories;

    /** The sun power in percent */
    private Sun suns;

    /** The sector where this complex is build */
    private Sector sector = null;

    /** If base complex should be calculated or not */
    private boolean addBaseComplex = false;

    /** Custom buy/sell prices in this complex */
    private final Map<Ware, Integer> customPrices;

    /** If complex setup should be displayed */
    private boolean showingComplexSetup = true;

    /** If production statistics should be displayed */
    private boolean showingProductionStats = false;

    /** If storage capacities should be displayed */
    private boolean showingStorageCapacities = false;

    /** If shopping list should be displayed */
    private boolean showingShoppingList = false;

    /** The built factories */
    private final Map<String, Integer> builtFactories;

    /** The number of built kits */
    private int builtKits;

    /** The cached shopping list */
    private ShoppingList shoppingList;

    /**
     * Constructor
     *
     * @param game
     *            The game this complex belongs to.
     */
    public Complex(final Game game)
    {
        this(game, createComplexName());
    }

    /**
     * Constructor
     *
     * @param game
     *            The game this complex belongs to.
     * @param name
     *            The complex name
     */
    public Complex(final Game game, final String name)
    {
        this.game = game;
        this.suns = game.getSunFactory().getDefaultSun();
        this.name = name;
        this.factories = new ArrayList<>();
        this.autoFactories = new ArrayList<>();
        this.customPrices = new HashMap<>();
        this.builtFactories = new HashMap<>();
    }

    /**
     * Returns a new name for a complex.
     *
     * @return A new complex name
     */
    private static String createComplexName()
    {
        complexCounter++;
        return I18N.getString("complex.nameTemplate", complexCounter);
    }

    /**
     * Returns the name.
     *
     * @return The name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the complex name.
     *
     * @param name
     *            The complex name to set
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * Returns the total number of factories in the complex
     *
     * @return The total number of factories in the complex
     */
    public int getTotalQuantity()
    {
        int quantity = 0;
        quantity = this.factories.stream().map((factory) -> factory.getQuantity()).reduce(quantity, Integer::sum);
        quantity = this.autoFactories.stream().map((factory) -> factory.getQuantity()).reduce(quantity, Integer::sum);
        return quantity;
    }

    /**
     * Returns the total complex price.
     *
     * @return The total complex price
     */
    public long getTotalPrice()
    {
        long price = 0;
        price = this.factories.stream().map((complexFactory) -> ((long) complexFactory.getQuantity())
                * complexFactory.getFactory().getPrice()).reduce(price, (accumulator, _item) -> accumulator + _item);
        price = this.autoFactories.stream().map((complexFactory) -> ((long) complexFactory.getQuantity())
                * complexFactory.getFactory().getPrice()).reduce(price, (accumulator, _item) -> accumulator + _item);
        return price + getTotalKitPrice();
    }

    /**
     * A immutable copy of the factories in this complex.
     *
     * @return The factories in this complex
     */
    public List<ComplexFactory> getFactories()
    {
        return Collections.unmodifiableList(this.factories);
    }

    /**
     * A immutable copy of the automatically added factories in this complex.
     *
     * @return The automatically added factories in this complex
     */
    public List<ComplexFactory> getAutoFactories()
    {
        return Collections.unmodifiableList(this.autoFactories);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(this.name).append(this.factories)
            .append(this.autoFactories).hashCode();
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
        final Complex other = (Complex) obj;
        return new EqualsBuilder().append(this.name, other.name).append(
            this.factories, other.factories).append(this.suns, other.suns)
            .append(this.sector, other.sector).isEquals();
    }

    /**
     * Removes the factory with the given index.
     *
     * @param index
     *            The factory index
     */
    public void removeFactory(final int index)
    {
        this.factories.remove(index);
        calculateBaseComplex();
        updateShoppingList();
    }

    /**
     * Disables the factory with the given index.
     *
     * @param index
     *            The factory index
     */
    public void disableFactory(final int index)
    {
        this.factories.get(index).disable();
        calculateBaseComplex();
    }

    /**
     * Disables the factory with the given index.
     *
     * @param index
     *            The factory index
     */
    public void enableFactory(final int index)
    {
        this.factories.get(index).enable();
        calculateBaseComplex();
    }

    /**
     * Accepts the automatically added factory with the given index.
     *
     * @param index
     *            The factory index
     */
    public void acceptFactory(final int index)
    {
        addFactory(this.autoFactories.get(index));
        calculateBaseComplex();
    }

    /**
     * Returns the quantity of the factory with the given index.
     *
     * @param index
     *            The factory index
     * @return The quantity
     */
    public int getQuantity(final int index)
    {
        return this.factories.get(index).getQuantity();
    }

    /**
     * Increases the quantity of the factory with the given index.
     *
     * @param index
     *            The factory index
     * @return True if quantity was changed, false if not.
     */
    public boolean increaseQuantity(final int index)
    {
        if (this.factories.get(index).increaseQuantity())
        {
            calculateBaseComplex();
            updateShoppingList();
            return true;
        }
        return false;
    }

    /**
     * Decreases the quantity of the factory with the given index.
     *
     * @param index
     *            The factory index
     * @return True if quantity was changed, false if not.
     */
    public boolean decreaseQuantity(final int index)
    {
        if (this.factories.get(index).decreaseQuantity())
        {
            calculateBaseComplex();
            updateShoppingList();
            return true;
        }
        return false;
    }

    /**
     * Sets the quantity of the factory with the given index to the specified
     * quantity.
     *
     * @param index
     *            The factory index
     * @param quantity
     *            The quantity to set
     */
    public void setQuantity(final int index, final int quantity)
    {
        final ComplexFactory factory = this.factories.get(index);
        if (factory.getQuantity() != quantity)
        {
            factory.setQuantity(quantity);
            calculateBaseComplex();
            updateShoppingList();
        }
    }

    /**
     * Returns the yield of the factory with the given index.
     *
     * @param index
     *            The factory index
     * @return The yield
     */
    public List<Integer> getYields(final int index)
    {
        return this.factories.get(index).getYields();
    }

    /**
     * Sets the yields of the factory with the given index.
     *
     * @param index
     *            The factory index
     * @param yields
     *            The yields to set
     */
    public void setYields(final int index, final List<Integer> yields)
    {
        final ComplexFactory factory = this.factories.get(index);
        factory.setYields(yields);
        calculateBaseComplex();
        updateShoppingList();
    }

    /**
     * Returns the factory type of factory with the given index.
     *
     * @param index
     *            The factory index
     * @return The factory
     */
    public Factory getFactory(final int index)
    {
        return this.factories.get(index).getFactory();
    }

    /**
     * Sets the suns in percent.
     *
     * @param suns
     *            The suns in percent to set
     */
    public void setSuns(final Sun suns)
    {
        this.suns = suns;
        calculateBaseComplex();
    }

    /**
     * Returns the suns in percent.
     *
     * @return The suns in percent
     */
    public Sun getSuns()
    {
        if (this.sector != null) return this.sector.getSuns();
        return this.suns;
    }

    /**
     * Adds a factory to the complex.
     *
     * @param factory
     *            The factory to add
     */
    public void addFactory(final Factory factory)
    {
        if (factory.isMine())
        {
            final SetYieldsDialog dialog = new SetYieldsDialog(factory);
            dialog.setYields(null);
            dialog.setSector(getSector());
            if (dialog.open() == Result.OK)
            {
                setSector(dialog.getSector());
                addFactory(new ComplexFactory(this.game, factory,
                    dialog.getYields()));
                calculateBaseComplex();
                updateShoppingList();
            }
        }
        else
        {
            addFactory(new ComplexFactory(this.game, factory, 1, 0));
            calculateBaseComplex();
            updateShoppingList();
        }
    }

    /**
     * Adds the specified factory/factories to the complex.
     *
     * @param complexFactory
     *            The factory/factories to add
     */
    private void addFactory(final ComplexFactory complexFactory)
    {
        if (!complexFactory.getFactory().isMine())
        {
            for (final ComplexFactory current: this.factories)
            {
                if (current.getFactory().equals(complexFactory.getFactory())
                    && current.getYield() == complexFactory.getYield())
                {
                    current.addQuantity(complexFactory.getQuantity());
                    return;
                }
            }
        }
        this.factories.add(complexFactory);
        Collections.sort(this.factories);
        updateShoppingList();
    }

    /**
     * Converts the complex into XML and returns it.
     *
     * @return The complex as XML
     */
    public Document toXML()
    {
        final Document document = DocumentHelper.createDocument();
        final Element root = document.addElement("complex");
        root.addAttribute("version", "4");
        root.addAttribute("game", this.game.getId());
        root.addAttribute("suns", Integer.toString(getSuns().getPercent()));
        if (this.sector != null)
            root.addAttribute("sector", this.sector.getId());
        root.addAttribute("addBaseComplex", Boolean
            .toString(this.addBaseComplex));
        root.addAttribute("showingProductionStats", Boolean
            .toString(this.showingProductionStats));
        root.addAttribute("showingShoppingList", Boolean
            .toString(this.showingShoppingList));
        root.addAttribute("showingStorageCapacities", Boolean
            .toString(this.showingStorageCapacities));
        root.addAttribute("showingComplexSetup", Boolean
            .toString(this.showingComplexSetup));
        if (!this.factories.isEmpty())
        {
            final Element factoriesE = root.addElement("complexFactories");
            this.factories.stream().forEach((factory) -> {
                final Element factoryE = factoriesE
                        .addElement("complexFactory");
                factoryE.addAttribute("factory", factory.getFactory().getId());
                factoryE.addAttribute("disabled", Boolean.toString(factory
                        .isDisabled()));
                if (factory.getFactory().isMine())
                {
                    final Element yieldsE = factoryE.addElement("yields");
                    factory.getYields().stream().forEach((yield) -> {
                        final Element yieldE = yieldsE.addElement("yield");
                        yieldE.setText(Integer.toString(yield));
                    });
                }
                else
                {
                    factoryE.addAttribute("quantity", Integer.toString(factory
                            .getQuantity()));
                }
            });
        }
        if (!this.customPrices.isEmpty())
        {
            final Element waresE = root.addElement("complexWares");
            this.customPrices
                    .entrySet().stream().forEach((entry) -> {
                        final Ware ware = entry.getKey();
                        final int price = entry.getValue();
                        final Element wareE = waresE.addElement("complexWare");
                        wareE.addAttribute("ware", ware.getId());
                        wareE
                                .addAttribute("use", Boolean
                                        .toString(price > 0));
                        wareE.addAttribute("price", Integer.toString(Math.abs(price)));
            });
        }

        final Element shoppingListE = root.addElement("built");
        shoppingListE.addAttribute("kits", Integer.toString(this.builtKits));
        this.builtFactories
                .entrySet().stream().forEach((entry) -> {
                    final String id = entry.getKey();
                    final int quantity = entry.getValue();
                    final Element factoryE = shoppingListE.addElement("factory");
                    factoryE.addAttribute("id", id);
                    factoryE.addAttribute("quantity", Integer.toString(quantity));
        });

        return document;
    }

    /**
     * Loads a complex from the specified XML document and returns it.
     *
     * @param document
     *            The XML document
     * @return The complex
     * @throws DocumentException
     *             If XML file could not be read
     */
    public static Complex fromXML(final Document document)
        throws DocumentException
    {
        final Element root = document.getRootElement();

        // Check the version
        final String versionStr = root.attributeValue("version");
        int version = 1;
        if (versionStr != null) version = Integer.parseInt(versionStr);
        if (version > 4)
            throw new DocumentException(
                I18N.getString("error.fileFormatTooNew"));

        // Determine the game for this complex.
        String gameId = "x3tc";
        if (version == 4) gameId = root.attributeValue("game");
        final Game game = GameFactory.getInstance().getGame(gameId);

        final Complex complex = new Complex(game);
        final FactoryFactory factoryFactory = game.getFactoryFactory();
        final SectorFactory sectorFactory = game.getSectorFactory();
        final WareFactory wareFactory = game.getWareFactory();
        final SunFactory sunsFactory = game.getSunFactory();

        complex.setSuns(sunsFactory.getSun(Integer.parseInt(root
            .attributeValue("suns"))));
        complex.setSector(sectorFactory
            .getSector(root.attributeValue("sector")));
        complex.setAddBaseComplex(Boolean.parseBoolean(root.attributeValue(
            "addBaseComplex", "false")));
        complex.showingProductionStats = Boolean.parseBoolean(root
            .attributeValue("showingProductionStats", "false"));
        complex.showingShoppingList = Boolean.parseBoolean(root.attributeValue(
            "showingShoppingList", "false"));
        complex.showingStorageCapacities = Boolean.parseBoolean(root
            .attributeValue("showingStorageCapacities", "false"));
        complex.showingComplexSetup = Boolean.parseBoolean(root.attributeValue(
            "showingComplexSetup", "true"));

        // Get the factories parent element (In older version this was the root
        // node)
        Element factoriesE = root.element("complexFactories");
        if (factoriesE == null) factoriesE = root;

        // Read the complex factories
        for (final Object item: factoriesE.elements("complexFactory"))
        {
            final Element element = (Element) item;
            final Factory factory = factoryFactory.getFactory(element
                .attributeValue("factory"));
            final ComplexFactory complexFactory;
            final Element yieldsE = element.element("yields");
            if (yieldsE == null)
            {
                final int yield =
                    Integer.parseInt(element.attributeValue("yield", "0"));
                final int quantity = Integer.parseInt(element
                    .attributeValue("quantity"));
                complexFactory =
                    new ComplexFactory(game, factory, quantity, yield);
            }
            else
            {
                final List<Integer> yields = new ArrayList<>();
                for (final Object yieldItem: yieldsE.elements("yield"))
                {
                    final Element yieldE = (Element) yieldItem;
                    yields.add(Integer.parseInt(yieldE.getText()));
                }
                complexFactory = new ComplexFactory(game, factory, yields);
            }
            if (Boolean.parseBoolean(element
                .attributeValue("disabled", "false")))
                complexFactory.disable();
            complex.addFactory(complexFactory);
        }

        // Read the complex wares
        final Element waresE = root.element("complexWares");
        if (waresE != null)
        {
            complex.customPrices.clear();
            for (final Object item: waresE.elements("complexWare"))
            {
                final Element element = (Element) item;
                final Ware ware = wareFactory.getWare(element
                    .attributeValue("ware"));
                final boolean use = Boolean.parseBoolean(element
                    .attributeValue("use"));
                final int price = Integer.parseInt(element
                    .attributeValue("price"));
                complex.customPrices.put(ware, use ? price : -price);
            }
        }

        final Element builtE = root.element("built");
        if (builtE != null)
        {
            complex.builtKits = Integer.parseInt(builtE.attributeValue("kits",
                "0"));
            for (final Object item: builtE.elements("factory"))
            {
                final Element element = (Element) item;
                final String id = element.attributeValue("id");
                final int quantity = Integer.parseInt(element
                    .attributeValue("quantity"));
                complex.builtFactories.put(id, quantity);
            }
        }

        complex.calculateBaseComplex();
        return complex;
    }

    /**
     * Returns all factories (Manually and automatically added ones):
     *
     * @return All factories
     */
    @SuppressWarnings("unchecked")
    private Collection<ComplexFactory> getAllFactories()
    {
        return new MultiCollection<>(this.factories,
            this.autoFactories);
    }

    /**
     * Returns the products this complex produces in one hour.
     *
     * @return The products per hour.
     */
    public Collection<Product> getProductsPerHour()
    {
        final Map<String, Product> products = new HashMap<>();
        getAllFactories().stream().map((factory) -> factory.getProductPerHour(getSuns())).forEach((product) -> {
            final Ware ware = product.getWare();
            final Product mapProduct = products.get(ware.getId());
            if (mapProduct == null)
                products.put(ware.getId(), product);
            else
                products.put(ware.getId(), new Product(ware, mapProduct
                        .getQuantity()
                        + product.getQuantity()));
        });
        return products.values();
    }

    /**
     * Returns the resources this complex needs in one hour.
     *
     * @return The needed resources per hour.
     */
    public Collection<Product> getResourcesPerHour()
    {
        final Map<String, Product> resources = new HashMap<>();
        getAllFactories().stream().forEach((factory) -> {
            factory
                    .getResourcesPerHour(getSuns()).stream().forEach((resource) -> {
                        final Ware ware = resource.getWare();
                        final Product mapResource = resources.get(ware.getId());
                        if (mapResource == null)
                            resources.put(ware.getId(), resource);
                        else
                            resources.put(ware.getId(), new Product(ware, mapResource
                                    .getQuantity()
                                    + resource.getQuantity()));
                    });
        });
        return resources.values();
    }

    /**
     * Returns the list of complex wares (Produced and needed).
     *
     * @return The list of complex wares
     */
    public Collection<ComplexWare> getWares()
    {
        final Map<String, ComplexWare> wares =
            new HashMap<>();

        // Add the products
        getProductsPerHour().stream().forEach((product) -> {
            final Ware ware = product.getWare();
            final String wareId = ware.getId();
            wares.put(wareId, new ComplexWare(ware, product.getQuantity(), 0,
                    getWarePrice(ware)));
        });

        // Add the resources
        getResourcesPerHour().stream().forEach((resource) -> {
            final Ware ware = resource.getWare();
            final String wareId = ware.getId();
            ComplexWare complexWare = wares.get(wareId);
            if (complexWare == null)
                complexWare = new ComplexWare(ware, 0, resource.getQuantity(),
                        getWarePrice(ware));
            else
                complexWare = new ComplexWare(ware, complexWare.getProduced(),
                        resource.getQuantity(), getWarePrice(ware));
            wares.put(wareId, complexWare);
        });

        final List<ComplexWare> result = new ArrayList<>(wares
            .values());
        Collections.sort(result);
        return result;
    }

    /**
     * Returns the profit of this complex.
     *
     * @return The profit
     */
    public double getProfit()
    {
        double profit;

        profit = 0;
        profit = getWares().stream().map((complexWare) -> complexWare.getProfit()).reduce(profit, (accumulator, _item) -> accumulator + _item);
        return profit;
    }

    /**
     * Returns the number of needed complex construction kits in this complex.
     *
     * @return The number of needed complex construction kits.
     */
    public int getKitQuantity()
    {
        return Math.max(0, getTotalQuantity() - 1);
    }

    /**
     * Returns the price of a single complex construction kit.
     *
     * @return The price of a single complex construction kit
     */
    public int getKitPrice()
    {
        return KIT_PRICE;
    }

    /**
     * Returns the total price of all needed complex construction kits.
     *
     * @return The total price of all needed complex construction kits
     */
    public int getTotalKitPrice()
    {
        return getKitQuantity() * getKitPrice();
    }

    /**
     * Calculates and adds the factories needed to keep the factories of this
     * complex running stable.
     */
    private void calculateBaseComplex()
    {
        final FactoryFactory factoryFactory = this.game.getFactoryFactory();
        final RaceFactory raceFactory = this.game.getRaceFactory();
        final Ware crystals = this.game.getWareFactory().getWare("crystals");
        final Config config = Config.getInstance();
        long currentPrice;
        long price;
        final List<ComplexFactory> backup = new ArrayList<>();

        // First of all remove all automatically added factories
        this.autoFactories.clear();
        updateShoppingList();

        if (!this.addBaseComplex) return;

        // First of all we build a base complex without specific crystal fab
        // race and remember the price
        while (true)
            if (!addBaseComplex(null)) break;
        currentPrice = getTotalPrice();

        // Now cycle over all races and check if the complex gets cheaper if
        // the crystal fabs are bought from them
        for (final Race race: raceFactory.getRaces())
        {
            // If race is ignored then don't use it
            if (config.isRaceIgnored(race)) continue;

            // If race has no crystal fabs then don't use it
            if (!factoryFactory.hasFactories(race, crystals)) continue;

            // Backup current automatically added factories, clear the
            // calculated factories and then calculate the complex again with
            // a specific "crystal race"
            backup.addAll(this.autoFactories);
            this.autoFactories.clear();
            while (true)
                if (!addBaseComplex(race)) break;

            // Check if new price is cheaper then the old one. If cheaper
            // then the new complex is used (and checked against the next
            // available race). If not cheaper then the old complex is restored
            price = getTotalPrice();
            if (price < currentPrice)
            {
                currentPrice = price;
            }
            else
            {
                this.autoFactories.clear();
                this.autoFactories.addAll(backup);
            }
            backup.clear();
        }
        updateShoppingList();
    }

    /**
     * Updates the base complex.
     */
    public void updateBaseComplex()
    {
        calculateBaseComplex();
    }

    /**
     * Updates the shopping list.
     */
    public void updateShoppingList()
    {
        this.shoppingList = null;
        this.builtKits = Math.max(0, Math.min(this.builtKits,
            getTotalQuantity() - 1));
        this.builtFactories
                .entrySet().stream().forEach((entry) -> {
                    final String id = entry.getKey();
                    final int max = getMaxFactories(id);
                    final int quantity = Math.min(entry.getValue(), max);
                    this.builtFactories.put(id, quantity);
        });
        this.shoppingList = null;
    }

    /**
     * Searches for the first unfulfilled resource need (which is not a mineral)
     * and adds the necessary factories for this. If a need was found (and
     * fixed) then this method returns true. If all needs are already fulfilled
     * then it returns false.
     *
     * @param crystalRace
     *            Optional race from which crystal fabs should be bought. If
     *            null then the cheapest fab is searched.
     * @return True if a need was found and fixed, false if everything is
     *         finished
     */
    private boolean addBaseComplex(final Race crystalRace)
    {
        for (final ComplexWare ware: getWares())
        {
            // We are not going to add mines
            if (ware.getWare().isMineral()) continue;

            // If the current ware has missing units then add the necessary
            // factories for this ware and then restart the adding of factories
            if (ware.getMissing() > 0)
            {
                final Race race = ware.getWare().getId().equals("crystals")
                    ? crystalRace : null;
                if (!addBaseComplexForWare(ware, race)) continue;
                return true;
            }

        }
        return false;
    }

    /**
     * Adds the factories needed to fulfill the need of the specified complex
     * ware.
     *
     * @param complexWare
     *            The complex ware for which factories must be added
     * @param race
     *            The race from which factories should be bought. If null then
     *            the cheapest factory is used.
     * @return True if a new factories were added, false if this was not
     *         possible
     */
    private boolean addBaseComplexForWare(final ComplexWare complexWare,
        final Race race)
    {
        final Ware ware = complexWare.getWare();
        final FactoryFactory factoryFactory = this.game.getFactoryFactory();

        // Remove all automatically added factories which produces the
        // specified ware and calculate the real need which must be
        // fulfilled.
        double need = complexWare.getMissing();
        final double oldNeed = need;
        for (final ComplexFactory complexFactory: new ArrayList<>(
            this.autoFactories))
        {
            if (complexFactory.getFactory().getProduct().getWare().equals(ware))
            {
                need += complexFactory.getProductPerHour(getSuns())
                    .getQuantity();
                this.autoFactories.remove(complexFactory);
            }
        }

        // Determine the available factory sizes
        final SortedSet<FactorySize> sizesSet =
            factoryFactory.getFactorySizes(ware, race);
        final FactorySize[] sizes =
            sizesSet.toArray(new FactorySize[sizesSet.size()]);

        // Abort if no factories were found
        if (sizes.length == 0) return false;

        // Get the cheapest factories for the sizes
        final Map<FactorySize, Factory> localFactories =
            new HashMap<>();
        for (final FactorySize size: sizes)
        {
            if (race == null)
                localFactories.put(size, factoryFactory.getCheapestFactory(ware,
                    size));
            else
                localFactories
                    .put(size, factoryFactory.getFactory(ware, size, race));
        }

        // Get the smallest possible production quantity
        final double minProduction = localFactories.get(sizes[0]).getProductPerHour(
            getSuns(), 0).getQuantity();

        // Iterate the available sizes (from largest to smallest) and add
        // the factories producing an adequate number of products
        for (int i = sizes.length - 1; i >= 0; i--)
        {
            final FactorySize size = sizes[i];
            final Factory factory = localFactories.get(size);
            final double product = factory.getProductPerHour(getSuns(), 0)
                .getQuantity();

            // Calculate the number of factories of the current size needed
            log.debug("Need " + need + " units of " + ware + ". Considering "
                + factory + " which produces " + product + " units");
            final int quantity = (int) Math.floor((need + minProduction - 0.1)
                / product);

            // Add the number of factories and decrease the need
            if (quantity > 0)
            {
                log.debug("Adding " + quantity + "x " + factory);
                this.autoFactories
                    .add(new ComplexFactory(this.game, factory, quantity, 0));
                need -= quantity * product;
            }
            else
                log.debug("Not adding any " + factory);
        }
        if (Math.abs(need - oldNeed) < .0000001)
        {
            log.debug("Unable to calculate best matching factory. Aborting");
            return false;
        }
        return true;
    }

    /**
     * Toggles the addition of automatically calculated base complex.
     */
    public void toggleAddBaseComplex()
    {
        this.addBaseComplex = !this.addBaseComplex;
        calculateBaseComplex();
    }

    /**
     * Enables or disabled base complex addition.
     *
     * @param addBaseComplex
     *            True if base complex should be added, false if not
     */
    public void setAddBaseComplex(final boolean addBaseComplex)
    {
        this.addBaseComplex = addBaseComplex;
    }

    /**
     * Checks whether a base complex was added or not.
     *
     * @return True if a base complex was added. False if not.
     */
    public boolean isAddBaseComplex()
    {
        return this.addBaseComplex;
    }

    /**
     * Returns the storage capacities.
     *
     * @return The storage capacities.
     */
    public Collection<Capacity> getCapacities()
    {
        final Map<String, Capacity> capacities =
            new HashMap<>();
        getAllFactories().stream().forEach((factory) -> {
            factory.getCapacities().stream().forEach((capacity) -> {
                final Ware ware = capacity.getWare();
                final Capacity mapCapacity = capacities.get(ware.getId());
                if (mapCapacity == null)
                    capacities.put(ware.getId(), capacity);
                else
                    capacities.put(ware.getId(), new Capacity(ware, mapCapacity
                            .getQuantity()
                            + capacity.getQuantity()));
            });
        });
        final List<Capacity> result = new ArrayList<>(capacities
            .values());
        Collections.sort(result);
        return result;
    }

    /**
     * Returns the total storage capacity
     *
     * @return The total storage capacity;
     */
    public long getTotalCapacity()
    {
        long total = 0;
        total = getCapacities().stream().map((capacity) -> capacity.getQuantity()).reduce(total, (accumulator, _item) -> accumulator + _item);
        return total;
    }

    /**
     * Returns the total storage volume
     *
     * @return The total storage volume;
     */
    public long getTotalStorageVolume()
    {
        long total = 0;
        total = getCapacities().stream().map((capacity) -> capacity.getVolume()).reduce(total, (accumulator, _item) -> accumulator + _item);
        return total;
    }

    /**
     * Sets the sector in which to build this complex
     *
     * @param sector
     *            The sector to set
     */
    public void setSector(final Sector sector)
    {
        if ((sector != null && !sector.equals(this.sector))
            || (sector == null && this.sector != null))
        {
            this.sector = sector;
            calculateBaseComplex();
            updateShoppingList();
        }
    }

    /**
     * Returns the sector in which this complex is build.
     *
     * @return The sector
     */
    public Sector getSector()
    {
        return this.sector;
    }

    /**
     * Returns the factory shopping list.
     *
     * @return The factory shopping list
     */
    public ShoppingList getShoppingList()
    {
        // Return cached shopping list if present
        if (this.shoppingList != null) return this.shoppingList;

        final ShoppingList list = new ShoppingList(this.sector == null ? null
            : this.sector.getNearestKitSellingSector(), this.builtKits);
        this.factories.stream().forEach((factory) -> {
            list.addItem(new ShoppingListItem(factory.getFactory(), factory
                    .getQuantity(), this.sector == null ? null : factory
                            .getFactory().getNearestManufacturer(this.sector),
                    getFactoriesBuilt(factory.getFactory())));
        });
        this.autoFactories.stream().forEach((factory) -> {
            list.addItem(new ShoppingListItem(factory.getFactory(), factory
                    .getQuantity(), this.sector == null ? null : factory
                            .getFactory().getNearestManufacturer(this.sector),
                    getFactoriesBuilt(factory.getFactory())));
        });

        this.shoppingList = list;
        return list;
    }

    /**
     * Returns the price for the specified ware. If the price has a custom price
     * then this one is returned. If not then the standard average price of the
     * ware is returned.
     *
     * @param ware
     *            The ware
     * @return The price
     */
    public int getWarePrice(final Ware ware)
    {
        final Integer price = this.customPrices.get(ware);
        if (price == null) return ware.getAvgPrice();
        if (price < 0) return 0;
        return price;
    }

    /**
     * Returns the map with custom prices.
     *
     * @return The map with custom prices
     */
    public Map<Ware, Integer> getCustomPrices()
    {
        return Collections.unmodifiableMap(this.customPrices);
    }

    /**
     * Sets a new map with custom prices.
     *
     * @param customPrices
     *            The new map with custom prices
     */
    public void setCustomPrices(final Map<Ware, Integer> customPrices)
    {
        this.customPrices.clear();
        this.customPrices.putAll(customPrices);
    }

    /**
     * Checks if complex setup should be displayed.
     *
     * @return True if complex setup should be displayed, false if not
     */
    public boolean isShowingComplexSetup()
    {
        return this.showingComplexSetup;
    }

    /**
     * Toggles the display of the complex setup.
     */
    public void toggleShowingComplexSetup()
    {
        this.showingComplexSetup = !this.showingComplexSetup;
    }

    /**
     * Checks if production stats should be displayed.
     *
     * @return True if production stats should be displayed, false if not
     */
    public boolean isShowingProductionStats()
    {
        return this.showingProductionStats;
    }

    /**
     * Toggles the display of production statistics.
     */
    public void toggleShowingProductionStats()
    {
        this.showingProductionStats = !this.showingProductionStats;
    }

    /**
     * Checks if storage capacities should be displayed.
     *
     * @return True if storage capacities should be displayed, false if not
     */
    public boolean isShowingStorageCapacities()
    {
        return this.showingStorageCapacities;
    }

    /**
     * Toggles the display of storage capacities.
     */
    public void toggleShowingStorageCapacities()
    {
        this.showingStorageCapacities = !this.showingStorageCapacities;
    }

    /**
     * Checks if the shopping list should be displayed.
     *
     * @return True if the shopping list should be displayed, false if not
     */
    public boolean isShowingShoppingList()
    {
        return this.showingShoppingList;
    }

    /**
     * Toggles the display of the shopping list.
     */
    public void toggleShowingShoppingList()
    {
        this.showingShoppingList = !this.showingShoppingList;
    }

    /**
     * Returns the maximum number of factories in the shopping list for the
     * specified factory id.
     *
     * @param id
     *            The id of the factory
     * @return The number of factories in the shopping list
     */
    private int getMaxFactories(final String id)
    {
        final ShoppingList list = getShoppingList();
        for (final ShoppingListItem item: list.getItems())
        {
            if (item.getFactory().getId().equals(id))
                return item.getQuantity();
        }
        return 0;
    }

    /**
     * Builds a factory with the specified id
     *
     * @param id
     *            The id of the factory to build
     */
    public void buildFactory(final String id)
    {
        Integer oldCount = this.builtFactories.get(id);
        if (oldCount == null) oldCount = 0;
        if (oldCount == getMaxFactories(id)) return;
        this.builtFactories.put(id, oldCount + 1);
        updateShoppingList();
    }

    /**
     * Destroys a factory with the specified id.
     *
     * @param id
     *            The id of the factory to destroy
     */
    public void destroyFactory(final String id)
    {
        final Integer oldCount = this.builtFactories.get(id);
        if (oldCount == null) return;
        if (oldCount == 0) return;
        this.builtFactories.put(id, oldCount - 1);
        updateShoppingList();
    }

    /**
     * Build a kit.
     */
    public void buildKit()
    {
        if (this.builtKits >= getTotalQuantity() - 1) return;
        this.builtKits++;
        updateShoppingList();
    }

    /**
     * Destroys a kit.
     */
    public void destroyKit()
    {
        if (this.builtKits == 0) return;
        this.builtKits--;
        updateShoppingList();
    }

    /**
     * Returns the number of built factories of the specified type.
     *
     * @param factory
     *            The factory type
     * @return The number of built factores of the specified type
     */
    private int getFactoriesBuilt(final Factory factory)
    {
        final Integer count = this.builtFactories.get(factory.getId());
        return count == null ? 0 : count;
    }

    /**
     * @see de.ailis.xadrian.interfaces.GameProvider#getGame()
     */
    @Override
    public Game getGame()
    {
        return this.game;
    }

    /**
     * Checks if the complex uses the specified ware as product or resource.
     *
     * @param ware
     *            The ware to check.
     * @return True if ware is used, false if not.
     */
    public boolean usesWare(final Ware ware)
    {
        for (final ComplexFactory complexFactory: getAllFactories())
        {
            final Factory factory = complexFactory.getFactory();
            if (factory.getProduct().getWare().equals(ware)) return true;
            for (final Product product: factory.getResources())
                if (product.getWare().equals(ware)) return true;
        }
        return false;
    }

    /**
     * Checks if the complex is empty.
     *
     * @return True when the complex is empty, false if not.
     */
    public boolean isEmpty()
    {
        return this.factories.isEmpty();
    }

    /**
     * Checks if the specified code is a valid template code.
     *
     * @param templateCode The template code to check.
     * @return True if code is valid, false if not.
     */
    public static boolean isValidTemplateCode(final String templateCode)
    {
        try
        {
            // Decode base 64
            final byte[] data =
                DatatypeConverter.parseBase64Binary(templateCode.trim());

            try
            (InputStream stream = new DynaByteInputStream(
                    new ByteArrayInputStream(data))) {
                // Read complex settings
                final int settings = stream.read();
                final boolean hasSector = (settings & 1) == 1;
                final int gameNid = (settings >> 1) & 7;
    
                final Game game;
                try
                {
                    game = GameFactory.getInstance().getGame(gameNid);
                }
                catch (final GameNotFoundException e)
                {
                    return false;
                }
    
                // Read sector coordinates or sun power
                if (hasSector)
                {
                    final int x = stream.read();
                    final int y = stream.read();
                    if (game.getSectorFactory().getSector(x, y) == null) return false;
                }
                else
                {
                    final int percent = stream.read();
                    try
                    {
                        game.getSunFactory().getSun(percent);
                    }
                    catch (final DataException e)
                    {
                        return false;
                    }
                }
    
                int factoryId;
                while ((factoryId = stream.read()) != 0)
                {
                    final Factory factory =
                        game.getFactoryFactory().getFactory(factoryId);
                    if (factory == null) return false;
                    if (factory.isMine())
                    {
                        int yield;
                        while ((yield = stream.read()) != 0)
                            if (yield > 256) return false;
                    }
                    else
                    {
                        stream.read();
                    }
                }
    
                return true;
            }
        }
        catch (final Exception e)
        {
            return false;
        }
    }

    /**
     * Creates a complex from the specified template code.
     *
     * @param templateCode
     *            The template code
     * @return The complex.
     */
    public static Complex fromTemplateCode(final String templateCode)
    {
        try
        {
            // Decode base 64
            final byte[] data =
                DatatypeConverter.parseBase64Binary(templateCode.trim());

            try
            (InputStream stream = new DynaByteInputStream(
                    new ByteArrayInputStream(data))) {
                // Read complex settings
                final int settings = stream.read();
                final boolean hasSector = (settings & 1) == 1;
                final int gameNid = (settings >> 1) & 7;
    
                final Game game = GameFactory.getInstance().getGame(gameNid);
                final Complex complex = new Complex(game);
    
                // Read sector coordinates or sun power
                if (hasSector)
                {
                    final int x = stream.read();
                    final int y = stream.read();
                    complex.setSector(game.getSectorFactory().getSector(x, y));
                }
                else
                {
                    final int percent = stream.read();
                    complex.setSuns(game.getSunFactory().getSun(percent));
                }
    
                int factoryId;
                while ((factoryId = stream.read()) != 0)
                {
                    final Factory factory =
                        game.getFactoryFactory().getFactory(factoryId);
                    if (factory.isMine())
                    {
                        final List<Integer> yields = new ArrayList<>();
                        int yield;
                        while ((yield = stream.read()) != 0)
                            yields.add(yield - 1);
                        complex
                            .addFactory(new ComplexFactory(game, factory, yields));
                    }
                    else
                    {
                        final int quantity = stream.read();
                        complex.addFactory(new ComplexFactory(game, factory,
                            quantity, 0));
                    }
                }
    
                return complex;
            }
        }
        catch (final IOException e)
        {
            throw new TemplateCodeException(e.toString(), e);
        }
    }

    /**
     * Returns the template code.
     *
     * @return The template code.
     */
    public String getTemplateCode()
    {
        try
        {
            final ByteArrayOutputStream arrayStream =
                new ByteArrayOutputStream();
            // Write the template settings bit mask.
            try (OutputStream stream = new DynaByteOutputStream(arrayStream)) {
                // Write the template settings bit mask.
                int settings = this.sector == null ? 0 : 1;
                settings |= this.game.getNid() << 1;
                stream.write(settings);
                
                // Write the sector coordinates
                if (this.sector != null)
                {
                    stream.write(this.sector.getX());
                    stream.write(this.sector.getY());
                }
                
                // Or else write the sun power
                else
                {
                    stream.write(this.suns.getPercent());
                }
                
                // Write the factories
                for (final ComplexFactory complexFactory: getAllFactories())
                {
                    if (complexFactory.isDisabled()) continue;
                    final Factory factory = complexFactory.getFactory();
                    stream.write(factory.getNid());
                    if (factory.isMine())
                    {
                        for (final int yield: complexFactory.getYields())
                            stream.write(yield + 1);
                        stream.write(0);
                    }
                    else
                        stream.write(complexFactory.getQuantity());
                }
                
                // Write end marker
                stream.write(0);
            }

            // Get byte array from stream
            final byte[] data = arrayStream.toByteArray();

            // Return base 64 encoded bytes
            return DatatypeConverter.printBase64Binary(data);
        }
        catch (final IOException e)
        {
            throw new TemplateCodeException(e.toString(), e);
        }
    }

    /**
     * Checks if this complex has mines.
     *
     * @return True if complex has mines, false if not.
     */
    public boolean hasMines()
    {
        return this.factories.stream().anyMatch((factory) -> (factory.getFactory().isMine()));
    }

    /**
     * Returns the complex data as ASCII.
     *
     * @return The complex data as ASCII.
     */
    public String getASCII()
    {
        final StringWriter writer = new StringWriter();
        final PrintWriter out = new PrintWriter(writer);

        // Print the game name.
        out.print(I18N.getString("complex.game"));
        out.print(": ");
        out.println(getGame().getName());

        // Print the sector name if chosen
        final Sector localSector = getSector();
        if (localSector != null)
        {
            out.print(I18N.getString("complex.sector"));
            out.print(": ");
            out.println(localSector.getName());
        }

        // Print the sun power
        out.print(I18N.getString("complex.suns"));
        out.print(": ");
        out.println(getSuns().toString());

        // Print the template code
        out.print(I18N.getString("complex.templateCode"));
        out.print(": ");
        out.println(getTemplateCode());

        out.println();

        // Print factories
        out.print(I18N.getString("complex.factories"));
        out.println(":");
        getAllFactories().stream().filter((complexFactory) -> !(complexFactory.isDisabled())).map((complexFactory) -> {
            final Factory factory = complexFactory.getFactory();
            out.print(complexFactory.getQuantity());
            out.print("x ");
            out.print(factory.getName());
            out.print(" (");
            out.print(factory.getRace().getName());
            if (factory.isMine())
            {
                out.print(", ");
                out.print(I18N.getString("complex.yield"));
                out.print(": ");
                boolean first = true;
                for (final int yield: complexFactory.getYields())
                {
                    if (!first)
                        out.print(", ");
                    out.print(yield);
                    first = false;
                }
            }
            return complexFactory;
        }).forEach((_item) -> {
            out.println(")");
        });

        out.println();

        // Print total complex price
        out.print(I18N.getString("complex.totalPrice"));
        out.print(": ");
        out.print(new DecimalFormat().format(getTotalPrice()));
        out.println(" Cr");

        // Print total complex price
        out.print(I18N.getString("complex.profitPerHour"));
        out.print(": ");
        out.print(new DecimalFormat().format(Math.round(getProfit())));
        out.println(" Cr");
        return writer.toString();
    }
}

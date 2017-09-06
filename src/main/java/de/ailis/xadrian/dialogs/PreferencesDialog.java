/*
 * Copyright (C) 2010-2012 Klaus Reimer <k@ailis.de>
 * See LICENSE.TXT for licensing information.
 */

package de.ailis.xadrian.dialogs;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import de.ailis.xadrian.components.ComboBoxEntry;
import de.ailis.xadrian.components.LabelSeparator;
import de.ailis.xadrian.data.Game;
import de.ailis.xadrian.data.Race;
import de.ailis.xadrian.data.Theme;
import de.ailis.xadrian.data.factories.GameFactory;
import de.ailis.xadrian.data.factories.ThemeFactory;
import de.ailis.xadrian.frames.SplashFrame;
import de.ailis.xadrian.support.Config;
import de.ailis.xadrian.support.I18N;
import de.ailis.xadrian.support.ModalDialog;
import de.ailis.xadrian.utils.SwingUtils;

/**
 * Dialog for setting up the application preferences.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
public class PreferencesDialog extends ModalDialog
{
    /** Serial version UID */
    private static final long serialVersionUID = 6796446567309230958L;

    /** The singleton instance of this dialog */
    private final static PreferencesDialog instance = new PreferencesDialog();

    /** The checkboxes for allowed manufacturer races */
    private Map<Race, JCheckBox> racesCheckBoxes;

    /**
     * The checkbox to enable factory description (resources needed) in Complex
     * table.
     */
    private JCheckBox showFactoryResourcesCheckBox;

    /**
     * The night mode checkbox.
     */
    private JCheckBox nightModeCheckBox;

    /** The X3TC player sector combo box */
    private JComboBox x3tcPlayerSectorComboBox;

    /** The X3AP player sector combo box */
    private JComboBox x3apPlayerSectorComboBox;

    /** The games combo box */
    private JComboBox gamesComboBox;

    /** The theme combo box. */
    private JComboBox themeComboBox;

    /** The locale combo box. */
    private JComboBox localeComboBox;

    /** The production statistics combo box. */
    private JComboBox prodStatsComboBox;

    /**
     * Constructor
     */
    private PreferencesDialog()
    {
        init("preferences", Result.OK, Result.CANCEL);
        SplashFrame.advanceProgress();
    }

    /**
     * Creates the UI
     */
    @Override
    protected void createUI()
    {
        // Create the content panel
        final JPanel contentPanel = new JPanel();
        contentPanel
            .setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
        contentPanel
            .setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        contentPanel.add(createGeneralSettings());
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(createUsedRaces());
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(createX3TCSettings());
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(createX3APSettings());
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(createViewSettings());

        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Creates and returns the panel with the races settings.
     *
     * @return The created panel
     */
    private JPanel createUsedRaces()
    {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        // Create the races separator
        final LabelSeparator separator =
            new LabelSeparator(I18N.getString("dialog.preferences.usedRaces"));
        separator.setToolTipText(I18N
            .getToolTip("dialog.preferences.usedRaces"));
        panel.add(separator);

        // Create the race checkboxes
        final JPanel racePanel = new JPanel();
        racePanel.setLayout(new GridBagLayout());
        racePanel.setBorder(new EmptyBorder(2, 20, 5, 0));

        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        this.racesCheckBoxes = new HashMap<>();
        getIgnorableRaces().stream().map((race) -> {
            final JCheckBox raceCheckBox = new JCheckBox(race.getName());
            if (c.gridx == 3)
            {
                c.gridx = 0;
                c.gridy++;
            }
            racePanel.add(raceCheckBox, c);
            this.racesCheckBoxes.put(race, raceCheckBox);
            return race;
        }).forEach((_item) -> {
            c.gridx++;
        });
        panel.add(racePanel);
        return panel;
    }

    /**
     * Creates and returns the list of ignorable races.
     *
     * @return The list of ignorable races.
     */
    private SortedSet<Race> getIgnorableRaces()
    {
        final SortedSet<Race> races = new TreeSet<>();
        GameFactory.getInstance().getGames().stream().forEach((game) -> {
            game.getRaceFactory().getManufacturerRaces().stream().forEach((race) -> {
                races.add(race);
            });
        });
        return races;
    }

    /**
     * Creates and returns the panel with the X3TC game settings.
     *
     * @return The created panel
     */
    private JPanel createX3TCSettings()
    {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        // Create the separator
        final LabelSeparator separator =
            new LabelSeparator(I18N.getString("game.x3tc"));
        panel.add(separator);

        // Create the control panel
        final JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        controlPanel.setBorder(new EmptyBorder(2, 20, 5, 0));
        final GridBagConstraints c = new GridBagConstraints();
        final JLabel label =
            new JLabel(I18N.getString("dialog.preferences.playerSector"));
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 0;
        c.insets.top = 2;
        c.insets.bottom = 2;
        controlPanel.add(label, c);
        c.weightx = 1;
        c.gridx = 1;
        c.insets.left = 5;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.x3tcPlayerSectorComboBox = new JComboBox();
        final Game x3tc = GameFactory.getInstance().getGame("x3tc");
        for (int i = 0; i < 5; i++)
        {
            this.x3tcPlayerSectorComboBox.addItem(I18N.getString(x3tc,
                "sector.sec-20-2-" + i));
        }
        controlPanel.add(this.x3tcPlayerSectorComboBox, c);
        c.fill = GridBagConstraints.NONE;
        panel.add(controlPanel);
        return panel;
    }

    /**
     * Creates and returns the panel with the X3AP game settings.
     *
     * @return The created panel
     */
    private JPanel createX3APSettings()
    {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        // Create the separator
        final LabelSeparator separator =
            new LabelSeparator(I18N.getString("game.x3ap"));
        panel.add(separator);

        // Create the control panel
        final JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        controlPanel.setBorder(new EmptyBorder(2, 20, 5, 0));
        final GridBagConstraints c = new GridBagConstraints();
        final JLabel label =
            new JLabel(I18N.getString("dialog.preferences.playerSector"));
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 0;
        c.insets.top = 2;
        c.insets.bottom = 2;
        controlPanel.add(label, c);
        c.weightx = 1;
        c.gridx = 1;
        c.insets.left = 5;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.x3apPlayerSectorComboBox = new JComboBox();
        final Game x3ap = GameFactory.getInstance().getGame("x3ap");
        for (int i = 0; i < 6; i++)
        {
            this.x3apPlayerSectorComboBox.addItem(I18N.getString(x3ap,
                "sector.sec-12-4-" + i));
        }
        controlPanel.add(this.x3apPlayerSectorComboBox, c);
        c.fill = GridBagConstraints.NONE;
        panel.add(controlPanel);
        return panel;
    }
    
    /**
     * Creates and returns the panel with the general settings.
     *
     * @return The created panel
     */
    private JPanel createGeneralSettings()
    {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        // Create the separator
        final LabelSeparator separator =
            new LabelSeparator(I18N.getString("dialog.preferences.general"));
        panel.add(separator);

        // Create the control panel
        final JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        controlPanel.setBorder(new EmptyBorder(2, 20, 5, 0));
        final GridBagConstraints c = new GridBagConstraints();
        final JLabel label =
            new JLabel(I18N.getString("dialog.preferences.game"));
        c.anchor = GridBagConstraints.WEST;
        c.insets.top = 2;
        c.insets.bottom = 2;
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 0;
        controlPanel.add(label, c);
        c.weightx = 1;
        c.gridx = 1;
        c.insets.left = 5;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.gamesComboBox = new JComboBox();
        this.gamesComboBox.addItem(I18N
            .getString("dialog.preferences.alwaysAsk"));
        GameFactory.getInstance().getGames().stream().forEach((game) -> {
            this.gamesComboBox.addItem(game);
        });
        controlPanel.add(this.gamesComboBox, c);
        c.fill = GridBagConstraints.NONE;
        panel.add(controlPanel);
        return panel;
    }

    /**
     * Creates and returns the panel with the view settings.
     *
     * @return The created panel
     */
    private JPanel createViewSettings()
    {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        // Create the separator
        final LabelSeparator separator =
            new LabelSeparator(
                I18N.getString("dialog.preferences.viewSettings"));
        panel.add(separator);

        // Create the control panel
        final JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        controlPanel.setBorder(new EmptyBorder(2, 20, 5, 0));
        panel.add(controlPanel);
        final GridBagConstraints c = new GridBagConstraints();

        // Create the theme controls
        JLabel label =
            new JLabel(I18N.getString("dialog.preferences.theme"));
        c.anchor = GridBagConstraints.LINE_START;
        c.insets.top = 2;
        c.insets.bottom = 2;
        c.gridx = 0;
        c.gridy = 0;
        controlPanel.add(label, c);
        c.gridx = 1;
        c.insets.left = 5;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.themeComboBox = new JComboBox();
        ThemeFactory.getInstance().getThemes().stream().forEach((theme) -> {
            this.themeComboBox.addItem(theme);
        });
        controlPanel.add(this.themeComboBox, c);
        c.insets.left = 0;
        c.fill = GridBagConstraints.NONE;

        // Create the theme controls
        label =
            new JLabel(I18N.getString("dialog.preferences.language"));
        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 0;
        c.gridy = 1;
        controlPanel.add(label, c);
        c.gridx = 1;
        c.insets.left = 5;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.localeComboBox = new JComboBox();
        this.localeComboBox.addItem(new ComboBoxEntry(I18N
            .getString("locale.system"), null));
        for (final String locale: I18N.getString("locale.locales").split(","))
        {
            this.localeComboBox.addItem(new ComboBoxEntry(I18N
                .getString("locale." + locale), locale));
        }
        controlPanel.add(this.localeComboBox, c);
        c.insets.left = 0;
        c.fill = GridBagConstraints.NONE;

        // Create the theme controls
        label =
            new JLabel(I18N.getString("dialog.preferences.prodStats"));
        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 0;
        c.gridy++;
        controlPanel.add(label, c);
        c.gridx = 1;
        c.insets.left = 5;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.prodStatsComboBox = new JComboBox();
        this.prodStatsComboBox.addItem(new ComboBoxEntry(I18N
            .getString("dialog.preferences.prodStats.perHour"), false));
        this.prodStatsComboBox.addItem(new ComboBoxEntry(I18N
            .getString("dialog.preferences.prodStats.perMinute"), true));
        controlPanel.add(this.prodStatsComboBox, c);
        c.insets.left = 0;
        c.fill = GridBagConstraints.NONE;

        // Create the "show factory resources" controls
        this.showFactoryResourcesCheckBox = new JCheckBox(
            I18N.getString("dialog.preferences.showFactoryResources"));
        this.showFactoryResourcesCheckBox.setToolTipText(
            I18N.getToolTip("dialog.preferences.showFactoryResources"));
        c.weightx = 1;
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        controlPanel.add(this.showFactoryResourcesCheckBox, c);

        // Create the "night mode" controls
        this.nightModeCheckBox = new JCheckBox(
            I18N.getString("dialog.preferences.nightMode"));
        this.nightModeCheckBox.setToolTipText(
            I18N.getToolTip("dialog.preferences.nightMode"));
        c.gridy++;
        controlPanel.add(this.nightModeCheckBox, c);

        panel.add(controlPanel);
        return panel;
    }

    /**
     * Returns the singleton instance
     *
     * @return The singleton instance
     */
    public static PreferencesDialog getInstance()
    {
        return instance;
    }

    /**
     * @return 
     * @see de.ailis.xadrian.support.ModalDialog#open()
     */
    @Override
    public Result open()
    {
        final Config config = Config.getInstance();

        // Load preference values
        this.racesCheckBoxes
                .entrySet().stream().forEach((entry) -> {
                    final Race race = entry.getKey();
                    final JCheckBox checkBox = entry.getValue();
                    checkBox.setSelected(!config.isRaceIgnored(race));
        });
        this.showFactoryResourcesCheckBox.setSelected(config
            .isShowFactoryResources());
        this.nightModeCheckBox.setSelected(config
            .isNightMode());
        this.themeComboBox.setSelectedItem(ThemeFactory.getInstance().getTheme(
            UIManager.getLookAndFeel().getClass().getName()));
        this.localeComboBox.setSelectedItem(new ComboBoxEntry(null,
            config.getLocale()));
        this.prodStatsComboBox.setSelectedItem(new ComboBoxEntry(null,
            config.isProdStatsPerMinute()));
        this.x3tcPlayerSectorComboBox.setSelectedIndex(config.getX3TCPlayerSector());
        this.x3apPlayerSectorComboBox.setSelectedIndex(config.getX3APPlayerSector());

        final String defaultGameId = config.getDefaultGame();
        final GameFactory gameFactory = GameFactory.getInstance();
        if (defaultGameId == null || !gameFactory.hasGame(defaultGameId))
            this.gamesComboBox.setSelectedIndex(0);
        else
            this.gamesComboBox.setSelectedItem(GameFactory.getInstance()
                .getGame(defaultGameId));

        final Result result = super.open();
        if (result == Result.OK)
        {
            // Save preference values
            this.racesCheckBoxes
                    .entrySet().stream().forEach((entry) -> {
                        final Race race = entry.getKey();
                        final JCheckBox checkBox = entry.getValue();
                        config.setRaceIgnored(race, !checkBox.isSelected());
            });
            GameFactory.getInstance().getGames().stream().forEach((game) -> {
                game.getAddFactoryDialog().resetFactoriesTreeModel();
            });
            config.setShowFactoryResources(this.showFactoryResourcesCheckBox
                .isSelected());
            config.setNightMode(this.nightModeCheckBox.isSelected());
            config.setX3TCPlayerSector(
                this.x3tcPlayerSectorComboBox.getSelectedIndex());
            config.setX3APPlayerSector(
                this.x3apPlayerSectorComboBox.getSelectedIndex());
            config.setTheme(((Theme) this.themeComboBox.getSelectedItem())
                .getClassName());
            config.setLocale((String) ((ComboBoxEntry)
                this.localeComboBox.getSelectedItem()).getValue());
            config.setProdStatsPerMinute((Boolean) ((ComboBoxEntry)
                this.prodStatsComboBox.getSelectedItem()).getValue());
            if (this.gamesComboBox.getSelectedIndex() == 0)
                config.setDefaultGame(null);
            else
                config.setDefaultGame(((Game) this.gamesComboBox
                    .getSelectedItem()).getId());
        }
        return result;
    }

    /**
     * Test main method.
     *
     * @param args
     *            Command line arguments
     */
    public static void main(final String[] args)
    {
        SwingUtils.prepareGUI();
        new PreferencesDialog().open();
        Config.getInstance().save();
        System.exit(0);
    }
}

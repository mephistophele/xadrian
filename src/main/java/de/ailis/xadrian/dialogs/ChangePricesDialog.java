/*
 * Copyright (C) 2010-2012 Klaus Reimer <k@ailis.de>
 * See LICENSE.TXT for licensing information.
 */

package de.ailis.xadrian.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;

import de.ailis.xadrian.data.Complex;
import de.ailis.xadrian.data.Game;
import de.ailis.xadrian.data.Ware;
import de.ailis.xadrian.data.factories.WareFactory;
import de.ailis.xadrian.frames.SplashFrame;
import de.ailis.xadrian.support.I18N;
import de.ailis.xadrian.support.ModalDialog;
import de.ailis.xadrian.utils.SwingUtils;

/**
 * Dialog for setting up the ware prices.
 * 
 * @author Klaus Reimer (k@ailis.de)
 */
final public class ChangePricesDialog extends ModalDialog
{
    /** Serial version UID */
    private static final long serialVersionUID = -7047840854198687941L;

    /** The map with custom prices */
    private final Map<Ware, Integer> customPrices =
        new HashMap<>();

    /** The ware prices panel */
    private JPanel warePricesPanel;

    /** The active ware (Is focused when dialog opens) */
    private Ware activeWare;

    /** The scroll pane */
    private JScrollPane scrollPane;

    /** The game. */
    private final Game game;

    /**
     * Constructor
     * 
     * @param game
     *            The game. Must not be null.
     */
    public ChangePricesDialog(final Game game)
    {
        if (game == null)
            throw new IllegalArgumentException("game must be set");
        this.game = game;
        init("changePrices", Result.OK, Result.CANCEL);
        SplashFrame.advanceProgress();
    }

    /**
     * Creates the UI
     */
    @Override
    protected void createUI()
    {
        // Enable dialog resizing
        setResizable(true);

        // Create the content panel
        this.warePricesPanel = new JPanel();
        this.warePricesPanel.setLayout(new GridBagLayout());
        this.warePricesPanel.setBackground(Color.WHITE);

        final JScrollPane localScrollPane = new JScrollPane(this.warePricesPanel);
        localScrollPane.setBorder(BorderFactory
            .createBevelBorder(BevelBorder.LOWERED));
        localScrollPane.setPreferredSize(new Dimension(720, 512));
        localScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        this.scrollPane = localScrollPane;

        final JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.add(localScrollPane, BorderLayout.CENTER);

        // Put this last panel into the window
        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Initializes the content.
     * 
     * @param complex
     *            The current complex
     */
    private void initContent(final Complex complex)
    {
        final WareFactory wareFactory = this.game.getWareFactory();
        final Color gray = new Color(0xee, 0xee, 0xee);
        final NumberFormat formatter = NumberFormat.getNumberInstance();
        JSpinner focusComponent = null;

        final GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.BOTH;
        for (final Ware ware : wareFactory.getWares())
        {
            // Ignore ware if not used by the complex
            if (!complex.usesWare(ware)) continue;

            // Get price and check if ware is used
            int price;
            final boolean used;
            if (this.customPrices.containsKey(ware))
            {
                price = this.customPrices.get(ware);
                used = price > 0;
                price = Math.abs(price);
            }
            else
            {
                price = ware.getAvgPrice();
                used = true;
            }

            // Calculate the color to use in this row
            final Color color = c.gridy % 2 == 0 ? Color.WHITE : gray;

            // Add the title label
            c.gridx = 0;
            c.weightx = 1;
            final JLabel titleLabel = new JLabel(String.format(
                "<html>%s<br /><font size=\"2\" color=\"#888888\">"
                    + "%s / %s / %s Cr</font></html>", ware.getName(),
                formatter.format(ware.getMinPrice()), formatter.format(ware
                    .getAvgPrice()), formatter.format(ware.getMaxPrice())));
            titleLabel.setAlignmentX(LEFT_ALIGNMENT);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            titleLabel.setOpaque(true);
            titleLabel.setBackground(color);
            this.warePricesPanel.add(titleLabel, c);
            c.weightx = 0;

            // Add the buy/sell checkbox
            c.gridx++;
            final JCheckBox checkBox = new JCheckBox(I18N
                .getString("dialog.changePrices.used"));
            checkBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            checkBox.setBackground(color);
            checkBox.setOpaque(true);
            checkBox.setSelected(used);
            this.warePricesPanel.add(checkBox, c);

            // Add the price label
            c.gridx++;
            final JLabel priceLabel = new JLabel(I18N
                .getString("dialog.changePrices.price")
                + ":");
            final JPanel pricePanel = new JPanel(new BorderLayout());
            pricePanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 0));
            pricePanel.setBackground(color);
            priceLabel.setEnabled(used);
            pricePanel.add(priceLabel, BorderLayout.CENTER);
            this.warePricesPanel.add(pricePanel, c);

            // Add the price slider
            c.gridx++;
            final JSlider slider = new JSlider(ware.getMinPrice(), ware
                .getMaxPrice(), price);
            final JPanel sliderPanel = new JPanel(new BorderLayout());
            sliderPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            slider.setEnabled(used);
            sliderPanel.setBackground(color);
            slider.setSnapToTicks(true);
            slider.setMinorTickSpacing(1);
            slider.setPaintLabels(false);
            slider.setPaintTicks(false);
            slider.setOpaque(false);
            slider.setPaintTrack(true);
            slider.setPreferredSize(new Dimension(100, 1));
            sliderPanel.add(slider);
            this.warePricesPanel.add(sliderPanel, c);

            // Add the price spinner
            c.gridx++;
            final SpinnerModel model = new SpinnerNumberModel(price, ware
                .getMinPrice(), ware.getMaxPrice(), 1);
            final JSpinner spinner = new JSpinner(model);
            final JPanel spinnerPanel = new JPanel(new BorderLayout());
            spinner.setEnabled(used);
            spinner.setPreferredSize(new Dimension(100, 1));
            SwingUtils.installSpinnerBugWorkaround(spinner);
            spinnerPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
            spinnerPanel.setBackground(color);
            spinnerPanel.add(spinner, BorderLayout.CENTER);
            this.warePricesPanel.add(spinnerPanel, c);

            // Focus the spinner if current ware is the active ware
            if (ware.equals(this.activeWare)) focusComponent = spinner;

            // Add the credits label
            c.gridx++;
            final JLabel credits = new JLabel("Cr");
            final JPanel creditsPanel = new JPanel(new BorderLayout());
            creditsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
            credits.setEnabled(used);
            creditsPanel.setBackground(color);
            creditsPanel.add(credits, BorderLayout.CENTER);
            this.warePricesPanel.add(creditsPanel, c);

            // Add the reset button
            c.gridx++;
            final JButton resetButton = new JButton(I18N
                .getString("dialog.changePrices.reset"));
            resetButton.setEnabled(!used || price != ware.getAvgPrice());
            final JPanel buttonPanel = new JPanel(new GridBagLayout());
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            buttonPanel.setBackground(color);
            buttonPanel.add(resetButton);
            this.warePricesPanel.add(buttonPanel, c);

            // Setup events
            slider.addChangeListener((final ChangeEvent e) -> {
                spinner.setValue(slider.getValue());
            });
            spinner.addChangeListener((final ChangeEvent e) -> {
                slider.setValue((Integer) spinner.getValue());
                resetButton.setEnabled(slider.getValue() != ware
                        .getAvgPrice()
                        || !checkBox.isSelected());
                updateCustomWare(ware, checkBox.isSelected(), slider
                        .getValue());
            });
            checkBox.addChangeListener((final ChangeEvent e) -> {
                final boolean enabled1 = checkBox.isSelected();
                slider.setEnabled(enabled1);
                spinner.setEnabled(enabled1);
                credits.setEnabled(enabled1);
                priceLabel.setEnabled(enabled1);
                resetButton.setEnabled(slider.getValue() != ware
                        .getAvgPrice()
                        || !checkBox.isSelected());
                updateCustomWare(ware, checkBox.isSelected(), slider
                        .getValue());
            });
            resetButton.addActionListener((final ActionEvent e) -> {
                checkBox.setSelected(true);
                slider.setValue(ware.getAvgPrice());
                resetButton.setEnabled(slider.getValue() != ware
                        .getAvgPrice()
                        || !checkBox.isSelected());
                updateCustomWare(ware, checkBox.isSelected(), slider
                        .getValue());
            });

            c.gridy++;
        }
        
        c.weighty = 1;
        this.warePricesPanel.add(new Panel(), c);
        validate();

        if (focusComponent != null)
        {
            final int height = this.scrollPane.getHeight();
            focusComponent.scrollRectToVisible(new Rectangle(0, -height / 2
                + focusComponent.getHeight() / 2, 0, height));
            focusComponent.requestFocus();
        }
        else
        {
            this.scrollPane.getVerticalScrollBar().setValue(0);
        }
    }

    /**
     * Updates a custom price
     * 
     * @param ware
     *            The ware
     * @param used
     *            If ware is used
     * @param price
     *            The price
     */
    void updateCustomWare(final Ware ware, final boolean used, final int price)
    {
        if (price == ware.getAvgPrice() && used)
            this.customPrices.remove(ware);
        else
            this.customPrices.put(ware, used ? price : -price);
    }

    /**
     * Releases the content.
     */
    private void releaseContent()
    {
        for (final Component component : this.warePricesPanel.getComponents())
            this.warePricesPanel.remove(component);
    }

    /**
     * Sets the custom prices.
     * 
     * @param customPrices
     *            The custom prices to set
     */
    public void setCustomPrices(final Map<Ware, Integer> customPrices)
    {
        this.customPrices.clear();
        this.customPrices.putAll(customPrices);
    }

    /**
     * Returns the custom prices.
     * 
     * @return The custom prices
     */
    public Map<Ware, Integer> getCustomPrices()
    {
        return Collections.unmodifiableMap(this.customPrices);
    }

    /**
     * Sets the active ware. This is the ware which is focused when the dialog
     * starts. Set it to null to not use a focused ware.
     * 
     * @param ware
     *            The ware to focus (or null for none)
     */
    public void setActiveWare(final Ware ware)
    {
        this.activeWare = ware;
    }
    
    /**
     * @return 
     * @deprecated This method is not supported by this dialog. Use the
     * {@link ChangePricesDialog#open(Complex)} method instead.
     */
    @Override
    @Deprecated
    public Result open()
    {
        throw new UnsupportedOperationException("Use the open(complex) method");
    }

    /**
     * Opens the dialog for the specified complex.
     * 
     * @param complex
     *            The complex. Only the wares used by this complex are
     *            displayed.
     * @return The dialog result.
     */
    public Result open(final Complex complex)
    {
        initContent(complex);
        try
        {
            return super.open();
        }
        finally
        {
            releaseContent();
        }
    }
}

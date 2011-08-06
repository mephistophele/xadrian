/*
 * Copyright (C) 2010 Klaus Reimer <k@ailis.de>
 * See LICENSE.TXT for licensing information
 */

package de.ailis.xadrian.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.ailis.xadrian.data.Sun;
import de.ailis.xadrian.data.factories.SunFactory;
import de.ailis.xadrian.support.I18N;
import de.ailis.xadrian.support.ModalDialog;


/**
 * Dialog for selecting the suns in percent.
 *
 * @author Klaus Reimer (k@ailis.de)
 */

public class ChangeSunsDialog extends ModalDialog
{
    /** Serial version UID */
    private static final long serialVersionUID = 5592052723685985901L;

    /** The singleton instance of this dialog */
    private final static ChangeSunsDialog instance = new ChangeSunsDialog();

    /** The suns combo box */
    private JComboBox sunsComboBox;


    /**
     * Constructor
     */

    private ChangeSunsDialog()
    {
        super("changeSuns", Result.OK, Result.CANCEL);
    }


    /**
     * Creates the UI
     */

    @Override
    protected void createUI()
    {
        // Create the content controls
        final JLabel sunsLabel = new JLabel(I18N
            .getString("dialog.changeSuns.suns"));
        this.sunsComboBox = new JComboBox(SunFactory.getInstance()
            .getSuns().toArray(new Sun[0]));
        sunsLabel.setLabelFor(this.sunsComboBox);

        // Create the content panel
        final JPanel contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
        contentPanel.add(sunsLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        contentPanel.add(this.sunsComboBox);

        // Add the panels to the dialog
        add(contentPanel, BorderLayout.CENTER);
    }


    /**
     * Returns the singleton instance
     *
     * @return The singleton instance
     */

    public static ChangeSunsDialog getInstance()
    {
        return instance;
    }


    /**
     * @see de.ailis.xadrian.support.ModalDialog#open()
     */

    @Override
    public Result open()
    {
        this.sunsComboBox.requestFocus();
        final Result result = super.open();
        return result;
    }


    /**
     * Sets the suns.
     *
     * @param suns
     *            The yield to set
     */

    public void setSuns(final Sun suns)
    {
        this.sunsComboBox.setSelectedItem(suns);
    }


    /**
     * Returns the suns.
     *
     * @return The suns
     */

    public Sun getSuns()
    {
        return (Sun) this.sunsComboBox.getSelectedItem();
    }
}
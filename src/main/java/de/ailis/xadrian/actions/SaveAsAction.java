/*
 * Copyright (C) 2010-2012 Klaus Reimer <k@ailis.de>
 * See LICENSE.TXT for licensing information.
 */
package de.ailis.xadrian.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.ailis.xadrian.components.ComplexEditor;
import de.ailis.xadrian.frames.MainFrame;
import de.ailis.xadrian.listeners.MainStateListener;
import de.ailis.xadrian.resources.Icons;
import de.ailis.xadrian.support.FrameAction;

/**
 * Saves the current file under a new name.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
public class SaveAsAction extends FrameAction<MainFrame> implements MainStateListener
{
    /** Serial version UID */
    private static final long serialVersionUID = -6167535175101165478L;

    /**
     * Constructor
     *
     * @param frame
     *            The frame
     */
    public SaveAsAction(final MainFrame frame)
    {
        super(frame, "saveAs", Icons.SAVEAS);
        this.frame.addStateListener(this);
        setEnabled(false);
    }

    /**
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    @Override
    public void actionPerformed(final ActionEvent e)
    {
        final Component component = this.frame.getCurrentTab();
        if (component instanceof ComplexEditor)
            ((ComplexEditor) component).saveAs();
    }

    /**
     * @see MainStateListener#mainStateChanged(MainFrame)
     */
    @Override
    public void mainStateChanged(final MainFrame sender)
    {
        setEnabled(sender.getTabs().getComponentCount() > 0);
    }
}

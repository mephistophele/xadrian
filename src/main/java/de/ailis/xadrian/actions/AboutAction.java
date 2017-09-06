/*
 * Copyright (C) 2010-2012 Klaus Reimer <k@ailis.de>
 * See LICENSE.TXT for licensing information.
 */
package de.ailis.xadrian.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.ailis.xadrian.frames.MainFrame;
import de.ailis.xadrian.resources.Icons;
import de.ailis.xadrian.support.FrameAction;

/**
 * Displays the about dialog.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
public class AboutAction extends FrameAction<MainFrame>
{
    /** Serial version UID */
    private static final long serialVersionUID = -7386414570191434722L;

    /**
     * Constructor
     *
     * @param frame
     *            The frame
     */
    public AboutAction(final MainFrame frame)
    {
        super(frame, "about", Icons.INFO);
    }

    /**
     * @param e
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    @Override
    public void actionPerformed(final ActionEvent e)
    {
        this.frame.about();
    }
}

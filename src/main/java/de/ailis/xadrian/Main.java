/*
 * Copyright (C) 2010-2012 Klaus Reimer <k@ailis.de>
 * See LICENSE.TXT for licensing information.
 */

package de.ailis.xadrian;

import java.io.File;

import javax.swing.SwingUtilities;

import de.ailis.oneinstance.OneInstance;
import de.ailis.xadrian.data.factories.GameFactory;
import de.ailis.xadrian.dialogs.AboutDialog;
import de.ailis.xadrian.dialogs.ChangeQuantityDialog;
import de.ailis.xadrian.dialogs.OpenComplexDialog;
import de.ailis.xadrian.dialogs.PreferencesDialog;
import de.ailis.xadrian.dialogs.SaveComplexDialog;
import de.ailis.xadrian.dialogs.SelectGameDialog;
import de.ailis.xadrian.frames.MainFrame;
import de.ailis.xadrian.frames.SplashFrame;
import de.ailis.xadrian.support.ErrorHandler;
import de.ailis.xadrian.utils.SwingUtils;

/**
 * Main class.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
public class Main
{
    /** The application name. */
    private static final String APP_NAME = "Xadrian";

    /** The application user model ID (For Windows 7 Taskbar). */
    private static final String APP_USER_MODEL_ID = "Ailis.Xadrian";

    /**
     * Main method.
     *
     * @param args
     *            Command line arguments
     */
    public static void main(final String[] args)
    {
        // If Xadrian is already running then focus the already running
        // Xadrian and pass command line arguments to it. This allows us
        // to open more complexes in the already running Xadrian by
        // double-clicking the *.x3c files.
        final OneInstance oneInstance = OneInstance.getInstance();
        oneInstance.addListener((final File workingDir, final String[] args1) -> {
            SwingUtilities.invokeLater(() -> {
                MainFrame.open(workingDir, args1);
            });
            return false;
        });
        if (!oneInstance.register(Main.class, args)) System.exit(0);

        try
        {
            // Set the App user model ID (needed for Windows)
            SwingUtils.setAppUserModelId(APP_USER_MODEL_ID);

            // Set the application name (needed for Gnome Shell for example)
            SwingUtils.setAppName(APP_NAME);

            // Set theme and locale
            SwingUtils.prepareGUI();

            // Install the error handler
            ErrorHandler.install();

            // Open the splash screen.
            SplashFrame.open();

            // Preload everything
            GameFactory.getInstance().getGames().forEach(game -> {
                game.getAddFactoryDialog();
                game.getChangePricesDialog();
                game.getChangeSunsDialog();
                game.getSelectSectorDialog();
            });
            AboutDialog.getInstance();
            OpenComplexDialog.getInstance();
            SaveComplexDialog.getInstance();
            ChangeQuantityDialog.getInstance();
            PreferencesDialog.getInstance();
            SelectGameDialog.getInstance();           

            // Close the splash screen
            SplashFrame.close();

            // Start the main frame and open the files specified on the
            // command line
            SwingUtilities.invokeLater(() -> {
                MainFrame.start(args);
            });
        }
        catch (final Throwable t)
        {
            ErrorHandler.showError(t);
            System.exit(1);
        }
    }
}

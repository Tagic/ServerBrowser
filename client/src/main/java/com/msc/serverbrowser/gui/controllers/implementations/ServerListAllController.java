package com.msc.serverbrowser.gui.controllers.implementations;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import com.msc.serverbrowser.data.SampServer;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.ServerUtil;

import javafx.application.Platform;
import javafx.scene.control.Label;

/**
 * Controller, that controlls the "All Servers" view.
 *
 * @author Marcel
 */
public class ServerListAllController extends ServerListControllerMain
{
	private Thread serverLookup;

	@Override
	public void initialize()
	{
		super.initialize();

		serverTable.setPlaceholder(new Label("Fetching servers, please wait a moment."));

		serverLookup = new Thread(() ->
		{
			try
			{
				servers.addAll(ServerUtil.retrieveAnnouncedServers());
				Platform.runLater(() -> serverTable.refresh());
			}
			catch (final IOException exception)
			{
				Logging.logger().log(Level.SEVERE, "Couldn't retrieve data from announce api.", exception);
				Platform.runLater(() -> serverTable.setPlaceholder(new Label("Couldn't fetch servers")));
			}

			Platform.runLater(() -> updateGlobalInfo());
		});

		serverLookup.start();
	}

	@Override
	protected void displayMenu(final List<SampServer> selectedServers, final double posX, final double posY)
	{
		super.displayMenu(selectedServers, posX, posY);

		addToFavouritesMenuItem.setVisible(true);
		removeFromFavouritesMenuItem.setVisible(false);
	}

	@Override
	public void onClose()
	{
		super.onClose();
		serverLookup.interrupt();
	}
}

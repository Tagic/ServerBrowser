package com.msc.serverbrowser.gui.controllers.implementations;

import java.io.IOException;
import java.util.logging.Level;

import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.properties.ClientProperties;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.Views;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.logging.Logging;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Controller for the Main view, e.g. the view that contains the menu bar, the header and the loaded
 * view (Settings, Servers ...).
 *
 * @author Marcel
 */
public class MainController implements ViewController
{
	@FXML
	private Parent				rootPane;
	@FXML
	private VBox				commandPane;
	@FXML
	private TextField			commandSearchField;
	@FXML
	private ListView<String>	searchResultsList;

	@FXML
	private StackPane	menuItemFav;
	@FXML
	private StackPane	menuItemAll;
	@FXML
	private StackPane	menuItemUser;
	@FXML
	private StackPane	menuItemVersion;
	@FXML
	private StackPane	menuItemFiles;
	@FXML
	private StackPane	menuItemSettings;

	@FXML
	private ScrollPane	activeViewContainer;
	private Views		activeView;

	@Override
	public void initialize()
	{
		/**
		 * Disable Under Development Features
		 */
		if (!ClientProperties.getPropertyAsBoolean(Property.DEVELOPMENT))
		{
		}

		if (ClientProperties.getPropertyAsBoolean(Property.REMEMBER_LAST_VIEW))
		{
			loadView(Views.valueOf(ClientProperties.getPropertyAsInt(Property.LAST_VIEW)));
		}
		else
		{
			loadView(Views.valueOf(ClientProperties.getDefaultAsInt(Property.LAST_VIEW)));
		}

	}

	@FXML
	private void onServersFavMenuItemClicked()
	{
		loadView(Views.SERVERS_FAV);
	}

	@FXML
	private void onServersAllMenuItemClicked()
	{
		loadView(Views.SERVERS_ALL);
	}

	@FXML
	private void onUsernameMenuItemClicked()
	{
		loadView(Views.USERNAME_CHANGER);
	}

	@FXML
	private void onVersionMenuItemClicked()
	{
		loadView(Views.VERSION_CHANGER);
	}

	@FXML
	private void onFileMenuItemClicked()
	{
		loadView(Views.FILES);
	}

	@FXML
	private void onSettingsMenuItemClicked()
	{
		loadView(Views.SETTINGS);
	}

	private void loadView(final Views view)
	{
		final String CLICKED_STYLE_CLASS = "clickedItem";

		menuItemFav.getStyleClass().remove(CLICKED_STYLE_CLASS);
		menuItemSettings.getStyleClass().remove(CLICKED_STYLE_CLASS);
		menuItemUser.getStyleClass().remove(CLICKED_STYLE_CLASS);
		menuItemAll.getStyleClass().remove(CLICKED_STYLE_CLASS);
		menuItemVersion.getStyleClass().remove(CLICKED_STYLE_CLASS);
		menuItemFiles.getStyleClass().remove(CLICKED_STYLE_CLASS);

		switch (view)
		{
			case VERSION_CHANGER:
			{
				menuItemVersion.getStyleClass().add(CLICKED_STYLE_CLASS);
				break;
			}
			case USERNAME_CHANGER:
			{
				menuItemUser.getStyleClass().add(CLICKED_STYLE_CLASS);
				break;
			}
			case SETTINGS:
			{
				menuItemSettings.getStyleClass().add(CLICKED_STYLE_CLASS);
				break;
			}
			case SERVERS_FAV:
			{
				menuItemFav.getStyleClass().add(CLICKED_STYLE_CLASS);
				break;
			}
			case SERVERS_ALL:
			{
				menuItemAll.getStyleClass().add(CLICKED_STYLE_CLASS);
				break;
			}
			case FILES:
				menuItemFiles.getStyleClass().add(CLICKED_STYLE_CLASS);
				break;
		}

		loadFXML(view);
		activeView = view;
	}

	private void loadFXML(final Views view)
	{
		try
		{
			final FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource(view.getFXMLPath()));
			loader.setController(view.getControllerType().newInstance());
			final Parent toLoad = loader.load();
			toLoad.getStylesheets().setAll(view.getStylesheetPath());
			activeViewContainer.setContent(toLoad);
			Client.getInstance().setTitle(Client.APPLICATION_NAME + " - " + view.getTitle());
		}
		catch (final IOException | InstantiationException | IllegalAccessException exception)
		{
			Logging.logger().log(Level.SEVERE, "Couldn't load view.", exception);
		}
	}

	@Override
	public void onClose()
	{
		ClientProperties.setProperty(Property.LAST_VIEW, activeView.getId());
		System.exit(0); // Make sure that the application doesnt stay open for some reason
	}
}

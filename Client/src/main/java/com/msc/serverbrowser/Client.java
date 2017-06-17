package com.msc.serverbrowser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;

import com.msc.sampbrowser.entities.SampServer;
import com.msc.sampbrowser.interfaces.DataServiceInterface;
import com.msc.sampbrowser.interfaces.UpdateServiceInterface;
import com.msc.sampbrowser.util.Hashing;
import com.msc.serverbrowser.data.Favourites;
import com.msc.serverbrowser.data.PastUsernames;
import com.msc.serverbrowser.data.properties.ClientProperties;
import com.msc.serverbrowser.data.properties.PropertyIds;
import com.msc.serverbrowser.data.rmi.CustomRMIClientSocketFactory;
import com.msc.serverbrowser.gui.controllers.implementations.MainController;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.FileUtility;
import com.msc.serverbrowser.util.windows.OSUtil;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Client extends Application
{
	private static Boolean sendStatistics = true;

	/**
	 * Default public IP, can be changed on startup using <code>-s</code> / <code>-server</code>
	 * followed by a domain, IP or hostname.
	 */
	private static String serverToConnectTo = "164.132.193.101";

	/**
	 * Application icon that can be used everywhere where necessary.
	 */
	public static final Image	APPLICATION_ICON	= new Image(Client.class.getResourceAsStream("/com/msc/serverbrowser/icons/icon.png"));
	public static final String	APPLICATION_NAME	= "SA-MP Client Extension";

	public static Registry registry;

	public static DataServiceInterface		remoteDataService;
	public static UpdateServiceInterface	remoteUpdateService;

	private Stage stage;

	private static Client instance;

	public static Client getInstance()
	{
		return instance;
	}

	@Override
	public void start(final Stage primaryStage)
	{
		instance = this;
		checkOperatingSystemCompatibility();
		initClient();
		loadUI(primaryStage);
		establishConnection();

		new Thread(() -> checkVersion()).start();
	}

	/*
	 * + Establishes the connection with the rmi server.
	 */
	private void establishConnection()
	{
		try
		{
			registry = LocateRegistry.getRegistry(serverToConnectTo, 1099, new CustomRMIClientSocketFactory());
			remoteDataService = (DataServiceInterface) registry.lookup(DataServiceInterface.INTERFACE_NAME);
			remoteUpdateService = (UpdateServiceInterface) registry.lookup(UpdateServiceInterface.INTERFACE_NAME);

			if (sendStatistics)
			{
				System.out.println("Sending statistic");
				remoteDataService.tellServerThatYouUseTheApp(Locale.getDefault().toString());
			}
		}
		catch (RemoteException | NotBoundException exception)
		{
			Logging.logger.log(Level.SEVERE, "Couldn't connect to RMI Server.", exception);
			displayNoConnectionDialog();
		}
	}

	/**
	 * @return {@link #stage}
	 */
	public Stage getStage()
	{
		return stage;
	}

	/**
	 * Loads the main UI.
	 *
	 * @param primaryStage
	 *            the stage to use for displaying the UI
	 */
	private void loadUI(final Stage primaryStage)
	{
		final FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/com/msc/serverbrowser/views/Main.fxml"));
		final MainController controller = new MainController();
		loader.setController(controller);
		try
		{
			final Parent root = loader.load();
			final Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/com/msc/serverbrowser/views/stylesheets/mainStyle.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.getIcons().add(APPLICATION_ICON);
			primaryStage.setTitle(APPLICATION_NAME);
			primaryStage.show();
			primaryStage.setMinWidth(primaryStage.getWidth());
			primaryStage.setMinHeight(primaryStage.getHeight());
			primaryStage.setMaximized(ClientProperties.getPropertyAsBoolean(PropertyIds.MAXIMIZED));
			primaryStage.setFullScreen(ClientProperties.getPropertyAsBoolean(PropertyIds.FULLSCREEN));
			primaryStage.setOnCloseRequest(close ->
			{
				controller.onClose();
				ClientProperties.setProperty(PropertyIds.MAXIMIZED, primaryStage.isMaximized());
				ClientProperties.setProperty(PropertyIds.FULLSCREEN, primaryStage.isFullScreen());
			});

			stage = primaryStage;

			if (ClientProperties.getPropertyAsBoolean(PropertyIds.SHOW_CHANGELOG))
			{
				final Alert alert = new Alert(AlertType.INFORMATION);
				setAlertIcon(alert);
				alert.initOwner(stage);
				alert.initModality(Modality.APPLICATION_MODAL);
				alert.setTitle(APPLICATION_NAME);
				alert.setHeaderText("Your client has been updated");

				final StringBuilder updateText = new StringBuilder();
				updateText.append("- Minor Refactoring of Code");
				updateText.append(System.lineSeparator());
				updateText.append("- You can connect to custom servers using -s / -server followed by an address on startup arguments");
				updateText.append(System.lineSeparator());

				alert.setContentText(updateText.toString());
				alert.show();
				ClientProperties.setProperty(PropertyIds.SHOW_CHANGELOG, false);
			}
		}
		catch (final Exception e)
		{
			Logging.logger.log(Level.SEVERE, "Couldn't load UI", e);
			System.exit(0);
		}
	}

	/**
	 * Checks if the operating system is windows, if not, the application will shutdown.
	 */
	private void checkOperatingSystemCompatibility()
	{
		if (!OSUtil.isWindows())
		{
			final Alert alert = new Alert(AlertType.WARNING);
			setAlertIcon(alert);
			alert.setTitle("Launching Application");
			alert.setHeaderText("Operating System not supported");
			alert.setContentText("You seem to be not using windows, sorry, but this application does not support other systems than Windows.");
			alert.showAndWait();
			System.exit(0);
		}
	}

	public void displayNoConnectionDialog()
	{
		final Alert alert = new Alert(AlertType.ERROR);
		setAlertIcon(alert);
		alert.initOwner(stage);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.setTitle("Connecting to server");
		alert.setHeaderText("Server connection could not be established");
		alert.setContentText("The server connection doesn't seeem to be established, try again later, for more information check the log files.");
		alert.showAndWait();
	}

	public static void setAlertIcon(final Alert alert)
	{
		((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(APPLICATION_ICON);
	}

	/**
	 * Creates files and folders that are necessary for the application to run properly and migrates
	 * old xml data.
	 */
	private void initClient()
	{
		File file = new File(System.getProperty("user.home") + File.separator + "sampex");

		if (!file.exists())
		{
			file.mkdir();
		}

		file = new File(System.getProperty("user.home") + File.separator + "sampex" + File.separator + "favourites.xml");

		// Migration from XML to SQLLite
		if (file.exists())
		{
			for (final SampServer server : Favourites.getFavouritesFromXML())
			{
				Favourites.addServerToFavourites(server);
			}
			file.delete();
		}

		file = new File(System.getProperty("user.home") + File.separator + "sampex" + File.separator + "pastusernames.xml");

		if (file.exists())
		{
			for (final String username : PastUsernames.getPastUsernamesFromXML())
			{
				PastUsernames.addPastUsername(username);
			}
			file.delete();
		}
	}

	/**
	 * Compares the local version number to the one lying on the server. If an update is availbable
	 * the user will be asked if he wants to update.
	 */
	private void checkVersion()
	{
		if (Objects.nonNull(remoteDataService))
		{// Connection with server was not sucessful
			try
			{
				final String localVersion = Hashing.verifyChecksum(getOwnJarFile().toString());
				final String remoteVersion = remoteUpdateService.getLatestVersionChecksum();

				if (!localVersion.equals(remoteVersion))
				{
					final Alert alert = new Alert(AlertType.CONFIRMATION);
					setAlertIcon(alert);
					alert.setTitle("Launching Application");
					alert.setHeaderText("Update required");
					alert.setContentText("The launcher needs an update. Not updating the client might lead to problems. Click 'OK' to update and 'Cancel' to not update.");

					alert.showAndWait().ifPresent(result ->
					{
						if (result == ButtonType.OK)
						{
							updateLauncher();
						}
					});
				}
			}
			catch (final FileNotFoundException notFound)
			{
				Logging.logger.log(Level.INFO, "Couldn't retrieve Update Info, the client is most likely being run in an ide.");
			}
			catch (final NoSuchAlgorithmException nonExistentAlgorithm)
			{
				Logging.logger.log(Level.INFO, "The used Hashing-Algorithm doesan't exist.", nonExistentAlgorithm);
			}
			catch (final IOException updateException)
			{
				Logging.logger.log(Level.SEVERE, "Couldn't retrieve Update Info.", updateException);
			}
		}
	}

	/**
	 * Downloads the latest version and restarts the client.
	 */
	private void updateLauncher()
	{
		try
		{
			final URI url = new URI(remoteUpdateService.getLatestVersionURL());
			FileUtility.downloadFile(url.toString(), getOwnJarFile().getPath().toString());
			ClientProperties.setProperty(PropertyIds.SHOW_CHANGELOG, true);
			selfRestart();
		}
		catch (final IOException | URISyntaxException exception)
		{
			Logging.logger.log(Level.SEVERE, "Couldn't retrieve update.", exception);
		}
	}

	/**
	 * @return a File pointing to the applications own jar file
	 */
	private File getOwnJarFile()
	{
		return new File(System.getProperty("java.class.path")).getAbsoluteFile();
	}

	/**
	 * Restarts the application.
	 */
	private void selfRestart()
	{
		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		final File currentJar = getOwnJarFile();

		if (!currentJar.getName().endsWith(".jar"))
		{// The application wasn't run with a jar file, but in an ide
			return;
		}

		final ArrayList<String> command = new ArrayList<>();
		command.add(javaBin);
		command.add("-jar");
		command.add(currentJar.getPath());

		final ProcessBuilder builder = new ProcessBuilder(command);

		try
		{
			builder.start();
			System.exit(0);
		}
		catch (final IOException e)
		{
			Logging.logger.log(Level.SEVERE, "Couldn't selfrestart.", e);
		}
	}

	public static void main(final String[] args)
	{
		if (args.length >= 2)
		{
			for (int i = 0; i < args.length; i++)
			{
				final String arg = args[i];
				if (arg.equals("-s") || arg.equals("-server"))
				{
					serverToConnectTo = args[i + 1];
				}
				else if (arg.equals("-nostatistic"))
				{
					sendStatistics = false;
				}
			}
		}

		Application.launch(args);
	}
}
package main;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.tika.Tika;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import slider_material.JsliderCustom;

public class CPlayer extends JFrame implements ActionListener, Runnable {

	private JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

	private JPanel tracksPanel = new JPanel();

	private ListCellRenderer<? super Object> defaultCellRenderer = null;
	private CheckListRenderer checkListRenderer = new CheckListRenderer();

	private JList<Object> tracksList = new JList<>(new DefaultListModel());
	private JScrollPane scrollPane = new JScrollPane(tracksList);

	private MouseAdapter mouseAdapter = null;

	private JPopupMenu popupMenuTracks = new JPopupMenu();

	private JButton removeButton = new JButton("Remove");
	private JButton cancelButton = new JButton("Cancel");

	private JPanel mainPanel = new JPanel();

	private JPanel buttonPanel = new JPanel();

	private RoundButton trackBackButton = new RoundButton();
	private RoundButton trackForwardButton = new RoundButton();

	private RoundButton mainButton = new RoundButton();

	private static Media media;
	private static MediaPlayer player;

	private boolean autoPlaySelected = true;

	private Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);

	private JsliderCustom songSlider = new JsliderCustom(false);

	private JLabel songCurrentTime = new JLabel("0:00");
	private JLabel songTotalTime = new JLabel("0:00");

	private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private boolean check = false;

	private JsliderCustom soundSlider = new JsliderCustom(true); // soundSlider from an external jar file

	private JLabel autoPlayLabel = new JLabel();

	private JLabel soundLabel = new JLabel();

	private BufferedImage soundImageMinimum = getBufferedImageIcon("src\\resources\\soundMinimum.png");
	private BufferedImage soundImageMedium = getBufferedImageIcon("src\\resources\\soundMedium.png");
	private BufferedImage soundImageMaximum = getBufferedImageIcon("src\\resources\\soundMaximum.png");
	private BufferedImage soundImageNone = getBufferedImageIcon("src\\resources\\soundNone.png");

	private CustomizedIcon soundIconMinimum = new CustomizedIcon(30, 30, soundImageMinimum);
	private CustomizedIcon soundIconMedium = new CustomizedIcon(30, 30, soundImageMedium);
	private CustomizedIcon soundIconMaximum = new CustomizedIcon(30, 30, soundImageMaximum);
	private CustomizedIcon soundIconNone = new CustomizedIcon(30, 30, soundImageNone);

	private boolean leftClickOnPopopMenuTracks = false;

	public CPlayer() throws IOException, LineUnavailableException, UnsupportedAudioFileException {

		setSize(700, 500);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(null);
		setResizable(false);
		setTitle("C PLAYER");

		readSongPathsFromDatabase();

		tracksPanel.setLayout(new GridLayout());
		tracksPanel.add(scrollPane);

		JLabel label = new JLabel("Tracks");
		label.setHorizontalTextPosition(JLabel.CENTER);
		label.setPreferredSize(new Dimension(175, 25));

		tabbedPane.addTab("Tracks", null, tracksPanel, "Right click to edit tracks");
		tabbedPane.setTabComponentAt(0, label);

		tabbedPane.setBounds(0, 0, 200, 430);
		tabbedPane.setFont(new Font("Arial", Font.CENTER_BASELINE, 20));

		tabbedPane.setBackground(Color.WHITE);

		JMenuItem menuItem1 = new JMenuItem("Add");
		menuItem1.setToolTipText(
				"For multiple song selection, choose this option and then hold CTRL and select songs you want to add.");

		JMenuItem menuItem2 = new JMenuItem("Remove");
		JMenuItem menuItem3 = new JMenuItem("Clear");

		menuItem1.addActionListener(CPlayer.this);
		menuItem2.addActionListener(CPlayer.this);
		menuItem3.addActionListener(CPlayer.this);

		popupMenuTracks.add(menuItem1);
		popupMenuTracks.add(menuItem2);
		popupMenuTracks.add(menuItem3);

		MouseAdapter menuItemMouseAdapterSongs = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent event) { // entered
				if (SwingUtilities.isLeftMouseButton(event)) { // making sure the jmenuitems can be selected only with
																// left click
					leftClickOnPopopMenuTracks = true;
				}
			}
		};

		menuItem1.addMouseListener(menuItemMouseAdapterSongs);
		menuItem2.addMouseListener(menuItemMouseAdapterSongs);
		menuItem3.addMouseListener(menuItemMouseAdapterSongs);

		tabbedPane.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {

				if (SwingUtilities.isRightMouseButton(e)) {

					popupMenuTracks.show(tabbedPane, e.getX(), e.getY());

				}
			}
		});

		tracksList.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent event) {

				if (event.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(event)

						&& tracksList.getSelectedValue() instanceof Song) { // playing the song on double click

					try {

						if (timer.isRunning() == false && player != null) {

							player.stop();
							player = new MediaPlayer(media);

							player.setStartTime(Duration.seconds(0));
							playing = false;
							playSong();
						}

						else if (timer.isRunning() == true && player != null) {

							player.stop();
							player = new MediaPlayer(media);

							player.setStartTime(Duration.seconds(0));
							playing = false;
							playSong();
						}

						else if (player == null) {
							playing = false;
							playSong();
						}

					} catch (Exception ex) {
						ex.printStackTrace();
					}

				}

			}

		});

		removeButton.setBounds(0, 430, 100, 35);
		removeButton.setEnabled(false);

		removeButton.addActionListener(new ActionListener() { // here we are working with CheckListItem values
																// not Song values

			@Override
			public void actionPerformed(ActionEvent e) {

				Song song = null;
				int indexOfSong = 0;

				if (playing == true) { // if true then one song is currently playing and should not be removed

					song = getCurrentSongPlaying(); // this is the song that is currently being played and needs
													// to stay in the list

				}

				DefaultListModel listModel = new DefaultListModel();

				List<CheckListItem> listValues = new ArrayList<>(); // store here all values from the list

				CheckListItem listItem;

				for (int i = 0; i < tracksList.getModel().getSize(); i++) {
					listItem = (CheckListItem) tracksList.getModel().getElementAt(i);
					listValues.add(listItem);

					if (listItem.getSong().equals(song)) {
						indexOfSong = i;
					}
				}

				var list = listValues.stream().filter(checkListItem -> !checkListItem.isSelected())
						.map(checkListItem -> checkListItem.getSong()).collect(Collectors.toList()); // leave
																										// the
																										// ones
																										// that
																										// are
																										// not
																										// selected
																										// and
																										// return
																										// them
																										// as
																										// Song
																										// objects

				// add/leave in the list the song that is currently being played
				if (song != null && list.contains(song) == false) {

					if (list.size() == 0) {
						list.add(0, song);
					} else {
						list.add(indexOfSong, song);
					}
				}

				tracksList.removeMouseListener(mouseAdapter);
				tracksList.setCellRenderer(defaultCellRenderer);
				tracksList.setModel(listModel);
				tracksList.setListData(list.toArray());

				removeButton.setEnabled(false);
				cancelButton.setEnabled(false);

				if (playing == true || currentTime != 0.0 && player != null) {
					tracksList.setSelectedIndex(getIndexOfCurrentSongPlaying());
				}

				if (playing == false) {
					media = null;
					player = null;
					songCurrentTime.setText("0:00");
					songTotalTime.setText("0:00");
					songSlider.setValue(0);
				}

			}
		});

		cancelButton.setEnabled(false);
		cancelButton.setBounds(100, 430, 100, 35);

		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				System.out.println("cancelButton clicked");
				DefaultListModel listModel = new DefaultListModel();

				ArrayList<Song> myList = new ArrayList<>();

				for (int i = 0; i < tracksList.getModel().getSize(); i++) {

					myList.add(((CheckListItem) tracksList.getModel().getElementAt(i)).getSong());

				}

				tracksList.removeMouseListener(mouseAdapter);
				tracksList.setCellRenderer(defaultCellRenderer);
				tracksList.setModel(listModel);
				tracksList.setListData(myList.toArray());

				cancelButton.setEnabled(false);
				removeButton.setEnabled(false);

				tracksList.setSelectedIndex(selectedIndex);
				System.out.println("selected index set: " + selectedIndex);

				if (playing == true || currentTime != 0.0 && player != null) {
					tracksList.setSelectedIndex(getIndexOfCurrentSongPlaying());
				}
			}
		});

		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		mainPanel.setBounds(200, 0, 490, 380);

		BufferedImage bufferedImage = getBufferedImageIcon("src\\resources\\musicBackground4.png");
		CustomizedIcon musicIcon = new CustomizedIcon(490, 380, bufferedImage);

		JLabel iconLabel = new JLabel(musicIcon);

		buttonPanel.setBounds(200, 380, 490, 83);
		buttonPanel.setBackground(Color.WHITE);
		buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

		mainButton.setBounds(420, 398, 43, 43);

		MouseAdapter changeSizeOfButtonWhenClicked = new MouseAdapter() { // change size of buttons when pressed

			public void mousePressed(MouseEvent e) {

				if (e.getSource() == mainButton) {

					mainButton.setSize(mainButton.getWidth() - 2, mainButton.getHeight() - 2);

				} else if (e.getSource() == trackBackButton) {

					trackBackButton.setSize(trackBackButton.getWidth() - 2, trackBackButton.getHeight() - 2);

				} else if (e.getSource() == trackForwardButton) {

					trackForwardButton.setSize(trackForwardButton.getWidth() - 2, trackForwardButton.getHeight() - 2);

				}

			}

			public void mouseReleased(MouseEvent e) {

				if (e.getSource() == mainButton) {

					mainButton.setSize(mainButton.getWidth() + 2, mainButton.getHeight() + 2);

				} else if (e.getSource() == trackBackButton) {

					trackBackButton.setSize(trackBackButton.getWidth() + 2, trackBackButton.getHeight() + 2);

				} else if (e.getSource() == trackForwardButton) {

					trackForwardButton.setSize(trackForwardButton.getWidth() + 2, trackForwardButton.getHeight() + 2);
				}
			}
		};

		mainButton.addMouseListener(changeSizeOfButtonWhenClicked);

		ActionListener mainButtonListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				try {

					if (tracksList.getSelectedValue() instanceof Song) {
						playSong();
					}

				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		};

		mainButton.addActionListener(mainButtonListener);

		ActionListener trackBackListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (playing == true && !removeButton.isEnabled()) {

					int indexOfCurrentSong = getIndexOfCurrentSongPlaying();
					System.out.println("INDEX OF CURRENT SONG: " + indexOfCurrentSong);
					int listSize = tracksList.getModel().getSize();

					if (indexOfCurrentSong == 0) {
						System.out.println("can not put track back cause this is the first song in the list");
					} else if (indexOfCurrentSong < listSize) {

						Song songToBePlayed = (Song) tracksList.getModel().getElementAt(indexOfCurrentSong - 1);
						File myFile = songToBePlayed.getFile();

						try {
							player.stop();

							media = new Media(myFile.toURI().toURL().toExternalForm());
							player = new MediaPlayer(media);

							playing = false;

							tracksList.setSelectedIndex(indexOfCurrentSong - 1);

							playSong();

						} catch (MalformedURLException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}

					}

				} else if (playing == false) {

					int indexOfSelectedSong = tracksList.getSelectedIndex();

					int listSize = tracksList.getModel().getSize();

					if (indexOfSelectedSong != -1 && listSize > 1 && indexOfSelectedSong != 0) {

						tracksList.setSelectedIndex(indexOfSelectedSong - 1);
						songSlider.setValue(0);

						songCurrentTime.setText("0:00");
						songTotalTime.setText("0:00");

						media = null;
						player = null;
					}

				}

			}

		};

		trackBackButton.addActionListener(trackBackListener);
		trackBackButton.addMouseListener(changeSizeOfButtonWhenClicked);
		trackBackButton.setBounds(365, 400, 38, 38);

		ActionListener trackForwardListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (playing == true && !removeButton.isEnabled()) {

					int indexOfCurrentSong = getIndexOfCurrentSongPlaying();

					int listSize = tracksList.getModel().getSize();

					if (indexOfCurrentSong < listSize - 1) {

						Song songToBePlayed = (Song) tracksList.getModel().getElementAt(indexOfCurrentSong + 1);
						File myFile = songToBePlayed.getFile();

						try {
							player.stop();

							media = new Media(myFile.toURI().toURL().toExternalForm());
							player = new MediaPlayer(media);

							playing = false;

							tracksList.setSelectedIndex(indexOfCurrentSong + 1);

							playSong();

						} catch (MalformedURLException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}

					}

				} else if (playing == false) {

					int indexOfSelectedSong = tracksList.getSelectedIndex();

					int listSize = tracksList.getModel().getSize();

					if (indexOfSelectedSong != -1 && listSize > 1 && indexOfSelectedSong != listSize - 1) {

						System.out.println("setting track forward!");

						tracksList.setSelectedIndex(indexOfSelectedSong + 1);

						songSlider.setValue(0);

						songCurrentTime.setText("0:00");
						songTotalTime.setText("0:00");

						media = null;
						player = null;

					}

				}

			}

		};

		trackForwardButton.addActionListener(trackForwardListener);
		trackForwardButton.addMouseListener(changeSizeOfButtonWhenClicked);
		trackForwardButton.setBounds(480, 400, 38, 38);

		BufferedImage mainButtonPlayImage = getBufferedImageIcon("src\\resources\\playButton.png");
		mainButton.setImage(mainButtonPlayImage);

		BufferedImage previousButtonImage = getBufferedImageIcon("src\\resources\\previousButton.png");
		trackBackButton.setImage(previousButtonImage);

		BufferedImage nextButtonImage = getBufferedImageIcon("src\\resources\\nextButton.png");
		trackForwardButton.setImage(nextButtonImage);

		BufferedImage autoPlayOnImage = getBufferedImageIcon("src\\resources\\autoPlayOn.png");
		CustomizedIcon autoPlayOnIcon = new CustomizedIcon(40, 40, autoPlayOnImage);

		BufferedImage autoPlayOffImage = getBufferedImageIcon("src\\resources\\autoPlayOff.png");
		CustomizedIcon autoPlayOffIcon = new CustomizedIcon(40, 40, autoPlayOffImage);

		autoPlayLabel.setIcon(autoPlayOnIcon);
		autoPlayLabel.setText("Autoplay");

		autoPlayLabel.setBounds(560, 425, autoPlayOnIcon.getIconWidth() + 65, autoPlayOnIcon.getIconHeight());

		autoPlayLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {

				if (autoPlaySelected == true) {
					autoPlaySelected = false;

					autoPlayLabel.setIcon(autoPlayOffIcon);

				} else if (autoPlaySelected == false) {
					autoPlaySelected = true;

					autoPlayLabel.setIcon(autoPlayOnIcon);
				}

			}
		});

		autoPlayLabel.setCursor(handCursor);
		trackForwardButton.setCursor(handCursor);
		trackBackButton.setCursor(handCursor);
		mainButton.setCursor(handCursor);

		songSlider.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {

				if (timer.isRunning() == true && !removeButton.isEnabled()) {
					check = false;
					playing = false; 

					if (player != null) {
						player.stop();
						timer.stop();
					}
				} else if (timer.isRunning() == false) {

					check = true;

				}

			}

			@Override
			public void mouseReleased(MouseEvent e) {

				if (playing == false && check == false && !removeButton.isEnabled()) {

					if (player != null) {

						try {
							player = null;

							player = new MediaPlayer(media);

							double value = songSlider.getValue();

							currentTime = value; // new Media and MediaPlayer objects will not be created in the
													// playSong() method

							player.setStartTime(Duration.seconds(value));

							playSong();

						} catch (IOException | InterruptedException e1) {
							e1.printStackTrace();
						}

					}

				}
				if (check == true && !removeButton.isEnabled()) {

					if (player != null) {

						player = null;
						player = new MediaPlayer(media);

						double value = songSlider.getValue();

						currentTime = value;

						player.setStartTime(Duration.seconds(value));

						String time = getSongTimeAsString((int) value);
						songCurrentTime.setText(time);

					}
				}

			}

		});

		soundSlider.setBounds(240, 435, 100, 20);

		soundSlider.setBackground(new java.awt.Color(241, 79, 107));
		soundSlider.setForeground(new java.awt.Color(115, 118, 120));

		add(soundSlider);

		soundSlider.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {

				double sliderValue = soundSlider.getValue();

				double volume = sliderValue / 100;

				if (player != null) {

					player.setVolume(volume);
					System.out.println("volume is " + volume);
				}

				int soundSliderValue = (int) sliderValue;

				System.out.println("sliderr value is : " + soundSliderValue);

				if (soundSliderValue <= 30 && soundSliderValue > 0) {
					
					soundLabel.setIcon(soundIconMinimum);
					soundLabelClicked = false;
					
				} 
				else if (soundSliderValue > 30 && soundSliderValue <= 60) {
					
					soundLabel.setIcon(soundIconMedium);
					soundLabelClicked = false;
					
				} 
				else if (soundSliderValue > 60) {
					
					soundLabel.setIcon(soundIconMaximum);
					soundLabelClicked = false;
					
				} 
				else if (soundSliderValue == 0) {
					
					soundLabel.setIcon(soundIconNone);
				}

			}

		});

		soundLabel.setBounds(205, 430, 30, 30);
		soundLabel.setCursor(handCursor);

		soundLabel.setIcon(soundIconMaximum);

		soundLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {

				if (soundLabelClicked == false) {
					soundLabelClicked = true;

					System.out.println("SETTING VOLUME TO 0 !!!");

					oldVolumeValue = (double) soundSlider.getValue();

					if (player != null) {
						player.setVolume(0.0);
					}

					soundSlider.setValue(0);

					soundLabel.setIcon(soundIconNone);
				}

				else if (soundLabelClicked == true) {
					soundLabelClicked = false;

					int soundSliderValue = (int) oldVolumeValue;

					if (player != null) {
						player.setVolume(oldVolumeValue / 100);
					}

					soundSlider.setValue((int) oldVolumeValue);

					System.out.println("setting volume back again at: " + (int) oldVolumeValue);

					if (soundSliderValue <= 30) {
						
						soundLabel.setIcon(soundIconMinimum);
						
					} 
					else if (soundSliderValue > 30 && soundSliderValue <= 60) {
						
						soundLabel.setIcon(soundIconMedium);
						
					} 
					else if (soundSliderValue > 60) {
						
						soundLabel.setIcon(soundIconMaximum);
					}
				}

			}
		});

		songSlider.setBounds(330, 345, 230, 20);
		songCurrentTime.setBounds(290, 345, 50, 15);
		songTotalTime.setBounds(560, 345, 50, 15);

		add(soundLabel);
		add(tabbedPane);
		add(cancelButton);
		add(removeButton);
		add(autoPlayLabel);
		add(trackBackButton);
		add(mainButton);
		add(trackForwardButton);
		add(buttonPanel);
		add(songCurrentTime);
		add(songTotalTime);
		add(songSlider);

		setVisible(true);

		soundSlider.setValue(100);
		songSlider.setValue(0);

		mainPanel.add(iconLabel);
		add(mainPanel);

		Runtime.getRuntime().addShutdownHook(closingThread); // saving songs to the database before closing the program

	}

	Thread closingThread = new Thread() {

		@Override
		public void run() {

			System.out.println("SHUTTING DOWN");

			String createTable = "CREATE TABLE songs (PATH VARCHAR(400))";

			boolean databaseExists = false;
			boolean gotSQLexc = false;

			try (Connection connection = DriverManager.getConnection("jdbc:derby:songs;create=true"); // creating
																										// connection
					Statement statement = connection.createStatement()) {

				DatabaseMetaData dbmd = connection.getMetaData();
				ResultSet result = dbmd.getTables(null, null, "%", null);

				while (result.next()) { // checking if the database exists

					String databaseName = result.getString(3);

					if (databaseName.equalsIgnoreCase("songs")) {
						databaseExists = true;
						System.out.println("DATABASE EXISTS");
					}

				}

				if (databaseExists == false) {
					System.out.println("create table : " + statement.executeUpdate(createTable)); // if not create table
				}

				else if (databaseExists == true) { // if exists, input the song paths

					statement.execute("DELETE from songs");

					ArrayList<Song> songsToStore = new ArrayList<>();

					for (int i = 0; i < tracksList.getModel().getSize(); i++) {

						Object undefinedObjectType = tracksList.getModel().getElementAt(i); // is either Song or
																							// CheckListItem object
						Song song = null;

						if (undefinedObjectType instanceof Song) {
							song = (Song) undefinedObjectType;
						} else if (undefinedObjectType instanceof CheckListItem) {
							song = ((CheckListItem) undefinedObjectType).getSong();
						}

						songsToStore.add(song);

					}

					for (int counter = 0; counter < songsToStore.size(); counter++) {

						String songPath = songsToStore.get(counter).getFullPathAsString();

						PreparedStatement statement2 = connection.prepareStatement("INSERT into songs VALUES(?)");
						System.out.println("INSERTING INTO DATABASE : " + songPath);
						statement2.setString(1, songPath);
						statement2.executeUpdate();

					}

				}

				DriverManager.getConnection("jdbc:derby:;shutdown=true"); // closing the database
				result.close();

			} catch (SQLException ex) {

				if (ex.getSQLState().equals("XJ015") || ex.getSQLState().equals("08006")) {

					System.out.println("sql state is : " + ex.getSQLState()); // confirmation that the database is
																				// closed

					gotSQLexc = true;

				}
				// ex.printStackTrace(); // uncomment only when checking the database close state
				
			}

			if (!gotSQLexc) {
				System.out.println("Database did not shut down normally");
			} else {
				System.out.println("Database shut down normally");
			}

		}

	};

	private void readSongPathsFromDatabase() {

		System.out.println("READING FROM DATABASE");

		boolean databaseExists = false;
		boolean gotSQLexc = false;

		try (Connection connection = DriverManager.getConnection("jdbc:derby:songs;create=true")) {

			DatabaseMetaData dbmd = connection.getMetaData();
			ResultSet result = dbmd.getTables(null, null, "%", null);

			while (result.next()) { // checking if the database exists

				String databaseName = result.getString(3);

				if (databaseName.equalsIgnoreCase("songs")) {
					databaseExists = true;
					System.out.println("DATABASE EXISTS");
				}

			}

			if (databaseExists == true) {

				PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM songs");

				ResultSet resultPath = preparedStatement.executeQuery();

				ArrayList<Song> songList = new ArrayList<>();

				while (resultPath.next()) {
					String songPath = resultPath.getString(1);

					Song song = new Song(songPath);
					songList.add(song);

					System.out.println("ADDING SONG TO THE LIST : " + songPath);
				}
				resultPath.close();

				defaultCellRenderer = tracksList.getCellRenderer();
				tracksList.setCellRenderer(new SongWithIconRenderer());
				tracksList.setListData(songList.toArray());

			}

		} catch (Exception ex) {

			 ex.printStackTrace(); 

		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (tabbedPane.getSelectedIndex() == 0) { // checking if the user has right clicked on tracks tab

			if (e.getActionCommand() == "Clear" && leftClickOnPopopMenuTracks == true) {
				clearTracks();
				leftClickOnPopopMenuTracks = false;
			}

			else if (e.getActionCommand() == "Add" && leftClickOnPopopMenuTracks == true) {
				addTracks();
				leftClickOnPopopMenuTracks = false;
			}

			else if (e.getActionCommand() == "Remove" && leftClickOnPopopMenuTracks == true) {
				removeTracks();
				leftClickOnPopopMenuTracks = false;
			}

		}

	}

	boolean soundLabelClicked = false; // used for soundLabel - MouseListener above
	double oldVolumeValue = 0; // used for soundLabel - MouseListener above

	protected static BufferedImage getBufferedImageIcon(String iconPath) {

		BufferedImage bm = null;
		BufferedImage bim = null;
		try {
			File file = new File(iconPath);

			bm = ImageIO.read(file);
			bim = new BufferedImage(bm.getWidth(), bm.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = bim.createGraphics();
			graphics.drawImage(bm, 0, 0, null);
			graphics.dispose();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return bim;
	}

	boolean playing = false;
	double currentTime;

	int selectedIndex; // this variable is used in removeTracks() and cancelButton() to keep track of
						// the current selected song

	Timer timer = new Timer(0, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {

			if (playing == true) {

				currentTime = player.getCurrentTime().toSeconds();

				int currentTimeAsInt = (int) currentTime;

				songSlider.setValue(currentTimeAsInt); // updating the slider value while the song is playing

				// updating the current song time on the GUI while the song is playing

				String time = getSongTimeAsString(currentTimeAsInt);

				songCurrentTime.setText(time);

			}

		}

	});

	private String getSongTimeAsString(int timeInSeconds) {

		int totalTime = timeInSeconds;

		int minutes = 0;
		int seconds = 0;

		while (totalTime >= 60) {
			minutes += 1;
			totalTime -= 60;
		}

		seconds += totalTime;

		String secondsString = String.valueOf(seconds);
		String minutesString = String.valueOf(minutes);

		if (secondsString.length() == 1) {
			secondsString = "0" + secondsString;
		}

		String finalString = minutesString.concat(":").concat(secondsString);

		return finalString;

	}

	/**
	 * Check if playing == true before using this method.
	 * 
	 * @return
	 */
	private int getIndexOfCurrentSongPlaying() {

		int indexOfCurrentSong;

		Song song = getCurrentSongPlaying();

		int listSize = tracksList.getModel().getSize();

		ArrayList<Song> tracksInTheList = new ArrayList<>();

		for (int i = 0; i < listSize; i++) {
			tracksInTheList.add((Song) tracksList.getModel().getElementAt(i)); // getting all the elements from the list
			System.out.println((Song) tracksList.getModel().getElementAt(i));
		}

		indexOfCurrentSong = tracksInTheList.indexOf(song); // at what index is the song that is playing

		return indexOfCurrentSong;

	}

	/**
	 * Check if playing == true before using this method.
	 */
	private Song getCurrentSongPlaying() {

		String currentSongPlaying = player.getMedia().getSource().replace("file:/", "");
		currentSongPlaying = currentSongPlaying.replace("%20", " ");
		File file = new File(currentSongPlaying);
		Song song = new Song(file);
		System.out.println("CURRENT SONG PLAYING : " + song.getFullPathAsString());

		return song;
	}

	public void run() { // this method is needed because the application is started with the
						// SwingUtilities class

	}

	private void playSong() throws IOException, InterruptedException {

		System.out.println("playSong method entered");

		Song song = (Song) tracksList.getSelectedValue();

		BufferedImage pauseImage = getBufferedImageIcon("src\\resources\\pauseButton.png");
		BufferedImage playImage = getBufferedImageIcon("src\\resources\\playButton.png");

		if (song != null && playing == false) {

			System.out.println("GOOD");

			File file = song.getFile();

			String songName = "";

			if (player != null) {

				songName = player.getMedia().getSource();
				songName = songName.substring(songName.lastIndexOf('/') + 1);
				songName = songName.replace("%20", " ");
			}

			if (currentTime == 0.0 || songName.equals(song.getSongName()) == false) {

				if (file.exists()) {
					
					media = new Media(file.toURI().toURL().toExternalForm());
					player = new MediaPlayer(media);
					
				} else if (!file.exists()) {
					
					StringBuilder userMessage = new StringBuilder(
							"Following song was not found in the speicified location: ");

					userMessage.append("\n" + file.toString());
					userMessage.append("\nTry adding the song again.");

					JOptionPane.showMessageDialog(this, userMessage, "Song not found", JOptionPane.INFORMATION_MESSAGE);

					Song songToRemove = (Song) tracksList.getModel().getElementAt(tracksList.getSelectedIndex());

					ArrayList<Song> filesInTheList = new ArrayList<>();

					for (int i = 0; i < tracksList.getModel().getSize(); i++) {
						filesInTheList.add((Song) tracksList.getModel().getElementAt(i)); // getting the values that are
																							// already
																							// in the list
					}

					filesInTheList.remove(songToRemove);

					tracksList.setListData(filesInTheList.toArray());

					songSlider.setValue(0);
					songCurrentTime.setText("0:00");
					songTotalTime.setText("0:00");

					media = null;
					player = null;
					mainButton.setImage(playImage);
				}

			}

			if (player != null) {

				player.setOnEndOfMedia(new Runnable() {

					@Override
					public void run() {

						System.out.println("setOnEndOfMedia");
						playing = false;
						mainButton.setImage(playImage);
						currentTime = 0.0;
						int indexOfCurrentSong = 0;
						int indexOfNextSong;

						timer.stop();

						songSlider.setValue(0);

						songCurrentTime.setText("0:00");

						if (autoPlaySelected == true && tracksList.getModel().getSize() > 0) {

							indexOfCurrentSong = getIndexOfCurrentSongPlaying(); // at what index is the song that just
																					// finished playing
							indexOfNextSong = indexOfCurrentSong + 1;

							System.out.println("listSize: " + tracksList.getModel().getSize());

							if (indexOfNextSong < tracksList.getModel().getSize()) { // checking if there is a next song

								tracksList.setSelectedIndex(indexOfNextSong);

								try {
									playSong();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							else {
								System.out.println("last song was played");
							}

						} else {
							tracksList.setSelectedIndex(getIndexOfCurrentSongPlaying());
						}

					}
				});

				player.setOnPlaying(() -> {

					System.out.println("setOnPlaying");
					playing = true;
					mainButton.setImage(pauseImage);

					timer.start();

				});

				player.setOnReady(() -> {
					System.out.println("setOnReady");
					if (song != null) { // determining the total song time and appending it to the GUI

						try {

							double totalTimeInSeconds;
							int totalSongTime = 0;

							if (song.getExtension().equals(".wav")) {

								AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(song.getFile());
								AudioFormat format = audioInputStream.getFormat();
								long frames = audioInputStream.getFrameLength();
								totalTimeInSeconds = (frames + 0.0) / format.getFrameRate();

								totalTimeInSeconds = Math.ceil(totalTimeInSeconds); // make it a round double
								totalSongTime = (int) totalTimeInSeconds - 1; // converting the double to an int

								songSlider.setMaximum(totalSongTime);

								System.out.println(totalTimeInSeconds);

							}

							else if (song.getExtension().equals(".mp3")) {

								Header header = null;
								FileInputStream fileInputStream = null;
								Bitstream bitStream = null;
								long size = 0;

								try {
									fileInputStream = new FileInputStream(song.getFile());
									bitStream = new Bitstream(fileInputStream);
									header = bitStream.readFrame();
									size = fileInputStream.getChannel().size();

								} catch (IOException ex) {
									ex.printStackTrace();
								}

								totalTimeInSeconds = header.total_ms((int) size) / 1000;

								totalTimeInSeconds = Math.ceil(totalTimeInSeconds); // make it a round double
								totalSongTime = (int) totalTimeInSeconds - 1; // converting the double to an int

								songSlider.setMaximum(totalSongTime);

								System.out.println(totalTimeInSeconds);

							}

							// converting the totalSongTime to String value and appending it to the GUI

							int copyOfTotalTime = totalSongTime;

							System.out.println();

							int minutes = 0;
							int seconds = 0;

							if (copyOfTotalTime == 0) {

							}

							else if (copyOfTotalTime >= 60) {

								while (copyOfTotalTime >= 60) {
									minutes += 1;
									copyOfTotalTime -= 60;
								}

								seconds += copyOfTotalTime;

							}

							else if (copyOfTotalTime < 60) {
								seconds += copyOfTotalTime;
							}

							String totalTimeAsString = String.valueOf(minutes).concat(":")
									.concat(String.valueOf(seconds));

							songTotalTime.setText(totalTimeAsString);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				});

				double volume = (double) soundSlider.getValue();

				player.setVolume(volume / 100);
				player.play();

				Thread.sleep(200);

				System.out.println("playing: " + playing);
				// get duration time of the song
			}

		} else if (playing == true) {

			System.out.println("PAUSE");

			player.pause();

			double pauseTime = songSlider.getValue() + 0.7;

			player.setStartTime(Duration.seconds(pauseTime));

			mainButton.setImage(playImage);

			timer.stop();

			playing = false;
		}
	}

	private void addTracks() {

		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("mp3,wav", "mp3", "wav");

		fileChooser.setFileFilter(filter);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle("Add songs");

		int returnValue = fileChooser.showOpenDialog(this);

		if (returnValue == JFileChooser.APPROVE_OPTION) {

			ArrayList<Song> filesInTheList = new ArrayList<>();

			for (int i = 0; i < tracksList.getModel().getSize(); i++) {
				filesInTheList.add((Song) tracksList.getModel().getElementAt(i)); // getting the values that are already
																					// in the list
			}

			List<Song> filesToBeAdded = new ArrayList<>(Arrays.asList(fileChooser.getSelectedFiles())).stream()
					.map(absoluteFilePath -> new Song(absoluteFilePath)).collect(Collectors.toList()); // getting the
																										// values that
																										// the user
																										// wants to add

			var songsWithSameName = new ArrayList<>();

			for (Song song : filesInTheList) {

				for (Song song2 : filesToBeAdded) {

					if (song.getSongNameWithoutExtension().equals(song2.getSongNameWithoutExtension())) {
						songsWithSameName.add(song2);
					}

				}

			}

			filesToBeAdded.removeAll(songsWithSameName); // if there are same songs don't add both

			
			for (Song song : filesToBeAdded) {
				if (!filesInTheList.contains(song)) {
					filesInTheList.add(song); // checking if the new values already exist. If not then add them. Don't
												// add duplicates
				}
			}

			ArrayList<Song> corruptedFiles = new ArrayList<Song>(); // corrupted mp3 files that are not allowed to be
																	// added

			Tika tika = new Tika(); // class from an external jar file that is used to check the validity of the mp3
									// file
			FileInputStream fileInputStream;

			for (int counter = 0; counter < filesToBeAdded.size(); counter++) { // checking if the mp3 file is valid or
																				// not
				Song song = filesToBeAdded.get(counter);
				String fullPath = song.getFullPathAsString();

				if (fullPath.endsWith("mp3")) {

					try {

						fileInputStream = new FileInputStream(new File(fullPath));
						String result = tika.detect(fileInputStream);
						System.out.println(fullPath + " ---- result:   " + result);

						if (result.equals("video/quicktime")) { // if true then the mp3 file is not valid and can not be
																// added to the list
							corruptedFiles.add(song);

						}

					} catch (IOException e) {
						e.printStackTrace();
					}

				}

			}

			filesInTheList.removeAll(corruptedFiles);

			if (corruptedFiles.size() > 0) {
				StringBuilder informationMessage = new StringBuilder(
						"Program has detected some files that are invalid: \n");

				corruptedFiles.stream().forEach(a -> informationMessage.append(a.getFullPathAsString() + "\n"));

				JOptionPane.showMessageDialog(this, informationMessage, "Invalid files",
						JOptionPane.INFORMATION_MESSAGE); // inform user about corrupted files
			}

			tracksList.setCellRenderer(new SongWithIconRenderer());

			tracksList.setListData(filesInTheList.toArray()); // added as Songs

		} else if (returnValue == JFileChooser.CANCEL_OPTION) {
			System.out.println("You have not selected any files.");
		}

		if (playing == true) {
			tracksList.setSelectedIndex(getIndexOfCurrentSongPlaying());
		}

	}

	private void clearTracks() {

		if (tracksList.getModel().getSize() > 0) { // if user pressed 'no' do nothing

			int selectedOption = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove all tracks?",
					"Clear all tracks", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			ArrayList<Song> songs = new ArrayList<>();

			if (selectedOption == JOptionPane.YES_OPTION) { // if the user has pressed yes do this /// clear the whole
															// list

				if (playing == true) { // if true then one song is being played and should not be removed from the list

					Song song = getCurrentSongPlaying(); // this is the song that is currently being played and needs to
															// stay in the list

					for (int i = 0; i < tracksList.getModel().getSize(); i++) {
						songs.add((Song) tracksList.getModel().getElementAt(i));
					}

					songs = songs.stream().filter((a) -> a.equals(song))
							.collect(Collectors.toCollection(ArrayList::new));
				}

				DefaultListModel listModel = new DefaultListModel();
				tracksList.setModel(listModel);

				if (tracksList.getCellRenderer() == checkListRenderer) {
					tracksList.setCellRenderer(defaultCellRenderer);
				}

				tracksList.removeMouseListener(mouseAdapter);
				tracksList.setListData(songs.toArray());

			}

			if (playing == true || currentTime != 0.0) {
				tracksList.setSelectedIndex(getIndexOfCurrentSongPlaying());
			}

			if (playing == false) {
				media = null;
				player = null;
				songCurrentTime.setText("0:00");
				songTotalTime.setText("0:00");
				songSlider.setValue(0);
			}

		}

		cancelButton.setEnabled(false);
		removeButton.setEnabled(false);
	}

	private void removeTracks() {

		if (tracksList.getModel().getSize() > 0) {

			selectedIndex = tracksList.getSelectedIndex();

			cancelButton.setEnabled(true);
			removeButton.setEnabled(true);

			final DefaultListModel model = new DefaultListModel();

			defaultCellRenderer = tracksList.getCellRenderer();
			tracksList.setCellRenderer(checkListRenderer);

			for (int i = 0; i < tracksList.getModel().getSize(); i++) {

				Song song = (Song) tracksList.getModel().getElementAt(i);

				model.addElement(new CheckListItem(song.getFullPathAsString()));

			}

			tracksList.setModel(model);

			tracksList.addMouseListener(mouseAdapter = new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent event) {

					int index = tracksList.locationToIndex(event.getPoint());

					CheckListItem item;
					if (index >= 0 && (item = (CheckListItem) tracksList.getModel().getElementAt(index)) != null) {

						item.setSelected(!item.isSelected());
						tracksList.repaint(tracksList.getCellBounds(index, index));

					}

				}
			});

		}
	}

	static {
		JFXPanel fxPanel = new JFXPanel();
	}
}

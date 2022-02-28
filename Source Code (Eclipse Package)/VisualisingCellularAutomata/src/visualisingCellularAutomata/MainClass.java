package visualisingCellularAutomata;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import javax.swing.JFormattedTextField;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.Component;
import javax.swing.JPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuBar;
import javax.swing.JSlider;
import javax.swing.JLabel;

/**
 * The MainClass handles the running of the application as well as creating its UI.
 * It also maintains control over the UI, which can change the behaviour of <code>LifePanel</code> (such as saving, speeding up or pausing).
 * This class also contains the main method of the program.
 */
public class MainClass extends JFrame {
	/** Auto-generated ID. */
	private static final long serialVersionUID = 6750773676820411760L; 
	/** The master panel that is the parent of all other panels and objects. First child of the JFrame. */
	private JPanel contentPane;
	/** A local save for whether or not the simulation is running. */
	private int playState = 0;
	
	/**
	 * Launch the application. This is the main method.
	 * Launches a dialog that asks to user to set the size of the simulation and the initial state of the simulation.
	 * Then, launches the simulation.
	 * @param args Main method. Not used.
	 */
	public static void main(String[] args) {		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					InitialPopup dialog = new InitialPopup(); // This popup determines the size that the simulation will be. It also allows the user to choose from a few predefined start states.
					dialog.pack();
					dialog.setModal(true); // The dialog is already modal, but just in case the OS blocks it - it is written here explicitly. Modal means that the program will wait for input before continuing.
					dialog.setTitle("Choose size and initial save");
					dialog.setVisible(true); 
					
					// Once dialog input has been received, start the simulation.
					MainClass simulation = new MainClass();
					simulation.setSize(new Dimension(InitialPopup.initialWidth+20, InitialPopup.initialHeight+20)); // Set size for when the user de-maximises the program, plus some leeway.
					simulation.setExtendedState(MAXIMIZED_BOTH); // The program (the frame) is maximised by default.
					simulation.setTitle("Lifelike Cellular Automata Simulator 2021");
					simulation.setVisible(true);
				} 
				catch (Exception e) {
					e.printStackTrace(); // Debugging purposes.
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * This unique constructor generates the frame and the UI on it.
	 * The constructed object is what listens to user input and changes the behaviour of <code>LifePanel</code>, which is displayed in this object of <code>MainClass</code> (as <code>MainClass</code> is an extension of JFrame).
	 */
	public MainClass() {
		// Get values from the initial JDialog as parameters to start the simulation with.
		// The user can select the size of the simulation as well as the initial configuration of live cells to load.
		int inputCellSize = InitialPopup.initialSize;
		int inputWidth = InitialPopup.initialWidth;
		int inputHeight = InitialPopup.initialHeight;
		String inputLoad = InitialPopup.initialLoad;
		
		// Set basic frame properties.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 1000); // When the user de-maximises the program, they will be able to drag it around easily.
		contentPane = new JPanel(); // contentPane acts as a base on which all other UI elements lie on.
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		// Set up the toolbar.
		JToolBar toolBar = new JToolBar(); // The toolbar has all the buttons that the user interacts with (except for stamps).
		toolBar.setAutoscrolls(true);
		toolBar.setPreferredSize(new Dimension(120, 120)); // The toolbar itself abides by a preferred size, unless the user violates that by using a small resolution.
		toolBar.setDoubleBuffered(true);
		toolBar.setOrientation(SwingConstants.VERTICAL);
		toolBar.setFloatable(false);
		contentPane.add(toolBar, BorderLayout.WEST);
		
		// Initialise menu used for selecting stamps.
		JMenuBar stampMenu = new JMenuBar();
		
		// Drawing Options label - buttons underneath this label let the user choose what they draw onto the grid with their mouse.
		JLabel lblDrawingOptions = new JLabel("<HTML><U>Drawing Options</U></HTML>");
		lblDrawingOptions.setVerticalAlignment(SwingConstants.BOTTOM);
		lblDrawingOptions.setHorizontalAlignment(SwingConstants.CENTER);
		lblDrawingOptions.setMaximumSize(new Dimension(120, 30));
		toolBar.add(lblDrawingOptions);
		
		/* SHOW STAMP MENU
		 * NOTE: The menus here are written at the bottom of this class to keep the code more readable.
		 * This is why "JMenuBar stampMenu = new JMenuBar();" appears above.
		 * The JMenuBar is initialised so that buttonStamp can control it, without having to move any other code down to the bottom of the class as well.
		 * The stampMenu allows the user to draw several pre-defined patterns onto the automaton, such as gliders.
		 */
		JButton buttonStamp = new JButton("Stamps");
		buttonStamp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(stampMenu.isVisible()) {
					stampMenu.setVisible(false);
				}
				else {
					stampMenu.setVisible(true);
				}
			}
		});
		buttonStamp.setToolTipText("Current stamp: none");
		buttonStamp.setMaximumSize(new Dimension(120, 60));
		toolBar.add(buttonStamp);
		
		/* TOGGLE CELL STATE
		 * Toggles between the user drawing live / drawing dead cells.
		 */
		JButton buttonClickState = new JButton("Erase Cells");
		buttonClickState.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.drawState = 1 - LifePanel.drawState; // Alternates between 0 and 1 (a value of 1 means we want to erase cells).
				if(LifePanel.drawState == 0) { // The user has chosen to draw live cells.
					buttonClickState.setToolTipText("Currently drawing live cells to the grid. This option does not change stamps.");
					buttonClickState.setText("Erase Cells");
				}
				else { // The user has chosen to erase live cells, killing them.
					buttonClickState.setText("Draw Cells");
					buttonClickState.setToolTipText("Currently erasing live cells from the grid. This option does not change stamps.");
				}
			}
		});
		buttonClickState.setToolTipText("Currently drawing live cells to the grid. This option does not change stamps.");
		buttonClickState.setMaximumSize(new Dimension(120, 60));
		toolBar.add(buttonClickState);
		
		// Running Options label - buttons underneath this label are ones that let the user determine how the simulation runs.
		JLabel lblRunningOptions = new JLabel("<HTML><U>Running Options</U></HTML>");
		lblRunningOptions.setVerticalAlignment(SwingConstants.BOTTOM);
		lblRunningOptions.setHorizontalAlignment(SwingConstants.CENTER);
		lblRunningOptions.setMaximumSize(new Dimension(120, 30));
		toolBar.add(lblRunningOptions);
		
		
		/* PLAY-PAUSE
		 * This button controls the playing and pausing of the simulation of the cellular automaton.
		 */
		JButton buttonPlayPause = new JButton("Play");
		buttonPlayPause.setMaximumSize(new Dimension(120, 60));
		buttonPlayPause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switch(playState) {
					case(1): // The simulation is currently playing.
						buttonPlayPause.setText("Play");
						LifePanel.pauseFlag = true;
						playState = 0;
						break;
					case(0): // The simulation is currently paused.
						buttonPlayPause.setText("Pause");
						LifePanel.pauseFlag = false;
						playState = 1;
						break;
				}
			}
		});
		toolBar.add(buttonPlayPause);
		
		/* SINGLE STEP
		 * This button will advance the state of the cellular automaton by one generation.
		 */
		JButton buttonSingleStep = new JButton("Single Step");
		buttonSingleStep.setMaximumSize(new Dimension(120, 60));
		buttonSingleStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.singleStepFlag = true;
			}
		});
		toolBar.add(buttonSingleStep);
		
		// Speed Options label - buttons underneath this label let the user choose the speed at which the simulation runs.
		JLabel lblSpeedOptions = new JLabel("<HTML><U>Speed Options</U></HTML>");
		lblSpeedOptions.setVerticalAlignment(SwingConstants.BOTTOM);
		lblSpeedOptions.setHorizontalAlignment(SwingConstants.CENTER);
		lblSpeedOptions.setMaximumSize(new Dimension(120, 30));
		toolBar.add(lblSpeedOptions);
		
		/* TIMER
		 * This slider lets the user choose how fast the simulation runs. A range of 10ms steps between 250ms and 10ms updates.
		 */
		JSlider timerSlider = new JSlider();
		timerSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
		timerSlider.setBorder(new EmptyBorder(15, 0, 15, 0)); // This border pads the timer.
		timerSlider.setSize(new Dimension (120, 60));
		timerSlider.setMinorTickSpacing(10);
		timerSlider.setMaximum(200); // Slowest delay is 200ms, i.e. 5 updates per second.
		timerSlider.setMinimum(10); // Fastest delay is 10ms.
		timerSlider.setValue(75);
		timerSlider.setInverted(true); // We want the higher values to be on the left to signify slower.
		timerSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				LifePanel.timer.setDelay(timerSlider.getValue()); // Set the timer delay to the chosen value.
			}
		});
		toolBar.add(timerSlider);
		
		/* DEFAULT SPEED
		 * This button resets timerSlider to its default value (of 75ms).
		 */
		JButton buttonDefaultSpeed = new JButton("Default Speed");
		buttonDefaultSpeed.setMaximumSize(new Dimension(120, 60));
		buttonDefaultSpeed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.timer.setDelay(75);
				timerSlider.setValue(75); // This will technically repeat the above line of code, but it is still there for clarity.
			}
		});
		toolBar.add(buttonDefaultSpeed);

		// Grid Options label - buttons underneath this label let the user influence the state of the grid, e.g. clearing it.
		JLabel lblGridOptions = new JLabel("<HTML><U>Grid Options</U></HTML>");
		lblGridOptions.setVerticalAlignment(SwingConstants.BOTTOM);
		lblGridOptions.setHorizontalAlignment(SwingConstants.CENTER);
		lblGridOptions.setMaximumSize(new Dimension(120, 30));
		toolBar.add(lblGridOptions);
		
		/* EMPTY AUTOMATON
		 * This button will set all cells of the automaton to state 0.
		 */
		JButton buttonEmptyAutomaton = new JButton("Clear Grid");
		buttonEmptyAutomaton.setMaximumSize(new Dimension(120, 60));
		buttonEmptyAutomaton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.emptyAutomatonFlag = true;
			}
		});
		toolBar.add(buttonEmptyAutomaton);
		
		/* RANDOM STATE
		 * Set the grid to a randomised state.
		 */
		JButton buttonRandomState = new JButton("Randomise Grid");
		buttonRandomState.setMaximumSize(new Dimension(120, 60));
		buttonRandomState.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.randomState = true;
			}
		});
		toolBar.add(buttonRandomState);
		
		/* TOGGLE HEATMAP
		 * This button toggles the heatmap.
		 * Instead of using a local variable like "playState", this button reads directly from LifePanel.
		 * This is because toggleHeatmap is a variable that can not be extended, and will only ever be either true or false.
		 * However, the program could be extended to incorporate more than two play states.
		 */
		JButton buttonHeatmap = new JButton("Disable Heatmap");
		buttonHeatmap.setMaximumSize(new Dimension(120, 60));
		buttonHeatmap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (LifePanel.heatmapFlag) {
					LifePanel.heatmapFlag = false;
					buttonHeatmap.setText("Enable Heatmap");
				}
				else {
					LifePanel.heatmapFlag = true;
					buttonHeatmap.setText("Disable Heatmap");
				}
			}
		});
		toolBar.add(buttonHeatmap);
		
		// Save / Load Options label - buttons underneath this label let the user load and save states.
		JLabel lblSaveLoadOptions = new JLabel("<HTML><U>Save / Load Options</U></HTML>");
		lblSaveLoadOptions.setVerticalAlignment(SwingConstants.BOTTOM);
		lblSaveLoadOptions.setHorizontalAlignment(SwingConstants.CENTER);
		lblSaveLoadOptions.setMaximumSize(new Dimension(120, 30));
		toolBar.add(lblSaveLoadOptions);
		
		/* SAVE FILE
		 * This button prompts the program to save its state to an external file.
		 * Uses a static flag to tell LifePanel to save.
		 */
		JButton buttonSaveFile = new JButton("Save");
		buttonSaveFile.setMaximumSize(new Dimension(120, 60));
		buttonSaveFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.saveFlag = true;
			}
		});
		toolBar.add(buttonSaveFile);
		
		/* LOAD FILE
		 * This button prompts to program to load a saved file.
		 * The popup menu and its buttons are written in code below this button. This is because the popup menu is a separate entity.
		 * Uses a static flag and a static string to tell LifePanel to load, and what filename it should load.
		 */
		JButton buttonLoadFile = new JButton("Load");
		buttonLoadFile.setMaximumSize(new Dimension(120, 60));
		toolBar.add(buttonLoadFile);

		/* POPUP MENU (LOAD FILES) AND LOAD BUTTONS
		 * This popup menu shows up when "buttonLoadFile" is clicked.
		 * It gives the user options to load several different files, or to cancel the operation.
		 * This popup menu controls the value of the static string "saveToLoad".
		 */
		JPopupMenu popupMenuLoads = new JPopupMenu();
		// Bounds for the popup menu - height is 50, which makes the default button size shorter (from 60).
		// Width is 200, which allows for buttons to be made wider if need be.
		popupMenuLoads.setBounds(0, 0, 200, 50); 		
		
		// Load the save that the user themselves has saved.
		JButton buttonLoadCurrent = new JButton("User Save");
		buttonLoadCurrent.setMaximumSize(new Dimension(120, 60));
		buttonLoadCurrent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.saveToLoad = "current_save.txt";
				LifePanel.loadFlag = true;
				popupMenuLoads.setVisible(false);
			}
		});
		popupMenuLoads.add(buttonLoadCurrent);
		
		// Load a predefined save that has two glider guns that emit gliders that crash into and destroy each other.
		JButton buttonLoadGliders = new JButton("Glider Guns");
		buttonLoadGliders.setMaximumSize(new Dimension(120, 60));
		buttonLoadGliders.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.saveToLoad = "glider_guns.txt";
				LifePanel.loadFlag = true;
				popupMenuLoads.setVisible(false);
			}
		});
		popupMenuLoads.add(buttonLoadGliders);
		
		// Load a predefined save that has two glider-emitting rakes that eventually cause a massive crash with one another.
		// The lower the resolution, the earlier this crash occurs.
		// 1080p with 75ms update delay (default settings): about 100 seconds before the crash occurs.
		// 720p is much earlier than that; and 1440p is much later than that.
		// No other resolutions were tested.
		JButton buttonLoadRakeCrash = new JButton("Rake Crash");
		buttonLoadRakeCrash.setMaximumSize(new Dimension(120, 60));
		buttonLoadRakeCrash.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.saveToLoad = "rake_crash.txt";
				LifePanel.loadFlag = true;
				popupMenuLoads.setVisible(false);
			}
		});
		popupMenuLoads.add(buttonLoadRakeCrash);
		
		// Loads a predefined methuselah pattern named "Gliders by the Dozen", originally discovered by Roger H. Rosenbaum.
		// More information is available at https://conwaylife.com/wiki/Gliders_by_the_dozen.
		JButton buttonLoadDozenGliders = new JButton("Dozen Gliders");
		buttonLoadDozenGliders.setMaximumSize(new Dimension(120, 60));
		buttonLoadDozenGliders.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.saveToLoad = "dozen_gliders.txt";
				LifePanel.loadFlag = true;
				popupMenuLoads.setVisible(false);
			}
		});
		popupMenuLoads.add(buttonLoadDozenGliders);
		
		// Loads a methuselah pattern named "Bunnies", originally discovered by Robert Wainwright.
		// More information is available at https://conwaylife.com/wiki/Bunnies.
		JButton buttonLoadBunnies = new JButton("Bunnies");
		buttonLoadBunnies.setMaximumSize(new Dimension(120, 60));
		buttonLoadBunnies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.saveToLoad = "bunnies.txt";
				LifePanel.loadFlag = true;
				popupMenuLoads.setVisible(false);
			}
		});
		popupMenuLoads.add(buttonLoadBunnies);
		
		// Loads a methuselah pattern named "Thunderbird", originally discovered by Hugh Thompson.
		// More information available at https://www.conwaylife.com/wiki/Thunderbird.
		JButton buttonLoadThunderbird = new JButton("Thunderbird");
		buttonLoadThunderbird.setMaximumSize(new Dimension(120, 60));
		buttonLoadThunderbird.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.saveToLoad = "thunderbird.txt";
				LifePanel.loadFlag = true;
				popupMenuLoads.setVisible(false);
			}
		});
		popupMenuLoads.add(buttonLoadThunderbird);	
		
		// Loads a save with four methuselah-type patterns in symmetry with each another.
		JButton buttonLoadFourCastles = new JButton("Four Castles");
		buttonLoadFourCastles.setMaximumSize(new Dimension(120, 60));
		buttonLoadFourCastles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.saveToLoad = "four_castles.txt";
				LifePanel.loadFlag = true;
				popupMenuLoads.setVisible(false);
			}
		});
		popupMenuLoads.add(buttonLoadFourCastles);		
		
		// Cancel the load operation. The user can click anywhere to hide the menu, but this button appears as an explicit option to hide the menu.
		JButton buttonCancelPopupMenuLoads = new JButton("Cancel");
		buttonCancelPopupMenuLoads.setMaximumSize(new Dimension(120, 60));
		buttonCancelPopupMenuLoads.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popupMenuLoads.setVisible(false);
			}
		});
		popupMenuLoads.add(buttonCancelPopupMenuLoads);
		
		addPopup(buttonLoadFile, popupMenuLoads); // Add this popup menu to the "load" button, buttonLoadFile.
		
		// Advanced Options label - buttons underneath this label are for use by advanced users, such as changing rules.
		JLabel lblAdvancedOptions = new JLabel("<HTML><U>Advanced Options</U></HTML>");
		lblAdvancedOptions.setVerticalAlignment(SwingConstants.BOTTOM);
		lblAdvancedOptions.setHorizontalAlignment(SwingConstants.CENTER);
		lblAdvancedOptions.setMaximumSize(new Dimension(120, 30));
		toolBar.add(lblAdvancedOptions);
		
		/* TOGGLE GRID WRAPPING
		 * By default, grid wrapping is enabled. It can be toggled on and off using this button.
		 * Again, grid wrapping can not be extended in the future, so its value is read directly from LifePanel.
		 */
		JButton buttonToggleGridWrap = new JButton("Disable Wrapping");
		buttonToggleGridWrap.setMaximumSize(new Dimension(120, 60));
		buttonToggleGridWrap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(LifePanel.gridWrapFlag) {
					LifePanel.gridWrapFlag = false;
					buttonToggleGridWrap.setText("Enable Wrapping");
				}
				else {
					LifePanel.gridWrapFlag = true;
					buttonToggleGridWrap.setText("Disable Wrapping");
				}
			}
		});
		toolBar.add(buttonToggleGridWrap);
		
		/* TEXT FIELD FOR BIRTH RULE
		 * This text field will allow the user to dynamically change birth rules.
		 * BirthSet and SurvivalSet are used in LifePanel.java in nextGeneration().
		 */
		JFormattedTextField birthTextField = new JFormattedTextField();
		birthTextField.setAlignmentX(Component.LEFT_ALIGNMENT); // Text fields align differently than buttons normally do.
		birthTextField.setMaximumSize(new Dimension(120, 60));
		birthTextField.addFocusListener(new FocusAdapter() { 
			@Override
			public void focusGained(FocusEvent e) {
                birthTextField.selectAll();
                birthTextField.setForeground(Color.GRAY);
			}
			// This focus listener automatically selects the entire field when it is clicked on, so that its contents can be easily overwritten.
			// It also sets the text colour to gray to let the user know that the new rules are not yet in effect.
		});		
		birthTextField.setToolTipText("Enter birth rule here");
		birthTextField.setForeground(Color.BLACK);
		birthTextField.setText("B3");
		birthTextField.setFocusLostBehavior(JFormattedTextField.COMMIT);
	    birthTextField.setMaximumSize(new Dimension(200, 30));
		toolBar.add(birthTextField);
	
		/* TEXT FIELD FOR SURVIVAL RULE
		 * This text field will allow the user to dynamically change survival rules.
		 * BirthSet and SurvivalSet are used in LifePanel.java in nextGeneration().
		 */
		JFormattedTextField survivalTextField = new JFormattedTextField();
		survivalTextField.setAlignmentX(Component.LEFT_ALIGNMENT); // Text fields align differently than buttons do, so this fixes that.
		survivalTextField.setMaximumSize(new Dimension(120, 60));
		survivalTextField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
                survivalTextField.selectAll();
                survivalTextField.setForeground(Color.GRAY);
			}
			// This focus listener automatically selects the entire field when it is clicked on, so that its contents can be easily overwritten.
		});
		survivalTextField.setToolTipText("Enter survival rule here");
		survivalTextField.setForeground(Color.BLACK);
		survivalTextField.setText("S23");
		survivalTextField.setFocusLostBehavior(JFormattedTextField.COMMIT);
		survivalTextField.setMaximumSize(new Dimension(200, 30));
		toolBar.add(survivalTextField);
		
		/* CONFIRM RULE CHANGES
		 * This button takes the input in the birthTextField and survivalTextField and commits them to the automaton.
		 * In turn, the rules that the user inputted will be simulated by the program when this button is used.
		 * BirthSet and SurvivalSet are used in LifePanel.java in nextGeneration(). This button commits changes to BirthSet and SurvivalSet.
		 */
		JButton buttonRuleChange = new JButton("Confirm Rules");
		buttonRuleChange.setMaximumSize(new Dimension(120, 60));
		buttonRuleChange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Clear the current rules.
				LifePanel.survivalSet.clear();
				LifePanel.birthSet.clear();
				
				// Fetch user-inputted values for new rules, which may be empty.
				String birthString = birthTextField.getText();
				String survivalString = survivalTextField.getText();
				
				// Remove all invalid inputs, and parse only correct rules that have numbers 0-8 (9 is not supported).
				birthString = birthString.replaceAll("[^0-8]", "");
				survivalString = survivalString.replaceAll("[^0-8]", "");
				
				// Set the visible text to the new rules of the automaton, and set the text colour to black to indicate that these rules are active.
				birthTextField.setText("B" + birthString);
				survivalTextField.setText("S" + survivalString);
				birthTextField.setForeground(Color.BLACK);
				survivalTextField.setForeground(Color.BLACK);
				
				// Now, add rules to the automaton to reflect the user's input. The HashSet's properties will automatically ignore duplicate numbers in the field.
				for(int i = 0; i < birthString.length(); i++) {
					// Add each number in the birthString to the birth set of rules.
					LifePanel.birthSet.add(Character.getNumericValue(birthString.charAt(i)));
				}
				for(int i = 0; i < survivalString.length(); i++) {
					// Add each number in the survivalString to the survival set of rules.
					LifePanel.survivalSet.add(Character.getNumericValue(survivalString.charAt(i)));
				}
			}
		});
		toolBar.add(buttonRuleChange);	
		 
		
		/* Create the LifePanel object.
		 * All buttons that appear on the JToolBar are written above.
		 * Below this are the buttons that appear on the Stamp menu, which appears at the top of the screen when the user makes it visible.
		 * screenWidth - 130 accounts for the width of the toolbar plus its border.
		 * screenHeight - 40 accounts for the height of the program's header bar (the bar that has the minimise, maximise and close window buttons) and the hidden stamp menu.
		 */
		LifePanel panel =  new LifePanel(inputWidth-130, inputHeight-40, inputCellSize, Color.DARK_GRAY, Color.WHITE, inputLoad);
		contentPane.add(panel, BorderLayout.CENTER); // Add the LifePanel to the base panel.
		
		
		/* CREATE STAMP MENUS  - BUTTONS ON THE JMENUBAR FOLLOW
		 * These menus allow the user to select from a set of predefined stamps.
		 * This allows them to directly draw interesting patterns to the automaton, such as gliders.
		 * These are only intended to be used with the rules of Conway's Game of Life in effect.
		 * The stampMenu is declared earlier so that a button declared earlier can affect its visibility.
		 * (Otherwise, the code would not see the stampMenu causing a syntax error)
		 */		
		JPanel panelMenu = new JPanel(); // This new panel will allow the Stamp Menu to appear at the top of the screen.
		contentPane.add(panelMenu, BorderLayout.NORTH); // Add the new panel to the top of the base panel.
		
		panelMenu.add(stampMenu); // Add the previously-defined JMenuBar to the newly created panel.
		stampMenu.setLayout(new GridLayout(1,0));
		stampMenu.setVisible(false); // By default, the menu isn't visible - the user must select a button to show it.
		stampMenu.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		// NO STAMP: Reset to no stamp, the user draws single cells with mouse clicks. They can also erase cells if the option is selected.
		JButton buttonStampNone = new JButton("No Stamp");
		buttonStampNone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.stampToUse = 0; // This integer is used in a switch statement to determine which stamp to use.
				buttonStamp.setToolTipText("Current stamp: none");
				stampMenu.setVisible(false); // Hide menu after a button is used.
			}
		});
		buttonStampNone.setMaximumSize(new Dimension(120, 60));
		stampMenu.add(buttonStampNone);
		
		
		/* GLIDER POPUP MENU 
		 * the user draws gliders. Shows a popup menu of four buttons with the four possible orientations.
		 */
		JButton buttonStampGliders = new JButton("Gliders");
		stampMenu.add(buttonStampGliders);
		
		JPopupMenu popupMenuGliders = new JPopupMenu(); // This is the popup menu for gliders.
		popupMenuGliders.setBounds(0, 0, 200, 50);
		
		JButton buttonUpLeftGlider = new JButton("Up-Left"); // This button draws a glider that moves diagonally up and left.
		buttonUpLeftGlider.setMaximumSize(new Dimension(120, 60));
		buttonUpLeftGlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.stampToUse = 10;
				buttonStamp.setToolTipText("Current stamp: Glider (diagonal up-left)");
				popupMenuGliders.setVisible(false);
				stampMenu.setVisible(false);
			}
		});
		popupMenuGliders.add(buttonUpLeftGlider);
		
		JButton buttonUpRightGlider = new JButton("Up-Right"); // This button draws a glider that moves diagonally up and right.
		buttonUpRightGlider.setMaximumSize(new Dimension(120, 60));
		buttonUpRightGlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.stampToUse = 11;
				buttonStamp.setToolTipText("Current stamp: Glider (diagonal up-right)");
				popupMenuGliders.setVisible(false);
				stampMenu.setVisible(false);
			}
		});
		popupMenuGliders.add(buttonUpRightGlider);
		
		JButton buttonDownRightGlider = new JButton("Down-Right"); // This button draws a glider that moves diagonally down and right.
		buttonDownRightGlider.setMaximumSize(new Dimension(120, 60));
		buttonDownRightGlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.stampToUse = 12;
				buttonStamp.setToolTipText("Current stamp: Glider (diagonal down-right)");
				popupMenuGliders.setVisible(false);
				stampMenu.setVisible(false);
			}
		});
		popupMenuGliders.add(buttonDownRightGlider);
		
		JButton buttonDownLeftGlider = new JButton("Down-Left"); // This button draws a glider that moves diagonally down and left.
		buttonDownLeftGlider.setMaximumSize(new Dimension(120, 60));
		buttonDownLeftGlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.stampToUse = 13;
				buttonStamp.setToolTipText("Current stamp: Glider (diagonal down-left)");
				popupMenuGliders.setVisible(false);
				stampMenu.setVisible(false);
			}
		});
		popupMenuGliders.add(buttonDownLeftGlider);
		
		addPopup(buttonStampGliders, popupMenuGliders);	// Add this popup menu to the "gliders" button, buttonStampGliders.
		
		
		/* LIGHTWEIGHT SPACESHIP POPUP MENU
		 * The user draws LWSS's. Shows a popup menu of the two possible orientations.
		 */
		JButton buttonStampLWSS = new JButton("LWSS's");
		stampMenu.add(buttonStampLWSS);
		
		JPopupMenu popupMenuLWSS = new JPopupMenu(); // This is the popup menu for LWSS's.
		popupMenuLWSS.setBounds(0, 0, 200, 50);
		
		JButton buttonRightLWSS = new JButton("Left-to-Right"); // This button draws an LWSS that moves from left to right.
		buttonRightLWSS.setMaximumSize(new Dimension(120, 60));
		buttonRightLWSS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.stampToUse = 20;
				buttonStamp.setToolTipText("Current stamp: LWSS (left-to-right)");
				popupMenuLWSS.setVisible(false);
				stampMenu.setVisible(false);
			}
		});
		popupMenuLWSS.add(buttonRightLWSS);
		
		JButton buttonLeftLWSS = new JButton("Right-to-Left"); // This button draws an LWSS that moves from right to left.
		buttonLeftLWSS.setMaximumSize(new Dimension(120, 60));
		buttonLeftLWSS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.stampToUse = 21;
				buttonStamp.setToolTipText("Current stamp: LWSS (right-to-left)");
				popupMenuLWSS.setVisible(false);
				stampMenu.setVisible(false);
			}
		});
		popupMenuLWSS.add(buttonLeftLWSS);		
		
		addPopup(buttonStampLWSS, popupMenuLWSS); // Add this popup menu to the "LWSS's" button, buttonStampLWSS.
		
		
		/* MEDIUMWEIGHT SPACESHIP POPUP MENU
		 * The user draws MWSS's. Shows a popup menu of the two possible orientations.
		 */
		JButton buttonStampMWSS = new JButton("MWSS's");
		stampMenu.add(buttonStampMWSS);
		
		JPopupMenu popupMenuMWSS = new JPopupMenu(); // This is the popup menu for MWSS's.
		popupMenuMWSS.setBounds(0, 0, 200, 50);
		
		JButton buttonRightMWSS = new JButton("Left-to-Right"); // This button draws an MWSS that travels from the left to the right.
		buttonRightMWSS.setMaximumSize(new Dimension(120, 60));
		buttonRightMWSS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.stampToUse = 30;
				buttonStamp.setToolTipText("Current stamp: MWSS (left-to-right)");
				popupMenuMWSS.setVisible(false);
				stampMenu.setVisible(false);
			}
		});
		popupMenuMWSS.add(buttonRightMWSS);
		
		JButton buttonLeftMWSS = new JButton("Right-to-Left"); // This button draws an MWSS that travels from the right to the left.
		buttonLeftMWSS.setMaximumSize(new Dimension(120, 60));
		buttonLeftMWSS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.stampToUse = 31;
				buttonStamp.setToolTipText("Current stamp: MWSS (right-to-left)");
				popupMenuMWSS.setVisible(false);
				stampMenu.setVisible(false);
			}
		});
		popupMenuMWSS.add(buttonLeftMWSS);	
		
		addPopup(buttonStampMWSS, popupMenuMWSS); // Add this popup menu to the "MWSS's" button, buttonStampMWSS.
		
		
		/* HEAVYWEIGHT SPACESHIP POPUP MENU
		 * The user draws HWSS's. Shows a popup menu of the two possible orientations.
		 */
		JButton buttonStampHWSS = new JButton("HWSS's");
		stampMenu.add(buttonStampHWSS);
		
		JPopupMenu popupMenuHWSS = new JPopupMenu(); // This is the popup menu for HWSS's.
		popupMenuHWSS.setBounds(0, 0, 200, 50);
		
		JButton buttonRightHWSS = new JButton("Left-to-Right"); // This button draws an HWSS that travels from the left to the right.
		buttonRightHWSS.setMaximumSize(new Dimension(120, 60));
		buttonRightHWSS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.stampToUse = 40;
				buttonStamp.setToolTipText("Current stamp: HWSS (left-to-right)");
				popupMenuHWSS.setVisible(false);
				stampMenu.setVisible(false);
			}
		});
		popupMenuHWSS.add(buttonRightHWSS);
		
		JButton buttonLeftHWSS = new JButton("Right-to-Left"); // This button draws an HWSS that travels from the right to the left.
		buttonLeftHWSS.setMaximumSize(new Dimension(120, 60));
		buttonLeftHWSS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.stampToUse = 41;
				buttonStamp.setToolTipText("Current stamp: HWSS (right-to-left)");
				popupMenuHWSS.setVisible(false);
				stampMenu.setVisible(false);
			}
		});
		popupMenuHWSS.add(buttonLeftHWSS);			
		
		addPopup(buttonStampHWSS, popupMenuHWSS); // Add this popup menu to the "HWSS's" button, buttonStampHWSS.
		
		
		/* OSCILLATORS POPUP MENU
		 * To draw different types of oscillators. Shows a popup menu of a few possible options.
		 */
		JButton buttonStampOscillators = new JButton("Oscillators");
		stampMenu.add(buttonStampOscillators);
		
		JPopupMenu popupMenuOscillators = new JPopupMenu(); // This is the popup menu for HWSS's.
		popupMenuOscillators.setBounds(0, 0, 200, 50);
		
		JButton buttonPulsar = new JButton("Pulsar"); // This button draws an HWSS that travels from the left to the right.
		buttonPulsar.setMaximumSize(new Dimension(140, 60)); // These buttons are wider than the others, as some of their names are very long.
		buttonPulsar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.stampToUse = 50;
				buttonStamp.setToolTipText("Current stamp: Pulsar");
				popupMenuOscillators.setVisible(false);
				stampMenu.setVisible(false);
			}
		});
		popupMenuOscillators.add(buttonPulsar);
		
		JButton buttonPentadecathlon = new JButton("Pentadecathlon"); // This button draws a pentadecathlon.
		buttonPentadecathlon.setMaximumSize(new Dimension(140, 60)); // This button's name is very long, and doesn't fit in 120 width like the other buttons.
		buttonPentadecathlon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.stampToUse = 51;
				buttonStamp.setToolTipText("Current stamp: Pentadecathlon");
				popupMenuOscillators.setVisible(false);
				stampMenu.setVisible(false);
			}
		});
		popupMenuOscillators.add(buttonPentadecathlon);
		
		JButton buttonTumbler = new JButton("Tumbler"); // This button draws a tumbler.
		buttonTumbler.setMaximumSize(new Dimension(140, 60));
		buttonTumbler.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.stampToUse = 52;
				buttonStamp.setToolTipText("Current stamp: Tumbler");
				popupMenuOscillators.setVisible(false);
				stampMenu.setVisible(false);
			}
		});
		popupMenuOscillators.add(buttonTumbler);
		
		JButton buttonFigureEight = new JButton("Figure Eight"); // This button draws a figure eight.
		buttonFigureEight.setMaximumSize(new Dimension(140, 60));
		buttonFigureEight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.stampToUse = 53;
				buttonStamp.setToolTipText("Current stamp: Figure Eight");
				popupMenuOscillators.setVisible(false);
				stampMenu.setVisible(false);
			}
		});
		popupMenuOscillators.add(buttonFigureEight);
		
		JButton buttonPhoenix = new JButton("Phoenix"); // This button draws a phoenix.
		buttonPhoenix.setMaximumSize(new Dimension(140, 60));
		buttonPhoenix.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LifePanel.stampToUse = 54;
				buttonStamp.setToolTipText("Current stamp: Phoenix");
				popupMenuOscillators.setVisible(false);
				stampMenu.setVisible(false);
			}
		});
		popupMenuOscillators.add(buttonPhoenix);
		
		addPopup(buttonStampOscillators, popupMenuOscillators); // Add this popup menu to the "Oscillators" button, buttonStampOscillators.
		
		/* HIDE STAMP MENU
		 * To close the stamp menu (not strictly necessary, as the menu can be closed in other ways).
		 */
		JButton buttonHideStampMenu = new JButton("Close Menu");
		buttonHideStampMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stampMenu.setVisible(false);
			}
		});
		stampMenu.add(buttonHideStampMenu);
	}
	
	/**
	 * Auto-generated method.
	 * This method handles how popup menus in the program are made visible.
	 * In this case, popup menus appear when the user clicks on the relevant button.
	 * For instance, when choosing a Glider stamp, a popup menu shows which gives four different options for directions that the glider can move.
	 * @param component The component to listen to, like a button.
	 * @param popup The popup menu to display.
	 */
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				showMenu(e);
			}
			public void mouseReleased(MouseEvent e) {
				showMenu(e);
			}				
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
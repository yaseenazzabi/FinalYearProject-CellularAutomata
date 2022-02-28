package visualisingCellularAutomata;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * This JDialog is what the user first sees when the program starts.
 * It allows the user to select the size of the simulation, and the initial state of the simulation.
 * It uses radio buttons to allow the user to select the save.
 * Each radio button has an internal String value.
 * When a radio button is selected, it signals to the program to use its internal value as an initial state for the simulation.
 * If the "random" radio button is selected, then the program won't load any save and will instead initialise randomly.
 */
public class InitialPopup extends JDialog {
	/** Auto-generated ID. */
	private static final long serialVersionUID = -1310520304275796804L;
	/** Master panel that holds all elements and subpanels. */
	private final JPanel contentPanel = new JPanel();
	/** Button group for radio buttons. */
	private final ButtonGroup buttonGroup = new ButtonGroup();
	
	/** Default value for cell size in the simulation (10 pixels).
	 * @see visualisingCellularAutomata.LifePanel#pixelSize */
	public static int initialSize = 10;
	/** Default width. Immediately overwritten by the JSpinners in this class.
	 * @see visualisingCellularAutomata.LifePanel#xSize */
	public static int initialWidth = 1280;
	/** Default height. Immediately overwritten by the JSpinners in this class.
	 * @see visualisingCellularAutomata.LifePanel#ySize*/
	public static int initialHeight = 720;
	/** Sets the value of <code>initalLoad</code> in <code>LifePanel</code>. 
	 * @see visualisingCellularAutomata.LifePanel#initialLoad */
	public static String initialLoad = ""; // By default, we load nothing. The empty string signals LifePanel to initialise its state randomly.

	/**
	 * Create the dialog.
	 */
	public InitialPopup() {
		setModal(true);
		setModalityType(ModalityType.APPLICATION_MODAL); // We definitely want this dialog to force the program to wait for its input.
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Automatically dispose the dialog when it is closed (e.g. with X button instead of OK).
		setBounds(100, 100, 900, 600); // Set size.
		
		// Set layout attributes.
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new GridLayout(0,2));
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		/*
		 * Label for the first set of inputs: changing size. These are generally intended only for advanced users.
		 */
		{
			JLabel labelSize = new JLabel("Set Simulation Size (Advanced Users Only)");
			labelSize.setToolTipText("The size of the simulation is width times height, divided by the size of each cell.");
			labelSize.setHorizontalAlignment(SwingConstants.CENTER);
			contentPanel.add(labelSize);
		}
		
		/*
		 * The controls that allow the user to set the simulation size. To include more cell sizes, decrease the minor tick spacing to sliderCellSize.
		 */
		{
			{
				JPanel panelSizeOptions = new JPanel();
				panelSizeOptions.setBorder(new EmptyBorder(5, 5, 5, 5));
				contentPanel.add(panelSizeOptions);
				panelSizeOptions.setLayout(new GridLayout(3, 2));
				
				// Get the screen dimensions for default values for height and width.
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				int screenWidth = (int) screenSize.getWidth();
				int screenHeight = (int) screenSize.getHeight();
												
				// Label for the slider that lets you select cell size.
				{
					JLabel labelCellSize = new JLabel("Cell Size / px");
					labelCellSize.setHorizontalAlignment(SwingConstants.CENTER);
					panelSizeOptions.add(labelCellSize);
				}
				
				// The slider that lets the user control the size of cells in the simulation.
				JSlider sliderCellSize = new JSlider();
				sliderCellSize.setBorder(new EmptyBorder(5, 0, 5, 0));
				panelSizeOptions.add(sliderCellSize);
				sliderCellSize.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						initialSize = sliderCellSize.getValue(); // Set the static int to the value chosen.
					}
				});
				sliderCellSize.setToolTipText("Size of cells in the simulation in pixels. Default is 10.");
				sliderCellSize.setMinorTickSpacing(5);
				sliderCellSize.setMajorTickSpacing(20);
				sliderCellSize.setPaintLabels(true);
				sliderCellSize.setSnapToTicks(true);
				sliderCellSize.setPaintTicks(true);
				sliderCellSize.setValue(10);
				sliderCellSize.setMinimum(5);
				sliderCellSize.setMaximum(25);

				// Label for the width text field.
				{
					JLabel labelSimWidth = new JLabel("Width / px");
					labelSimWidth.setHorizontalAlignment(SwingConstants.CENTER);
					panelSizeOptions.add(labelSimWidth);
				}
				
				// The width text field (which is actually a Spinner).
				{
					JSpinner spinnerWidth = new JSpinner();
					spinnerWidth.addChangeListener(new ChangeListener() {
						public void stateChanged(ChangeEvent e) {
							initialWidth = (int) spinnerWidth.getValue(); // Set the static int to the value chosen.
						}
					});
					spinnerWidth.setToolTipText("Width of simulation in pixels, only between 1280px and 3840px. For 1080p displays, this has no effect past 1920px.");
					spinnerWidth.setModel(new SpinnerNumberModel(1280, 1280, 3840, 1)); // Set model. Min = 700; Max = 3840.
					spinnerWidth.setValue(Math.min(3840, screenWidth)); // Immediately set default value to the screen size, but maximising at 4K resolution.
					panelSizeOptions.add(spinnerWidth);
				}
				
				// Label for the height text field.
				{
					JLabel labelSimHeight = new JLabel("Height / px");
					labelSimHeight.setHorizontalAlignment(SwingConstants.CENTER);
					panelSizeOptions.add(labelSimHeight);
				}
				
				// The height text field (which is actually a Spinner).
				{
					JSpinner spinnerHeight = new JSpinner();
					spinnerHeight.addChangeListener(new ChangeListener() {
						public void stateChanged(ChangeEvent e) {
							initialHeight = (int) spinnerHeight.getValue(); // Set the static int to the value chosen.
						}
					});
					spinnerHeight.setToolTipText("Height of simulation in pixels, only between 720px and 2160px. For 1080p displays, this has no effect past 1080px.");
					spinnerHeight.setModel(new SpinnerNumberModel(720, 720, 2160, 1));
					spinnerHeight.setValue(Math.min(2160, screenHeight)); // Immediately set default value to the screen size, but maximising at 4K resolution.
					panelSizeOptions.add(spinnerHeight);
				}
			}
		}
		
		/*
		 * Add the label for the second input: the initial save to load (e.g. randomly initialise the simulation, or select from a pre-defined start state.
		 */
		{
			JLabel labelList = new JLabel("Choose Starting State");
			labelList.setHorizontalAlignment(SwingConstants.CENTER);			
			contentPanel.add(labelList);
		}
		
		/*
		 * The radio buttons that allow the user to select from a few different initial save configurations, including a random option.
		 * These buttons are highly expandable - to add a new start state, simply add a new radio button with a relevant actionCommand.
		 */
		{
			JPanel panelButtons = new JPanel();
			panelButtons.setBorder(new EmptyBorder(5, 15, 5, 15));
			contentPanel.add(panelButtons);
			panelButtons.setLayout(new GridLayout(4, 1));
			{
				JRadioButton startRandom = new JRadioButton("Random");
				panelButtons.add(startRandom);
				startRandom.setSelected(true);
				startRandom.setActionCommand(""); // This is the default string - signals to load nothing, and instead initialise randomly.
				buttonGroup.add(startRandom);
			}
			{
				JRadioButton startGliderGuns = new JRadioButton("Glider Guns");
				startGliderGuns.setActionCommand("glider_guns.txt"); // This string indicates to load the file "glider_guns.txt".
				buttonGroup.add(startGliderGuns);
				panelButtons.add(startGliderGuns);
			}
			{
				JRadioButton startRakeCrash = new JRadioButton("Rake Crash");
				startRakeCrash.setActionCommand("rake_crash.txt");
				buttonGroup.add(startRakeCrash);
				panelButtons.add(startRakeCrash);
			}
			{
				JRadioButton startDozenGliders = new JRadioButton("Dozen Gliders");
				startDozenGliders.setActionCommand("dozen_gliders.txt");
				buttonGroup.add(startDozenGliders);
				panelButtons.add(startDozenGliders);
			}
			{
				JRadioButton startBunnies = new JRadioButton("Bunnies");
				startBunnies.setActionCommand("bunnies.txt");
				buttonGroup.add(startBunnies);
				panelButtons.add(startBunnies);
			}
			{
				JRadioButton startThunderbird = new JRadioButton("Thunderbird");
				startThunderbird.setActionCommand("thunderbird.txt");
				buttonGroup.add(startThunderbird);
				panelButtons.add(startThunderbird);
			}
			{
				JRadioButton startFourCastles = new JRadioButton("Four Castles");
				startFourCastles.setActionCommand("four_castles.txt");
				buttonGroup.add(startFourCastles);
				panelButtons.add(startFourCastles);
			}
			{
				JRadioButton startUserSave = new JRadioButton("User Save");
				startUserSave.setToolTipText("If no save is found, the simulation will start empty.");
				startUserSave.setActionCommand("current_save.txt");
				buttonGroup.add(startUserSave);
				panelButtons.add(startUserSave);
			}
			// More information on save / load files is located in LifePanel.java, under the relevant methods (loadFile).
		}
		
		/*
		 *  The OK button - saves the radio button selected to a static string that the constructor of the simulating LifePanel uses to select the initial state to load.
		 *  Also, disposes the dialog (which happens automatically with the DISPOSE_ON_CLOSE attribute anyway).
		 */
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
			getContentPane().add(buttonPane, BorderLayout.SOUTH); {
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						initialLoad = buttonGroup.getSelection().getActionCommand(); // Ensure that the load variable is properly updated.
						dispose();
					}
				});
				okButton.setActionCommand("OK"); // Not used, but typically left as default anyway.
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}
}

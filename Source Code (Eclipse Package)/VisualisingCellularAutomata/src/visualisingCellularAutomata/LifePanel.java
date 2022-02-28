package visualisingCellularAutomata;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * This is what pixels appear on.
 */
public class LifePanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
	/** Auto-generated ID. */
	private static final long serialVersionUID = -5216611180689834553L;	
	/** The amount of pixels in the x-dimension. */
	int xSize; 	
	/** The amount of pixels in the y-dimension. */
	int ySize;	
	/** The size in pixels of each cell (square). */
	int pixelSize; 	
	/** xSize divided by pixelSize. 
	 * @see xSize 
	 * @see pixelSize */
	int xWidth;	
	/** ySize divided by pixelSize. 
	 * @see ySize 
	 * @see pixelSize */
	int yHeight; 	
	
	/** Stores the grid of cells. This grid is displayed graphically. */
	Cell lifeArray[][]; 	
	/** Uses <code>lifeArray</code> to generate the next generation of cells, which are stored in this array. */
	Cell postLifeArray[][]; 	
	/** The colour of the gridlines. */
	Color gridColor; 	
	/** Default colours for the grid and cells to be drawn (Grey and White). Can't be changed by the user (in this version). */
	Color pixelColor; 
	
	/* Public static properties that the user interacts with to influence the behaviour of the program follow. */
	
	/** Default update delay = 75ms. */
	public static Timer timer = new Timer(75, null); 
	/** This string determines which file to load. Default value does not matter. */
	public static String saveToLoad = "current_save.txt"; 
	/** An empty string indicates to randomly load. Otherwise, load the save specified by the string. 
	 * Effectively controlled by <code>InitialPopup</code> class, and is only ever set once (when the program starts).
	 * @see visualisingCellularAutomata.InitialPopup*/
	public static String initialLoad = ""; 
	/** Default stamp is no stamp at all, indicated by 0. */
	public static int stampToUse = 0;
	/** Default draw state of 0 is to draw live cells. */		
	public static int drawState = 0; 
	
	/** Determines if the simulation should be paused or not. */
	public static boolean pauseFlag = true;
	/** Signals to generate the next generation and immediately pause. */
	public static boolean singleStepFlag = false;
	/** Signals to clear all cells in the simulation. */
	public static boolean emptyAutomatonFlag = false;
	/** Enables / disables the heatmap functionality. */
	public static boolean heatmapFlag = true;
	/** Signals to save the current state of the simulation. */
	public static boolean saveFlag = false;
	/** Signals to load a save, determined by <code>saveToLoad</code>. 
	 * @see saveToLoad */
	public static boolean loadFlag = false;
	/** Enables / disables the grid wrapping functionality. */
	public static boolean gridWrapFlag = true;
	/** Signals to randomly set the states of all cells in the simulation. */
	public static boolean randomState = false;
	
	/** This stores the rules that result in a dead cell becoming alive. Default is Conway's Game of Life. */
	public static Set<Integer> birthSet = new HashSet<Integer>(Arrays.asList(3)); 
	/** This stores the rules that result in an alive cell staying alive. Default is Conway's Game of Life. */
	public static Set<Integer> survivalSet = new HashSet<Integer>(Arrays.asList(2,3)); 
	
	/**
	 * This unique constructor creates a <code>LifePanel</code> object. Its arguments' values will be dependent on user input.
	 * Only one specific type of <code>LifePanel</code> object can exist, which explains why there is only one constructor.
	 * @param xSize The number of columns in the array.
	 * @param ySize The number of rows in the array.
	 * @param pixelSize The size that each cell will be when displayed graphically. Influences xSize and ySize.
	 * @param gridColor The default colour of the grid (used American spelling as the class is Color).
	 * @param pixelColor The default colour of a cell. Can not be changed due to the existence of the heatmap.
	 * @param initialLoad The initial state to start the automaton with. The user selects a save to start with using SizePopup.java, and the save file's name passed from it is used for initialLoad. If no save is chosen, it is an empty string.
	 */
	public LifePanel(int xSize, int ySize, int pixelSize, Color gridColor, Color pixelColor, String initialLoad) {
		// Set object attributes from the arguments.
		this.xSize = xSize;
		this.ySize = ySize;
		this.pixelSize = pixelSize;
		this.xWidth = xSize / pixelSize;
		this.yHeight = ySize / pixelSize;
		this.lifeArray = new Cell[xWidth][yHeight];
		this.postLifeArray = new Cell[xWidth][yHeight];
		this.gridColor = gridColor;
		this.pixelColor = pixelColor;
		
		// Initialise array by filling them with Cells (with values of 0,0).
		for(int i = 0; i < (xWidth); i++) {
			for(int j = 0; j < (yHeight); j++) {
				lifeArray[i][j] = new Cell();
				postLifeArray[i][j] = new Cell();
			}
		}
		
		// Initialise the cells of the automaton. Can be via a saved file or simply randomly set. Governed by SizePopup.java.
		if(!initialLoad.contentEquals("")) { // If the string has a value, load the save with the name equal to the string.
			saveToLoad = initialLoad;
			loadFlag = true; // Start the simulation, and immediately signal it to load the given save.			
		}
		else { // If the user didn't choose a starting save, randomly initialise cells.
			randomSpawn();
		}
		
		// Add mouse listeners to the object. Allows the user to be able to draw and use stamps.
		addMouseMotionListener(this);
		addMouseListener(this);
		
		// Set the JPanel up. Allows for graphics to be displayed.
		setSize(this.xSize, this.ySize);
		setLayout(null); // There is no layout for this panel as it is purely graphical.
		setBackground(Color.BLACK);
		
		// Set the timer to listen to this object and start it. 
		// It will never be paused, as pausing the timer makes it more difficult and inefficient to interact with the simulation when paused.
		timer.addActionListener(this); 
		timer.start();
	}
	
	/**
	 * Handles the drawing of the graphics of live pixels.
	 * @param g The Graphics object used to draw onto the screen. Arbitrarily named.
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g); // Required for use of paintComponent.
		
		// Every frame, the grid is drawn. 
		g.setColor(gridColor);	
		gridCreator(g); // This may sound inefficient, but it isn't and it is actually the only one: "hard-drawing" the grid is inflexible and undesirable.
		
		// Draw the live cells to the screen.
		displayPixels(g);
	}
	
	/**
	 * This method iterates through the current 2D array of cells and displays them on the life panel.
	 * @param g The Graphics object used to draw onto the screen. Arbitrarily named.
	 */
	private void displayPixels(Graphics g) {
		g.setColor(pixelColor); // Set the pixel colour to the default.
		copyLifeArray(); // Copy the generated array into the array that is actually displayed onto the screen.
		for(int i = 0; i < (xWidth); i++) {
			for(int j = 0; j < (yHeight); j++) {
				if(lifeArray[i][j].state == 1) { // if the cell is active, then colour it.
					if(heatmapFlag) { // Set the pixel colour according to the cell's age, only if the user has decided to enable the heatmap.
						g.setColor(
								new Color(255, 
								Math.max(0, (255-(lifeArray[i][j].age*10))), 
								Math.max(0, (255-(lifeArray[i][j].age*10)))
								));
					// If the timer were paused, enabling and disabling heatmap colour would only be possible while the simulation was running.
					// This is part of the reason why the timer is never paused.
					} 
					
					// Fill the cell with the set colour.
					g.fillRect(i*pixelSize, j*pixelSize, pixelSize, pixelSize);
				}
			}
		}
	}
	
	/**
	 * This flexible method creates a grid based on the number of cells and the size of cell graphics to be used.
	 * @param g The Graphics object used to draw onto the screen. Arbitrarily named.
	 */
	private void gridCreator(Graphics g) {
		for(int i = 0; i < (yHeight); i++) {
			g.drawLine(0, i*pixelSize, xSize, i*pixelSize); // Draws rows.
		}
		
		for(int i = 0; i < (xWidth); i++) {				
			g.drawLine(i*pixelSize, 0, i*pixelSize, ySize); // Draws columns.
		}
	}
	
	/**
	 * This method handles the random spawning of a start state.
	 * It sets cells in the automaton to age = 0, and some will randomly be state 1, and the rest are randomly state 0.
	 * With a 30% chance, a lot of cells that are made active will immediately die to overpopulation (default rules are Conway's Game of Life).
	 * This results in consistently "appropriate" starting states using this random initialisation.
	 */
	private void randomSpawn() {
		emptyAutomaton();
		float randomNum = 0; // Initialise a random number variable. Automatically disposed.
		Random random = new Random(System.currentTimeMillis()); // Initialise a random number generator with a seed of the current time. Automatically disposed.
		for(int i = 0; i < (xWidth); i++) {
			for(int j = 0; j < (yHeight); j++) {
				randomNum = random.nextFloat(); // Generate a number in (0, 1].
				if (randomNum < 0.3) { // A value of 0.3 was chosen, as 0.5 ended up with patterns that were far too dense.
					postLifeArray[i][j].state = 1;
					postLifeArray[i][j].age = 0;
					lifeArray[i][j].state = 1;
					lifeArray[i][j].age = 0;
				}
				else {
					postLifeArray[i][j].state = 0;
					postLifeArray[i][j].age = 0;
					lifeArray[i][j].state = 0;
					lifeArray[i][j].age = 0;
				}
			}
		}
	}
	
	/**
	 * This method applies rules and creates the next generation's life array (<code>postLifeArray</code>).
	 * First, check live cells against survival rules - if one applies, then the live cell will stay alive.
	 * Second, check dead cells against birth rules - if one applies, then the dead cell becomes alive.
	 * If no rules apply, then a live cell dies and a dead cell remains dead.
	 * This forms all possible rulesets of lifelike cellular automata.
	 */
	void nextGeneration() {		
		int neighbours;
	
		for(int i = 0; i < (xWidth); i++) {
			for(int j = 0; j < (yHeight); j++) {
				neighbours = check(i, j);
				
				if(lifeArray[i][j].state == 1 && survivalSet.contains(neighbours)) { // If birth rule applies:
					postLifeArray[i][j].age = Math.min(255, (postLifeArray[i][j].age+1)); // This ensures that age never increases past 255 (preventing high use of resources).
				}
				else if(lifeArray[i][j].state == 0 && birthSet.contains(neighbours)) { // Else, if survival rule applies:
					postLifeArray[i][j].state = 1;
				}
				else { // If no rules apply, then the cell must die.
					postLifeArray[i][j].state = 0;
					postLifeArray[i][j].age = 0;
				}
			}
		}
	}

	/**
	 * This method checks the neighbour count of a given cell. It can be used with grid wrapping enabled or with it disabled.
	 * This method is effectively the heart of the simulation - so it must be as efficient as possible.
	 * @param x The x-coordinate of the cell to check.
	 * @param y The y-coordinate of the cell to check.
	 * @return The number of neighbours that the cell at (x, y) has.
	 */
	private int check(int x, int y) {
		// Initialise neighbour variable.
		int neighbours = 0;
		
		// If grid wrapping enabled, use modulo operator to turn the grid into a torus - no if or switch statements are required here, maximising efficiency.
		if(gridWrapFlag) {
			// y - 1: cells above the cell being considered.
			neighbours += lifeArray[(x + xWidth - 1) % xWidth][(y + yHeight - 1) % yHeight].state;
			neighbours += lifeArray[(x + xWidth) % xWidth][(y + yHeight - 1) % yHeight].state;
			neighbours += lifeArray[(x + xWidth + 1)  % xWidth][(y + yHeight - 1) % yHeight].state;
			
			// y + 0: cells on the same row as the cell being considered.
			neighbours += lifeArray[(x + xWidth - 1) % xWidth][(y + yHeight) % yHeight].state;
			neighbours += lifeArray[(x + xWidth + 1) % xWidth][(y + yHeight) % yHeight].state;
			
			// y + 1: cells below the cell being considered.
			neighbours += lifeArray[(x + xWidth - 1) % xWidth][(y + yHeight + 1) % yHeight].state;
			neighbours += lifeArray[(x + xWidth)  % xWidth][(y + yHeight + 1) % yHeight].state;
			neighbours += lifeArray[(x + xWidth + 1) % xWidth][(y + yHeight + 1) % yHeight].state;
			
			// Finally, return found neighbours (could be outside of if/else).
			return  neighbours;
		}
		
		// If grid wrapping is disabled, try/catch each array index to minimise the use of if statements.
		else { // NOTE: try / catch has no impact on performance vs. normal code unless an error is found. This is the most efficient possible solution.
			// y - 1: cells above the cell being considered.
			try {neighbours += lifeArray[x - 1][y - 1].state;}
			catch(ArrayIndexOutOfBoundsException e) {}
			try {neighbours += lifeArray[x][y - 1].state;}
			catch(ArrayIndexOutOfBoundsException e) {}			
			try {neighbours += lifeArray[x + 1][y - 1].state;}
			catch(ArrayIndexOutOfBoundsException e) {}
			
			// y + 0: cells on the same row as the cell being considered.
			try {neighbours += lifeArray[x - 1][y].state;}
			catch(ArrayIndexOutOfBoundsException e) {}			
			try {neighbours += lifeArray[x + 1][y].state;}
			catch(ArrayIndexOutOfBoundsException e) {}			
			
			// y + 1: cells below the cell being considered.
			try {neighbours += lifeArray[x - 1][y + 1].state;}
			catch(ArrayIndexOutOfBoundsException e) {}			
			try {neighbours += lifeArray[x][y + 1].state;}
			catch(ArrayIndexOutOfBoundsException e) {}			
			try {neighbours += lifeArray[x + 1][y + 1].state;}
			catch(ArrayIndexOutOfBoundsException e) {}
			
			// Finally, return found neighbours (could be outside of if/else).
			return neighbours;
		}
	}

	/**
	 * This method copies the computed life array of the previous generation into the current generation's array that is displayed graphically.
	 * That is, <code>postLifeArray</code> is not displayed, but it is computed based on <code>lifeArray</code>.
	 * <code>lifeArray</code> is copied FROM <code>postLifeArray</code>, displayed to the screen, and used to generate the next <code>postLifeArray</code>.
	 * The new value for <code>postLifeArray</code> is then copied again to <code>lifeArray</code> for display, and so on.
	 */
	private void copyLifeArray() {
		for(int i = 0; i < (xWidth); i++) {
			for(int j = 0; j < (yHeight); j++) {
				lifeArray[i][j].state = postLifeArray[i][j].state;
				lifeArray[i][j].age = postLifeArray[i][j].age;
			}
		}
	}
	
	/**
	 * This method sets all cell states to 0, in all known generations.
	 * The ages of all cells are also reset.
	 */
	void emptyAutomaton() {
		for(int i = 0; i < (xWidth); i++) {
			for(int j = 0; j < (yHeight); j++) {
				// Reset all cells (in all known generations).
				postLifeArray[i][j].state = 0;
				postLifeArray[i][j].age = 0;
				lifeArray[i][j].state = 0;
				lifeArray[i][j].age = 0;
			}
		}
	}

	/**
	 * This method saves the current state of the automaton (without its ruleset) to an external text file.
	 */
	public void saveFile() {
		// First, we must generate the string to write to the file.
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < (xWidth); i++) {
		   for(int j = 0; j < (yHeight); j++) {
		      builder.append(lifeArray[i][j].state+";"+lifeArray[i][j].age+""); // Append the current cell's age and state, split by a semicolon.
		      if(j < lifeArray.length - 1) { // If this is NOT the last row element, then split it with a comma.
		         builder.append(",");
		      }
		   }
		   builder.append("\n"); // Append a new line at the end of the row.
		}
		
		// Now that the string is built, we write it to the current_save.txt file.
		try { 
			BufferedWriter writer = new BufferedWriter(new FileWriter("current_save.txt"));
			writer.write(builder.toString());
			writer.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method loads a saved file, and sets the program to begin simulation from the loaded state.
	 * Makes use of the public static string <code>saveToLoad</code> to decide what filename to load.
	 * @see saveToLoad
	 */
	public void loadFile() {
		emptyAutomaton(); // First, clear the automaton.
		try { // Now, read the file and extract its contents into the simulation.
			BufferedReader reader = new BufferedReader(new FileReader(saveToLoad)); // saveToLoad is static and is controlled by the user through the UI.
			String line = "";
			int row = 0;
			while((line = reader.readLine()) != null) { // Read the file.
			   String[] cols = line.split(","); // Cells are separated by commas.
			   int col = 0;
			   for(String  s : cols) {
				  String[] both = s.split(";"); // State and age are separated by a semicolon, e.g. state = 0, age = 1 is depicted "0;1"
				  try { // When reading the array, we must avoid ArrayIndexOutOfBounds errors - this try/except cycle does that.
				      postLifeArray[row][col].state = Integer.parseInt(both[0]);
				      postLifeArray[row][col].age = Integer.parseInt(both[1]);
				      lifeArray[row][col].state = Integer.parseInt(both[0]);
				      lifeArray[row][col].age = Integer.parseInt(both[1]);
				      // Both the current and next generation are updated, to prevent anything from changing due to rules after loading.
				  }
				  catch (ArrayIndexOutOfBoundsException e) { // If the file is too large, we skip values that are outside the size of the array.
					  break;
					  // NOTE: the array will not get larger to accommodate files that are too big, it will simply load only a subset of the file to fit.
					  // Furthermore, if the file is too small, the remainder of the values will be 0-state cells with 0 age, as the automaton was first cleared.
				  }
			      col++;
			   }
			   row++;
			}
			reader.close(); // When finished reading.
		}
		catch(Exception e) {
			e.printStackTrace(); // If an unknown (usually FileNameNotFound) exception occurs, this is printed for debugging purposes.
		}
		
	}

	/**
	 * Used for drawing stamps. If the mouse is moved after clicking, stamps will not be used, allowing the user to draw a smooth line of live cells.
	 * That is, if the user clicks and drags, they will draw a smooth line of live cells instead of a line of clumped-up stamps.
	 */
	public void mouseClicked(MouseEvent e) {
		int x = e.getX() / pixelSize;
		int y = e.getY() / pixelSize;
		
		stampPattern(x, y); // Stamp the selected pattern (determined by a switch statement and the value of stampToUse).
	}

	/**
	 * Draws a live cell onto the grid (or remove one if <code>drawState</code> == 1).
	 */
	public void mousePressed(MouseEvent e) {
		int x = e.getX() / pixelSize;
		int y = e.getY() / pixelSize;
		
		try {
			// If draw state is 1, then dead cells will be drawn.
			lifeArray[x][y].state = 1 - drawState;
			postLifeArray[x][y].state = 1 - drawState;
			
			// If we are drawing dead cells, there is no need to reset age. It will naturally reset to 0 in the generation that it is made dead.
		}
		catch(Exception r) { 
			// Several exceptions are thrown when the user attempts to draw outside of the grid.
			// None of these exceptions are fatal, so their warnings are suppressed.
		}
	}

	/**
	 * Draws live cells onto the grid (or removes them if <code>drawState</code> == 1).
	 */
	public void mouseDragged(MouseEvent e) {
		int x = e.getX() / pixelSize;
		int y = e.getY() / pixelSize;
		
		try {
			lifeArray[x][y].state = 1 - drawState;
			postLifeArray[x][y].state = 1 - drawState;
		}
		catch(Exception r) {
			// Several exceptions are thrown when the user attempts to draw outside of the grid.
			// None of these exceptions are fatal, so their warnings are suppressed.
		}
	}
	
	/**
	 * Not used, but a part of the mouse listeners implemented.
	 */
	public void mouseMoved(MouseEvent e) {
		
	}
	
	/**
	 * Not used, but a part of the mouse listeners implemented.
	 */
	public void mouseReleased(MouseEvent e) {
		
	}
	
	/**
	 * Not used, but a part of the mouse listeners implemented.
	 */
	public void mouseEntered(MouseEvent e) {
		
	}
	
	/**
	 * Not used, but a part of the mouse listeners implemented.
	 */
	public void mouseExited(MouseEvent e) {
		
	}
	
	/** Determines what should be done every time the program is updated, usually by <code>timer</code> generating an interrupt.
	 * 
	 * Many of these flags and values are static, belonging to <code>LifePanel</code>. 
	 * It makes little sense to initialise an object of <code>LifePanel</code>. Instead, the class is controlled statically.
	 *
	 * Every time the automaton updates (determined by the timer or by singleStepFlag), 5 statements are checked.
	 * 
	 * 1: If the user wants to save.
	 * 
	 * 2: If the user wants to load.
	 * 
	 * 3: If the user wants to clear the automaton.
	 * 
	 * 4: If the user wants to randomly generate a state.
	 * 
	 * 5: If the simulation is currently not paused, create the next generation.
	 * @param e The action event that caused the program to update. This is usually caused by the timer generating an interrupt rhythmically, but can be triggered by other things.
	 */
	public void actionPerformed(ActionEvent e) {
		// If the user prompts a save should occur, do so.
		if(saveFlag) { 
			saveFile(); // Always saves to "current_save.txt".
			saveFlag = false;
		}
		
		// If the user prompts to load a file, do so.
		if(loadFlag) { 
			loadFile(); // Loads using a static string for the filename to load. The string's value is handled by the UI.
			loadFlag = false;
		}
		
		// If the user has requested that the cell grid be cleared, do so.
		if(emptyAutomatonFlag) { 
			emptyAutomaton();
			emptyAutomatonFlag = false;
		}
		
		// If the user wants to randomly set the state of the automaton, do so.
		if(randomState) { 
			randomSpawn();
			randomState = false;
		}
		
		// If the simulation is not paused, then generate the next state of the automaton.
		if (!pauseFlag) { 
			nextGeneration();
			// A flag is used instead of timer.stop(). 
			// This is because timer.stop() can prevent the user from interacting with the program in certain ways, which is undesirable.
		}
		
		// "Else" statement is purely for clarity, to reflect how singleStepFlag is used with pauseFlag.
		else { 
			if(singleStepFlag) { // If the simulation is paused, we can still single-step the simulation.
				nextGeneration();
				singleStepFlag = false;
				// After this generation, the flag is immediately reset to false.
			}
		}	
		// After all requested changes have been made to the automaton, redraw graphics.
		repaint();
	}
	
	/** Stamps predefined patterns onto the screen.
	 * This method is located at the bottom of the code as it is effectively just a database for patterns.
	 * This method draws pre-defined patterns onto the <code>LifePanel</code>, such as gliders.
	 * The data for what each pattern looks like is directly stored in lines of code (in methods beneath this one).
	 * Adding more patterns means adding more cases to this switch statement, and adding new buttons to the UI to call those switch cases.
	 * Creating a new case involves creating a new helper method that draws a specific pattern, and calling that method in the relevant switch.
	 * This makes the method flexible and well-expandable.
	 * @param x The mouse pointer's current x-coordinate.
	 * @param y The mouse pointer's current y-coordinate.
	 */
	private void stampPattern(int x, int y) {
		try { // Catches all errors thrown when the user attempts to draw outside of the grid bounds.
			switch(stampToUse) { // stampToUse is an int that the UI controls, allowing the user to select the pattern that they want to draw.
			/* NO STAMP CASE
			 * In this case, the user draws single cells when clicking the mouse button. Obeys the draw state rule (i.e. allows for removing of live cells).
			 */
			case(0): // Use no stamp.
				lifeArray[x][y].state = 1 - drawState;
				postLifeArray[x][y].state = 1 - drawState;
				break;			
			
			/* GLIDER CASES: Draw gliders, four orientations.
			 * Gliders can move diagonally in any direction.
			 */
			case(10): // Draw glider (up-left).
				stampGlider(x, y, 1, 1);
				break;
			case(11): // Draw glider (up-right) (mirrored x-axis of up-left).
				stampGlider(x, y, -1, 1);	
				break;
			case(12): // Draw glider (down-right) (mirrored x-axis and y-axis of up-left).
				stampGlider(x, y, -1, -1);
				break;
			case(13): // Draw glider (down-left) (mirrored y-axis of up-left).
				stampGlider(x, y, 1, -1);
				break;			
			
			/* DRAW LWSS's - two orientations.
			 * These spaceships, like MWSS's and HWSS's can move left or right.
			 */
			case(20): // Draw LWSS - moving from left to right (method is multiplied by 1 - no mirroring occurs).
				stampLWSS(x, y, 1);
				break;
			case(21): // Draw LWSS - moving from right to left (mirrored in x-axis of left-to-right, by multiplying x by -1).
				stampLWSS(x, y, -1);		
				break;	
				
			/* DRAW MWSS's - two orientations.
			 * These spaceships, like LWSS's and HWSS's can move left or right.
			 */
			case(30): // Draw MWSS - moving from left to right.
				stampMWSS(x, y, 1);
				break;
			case(31): // Draw MWSS - moving from right to left (mirrored in x-axis of left-to-right).
				stampMWSS(x, y, -1);
				break;	
				
			/* DRAW HWSS's - two orientations.
			 * These spaceships, like LWSS's and MWSS's can move left or right.
			 */
			case(40): // Draw HWSS - moving from left to right.
				stampHWSS(x, y, 1);
				break;
			case(41): // Draw HWSS - moving from right to left (mirrored in x-axis of left-to-right).
				stampHWSS(x, y, -1);
				break;
				
			/* DRAW OSCILLATORS - A few options are available. 
			 *
			 */
			case(50): // Draw a pulsar.
				stampPulsar(x, y);
				break;
			case(51): // Draw a pentadecathlon.
				stampPentadecathlon(x, y);
				break;
			case(52): // Draw a tumbler.
				stampTumbler(x, y);
				break;
			case(53): // Draw a figure-eight.
				stampFigureEight(x, y);
				break;
			case(54):
				stampPhoenix(x, y);
				break;
			}
		}
		
		catch(Exception r) { 
			// Several exceptions are thrown when the user attempts to draw outside of the grid.
			// No other exceptions are thrown when using this method.
			// None of the thrown exceptions are harmful, so warnings are simply suppressed with a catch all.
			// If the user does attempt to stamp out-of-bounds, then all cells that are within range until the error is thrown will be drawn.
		}
	}
	
	/**
	 * A helper method to <code>stampPattern</code> that draws a glider pattern to the screen.
	 * If both <code>mirrorX</code> and <code>mirrorY</code> are disabled (i.e. both have the value 1),
	 * a glider that moves diagonally up-left will be drawn.
	 * If <code>mirrorX</code> is -1, the glider is flipped horizontally.
	 * If <code>mirrorY</code> is -1, the glider is flipped vertically.
	 * This means that four different orientations of glider can be drawn.
	 * @param x x-coordinate to draw the glider at.
	 * @param y y-coordinate to draw the glider at.
	 * @param mirrorX If this is -1, flip the glider horizontally. Either -1 or 1.
	 * @param mirrorY If this is -1, flip the glider vertically. Either -1 or 1.
	 * @see stampPattern
	 */
	private void stampGlider(int x, int y, int mirrorX, int mirrorY) {
		lifeArray[x][y].state = 1;
		postLifeArray[x][y].state = 1;
		
		lifeArray[x+(1*mirrorX)][y].state = 1; // x-value is mirrored.
		postLifeArray[x+(1*mirrorX)][y].state = 1;
		
		lifeArray[x+(2*mirrorX)][y+(1*mirrorY)].state = 1;
		postLifeArray[x+(2*mirrorX)][y+(1*mirrorY)].state = 1;
		
		lifeArray[x][y+(1*mirrorY)].state = 1;
		postLifeArray[x][y+(1*mirrorY)].state = 1;
		
		lifeArray[x][y+(2*mirrorY)].state = 1;
		postLifeArray[x][y+(2*mirrorY)].state = 1;		
		// It is possible to mirror the entire x-axis by multiplying it's value after calculation by -1.
		// This is just an alternative approach, and offers no actual advantages.
	}
	
	/**
	 * A helper method to <code>stampPattern</code> that draws an LWSS pattern to the screen. 
	 * If <code>mirrorX</code> is disabled (i.e. it has the value 1), then the LWSS will move left-to-right.
	 * If the pattern is mirrored in the x-axis, the LWSS will move right-to-left.
	 * @param x x-coordinate to draw the LWSS at.
	 * @param y y-coordinate to draw the LWSS at.
	 * @param mirrorX If this is -1, flip the LWSS horizontally. Either -1 or 1.
	 * @see stampPattern
	 */
	private void stampLWSS(int x, int y, int mirrorX) {
		lifeArray[x-(2*mirrorX)][y-3].state = 1;
		postLifeArray[x-(2*mirrorX)][y-3].state = 1;
		
		lifeArray[x-(2*mirrorX)][y-1].state = 1;
		postLifeArray[x-(2*mirrorX)][y-1].state = 1;
		
		lifeArray[x-(1*mirrorX)][y].state = 1;
		postLifeArray[x-(1*mirrorX)][y].state = 1;
		
		lifeArray[x][y].state = 1;
		postLifeArray[x][y].state = 1;
		
		lifeArray[x+(1*mirrorX)][y].state = 1;
		postLifeArray[x+(1*mirrorX)][y].state = 1;
		
		lifeArray[x+(2*mirrorX)][y].state = 1;
		postLifeArray[x+(2*mirrorX)][y].state = 1;

		lifeArray[x+(2*mirrorX)][y-1].state = 1;
		postLifeArray[x+(2*mirrorX)][y-1].state = 1;
		
		lifeArray[x+(2*mirrorX)][y-2].state = 1;
		postLifeArray[x+(2*mirrorX)][y-2].state = 1;
		
		lifeArray[x+(1*mirrorX)][y-3].state = 1;
		postLifeArray[x+(1*mirrorX)][y-3].state = 1;
	}
	
	/**
	 * A helper method to <code>stampPattern</code> that draws an MWSS pattern to the screen. 
	 * If <code>mirrorX</code> is disabled (i.e. it has the value 1), then the MWSS will move left-to-right.
	 * If the pattern is mirrored in the x-axis, the MWSS will move right-to-left.
	 * @param x x-coordinate to draw the MWSS at.
	 * @param y y-coordinate to draw the MWSS at.
	 * @param mirrorX If this is -1, flip the MWSS horizontally. Either -1 or 1.
	 * @see stampPattern
	 */
	private void stampMWSS(int x, int y, int mirrorX) {
		lifeArray[x-(1*mirrorX)][y-4].state = 1;
		postLifeArray[x-(1*mirrorX)][y-4].state = 1;
		
		lifeArray[x+(1*mirrorX)][y-3].state = 1;
		postLifeArray[x+(1*mirrorX)][y-3].state = 1;	
		
		lifeArray[x+(2*mirrorX)][y-2].state = 1;
		postLifeArray[x+(2*mirrorX)][y-2].state = 1;
		
		lifeArray[x+(2*mirrorX)][y-1].state = 1;
		postLifeArray[x+(2*mirrorX)][y-1].state = 1;
		
		lifeArray[x+(2*mirrorX)][y].state = 1;
		postLifeArray[x+(2*mirrorX)][y].state = 1;
		
		lifeArray[x+(1*mirrorX)][y].state = 1;
		postLifeArray[x+(1*mirrorX)][y].state = 1;
		
		lifeArray[x][y].state = 1;
		postLifeArray[x][y].state = 1;
		
		lifeArray[x-(1*mirrorX)][y].state = 1;
		postLifeArray[x-(1*mirrorX)][y].state = 1;
		
		lifeArray[x-(2*mirrorX)][y].state = 1;
		postLifeArray[x-(2*mirrorX)][y].state = 1;
		
		lifeArray[x-(3*mirrorX)][y-1].state = 1;
		postLifeArray[x-(3*mirrorX)][y-1].state = 1;
		
		lifeArray[x-(3*mirrorX)][y-3].state = 1;
		postLifeArray[x-(3*mirrorX)][y-3].state = 1;
	}
	
	/**
	 * A helper method to <code>stampPattern</code> that draws an HWSS pattern to the screen. 
	 * If <code>mirrorX</code> is disabled (i.e. it has the value 1), then the HWSS will move left-to-right.
	 * If the pattern is mirrored in the x-axis, the HWSS will move right-to-left.
	 * @param x x-coordinate to draw the HWSS at.
	 * @param y y-coordinate to draw the HWSS at.
	 * @param mirrorX If this is -1, flip the HWSS horizontally. Either -1 or 1.
	 * @see stampPattern
	 */
	private void stampHWSS(int x, int y, int mirrorX) {
		lifeArray[x-(1*mirrorX)][y-4].state = 1;
		postLifeArray[x-(1*mirrorX)][y-4].state = 1;
		
		lifeArray[x-(2*mirrorX)][y-4].state = 1;
		postLifeArray[x-(2*mirrorX)][y-4].state = 1;
		
		lifeArray[x+(1*mirrorX)][y-3].state = 1;
		postLifeArray[x+(1*mirrorX)][y-3].state = 1;	
		
		lifeArray[x+(2*mirrorX)][y-2].state = 1;
		postLifeArray[x+(2*mirrorX)][y-2].state = 1;
		
		lifeArray[x+(2*mirrorX)][y-1].state = 1;
		postLifeArray[x+(2*mirrorX)][y-1].state = 1;
		
		lifeArray[x+(2*mirrorX)][y].state = 1;
		postLifeArray[x+(2*mirrorX)][y].state = 1;
		
		lifeArray[x+(1*mirrorX)][y].state = 1;
		postLifeArray[x+(1*mirrorX)][y].state = 1;
		
		lifeArray[x][y].state = 1;
		postLifeArray[x][y].state = 1;
		
		lifeArray[x-(1*mirrorX)][y].state = 1;
		postLifeArray[x-(1*mirrorX)][y].state = 1;
		
		lifeArray[x-(2*mirrorX)][y].state = 1;
		postLifeArray[x-(2*mirrorX)][y].state = 1;
		
		lifeArray[x-(3*mirrorX)][y].state = 1;
		postLifeArray[x-(3*mirrorX)][y].state = 1;
		
		lifeArray[x-(4*mirrorX)][y-1].state = 1;
		postLifeArray[x-(4*mirrorX)][y-1].state = 1;
		
		lifeArray[x-(4*mirrorX)][y-3].state = 1;
		postLifeArray[x-(4*mirrorX)][y-3].state = 1;
	}
	
	/** 
	 * A helper method to <code>stampPattern</code> that draws a pulsar pattern to the screen. 
	 * A pulsar is a 3-period oscillator.
	 * @param x x-coordinate to draw the pulsar at.
	 * @param y y-coordinate to draw the pulsar at.
	 * @see stampPattern
	 */
	private void stampPulsar(int x, int y) {
		// Top-left segment.
		lifeArray[x-1][y-2].state = 1;
		postLifeArray[x-1][y-2].state = 1;
		
		lifeArray[x-1][y-3].state = 1;
		postLifeArray[x-1][y-3].state = 1;
		
		lifeArray[x-1][y-4].state = 1;
		postLifeArray[x-1][y-4].state = 1;
		
		lifeArray[x-2][y-1].state = 1;
		postLifeArray[x-2][y-1].state = 1;
		
		lifeArray[x-3][y-1].state = 1;
		postLifeArray[x-3][y-1].state = 1;
		
		lifeArray[x-4][y-1].state = 1;
		postLifeArray[x-4][y-1].state = 1;
				
		lifeArray[x-2][y-6].state = 1;
		postLifeArray[x-2][y-6].state = 1;
		
		lifeArray[x-3][y-6].state = 1;
		postLifeArray[x-3][y-6].state = 1;
		
		lifeArray[x-4][y-6].state = 1;
		postLifeArray[x-4][y-6].state = 1;
		
		lifeArray[x-6][y-2].state = 1;
		postLifeArray[x-6][y-2].state = 1;
		
		lifeArray[x-6][y-3].state = 1;
		postLifeArray[x-6][y-3].state = 1;
		
		lifeArray[x-6][y-4].state = 1;
		postLifeArray[x-6][y-4].state = 1;
		
		// Top-right segment.
		lifeArray[x+1][y-2].state = 1;
		postLifeArray[x+1][y-2].state = 1;
		
		lifeArray[x+1][y-3].state = 1;
		postLifeArray[x+1][y-3].state = 1;
		
		lifeArray[x+1][y-4].state = 1;
		postLifeArray[x+1][y-4].state = 1;
		
		lifeArray[x+2][y-1].state = 1;
		postLifeArray[x+2][y-1].state = 1;
		
		lifeArray[x+3][y-1].state = 1;
		postLifeArray[x+3][y-1].state = 1;
		
		lifeArray[x+4][y-1].state = 1;
		postLifeArray[x+4][y-1].state = 1;
				
		lifeArray[x+2][y-6].state = 1;
		postLifeArray[x+2][y-6].state = 1;
		
		lifeArray[x+3][y-6].state = 1;
		postLifeArray[x+3][y-6].state = 1;
		
		lifeArray[x+4][y-6].state = 1;
		postLifeArray[x+4][y-6].state = 1;
		
		lifeArray[x+6][y-2].state = 1;
		postLifeArray[x+6][y-2].state = 1;
		
		lifeArray[x+6][y-3].state = 1;
		postLifeArray[x+6][y-3].state = 1;
		
		lifeArray[x+6][y-4].state = 1;
		postLifeArray[x+6][y-4].state = 1;
		
		// Bottom-right segment.
		lifeArray[x+1][y+2].state = 1;
		postLifeArray[x+1][y+2].state = 1;
		
		lifeArray[x+1][y+3].state = 1;
		postLifeArray[x+1][y+3].state = 1;
		
		lifeArray[x+1][y+4].state = 1;
		postLifeArray[x+1][y+4].state = 1;
		
		lifeArray[x+2][y+1].state = 1;
		postLifeArray[x+2][y+1].state = 1;
		
		lifeArray[x+3][y+1].state = 1;
		postLifeArray[x+3][y+1].state = 1;
		
		lifeArray[x+4][y+1].state = 1;
		postLifeArray[x+4][y+1].state = 1;
				
		lifeArray[x+2][y+6].state = 1;
		postLifeArray[x+2][y+6].state = 1;
		
		lifeArray[x+3][y+6].state = 1;
		postLifeArray[x+3][y+6].state = 1;
		
		lifeArray[x+4][y+6].state = 1;
		postLifeArray[x+4][y+6].state = 1;
		
		lifeArray[x+6][y+2].state = 1;
		postLifeArray[x+6][y+2].state = 1;
		
		lifeArray[x+6][y+3].state = 1;
		postLifeArray[x+6][y+3].state = 1;
		
		lifeArray[x+6][y+4].state = 1;
		postLifeArray[x+6][y+4].state = 1;
		
		// Bottom-left segment.
		lifeArray[x-1][y+2].state = 1;
		postLifeArray[x-1][y+2].state = 1;
		
		lifeArray[x-1][y+3].state = 1;
		postLifeArray[x-1][y+3].state = 1;
		
		lifeArray[x-1][y+4].state = 1;
		postLifeArray[x-1][y+4].state = 1;
		
		lifeArray[x-2][y+1].state = 1;
		postLifeArray[x-2][y+1].state = 1;
		
		lifeArray[x-3][y+1].state = 1;
		postLifeArray[x-3][y+1].state = 1;
		
		lifeArray[x-4][y+1].state = 1;
		postLifeArray[x-4][y+1].state = 1;
				
		lifeArray[x-2][y+6].state = 1;
		postLifeArray[x-2][y+6].state = 1;
		
		lifeArray[x-3][y+6].state = 1;
		postLifeArray[x-3][y+6].state = 1;
		
		lifeArray[x-4][y+6].state = 1;
		postLifeArray[x-4][y+6].state = 1;
		
		lifeArray[x-6][y+2].state = 1;
		postLifeArray[x-6][y+2].state = 1;
		
		lifeArray[x-6][y+3].state = 1;
		postLifeArray[x-6][y+3].state = 1;
		
		lifeArray[x-6][y+4].state = 1;
		postLifeArray[x-6][y+4].state = 1;
		
		// Remove the cell at the centre (if it was drawn by mousePressed).
		lifeArray[x][y].state = 0;
		postLifeArray[x][y].state = 0;
	}
	
	/** 
	 * A helper method to <code>stampPattern</code> that draws a pentadecathlon pattern to the screen. 
	 * A pentadecathlon is a 15-period oscillator.
	 * @param x x-coordinate to draw the pentadecathlon at.
	 * @param y y-coordinate to draw the pentadecathlon at.
	 * @see stampPattern
	 */
	private void stampPentadecathlon(int x, int y) {
		lifeArray[x][y].state = 1;
		postLifeArray[x][y].state = 1;
		
		lifeArray[x+1][y].state = 1;
		postLifeArray[x+1][y].state = 1;
		
		lifeArray[x+2][y].state = 1;
		postLifeArray[x+2][y].state = 1;
		
		lifeArray[x+3][y-1].state = 1;
		postLifeArray[x+3][y-1].state = 1;
		
		lifeArray[x+3][y+1].state = 1;
		postLifeArray[x+3][y+1].state = 1;
		
		lifeArray[x+4][y].state = 1;
		postLifeArray[x+4][y].state = 1;
		
		lifeArray[x+5][y].state = 1;
		postLifeArray[x+5][y].state = 1;
		
		lifeArray[x-1][y].state = 1;
		postLifeArray[x-1][y].state = 1;
		
		lifeArray[x-2][y-1].state = 1;
		postLifeArray[x-2][y-1].state = 1;
		
		lifeArray[x-2][y+1].state = 1;
		postLifeArray[x-2][y+1].state = 1;
		
		lifeArray[x-3][y].state = 1;
		postLifeArray[x-3][y].state = 1;
		
		lifeArray[x-4][y].state = 1;
		postLifeArray[x-4][y].state = 1;
	}
	
	/** 
	 * A helper method to <code>stampPattern</code> that draws a tumbler pattern to the screen. 
	 * A tumbler is a 14-period oscillator.
	 * @param x x-coordinate to draw the tumbler at.
	 * @param y y-coordinate to draw the tumbler at.
	 * @see stampPattern
	 */
	private void stampTumbler(int x, int y) {
		// Left side.
		lifeArray[x-1][y+1].state = 1;
		postLifeArray[x-1][y+1].state = 1;
		
		lifeArray[x-2][y+1].state = 1;
		postLifeArray[x-2][y+1].state = 1;
		
		lifeArray[x-2][y].state = 1;
		postLifeArray[x-2][y].state = 1;
		
		lifeArray[x-1][y-1].state = 1;
		postLifeArray[x-1][y-1].state = 1;
		
		lifeArray[x-2][y-2].state = 1;
		postLifeArray[x-2][y-2].state = 1;
		
		lifeArray[x-3][y-3].state = 1;
		postLifeArray[x-3][y-3].state = 1;
		
		lifeArray[x-4][y-2].state = 1;
		postLifeArray[x-4][y-2].state = 1;
		
		lifeArray[x-4][y-1].state = 1;
		postLifeArray[x-4][y-1].state = 1;
		
		// Right side.
		lifeArray[x+1][y+1].state = 1;
		postLifeArray[x+1][y+1].state = 1;
		
		lifeArray[x+2][y+1].state = 1;
		postLifeArray[x+2][y+1].state = 1;
		
		lifeArray[x+2][y].state = 1;
		postLifeArray[x+2][y].state = 1;
		
		lifeArray[x+1][y-1].state = 1;
		postLifeArray[x+1][y-1].state = 1;
		
		lifeArray[x+2][y-2].state = 1;
		postLifeArray[x+2][y-2].state = 1;
		
		lifeArray[x+3][y-3].state = 1;
		postLifeArray[x+3][y-3].state = 1;
		
		lifeArray[x+4][y-2].state = 1;
		postLifeArray[x+4][y-2].state = 1;
		
		lifeArray[x+4][y-1].state = 1;
		postLifeArray[x+4][y-1].state = 1;
		
		// Remove the cell at the centre (if it was drawn by mousePressed).
		lifeArray[x][y].state = 0;
		postLifeArray[x][y].state = 0;	
	}
	
	/** 
	 * A helper method to <code>stampPattern</code> that draws a figure eight pattern to the screen. 
	 * A figure eight is an 8-period oscillator.
	 * @param x x-coordinate to draw the figure eight at.
	 * @param y y-coordinate to draw the figure eight at.
	 * @see stampPattern
	 */
	private void stampFigureEight(int x, int y) {
		lifeArray[x-1][y+1].state = 1;
		postLifeArray[x-1][y+1].state = 1;
		
		lifeArray[x-2][y].state = 1;
		postLifeArray[x-2][y].state = 1;
		
		lifeArray[x+1][y-1].state = 1;
		postLifeArray[x+1][y-1].state = 1;
		
		lifeArray[x][y-2].state = 1;
		postLifeArray[x][y-2].state = 1;
		
		lifeArray[x-2][y-2].state = 1;
		postLifeArray[x-2][y-2].state = 1;
		
		lifeArray[x-2][y-3].state = 1;
		postLifeArray[x-2][y-3].state = 1;
		
		lifeArray[x-3][y-2].state = 1;
		postLifeArray[x-3][y-2].state = 1;
		
		lifeArray[x-3][y-3].state = 1;
		postLifeArray[x-3][y-3].state = 1;
		
		lifeArray[x+1][y+1].state = 1;
		postLifeArray[x+1][y+1].state = 1;
		
		lifeArray[x+1][y+2].state = 1;
		postLifeArray[x+1][y+2].state = 1;
		
		lifeArray[x+2][y+1].state = 1;
		postLifeArray[x+2][y+1].state = 1;
		
		lifeArray[x+2][y+2].state = 1;
		postLifeArray[x+2][y+2].state = 1;
		
		// Remove the cell at the centre (if it was drawn by mousePressed).
		lifeArray[x][y].state = 0;
		postLifeArray[x][y].state = 0;
	}
	
	/** 
	 * A helper method to <code>stampPattern</code> that draws a phoenix pattern to the screen. 
	 * The phoenix is a special type of period-2 oscillator.
	 * @param x x-coordinate to draw the figure eight at.
	 * @param y y-coordinate to draw the figure eight at.
	 * @see stampPattern
	 */
	private void stampPhoenix(int x, int y) {
		lifeArray[x-2][y].state = 1;
		postLifeArray[x-2][y].state = 1;

		lifeArray[x-3][y].state = 1;
		postLifeArray[x-3][y].state = 1;
		
		lifeArray[x-1][y-2].state = 1;
		postLifeArray[x-1][y-2].state = 1;
		
		lifeArray[x+1][y-2].state = 1;
		postLifeArray[x+1][y-2].state = 1;
		
		lifeArray[x+1][y-3].state = 1;
		postLifeArray[x+1][y-3].state = 1;
		
		lifeArray[x+3][y-1].state = 1;
		postLifeArray[x+3][y-1].state = 1;
		
		lifeArray[x+3][y+1].state = 1;
		postLifeArray[x+3][y+1].state = 1;
		
		lifeArray[x+4][y+1].state = 1;
		postLifeArray[x+4][y+1].state = 1;
		
		lifeArray[x+2][y+3].state = 1;
		postLifeArray[x+2][y+3].state = 1;
		
		lifeArray[x][y+3].state = 1;
		postLifeArray[x][y+3].state = 1;
		
		lifeArray[x][y+3].state = 1;
		postLifeArray[x][y+3].state = 1;
		
		lifeArray[x][y+4].state = 1;
		postLifeArray[x][y+4].state = 1;
		
		lifeArray[x-2][y+2].state = 1;
		postLifeArray[x-2][y+2].state = 1;
		
		// Remove the cell at the centre (if it was drawn by mousePressed).
		lifeArray[x][y].state = 0;
		postLifeArray[x][y].state = 0;
	}
}
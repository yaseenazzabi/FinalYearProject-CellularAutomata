package visualisingCellularAutomata;

/**
 * Cells are what the simulation operates on. They are either alive (state 1), or dead (state 0).
 * Live cells appear with Graphics colour, while dead cells are not drawn.
 */
public class Cell {
	/** The state of the Cell. */
	int state; 
	/** The number of generations the Cell has survived. */
	int age;
	// NOTE: state is saved as an integer as it is both a better representation of cell state than a boolean (this is subjective),
	// and it offers greater potential expandability to the program than a boolean (cellular automata can have more than 2 states for cells).
	
	/**
	 * Cell constructor which allows for explicit assignment of its initial <code>state</code> and <code>age</code>.
	 * @param state Explicit assignment of cell state.
	 * @param age Explicit assignment of cell age.
	 */
	Cell(int state, int age) {
		this.state = state;
		this.age = age;
	}
	
	/**
	 * Default Cell constructor which initialises a cell with a <code>state</code> of 0 and an <code>age</code> of 0. Takes no arguments.
	 */
	Cell() {
		this.state = 0;
		this.age = 0;
	}
}
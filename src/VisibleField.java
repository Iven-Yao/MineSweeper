// Name:Chih-Ken Yao
// USC NetID:7958315200
// CS 455 PA3
// Fall 2021


/**
 VisibleField class
 This is the data that's being displayed at any one point in the game (i.e., visible field, because it's what the
 user can see about the minefield). Client can call getStatus(row, col) for any square.
 It actually has data about the whole current state of the game, including
 the underlying mine field (getMineField()).  Other accessors related to game status: numMinesLeft(), isGameOver().
 It also has mutators related to actions the player could do (resetGameDisplay(), cycleGuess(), uncover()),
 and changes the game state accordingly.

 It, along with the MineField (accessible in mineField instance variable), forms
 the Model for the game application, whereas GameBoardPanel is the View and Controller, in the MVC design pattern.
 It contains the MineField that it's partially displaying.  That MineField can be accessed (or modified) from
 outside this class via the getMineField accessor.
 */
public class VisibleField {
   // ----------------------------------------------------------
   // The following public constants (plus numbers mentioned in comments below) are the possible states of one
   // location (a "square") in the visible field (all are values that can be returned by public method
   // getStatus(row, col)).

   // The following are the covered states (all negative values):
   public static final int COVERED = -1;   // initial value of all squares
   public static final int MINE_GUESS = -2;
   public static final int QUESTION = -3;

   // The following are the uncovered states (all non-negative values):

   // values in the range [0,8] corresponds to number of mines adjacent to this square

   public static final int MINE = 9;      // this loc is a mine that hasn't been guessed already (end of losing game)
   public static final int INCORRECT_GUESS = 10;  // is displayed a specific way at the end of losing game
   public static final int EXPLODED_MINE = 11;   // the one you uncovered by mistake (that caused you to lose)
   // ----------------------------------------------------------

   // <put instance variables here>
   // representation invariant:
   // 1. mineField should not be null
   // 2. status should not be null
   // 3. status size should be equal to mineField.numRows() * mineField.numCols()
   // 4. guessCount should be >= 0
   private MineField mineField;
   private boolean isLosing;
   private int[][] status;
   private int guessCount;


   /**
    Create a visible field that has the given underlying mineField.
    The initial state will have all the mines covered up, no mines guessed, and the game
    not over.
    @param mineField  the minefield to use for for this VisibleField
    */
   public VisibleField(MineField mineField) {
      // initialize instance variables
      this.mineField = mineField;
      this.isLosing = false;
      // visible status 2d array has the same size of mineField
      this.status = new int[mineField.numRows()][mineField.numCols()];
      // visible status are all covered at first
      for(int i = 0; i < mineField.numRows(); i++) {
         for(int j = 0; j < mineField.numCols(); j++) {
            status[i][j] = -1;
         }
      }
      this.guessCount = 0;

   }


   /**
    Reset the object to its initial state (see constructor comments), using the same underlying
    MineField.
    */
   public void resetGameDisplay() {
      // reset the status to all covered, isLosing to false, guessCount to 0
      this.isLosing = false;
      this.guessCount = 0;
      for(int i = 0; i < mineField.numRows(); i++) {
         for(int j = 0; j < mineField.numCols(); j++) {
            status[i][j] = -1;
         }
      }
   }


   /**
    Returns a reference to the mineField that this VisibleField "covers"
    @return the minefield
    */
   public MineField getMineField() {
      return mineField;
   }


   /**
    Returns the visible status of the square indicated.
    @param row  row of the square
    @param col  col of the square
    @return the status of the square at location (row, col).  See the public constants at the beginning of the class
    for the possible values that may be returned, and their meanings.
    PRE: getMineField().inRange(row, col)
    */
   public int getStatus(int row, int col) {
      return status[row][col];
   }


   /**
    Returns the the number of mines left to guess.  This has nothing to do with whether the mines guessed are correct
    or not.  Just gives the user an indication of how many more mines the user might want to guess.  This value can
    be negative, if they have guessed more than the number of mines in the minefield.
    @return the number of mines left to guess.
    */
   public int numMinesLeft() {
      return mineField.numMines()-guessCount;
   }


   /**
    Cycles through covered states for a square, updating number of guesses as necessary.  Call on a COVERED square
    changes its status to MINE_GUESS; call on a MINE_GUESS square changes it to QUESTION;  call on a QUESTION square
    changes it to COVERED again; call on an uncovered square has no effect.
    @param row  row of the square
    @param col  col of the square
    PRE: getMineField().inRange(row, col)
    */
   public void cycleGuess(int row, int col) {
      // while right click on a specific cell
      // when the cell's covered, it becomes a guess status, and add 1 to guessCount
      if(status[row][col] == COVERED) {
         status[row][col] = MINE_GUESS;
         guessCount++;
      }
      // when the cell's guess status, it becomes a question status, and minus 1 to guessCount
      else if(status[row][col] == MINE_GUESS){
         status[row][col] = QUESTION;
         guessCount--;
      }
      // when the cell's question status, it becomes a covered cell
      else if(status[row][col] == QUESTION){
         status[row][col] = COVERED;
      }
      // otherwise, do nothing.
   }


   /**
    Uncovers this square and returns false iff you uncover a mine here.
    If the square wasn't a mine or adjacent to a mine it also uncovers all the squares in
    the neighboring area that are also not next to any mines, possibly uncovering a large region.
    Any mine-adjacent squares you reach will also be uncovered, and form
    (possibly along with parts of the edge of the whole field) the boundary of this region.
    Does not uncover, or keep searching through, squares that have the status MINE_GUESS.
    Note: this action may cause the game to end: either in a win (opened all the non-mine squares)
    or a loss (opened a mine).
    @param row  of the square
    @param col  of the square
    @return false   iff you uncover a mine at (row, col)
    PRE: getMineField().inRange(row, col)
    */
   public boolean uncover(int row, int col) {
      // if uncover the square with mine
      if(mineField.hasMine(row, col)) {
         // we lose
         isLosing = true;
         // change the uncovered square to exploded_mine status
         status[row][col] = EXPLODED_MINE;
         return false;
      }

      // it's not a mine square, change the status to how many mines adjacent to this square.
      status[row][col] = mineField.numAdjacentMines(row, col);
      // if numAdjacentMine is 0, we will also uncover all its adjacent square if it's covered or question
      if(mineField.numAdjacentMines(row, col) == 0) {
         for(int i = -1; i <=1; i++) {
            for(int j = -1; j <=1; j++) {
               if(mineField.inRange(row+i, col+j)) {
                  if (status[row + i][col + j] == COVERED || status[row + i][col + j] == QUESTION) {
                     uncover(row+i, col+j);
                  }
               }
            }
         }
      }
      return true;
   }


   /**
    Returns whether the game is over.
    (Note: This is not a mutator.)
    @return whether game over
    */
   public boolean isGameOver() {
      // if we lose, update the status as following
      if(isLosing){
         for(int i = 0; i < mineField.numRows(); i++) {
            for(int j = 0; j < mineField.numCols(); j++) {
               // update the covered mine to mine status
               if(mineField.hasMine(i, j)) {
                  if(status[i][j] == COVERED) {
                     status[i][j] = MINE;
                  }
               }
               // update the wrong guess square to incorrect_guess status
               else {
                  if(status[i][j] == MINE_GUESS) {
                     status[i][j] = INCORRECT_GUESS;
                  }
               }
            }
         }
         return true;
      }

      // else, checking if we already uncovered every non mine square,
      // if yes, we won
      for(int i = 0; i < mineField.numRows(); i++) {
         for(int j = 0; j < mineField.numCols(); j++) {
            // if there's a square that's covered and has no mine, indicates that game is not over yet
            if(!isUncovered(i, j) && !getMineField().hasMine(i,j)) {
               return false;
            }
         }
      }

      for(int i = 0; i < mineField.numRows(); i++) {
         for(int j = 0; j < mineField.numCols(); j++) {
            if(!isUncovered(i,j)) {
               status[i][j] = MINE_GUESS;
            }
         }
      }

      return true;
   }


   /**
    Returns whether this square has been uncovered.  (i.e., is in any one of the uncovered states,
    vs. any one of the covered states).
    @param row of the square
    @param col of the square
    @return whether the square is uncovered
    PRE: getMineField().inRange(row, col)
    */
   public boolean isUncovered(int row, int col) {
      if(status[row][col] < 0) {
         return false;
      }
      return true;
   }


   // <put private methods here>

}

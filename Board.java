public class Board
{
    public enum Sides
    {
        Blank,
        O,
        X
    }

    private Sides[][] board = {{Sides.Blank, Sides.Blank, Sides.Blank}, {Sides.Blank, Sides.Blank, Sides.Blank}, {Sides.Blank, Sides.Blank, Sides.Blank}};

    public Board() {}

    // sets and gets pieces on the board
    public void SetPiece(int x, int y, Sides side) {  board[x][y] = side;  }
    public Sides GetPiece(int x, int y) {  return board[x][y];  }

    // sets and gets the entire board
    public void SetBoard(Sides[][] board) {  this.board = board;  }
    public Sides[][] GetBoard() {  return board;  }

    // The state of win/loose/tie/on going
    public enum WinStates
    {
        None,
        WinX,
        WinO,
        Tie
    }

    // gets the win condition for the line
    private WinStates GetLine(int startX, int startY, int dirX, int dirY)
    {
        // gets the pieces along the path
        Sides piece1 = board[startX][startY];
        Sides piece2 = board[startX + dirX][startY + dirY];
        Sides piece3 = board[startX + dirX * 2][startY + dirY * 2];

        // checks the win conditions
        if (piece1 == piece2 && piece2 == piece3)
        {
            if      (piece1 == Sides.X) return WinStates.WinX;
            else if (piece1 == Sides.O) return WinStates.WinO;
        }

        // no win was found
        return WinStates.None;
    }

    // gets the state of the game
    public WinStates CheckState()
    {
        WinStates winState;

        // checking horizontal lines
        for (int y = 0; y < 3; y++)
        {
            winState = GetLine(0, y, 1, 0);
            if (winState != WinStates.None) return winState;
        }

        // checking vertical lines
        for (int x = 0; x < 3; x++)
        {
            winState = GetLine(x, 0, 0, 1);
            if (winState != WinStates.None) return winState;
        }

        // checking diagonals
        winState = GetLine(0, 2, 1, -1);
        if (winState != WinStates.None) return winState;

        winState = GetLine(0, 0, 1, 1);
        if (winState != WinStates.None) return winState;

        // checking for a tie
        boolean blank = false;
        for (int x = 0; x < 3; x++)
        {
            for (int y = 0; y < 3; y++)
            {
                if (board[x][y] == Sides.Blank)
                {
                    // there is a blank
                    blank = true;

                    // leaving the loop
                    x = 10;
                    y = 10;
                }
            }
        }

        if (!blank) return WinStates.Tie;

        // there is no win detected
        return WinStates.None;
    }

    // evaluates the quality of a position
    public int EvalQualityOfPos(Sides side)
    {
        WinStates winState = CheckState();
        if (winState == WinStates.None) return 0;
        if (winState == WinStates.Tie) return -1;
        if ((winState == WinStates.WinX && side == Sides.O) || (winState == WinStates.WinO && side == Sides.X)) return -2;
        return 2;
    }
}

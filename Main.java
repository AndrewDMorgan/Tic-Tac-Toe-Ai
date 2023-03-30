import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        // use a branching algorithm to track where wins and looses are using the EvalQualityOfPos function under Board

        // the scanner for user input
        Scanner scanner = new Scanner(System.in);

        // the board
        Board board = new Board();

        // whose turn it is (0 == X and 1 == O, the player is X)
        int turn = (int) Math.round(Math.random());  // choosing a random player to start the game

        // the chars that go with each side
        HashMap<Board.Sides, String> sideChars = new HashMap<>();
        sideChars.put(Board.Sides.Blank, "[ ]");
        sideChars.put(Board.Sides.X    , "[X]");
        sideChars.put(Board.Sides.O    , "[O]");

        // running the game while there is no win condition
        Board.WinStates gameState = Board.WinStates.None;
        while (gameState == Board.WinStates.None)
        {
            // checking if the player or AI is going
            if (turn == 0)  // players turn
            {
                System.out.println("(Player) X's turn");

                // getting the players choice of position
                int x = 0, y = 0;
                boolean validChoice = false;
                while (!validChoice)
                {
                    System.out.println("X position");
                    x = scanner.nextInt() - 1;
                    System.out.println("Y position");
                    y = scanner.nextInt() - 1;

                    // checking if the choice is valid
                    if (x >= 0 && x < 3 && y >= 0 && y < 3) validChoice = board.GetPiece(x, y) == Board.Sides.Blank;
                }

                // placing the piece
                board.SetPiece(x, y, Board.Sides.X);
            }
            else  // AI's turn
            {
                System.out.println("(AI) O's turn");

                // the best choice
                float bestQuality = -10;
                ArrayList<Integer> bestX = new ArrayList<>();
                ArrayList<Integer> bestY = new ArrayList<>();

                // looping through all cells
                for (int x = 0; x < 3; x++)
                {
                    for (int y = 0; y < 3; y++)
                    {
                        // checking if the cell is empty
                        if (board.GetPiece(x, y) == Board.Sides.Blank)
                        {
                            // getting the quality of the branch/choice
                            Board newBoard = new Board();
                            newBoard.SetBoard(CloneArray(board.GetBoard()));
                            newBoard.SetPiece(x, y, Board.Sides.O);
                            float quality = GetBranchQuality(newBoard, Board.Sides.O, 1 - turn, 0);

                            // checking if it's better than the current best
                            if (quality > bestQuality)
                            {
                                // setting the best quality and the position of it
                                bestQuality = quality;
                                bestX = new ArrayList<>();
                                bestY = new ArrayList<>();
                            }
                            if (quality >= bestQuality)
                            {
                                bestX.add(x);
                                bestY.add(y);
                            }
                        }
                    }
                }

                // choosing a random index from bestX and bestY
                int index = Math.min((int) (Math.random() * bestX.size()), bestX.size() - 1);

                // placing the piece
                board.SetPiece(bestX.get(index), bestY.get(index), Board.Sides.O);
            }

            // rendering the board
            for (int y = 0; y < 3; y++)
            {
                for (int x = 0; x < 3; x++)
                {
                    System.out.print(sideChars.get(board.GetPiece(x, y)));
                }
                System.out.println("");
            }

            // updating the win state
            gameState = board.CheckState();

            // updating whose turn it is
            turn = 1 - turn;  // 1 - 0 = 1; 1 - 1 = 0; should cycle between 0 and 1
        }

        if      (gameState == Board.WinStates.Tie)  System.out.println("Tie...");
        else if (gameState == Board.WinStates.WinX) System.out.println("X won!!!");
        else if (gameState == Board.WinStates.WinO) System.out.println("O won!!!");
    }

    // clones the board correctly
    public static Board.Sides[][] CloneArray(Board.Sides[][] board)
    {
        // the new board
        Board.Sides[][] newBoard = new Board.Sides[3][3];

        // cloning all elements
        for (int x = 0; x < 3; x++)
        {
            // copping over this segment of the array
            System.arraycopy(board[x], 0, newBoard[x], 0, 3);
        }

        // returning the cloned array
        return newBoard;
    }

    // gets the quality of a branch (turn = 0 is X and 1 is O)å
    public static float GetBranchQuality(Board board, Board.Sides AI, int turn, int depth)
    {
        // the scalar for prioritizing quick wins over long wins
        float depthScalar = 0.5f;
        float dumbness = 0.f;

        // getting the side
        Board.Sides side = turn == 0 ? Board.Sides.X : Board.Sides.O;

        // something went wrong or there is a win/tie/loose
        Board.WinStates winState = board.CheckState();
        if (depth > 20 || winState != Board.WinStates.None) return board.EvalQualityOfPos(AI) / (depth * depthScalar + 1);

        // for getting the quality of all branches
        ArrayList<Float> branches = new ArrayList<>();

        // looping through all spots and finding a valid choice
        for (int x = 0; x < 3; x++)
        {
            for (int y = 0; y < 3; y++)
            {
                // checking that the cell is empty
                if (board.GetPiece(x, y) == Board.Sides.Blank)
                {
                    // adding this as a new branch
                    Board newBoard = new Board();
                    newBoard.SetBoard(CloneArray(board.GetBoard()));
                    newBoard.SetPiece(x, y, side);
                    branches.add(GetBranchQuality(newBoard, AI, 1 - turn, depth + 1));
                }
            }
        }

        // checking if there are no valid moves for the player (which would mean the game is over/tied)
        if (branches.size() == 0) return board.EvalQualityOfPos(AI) / (depth * depthScalar + 1);

        // checking the worst choice the player can choose for the AI's outcome (best choice for the player)
        if (turn == 0)  // X's turn aka the player
        {
            // finding the worst option for the AI (best move for the player)
            float worst = branches.get(0);
            for (int i = 1; i < branches.size(); i++)
            {
                // checking if a new worst has been found
                if (branches.get(i) < worst) worst = branches.get(i);
            }

            // returning the worst
            return worst / (depth * depthScalar + 1);
        }

        // O's turn aka the AI

        // finding the worst option for the AI (best move for the player)
        float best = branches.get(0);
        for (int i = 1; i < branches.size(); i++)
        {
            // checking if a new worst has been found
            if (branches.get(i) > best) best = branches.get(i);
        }

        // returning the worst
        return best / (depth * depthScalar + 1) + (float) (Math.random() - 0.5) * dumbness;
    }
}

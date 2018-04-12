package com.sunflower.fifteenwidget;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by SuperComputer on 3/7/2017.
 */

// represents location a cell
class SuperPoint
{
    int x;
    int y;

    public SuperPoint()
    {
        x = y = 0;
    }

    static public SuperPoint Empty()
    {
        return new SuperPoint(GameBoard.OutOfReach, GameBoard.OutOfReach);
    }

    public SuperPoint(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    public String toString()
    {
        return "("+ this.x + "," + this.y + ")";
    }
    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof SuperPoint == false)
            return false;

        SuperPoint p = (SuperPoint)obj;

        return this.x == p.x && this.y == p.y;
    }
}

interface Listenable<T>
{
    void addListener(T listener);
    void removeListener(T listener);
    ArrayList<T> getListeners();
}

class GameBoard
{
    private int [][]board;
    private int width = 0, height = 0;

    public final static int BlankTile = 0, OutOfReach = -666;

    public GameBoard(int x, int y)
    {
        this.width = x;
        this.height = y;

        this.board = new int[width][height];
    }

    // copy constr
    public GameBoard(GameBoard b1)
    {
        this(b1.getWidth(), b1.getHeight());

        for(int x = 0; x < width ; x++)
        {
            for(int y = 0; y < height; y++)
            {
                this.set(x, y, b1.get(x, y));
            } // for
        }
    }

    public boolean set(SuperPoint location, int value)
    {
        if(location.x > width || location.y > height || location.x < 0 || location.y < 0)
            return false;

        board[location.x][location.y] = value;

        return true;
    }

    public boolean set(int x, int y, int value)
    {
        if(x > width || y > height || x < 0 || y < 0)
            return false;

        board[x][y] = value;

        return true;
    }

    public int get(SuperPoint location)
    {
        if(location.x > width || location.y > height || location.x < 0 || location.y < 0)
            return OutOfReach;

        return board[location.x][location.y];
    }

    public int get(int x, int y)
    {
        if(x > width-1 || y > height-1 || x < 0 || y < 0)
            return OutOfReach;

        return board[x][y];
    }

    public int getWidth()
    {
        return width;
    }
    public int getHeight()
    {
        return height;
    }

    public SuperPoint getBlackTileIndex()
    {
        for(int x = 0; x < width ; x++)
        {
            for(int y = 0; y < height; y++)
            {
                if(board[x][y] == BlankTile)
                {
                    return new SuperPoint(x, y);
                }
            } // for
        }
        return SuperPoint.Empty();
    }

    public int[] toArray()
    {
        int[] res = new int[width * height];

        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width ; x++)
            {
                res[x + width * y] = board[x][y];
            } // for x
        }
        return res;
    }

    @Override
    public int hashCode()
    {
        int out=0;
        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width ; x++)
            {
                out= (out * width * height) + board[x][y];
            }
        }
        return out;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof GameBoard == false)
            return false;

        GameBoard b1 = (GameBoard)obj;

        if(b1.getHeight() != this.getHeight() || b1.getWidth() != this.getWidth())
            return false;

        for(int x = 0; x < width ; x++)
        {
            for(int y = 0; y < height; y++)
            {
                if(board[x][y] != b1.get(x, y))
                {
                    return false;
                }
            } // for
        }

        return true;
    }

    public void swap(SuperPoint first, SuperPoint second)
    {
        if(first == null || second == null || first.equals(second)) return;

        int temp = board[first.x][first.y];
        board[first.x][first.y] = board[second.x][second.y];
        board[second.x][second.y] = temp;
    }

    public ArrayList<SuperPoint> getAllPossibleMoveLocations()
    {
        ArrayList<SuperPoint> res = new ArrayList<>();

        SuperPoint blankTile = getBlackTileIndex();

        if(blankTile.x > 0)
            res.add(new SuperPoint(blankTile.x - 1, blankTile.y));
        if(blankTile.x < width - 1)
            res.add(new SuperPoint(blankTile.x + 1, blankTile.y));
        if(blankTile.y > 0)
            res.add(new SuperPoint(blankTile.x, blankTile.y - 1));
        if(blankTile.y < height - 1)
            res.add(new SuperPoint(blankTile.x, blankTile.y + 1));

        return res;
    }

    public String toString()
    {
        String res = "Board: \n";
        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width ; x++)
            {
                res += board[x][y] + " ";
            }
            res += "\r\n";
        }
        return res;
    }
}

public class FifteenGame implements Serializable, Listenable<FifteenGame.FifteenGameListener>
{
    private GameState gameState = GameState.InTheProcess;

    private int fieldWidth = 4;
    private int fieldHeight = 4;
    private GameBoard gameField;

    // list of listeners
    private ArrayList<FifteenGameListener> listeners = new ArrayList<>();

    public FifteenGame()
    {
        startTheGame();
    }

    // starts or restarts a new game
    public void startTheGame()
    {
        generateField(fieldWidth*fieldHeight*10);

        gameState = GameState.InTheProcess;
        for(FifteenGameListener listener: listeners)
            listener.gameStateChanged(this, gameState);
    }

    public void reset()
    {
        gameState = GameState.ForceEnd;
        for(FifteenGameListener listener: listeners)
            listener.gameStateChanged(this, gameState);

    }

    // returns -1 in case the tile could not be placed
    public int tryToMoveBlankTo(SuperPoint location)
    {
        if(gameState != GameState.InTheProcess) return -1;

        if(location.x > fieldWidth || location.y > fieldHeight || location.x < 0 || location.y < 0)
            return -1;

        if(location.x > 0 && gameField.get(location.x-1, location.y) == GameBoard.BlankTile)
        {
            swap(new SuperPoint(location.x-1, location.y), new SuperPoint(location.x, location.y));
            return gameField.get(location.x-1, location.y);
        }

        if(location.x < fieldWidth-1 && gameField.get(location.x+1, location.y) == GameBoard.BlankTile)
        {
            swap(new SuperPoint(location.x+1, location.y), new SuperPoint(location.x, location.y));
            return gameField.get(location.x+1, location.y);
        }
        if(location.y > 0 && gameField.get(location.x, location.y-1) == GameBoard.BlankTile)
        {
            swap(new SuperPoint(location.x, location.y-1), new SuperPoint(location.x, location.y));
            return gameField.get(location.x, location.y-1);
        }

        if(location.y < fieldHeight-1 && gameField.get(location.x, location.y+1) == GameBoard.BlankTile)
        {
            swap(new SuperPoint(location.x, location.y+1), new SuperPoint(location.x, location.y));
            return gameField.get(location.x, location.y+1);
        }

        return -1;
    }

    void generateField(int swapCount)
    {
        int stepCount = swapCount;
        this.gameField = new GameBoard(fieldWidth, fieldHeight);
        GameBoard startState = getVictoriousBoard();

        Random r = new Random();
        while (stepCount > 0)
        {
            ArrayList<SuperPoint> locations = startState.getAllPossibleMoveLocations();
            int j = r.nextInt(locations.size());
            startState.swap(startState.getBlackTileIndex(), locations.get(j));
            stepCount--;
        }
        this.gameField = startState;
    }

    boolean isLocked = false;
    public void setLocked(boolean locked)
    {
        if(isLocked && this.hasGameEnded()) return;

        isLocked = locked;
    }

    public GameBoard getVictoriousBoard()
    {
        GameBoard resBoard = new GameBoard(fieldWidth, fieldHeight);
        int count = 1;

        for(int y = 0; y < gameField.getHeight(); y++)
        {
            for(int x = 0; x < gameField.getWidth(); x++)
            {
                if(x * y < (fieldWidth-1) * (fieldHeight-1))
                    resBoard.set(x, y, count++);
                else resBoard.set(x, y, GameBoard.BlankTile);
            } // for y
        } // for x

        return resBoard;
    }

    public void swap(SuperPoint first, SuperPoint second)
    {
        if(gameState != GameState.InTheProcess)
            return;
        if(first.equals(second)) return;

        gameField.swap(first, second);

        if(gameState != GameState.Ended)
        {
            for(FifteenGameListener listener: listeners)
                listener.cellsSwapped(this, first, second);
        }

        checkVictory();
    }

    private Random rand = new Random();

    // shouldn't use it!
    public void shuffle(int num)
    {
        if(gameState == GameState.InTheProcess) return;


        for(int i = 0; i < num; i++)
        {
            SuperPoint p1 = new SuperPoint(), p2 = new SuperPoint();

            do // make sure the tiles are different
            {
                p1 = new SuperPoint(rand.nextInt(fieldWidth), rand.nextInt(fieldHeight));
                p2 = new SuperPoint(rand.nextInt(fieldWidth), rand.nextInt(fieldHeight));
            }
            while (p1.equals(p2));

            //swap(p1, p2);
            gameField.swap(p1, p2);
        }

    }

    public void checkVictory()
    {
        if(gameState != GameState.InTheProcess)
            return;

        boolean flag = true;
        int count = 1;
        outer: for(int y = 0; y < gameField.getHeight(); y++)
        {
            for(int x = 0; x < gameField.getWidth(); x++)
            {
                if(gameField.get(x, y) != count++ && gameField.get(x, y) != GameBoard.BlankTile)
                {
                    flag = false;
                    break outer;
                }
            } // for y
        } // for x

        if(flag)
        {
            gameState = GameState.Ended;
            for(FifteenGameListener listener: listeners)
                listener.gameStateChanged(this, gameState);
        }
    }

    public boolean hasGameEnded()
    {
        return gameState == GameState.Ended || gameState == GameState.ForceEnd;
    }

    public GameState getGameState()
    {
        return gameState;
    }

    public GameBoard getGameField()
    {
        return gameField;
    }

    public int getFieldWidth()
    {
        return fieldWidth;
    }

    public int getFieldHeight()
    {
        return fieldHeight;
    }

    @Override
    public void addListener(FifteenGameListener listener)
    {
        if(listener != null && !listeners.contains(listener))
            listeners.add(listener);
    }

    @Override
    public void removeListener(FifteenGameListener listener)
    {
        if(listener != null)
            listeners.remove(listener);
    }

    @Override
    public ArrayList<FifteenGameListener> getListeners()
    {
        return listeners;
    }

    // listener interface for the game
    public interface FifteenGameListener
    {
        void cellsSwapped(FifteenGame source, SuperPoint first, SuperPoint secont);
        void fieldUpdated(FifteenGame source);
        void gameStateChanged(FifteenGame source, GameState newState);
    }

    public enum GameState
    {
        InTheProcess,
        Ended,
        ForceEnd
    }
}

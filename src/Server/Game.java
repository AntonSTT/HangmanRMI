package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Game extends Remote {
    public GameState startNewGame() throws RemoteException;
    public boolean endGame() throws RemoteException;

    public GameState guessLetter(Character c) throws RemoteException;
    public GameState guessWord(String word) throws RemoteException;

    public GameState continueNewWord() throws RemoteException;
    public boolean saveResult(String name) throws RemoteException;
    public String viewScores() throws RemoteException;
    public String getHint() throws RemoteException;
}

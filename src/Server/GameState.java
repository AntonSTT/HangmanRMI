package Server;

import java.io.Serializable;
import java.util.*;

public class GameState implements Serializable {
    public List<Character> charlist;
    public List<Character> progress;
    public int score;
    public int attempts;
    public boolean victory;
    public boolean defeat;
    public boolean error;

    public int scoreburn;

    public boolean isGameRunning(){
        return !victory && !defeat;
    }
}

package Client;

import Server.Game;

import javax.swing.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class GameClient {
    public static void main(String[] args) {
        try{
            Scanner in = new Scanner(System.in);
            in.useDelimiter("\\n");
            Registry registry = LocateRegistry.getRegistry(5000);
            Game gamestub = (Game) registry.lookup("Game");
            SwingUtilities.invokeLater(()->{
                new GameClientGUI(gamestub);
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

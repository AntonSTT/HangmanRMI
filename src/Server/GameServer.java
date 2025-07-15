package Server;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;

public class GameServer {
    public static void main(String[] args) {
        try{
            GameImpl game = new GameImpl();
            if( !UnicastRemoteObject.unexportObject(game, true)){
                System.err.println("Could not unexport object");
            }
            Game stub = (Game) UnicastRemoteObject.exportObject(game, 0);
            Registry registry = LocateRegistry.createRegistry(5000);
            registry.rebind("Game", stub);
            System.out.println("Игровой сервер запущен! ");

        }catch (Exception e){
            e.printStackTrace();
        }


    }



}

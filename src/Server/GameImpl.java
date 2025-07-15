package Server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GameImpl extends UnicastRemoteObject implements Game {

    private int maxattempts; //Максимальное количество ошибок - на случай изменения уровня сложности
    private int attempts; //текущее количество ошибок во время игры
    private int attemptburn; // горение попыток - сколько сгорит при неверном
    private String curWord; //текущее слово
    private String hint;
    private List<Character> progress; //прогресс игрока

    private boolean victory;
    private boolean defeat;
    private boolean nohint;



    private List<Integer> guessedWords; //уже угаданные слова - для продолжения игра
    private List<Character> characterList; //список букв, по мере игры уменьшается ля угадывания и обновляется с новым словом
    private int points; //очки - для рекорда

    private List<String> allwords; //все слова
    private List<String> allhints; //все подсказки - связаны со словами тем, что индексы одинаоквы

    private Random rand;

    public GameImpl() throws RemoteException {
        maxattempts = 8;
        attemptburn = 1;
        guessedWords = new ArrayList<>();
        characterList = new ArrayList<>();
        allwords = new ArrayList<>();
        allhints = new ArrayList<>();
        rand = new Random();
        loadWordsFromFile();
    }
    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public String getHint() throws RemoteException {
        return hint;
    }

    @Override
    public GameState startNewGame() throws RemoteException {
        try{
            int inin = rand.nextInt(allwords.size());
            curWord = allwords.get(inin);
            hint = allhints.get(inin);
            nohint = false;
            progress = new ArrayList<>();
            for(int i = 0; i < curWord.length(); i++){
                if (!(curWord.charAt(i) == ' ')){progress.add('-');}
                else {progress.add(' ');}

            }
            points = 0;
            attempts = maxattempts;
            victory = false;
            defeat = false;
            resetCharList();

            return takeGameState(false);

        }catch(Exception e){
            System.out.println(e.getMessage());
            return takeGameState(true);
        }

    }
    @Override
    public GameState guessLetter(Character c) throws RemoteException {
        c = Character.toLowerCase(c);
        if (curWord.contains(c.toString()) && characterList.contains(c)){
            int index = curWord.indexOf(c);  // Находим первое вхождение
            while (index != -1) {
                progress.set(index, c);            // Добавляем индекс в список
                index = curWord.indexOf(c, index + 1);  // Ищем следующее вхождение
                //System.out.println(index);
                //System.out.println(curWord);
            }
            points = points + 10;
            characterList.remove(c);
            if (!progress.contains('-')){
                points = points + 100;
                victory = true;
            }
        }else {
            attempts = attempts - attemptburn;
            if (attempts <= 0){
                defeat = true;
                for(int i = 0; i < curWord.length(); i++){
                    progress.set(i, curWord.charAt(i));
                }
            }
            else{
                characterList.remove(c);
            }
        }
        return takeGameState(false);
    }

    @Override
    public GameState guessWord(String word) throws RemoteException {
        word = word.toLowerCase();
        if(curWord.equals(word)){
            int unguessed = Collections.frequency(progress, '-');
            for(int i = 0; i < curWord.length(); i++){
                progress.set(i, curWord.charAt(i));
            }
            points = points + unguessed*12 + 100;
            victory = true;
        }
        else {
            attempts = attempts - attemptburn*2;
            if (attempts <= 0){
                defeat = true;
                for(int i = 0; i < curWord.length(); i++){
                    progress.set(i, curWord.charAt(i));
                }
            }
        }
        return takeGameState(false);
    }

    @Override
    public GameState continueNewWord() throws RemoteException {
        victory = false;
        defeat = false;
        guessedWords.add(allwords.indexOf(curWord));
        if(guessedWords.size() >= allwords.size()){
            nohint = true;
            //attemptburn++;
            guessedWords.clear();
        }
        if (guessedWords.size() % 3 ==0){
            attemptburn++;
        }
        List<Integer> available = new ArrayList<>();
        for (int i = 0; i < allwords.size(); i++){
            if(!guessedWords.contains(i)){
                available.add(i);
            }
        }
        int inin = available.get(rand.nextInt(available.size()));


        curWord = allwords.get(inin);
        hint = allhints.get(inin);
        progress = new ArrayList<>();
        for(int i = 0; i < curWord.length(); i++){
            if (!(curWord.charAt(i) == ' ')){progress.add('-');}
            else {progress.add(' ');}
        }
        resetCharList();
        attempts = attempts + 3;
        return takeGameState(false);
    }

    @Override
    public boolean endGame() throws RemoteException {
        attempts = 0;
        victory = false;
        defeat = false;
        progress = null;
        curWord = null;
        hint = null;
        points = 0;
        guessedWords.clear();
        characterList.clear();
        attemptburn = 1;
        nohint = false;
        return true;

    }

    @Override
    public boolean saveResult(String name) throws RemoteException {
        if(name.length() > 16){
            name = name.substring(0, 16);
        }else if(name.length() == 0){
            name = "AAAA";
        }
        String fileName = "src/Server/scores.txt";

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String record = String.format("%s | %d | %s", name, points, LocalDateTime.now().format(dateTimeFormatter));
        try{
            List<String> records = new ArrayList<String>();
            if(Files.exists(Paths.get(fileName))){
                records =Files.readAllLines(Paths.get(fileName));
            }
            System.out.println(records);

            records.add(record);

            if(records.size() > 1){
                records.sort((a, b) -> {
                    int pointsA = Integer.parseInt(a.split("\\|")[1].trim());
                    int pointsB = Integer.parseInt(b.split("\\|")[1].trim());
                    return Integer.compare(pointsB, pointsA); // Сортировка от большего к меньшему
                });
            }

            //System.out.println(records);

            PrintWriter pw = new PrintWriter(new FileWriter(fileName));
            for (String rec : records){
                pw.println(rec);
            }

            pw.close();
            return true;
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            return false;
        }

    }

    @Override
    public String viewScores() throws RemoteException {
        try{
            return new String(Files.readAllBytes(Paths.get("src/Server/scores.txt")));
        }catch(Exception e){
            System.out.println(e.getMessage());
            return "Нет данных";
        }
    }



    private void resetCharList(){
        characterList.clear();
        for (char c = 'а'; c <= 'е'; c++) {
            characterList.add(c);
            //System.out.println(c);
        }
        characterList.add('ё');
        for(char c = 'ж'; c <= 'я'; c++){
            characterList.add(c);
        }
    }

    private void loadWordsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/Server/words.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    allwords.add(parts[0].trim());
                    allhints.add(parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());

        }
    }

    private GameState takeGameState(boolean error){
        GameState gameState = new GameState();
        if (error){
            gameState.error = true;
        }else{
            gameState.charlist = characterList;
            gameState.attempts = attempts;
            gameState.defeat = defeat;
            gameState.victory = victory;
            gameState.score = points;
            gameState.progress = progress;
            gameState.error = false;
            gameState.scoreburn = attemptburn;
        }
        return gameState;



    }


}

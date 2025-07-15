package Client;

import Server.Game;
import Server.GameState;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.stream.Collectors;

public class GameClientGUI extends JFrame {
    private Game game;
    private GameState gameState;

    private JFrame frame;

    private JPanel cardPanel;
    private JPanel mainMenuPanel;
    private JLabel gameNameLabel;
    private JButton newGameButton;
    private JButton recordViewButton;
    private JButton exitButton;
    private JPanel gamePlayPanel;
    private JPanel infoPanel;
    private JLabel attemptLabel;
    private JPanel lowerGamePanel;
    private JPanel middleGamePanel;
    private JTextArea hintLabel;
    private JLabel wordHider;
    private JLabel availableLetters;
    private JTextField letterTextField;
    private JTextField wordTextField;
    private JButton guessLetterButton;
    private JButton guessWordButton;
    private JButton surrenderButton;
    private JLabel letterCost;
    private JLabel wordCost;
    private JPanel viewScoresPanel;
    private JTextArea recordArea;
    private JLabel recordBigLabel;
    private JButton backToMenuButton;
    private JLabel scoreLabelGame;
    private JTable scoresTable;

    public GameClientGUI(Game game) {

        this.game = game;
        initialize();

    }
    private void initialize() {
        setTitle("Поле чудес RMI - 6403 Старков Антон");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800,600);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);


        cardPanel.add(mainMenuPanel, "Main Menu");
        cardPanel.add(gamePlayPanel, "Game Play");
        cardPanel.add(viewScoresPanel, "View Scores");
        add(cardPanel);

        mainMenuSetUp();
        setUpGamePlayButtons();
        setUpScoreBoardButton();
        setExitButton();

        showPanel("Main Menu");



    }

    private void mainMenuSetUp(){
        newGameButton.addActionListener(e -> {
            startGame();
            showPanel("Game Play");
            updateGamePlayUI();
        });
        recordViewButton.addActionListener(e -> {
            updateScoreView();
            showPanel("View Scores");
        });

    }

    private void showPanel(String name){
        CardLayout cl = (CardLayout)(cardPanel.getLayout());
        cl.show(cardPanel, name);
    }



    private void updateGamePlayUI(){
        try{
            attemptLabel.setText(String.format("Попыток: %d",gameState.attempts));
            scoreLabelGame.setText(String.format("Очки: %d",gameState.score));
            letterCost.setText(String.format("Стоимость буквы: %d",gameState.scoreburn));
            wordCost.setText(String.format("Стоимость слова: %d",gameState.scoreburn*2));
            hintLabel.setText(game.getHint());
            StringBuilder wordDisplayer = new StringBuilder();
            for (Character c : gameState.progress){
                if(c.equals('-')){
                    wordDisplayer.append("_");
                }
                else if (c.equals(' ')){
                    wordDisplayer.append("   ");
                }
                else{
                    wordDisplayer.append(c.toString());
                }
                wordDisplayer.append(' ');

            }
            wordHider.setText(wordDisplayer.toString());
            String lettersText = gameState.charlist.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(" "));
            availableLetters.setText(lettersText);
            if(!gameState.isGameRunning()){
                endGameLogic();
            }

        }catch (Exception e){
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }


    }
    private void setUpGamePlayButtons(){
        surrenderButton.addActionListener(e -> {

            SurrenderDialog custmo = new SurrenderDialog(this, gameState.score, true);
            try{
                if (!custmo.dismissed){
                    game.endGame();
                }
            }catch (Exception ex){
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }

        });
        guessLetterButton.addActionListener(e -> {
            if (letterTextField.getText().equals("")) {
                JOptionPane.showMessageDialog(this, "Поле буквы пустое",
                        "Неправильный ввод", JOptionPane.PLAIN_MESSAGE);
            }else {
                guessLetter(letterTextField.getText());
                letterTextField.setText("");
                updateGamePlayUI();
            }
        });
        guessWordButton.addActionListener(e -> {
            if (wordTextField.getText().equals("")) {
                JOptionPane.showMessageDialog(this, "Поле слова пустое",
                        "Неправильный ввод", JOptionPane.PLAIN_MESSAGE);
            }
            else{
                guessWord(wordTextField.getText());
                wordTextField.setText("");
                updateGamePlayUI();
            }
        });
    }

    private void setUpScoreBoardButton(){
        backToMenuButton.addActionListener(e -> {
            showPanel("Main Menu");
        });
    }

    private void setExitButton(){
        exitButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    frame,
                    "Вы точно хотите выйти?",
                    "Подтверждение выхода",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);  // или frame.dispose() для закрытия только окна
            }
        });
    }

    private void updateScoreView(){
        try{
            String[] columns = {"Псевдоним", "Очки", "Время"};
            String record = game.viewScores();
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            model.addRow(columns);
            String[] lines = record.split("\n");
            for (String line : lines) {
                String[] parts = line.trim().split("\\s*\\|\\s*");
                if (parts.length ==3){
                    model.addRow(parts);
                }
            }

            scoresTable.setModel(model);
        }catch (Exception e){
            JOptionPane.showMessageDialog(this, "Ошибка в загрузке рекордов",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void createUIComponents() {
        letterTextField = new JTextField();// TODO: place custom component creation code here
        letterTextField.setDocument(new PlainDocument(){
            @Override
            public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
                if(getLength() + str.length() <= 1){
                    super.insertString(offset, str, attr);
                }
            }
        });
    }

    private void startGame(){
        try {
            gameState = game.startNewGame();
        }catch (Exception e){
            JOptionPane.showMessageDialog(this, "Ошибка при начале игры",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

    }
    private void guessLetter(String letter){
        try{
            gameState = game.guessLetter(letter.charAt(0));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка при угадываии буквы",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guessWord(String word){
        try{
            gameState = game.guessWord(word);
        }catch (Exception e){
            JOptionPane.showMessageDialog(this, "Ошибка при угадывании слова",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveResult(String name){
        try{
            game.saveResult(name);
        }catch (Exception e){
            JOptionPane.showMessageDialog(this, "Ошибка при сохранении результата",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void endGameLogic(){
        try{
            if(gameState.victory){
                int choice = JOptionPane.showOptionDialog(
                        this,
                        "Слово угадано верно! Хотите продолжить?",
                        "Победа!",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        new String[] {"Продолжить","Выйти"},
                        "Продолжить"
                );
                if(choice == JOptionPane.YES_OPTION){
                    gameState = game.continueNewWord();
                    updateGamePlayUI();
                }else if(choice == JOptionPane.NO_OPTION){
                    SurrenderDialog custmo = new SurrenderDialog(this, gameState.score, false);
                    game.endGame();
                    if (custmo.dismissed = true){
                        showPanel("Main Menu");
                    }
                }


            }else if(gameState.defeat){
                SurrenderDialog custmo = new SurrenderDialog(this, gameState.score, false);
                game.endGame();
                if (custmo.dismissed = true){
                    showPanel("Main Menu");
                }
            }
        }catch (Exception e){
            JOptionPane.showMessageDialog(this, "Ошибка при окончании игры " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

    }


    private class SurrenderDialog extends JDialog {
        private boolean save;
        private JTextField textField;
        private String userInput;
        private boolean dismissed = true;

        public SurrenderDialog(JFrame parent, int points, boolean surrender) { //продумать как различается когда сдаёшься или когда не сдаёшься
            super(parent, "Подтверждение", true);

            setSize(550,160);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(parent);
            setResizable(false);


            JPanel panel = new JPanel(new BorderLayout(10,10));
            panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

            JLabel label = new JLabel(String.format("Вы заработали %d Очков. Сохранить результат под введённым псевдонимом?", points));
            panel.add(label, BorderLayout.NORTH);

            textField = new JTextField();
            JPanel textFieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // или new BorderLayout()
            textField.setPreferredSize(new Dimension(300, 20));
            textFieldPanel.add(textField);

            panel.add(textFieldPanel, BorderLayout.CENTER);
            //panel.add(textField, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            JButton saverecButton = new JButton("Сохранить");
            JButton nonsavelButton = new JButton("Не сохранять");

            saverecButton.addActionListener(e -> {
                dismissed = false;
                showPanel("Main Menu");

                save = true;
                saveResult(textField.getText());
                dispose();
            });

            nonsavelButton.addActionListener(e -> {
                dismissed = false;
                showPanel("Main Menu");
                dispose();
            });


            buttonPanel.add(saverecButton);
            buttonPanel.add(nonsavelButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            add(panel);
            setVisible(true);
        }



        public String getUserInput() {
            return userInput;
        }

        public boolean isSave() {
            return save;
        }

        public boolean isDismissed() {
            return dismissed;
        }




    }
}

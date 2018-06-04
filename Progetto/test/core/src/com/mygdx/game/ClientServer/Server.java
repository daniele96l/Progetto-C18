/*
* DA FARE:
*
* Generalizzare metodo writemessage() eliminando riferimenti a istanze
*
* Utilizzare classi di Timer per i DUE metodi che usano il timer. Usare il polimorfismo
*
* Pensare se separare dalla classe i metodi gestioneCollisioni e lostLife
*
*
*
* */
package com.mygdx.game.ClientServer;

import DatabaseManagement.Database;
import DatabaseManagement.Enum.DropType;
import DatabaseManagement.Enum.TableType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Collision;
import com.mygdx.game.CommandPlayer;
import com.mygdx.game.Leaderboard.Score;
import com.mygdx.game.Levels.GestoreLivelli;
import com.mygdx.game.Player.HumanPlayer;
import com.mygdx.game.Player.Player;
import com.mygdx.game.Player.RobotPlayer;
import help.GameState;
import help.Info;

import help.Timer;
import sprites.Ball;
import sprites.Brick.Brick;
import sprites.Brick.NormalBrick;
import sprites.Paddle;
import sprites.powerup.*;

import javax.swing.*;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * @Autor Marco Mari, Marco Cotogni, Alessandro Oberti
 *
 * Definisce l'oggetto Server che permette di instanziare una connessione tra diversi client, che si connettono tramite un Socket
 * alla porta definita.
 * La connessione tra client prevede uno scambio di pacchetti tramite UDP.
 *
 */

public class Server extends Game {
    private DatagramSocket datagramSocket;
    private int portaServer = 4444;
    private ArrayList<ServerThread> threadsIn;
    private ArrayList<DatagramSocket> sockets;
    private ArrayList<Player> players;
    private ArrayList<Paddle> paddles;
    private ArrayList<CommandPlayer> commandPlayers;
    private int numeroPlayer = 1;
    private Collision collision;
    private ArrayList<Brick> bricks = new ArrayList();
    private Ball palla;
    int contatore;
    private Texture bg;
    private GameState gameState;
    private int contatore2 = 0;
    private boolean nextLevel;
    private ArrayList<Integer> indici;
    private ArrayList<PowerUp> powerUps;
    private int matEliminati;
    private GestoreLivelli gestoreLivelli;
    private int livelloCorrente;
    private boolean isFinished;
    static private boolean nick;
    private Score score;
    private static String playerName;
    private Player gameHolder;  //Giocatore che ha toccato la pallina per ultimo
    private int brickCounter;
    private int tmpDT;
    private boolean isPaused;
    private boolean isFirstCalled;
    private boolean creato;
    private Date datetmp;
    private ArrayList<Date> date;
    private Database db = new Database();
    private Icon icon = new ImageIcon("playersIcon.png");
    private String address;
    private Timer timer;


    /**
     * Crea una nuova partita, istanziando gli N giocatori
     *
     * @see:initServer()
     * @see:Info
     * @see:updateScene
     * @see:updateLevel
     */

    @Override
    public void create() {
        try {
                address = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        String s=(String) JOptionPane.showInputDialog(null, "Enter the number of players\n Server IP: "+address, "Players", 1, icon, null, "");
        if(s != null&& !s.isEmpty() && s.matches("[-+]?\\d*\\.?\\d+")) {
            numeroPlayer=Integer.parseInt(s);
            if(!(numeroPlayer>0 && numeroPlayer<5)) {
                JOptionPane.showMessageDialog(null, "Choose a number between 1-4", "Error", 1);
                System.exit(-1);
            }
        }
        else {
            JOptionPane.showMessageDialog(null, "Input must be a number", "Error", 1);
            System.exit(-1);
        }
        players = new ArrayList<Player>();
        paddles = new ArrayList<Paddle>();
        date = new ArrayList<Date>();
        commandPlayers = new ArrayList<CommandPlayer>();
        initServer();
        creato = false;

        timer=new Timer();

        for (int i = 0; i < numeroPlayer; i++) {
            paddles.add(new Paddle(numeroPlayer, i + 1));
            Info.getInstance().getPaddleresizex().add(0.5f);
            commandPlayers.add(new CommandPlayer(paddles.get(i), players.get(i), numeroPlayer, i + 1));
        }

        isFirstCalled = true;

        if (isFirstCalled) {
            isFirstCalled = false;
            isFinished = false;
            livelloCorrente = 1;
            nextLevel = false;
            isPaused = false;
            palla = new Ball();
            tmpDT = Info.getInstance().getDt();

            for (int i = 0; i < numeroPlayer; i++) {
                date.add(new Date());
            }

            gestoreLivelli = new GestoreLivelli("fileLivelli.txt");

            updateScene();
            updateLevel();
            gameHolder = players.get(0);
            brickCounter = 0;
            bg = gestoreLivelli.getLivello(livelloCorrente - 1).getBackground();
        }
    }

    /**
     * Si aggiorna 60 volte al secondo, e aggiorna le posizione degli oggetti
     * Gestisce i movimenti del player
     *
     * @see:writeMessage();
     * @see:gestisciCollisioni();
     * @see:checktimerpowerup(); Gestisce lo stato della partita
     */
    public void render() {
        if (nextLevel) {//deve stare dentro render perchè deve essere controllato sempre
            bricks = gestoreLivelli.getLivello(livelloCorrente - 1).getBricks();//ritorno l'array adatto al nuovo livello
            bg = gestoreLivelli.getLivello(livelloCorrente - 1).getBackground();

        }

        palla.getPositionBall().add(palla.getSpeedBall().x * Info.getInstance().getDt(), palla.getSpeedBall().y * Info.getInstance().getDt());
        palla.getBoundsBall().setPosition(palla.getPositionBall());
        ArrayList<PowerUp> tmpPUps = new ArrayList<PowerUp>();
        for (PowerUp p : powerUps) {
            if (p.getPosition().y + Info.getInstance().getPowerUpHeight() < 30) {
                tmpPUps.add(p);
            }
            p.getPosition().add(p.getSpeed().x * Info.getInstance().getDt(), p.getSpeed().y * Info.getInstance().getDt());
            p.getBounds().setPosition(p.getPosition());
        }
        for (PowerUp p : tmpPUps) {
            powerUps.remove(p);
        }

        for (int i = 0; i < commandPlayers.size(); i++) {
            commandPlayers.get(i).move(threadsIn.get(i).getKey());//mipermettedimuovereilgiocatore
        }

        gestisciCollisioni();

        writeMessage();



        checkTimerPowerUp(); // controlla il tempo

        if (matEliminati == gestoreLivelli.getLivello(livelloCorrente - 1).getnMatMorbidi()) {
            gameState = GameState.YOU_WON;

            livelloCorrente++;
            if (livelloCorrente > gestoreLivelli.getNumeroLivelli()) {
                isFinished = true;
            }
        }

        if (palla.getPositionBall().y <= 0) {
            lostLife();
            updateScene();
        }


        if (gameState == GameState.YOU_WON) {
            if (isFinished) {
                livelloCorrente = 1;
                isFinished = false;
                db.modify(ranGen(), playerName, players.get(0).getScore(), DropType.INSERT, TableType.OFFLINE);
                updateScene();
                updateLevel(); /////////////SERVE UNO SCREEN DI FINE GIOCO
            } else {
                nextLevel = true;
                updateScene();
                updateLevel();
            }
        }


        if (gameState == GameState.GAME_OVER) {

        }

        if (gameState == GameState.WAIT) {
            Info.getInstance().setDt(0);
            if (!creato) {
                datetmp = new Date();
                creato = true;
            }
            if (checktimer(3000, datetmp)) {
                Info.getInstance().setDt(tmpDT);
                gameState = GameState.ACTION;
                creato = false;
            }
        }
    }

    /**
     * Permette di scrivere il messaggio che verrà inviatro ai vari client
     * Il messaggio conterrà le posizioni degli oggetti
     */

    private void writeMessage() {

        String message = "";
        if (numeroPlayer == 0) {
            message = "Empty";
        } else {
            message += palla.getPositionBall().x + " " + palla.getPositionBall().y + "\t";
            for (Paddle paddle : paddles) {
                message += paddle.getPosition().x + " ";
            }
            message += "\t";

            for (float f : Info.getInstance().getPaddleresizex()) {
                message += f + " ";
            }
            message += "\t";

            for (Player player : players) {
                message += player.getPlayerName() + " ";
                message += player.getScore() + " ";
                message += player.getLives() + " ";
                message += "\t";
            }

            for (Brick brick : bricks) {
                message += brick.getPositionBrick().x + " " + brick.getPositionBrick().y + " ";
                message+=brick.getClass().getSimpleName()+"\t";
            }
            for (PowerUp powerUp : powerUps) {
                message += powerUp.getPosition().x + " " + powerUp.getPosition().y + " ";
                message+=powerUp.getClass().getSimpleName()+"\t";
            }

            String bgPath = ((FileTextureData) bg.getTextureData()).getFileHandle().name();

            message += bgPath + "\t";


            message = message.substring(0, message.length() - 1);
        }
        byte[] bytes = message.getBytes();

        for (ServerThread thread : threadsIn) {
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, thread.getAddress(), thread.getPort());
            try {
                thread.getSocket().send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        for (int i = 0; i < threadsIn.size(); i++) {
            if (threadsIn.get(i).isDeletable()) {
                threadsIn.remove(threadsIn.get(i));
                i--;
            }
        }
        if (numeroPlayer == 0) {
            Gdx.app.exit();
        }


    }
    /**
     * Imposta la scena ai valori di default
     */
    private void updateScene() {
        palla.setDefaultState();
        for (Paddle paddle : paddles) {
            paddle.setDefaultState(numeroPlayer);
        }
        commandPlayers = new ArrayList<CommandPlayer>();
        for (int i = 0; i < players.size(); i++) {
            commandPlayers.add(new CommandPlayer(paddles.get(i), players.get(i), numeroPlayer, i + 1));
        }
    }

    /**
     * Permette di gestire il passaggio ai livelli successivi
     */

    private void updateLevel() {
        bricks = gestoreLivelli.getLivello(livelloCorrente - 1).getBricks();//laclasselivellosioccuperàdiritornarel'arraylistdeimattonciniadattiaquestolivello
        powerUps = new ArrayList<PowerUp>();
        bg = gestoreLivelli.getLivello(livelloCorrente - 1).getBackground();
        matEliminati = 0;
        gameState = GameState.WAIT;
    }

    /**
     * Genera un numero casuale che servirà per la generazione dei power up in maniera pseudo casuale
     *
     * @return String che è il numero casuale generato
     */

    private String ranGen() {
        Random n = new Random();
        String s = "" + n.nextInt(1000);
        return s;
    }

    /**
     * Gestisce la durata dell'effetto del power up
     */
    private void checkTimerPowerUp() {
        timer.checkTimer(date, numeroPlayer );
    }

    /**
     * Gestisce il tempo di caduta della pallina una volta che si perde
     *
     * @param durata
     * @param datetmp
     * @return boolean
     */
    private boolean checktimer(int durata, Date datetmp) {
        Date date2 = new Date();
        if (date2.getTime() - datetmp.getTime() > durata) {
            return true;
        }
        return false;
    }

    /**
     * Inizializza il server, istanziando le variabili di cui farà uso
     * E fa partire i Thread relativi ai client
     */

    private void initServer() {
        try {
            datagramSocket = new DatagramSocket(portaServer);
            threadsIn = new ArrayList<ServerThread>();
            sockets = new ArrayList<DatagramSocket>();
            while (threadsIn.size() < numeroPlayer) {
                byte[] b = new byte[1024];
                DatagramPacket packet = new DatagramPacket(b, b.length);
                datagramSocket.receive(packet);
                String playerName = new String(packet.getData(), 0, packet.getLength());
                int newPort = portaServer + threadsIn.size() + 1;
                b = ((Integer) newPort).toString().getBytes();
                DatagramPacket packetBack = new DatagramPacket(b, b.length, packet.getAddress(), packet.getPort());
                datagramSocket.send(packetBack);
                sockets.add(new DatagramSocket(newPort));
                players.add(new HumanPlayer(playerName));
                threadsIn.add(new ServerThread(sockets.get(sockets.size() - 1), packet.getAddress(), packet.getPort()));
            }

            datagramSocket.close();

            for (ServerThread t : threadsIn) {
                t.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gestisce le collisioni
     * tra la pallina e il mattoncini e l'eliminazione degli stessi dalla scena
     * tra la paddle e i power up in modo da ottenere l'effetto
     * <p>
     * //Secondo me si deve astrarre l'eliminazione dei mattoncini dal controllo della collisione
     * //Applicare quindi il pattern.
     */
    public void gestisciCollisioni() {

        collision = new Collision(palla, bricks, powerUps, paddles, players);

        int numeroEliminati = collision.checkBrickCollision();
        matEliminati+=numeroEliminati;
        gameHolder.setScore(gameHolder.getScore()+numeroEliminati);

        collision.checkBorderCollision();

        Player newGameHolder =players.get(collision.checkPaddleCollision(players.indexOf(gameHolder)));
        if(!newGameHolder.equals(gameHolder)) {
            gameHolder=newGameHolder;
        }

        collision.checkPowerUpCollision(date);
        ArrayList<Player> eliminabili=new ArrayList<Player>();
        for(int i=0; i<numeroPlayer;i++) {
            if(players.get(i).getLives()<0) {
                eliminabili.add(players.get(i));
            }
        }
        for(Player player:eliminabili) {
            deletePlayer(player);
        }
    }

    /**
     * Se il giocatore ha preso il power up che fa perdere la vita, faccio agire l'effetto
     */
    private void lostLife() {
        int range=Info.getInstance().getLarghezza()/numeroPlayer;
        Player loser=new RobotPlayer("default", palla, paddles.get(0));

        for(int i=0; i<numeroPlayer; i++) {
            if(palla.getPositionBall().x>=i*range && palla.getPositionBall().x<(i+1)*range) {
                loser=players.get(i);
            }
        }

        loser.setLives(loser.getLives()-1);
        gameState=GameState.WAIT;

        if(loser.getLives() < 0) {
            deletePlayer(loser);
        }
    }

    private void deletePlayer(Player loser) {
        if(players.get(0).equals(loser)) {
            db.modify(""+(int)Math.random()*1000, playerName, players.get(0).getScore(), DropType.INSERT, TableType.OFFLINE);
            gameState=GameState.GAME_OVER;
        }
        else {
            int index=players.indexOf(loser);
            players.remove(loser);
            paddles.remove(index);
            numeroPlayer--;
        }
    }
}
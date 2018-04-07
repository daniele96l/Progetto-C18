package com.mygdx.game;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import sprites.*;



import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

public class MyGdxGame extends Game {
    int nColonne = 4;
    private Collision col;
    SpriteBatch batch;
    public mattoncino mattoncino;
    public ArrayList<mattoncino> mattoncini = new ArrayList<mattoncino>();
    public  ball palla;
    public mattonella mattonella;
    private Texture bg;
    private Texture start;
    private Texture gameOver;
    private GameState gameState;
    private  int shift;
    private CommandPlayer player1;
    private  Livello livello = new Livello(mattoncino, palla);


	
	@Override
	public void create () {
		batch = new SpriteBatch();
		palla = new ball();
		mattonella  = new mattonella();
        player1 = new CommandPlayer(mattonella);     //istanzio un Commandplayer( posso averne diversi per ogni player
		shift=600;


        mattoncini = livello.selectLv(); //la classe livello si occuperà di ritornare l'array list dei mattoncini adatti a questo livello

        bg = new Texture("bg.jpg");
        System.out.println("eseguo");

        start=new Texture("start.jpg");
        gameOver=new Texture("gameover.jpeg");

        /*Immagini ovviamente temporanee (fanno cagare)*/
        gameState=GameState.INIT;

	}

	@Override
	public void render () {

        int index = -1;
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if(livello.nextLevel()) {//deve stare dentro render perchè deve essere controllato sempre
            mattoncini = livello.selectLv();  //ritorno l'array adatto al nuovo livello
            bg = livello.getBg(); //reimposto il bg
            ball.reposition(); //palla al centro

        }

		if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) { //Barra spaziatrice per iniziare
		    if(gameState.equals(GameState.INIT)) {
		        gameState=GameState.ACTION;
            }
            if(gameState.equals(GameState.GAME_OVER)) {
                create();   //Soluzione TEMPORANEA, in attesa dell'implementazione dei livelli
                gameState=GameState.INIT;
            }
        }
        batch.begin();

        if(gameState.equals(GameState.ACTION)) {
            batch.draw(bg, 0, 0);
            batch.draw(palla, palla.getPositionBall().x, palla.getPositionBall().y);

            for (mattoncino mattoncino : mattoncini) {
                batch.draw(mattoncino, mattoncino.getPositionBrick().x, mattoncino.getPositionBrick().y);

                //disegno i mattoncini
            }

            batch.draw(mattonella, mattonella.getPosition().x, mattonella.getPosition().y);

            player1.Move();     //mi permette di muovere il giocatore

            for (mattoncino mattoncino : mattoncini) {
                //dato che ho un arraylist devo aggiornare le condizioni dei mattoncini dentro un ciclo for
                col = new Collision(mattoncino, palla);
                if (col.check()) {
                    index = mattoncini.indexOf(mattoncino);
                }
            }

            col.checkside((float) 1, mattonella);
            //palla.update((float) 2, mattonella, mattoncino);
            if (index != -1) {
                mattoncini.remove(mattoncini.get(index));
            }
            if(palla.getPositionBall().y<=0) {
                gameState=GameState.GAME_OVER;
            }
        }
        else {
		    if(gameState.equals(GameState.INIT)) {
		        batch.draw(start,0,0);
            }
            if(gameState.equals(GameState.GAME_OVER)) {
		        batch.draw(gameOver,0,0);
            }
        }



        batch.end();
    }
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}
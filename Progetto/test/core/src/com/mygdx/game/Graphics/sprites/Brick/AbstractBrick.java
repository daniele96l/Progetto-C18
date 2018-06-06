package com.mygdx.game.Graphics.sprites.Brick;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.help.Info;
import com.mygdx.game.Graphics.sprites.powerup.PowerUp;

/**
 * La classe astratta Brick extende la classe di libGDX
 * chiama Sprite e contiene tutte le informazioni e variabili del "mattoncino base",
 * quali per esempio la sua posizione.
 *
 * @author Daniele Ligato
 */
public abstract class AbstractBrick extends Sprite implements Brick{

    protected Vector2 positionBrick;
    protected Rectangle boundsBrick;
    protected boolean eliminato;
    protected PowerUp powerUp;
    protected boolean hasPowerUp;
    protected boolean deletable;

    public AbstractBrick(int posX, int posY, String immagine) {
        super(new Texture(immagine));
        hasPowerUp = false;
        positionBrick = new Vector2(posX, posY);
        boundsBrick = new Rectangle(posX, posY, this.getWidth() * Info.getInstance().getBrickresize(), this.getHeight() * Info.getInstance().getBrickresize());
    }

    /**
     * Questo metodo assegna il PowerUp al mattoncino che l'ha colpito
     * e setta la variabile booleana hasPowerUp a "vera".
     *
     * @param powerUp oggetto potenziamento che cade dal mattoncino
     */
    public void setPowerUp(PowerUp powerUp) {
        this.powerUp = powerUp;
        hasPowerUp = true;
    }

    @Override
    public String getRelativePosition(Brick brick) {
        if(this.getPositionBrick().y==brick.getPositionBrick().y) {
            return "line";
        }
        else if(this.getPositionBrick().x==brick.getPositionBrick().x) {
            return "column";
        }
        return "else";
    }

    /**
     * Questo metodo ritorna la variabile booleana hasPowerUp
     *
     * @return variabile booleana hasPowerUp
     */
    public boolean hasPowerUp() {
        return hasPowerUp;
    }

    public PowerUp getPowerUp() {
        return powerUp;
    }

    public Vector2 getPositionBrick() {
        return positionBrick;
    }

    public void delete() {
        eliminato = true;
    }

    public boolean isDeletable() {return deletable;}

    public void setPositionBrick(Vector2 positionBrick) {
        this.positionBrick = positionBrick;
    }

    public void setBoundsBrick(Rectangle boundsBrick) {
        this.boundsBrick = boundsBrick;
    }

    public void setEliminato(boolean eliminato) {
        this.eliminato = eliminato;
    }

    public Rectangle getBoundsBrick() {
        return boundsBrick;
    }

    public boolean isEliminato() {
        return eliminato;
    }
}
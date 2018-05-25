package com.mygdx.game.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.ClientServer.Server;
import help.Info;

/**
 * @author regna, schillaci
 * Fa partire il server
 */
public class TestServer
{
    public static void main(String[] args){
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 60;
        new LwjglApplication(new Server(), config);
    }

}

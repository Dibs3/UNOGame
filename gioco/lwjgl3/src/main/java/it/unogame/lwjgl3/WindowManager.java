package it.unogame.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class WindowManager {    //adatta finestra alla dimensione del monitor

    public static void configura(
        Lwjgl3ApplicationConfiguration config) {

        //risoluzione monitor
        var monitor=Lwjgl3ApplicationConfiguration.getDisplayMode();

        int larghezza = monitor.width;
        int altezza = monitor.height;

        //85% monitor
        int finestraL = (int) (larghezza * 0.85);
        int finestraH = (int) (altezza * 0.85);

        config.setWindowedMode(finestraL, finestraH);
        config.setResizable(true);
    }
}

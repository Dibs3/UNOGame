package it.unogame;

import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;


//proporzione gli oggetti e amplia l'area visibile quando la finestra ha proporzioni diverse
public class ResponsiveViewport {

    public static Viewport create(
        float worldWidth,
        float worldHeight) {

        return new ExtendViewport(
            worldWidth,
            worldHeight
        );
    }
}

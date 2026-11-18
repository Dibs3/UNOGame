package it.unogame;

import com.badlogic.gdx.utils.viewport.Viewport;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

public class ResponsiveBackground {

    private final SceneLoader sceneLoader;
    private final Viewport viewport;
    private final int entity;

    private final float originalWidth;
    private final float originalHeight;
    private final float originalScaleX;
    private final float originalScaleY;

    public ResponsiveBackground(
        SceneLoader sceneLoader,
        Viewport viewport,
        int entity) {

        this.sceneLoader = sceneLoader;
        this.viewport = viewport;
        this.entity = entity;

        DimensionsComponent dimensions = ComponentRetriever.get(
            entity,
            DimensionsComponent.class,
            sceneLoader.getEngine()
        );

        TransformComponent transform = ComponentRetriever.get(
            entity,
            TransformComponent.class,
            sceneLoader.getEngine()
        );

        originalWidth = dimensions.width;
        originalHeight = dimensions.height;
        originalScaleX = transform.scaleX;
        originalScaleY = transform.scaleY;
    }

    public void resize() {

        TransformComponent transform = ComponentRetriever.get(
            entity,
            TransformComponent.class,
            sceneLoader.getEngine()
        );

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        float scaleX = worldWidth /
            (originalWidth * originalScaleX);

        float scaleY = worldHeight /
            (originalHeight * originalScaleY);

        float scale = Math.max(scaleX, scaleY);

        float newWidth = originalWidth * originalScaleX * scale;
        float newHeight = originalHeight * originalScaleY * scale;

        transform.scaleX = originalScaleX * scale;
        transform.scaleY = originalScaleY * scale;

        transform.x = (worldWidth - originalWidth) / 2f;
        transform.y = (worldHeight - originalHeight) / 2f;
    }
}

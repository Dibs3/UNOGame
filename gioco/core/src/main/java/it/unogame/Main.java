
package it.unogame;

import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.NodeComponent;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;

import games.rednblack.editor.renderer.SceneConfiguration;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

public class Main extends ApplicationAdapter {

    // Nome dell'oggetto START in HyperLap2D
    private static final String START_NAME = "start";

    // Nome della scena esportata
    private static final String SCENE_NAME = "MainScene";

    private SceneLoader sceneLoader;
    private Viewport viewport;

    // Gestione sfondo adattivo
    private ResponsiveBackground background;

    // Entità della scena
    private int startEntity = -1;
    private int backgroundEntity = -1;

    private Client client;

    private volatile boolean connecting = false;

    @Override
    public void create() {

        // Configurazione HyperLap2D
        SceneConfiguration config = new SceneConfiguration();

        sceneLoader = new SceneLoader(config);

        // Informazioni progetto
        ProjectInfoVO project = sceneLoader.getRm().getProjectVO();

        float worldW = (float) project.originalResolution.width
            / project.pixelToWorld;

        float worldH = (float) project.originalResolution.height
            / project.pixelToWorld;

        // Viewport adattivo
        viewport = new ExtendViewport(worldW, worldH);

        // Caricamento scena
        sceneLoader.loadScene(SCENE_NAME, viewport);

        // Recupero oggetti della scena
        NodeComponent rootNode = ComponentRetriever.get(
            sceneLoader.getRoot(),
            NodeComponent.class,
            sceneLoader.getEngine()
        );

        int foundStart = -1;
        int foundBackground = -1;
        int last = -1;

        for (int child : rootNode.children) {

            MainItemComponent info = ComponentRetriever.get(
                child,
                MainItemComponent.class,
                sceneLoader.getEngine()
            );

            String name = info != null ? info.itemIdentifier : "";

            System.out.println(
                "OGGETTO: entity=" + child + " nome='" + name + "'"
            );

            last = child;

            if (name != null) {

                String lowerName = name.toLowerCase();

                if (lowerName.contains(START_NAME)) {
                    foundStart = child;
                }

                if (lowerName.contains("background")) {
                    foundBackground = child;
                }
            }
        }

        // Fallback per la scena attuale
        // Dai log: entity 1 = background, entity 2 = START
        if (foundBackground == -1) {
            foundBackground = 1;
        }

        if (foundStart == -1) {
            foundStart = last;
        }

        backgroundEntity = foundBackground;
        startEntity = foundStart;

        System.out.println("Background entity = " + backgroundEntity);
        System.out.println("START entity = " + startEntity);

        // Creazione sfondo responsive
        if (backgroundEntity != -1) {

            background = new ResponsiveBackground(
                sceneLoader,
                viewport,
                backgroundEntity
            );
        }

        // Inizializzazione client
        client = new Client();

        // Gestione click sul pulsante START
        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean touchDown(
                int screenX,
                int screenY,
                int pointer,
                int button
            ) {

                Vector2 position = viewport.unproject(
                    new Vector2(screenX, screenY)
                );

                System.out.println(
                    "Click a: " + position.x + ", " + position.y
                );

                if (isInsideStart(position.x, position.y)) {

                    onStartClicked();

                    return true;
                }

                return false;
            }
        });

        // Adatta lo sfondo già all'avvio
        resize(
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

    }

    // Controlla se il click è dentro START
    private boolean isInsideStart(float x, float y) {

        if (startEntity == -1 || sceneLoader == null) {
            return false;
        }

        TransformComponent transform = ComponentRetriever.get(
            startEntity,
            TransformComponent.class,
            sceneLoader.getEngine()
        );

        DimensionsComponent dimensions = ComponentRetriever.get(
            startEntity,
            DimensionsComponent.class,
            sceneLoader.getEngine()
        );

        if (transform == null || dimensions == null) {
            return false;
        }

        float width = dimensions.width * transform.scaleX;
        float height = dimensions.height * transform.scaleY;

        float minX = Math.min(
            transform.x,
            transform.x + width
        );

        float maxX = Math.max(
            transform.x,
            transform.x + width
        );

        float minY = Math.min(
            transform.y,
            transform.y + height
        );

        float maxY = Math.max(
            transform.y,
            transform.y + height
        );

        return x >= minX && x <= maxX
            && y >= minY && y <= maxY;
    }

    // Azione quando viene cliccato START
    private void onStartClicked() {

        if (connecting || client == null) {
            return;
        }

        connecting = true;

        System.out.println("START cliccato!");

        // Connessione in thread separato
        Thread connectionThread = new Thread(() -> {

            try {

                client.connect();

            } catch (Exception e) {

                e.printStackTrace();

            } finally {

                Gdx.app.postRunnable(() -> {
                    connecting = false;
                });
            }

        });

        connectionThread.setName("Client-Connection-Thread");
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    @Override
    public void render() {

        ScreenUtils.clear(0f, 0f, 0f, 1f);

        if (sceneLoader == null || viewport == null) {
            return;
        }

        viewport.apply();

        sceneLoader.getEngine().setDelta(
            Gdx.graphics.getDeltaTime()
        );

        sceneLoader.getEngine().process();
    }

    @Override
    public void resize(int width, int height) {

        if (width <= 0 || height <= 0) {
            return;
        }

        viewport.update(width, height, true);

        sceneLoader.resize(width, height);

        // Applichiamo nuovamente il viewport
        viewport.update(width, height, true);

        // Ridimensioniamo lo sfondo per ultimo
        background.resize();

        System.out.println("Finestra: " + width + "x" + height);
        System.out.println("Mondo: "
            + viewport.getWorldWidth() + "x"
            + viewport.getWorldHeight());
    }

    @Override
    public void dispose() {

        Gdx.input.setInputProcessor(null);

        if (sceneLoader != null) {
            sceneLoader.dispose();
        }
    }
}

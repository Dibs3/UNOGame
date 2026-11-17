
package it.unogame;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.NodeComponent;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import games.rednblack.editor.renderer.SceneConfiguration;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.renderer.utils.ItemWrapper;

public class Main extends ApplicationAdapter {

    // Nome dell'oggetto START in HyperLap2D
    private static final String START_NAME = "start";

    // Nome della scena esportata da HyperLap2D
    private static final String SCENE_NAME = "MainScene";

    private SceneLoader sceneLoader;
    private Viewport viewport;

    private int startEntity = -1;

    private Client client;

    private volatile boolean connecting = false;

    @Override
    public void create() {

        // Configurazione della scena
        SceneConfiguration config = new SceneConfiguration();

        sceneLoader = new SceneLoader(config);

        // Recupero informazioni del progetto
        ProjectInfoVO project = sceneLoader.getRm().getProjectVO();

        float worldW = (float) project.originalResolution.width
            / project.pixelToWorld;

        float worldH = (float) project.originalResolution.height
            / project.pixelToWorld;

        // Creazione viewport
        viewport = new FitViewport(worldW, worldH);

        // Caricamento scena
        sceneLoader.loadScene(SCENE_NAME, viewport);

        // Ricerca dell'oggetto START
        // Cerco START tra gli oggetti della scena
        NodeComponent rootNode = ComponentRetriever.get(
            sceneLoader.getRoot(), NodeComponent.class, sceneLoader.getEngine());

        int found = -1;
        int last = -1;

        for (int child : rootNode.children) {
            MainItemComponent info = ComponentRetriever.get(
                child, MainItemComponent.class, sceneLoader.getEngine());

            String name = info.itemIdentifier;
            System.out.println("OGGETTO: entity=" + child + " nome='" + name + "'");

            last = child; // l'ultimo oggetto e' quello piu' in alto nella scena

            if (name != null && name.toLowerCase().contains("start")) {
                found = child;
            }
        }

        // Se nessun nome contiene "start", uso l'ultimo oggetto della scena
        if (found == -1) {
            System.out.println("Nessun oggetto chiamato start, uso l'ultimo: " + last);
            found = last;
        }

        if (found == -1) {
            Gdx.app.error("Main", "Nessun oggetto nella scena!");
            return;
        }

        startEntity = found;
        System.out.println("startEntity scelto = " + startEntity);
        // Inizializzazione Client
        client = new Client();

        // Gestione click
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
                System.out.println("Click a: " + position.x + ", " + position.y);
                if (isInsideStart(position.x, position.y)) {

                    onStartClicked();

                    return true;
                }

                return false;
            }
        });
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
        System.out.println("transform=" + transform + " dimensions=" + dimensions);
        if (transform == null || dimensions == null) {
            return false;
        }

        float width = dimensions.width * transform.scaleX;
        float height = dimensions.height * transform.scaleY;

        float minX = Math.min(transform.x, transform.x + width);
        float maxX = Math.max(transform.x, transform.x + width);

        float minY = Math.min(transform.y, transform.y + height);
        float maxY = Math.max(transform.y, transform.y + height);
        System.out.println("START occupa x:" + minX + "-" + maxX + "  y:" + minY + "-" + maxY);
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

        // Connessione in un thread separato
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

        if (viewport != null) {
            viewport.update(width, height, true);
        }

        if (sceneLoader != null) {
            sceneLoader.resize(width, height);
        }
    }

    @Override
    public void dispose() {

        Gdx.input.setInputProcessor(null);

        if (sceneLoader != null) {
            sceneLoader.dispose();
        }
    }
}

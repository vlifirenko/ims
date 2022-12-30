package com.ims.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.ims.R;
import com.ims.content.model.Item;
import com.ims.graph.ColorUtils;
import com.ims.graph.Node;
import com.ims.graph.Tag;
import com.ims.helpers.ArrayUtils;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.source.EmptyBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.bitmap.source.decorator.BaseBitmapTextureAtlasSourceDecorator;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleLayoutGameActivity;
import org.andengine.util.color.Color;
import org.andengine.util.modifier.IModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GraphActivity extends SimpleLayoutGameActivity implements ScrollDetector.IScrollDetectorListener, PinchZoomDetector.IPinchZoomDetectorListener, IOnSceneTouchListener {
    private static final String TAG = GraphActivity.class.getName();

    private static final int CAMERA_WIDTH = 480;
    private static final int CAMERA_HEIGHT = 800;

    private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(0.2f, 0.4f, 0.6f);

    private Scene mScene;

    private ZoomCamera mZoomCamera;
    private SurfaceScrollDetector mScrollDetector;
    private PinchZoomDetector mPinchZoomDetector;
    private float mPinchZoomStartedCameraZoomFactor;

    private PhysicsWorld mPhysicsWorld;

    private ArrayList<Node> nodes = new ArrayList<Node>();
    private Font mFont;
    private List<TextureRegion> mNodeTextureRegion = new ArrayList<TextureRegion>();
    private boolean isNodeDrag;

    private ListView list;
    private List<Item> items;
    private List<Tag> tags = new ArrayList<Tag>();

    @Override
    protected int getLayoutID() {
        return R.layout.activity_graph;
    }

    @Override
    protected int getRenderSurfaceViewID() {
        return R.id.xmllayoutexample_rendersurfaceview;
    }

    @Override
    public synchronized void onGameCreated() {
        super.onGameCreated();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initViews();
            }
        });
    }

    private void initViews() {
        list = (ListView) findViewById(R.id.list);
        findViewById(R.id.btn_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (list.getVisibility() == View.GONE)
                    list.setVisibility(View.VISIBLE);
                else
                    list.setVisibility(View.GONE);
            }
        });
        List<String> nodesNames = new ArrayList<String>();
        for (Node node : nodes) {
            nodesNames.add(node.name);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, nodesNames);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Toast.makeText(GraphActivity.this, "node " + nodes.get(position).id + " clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initTags() {
        if (MainActivity.service != null)
            try {
                items = MainActivity.service.getAll();
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }
        if (items.size() == 0)
            return;

        for (Item it : items) {
            for (String tag : it.tags) {
                boolean contain = false;
                for (Tag tg: tags) {
                    if (tg.name.equals(tag)) {
                        tg.items.add(it);
                        contain = true;
                        break;
                    }
                }
                if (!contain) {
                    tags.add(new Tag(tag, it));
                }
            }
        }
    }

    private void buildNodes() {
        Random rand = new Random();
        for (int i = 0; i < tags.size(); i++) {
            float pX = rand.nextInt(CAMERA_WIDTH + 1 - Node.WIDTH);
            float pY = rand.nextInt(CAMERA_HEIGHT + 1 - Node.HEIGHT);
            Node node = new Node(pX, pY, mNodeTextureRegion.get(i), this.getVertexBufferObjectManager()) {
                @Override
                public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                    if (pSceneTouchEvent.isActionDown())
                        isNodeDrag = true;
                    if (pSceneTouchEvent.isActionUp())
                        isNodeDrag = false;
                    if (pSceneTouchEvent.isActionDown() || pSceneTouchEvent.isActionMove()) {
                        final Body body = (Body) this.getUserData();
                        if (body == null)
                            return true;
                        body.setTransform(pSceneTouchEvent.getX() / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
                                pSceneTouchEvent.getY() / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
                                body.getAngle());
                    }
                    return false;
                }
            };
            Body body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, node, BodyType.DynamicBody, FIXTURE_DEF);
            node.setUserData(body);
            node.setText(this.mFont, tags.get(i).name, this.getVertexBufferObjectManager());
            node.setWeight(tags.get(i).items.size());
            node.setZIndex(1);
            node.items = tags.get(i).items;
            this.mScene.attachChild(node);
            this.mScene.registerTouchArea(node);
            this.mScene.setTouchAreaBindingOnActionDownEnabled(true);
            this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(node, (Body) node.getUserData(), true, true));
            node.id = i;
            nodes.add(node);
        }
        //links
        for (Node node : nodes) {
            for (Item item : items) {
                if (item.tags.contains(node.name)) {
                    for (String n : item.tags) {
                        boolean skip = false;
                        for (Node nd : node.links) {
                            if (nd.name.equals(n)) {
                                skip = true;
                                break;
                            }
                        }
                        if (skip)
                            break;
                        if (!n.equals(node.name)) {
                            for (Node nd : nodes) {
                                if (nd.name.equals(n)) {
                                    node.links.add(nd);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public EngineOptions onCreateEngineOptions() {
        this.mZoomCamera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mZoomCamera);
        engineOptions.getRenderOptions().setMultiSampling(true);
        return engineOptions;
    }

    @Override
    public void onCreateResources() {
        this.mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.NORMAL), 16);
        this.mFont.load();

        initTags();

        for (int i = 0; i < tags.size(); i++) {
            final BitmapTextureAtlas mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), Node.WIDTH, Node.HEIGHT, TextureOptions.BILINEAR);
            final IBitmapTextureAtlasSource baseTextureSource = new EmptyBitmapTextureAtlasSource(Node.WIDTH, Node.HEIGHT);
            final IBitmapTextureAtlasSource decoratedTextureAtlasSource = new BaseBitmapTextureAtlasSourceDecorator(baseTextureSource) {
                @Override
                protected void onDecorateBitmap(Canvas pCanvas) throws Exception {
                    Paint paint = new Paint();
                    Random rand = new Random();
                    ColorUtils.Rgb rgb = ColorUtils.hslToRgb(rand.nextFloat(), 1.0f, 0.65f);
                    paint.setColor(android.graphics.Color.rgb((int) rgb.r, (int) rgb.g, (int) rgb.b));
                    paint.setAntiAlias(true);
                    paint.setAlpha(255);
                    pCanvas.drawCircle(Node.WIDTH / 2, Node.HEIGHT / 2, Node.WIDTH / 2, paint);
                }

                @Override
                public BaseBitmapTextureAtlasSourceDecorator deepCopy() {
                    throw new IModifier.DeepCopyNotSupportedException();
                }
            };
            this.mNodeTextureRegion.add(BitmapTextureAtlasTextureRegionFactory.createFromSource(mBitmapTextureAtlas, decoratedTextureAtlasSource, 0, 0));
            mBitmapTextureAtlas.load();
        }

    }

    @Override
    public Scene onCreateScene() {
        this.mEngine.registerUpdateHandler(new FPSLogger());

        this.mScene = new Scene();
        this.mScene.setOnAreaTouchTraversalFrontToBack();
        this.mScene.setBackground(new Background(255f, 255f, 255f));

        this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);

        final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
        final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
        final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
        final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
        final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

        final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
        PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
        PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
        PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
        PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

        this.mScene.attachChild(ground);
        this.mScene.attachChild(roof);
        this.mScene.attachChild(left);
        this.mScene.attachChild(right);

        this.mScene.registerUpdateHandler(this.mPhysicsWorld);

        buildNodes();
        drawLinks();

        this.mScene.setOnSceneTouchListener(this);
        this.mScene.setTouchAreaBindingOnActionDownEnabled(true);

        this.mScrollDetector = new SurfaceScrollDetector(this);
        this.mPinchZoomDetector = new PinchZoomDetector(this);

        return this.mScene;
    }

    @Override
    public void onResumeGame() {
        super.onResumeGame();
    }

    @Override
    public void onPauseGame() {
        super.onPauseGame();
    }

    private void drawLinks() {
        for (final Node node : nodes) {
            // init links
            for (final Node link : node.links) {
                final Line connectionLine = new Line(node.getX() + node.getWidth() / 2, node.getY() + node.getHeight() / 2,
                        link.getX() + link.getWidth() / 2, link.getY() + link.getHeight() / 2, this.getVertexBufferObjectManager());
                connectionLine.setColor(Color.BLACK);
                connectionLine.setZIndex(0);
                this.mScene.attachChild(connectionLine);
                this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(node, (Body) node.getUserData(), true, true) {
                    @Override
                    public void onUpdate(final float pSecondsElapsed) {
                        super.onUpdate(pSecondsElapsed);
                        connectionLine.setPosition(node.getX() + node.getWidth() / 2, node.getY() + node.getHeight() / 2,
                                link.getX() + link.getWidth() / 2, link.getY() + link.getHeight() / 2);
                    }
                });
                DistanceJointDef distanceJointDef = new DistanceJointDef();
                Body b1 = (Body) node.getUserData();
                Body b2 = (Body) link.getUserData();
                distanceJointDef.initialize(b1, b2,
                        b1.getWorldCenter(), b2.getWorldCenter());
                distanceJointDef.length = Node.LENGTH;
                int intersection = ArrayUtils.intersection(node.items, link.items).size();
                distanceJointDef.length = (float) (Node.LENGTH * Math.sqrt(intersection));
                distanceJointDef.collideConnected = true;
                distanceJointDef.frequencyHz = Node.FREQUENCY_HZ;
                distanceJointDef.dampingRatio = Node.DAMPING_RATIO;
                Log.d(TAG, node.id + ":" + link.id);
                this.mPhysicsWorld.createJoint(distanceJointDef);
            }
        }
        this.mScene.sortChildren();
    }

    @Override
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
        if (isNodeDrag)
            return false;

        this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);

        if (this.mPinchZoomDetector.isZooming()) {
            this.mScrollDetector.setEnabled(false);
        } else {
            if (pSceneTouchEvent.isActionDown()) {
                this.mScrollDetector.setEnabled(true);
            }
            this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
        }

        return true;
    }

    @Override
    public void onScrollStarted(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
        final float zoomFactor = this.mZoomCamera.getZoomFactor();
        this.mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
    }

    @Override
    public void onScroll(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
        final float zoomFactor = this.mZoomCamera.getZoomFactor();
        this.mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
    }

    @Override
    public void onScrollFinished(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
        final float zoomFactor = this.mZoomCamera.getZoomFactor();
        this.mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
    }

    @Override
    public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
        this.mPinchZoomStartedCameraZoomFactor = this.mZoomCamera.getZoomFactor();
    }

    @Override
    public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
        this.mZoomCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
    }

    @Override
    public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
        this.mZoomCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
    }
}

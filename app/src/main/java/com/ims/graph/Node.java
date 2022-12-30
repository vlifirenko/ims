package com.ims.graph;

import com.ims.content.model.Item;

import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.HorizontalAlign;

import java.util.ArrayList;
import java.util.List;

public class Node extends Sprite {

    public static final int WIDTH = 64;
    public static final int HEIGHT = 64;

    // physic for connectors
    public static final float LENGTH = 6.0f;
    public static final float FREQUENCY_HZ = 0.01f;
    public static final float DAMPING_RATIO = 5.5f;

    public long id;
    public List<Node> links = new ArrayList<Node>();
    public String name;
    private int weight = 0;
    private Text text;
    public List<Item> items = new ArrayList<Item>();

    public Node(float pX, float pY, ITextureRegion pTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager) {
        super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
    }

    public void setText(Font pFont, String pText, VertexBufferObjectManager pVertexBufferObjectManager) {
        this.name = pText;
        text = new Text(0, 0, pFont, pText, new TextOptions(HorizontalAlign.CENTER), pVertexBufferObjectManager);
        this.attachChild(text);
    }

    public void setWeight(int weight) {
        this.weight = weight;
        setHeight(getHeight() * (float) Math.sqrt(this.weight));
        setWidth(getWidth() * (float) Math.sqrt(this.weight));
        text.setPosition(this.getWidth() / 2 - text.getWidth() / 2, (this.getHeight() - text.getHeight()) / 2);
    }

    @Override
    protected void onManagedUpdate(float pSecondsElapsed) {
        setRotation(0);
        super.onManagedUpdate(pSecondsElapsed);
    }

    @Override
    public String toString() {
        return this.name;
    }
}

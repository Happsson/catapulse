package nu.geeks.catapulse;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.FrictionJointDef;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.physics.box2d.joints.WheelJointDef;
import com.badlogic.gdx.utils.Array;
import java.util.Random;

public class Catapulse extends ApplicationAdapter{

    private World world;
    private Box2DDebugRenderer render;
    private Body body;
    private Body catp;
    private Body block;
    private Body fixed;
    private Body pivot;
    private Body ball, armLeft, armRight, legLeft, legRight, head;
    private int amount;
    private SpriteBatch sb;
    private int force;
    private int speed;
    private int xPos = 10;
    private int yPos = 10;
    private OrthographicCamera uiCam;
    private OrthographicCamera camera;
    private BitmapFont font;
    private String debug;
    private boolean goalHit = false, running = false;
    private long time;
    private int distance;


	@Override
	public void create () {

        camera = new OrthographicCamera(Gdx.graphics.getWidth() / 30, Gdx.graphics.getHeight() / 30);
        uiCam = new OrthographicCamera(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        render = new Box2DDebugRenderer();
        world = new World(new Vector2(0, -4f), true);
        debug = "";
        speed = 40;
        amount = 0;
        force = 40;

        createFloor();
        createCatapult();

        createBall();
        createGoal();
        createStop();

        setContactListener();

        time = System.currentTimeMillis();

        sb = new SpriteBatch();

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.setUseIntegerPositions(false);

        Gdx.input.setInputProcessor(new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {

                switch(keycode){
                    case Input.Keys.SPACE:
                        amount = force;
                        break;
                    case Input.Keys.ESCAPE:
                        resetGame();
                        break;
                    case Input.Keys.W:
                        force += 5;
                        break;
                    case Input.Keys.S:
                        force -= 5;
                        break;
                    case Input.Keys.Q:
                        force = 0;
                        break;
                    case Input.Keys.A:
                        speed -= 5;
                        break;
                    case Input.Keys.D:
                        speed += 5;
                        break;
                    case Input.Keys.I:
                        fixed.setTransform(fixed.getPosition().x, fixed.getPosition().y+.1f, 0);
                        break;
                    case Input.Keys.K:
                        fixed.setTransform(fixed.getPosition().x, fixed.getPosition().y-.1f, 0);
                        break;
                    case Input.Keys.J:
                        fixed.setTransform(fixed.getPosition().x-.1f, fixed.getPosition().y, 0);
                        break;
                    case Input.Keys.L:
                        fixed.setTransform(fixed.getPosition().x+.1f, fixed.getPosition().y, 0);
                        break;
                }

                return true;
            }

            @Override
            public boolean keyUp(int keycode) {
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                //fixed.setTransform(screenX, screenY, 0);


                if(System.currentTimeMillis() - time < 1000){
                    if(running) resetGame();
                    else amount = force;
                }else{
                    Vector3 v3 = new Vector3(screenX, screenY, 0);
                    Vector3 unprojected = camera.unproject(v3);
                    fixed.setTransform(unprojected.x,unprojected.y, 0);
                }
                time = System.currentTimeMillis();
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                //fixed.setTransform(screenX, screenY, 0);
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                return false;
            }

            @Override
            public boolean scrolled(int amount) {

                camera.zoom += amount / 10f;
                camera.update();
                return false;
            }
        });

	}

    private void setContactListener() {
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();

                if(a.getUserData() != null && b.getUserData() != null){
                    if((a.getUserData().equals("goal") && b.getUserData().equals("ball"))
                            || (a.getUserData().equals("ball") && b.getUserData().equals("goal"))){
                        goalHit = true;

                       }

                }

            }

            @Override
            public void endContact(Contact contact) {

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
    }

    private void createStop() {
        BodyDef bdef = new BodyDef();
        FixtureDef fdef = new FixtureDef();
        bdef.type = BodyDef.BodyType.StaticBody;

        CircleShape s = new CircleShape();
        s.setRadius(.1f);
        bdef.position.set(1,5);
        fdef.isSensor = false;
        fdef.shape = s;
        fixed = world.createBody(bdef);
        fixed.createFixture(fdef);
        fixed.setUserData("fixed");
        s.dispose();
    }

    private void createGoal() {

        if(goalHit){
            xPos += 10;
            goalHit = false;
        }

        BodyDef bdef = new BodyDef();
        bdef.position.set(0+xPos, 4+yPos);
        bdef.type = BodyDef.BodyType.StaticBody;

        PolygonShape s = new PolygonShape();
        s.setAsBox(.2f,1);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = s;

        block = world.createBody(bdef);
        block.createFixture(fdef);
        block.setUserData("goal");

        s.dispose();


    }

    private void resetGame() {
        Array<Body> bodies = new Array<Body>();
        amount = 0;
        world.getBodies(bodies);
        for(Body b : bodies){

            if(b.getUserData() == null) {
                world.destroyBody(b);
            }else if(!b.getUserData().equals("fixed")){
                world.destroyBody(b);
            }

        }
        running = false;
        createFloor();
        createCatapult();
        createBall();
        createGoal();
    }

    private void createBall() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(-3, 3);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.bullet = true;


        PolygonShape p = new PolygonShape();
        p.setAsBox(.05f,.2f);


        FixtureDef fdef = new FixtureDef();
        fdef.shape = p;
        fdef.density = .1f;
        fdef.restitution = .5f;
        fdef.isSensor = true;



        ball = world.createBody(bdef);
        ball.createFixture(fdef);
        ball.setUserData("ball");

        p.setAsBox(.2f,.05f);
        bdef.position.set(-3.1f,3.1f);
        bdef.position.set(-1,1);
        fdef.isSensor = false;
        fdef.shape = p;
        armLeft = world.createBody(bdef);
        armLeft.createFixture(fdef);
        armLeft.setUserData("ball");

        RevoluteJointDef rw = new RevoluteJointDef();


        rw.bodyA = ball;
        rw.bodyB = armLeft;
        rw.localAnchorA.set(-.1f,.15f);
        rw.localAnchorB.set(.2f, 0);
        world.createJoint(rw);

        bdef.position.set(-2.9f,2.9f);
        armRight = world.createBody(bdef);
        armRight.createFixture(fdef);
        armRight.setUserData("ball");


        rw.bodyA = ball;
        rw.bodyB = armRight;
        rw.localAnchorA.set(.1f,.15f);
        rw.localAnchorB.set(-.2f,0);
        world.createJoint(rw);

        legLeft = world.createBody(bdef);
        legLeft.createFixture(fdef);
        legLeft.setUserData("ball");

        rw.bodyB = legLeft;
        rw.localAnchorA.set(-.1f,-.2f);
        rw.localAnchorB.set(.2f,0);
        world.createJoint(rw);

        legRight = world.createBody(bdef);
        legRight.createFixture(fdef);
        legRight.setUserData("ball");

        rw.bodyB = legRight;
        rw.localAnchorA.set(.1f,-.2f);
        rw.localAnchorB.set(-.2f,0);
        world.createJoint(rw);

        CircleShape s = new CircleShape();
        s.setRadius(.2f);

        fdef.shape = s;
        //fdef.isSensor = true;
        head = world.createBody(bdef);
        head.createFixture(fdef);
        head.setUserData("ball");


        rw.bodyB = head;
        rw.bodyA = ball;
        rw.localAnchorB.set(0,0);
        rw.localAnchorA.set(0,.5f);
        world.createJoint(rw);


        s.dispose();
        p.dispose();



    }

    private void createCatapult() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(0,1);
        bdef.bullet = true;


        bdef.type = BodyDef.BodyType.DynamicBody;

        PolygonShape pShape = new PolygonShape();
        pShape.setAsBox(4, .2f);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = pShape;
        fdef.density = .1f;
        fdef.restitution = .1f;
        fdef.friction = .4f;


        catp = world.createBody(bdef);
        catp.createFixture(fdef);

        pShape.setAsBox(.5f,1);
        bdef.position.set(4, 1);
        bdef.type = BodyDef.BodyType.StaticBody;
        fdef.isSensor = true;

        block = world.createBody(bdef);
        block.createFixture(fdef);

        RevoluteJointDef wj = new RevoluteJointDef();
        wj.bodyA = catp;
        wj.bodyB = block;
        wj.localAnchorA.set(4,0);
        wj.localAnchorB.set(0,0);
        world.createJoint(wj);

        bdef.type = BodyDef.BodyType.StaticBody;




        pShape.setAsBox(.1f,1f);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(-4,1f);
        fdef.shape = pShape;
        fdef.isSensor = false;
        pivot = world.createBody(bdef);
        pivot.createFixture(fdef);

        WeldJointDef wl = new WeldJointDef();
        wl.bodyA = catp;
        wl.bodyB = pivot;
        wl.localAnchorA.set(-3.5f,0);
        wl.localAnchorB.set(0,-.4f);
        world.createJoint(wl);



        pShape.dispose();



    }

    private void createFloor(){

        BodyDef bdef = new BodyDef();
        bdef.position.set(1000,-1);
        bdef.type = BodyDef.BodyType.StaticBody;

        PolygonShape pShape = new PolygonShape();
        pShape.setAsBox(1000, 1);


        FixtureDef fdef = new FixtureDef();
        fdef.shape = pShape;

        body = world.createBody(bdef);
        body.createFixture(fdef);


    }

	@Override
	public void render () {
            Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
            if(amount > 0){
                running = true;
                catp.applyForceToCenter(new Vector2(0,speed), true);
                amount--;
            }

            sb.begin();
            distance = (int) ball.getPosition().dst(block.getPosition());
            String s = ""+force + "\n" + speed + "\n x: " + xPos + "\n y: " + yPos + "\n" + debug + "\n Distance to goal: " + distance;
            if(goalHit) s += "\n GOOOOAAAL!";
        font.draw(sb, s , uiCam.viewportWidth / 2, uiCam.viewportHeight / 2);

            sb.end();

            camera.position.set(ball.getPosition(), 0);
            camera.update();
            world.step(0.02f, 8, 3);
            render.render(world, camera.combined);

        }

    @Override
    public void dispose() {
        world.dispose();
        font.dispose();
        sb.dispose();
        render.dispose();
        super.dispose();
    }
}

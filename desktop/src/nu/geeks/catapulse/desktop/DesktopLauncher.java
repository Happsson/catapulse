package nu.geeks.catapulse.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import nu.geeks.catapulse.Catapulse;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        config.title = "Catapulse";
        config.width = 960;
        config.height = 540;
		new LwjglApplication(new Catapulse(), config);
	}
}

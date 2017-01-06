/**
 * Created by ahmedengu.
 */
public class Game {
    static int base = 40;
    static float x;
    static float y;
    static float gameScore = 0;
    static float changeX = -4;
    static float changeY = -4;
    static int gameOver = 0;
    static float Multiplier = 1.0f;
    static int gameLevel = 1;
    public static Sketch sketch;

    static void gameDraw() {
        if (gameOver == 0) {
            sketch.background(209, 157, 44);
            sketch.text("LEVEL " + gameLevel, sketch.width / 2, sketch.height / 2 - 50);
            sketch.text("SCORE " + gameScore, sketch.width / 2, sketch.height / 2);
            sketch.stroke(51, 149, 24);
            sketch.fill(51, 149, 24);
            sketch.rect(sketch.mouseX, sketch.height - base, 200, base);
            sketch.rect(sketch.mouseX, 0, 200, base);
            sketch.ellipse(x, y, 10, 10);
            sketch.stroke(0);
            sketch.fill(0);
            x = x + changeX;
            y = y + changeY;
            if (x < 0 | x > (sketch.width)) {
                changeX = -changeX;
            }
            if (y < base) {
                if (x > sketch.mouseX && x < sketch.mouseX + 200) {
                    changeY = -changeY; //bounce back
                    gameScore++;
                    if ((gameScore % 3) == 0) {
                        changeX = Multiplier * changeX;
                        changeY = Multiplier * changeY;
                        gameLevel++;
                    }
                } else {
                    gameOverSplash();
                }
            }

            if (y > sketch.height - base) {
                if (x > sketch.mouseX && x < sketch.mouseX + 200) {
                    changeY = -changeY; //bounce back
                    gameScore++;
                    if ((gameScore % 3) == 0) {
                        changeX = Multiplier * changeX;
                        changeY = Multiplier * changeY;
                        gameLevel++;
                    }
                } else {
                    gameOverSplash();
                }
            }
        } else {
            sketch.background(100, 100, 200);
            sketch.text("Game Over! Your Score: " + gameScore, sketch.width / 2 - 150, sketch.height / 2);
        }
    }

    static void gameOverSplash() {
        gameOver = 1;
    }

}

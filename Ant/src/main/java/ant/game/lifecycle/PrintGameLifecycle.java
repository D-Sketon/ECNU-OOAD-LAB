package ant.game.lifecycle;

import ant.game.core.CreepingGame;
import ant.game.core.GameContext;
import ant.game.entity.Ant;

import java.util.*;

/**
 * 控制台输出游戏信息
 */
public class PrintGameLifecycle extends AbstractGameLifecycle {

    private static final int COLOR_WHITE = 30;
    private static final int COLOR_RED = 31;
    private static final int COLOR_GREEN = 32;
    private static final int COLOR_BLUE = 34;
    private static final int COLOR_GREY = 37;

    public PrintGameLifecycle(GameContext gameContext, CreepingGame creepingGame) {
            super(gameContext, creepingGame);
        }

    private static final Integer[] COLORS = {COLOR_WHITE, COLOR_RED, COLOR_GREEN, COLOR_BLUE, COLOR_GREY};

    private final HashMap<Ant, Integer> ANT_COLOR = new HashMap<>();

    private void printInfo(){

        System.out.println("=====Game id: " + gameContext.getGameId() + "=====");
        System.out.println("Game ticks: " + gameContext.getTimeTicks());
        System.out.println("Ants: ");

        List<Ant> ants = new ArrayList<>(gameContext.getAnts());
        ants.sort((a1, a2) -> (a1.getPosition().getX() - a2.getPosition().getX()) < 0 ? -1 : 1);

        int rodLength = gameContext.getRod().getHalfLength() * 2;
        char[] directionChars = new char[rodLength / 5 + 3];
        char[] posChars = new char[rodLength / 5 + 3];
        Arrays.fill(directionChars, ' ');
        Arrays.fill(posChars, ' ');

        int lastPosi = 0;
        StringBuilder formattedAntString = new StringBuilder(" ");
        for (Ant ant : ants) {
            int posi = (int) ant.getPosition().getX() / 5 + 1;
            int velX = (int) ant.getVelocity().getX();

            directionChars[posi] = '-';
            if (velX > 0) {
                if(directionChars[posi - 1] == ' '){
                    directionChars[posi - 1] = '-';
                }
                directionChars[posi + 1] = '>';
            } else {
                directionChars[posi - 1] = '<';
                if(directionChars[posi + 1] == ' '){
                    directionChars[posi + 1] = '-';
                }
            }

            if(posi - lastPosi - 1 > 0 || lastPosi == 0){
                char[] emptyRod = new char[posi - lastPosi - 1];
                Arrays.fill(emptyRod, ' ');
                formattedAntString.append("\33[34;4m").append(emptyRod).append("\33[0m")
                        .append("\33[0m\33[").append(ANT_COLOR.get(ant)).append(";4mM\33[0m\33[34;4m");

            }
            lastPosi = posi;

            int posx = ((int)ant.getPosition().getX());
            String posStr = (posx < 100
                    ? (posx < 10 ? " " : "0")
                    : "") + posx;
            for (int i = 0; i < posStr.length(); i++) {
                posChars[posi + i - 1] = posStr.charAt(i);
            }
        }
        char[] emptyRod = new char[rodLength / 5 + 2 - lastPosi - 1];
        Arrays.fill(emptyRod, ' ');
        formattedAntString.append("\33[34;4m").append(emptyRod).append("\33[0m\n");

        System.out.printf("\33[31;2m%s\33[0m%n", new String(directionChars));
        System.out.print(formattedAntString);
        System.out.printf("\33[36;0m%s\33[0m%n", new String(posChars));

        System.out.println("=========================");
    }

    private void buildAntColor(){
        List<Integer> colors = new ArrayList<>(Arrays.asList(COLORS));
        Collections.shuffle(colors);
        List<Ant> ants = gameContext.getAnts();
        for (int i = 0; i < ants.size(); i++) {
            ANT_COLOR.put(ants.get(i), colors.get(i));
        }
    }

    @Override
    public void onCreate() {
        buildAntColor();
        printInfo();
    }

    @Override
    public void onUpdate() {
        printInfo();
    }
}

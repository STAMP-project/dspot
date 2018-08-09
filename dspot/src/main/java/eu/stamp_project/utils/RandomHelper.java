package eu.stamp_project.utils;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/08/18
 *
 * This class manages all the randomness of DSpot
 *
 */
public class RandomHelper {

    private static Random random = new Random(23L);


    public static void setSeedRandom(long seed) {
        random = new Random(seed);
    }

    public static Random getRandom() {
        return random;
    }

    public static String getRandomString(int length) {
        return IntStream.range(0, length)
                .map(i -> getRandomChar())
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static char getRandomChar() {
        int value = getRandom().nextInt(94) + 32;
        char c = (char) ((value == 34 || value == 39 || value == 92) ? value + (getRandom().nextBoolean() ? 1 : -1) : value);
        return c;//discarding " ' and \
    }
}

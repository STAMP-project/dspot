package fr.inria.diversify.dspot;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.amplifier.TestDataMutator;
import fr.inria.diversify.runner.InputConfiguration;

import java.util.Collections;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/18/17
 */
public class Main {

    public static void main(String[] args) {
        try {
            DSpot dSpot = new DSpot(new InputConfiguration(args[0]), Collections.singletonList(new TestDataMutator()));
            dSpot.amplifyTest(args[1]);
        } catch (InvalidSdkException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

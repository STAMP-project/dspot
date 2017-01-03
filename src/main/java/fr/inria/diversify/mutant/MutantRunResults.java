package fr.inria.diversify.mutant;

import spoon.reflect.declaration.CtClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/3/17
 */
public class MutantRunResults {

    private List<CtClass> killedMutants;
    private List<CtClass> remainsAliveMutant;

    public MutantRunResults() {
        this.killedMutants = new ArrayList<>();
        this.remainsAliveMutant = new ArrayList<>();
    }

    public void mutantKilled(CtClass mutant) {
        this.killedMutants.add(mutant);
    }

    public void mutantRemainAlive(CtClass mutant) {
        this.remainsAliveMutant.add(mutant);
    }

    public List<CtClass> getKilledMutants() {
        return killedMutants;
    }

    public List<CtClass> getRemainsAliveMutant() {
        return remainsAliveMutant;
    }

}

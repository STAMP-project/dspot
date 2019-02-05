package eu.stamp_project.prettifier.code2vec.builder;

public class NumbersOfMethodsPerSet {

    public final int numberOfMethodForTraining;

    public final int numberOfMethodForValidation;

    public final int numberOfMethodForTest;

    public NumbersOfMethodsPerSet(int numberOfMethodForTraining, int numberOfMethodForValidation, int numberOfMethodForTest) {
        this.numberOfMethodForTraining = numberOfMethodForTraining;
        this.numberOfMethodForValidation = numberOfMethodForValidation;
        this.numberOfMethodForTest = numberOfMethodForTest;
    }

    public int total() {
        return this.numberOfMethodForTraining + this.numberOfMethodForValidation + this.numberOfMethodForTest;
    }

    public NumbersOfMethodsPerSet sum(NumbersOfMethodsPerSet that) {
        return new NumbersOfMethodsPerSet(
                this.numberOfMethodForTraining + that.numberOfMethodForTraining,
                this.numberOfMethodForValidation + that.numberOfMethodForValidation,
                this.numberOfMethodForTest + that.numberOfMethodForTest
        );
    }

    @Override
    public String toString() {
        return "numberOfMethodForTraining=" + numberOfMethodForTraining +
                ", numberOfMethodForValidation=" + numberOfMethodForValidation +
                ", numberOfMethodForTest=" + numberOfMethodForTest;
    }
}
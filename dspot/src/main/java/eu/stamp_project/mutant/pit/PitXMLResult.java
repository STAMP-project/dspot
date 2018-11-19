package eu.stamp_project.mutant.pit;

/**
 * Created by Andrew Bwogi
 * abwogi@kth.se
 * on 14/11/18
 */
public class PitXMLResult extends AbstractPitResult {

    private final String methodDescription;

    private final String mutationDescription;

    private final int index;

    private final int block;

    protected final int numberOfTestsRun;

    protected final boolean detected;

    public PitXMLResult(String fullQualifiedNameOfMutatedClass, AbstractPitResult.State stateOfMutant,
                        String fullQualifiedNameMutantOperator,
                        String fullQualifiedNameMethod, String fullQualifiedNameOfKiller,
                        int lineNumber, String nameOfLocalisation, String methodDescription,
                        String mutationDescription, int index, int block, int numberOfTestsRun, boolean detected) {
        super(fullQualifiedNameOfMutatedClass, stateOfMutant, fullQualifiedNameMutantOperator,
                fullQualifiedNameMethod, fullQualifiedNameOfKiller, lineNumber, nameOfLocalisation);
        this.methodDescription = methodDescription;
        this.index = index;
        this.block = block;
        this.mutationDescription = mutationDescription;
        this.numberOfTestsRun = numberOfTestsRun;
        this.detected = detected;
    }

    public String getmethodDescription() {
        return methodDescription;
    }

    public String getMutationDescription() {
        return mutationDescription;
    }

    public int getIndex() {
        return index;
    }

    public int getBlock() {
        return block;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PitXMLResult result = (PitXMLResult) o;

        return lineNumber == result.lineNumber &&
                fullQualifiedNameOfMutatedClass.endsWith(result.fullQualifiedNameOfMutatedClass) &&
                nameOfMutatedMethod.equals(result.nameOfMutatedMethod) &&
                fullQualifiedNameMutantOperator.equals(result.fullQualifiedNameMutantOperator) &&
                index == result.index &&
                block == result.block &&
                mutationDescription.equals(result.mutationDescription);
    }

    @Override
    public int hashCode() {
        int result = stateOfMutant != null ? stateOfMutant.hashCode() : 0;
        result = 31 * result + (fullQualifiedNameMutantOperator != null ? fullQualifiedNameMutantOperator.hashCode() : 0);
        result = 31 * result + lineNumber;
        result = 31 * result + (nameOfMutatedMethod != null ? nameOfMutatedMethod.hashCode() : 0);
        result = 31 * result + (mutationDescription != null ? mutationDescription.hashCode() : 0);
        result = 31 * result + index;
        result = 31 * result + block;
        return result;
    }

    @Override
    public String toString() {
        return "PitXMLResult{" +
                "fullQualifiedNameOfMutatedClass='" + fullQualifiedNameOfMutatedClass + '\'' +
                ", fullQualifiedNameMutantOperator='" + fullQualifiedNameMutantOperator + '\'' +
                ", nameOfMutatedMethod='" + nameOfMutatedMethod + '\'' +
                ", lineNumber=" + lineNumber +
                ", stateOfMutant=" + stateOfMutant +
                ", fullQualifiedNameOfKiller='" + fullQualifiedNameOfKiller + '\'' +
                ", simpleNameMethod='" + simpleNameMethod + '\'' +
                ", testCase=" + testCase + '\'' +
                ", methodDescription='" + methodDescription + '\'' +
                ", mutationDescription='" + mutationDescription + '\'' +
                ", index='" + index + '\'' +
                ", block='" + block + '\'' +
                ", numberOfTestsRun='" + numberOfTestsRun + '\'' +
                ", detected='" + detected + '\'' +
                '}';
    }
}

package eu.stamp_project.diff_test_selection.coverage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Benjamin DANGLOT
 * benjamin.danglot@davidson.fr
 * on 14/06/2021
 */
public class Coverage {

    public final Map<String, TestClassCoverage> testClassCoverage;

    public Coverage() {
        this.testClassCoverage = new LinkedHashMap<>();
    }

    public void addCoverage(String testClassName, String testMethodName, String className, int line, int hitCounts) {
        if (!this.testClassCoverage.containsKey(testClassName)) {
            this.testClassCoverage.put(testClassName, new TestClassCoverage(testClassName));
        }
        this.testClassCoverage.get(testClassName).addCoverage(testMethodName, className, line, hitCounts);
    }

    public Set<String> getTestClasses() {
        return this.testClassCoverage.keySet();
    }

    public Set<String> getClassesForTestClassAndMethodName(String testClassName, String testMethodName) {
        return this.testClassCoverage.get(testClassName).getClassesForTestMethodName(testMethodName);
    }

    public Set<String> getTestMethodsForTestClassName(String testClassName) {
        return this.testClassCoverage.get(testClassName).getTestMethods();
    }

    public List<LineCoverage> getCoverageForTestClassTestMethodAndClassNames(String testClassName, String testMethodName, String className) {
        return this.testClassCoverage.get(testClassName).getCoverageForTestMethodAndClassNames(testMethodName, className);
    }

    public Map<String, ClassCoverage> getTestMethodCoverageForClassName(String testClassName, String testMethodName) {
        return this.testClassCoverage.get(testClassName).getTestMethodCoverage(testMethodName);
    }
}

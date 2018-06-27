package eu.stamp_project.compare;

import java.io.Serializable;
import java.util.*;

/**
 * User: Simon
 * Date: 23/10/15
 * Time: 15:36
 */
public class Observation implements Serializable {

    private Set<String> notDeterministValues;

    private Map<String, Object> observationValues;

    public Observation() {
        this.observationValues = new LinkedHashMap<>();
        this.notDeterministValues = new HashSet<>();
    }

    public boolean add(String stringObject, Object value) {
        if (!notDeterministValues.contains(stringObject)) {
            if (observationValues.containsKey(stringObject)) {
                Object oldValue = observationValues.get(stringObject);
                if (oldValue == null) {
                    if (value == null) {
                        return true;
                    } else {
                        notDeterministValues.add(stringObject);
                        return false;
                    }
                } else if (!equals(oldValue, value)) {
                    notDeterministValues.add(stringObject);
                    return false;
                }
            } else {
                observationValues.put(stringObject, value);
            }
            return true;
        } else {
            return false;
        }
    }

    protected boolean equals(Object o1, Object o2) {
        boolean isArray1 = o1.getClass().isArray();
        boolean isArray2 = o2.getClass().isArray();
        if (isArray1 == isArray2) {
            if (isArray1) {
                try {
                    String type = o1.getClass().getCanonicalName();
                    if (type.equals("int[]")) {
                        return Arrays.equals((int[]) o1, (int[]) o2);
                    }
                    if (type.equals("short[]")) {
                        return Arrays.equals((short[]) o1, (short[]) o2);
                    }
                    if (type.equals("byte[]")) {
                        return Arrays.equals((byte[]) o1, (byte[]) o2);
                    }
                    if (type.equals("long[]")) {
                        return Arrays.equals((long[]) o1, (long[]) o2);
                    }
                    if (type.equals("float[]")) {
                        return Arrays.equals((float[]) o1, (float[]) o2);
                    }
                    if (type.equals("double[]")) {
                        return Arrays.equals((double[]) o1, (double[]) o2);
                    }
                    if (type.equals("boolean[]")) {
                        return Arrays.equals((boolean[]) o1, (boolean[]) o2);
                    }
                    if (type.equals("char[]")) {
                        return Arrays.equals((char[]) o1, (char[]) o2);
                    }
                    return Arrays.equals((Object[]) o1, (Object[]) o2);
                } catch (Exception e) {
                    return false;
                }
            } else {
                return o1.equals(o2);
            }
        } else {
            return false;
        }
    }

    public void purify() {
        List<String> keysToBeRemoved = new ArrayList<>();
        for (String key : this.observationValues.keySet()) {
            try {
                if (!ObjectLog.isSerializable(this.observationValues.get(key))) {
                    keysToBeRemoved.add(key);
                }
            } catch (Exception e) {
                e.printStackTrace();
                keysToBeRemoved.add(key);
                continue;
            }
        }
        keysToBeRemoved.forEach(this.observationValues::remove);
    }

    public Set<String> getNotDeterministValues() {
        return notDeterministValues;
    }

    public Map<String, Object> getObservationValues() {
        return observationValues;
    }

}
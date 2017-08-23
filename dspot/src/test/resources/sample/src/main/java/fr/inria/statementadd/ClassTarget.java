package fr.inria.statementadd;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Collections;

public class ClassTarget {

	public List<ClassParameterAmplify> getList(int parameter) {
		return Collections.singletonList(new ClassParameterAmplify(parameter));
	}

	public int getSizeOf(Collection collection) {
		return collection.size();
	}

	public int getSizeOfTypedCollection(Collection<ClassParameterAmplify> collection) {
		return collection.size();
	}

	public int getSizeOf(Set set) {
		return set.size();
	}

	public int getSizeOfTypedCollection(Set<ClassParameterAmplify> set) {
		return set.size();
	}

	public int getSizeOf(Map map) {
		return map.size();
	}

	public int getSizeOfTypedMap(Map<ClassParameterAmplify, String> map) {
		return map.size();
	}

}
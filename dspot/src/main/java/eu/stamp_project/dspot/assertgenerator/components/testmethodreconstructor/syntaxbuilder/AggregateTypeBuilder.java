package eu.stamp_project.dspot.assertgenerator.components.testmethodreconstructor.syntaxbuilder;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AggregateTypeBuilder {
    public static String getNewArrayExpression(Object array){
        StringBuilder sb = new StringBuilder();
        ArrayList<Integer> al = new ArrayList<>();
        getArrayInstance(array,sb,al);
        int dimensions = getDimensions(array);
        for(int i = dimensions; i>0; i--){
            sb.insert(0,"[]");
        }
        for(int i = 1; i<sb.length(); i++){
            if(sb.charAt(i-1) == '}' && sb.charAt(i) == '{') {
                sb.insert(i,",");
            }
        }
        sb.insert(0,"new " + getArrayComponentType(array));
        return sb.toString();
    }

    private static void getArrayInstance(Object array, StringBuilder sb, ArrayList al) {
        sb.append("{");
        int size = Array.getLength(array);
        al.add(size);
        for (int i = 0; i < size; i++) {
            Object element = Array.get(array, i);
            if (element.getClass().isArray()) {
                getArrayInstance(element, sb, al);
            } else {
                addValue(element,sb);
                if(i+1 < size) {
                    sb.append(",");
                }
            }
        }
        sb.append("}");
    }

    private static void addValue(Object element,StringBuilder sb) {
        if(element instanceof Character) {
            switch ((char) element) {
                case '\t':
                    sb.append("'\\t'");
                    break;
                case '\b':
                    sb.append("'\\b'");
                    break;
                case '\n':
                    sb.append("'\\n'");
                    break;
                case '\r':
                    sb.append("'\\r'");
                    break;
                case '\f':
                    sb.append("'\\f'");
                    break;
                case '\'':
                    sb.append("'\\''");
                    break;
                case '\"':
                    sb.append("'\\\"'");
                    break;
                case '\\':
                    sb.append("'\\\\'");
                    break;
                default:
                    sb.append("'" + element + "'");
            }
        } else if(element instanceof Float) {
            sb.append(element + "F");
        } else if(element instanceof Long) {
            sb.append(element + "L");
        } else {
            sb.append(element);
        }
    }

    public static String getArrayComponentType(Object array) {
        int dimensions = getDimensions(array);
        char type = array.getClass().getName().charAt(dimensions);
        switch(type){
            case 'Z': return "boolean";
            case 'B': return "byte";
            case 'C': return "char";
            case 'D': return "double";
            case 'F': return "float";
            case 'I': return "int";
            case 'J': return "long";
            case 'S': return "short";
            default : return "";
        }
    }

    public static Boolean isPrimitiveArray(Object array) {
        return !array.getClass().getName().contains("L");
    }

    private static int getDimensions(Object array) {
        return 1 + array.getClass().getName().lastIndexOf('[');
    }
}

package fr.inria.amp;

public class JavaPoet {

    public String method() throws Exception {
        return ""
                + "package com.squareup.tacos;\n"
                + "\n"
                + "class A {\n"
                + "  class B {\n"
                + "    class C {\n"
                + "      Twin.D d;\n"
                + "\n"
                + "      class Nested {\n"
                + "        class Twin {\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "\n"
                + "  class Twin {\n"
                + "    class D {\n"
                + "    }\n"
                + "  }\n"
                + "}\n";
    }

}
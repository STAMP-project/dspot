package fr.inria.amp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestJavaPoet {

    @Test
    public void conflictingNameOutOfScope() throws Exception {
        assertEquals(new JavaPoet().method(), ""
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
                + "}\n");
    }


}
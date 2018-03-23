package com.squareup.javapoet;


import javax.lang.model.element.Modifier;
import org.junit.Assert;
import org.junit.Test;


public class AmplParameterSpecTest {
    @Test(timeout = 10000)
    public void equalsAndHashCode_add49() throws Exception {
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        boolean o_equalsAndHashCode_add49__15 = ParameterSpec.builder(int.class, "foo").build().equals(ParameterSpec.builder(int.class, "foo").build());
        Assert.assertTrue(o_equalsAndHashCode_add49__15);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32() throws Exception {
        ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        Assert.assertEquals("static int i", ParameterSpec.builder(int.class, "foo").build().toString());
    }

    @Test(timeout = 10000)
    public void equalsAndHashCodelitString18_failAssert7() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "L[{$QV5:Wz").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            Assert.fail("equalsAndHashCodelitString18 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_add40() throws Exception {
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        int o_equalsAndHashCode_add40__7 = ParameterSpec.builder(int.class, "foo").build().hashCode();
        Assert.assertEquals(1955995925, ((int) (o_equalsAndHashCode_add40__7)));
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
    }

    @Test(timeout = 10000)
    public void equalsAndHashCodelitString6_failAssert3() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "GdhscbCS@!").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            Assert.fail("equalsAndHashCodelitString6 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void nullAnnotationsAddition_add212() throws Exception {
        try {
            ParameterSpec.builder(int.class, "foo").addAnnotations(null);
        } catch (Exception e) {
            String o_nullAnnotationsAddition_add212__6 = e.getMessage();
            Assert.assertEquals("annotationSpecs == null", e.getMessage());
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd26() throws Exception {
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        int o_equalsAndHashCode_sd26__15 = ParameterSpec.builder(int.class, "foo").build().hashCode();
        Assert.assertEquals((-130075578), ((int) (o_equalsAndHashCode_sd26__15)));
    }

    @Test(timeout = 10000)
    public void equalsAndHashCodelitString2_failAssert0() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "annotationSpecs == null").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            Assert.fail("equalsAndHashCodelitString2 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCodelitString16_failAssert6() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "annotationSpecs == null").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            org.junit.Assert.fail("equalsAndHashCodelitString16 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }
}


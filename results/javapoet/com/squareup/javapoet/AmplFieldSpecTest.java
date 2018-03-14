package com.squareup.javapoet;


import javax.lang.model.element.Modifier;
import org.junit.Assert;
import org.junit.Test;


public class AmplFieldSpecTest {
    @Test(timeout = 10000)
    public void equalsAndHashCode() {
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        int o_equalsAndHashCode__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        int o_equalsAndHashCode__14 = b.hashCode();
        Assert.assertEquals((-1882877815), ((int) (o_equalsAndHashCode__14)));
        Assert.assertEquals((-1483589884), ((int) (o_equalsAndHashCode__7)));
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31() {
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        // AssertGenerator add assertion
        Assert.assertEquals(-1483589884, ((int) (o_equalsAndHashCode_sd31__7)));
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator add assertion
        Assert.assertEquals(-1882877815, ((int) (o_equalsAndHashCode_sd31__14)));
        // AssertGenerator add assertion
        Assert.assertEquals(-1483589884, ((int) (o_equalsAndHashCode_sd31__7)));
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd131() {
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31_sd131__21 = // StatementAdd: add invocation of a method
        a.hashCode();
        // AssertGenerator add assertion
        Assert.assertEquals(-1882877815, ((int) (o_equalsAndHashCode_sd31_sd131__21)));
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133() {
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd552 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd552_sd3599() {
        Object[] __DSPOT_args_539 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
        String __DSPOT_format_538 = "SF>=Hd[v*m?bWX!BwrY(";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd133_sd552__25 = // StatementAdd: add invocation of a method
        a.toString();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_538, __DSPOT_args_539);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133_sd552__25);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd552 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd552_sd3602_failAssert12() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Object[] __DSPOT_args_542 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
            String __DSPOT_format_541 = "HGthUmyN*yr|/$GFAA%N";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            a.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
            a.toString();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd31_sd133_sd552__25 = // StatementAdd: add invocation of a method
            a.toString();
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_541, __DSPOT_args_542);
            org.junit.Assert.fail("equalsAndHashCode_sd31_sd133_sd552_sd3602 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd554 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd554_sd2595() {
        Object[] __DSPOT_args_351 = new Object[0];
        String __DSPOT_format_350 = "LeJF|k#yzb011,aR/,/%";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31_sd133_sd554__25 = // StatementAdd: add invocation of a method
        b.hashCode();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_350, __DSPOT_args_351);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd554 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd554_sd2596() {
        Modifier[] __DSPOT_modifiers_352 = new Modifier[]{  };
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31_sd133_sd554__25 = // StatementAdd: add invocation of a method
        b.hashCode();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_352);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd554 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd554_sd2598() {
        Object[] __DSPOT_args_354 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_353 = "(Qx#R_2M%>[bF%=nK}(5";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31_sd133_sd554__25 = // StatementAdd: add invocation of a method
        b.hashCode();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_353, __DSPOT_args_354);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd558 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd558_sd1364() {
        Object[] __DSPOT_args_98 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
        String __DSPOT_format_97 = "X]YP!2!1tKs!9)M4gfZk";
        Object[] __DSPOT_args_28 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_27 = "Y][1u)p]QM-k,I]-r8//";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
        a.toString();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_27, __DSPOT_args_28);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_97, __DSPOT_args_98);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd558 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd558_sd1365() {
        Modifier[] __DSPOT_modifiers_99 = new Modifier[]{  };
        Object[] __DSPOT_args_28 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_27 = "Y][1u)p]QM-k,I]-r8//";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
        a.toString();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_27, __DSPOT_args_28);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_99);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd558 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd558_sd1367() {
        Object[] __DSPOT_args_101 = new Object[0];
        String __DSPOT_format_100 = "bR-2-=M,.G+$]g)e+[it";
        Object[] __DSPOT_args_28 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_27 = "Y][1u)p]QM-k,I]-r8//";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
        a.toString();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_27, __DSPOT_args_28);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_100, __DSPOT_args_101);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd558 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd558_sd1369() {
        Object[] __DSPOT_args_103 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
        String __DSPOT_format_102 = "{.K}Mjm5t1& yMN`s;U{";
        Object[] __DSPOT_args_28 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_27 = "Y][1u)p]QM-k,I]-r8//";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
        a.toString();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_34 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_27, __DSPOT_args_28);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_34.addJavadoc(__DSPOT_format_102, __DSPOT_args_103);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd558 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd558_sd1370() {
        Modifier[] __DSPOT_modifiers_104 = new Modifier[]{  };
        Object[] __DSPOT_args_28 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_27 = "Y][1u)p]QM-k,I]-r8//";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_34 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_27, __DSPOT_args_28);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_34.addModifiers(__DSPOT_modifiers_104);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd558 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd558_sd1372() {
        Object[] __DSPOT_args_106 = new Object[]{ new Object(), new Object() };
        String __DSPOT_format_105 = "!9gwX1[[en#R7RXkugQ}";
        Object[] __DSPOT_args_28 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_27 = "Y][1u)p]QM-k,I]-r8//";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
        a.toString();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_34 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_27, __DSPOT_args_28);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_34.initializer(__DSPOT_format_105, __DSPOT_args_106);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd559 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd559_sd1412() {
        Object[] __DSPOT_args_113 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_112 = "tF]or`woVZX>^L,%i.9>";
        Modifier[] __DSPOT_modifiers_29 = new Modifier[]{  };
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
        a.toString();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_29);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_112, __DSPOT_args_113);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd559 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd559_sd1414() {
        Object[] __DSPOT_args_115 = new Object[]{ new Object(), new Object() };
        String __DSPOT_format_114 = "vz-k<;Do^DZks#P][B@B";
        Modifier[] __DSPOT_modifiers_29 = new Modifier[]{  };
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
        a.toString();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_30 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_29);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_30.addJavadoc(__DSPOT_format_114, __DSPOT_args_115);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd559 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd559_sd1415() {
        Modifier[] __DSPOT_modifiers_116 = new Modifier[]{  };
        Modifier[] __DSPOT_modifiers_29 = new Modifier[]{  };
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_30 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_29);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_30.addModifiers(__DSPOT_modifiers_116);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd559 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd559_sd1417() {
        Object[] __DSPOT_args_118 = new Object[]{ new Object() };
        String __DSPOT_format_117 = ">V9s7n4hm|(J/H,Hzr;m";
        Modifier[] __DSPOT_modifiers_29 = new Modifier[]{  };
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_30 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_29);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_30.initializer(__DSPOT_format_117, __DSPOT_args_118);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd561 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd561_failAssert1_sd4166() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Object[] __DSPOT_args_650 = new Object[]{ new Object(), new Object(), new Object() };
            String __DSPOT_format_649 = "-.C!W/4=(weR>z8ZHE$J";
            Object[] __DSPOT_args_31 = new Object[]{ new Object() };
            String __DSPOT_format_30 = "1wly$),bA%.UJum&)<4o";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            a.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
            a.toString();
            // AssertGenerator add assertion
            Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_30, __DSPOT_args_31);
            org.junit.Assert.fail("equalsAndHashCode_sd31_sd133_sd561 should have thrown IllegalArgumentException");
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.addJavadoc(__DSPOT_format_649, __DSPOT_args_650);
        } catch (IllegalArgumentException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd561 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd561_failAssert1_sd4169() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Object[] __DSPOT_args_653 = new Object[]{ new Object() };
            String __DSPOT_format_652 = "#h]$#D +I^sJPc,]f*,q";
            Object[] __DSPOT_args_31 = new Object[]{ new Object() };
            String __DSPOT_format_30 = "1wly$),bA%.UJum&)<4o";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            a.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
            a.toString();
            // AssertGenerator add assertion
            Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_30, __DSPOT_args_31);
            org.junit.Assert.fail("equalsAndHashCode_sd31_sd133_sd561 should have thrown IllegalArgumentException");
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_652, __DSPOT_args_653);
        } catch (IllegalArgumentException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd561 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd561_failAssert1_sd4171() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Object[] __DSPOT_args_655 = new Object[]{ new Object() };
            String __DSPOT_format_654 = "1TccWub}`xH`r1W>TNYk";
            Object[] __DSPOT_args_31 = new Object[]{ new Object() };
            String __DSPOT_format_30 = "1wly$),bA%.UJum&)<4o";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            a.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
            a.toString();
            // AssertGenerator add assertion
            Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_32 = // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_30, __DSPOT_args_31);
            org.junit.Assert.fail("equalsAndHashCode_sd31_sd133_sd561 should have thrown IllegalArgumentException");
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_32.addJavadoc(__DSPOT_format_654, __DSPOT_args_655);
        } catch (IllegalArgumentException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd561 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd561_failAssert1_sd4172() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Modifier[] __DSPOT_modifiers_656 = new Modifier[]{  };
            Object[] __DSPOT_args_31 = new Object[]{ new Object() };
            String __DSPOT_format_30 = "1wly$),bA%.UJum&)<4o";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            a.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
            a.toString();
            // AssertGenerator add assertion
            Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_32 = // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_30, __DSPOT_args_31);
            org.junit.Assert.fail("equalsAndHashCode_sd31_sd133_sd561 should have thrown IllegalArgumentException");
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_32.addModifiers(__DSPOT_modifiers_656);
        } catch (IllegalArgumentException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd133_sd561 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd133_sd561_failAssert1_sd4174() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Object[] __DSPOT_args_658 = new Object[]{ new Object() };
            String __DSPOT_format_657 = "H#Xk%HO9Gb#sRrjByv5%";
            Object[] __DSPOT_args_31 = new Object[]{ new Object() };
            String __DSPOT_format_30 = "1wly$),bA%.UJum&)<4o";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            a.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd31_sd133__21 = // StatementAdd: add invocation of a method
            a.toString();
            // AssertGenerator add assertion
            Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd133__21);
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_32 = // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_30, __DSPOT_args_31);
            org.junit.Assert.fail("equalsAndHashCode_sd31_sd133_sd561 should have thrown IllegalArgumentException");
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_32.initializer(__DSPOT_format_657, __DSPOT_args_658);
        } catch (IllegalArgumentException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd135() {
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31_sd135__21 = // StatementAdd: add invocation of a method
        b.hashCode();
        // AssertGenerator add assertion
        Assert.assertEquals(-1882877815, ((int) (o_equalsAndHashCode_sd31_sd135__21)));
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137() {
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd634 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd634_sd2977() {
        Object[] __DSPOT_args_427 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_426 = "(sN&;Qy]-J(&+WU``^#(";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd137_sd634__25 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137_sd634__25);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_426, __DSPOT_args_427);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137_sd634__25);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd635 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd635_sd1541() {
        Object[] __DSPOT_args_137 = new Object[0];
        String __DSPOT_format_136 = "j*`gmH>[A}8/o#n)B( ^";
        Object __DSPOT_o_40 = new Object();
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_21 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
        // AssertGenerator create local variable with return value of invocation
        boolean o_equalsAndHashCode_sd31_sd137_sd635__27 = // StatementAdd: add invocation of a method
        b.equals(__DSPOT_o_40);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_21.addJavadoc(__DSPOT_format_136, __DSPOT_args_137);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd635 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd635_sd1542() {
        Modifier[] __DSPOT_modifiers_138 = new Modifier[0];
        Object __DSPOT_o_40 = new Object();
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_21 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator create local variable with return value of invocation
        boolean o_equalsAndHashCode_sd31_sd137_sd635__27 = // StatementAdd: add invocation of a method
        b.equals(__DSPOT_o_40);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_21.addModifiers(__DSPOT_modifiers_138);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd635 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd635_sd1544() {
        Object[] __DSPOT_args_140 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
        String __DSPOT_format_139 = "(YasZ%ds TuK*Yo^mgdh";
        Object __DSPOT_o_40 = new Object();
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_21 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
        // AssertGenerator create local variable with return value of invocation
        boolean o_equalsAndHashCode_sd31_sd137_sd635__27 = // StatementAdd: add invocation of a method
        b.equals(__DSPOT_o_40);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_21.initializer(__DSPOT_format_139, __DSPOT_args_140);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd636 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd636_sd3015() {
        Object[] __DSPOT_args_431 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_430 = "+ T}dLQ&?g<xEWJnf[-.";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31_sd137_sd636__25 = // StatementAdd: add invocation of a method
        b.hashCode();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_430, __DSPOT_args_431);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd636 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd636_sd3016() {
        Modifier[] __DSPOT_modifiers_432 = new Modifier[]{  };
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31_sd137_sd636__25 = // StatementAdd: add invocation of a method
        b.hashCode();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_432);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd636 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd636_sd3018() {
        Object[] __DSPOT_args_434 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
        String __DSPOT_format_433 = "[|_.,7KcmZhyus|yGP%H";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31_sd137_sd636__25 = // StatementAdd: add invocation of a method
        b.hashCode();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_433, __DSPOT_args_434);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd638 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd638_sd3105_failAssert10() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Object[] __DSPOT_args_453 = new Object[]{ new Object(), new Object(), new Object() };
            String __DSPOT_format_452 = "F+znA$ZCgaP7!;[:%D+[";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            a.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
            b.toString();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd31_sd137_sd638__25 = // StatementAdd: add invocation of a method
            b.toString();
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_452, __DSPOT_args_453);
            org.junit.Assert.fail("equalsAndHashCode_sd31_sd137_sd638_sd3105 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd640 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd640_sd1594() {
        Object[] __DSPOT_args_149 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_148 = "7yoEh?_F)3VJg?!KP(j8";
        Object[] __DSPOT_args_42 = new Object[]{ new Object(), new Object() };
        String __DSPOT_format_41 = "?h(*fl<xJgehgad?HCt1";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_31 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_41, __DSPOT_args_42);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_31.addJavadoc(__DSPOT_format_148, __DSPOT_args_149);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd640 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd640_sd1597_failAssert0() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Object[] __DSPOT_args_152 = new Object[0];
            String __DSPOT_format_151 = "r@!m3PO{$F=,gxwDv>@=";
            Object[] __DSPOT_args_42 = new Object[]{ new Object(), new Object() };
            String __DSPOT_format_41 = "?h(*fl<xJgehgad?HCt1";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            a.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
            b.toString();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_31 = // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.addJavadoc(__DSPOT_format_41, __DSPOT_args_42);
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_31.initializer(__DSPOT_format_151, __DSPOT_args_152);
            org.junit.Assert.fail("equalsAndHashCode_sd31_sd137_sd640_sd1597 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd641 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd641_sd1639() {
        Object[] __DSPOT_args_156 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
        String __DSPOT_format_155 = "}gb 2PqgP;4/v.v)|C5[";
        Modifier[] __DSPOT_modifiers_43 = new Modifier[0];
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_43);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_155, __DSPOT_args_156);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd641 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd641_sd1644() {
        Object[] __DSPOT_args_161 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_160 = "!0gGi?=}tR?SY{S>>6Om";
        Modifier[] __DSPOT_modifiers_43 = new Modifier[0];
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
        b.toString();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_30 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_43);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_30.addJavadoc(__DSPOT_format_160, __DSPOT_args_161);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd641 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd641_sd1645() {
        Modifier[] __DSPOT_modifiers_162 = new Modifier[]{  };
        Modifier[] __DSPOT_modifiers_43 = new Modifier[0];
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
        b.toString();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_30 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_43);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_30.addModifiers(__DSPOT_modifiers_162);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd641 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd641_sd1647() {
        Object[] __DSPOT_args_164 = new Object[]{ new Object(), new Object() };
        String __DSPOT_format_163 = "R!qjgSF<3#]s7@GrMJBJ";
        Modifier[] __DSPOT_modifiers_43 = new Modifier[0];
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_30 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_43);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_30.initializer(__DSPOT_format_163, __DSPOT_args_164);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd643 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd643_sd1737_failAssert1() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Object[] __DSPOT_args_176 = new Object[]{ new Object() };
            String __DSPOT_format_175 = "by}MPHPQu&o*9)[&-i]$";
            Object[] __DSPOT_args_45 = new Object[]{ new Object(), new Object() };
            String __DSPOT_format_44 = "{+DN-eV8<Or;(?xw0]W#";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            a.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
            b.toString();
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_44, __DSPOT_args_45);
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.addJavadoc(__DSPOT_format_175, __DSPOT_args_176);
            org.junit.Assert.fail("equalsAndHashCode_sd31_sd137_sd643_sd1737 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd643 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd643_sd1740_failAssert2() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Object[] __DSPOT_args_179 = new Object[]{ new Object() };
            String __DSPOT_format_178 = "VG_I.#8$PB^QAT?>YN{q";
            Object[] __DSPOT_args_45 = new Object[]{ new Object(), new Object() };
            String __DSPOT_format_44 = "{+DN-eV8<Or;(?xw0]W#";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            a.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
            b.toString();
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_44, __DSPOT_args_45);
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_178, __DSPOT_args_179);
            org.junit.Assert.fail("equalsAndHashCode_sd31_sd137_sd643_sd1740 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd643 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd643_sd1742() {
        Object[] __DSPOT_args_181 = new Object[0];
        String __DSPOT_format_180 = "ErJ,Q*wO|INCdXv>?dZt";
        Object[] __DSPOT_args_45 = new Object[]{ new Object(), new Object() };
        String __DSPOT_format_44 = "{+DN-eV8<Or;(?xw0]W#";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_33 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_44, __DSPOT_args_45);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_33.addJavadoc(__DSPOT_format_180, __DSPOT_args_181);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd643 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd643_sd1743() {
        Modifier[] __DSPOT_modifiers_182 = new Modifier[]{  };
        Object[] __DSPOT_args_45 = new Object[]{ new Object(), new Object() };
        String __DSPOT_format_44 = "{+DN-eV8<Or;(?xw0]W#";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd31__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        a.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_33 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_44, __DSPOT_args_45);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_33.addModifiers(__DSPOT_modifiers_182);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd31_sd137__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd31_sd137_sd643 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd31_sd137_sd643_sd1745_failAssert3() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Object[] __DSPOT_args_184 = new Object[]{ new Object() };
            String __DSPOT_format_183 = "IntEF_{0#RM)aJlLP]M{";
            Object[] __DSPOT_args_45 = new Object[]{ new Object(), new Object() };
            String __DSPOT_format_44 = "{+DN-eV8<Or;(?xw0]W#";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd31__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            a.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd31_sd137__21 = // StatementAdd: add invocation of a method
            b.toString();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_33 = // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_44, __DSPOT_args_45);
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_33.initializer(__DSPOT_format_183, __DSPOT_args_184);
            org.junit.Assert.fail("equalsAndHashCode_sd31_sd137_sd643_sd1745 should have thrown IllegalStateException");
        } catch (IllegalStateException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35() {
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // AssertGenerator add assertion
        Assert.assertEquals(-1882877815, ((int) (o_equalsAndHashCode_sd35__14)));
        // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator add assertion
        Assert.assertEquals(-1483589884, ((int) (o_equalsAndHashCode_sd35__7)));
        // AssertGenerator add assertion
        Assert.assertEquals(-1882877815, ((int) (o_equalsAndHashCode_sd35__14)));
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd171() {
        Object __DSPOT_o_9 = new Object();
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        boolean o_equalsAndHashCode_sd35_sd171__23 = // StatementAdd: add invocation of a method
        a.equals(__DSPOT_o_9);
        // AssertGenerator add assertion
        Assert.assertFalse(o_equalsAndHashCode_sd35_sd171__23);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd172() {
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35_sd172__21 = // StatementAdd: add invocation of a method
        a.hashCode();
        // AssertGenerator add assertion
        Assert.assertEquals(-1882877815, ((int) (o_equalsAndHashCode_sd35_sd172__21)));
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174() {
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd754 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd754_sd1824() {
        Object[] __DSPOT_args_195 = new Object[]{ new Object(), new Object() };
        String __DSPOT_format_194 = "q!M32Z7w;!lr:)EfUW+}";
        Object __DSPOT_o_60 = new Object();
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_21 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator create local variable with return value of invocation
        boolean o_equalsAndHashCode_sd35_sd174_sd754__27 = // StatementAdd: add invocation of a method
        a.equals(__DSPOT_o_60);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_21.addJavadoc(__DSPOT_format_194, __DSPOT_args_195);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd754 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd754_sd1825() {
        Modifier[] __DSPOT_modifiers_196 = new Modifier[]{  };
        Object __DSPOT_o_60 = new Object();
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_21 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator create local variable with return value of invocation
        boolean o_equalsAndHashCode_sd35_sd174_sd754__27 = // StatementAdd: add invocation of a method
        a.equals(__DSPOT_o_60);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_21.addModifiers(__DSPOT_modifiers_196);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd754 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd754_sd1827() {
        Object[] __DSPOT_args_198 = new Object[]{ new Object(), new Object() };
        String __DSPOT_format_197 = "(710U8Xh}`e!,3/H!B>(";
        Object __DSPOT_o_60 = new Object();
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_21 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
        // AssertGenerator create local variable with return value of invocation
        boolean o_equalsAndHashCode_sd35_sd174_sd754__27 = // StatementAdd: add invocation of a method
        a.equals(__DSPOT_o_60);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_21.initializer(__DSPOT_format_197, __DSPOT_args_198);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd755 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd755_sd3471_failAssert11() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Object[] __DSPOT_args_513 = new Object[0];
            String __DSPOT_format_512 = "($5iQ0^E:LLN(h9M8y-p";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            b.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
            a.toString();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35_sd174_sd755__25 = // StatementAdd: add invocation of a method
            a.hashCode();
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.addJavadoc(__DSPOT_format_512, __DSPOT_args_513);
            org.junit.Assert.fail("equalsAndHashCode_sd35_sd174_sd755_sd3471 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd755 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd755_sd3472() {
        Modifier[] __DSPOT_modifiers_514 = new Modifier[]{  };
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35_sd174_sd755__25 = // StatementAdd: add invocation of a method
        a.hashCode();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_514);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd755 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd755_sd3474() {
        Object[] __DSPOT_args_516 = new Object[0];
        String __DSPOT_format_515 = "2,OJTP_5b<&?,&1:v:z_";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35_sd174_sd755__25 = // StatementAdd: add invocation of a method
        a.hashCode();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_515, __DSPOT_args_516);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd757 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd757_sd3558() {
        Object[] __DSPOT_args_532 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_531 = "}lK2@hr>x>TD[EcXtGzy";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174_sd757__25 = // StatementAdd: add invocation of a method
        a.toString();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_531, __DSPOT_args_532);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174_sd757__25);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd757 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd757_sd3559() {
        Modifier[] __DSPOT_modifiers_533 = new Modifier[]{  };
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174_sd757__25 = // StatementAdd: add invocation of a method
        a.toString();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_533);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174_sd757__25);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd757 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd757_sd3561() {
        Object[] __DSPOT_args_535 = new Object[0];
        String __DSPOT_format_534 = "PzN3[jz(>G,*Nw}omtcr";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174_sd757__25 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174_sd757__25);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_534, __DSPOT_args_535);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174_sd757__25);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd761 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd761_sd3686() {
        Object[] __DSPOT_args_558 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_557 = "Y:3ia]GT35i&DZ7w6;tz";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174_sd761__25 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174_sd761__25);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_557, __DSPOT_args_558);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174_sd761__25);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd761 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd761_sd3687() {
        Modifier[] __DSPOT_modifiers_559 = new Modifier[0];
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174_sd761__25 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174_sd761__25);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_559);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174_sd761__25);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd761 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd761_sd3689() {
        Object[] __DSPOT_args_561 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
        String __DSPOT_format_560 = "_h(ex#Z^J-/RS8h65%8_";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174_sd761__25 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174_sd761__25);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_560, __DSPOT_args_561);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174_sd761__25);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd763 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd763_sd1872() {
        Object[] __DSPOT_args_202 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_201 = "SWVx(:.@ll+JCy` CpY ";
        Object[] __DSPOT_args_63 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_62 = "8p#]q;a7/ez@l%MFZw!E";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_62, __DSPOT_args_63);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_201, __DSPOT_args_202);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd763 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd763_sd1873() {
        Modifier[] __DSPOT_modifiers_203 = new Modifier[]{  };
        Object[] __DSPOT_args_63 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_62 = "8p#]q;a7/ez@l%MFZw!E";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_62, __DSPOT_args_63);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_203);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd763 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd763_sd1875() {
        Object[] __DSPOT_args_205 = new Object[]{ new Object() };
        String __DSPOT_format_204 = "Fo&]bD>!zc90 kasM39!";
        Object[] __DSPOT_args_63 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_62 = "8p#]q;a7/ez@l%MFZw!E";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_62, __DSPOT_args_63);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_204, __DSPOT_args_205);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd763 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd763_sd1877() {
        Object[] __DSPOT_args_207 = new Object[]{ new Object() };
        String __DSPOT_format_206 = "KT>gIevY]dkqSht.80@M";
        Object[] __DSPOT_args_63 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_62 = "8p#]q;a7/ez@l%MFZw!E";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_34 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_62, __DSPOT_args_63);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_34.addJavadoc(__DSPOT_format_206, __DSPOT_args_207);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd763 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd763_sd1878() {
        Modifier[] __DSPOT_modifiers_208 = new Modifier[0];
        Object[] __DSPOT_args_63 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_62 = "8p#]q;a7/ez@l%MFZw!E";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_34 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_62, __DSPOT_args_63);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_34.addModifiers(__DSPOT_modifiers_208);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd763 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd763_sd1880() {
        Object[] __DSPOT_args_210 = new Object[0];
        String __DSPOT_format_209 = "^cUT#UgA#<? kg{GqMXy";
        Object[] __DSPOT_args_63 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_62 = "8p#]q;a7/ez@l%MFZw!E";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_34 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_62, __DSPOT_args_63);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_34.initializer(__DSPOT_format_209, __DSPOT_args_210);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd766 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd766_sd2015_failAssert4() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Object[] __DSPOT_args_234 = new Object[]{ new Object(), new Object() };
            String __DSPOT_format_233 = "@)l]!qOeddHWm8&1a$RH";
            Object[] __DSPOT_args_66 = new Object[]{ new Object() };
            String __DSPOT_format_65 = "iCMs-NCPSNsen+,yJLZT";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            b.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
            a.toString();
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_65, __DSPOT_args_66);
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.addJavadoc(__DSPOT_format_233, __DSPOT_args_234);
            org.junit.Assert.fail("equalsAndHashCode_sd35_sd174_sd766_sd2015 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd766 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd766_sd2020_failAssert5() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Object[] __DSPOT_args_239 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
            String __DSPOT_format_238 = "rq<W]vnwH5v=i}`yj$kC";
            Object[] __DSPOT_args_66 = new Object[]{ new Object() };
            String __DSPOT_format_65 = "iCMs-NCPSNsen+,yJLZT";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            b.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
            a.toString();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_32 = // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_65, __DSPOT_args_66);
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_32.addJavadoc(__DSPOT_format_238, __DSPOT_args_239);
            org.junit.Assert.fail("equalsAndHashCode_sd35_sd174_sd766_sd2020 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd766 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd766_sd2021() {
        Modifier[] __DSPOT_modifiers_240 = new Modifier[]{  };
        Object[] __DSPOT_args_66 = new Object[]{ new Object() };
        String __DSPOT_format_65 = "iCMs-NCPSNsen+,yJLZT";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
        a.toString();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_32 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_65, __DSPOT_args_66);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_32.addModifiers(__DSPOT_modifiers_240);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd174__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd174_sd766 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd174_sd766_sd2023_failAssert6() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Object[] __DSPOT_args_242 = new Object[0];
            String __DSPOT_format_241 = ".Gl|?=xIp_Vly%}?c<+m";
            Object[] __DSPOT_args_66 = new Object[]{ new Object() };
            String __DSPOT_format_65 = "iCMs-NCPSNsen+,yJLZT";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            b.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd35_sd174__21 = // StatementAdd: add invocation of a method
            a.toString();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_32 = // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_65, __DSPOT_args_66);
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_32.initializer(__DSPOT_format_241, __DSPOT_args_242);
            org.junit.Assert.fail("equalsAndHashCode_sd35_sd174_sd766_sd2023 should have thrown IllegalStateException");
        } catch (IllegalStateException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd176() {
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35_sd176__21 = // StatementAdd: add invocation of a method
        b.hashCode();
        // AssertGenerator add assertion
        Assert.assertEquals(-1882877815, ((int) (o_equalsAndHashCode_sd35_sd176__21)));
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178() {
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd837 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd837_sd3850_failAssert13() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Object[] __DSPOT_args_586 = new Object[]{ new Object() };
            String __DSPOT_format_585 = "[c&=M-zk($_3?gQQmcN(";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            b.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
            b.toString();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35_sd178_sd837__25 = // StatementAdd: add invocation of a method
            a.hashCode();
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.addJavadoc(__DSPOT_format_585, __DSPOT_args_586);
            org.junit.Assert.fail("equalsAndHashCode_sd35_sd178_sd837_sd3850 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd837 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd837_sd3851() {
        Modifier[] __DSPOT_modifiers_587 = new Modifier[]{  };
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35_sd178_sd837__25 = // StatementAdd: add invocation of a method
        a.hashCode();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_587);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd837 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd837_sd3853() {
        Object[] __DSPOT_args_589 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
        String __DSPOT_format_588 = "-f?bfZ`LvH&)da1WGLR`";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35_sd178_sd837__25 = // StatementAdd: add invocation of a method
        a.hashCode();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_588, __DSPOT_args_589);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd839 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd839_sd3937() {
        Object[] __DSPOT_args_605 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
        String __DSPOT_format_604 = "I%e;*vSU*+J`<AWVcLF=";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178_sd839__25 = // StatementAdd: add invocation of a method
        a.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178_sd839__25);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_604, __DSPOT_args_605);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178_sd839__25);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd841 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd841_sd3978() {
        Object[] __DSPOT_args_612 = new Object[0];
        String __DSPOT_format_611 = "T-HU&;8&(VnC{ad1h#|]";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35_sd178_sd841__25 = // StatementAdd: add invocation of a method
        b.hashCode();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_611, __DSPOT_args_612);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd841 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd841_sd3979() {
        Modifier[] __DSPOT_modifiers_613 = new Modifier[]{  };
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35_sd178_sd841__25 = // StatementAdd: add invocation of a method
        b.hashCode();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_613);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd841 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd841_sd3981() {
        Object[] __DSPOT_args_615 = new Object[]{ new Object() };
        String __DSPOT_format_614 = "zqEL^YV4ObIgQC>asPSa";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35_sd178_sd841__25 = // StatementAdd: add invocation of a method
        b.hashCode();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_614, __DSPOT_args_615);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd843 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd843_sd4065() {
        Object[] __DSPOT_args_631 = new Object[]{ new Object(), new Object() };
        String __DSPOT_format_630 = "D(}2OM^+UG+r6^7J][{h";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178_sd843__25 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178_sd843__25);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_630, __DSPOT_args_631);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178_sd843__25);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd843 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd843_sd4066() {
        Modifier[] __DSPOT_modifiers_632 = new Modifier[]{  };
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178_sd843__25 = // StatementAdd: add invocation of a method
        b.toString();
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_632);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178_sd843__25);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd843 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd843_sd4068() {
        Object[] __DSPOT_args_634 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_633 = "ESL#^ETTSXDZQ<pTD<p_";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178_sd843__25 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178_sd843__25);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_633, __DSPOT_args_634);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178_sd843__25);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd845 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd845_sd2114() {
        Object[] __DSPOT_args_258 = new Object[0];
        String __DSPOT_format_257 = "kxyr;t]&]!vbNw8.:<e2";
        Object[] __DSPOT_args_77 = new Object[]{ new Object(), new Object() };
        String __DSPOT_format_76 = "f`y&R/x5,;cXLFumzTni";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
        b.toString();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_31 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_76, __DSPOT_args_77);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_31.addJavadoc(__DSPOT_format_257, __DSPOT_args_258);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd845 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd845_sd2117() {
        Object[] __DSPOT_args_261 = new Object[]{ new Object(), new Object(), new Object() };
        String __DSPOT_format_260 = "^cl&xZn2X00Sj5ra4M8l";
        Object[] __DSPOT_args_77 = new Object[]{ new Object(), new Object() };
        String __DSPOT_format_76 = "f`y&R/x5,;cXLFumzTni";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
        b.toString();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_31 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_76, __DSPOT_args_77);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_31.initializer(__DSPOT_format_260, __DSPOT_args_261);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd848 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd848_sd2257() {
        Object[] __DSPOT_args_285 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
        String __DSPOT_format_284 = "aL`9o.Tvur=+(`;+@S(B";
        Object[] __DSPOT_args_80 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
        String __DSPOT_format_79 = "OiR]O2;851PO!@hT=0]>";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_79, __DSPOT_args_80);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addJavadoc(__DSPOT_format_284, __DSPOT_args_285);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd848 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd848_sd2258() {
        Modifier[] __DSPOT_modifiers_286 = new Modifier[]{  };
        Object[] __DSPOT_args_80 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
        String __DSPOT_format_79 = "OiR]O2;851PO!@hT=0]>";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_79, __DSPOT_args_80);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.addModifiers(__DSPOT_modifiers_286);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd848 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd848_sd2260_failAssert7() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Object[] __DSPOT_args_288 = new Object[]{ new Object(), new Object() };
            String __DSPOT_format_287 = "4NJIgv]|Y&WO9NVR;Ao%";
            Object[] __DSPOT_args_80 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
            String __DSPOT_format_79 = "OiR]O2;851PO!@hT=0]>";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            b.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
            b.toString();
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_79, __DSPOT_args_80);
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_287, __DSPOT_args_288);
            org.junit.Assert.fail("equalsAndHashCode_sd35_sd178_sd848_sd2260 should have thrown IllegalStateException");
        } catch (IllegalStateException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd848 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd848_sd2262_failAssert8() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Object[] __DSPOT_args_290 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
            String __DSPOT_format_289 = "fvH[6v_$Ru5WDgX9`d38";
            Object[] __DSPOT_args_80 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
            String __DSPOT_format_79 = "OiR]O2;851PO!@hT=0]>";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            b.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
            b.toString();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_35 = // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_79, __DSPOT_args_80);
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_35.addJavadoc(__DSPOT_format_289, __DSPOT_args_290);
            org.junit.Assert.fail("equalsAndHashCode_sd35_sd178_sd848_sd2262 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd848 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd848_sd2263() {
        Modifier[] __DSPOT_modifiers_291 = new Modifier[]{  };
        Object[] __DSPOT_args_80 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
        String __DSPOT_format_79 = "OiR]O2;851PO!@hT=0]>";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
        b.toString();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_35 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_79, __DSPOT_args_80);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_35.addModifiers(__DSPOT_modifiers_291);
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd848 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd848_sd2264() {
        Object[] __DSPOT_args_80 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
        String __DSPOT_format_79 = "OiR]O2;851PO!@hT=0]>";
        FieldSpec a = FieldSpec.builder(int.class, "foo").build();
        FieldSpec b = FieldSpec.builder(int.class, "foo").build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__7 = b.hashCode();
        a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
        // AssertGenerator create local variable with return value of invocation
        int o_equalsAndHashCode_sd35__14 = b.hashCode();
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
        b.toBuilder();
        // AssertGenerator create local variable with return value of invocation
        String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
        b.toString();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
        // StatementAdd: generate variable from return value
        FieldSpec.Builder __DSPOT_invoc_35 = // StatementAdd: add invocation of a method
        __DSPOT_invoc_19.initializer(__DSPOT_format_79, __DSPOT_args_80);
        // StatementAdd: add invocation of a method
        __DSPOT_invoc_35.build();
        // AssertGenerator add assertion
        Assert.assertEquals("public static int FOO;\n", o_equalsAndHashCode_sd35_sd178__21);
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178 */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35_sd178_sd848 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35_sd178_sd848_sd2265_failAssert9() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            Object[] __DSPOT_args_293 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
            String __DSPOT_format_292 = "@1{ @{#Lk#? {%=kav)L";
            Object[] __DSPOT_args_80 = new Object[]{ new Object(), new Object(), new Object(), new Object() };
            String __DSPOT_format_79 = "OiR]O2;851PO!@hT=0]>";
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35__14 = b.hashCode();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_19 = // StatementAdd: add invocation of a method
            b.toBuilder();
            // AssertGenerator create local variable with return value of invocation
            String o_equalsAndHashCode_sd35_sd178__21 = // StatementAdd: add invocation of a method
            b.toString();
            // StatementAdd: generate variable from return value
            FieldSpec.Builder __DSPOT_invoc_35 = // StatementAdd: add invocation of a method
            __DSPOT_invoc_19.initializer(__DSPOT_format_79, __DSPOT_args_80);
            // StatementAdd: add invocation of a method
            __DSPOT_invoc_35.initializer(__DSPOT_format_292, __DSPOT_args_293);
            org.junit.Assert.fail("equalsAndHashCode_sd35_sd178_sd848_sd2265 should have thrown IllegalStateException");
        } catch (IllegalStateException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode_sd35 */
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd35litString169_failAssert22() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35__7 = b.hashCode();
            a = FieldSpec.builder(int.class, "`-k-a8(J8B", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            // AssertGenerator create local variable with return value of invocation
            int o_equalsAndHashCode_sd35__14 = b.hashCode();
            // StatementAdd: add invocation of a method
            b.toBuilder();
            org.junit.Assert.fail("equalsAndHashCode_sd35litString169 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    /* amplification of com.squareup.javapoet.FieldSpecTest#equalsAndHashCode */
    @Test(timeout = 10000)
    public void equalsAndHashCodelitString18_failAssert7() {
        // AssertGenerator generate try/catch block with fail statement
        try {
            FieldSpec a = FieldSpec.builder(int.class, "foo").build();
            FieldSpec b = FieldSpec.builder(int.class, "foo").build();
            b.hashCode();
            a = FieldSpec.builder(int.class, "F{OO", Modifier.PUBLIC, Modifier.STATIC).build();
            b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
            b.hashCode();
            org.junit.Assert.fail("equalsAndHashCodelitString18 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }
}


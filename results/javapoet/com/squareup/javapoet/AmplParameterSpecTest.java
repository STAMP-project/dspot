package com.squareup.javapoet;


import javax.lang.model.element.Modifier;
import org.junit.Assert;
import org.junit.Test;


public class AmplParameterSpecTest {
    @Test(timeout = 10000)
    public void equalsAndHashCode_sd30() throws Exception {
        ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        int o_equalsAndHashCode_sd30__15 = ParameterSpec.builder(int.class, "foo").build().hashCode();
        Assert.assertEquals((-130075578), ((int) (o_equalsAndHashCode_sd30__15)));
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
    public void equalsAndHashCodelitString23_failAssert10() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "[|+mr6#-Vt").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            Assert.fail("equalsAndHashCodelitString23 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28() throws Exception {
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        Assert.assertEquals("static int i", ParameterSpec.builder(int.class, "foo").build().toString());
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28_sd153() throws Exception {
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd28__15 = ParameterSpec.builder(int.class, "foo").build().toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
        Assert.assertEquals("static int i", ParameterSpec.builder(int.class, "foo").build().toString());
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd26litString106_failAssert7() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "`").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            int o_equalsAndHashCode_sd26__15 = ParameterSpec.builder(int.class, "foo").build().hashCode();
            Assert.fail("equalsAndHashCode_sd26litString106 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd26litString102_failAssert9() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, " TM1`_8;0L").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            int o_equalsAndHashCode_sd26__15 = ParameterSpec.builder(int.class, " TM1`_8;0L").build().hashCode();
            Assert.fail("equalsAndHashCode_sd26litString102 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd30_sd179() throws Exception {
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        int o_equalsAndHashCode_sd30__15 = ParameterSpec.builder(int.class, "foo").build().hashCode();
        int o_equalsAndHashCode_sd30_sd179__19 = ParameterSpec.builder(int.class, "foo").build().hashCode();
        Assert.assertEquals((-130075578), ((int) (o_equalsAndHashCode_sd30_sd179__19)));
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32_sd212() throws Exception {
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd32__15 = ParameterSpec.builder(int.class, "foo").build().toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
        ParameterSpec.builder(int.class, "foo").build().toBuilder();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28_sd153litString1911() throws Exception {
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd28__15 = ParameterSpec.builder(int.class, "$oo").build().toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
        Assert.assertEquals("static int i", ParameterSpec.builder(int.class, "foo").build().toString());
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32_sd213_sd1875() throws Exception {
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd32__15 = b.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
        String o_equalsAndHashCode_sd32_sd213__19 = ParameterSpec.builder(int.class, "foo").build().toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32_sd213__19);
        b.toBuilder();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32_sd213__19);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32_sd213_sd1873() throws Exception {
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd32__15 = b.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
        String o_equalsAndHashCode_sd32_sd213__19 = ParameterSpec.builder(int.class, "foo").build().toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32_sd213__19);
        boolean o_equalsAndHashCode_sd32_sd213_sd1873__25 = b.equals(new Object());
        Assert.assertFalse(o_equalsAndHashCode_sd32_sd213_sd1873__25);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32_sd213__19);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32litString186litString1367_failAssert42() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            String o_equalsAndHashCode_sd32__15 = ParameterSpec.builder(int.class, "foo").build().toString();
            Assert.fail("equalsAndHashCode_sd32litString186litString1367 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32_sd216_sd2041() throws Exception {
        ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd32__15 = b.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
        b.toBuilder().addModifiers(new Modifier[]{  });
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28litString126litString1466_failAssert35() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "fo").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "").build();
            a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            String o_equalsAndHashCode_sd28__15 = ParameterSpec.builder(int.class, "fo").build().toString();
            Assert.fail("equalsAndHashCode_sd28litString126litString1466 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32_sd217_sd1902() throws Exception {
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd32__15 = b.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
        String o_equalsAndHashCode_sd32_sd217__19 = b.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32_sd217__19);
        int o_equalsAndHashCode_sd32_sd217_sd1902__23 = ParameterSpec.builder(int.class, "foo").build().hashCode();
        Assert.assertEquals((-130075578), ((int) (o_equalsAndHashCode_sd32_sd217_sd1902__23)));
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32_sd217__19);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28_sd153_sd1936litString4630() throws Exception {
        ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "foo").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd28__15 = a.toString();
        Assert.assertEquals("static int foo", o_equalsAndHashCode_sd28__15);
        String o_equalsAndHashCode_sd28_sd153__19 = ParameterSpec.builder(int.class, "foo").build().toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153__19);
        Assert.assertEquals("static int foo", a.toString());
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153__19);
        Assert.assertEquals("static int foo", o_equalsAndHashCode_sd28__15);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28_sd153_sd1940_sd4674() throws Exception {
        ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd28__15 = a.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
        String o_equalsAndHashCode_sd28_sd153__19 = b.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153__19);
        String o_equalsAndHashCode_sd28_sd153_sd1940__23 = b.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153_sd1940__23);
        a.toBuilder();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153__19);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153_sd1940__23);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32_sd213_sd1876litString4766_failAssert49() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "Xbq-We!$8H").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            String o_equalsAndHashCode_sd32__15 = b.toString();
            String o_equalsAndHashCode_sd32_sd213__19 = ParameterSpec.builder(int.class, "foo").build().toString();
            String o_equalsAndHashCode_sd32_sd213_sd1876__23 = b.toString();
            Assert.fail("equalsAndHashCode_sd32_sd213_sd1876litString4766 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32_sd217_sd1902_sd4992() throws Exception {
        ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd32__15 = b.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
        String o_equalsAndHashCode_sd32_sd217__19 = b.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32_sd217__19);
        int o_equalsAndHashCode_sd32_sd217_sd1902__23 = a.hashCode();
        boolean o_equalsAndHashCode_sd32_sd217_sd1902_sd4992__29 = a.equals(new Object());
        Assert.assertFalse(o_equalsAndHashCode_sd32_sd217_sd1902_sd4992__29);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32_sd217__19);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32_sd213_sd1873litString4185_failAssert7() throws Exception {
        try {
            Object __DSPOT_o_79 = new Object();
            ParameterSpec a = ParameterSpec.builder(int.class, ",oo").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            String o_equalsAndHashCode_sd32__15 = b.toString();
            String o_equalsAndHashCode_sd32_sd213__19 = ParameterSpec.builder(int.class, ",oo").build().toString();
            boolean o_equalsAndHashCode_sd32_sd213_sd1873__25 = b.equals(new Object());
            Assert.fail("equalsAndHashCode_sd32_sd213_sd1873litString4185 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32_sd212_sd2006_sd4095() throws Exception {
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd32__15 = ParameterSpec.builder(int.class, "foo").build().toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
        ParameterSpec.builder(int.class, "foo").build().toBuilder().addModifiers(new Modifier[]{  }).addModifiers(new Modifier[0]);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28_sd153_sd1940_sd4677() throws Exception {
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd28__15 = ParameterSpec.builder(int.class, "foo").build().toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
        String o_equalsAndHashCode_sd28_sd153__19 = b.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153__19);
        String o_equalsAndHashCode_sd28_sd153_sd1940__23 = b.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153_sd1940__23);
        int o_equalsAndHashCode_sd28_sd153_sd1940_sd4677__27 = b.hashCode();
        Assert.assertEquals((-130075578), ((int) (o_equalsAndHashCode_sd28_sd153_sd1940_sd4677__27)));
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153__19);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153_sd1940__23);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28_sd153_sd1936_sd4641_sd12848() throws Exception {
        ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd28__15 = a.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
        String o_equalsAndHashCode_sd28_sd153__19 = ParameterSpec.builder(int.class, "foo").build().toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153__19);
        String o_equalsAndHashCode_sd28_sd153_sd1936__23 = a.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153_sd1936__23);
        int o_equalsAndHashCode_sd28_sd153_sd1936_sd4641__27 = a.hashCode();
        boolean o_equalsAndHashCode_sd28_sd153_sd1936_sd4641_sd12848__33 = a.equals(new Object());
        Assert.assertFalse(o_equalsAndHashCode_sd28_sd153_sd1936_sd4641_sd12848__33);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153_sd1936__23);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153__19);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28_sd153_sd1936_sd4640_sd10888() throws Exception {
        ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd28__15 = a.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
        String o_equalsAndHashCode_sd28_sd153__19 = b.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153__19);
        String o_equalsAndHashCode_sd28_sd153_sd1936__23 = a.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153_sd1936__23);
        boolean o_equalsAndHashCode_sd28_sd153_sd1936_sd4640__29 = a.equals(new Object());
        int o_equalsAndHashCode_sd28_sd153_sd1936_sd4640_sd10888__33 = b.hashCode();
        Assert.assertEquals((-130075578), ((int) (o_equalsAndHashCode_sd28_sd153_sd1936_sd4640_sd10888__33)));
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153_sd1936__23);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153__19);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32_sd212_sd2006_sd4095_sd11090() throws Exception {
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd32__15 = ParameterSpec.builder(int.class, "foo").build().toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
        ParameterSpec.builder(int.class, "foo").build().toBuilder().addModifiers(new Modifier[]{  }).addModifiers(new Modifier[0]).addModifiers(new Modifier[]{  });
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28_sd153_sd1936_sd4640litString10865_failAssert32() throws Exception {
        try {
            Object __DSPOT_o_234 = new Object();
            ParameterSpec a = ParameterSpec.builder(int.class, "").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            String o_equalsAndHashCode_sd28__15 = a.toString();
            String o_equalsAndHashCode_sd28_sd153__19 = ParameterSpec.builder(int.class, "foo").build().toString();
            String o_equalsAndHashCode_sd28_sd153_sd1936__23 = a.toString();
            boolean o_equalsAndHashCode_sd28_sd153_sd1936_sd4640__29 = a.equals(new Object());
            Assert.fail("equalsAndHashCode_sd28_sd153_sd1936_sd4640litString10865 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28_sd149_sd1972_sd4615litString12155_failAssert36() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "annotationSpecs == null").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            String o_equalsAndHashCode_sd28__15 = a.toString();
            String o_equalsAndHashCode_sd28_sd149__19 = a.toString();
            String o_equalsAndHashCode_sd28_sd149_sd1972__23 = b.toString();
            String o_equalsAndHashCode_sd28_sd149_sd1972_sd4615__27 = b.toString();
            Assert.fail("equalsAndHashCode_sd28_sd149_sd1972_sd4615litString12155 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28_sd149_sd1972_sd4609_sd12564() throws Exception {
        ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd28__15 = a.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
        String o_equalsAndHashCode_sd28_sd149__19 = a.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd149__19);
        String o_equalsAndHashCode_sd28_sd149_sd1972__23 = b.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd149_sd1972__23);
        int o_equalsAndHashCode_sd28_sd149_sd1972_sd4609__27 = a.hashCode();
        boolean o_equalsAndHashCode_sd28_sd149_sd1972_sd4609_sd12564__33 = b.equals(new Object());
        Assert.assertFalse(o_equalsAndHashCode_sd28_sd149_sd1972_sd4609_sd12564__33);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd149_sd1972__23);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd149__19);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28_sd151() throws Exception {
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd28__15 = ParameterSpec.builder(int.class, "foo").build().toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
        int o_equalsAndHashCode_sd28_sd151__19 = ParameterSpec.builder(int.class, "foo").build().hashCode();
        Assert.assertEquals((-130075578), ((int) (o_equalsAndHashCode_sd28_sd151__19)));
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28_sd153_sd1936_sd4640litString10874_failAssert1() throws Exception {
        try {
            Object __DSPOT_o_234 = new Object();
            ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "annotationSpecs == null").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            String o_equalsAndHashCode_sd28__15 = a.toString();
            String o_equalsAndHashCode_sd28_sd153__19 = ParameterSpec.builder(int.class, "foo").build().toString();
            String o_equalsAndHashCode_sd28_sd153_sd1936__23 = a.toString();
            boolean o_equalsAndHashCode_sd28_sd153_sd1936_sd4640__29 = a.equals(new Object());
            Assert.fail("equalsAndHashCode_sd28_sd153_sd1936_sd4640litString10874 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28_sd153_sd1940_sd4672_sd10920() throws Exception {
        ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd28__15 = a.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
        String o_equalsAndHashCode_sd28_sd153__19 = b.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153__19);
        String o_equalsAndHashCode_sd28_sd153_sd1940__23 = b.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153_sd1940__23);
        boolean o_equalsAndHashCode_sd28_sd153_sd1940_sd4672__29 = a.equals(new Object());
        int o_equalsAndHashCode_sd28_sd153_sd1940_sd4672_sd10920__33 = b.hashCode();
        Assert.assertEquals((-130075578), ((int) (o_equalsAndHashCode_sd28_sd153_sd1940_sd4672_sd10920__33)));
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153__19);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28_sd153_sd1940__23);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28_sd153_sd1940_sd4672litString10897_failAssert33() throws Exception {
        try {
            Object __DSPOT_o_236 = new Object();
            ParameterSpec a = ParameterSpec.builder(int.class, "").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            String o_equalsAndHashCode_sd28__15 = a.toString();
            String o_equalsAndHashCode_sd28_sd153__19 = b.toString();
            String o_equalsAndHashCode_sd28_sd153_sd1940__23 = b.toString();
            boolean o_equalsAndHashCode_sd28_sd153_sd1940_sd4672__29 = a.equals(new Object());
            Assert.fail("equalsAndHashCode_sd28_sd153_sd1940_sd4672litString10897 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28litString125_failAssert24() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "f!oo").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            String o_equalsAndHashCode_sd28__15 = ParameterSpec.builder(int.class, "f!oo").build().toString();
            Assert.fail("equalsAndHashCode_sd28litString125 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28litString138litString1620() throws Exception {
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "d").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        Assert.assertEquals("static int d", ParameterSpec.builder(int.class, "fo").build().toString());
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28litString139_failAssert37() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "wpauR%h1,x").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            String o_equalsAndHashCode_sd28__15 = ParameterSpec.builder(int.class, "foo").build().toString();
            Assert.fail("equalsAndHashCode_sd28litString139 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32_sd213_sd1870_sd5060() throws Exception {
        ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd32__15 = b.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
        String o_equalsAndHashCode_sd32_sd213__19 = a.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32_sd213__19);
        int o_equalsAndHashCode_sd32_sd213_sd1870__23 = a.hashCode();
        boolean o_equalsAndHashCode_sd32_sd213_sd1870_sd5060__29 = b.equals(new Object());
        Assert.assertFalse(o_equalsAndHashCode_sd32_sd213_sd1870_sd5060__29);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32_sd213__19);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32_sd213_sd1870litString5033_failAssert65() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "annotationSpecs == null").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            String o_equalsAndHashCode_sd32__15 = ParameterSpec.builder(int.class, "foo").build().toString();
            String o_equalsAndHashCode_sd32_sd213__19 = a.toString();
            int o_equalsAndHashCode_sd32_sd213_sd1870__23 = a.hashCode();
            Assert.fail("equalsAndHashCode_sd32_sd213_sd1870litString5033 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32_sd213_sd1872litString4793_failAssert57() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "#7Z;#Ib/>Z").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            String o_equalsAndHashCode_sd32__15 = ParameterSpec.builder(int.class, "foo").build().toString();
            String o_equalsAndHashCode_sd32_sd213__19 = a.toString();
            String o_equalsAndHashCode_sd32_sd213_sd1872__23 = a.toString();
            Assert.fail("equalsAndHashCode_sd32_sd213_sd1872litString4793 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32_sd213_sd1873_sd4201() throws Exception {
        ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd32__15 = b.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
        String o_equalsAndHashCode_sd32_sd213__19 = a.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32_sd213__19);
        boolean o_equalsAndHashCode_sd32_sd213_sd1873__25 = b.equals(new Object());
        int o_equalsAndHashCode_sd32_sd213_sd1873_sd4201__29 = a.hashCode();
        Assert.assertEquals((-130075578), ((int) (o_equalsAndHashCode_sd32_sd213_sd1873_sd4201__29)));
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32_sd213__19);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32_sd216() throws Exception {
        ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd32__15 = b.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
        b.toBuilder();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32_sd217litString1886_failAssert85() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "-oo").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            String o_equalsAndHashCode_sd32__15 = b.toString();
            String o_equalsAndHashCode_sd32_sd217__19 = b.toString();
            Assert.fail("equalsAndHashCode_sd32_sd217litString1886 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32litString186() throws Exception {
        ParameterSpec a = ParameterSpec.builder(int.class, "i").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        Assert.assertEquals("static int i", ParameterSpec.builder(int.class, "foo").build().toString());
    }

    @Test(timeout = 10000)
    public void equalsAndHashCodelitString16_failAssert6() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "annotationSpecs == null").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            Assert.fail("equalsAndHashCodelitString16 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCodelitString3_failAssert1() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "*oo").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            Assert.fail("equalsAndHashCodelitString3 should have thrown IllegalArgumentException");
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
        Assert.assertEquals(1955995925, ((int) (o_equalsAndHashCode_add40__7)));
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_add41_sd474() throws Exception {
        int o_equalsAndHashCode_add41__7 = ParameterSpec.builder(int.class, "foo").build().hashCode();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        boolean o_equalsAndHashCode_add41_sd474__20 = ParameterSpec.builder(int.class, "foo").build().equals(new Object());
        Assert.assertFalse(o_equalsAndHashCode_add41_sd474__20);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_add49() throws Exception {
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        boolean o_equalsAndHashCode_add49__15 = ParameterSpec.builder(int.class, "foo").build().equals(ParameterSpec.builder(int.class, "foo").build());
        Assert.assertTrue(o_equalsAndHashCode_add49__15);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_add49_add535() throws Exception {
        ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        boolean o_equalsAndHashCode_add49_add535__15 = a.equals(b);
        Assert.assertTrue(o_equalsAndHashCode_add49_add535__15);
        boolean o_equalsAndHashCode_add49__15 = a.equals(b);
        Assert.assertTrue(o_equalsAndHashCode_add49_add535__15);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_add49_sd524() throws Exception {
        ParameterSpec b;
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        boolean o_equalsAndHashCode_add49__15 = ParameterSpec.builder(int.class, "foo").build().equals(b);
        Assert.assertEquals("static int i", b.toString());
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_add49litString499_failAssert0() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            boolean o_equalsAndHashCode_add49__15 = ParameterSpec.builder(int.class, "").build().equals(ParameterSpec.builder(int.class, "foo").build());
            org.junit.Assert.fail("equalsAndHashCode_add49litString499 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_add49litString515_failAssert8() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "M-k,I]-r8/").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            boolean o_equalsAndHashCode_add49__15 = ParameterSpec.builder(int.class, "foo").build().equals(ParameterSpec.builder(int.class, "foo").build());
            org.junit.Assert.fail("equalsAndHashCode_add49litString515 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28_sd1758() throws Exception {
        ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
        ParameterSpec b;
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd28__15 = a.toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
        int o_equalsAndHashCode_sd28_sd1758__17 = a.hashCode();
        Assert.assertEquals(-130075578, ((int) (o_equalsAndHashCode_sd28_sd1758__17)));
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28_sd1761() throws Exception {
        ParameterSpec b;
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd28__15 = ParameterSpec.builder(int.class, "foo").build().toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
        boolean o_equalsAndHashCode_sd28_sd1761__19 = b.equals(new Object());
        Assert.assertFalse(o_equalsAndHashCode_sd28_sd1761__19);
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd28__15);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28litString1710() throws Exception {
        ParameterSpec a;
        ParameterSpec b = ParameterSpec.builder(int.class, "fo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        Assert.assertEquals("static int i", a.toString());
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd28litString1741_failAssert26() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "annotationSpecs == null").build();
            ParameterSpec b;
            a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            String o_equalsAndHashCode_sd28__15 = ParameterSpec.builder(int.class, "annotationSpecs == null").build().toString();
            org.junit.Assert.fail("equalsAndHashCode_sd28litString1741 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd30_sd307() throws Exception {
        ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        int o_equalsAndHashCode_sd30__15 = b.hashCode();
        int o_equalsAndHashCode_sd30_sd307__19 = b.hashCode();
        Assert.assertEquals(-130075578, ((int) (o_equalsAndHashCode_sd30_sd307__19)));
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32() throws Exception {
        ParameterSpec a;
        ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        Assert.assertEquals("u&sdcOgKS{qxxjff`y&R", "u&sdcOgKS{qxxjff`y&R");
    }

    @Test(timeout = 10000)
    public void equalsAndHashCode_sd32_sd1657() throws Exception {
        ParameterSpec a;
        a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
        String o_equalsAndHashCode_sd32__15 = ParameterSpec.builder(int.class, "foo").build().toString();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
        a.toBuilder();
        Assert.assertEquals("static int i", o_equalsAndHashCode_sd32__15);
    }

    @Test(timeout = 10000)
    public void equalsAndHashCodelitString13_failAssert5() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, ",y(q2 5[gp").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            org.junit.Assert.fail("equalsAndHashCodelitString13 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void equalsAndHashCodelitString19_failAssert8() throws Exception {
        try {
            ParameterSpec a = ParameterSpec.builder(int.class, "foo").build();
            ParameterSpec b = ParameterSpec.builder(int.class, "foo").build();
            a = ParameterSpec.builder(int.class, "").addModifiers(Modifier.STATIC).build();
            b = ParameterSpec.builder(int.class, "i").addModifiers(Modifier.STATIC).build();
            org.junit.Assert.fail("equalsAndHashCodelitString19 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException eee) {
        }
    }

    @Test(timeout = 10000)
    public void nullAnnotationsAddition_add4236() throws Exception {
        try {
            ParameterSpec.builder(int.class, "foo").addAnnotations(null);
        } catch (Exception e) {
            String o_nullAnnotationsAddition_add4236__6 = e.getMessage();
            Assert.assertEquals("annotationSpecs == null", e.getMessage());
        }
    }

    @Test(timeout = 10000)
    public void nullAnnotationsAddition_add4236_add4307() throws Exception {
        try {
            ParameterSpec.builder(int.class, "foo").addAnnotations(null);
            ParameterSpec.builder(int.class, "foo").addAnnotations(null);
        } catch (Exception e) {
            String o_nullAnnotationsAddition_add4236__6 = e.getMessage();
            Assert.assertEquals("annotationSpecs == null", e.getMessage());
        }
    }

    @Test(timeout = 10000)
    public void nullAnnotationsAddition_add4236_add4308litString4522() throws Exception {
        try {
            ParameterSpec.builder(int.class, "fo");
            ParameterSpec.builder(int.class, "foo").addAnnotations(null);
        } catch (Exception e) {
            String o_nullAnnotationsAddition_add4236__6 = e.getMessage();
            Assert.assertEquals("annotationSpecs == null", e.getMessage());
        }
    }

    @Test(timeout = 10000)
    public void nullAnnotationsAddition_add4236_sd4305litString4460() throws Exception {
        try {
            Modifier[] __DSPOT_modifiers_139 = new Modifier[]{  };
            ParameterSpec.Builder __DSPOT_invoc_3 = ParameterSpec.builder(int.class, "annotationSpecs == null").addAnnotations(null);
            ParameterSpec.builder(int.class, "annotationSpecs == null").addAnnotations(null).addModifiers(new Modifier[]{  });
        } catch (Exception e) {
            String o_nullAnnotationsAddition_add4236__6 = e.getMessage();
            Assert.assertEquals("not a valid name: annotationSpecs == null", e.getMessage());
        }
    }

    @Test(timeout = 10000)
    public void nullAnnotationsAddition_add4236litString4298() throws Exception {
        try {
            ParameterSpec.builder(int.class, "annotationSpecs == null").addAnnotations(null);
        } catch (Exception e) {
            String o_nullAnnotationsAddition_add4236__6 = e.getMessage();
            Assert.assertEquals("not a valid name: annotationSpecs == null", e.getMessage());
        }
    }
}


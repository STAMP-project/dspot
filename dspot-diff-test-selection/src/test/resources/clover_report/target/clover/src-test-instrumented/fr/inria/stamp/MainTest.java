/* $$ This file has been instrumented by Clover 4.4.1#2019101123313948 $$ */package fr.inria.stamp;

import fr.inria.stamp.tavern.Item;
import fr.inria.stamp.tavern.Player;
import fr.inria.stamp.tavern.Seller;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/09/17
 */
public class MainTest {static class __CLR4_4_11e1ekiu2vjxo{public static com_atlassian_clover.CoverageRecorder R;public static com_atlassian_clover.CloverProfile[] profiles = { };@java.lang.SuppressWarnings("unchecked") public static <I, T extends I> I lambdaInc(final int i,final T l,final int si){java.lang.reflect.InvocationHandler h=new java.lang.reflect.InvocationHandler(){public java.lang.Object invoke(java.lang.Object p,java.lang.reflect.Method m,java.lang.Object[] a) throws Throwable{R.inc(i);R.inc(si);try{return m.invoke(l,a);}catch(java.lang.reflect.InvocationTargetException e){throw e.getCause()!=null?e.getCause():new RuntimeException("Clover failed to invoke instrumented lambda",e);}}};return (I)java.lang.reflect.Proxy.newProxyInstance(l.getClass().getClassLoader(),l.getClass().getInterfaces(),h);}static{com_atlassian_clover.CoverageRecorder _R=null;try{com_atlassian_clover.CloverVersionInfo.An_old_version_of_clover_is_on_your_compilation_classpath___Please_remove___Required_version_is___4_4_1();if(2019101123313948L!=com_atlassian_clover.CloverVersionInfo.getBuildStamp()){com_atlassian_clover.Clover.l("[CLOVER] WARNING: The Clover version used in instrumentation does not match the runtime version. You need to run instrumented classes against the same version of Clover that you instrumented with.");com_atlassian_clover.Clover.l("[CLOVER] WARNING: Instr=4.4.1#2019101123313948,Runtime="+com_atlassian_clover.CloverVersionInfo.getReleaseNum()+"#"+com_atlassian_clover.CloverVersionInfo.getBuildStamp());}R=com_atlassian_clover.Clover.getNullRecorder();_R=com_atlassian_clover.Clover.getNullRecorder();_R=com_atlassian_clover.Clover.getRecorder("\u002f\u0068\u006f\u006d\u0065\u002f\u0062\u0065\u006e\u006a\u0061\u006d\u0069\u006e\u002f\u0077\u006f\u0072\u006b\u0073\u0070\u0061\u0063\u0065\u002f\u0064\u0073\u0070\u006f\u0074\u002f\u0064\u0073\u0070\u006f\u0074\u002d\u0064\u0069\u0066\u0066\u002d\u0074\u0065\u0073\u0074\u002d\u0073\u0065\u006c\u0065\u0063\u0074\u0069\u006f\u006e\u002f\u0073\u0072\u0063\u002f\u0074\u0065\u0073\u0074\u002f\u0072\u0065\u0073\u006f\u0075\u0072\u0063\u0065\u0073\u002f\u0074\u0061\u0076\u0065\u0072\u006e\u002f\u0074\u0061\u0072\u0067\u0065\u0074\u002f\u0063\u006c\u006f\u0076\u0065\u0072\u002f\u0063\u006c\u006f\u0076\u0065\u0072\u002e\u0064\u0062",1608284180637L,8589935092L,58,profiles,new java.lang.String[]{"clover.distributed.coverage",null});}catch(java.lang.SecurityException e){java.lang.System.err.println("[CLOVER] FATAL ERROR: Clover could not be initialised because it has insufficient security privileges. Please consult the Clover documentation on the security policy file changes required. ("+e.getClass()+":"+e.getMessage()+")");}catch(java.lang.NoClassDefFoundError e){java.lang.System.err.println("[CLOVER] FATAL ERROR: Clover could not be initialised. Are you sure you have Clover in the runtime classpath? ("+e.getClass()+":"+e.getMessage()+")");}catch(java.lang.Throwable t){java.lang.System.err.println("[CLOVER] FATAL ERROR: Clover could not be initialised because of an unexpected error. ("+t.getClass()+":"+t.getMessage()+")");}R=_R;}}public static final com_atlassian_clover.TestNameSniffer __CLR4_4_1_TEST_NAME_SNIFFER=com_atlassian_clover.TestNameSniffer.NULL_INSTANCE;

	@Test
	public void test() throws Exception {__CLR4_4_11e1ekiu2vjxo.R.globalSliceStart(getClass().getName(),50);int $CLV_p$=0;java.lang.Throwable $CLV_t$=null;try{__CLR4_4_1zi4lt31e();$CLV_p$=1;}catch(java.lang.Throwable $CLV_t2$){if($CLV_p$==0&&$CLV_t$==null){$CLV_t$=$CLV_t2$;}__CLR4_4_11e1ekiu2vjxo.R.rethrow($CLV_t2$);}finally{__CLR4_4_11e1ekiu2vjxo.R.globalSliceEnd(getClass().getName(),"fr.inria.stamp.MainTest.test",__CLR4_4_1_TEST_NAME_SNIFFER.getTestName(),50,$CLV_p$,$CLV_t$);}}private void  __CLR4_4_1zi4lt31e() throws Exception{try{__CLR4_4_11e1ekiu2vjxo.R.inc(50);
		__CLR4_4_11e1ekiu2vjxo.R.inc(51);Seller seller = new Seller(100, Collections.singletonList(new Item("Potion", 5)));
		__CLR4_4_11e1ekiu2vjxo.R.inc(52);Player player = new Player("Timoleon", 1000);

		__CLR4_4_11e1ekiu2vjxo.R.inc(53);assertEquals("Player{gold=1000, items=[]}", player.toString());
		__CLR4_4_11e1ekiu2vjxo.R.inc(54);assertEquals("Seller{gold=100, items=[Potion]}", seller.toString());

		__CLR4_4_11e1ekiu2vjxo.R.inc(55);player.buyItem("Potion", seller);

		__CLR4_4_11e1ekiu2vjxo.R.inc(56);assertEquals("Player{gold=995, items=[Potion]}", player.toString());
		__CLR4_4_11e1ekiu2vjxo.R.inc(57);assertEquals("Seller{gold=105, items=[Potion]}", seller.toString());
	}finally{__CLR4_4_11e1ekiu2vjxo.R.flushNeeded();}}

}

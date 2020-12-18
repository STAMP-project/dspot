/* $$ This file has been instrumented by Clover 4.4.1#2019101123313948 $$ */package fr.inria.stamp.tavern;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/09/17
 */
public class Player {public static class __CLR4_4_155kiu2vjsw{public static com_atlassian_clover.CoverageRecorder R;public static com_atlassian_clover.CloverProfile[] profiles = { };@java.lang.SuppressWarnings("unchecked") public static <I, T extends I> I lambdaInc(final int i,final T l,final int si){java.lang.reflect.InvocationHandler h=new java.lang.reflect.InvocationHandler(){public java.lang.Object invoke(java.lang.Object p,java.lang.reflect.Method m,java.lang.Object[] a) throws Throwable{R.inc(i);R.inc(si);try{return m.invoke(l,a);}catch(java.lang.reflect.InvocationTargetException e){throw e.getCause()!=null?e.getCause():new RuntimeException("Clover failed to invoke instrumented lambda",e);}}};return (I)java.lang.reflect.Proxy.newProxyInstance(l.getClass().getClassLoader(),l.getClass().getInterfaces(),h);}static{com_atlassian_clover.CoverageRecorder _R=null;try{com_atlassian_clover.CloverVersionInfo.An_old_version_of_clover_is_on_your_compilation_classpath___Please_remove___Required_version_is___4_4_1();if(2019101123313948L!=com_atlassian_clover.CloverVersionInfo.getBuildStamp()){com_atlassian_clover.Clover.l("[CLOVER] WARNING: The Clover version used in instrumentation does not match the runtime version. You need to run instrumented classes against the same version of Clover that you instrumented with.");com_atlassian_clover.Clover.l("[CLOVER] WARNING: Instr=4.4.1#2019101123313948,Runtime="+com_atlassian_clover.CloverVersionInfo.getReleaseNum()+"#"+com_atlassian_clover.CloverVersionInfo.getBuildStamp());}R=com_atlassian_clover.Clover.getNullRecorder();_R=com_atlassian_clover.Clover.getNullRecorder();_R=com_atlassian_clover.Clover.getRecorder("\u002f\u0068\u006f\u006d\u0065\u002f\u0062\u0065\u006e\u006a\u0061\u006d\u0069\u006e\u002f\u0077\u006f\u0072\u006b\u0073\u0070\u0061\u0063\u0065\u002f\u0064\u0073\u0070\u006f\u0074\u002f\u0064\u0073\u0070\u006f\u0074\u002d\u0064\u0069\u0066\u0066\u002d\u0074\u0065\u0073\u0074\u002d\u0073\u0065\u006c\u0065\u0063\u0074\u0069\u006f\u006e\u002f\u0073\u0072\u0063\u002f\u0074\u0065\u0073\u0074\u002f\u0072\u0065\u0073\u006f\u0075\u0072\u0063\u0065\u0073\u002f\u0074\u0061\u0076\u0065\u0072\u006e\u002f\u0074\u0061\u0072\u0067\u0065\u0074\u002f\u0063\u006c\u006f\u0076\u0065\u0072\u002f\u0063\u006c\u006f\u0076\u0065\u0072\u002e\u0064\u0062",1608284180361L,8589935092L,23,profiles,new java.lang.String[]{"clover.distributed.coverage",null});}catch(java.lang.SecurityException e){java.lang.System.err.println("[CLOVER] FATAL ERROR: Clover could not be initialised because it has insufficient security privileges. Please consult the Clover documentation on the security policy file changes required. ("+e.getClass()+":"+e.getMessage()+")");}catch(java.lang.NoClassDefFoundError e){java.lang.System.err.println("[CLOVER] FATAL ERROR: Clover could not be initialised. Are you sure you have Clover in the runtime classpath? ("+e.getClass()+":"+e.getMessage()+")");}catch(java.lang.Throwable t){java.lang.System.err.println("[CLOVER] FATAL ERROR: Clover could not be initialised because of an unexpected error. ("+t.getClass()+":"+t.getMessage()+")");}R=_R;}}public static final com_atlassian_clover.TestNameSniffer __CLR4_4_1_TEST_NAME_SNIFFER=com_atlassian_clover.TestNameSniffer.NULL_INSTANCE;

	private int gold;
	private List<Item> items;
	private String name;

	public Player(String name, int gold) {try{__CLR4_4_155kiu2vjsw.R.inc(5);
		__CLR4_4_155kiu2vjsw.R.inc(6);this.name = name;
		__CLR4_4_155kiu2vjsw.R.inc(7);this.gold = gold;
		__CLR4_4_155kiu2vjsw.R.inc(8);this.items = new ArrayList<>();
	}finally{__CLR4_4_155kiu2vjsw.R.flushNeeded();}}

	public String getName() {try{__CLR4_4_155kiu2vjsw.R.inc(9);
		__CLR4_4_155kiu2vjsw.R.inc(10);return this.name;
	}finally{__CLR4_4_155kiu2vjsw.R.flushNeeded();}}

	public int getGold() {try{__CLR4_4_155kiu2vjsw.R.inc(11);
		__CLR4_4_155kiu2vjsw.R.inc(12);return gold;
	}finally{__CLR4_4_155kiu2vjsw.R.flushNeeded();}}

	public void giveGold(int amount) {try{__CLR4_4_155kiu2vjsw.R.inc(13);
		__CLR4_4_155kiu2vjsw.R.inc(14);this.gold = this.getGold() - amount;
	}finally{__CLR4_4_155kiu2vjsw.R.flushNeeded();}}

	public void buyItem(String name, Seller seller) {try{__CLR4_4_155kiu2vjsw.R.inc(15);
		__CLR4_4_155kiu2vjsw.R.inc(16);final Item item = seller.sellItem(name, this);
		__CLR4_4_155kiu2vjsw.R.inc(17);if ((((item != null)&&(__CLR4_4_155kiu2vjsw.R.iget(18)!=0|true))||(__CLR4_4_155kiu2vjsw.R.iget(19)==0&false))) {{
			__CLR4_4_155kiu2vjsw.R.inc(20);this.items.add(item);
		}
	}}finally{__CLR4_4_155kiu2vjsw.R.flushNeeded();}}

	@Override
	public String toString() {try{__CLR4_4_155kiu2vjsw.R.inc(21);
		__CLR4_4_155kiu2vjsw.R.inc(22);return "Player{" +
				"gold=" + gold +
				", items=" + items +
				'}';
	}finally{__CLR4_4_155kiu2vjsw.R.flushNeeded();}}
}

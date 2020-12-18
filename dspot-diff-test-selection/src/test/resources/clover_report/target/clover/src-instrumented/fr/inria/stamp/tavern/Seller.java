/* $$ This file has been instrumented by Clover 4.4.1#2019101123313948 $$ */package fr.inria.stamp.tavern;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/09/17
 */
public class Seller {public static class __CLR4_4_1nnkiu2vjul{public static com_atlassian_clover.CoverageRecorder R;public static com_atlassian_clover.CloverProfile[] profiles = { };@java.lang.SuppressWarnings("unchecked") public static <I, T extends I> I lambdaInc(final int i,final T l,final int si){java.lang.reflect.InvocationHandler h=new java.lang.reflect.InvocationHandler(){public java.lang.Object invoke(java.lang.Object p,java.lang.reflect.Method m,java.lang.Object[] a) throws Throwable{R.inc(i);R.inc(si);try{return m.invoke(l,a);}catch(java.lang.reflect.InvocationTargetException e){throw e.getCause()!=null?e.getCause():new RuntimeException("Clover failed to invoke instrumented lambda",e);}}};return (I)java.lang.reflect.Proxy.newProxyInstance(l.getClass().getClassLoader(),l.getClass().getInterfaces(),h);}static{com_atlassian_clover.CoverageRecorder _R=null;try{com_atlassian_clover.CloverVersionInfo.An_old_version_of_clover_is_on_your_compilation_classpath___Please_remove___Required_version_is___4_4_1();if(2019101123313948L!=com_atlassian_clover.CloverVersionInfo.getBuildStamp()){com_atlassian_clover.Clover.l("[CLOVER] WARNING: The Clover version used in instrumentation does not match the runtime version. You need to run instrumented classes against the same version of Clover that you instrumented with.");com_atlassian_clover.Clover.l("[CLOVER] WARNING: Instr=4.4.1#2019101123313948,Runtime="+com_atlassian_clover.CloverVersionInfo.getReleaseNum()+"#"+com_atlassian_clover.CloverVersionInfo.getBuildStamp());}R=com_atlassian_clover.Clover.getNullRecorder();_R=com_atlassian_clover.Clover.getNullRecorder();_R=com_atlassian_clover.Clover.getRecorder("\u002f\u0068\u006f\u006d\u0065\u002f\u0062\u0065\u006e\u006a\u0061\u006d\u0069\u006e\u002f\u0077\u006f\u0072\u006b\u0073\u0070\u0061\u0063\u0065\u002f\u0064\u0073\u0070\u006f\u0074\u002f\u0064\u0073\u0070\u006f\u0074\u002d\u0064\u0069\u0066\u0066\u002d\u0074\u0065\u0073\u0074\u002d\u0073\u0065\u006c\u0065\u0063\u0074\u0069\u006f\u006e\u002f\u0073\u0072\u0063\u002f\u0074\u0065\u0073\u0074\u002f\u0072\u0065\u0073\u006f\u0075\u0072\u0063\u0065\u0073\u002f\u0074\u0061\u0076\u0065\u0072\u006e\u002f\u0074\u0061\u0072\u0067\u0065\u0074\u002f\u0063\u006c\u006f\u0076\u0065\u0072\u002f\u0063\u006c\u006f\u0076\u0065\u0072\u002e\u0064\u0062",1608284180361L,8589935092L,50,profiles,new java.lang.String[]{"clover.distributed.coverage",null});}catch(java.lang.SecurityException e){java.lang.System.err.println("[CLOVER] FATAL ERROR: Clover could not be initialised because it has insufficient security privileges. Please consult the Clover documentation on the security policy file changes required. ("+e.getClass()+":"+e.getMessage()+")");}catch(java.lang.NoClassDefFoundError e){java.lang.System.err.println("[CLOVER] FATAL ERROR: Clover could not be initialised. Are you sure you have Clover in the runtime classpath? ("+e.getClass()+":"+e.getMessage()+")");}catch(java.lang.Throwable t){java.lang.System.err.println("[CLOVER] FATAL ERROR: Clover could not be initialised because of an unexpected error. ("+t.getClass()+":"+t.getMessage()+")");}R=_R;}}public static final com_atlassian_clover.TestNameSniffer __CLR4_4_1_TEST_NAME_SNIFFER=com_atlassian_clover.TestNameSniffer.NULL_INSTANCE;

	private int gold;

	private List<Item> items;

	public Seller(int gold, List<Item> itemsToSell) {try{__CLR4_4_1nnkiu2vjul.R.inc(23);
		__CLR4_4_1nnkiu2vjul.R.inc(24);this.gold = gold;
		__CLR4_4_1nnkiu2vjul.R.inc(25);this.items = itemsToSell;
	}finally{__CLR4_4_1nnkiu2vjul.R.flushNeeded();}}

	public Item sellItem(String s, Player p) {try{__CLR4_4_1nnkiu2vjul.R.inc(26);
		__CLR4_4_1nnkiu2vjul.R.inc(27);Item i = null;
		__CLR4_4_1nnkiu2vjul.R.inc(28);for (int i2 = 0; (((i2 < this.items.size())&&(__CLR4_4_1nnkiu2vjul.R.iget(29)!=0|true))||(__CLR4_4_1nnkiu2vjul.R.iget(30)==0&false)); i2++) {{
			__CLR4_4_1nnkiu2vjul.R.inc(31);final Item i3 = this.items.get(i2);
			__CLR4_4_1nnkiu2vjul.R.inc(32);if ((((i3.name.equals(s))&&(__CLR4_4_1nnkiu2vjul.R.iget(33)!=0|true))||(__CLR4_4_1nnkiu2vjul.R.iget(34)==0&false))) {{
				__CLR4_4_1nnkiu2vjul.R.inc(35);i = i3;
			}
		}}
		}__CLR4_4_1nnkiu2vjul.R.inc(36);if ((((i != null)&&(__CLR4_4_1nnkiu2vjul.R.iget(37)!=0|true))||(__CLR4_4_1nnkiu2vjul.R.iget(38)==0&false))) {{
			__CLR4_4_1nnkiu2vjul.R.inc(39);final Integer g_p = p.getGold();
			__CLR4_4_1nnkiu2vjul.R.inc(40);final int value = g_p.compareTo(i.price);
			__CLR4_4_1nnkiu2vjul.R.inc(41);if ((((value >= 0)&&(__CLR4_4_1nnkiu2vjul.R.iget(42)!=0|true))||(__CLR4_4_1nnkiu2vjul.R.iget(43)==0&false))) {{
				__CLR4_4_1nnkiu2vjul.R.inc(44);this.gold = this.gold + i.price;
				__CLR4_4_1nnkiu2vjul.R.inc(45);p.giveGold(i.price);
				__CLR4_4_1nnkiu2vjul.R.inc(46);return i;
			}
		}}
		}__CLR4_4_1nnkiu2vjul.R.inc(47);return null;
	}finally{__CLR4_4_1nnkiu2vjul.R.flushNeeded();}}

	@Override
	public String toString() {try{__CLR4_4_1nnkiu2vjul.R.inc(48);
		__CLR4_4_1nnkiu2vjul.R.inc(49);return "Seller{" +
				"gold=" + gold +
				", items=" + items +
				'}';
	}finally{__CLR4_4_1nnkiu2vjul.R.flushNeeded();}}
}

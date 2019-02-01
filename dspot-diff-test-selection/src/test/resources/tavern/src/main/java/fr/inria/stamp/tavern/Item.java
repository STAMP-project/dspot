package fr.inria.stamp.tavern;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/09/17
 */
public class Item {

	public final String name;

	public final int price;

	public Item(String name, int price) {
		this.name = name;
		this.price = price;
	}

	@Override
	public String toString() {
		return this.name;
	}
}

package net.fieme.zerocraft;

/**
 * Tuple ported from C#
 *
 * @param <X> the first item type
 * @param <Y> the second item type
 */
public class Tuple<X, Y> {
	public final X item1;
	public final Y item2;
	
	/**
	 * Creates a tuple
	 * 
	 * @param item1 the first item
	 * @param item2 the second item
	 */
	public Tuple(X item1, Y item2) {
		this.item1 = item1;
		this.item2 = item2;
	}
}

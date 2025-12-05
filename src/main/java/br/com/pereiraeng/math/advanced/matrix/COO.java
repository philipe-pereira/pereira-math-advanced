package br.com.pereiraeng.math.advanced.matrix;

import java.util.Collections;

import br.com.pereiraeng.core.collections.sortedlist.NaturalSortedList;


/**
 * <a href=
 * "https://docs.scipy.org/doc/scipy/reference/generated/scipy.sparse.coo_matrix.html">Coordinate
 * list (COO)</a> stores a list of (row, column, value) tuples. Ideally, the
 * entries are sorted first by row index and then by column index, to improve
 * random access times. This is another format that is good for incremental
 * matrix construction.
 * 
 * @author Philipe PEREIRA
 *
 * @param <V> classe do objetos coeficientes
 */
public class COO<V> extends NaturalSortedList<COO.CoordValue<V>> implements Sparse<V> {
	private static final long serialVersionUID = 4741922024410651482L;

	@Override
	public void set(int row, int column, V value) {
		CoordValue<V> coordValue = new CoordValue<>(row, column, value);
		int pos = Collections.binarySearch(this, coordValue);
		if (pos >= 0) // achou um elemento na mesma posição
			get(pos).setValue(value);
		else
			add(coordValue);
	}

	@Override
	public V get(int row, int column) {
		CoordValue<V> coordValue = new CoordValue<>(row, column);
		int pos = Collections.binarySearch(this, coordValue);
		if (pos >= 0)
			return get(pos).getValue();
		else
			return null;
	}

	@Override
	public int getNNZ() {
		return this.size();
	}

	/**
	 * Classe do objeto em que um valor é indexado por duas coordenadas
	 * ({@link #get1()} e {@link #get2()}) para ser armazenado numa matriz esparsa
	 * do tipo {@link COO lista de coordenadas}
	 * 
	 * @author Philipe PEREIRA
	 *
	 * @param <V> classe do objeto indexado
	 */
	protected static class CoordValue<V> implements Comparable<CoordValue<V>> {

		private final int i1;

		private final int i2;

		private V value;

		public CoordValue(int i1, int i2) {
			this(i1, i2, null);
		}

		public CoordValue(int i1, int i2, V value) {
			this.i1 = i1;
			this.i2 = i2;
			this.value = value;
		}

		public int get1() {
			return i1;
		}

		public int get2() {
			return i2;
		}

		public void setValue(V value) {
			this.value = value;
		}

		public V getValue() {
			return value;
		}

		@Override
		public boolean equals(Object anObject) {
			if (this == anObject)
				return true;
			if (anObject instanceof CoordValue) {
				CoordValue<?> coordValue = (CoordValue<?>) anObject;
				return coordValue.get1() == get1() && coordValue.get2() == get2();
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Integer.valueOf(i1 + i2).hashCode();
		}

		@Override
		public String toString() {
			return "(" + i1 + ";" + i2 + ";" + value + ")";
		}

		@Override
		public int compareTo(CoordValue<V> o) {
			int out = Integer.compare(this.get1(), o.get1());
			if (out == 0)
				return Integer.compare(this.get2(), o.get2());
			else
				return out;
		}
	}
}

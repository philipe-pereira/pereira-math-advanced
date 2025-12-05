package br.com.pereiraeng.math.advanced.matrix;

/**
 * <a href=
 * "https://docs.scipy.org/doc/scipy/reference/generated/scipy.sparse.csc_matrix.html">Compressed
 * sparse column (CSC)</a> is similar to {@link CSR} except that values are read
 * first by column, a row index is stored for each value, and column pointers
 * are stored.
 * 
 * @author Philipe PEREIRA
 *
 * @param <V> classe do objetos coeficientes
 */
public class CSC<V> extends CS<V> {

//	public static void main(String[] args) {
//		CSC<Double> cs = new CSC<>(5);
//		cs.set(4, 2, 7.);
//		cs.set(6, 3, 9.);
//		cs.set(0, 2, 2.);
//		cs.set(4, 4, 2.);
//		cs.set(4, 3, 1.);
//		cs.set(0, 0, 8.);
//		cs.set(1, 2, 5.);
//
//		System.out.println(cs);
//	}

	public CSC(int columns) {
		super(columns, 0);
	}

	@Override
	public void set(int row, int column, V value) {
		int lower = super.indexPointers.get(column);
		int upper = super.indexPointers.get(column + 1);

		if (lower == upper) { // no elements in this row
			super.indices.add(lower, row);
			super.data.add(lower, value);
		} else {
			boolean flag = false;
			for (int i = lower; i < upper; i++) {
				int index = super.indices.get(i);
				if (row == index) {
					super.data.set(i, value);
					flag = true;
				} else if (row < index) {
					super.indices.add(i, row);
					super.data.add(i, value);
					flag = true;
				}
				if (flag)
					break;
			}
			if (!flag) {
				super.indices.add(upper, row);
				super.data.add(upper, value);
			}
		}
		for (int i = column + 1; i < super.indexPointers.size(); i++) {
			int ip = super.indexPointers.get(i);
			super.indexPointers.set(i, ip + 1);
		}
	}

	@Override
	public V get(int row, int column) {
		int lower = super.indexPointers.get(column);
		int upper = super.indexPointers.get(column + 1);
		if (lower == upper) // no elements in this column
			return null;
		else {
			for (int i = lower; i < upper; i++)
				if (super.indices.get(i) == row)
					return super.data.get(i);
			return null;
		}
	}

}

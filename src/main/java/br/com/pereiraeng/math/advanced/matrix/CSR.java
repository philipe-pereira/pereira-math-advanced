package br.com.pereiraeng.math.advanced.matrix;

/**
 * <a href=
 * "https://docs.scipy.org/doc/scipy/reference/generated/scipy.sparse.csr_matrix.html">Compressed
 * sparse row (CSR)</a> or compressed row storage (CRS) or Yale format
 * represents a matrix M by three (one-dimensional) arrays, that respectively
 * contain nonzero values, the extents of rows, and column indices.
 * 
 * @author Philipe PEREIRA
 *
 * @param <V> classe do objetos coeficientes
 */
public class CSR<V> extends CS<V> {

//	public static void main(String[] args) {
		
//		CSR<Double> cs = new CSR<>(7);
//		cs.set(4, 2, 7.);
//		cs.set(6, 3, 9.);
//		cs.set(0, 2, 2.);
//		cs.set(4, 4, 2.);
//		cs.set(4, 3, 1.);
//		cs.set(0, 0, 8.);
//		cs.set(1, 2, 5.);
//		System.out.println(cs);

//		CSR<Double> cs = new CSR<>(4);
//		cs.set(3, 5, 80.);
//		cs.set(2, 4, 70.);
//		cs.set(2, 3, 60.);
//		cs.set(2, 2, 50.);
//		cs.set(1, 3, 40.);
//		cs.set(1, 1, 30.);
//		cs.set(0, 1, 20.);
//		cs.set(0, 0, 10.);
//		System.out.println(cs);
		
//	}

	public CSR(int rows) {
		super(rows, 0);
	}

	@Override
	public void set(int row, int column, V value) {
		int lower = super.indexPointers.get(row);
		int upper = super.indexPointers.get(row + 1);

		if (lower == upper) { // no elements in this row
			super.indices.add(lower, column);
			super.data.add(lower, value);
		} else {
			boolean flag = false;
			for (int i = lower; i < upper; i++) {
				int index = super.indices.get(i);
				if (column == index) {
					super.data.set(i, value);
					flag = true;
				} else if (column < index) {
					super.indices.add(i, column);
					super.data.add(i, value);
					flag = true;
				}
				if (flag)
					break;
			}
			if (!flag) {
				super.indices.add(upper, column);
				super.data.add(upper, value);
			}
		}
		for (int i = row + 1; i < super.indexPointers.size(); i++) {
			int ip = super.indexPointers.get(i);
			super.indexPointers.set(i, ip + 1);
		}
	}

	@Override
	public V get(int row, int column) {
		int lower = super.indexPointers.get(row);
		int upper = super.indexPointers.get(row + 1);
		if (lower == upper) // no elements in this row
			return null;
		else {
			for (int i = lower; i < upper; i++)
				if (super.indices.get(i) == column)
					return super.data.get(i);
			return null;
		}
	}
}

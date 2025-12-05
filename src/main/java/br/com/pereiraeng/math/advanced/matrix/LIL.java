package br.com.pereiraeng.math.advanced.matrix;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * <a href=
 * "https://docs.scipy.org/doc/scipy/reference/generated/scipy.sparse.lil_matrix.html">List
 * of lists (LIL)</a> stores one list per row, with each entry containing the
 * column index and the value. Typically, these entries are kept sorted by
 * column index for faster lookup. This is another format good for incremental
 * matrix construction.
 * 
 * @author Philipe PEREIRA
 *
 * @param <V> classe do objetos coeficientes
 */
public class LIL<V> extends ArrayList<TreeMap<Integer, V>> implements Sparse<V> {
	private static final long serialVersionUID = 1L;

//	public static void main(String[] args) {
//		LIL<Double> lil = new LIL<>();
//		lil.set(3, 2, 5.0);
//		System.out.println(lil);
//		System.out.println(transpose(lil));
//	}

	/**
	 * Construtor do objeto de uma matriz esparsa
	 */
	public LIL() {
	}

	/**
	 * Construtor do objeto de uma matriz esparsa
	 * 
	 * @param rows número de linhas
	 */
	public LIL(int rows) {
		super(rows);
	}

	@Override
	public void set(int row, int column, V value) {
		if (row >= super.size()) {
			int newSize = row + 1;
			ensureCapacity(newSize);
			while (super.size() < newSize)
				add(null);
		}
		TreeMap<Integer, V> rowTable = this.get(row);
		if (rowTable == null)
			this.set(row, rowTable = new TreeMap<Integer, V>());
		rowTable.put(column, value);
	}

	@Override
	public V get(int row, int column) {
		if (row < super.size()) {
			Map<Integer, V> rowTable = this.get(row);
			if (rowTable != null)
				return rowTable.get(column);
			else
				return null;
		} else
			return null;
	}

	@Override
	public int getNNZ() {
		int out = 0;
		for (Map<Integer, V> c : this)
			out += c.size();
		return out;
	}

	// ---------------------------------------

	public Set<Integer> getColumnsNumbers() {
		TreeSet<Integer> out = new TreeSet<>();
		for (TreeMap<Integer, V> row : this)
			out.addAll(row.keySet());
		return out;
	}

	public void transpose() {
		LIL<V> newTable = transpose(this);
		this.clear();
		this.addAll(newTable);
	}

	public static <K> LIL<K> transpose(LIL<K> lil) {
		LIL<K> out = new LIL<>();

		int rowIndex = 0;
		for (TreeMap<Integer, K> cols : lil) {
			if (cols != null) {
				for (Entry<Integer, K> e : cols.entrySet()) {
					Integer colIndex = e.getKey();
					out.set(colIndex, rowIndex, e.getValue());
				}
			}
			rowIndex++;
		}

		return out;
	}

	// TODO: swap, transformação linear com um vetor, resolve sistema linear com
	// um vetor, mostra matriz, etc...
}

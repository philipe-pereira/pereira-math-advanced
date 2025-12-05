package br.com.pereiraeng.math.advanced.matrix;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import br.com.pereiraeng.math.DuplaV;

/**
 * <a href=
 * "https://docs.scipy.org/doc/scipy/reference/generated/scipy.sparse.dok_matrix.html">Dictionary
 * of keys (DOK)</a> consists of a dictionary that maps (row, column)-pairs to
 * the value of the elements. Elements that are missing from the dictionary are
 * taken to be zero. The format is good for incrementally constructing a sparse
 * matrix in random order, but poor for iterating over non-zero values in
 * lexicographical order. One typically constructs a matrix in this format and
 * then converts to another more efficient format for processing.
 * 
 * @author Philipe PEREIRA
 *
 * @param <V> classe do objetos coeficientes
 */
public class DOK<V> extends HashMap<DuplaV, V> implements Sparse<V> {
	private static final long serialVersionUID = -2434191274794640959L;

	@Override
	public void set(int row, int column, V value) {
		this.put(new DuplaV(row, column), value);
	}

	@Override
	public V get(int row, int column) {
		return this.get(new DuplaV(row, column));
	}

	@Override
	public int getNNZ() {
		return this.size();
	}

	public void transpose() {
		DOK<V> newTable = transpose(this);
		this.clear();
		this.putAll(newTable);
	}

	public static <V> DOK<V> transpose(DOK<V> dok) {
		DOK<V> out = new DOK<>();
		for (Entry<DuplaV, V> e : dok.entrySet())
			out.put(new DuplaV(e.getKey().get2(), e.getKey().get1()), e.getValue());
		return out;
	}

	public Set<Integer> getRowsIndices() {
		Set<Integer> out = new TreeSet<>();
		for (DuplaV d : this.keySet())
			out.add(d.get1());
		return out;
	}

	public Set<Integer> getColumnsIndices() {
		Set<Integer> out = new TreeSet<>();
		for (DuplaV d : this.keySet())
			out.add(d.get2());
		return out;
	}

	/**
	 * Função que retorna uma determinada linha, representada por uma tabela de
	 * dispersão que associa para cada índice da coluna o respectivo valor
	 * 
	 * @param row índice da linha
	 * @return tabela de dispersão que associa para cada índice da coluna o
	 *         respectivo valor
	 */
	public Map<Integer, V> getRow(int row) {
		Map<Integer, V> out = new TreeMap<>();
		for (Entry<DuplaV, V> e : this.entrySet())
			if (e.getKey().get1() == row)
				out.put(e.getKey().get2(), e.getValue());
		return out;
	}

	/**
	 * Função que retorna uma determinada coluna, representada por uma tabela de
	 * dispersão que associa para cada índice da linha o respectivo valor
	 * 
	 * @param column índice da coluna
	 * @return tabela de dispersão que associa para cada índice da linha o
	 *         respectivo valor
	 */
	public Map<Integer, V> getColumn(int column) {
		Map<Integer, V> out = new TreeMap<>();
		for (Entry<DuplaV, V> e : this.entrySet())
			if (e.getKey().get2() == column)
				out.put(e.getKey().get1(), e.getValue());
		return out;
	}

	public Map<Integer, Map<Integer, V>> getMapOfMaps() {
		Map<Integer, Map<Integer, V>> out = new HashMap<>();
		for (Entry<DuplaV, V> entry : this.entrySet()) {
			DuplaV duplaV = entry.getKey();
			Map<Integer, V> column = out.get(duplaV.get1());
			if (column == null)
				out.put(duplaV.get1(), column = new HashMap<>());
			column.put(duplaV.get2(), entry.getValue());
		}
		return out;
	}
}

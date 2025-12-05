package br.com.pereiraeng.math.advanced.matrix;

import java.util.ArrayList;

/**
 * Compressed sparse matrix
 * 
 * @author Philipe PEREIRA
 *
 * @param <V> classe do objetos coeficientes
 */
public abstract class CS<V> implements Sparse<V> {

	protected ArrayList<Integer> indexPointers;

	protected ArrayList<Integer> indices;

	protected ArrayList<V> data;

	/**
	 * Construtor da matriz comprimida
	 * 
	 * @param rc  number of rows or columns
	 * @param nnz number of values, including explicit zeros
	 */
	protected CS(int rc, int nnz) {
		this.indexPointers = new ArrayList<>(rc + 1);
		for (int i = 0; i <= rc; i++)
			this.indexPointers.add(0);
		this.indices = new ArrayList<>(nnz);
		this.data = new ArrayList<>(nnz);
	}

	@Override
	public int getNNZ() {
		return data.size();
	}
}

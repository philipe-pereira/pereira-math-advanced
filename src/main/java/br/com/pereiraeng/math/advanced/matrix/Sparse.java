package br.com.pereiraeng.math.advanced.matrix;

public interface Sparse<V> {

	/**
	 * Função que estabelece o valor de um dos coeficientes da matriz
	 * 
	 * @param row    índice da linha
	 * @param column índica da coluna
	 * @param value  valor do coeficiente
	 */
	public void set(int row, int column, V value);

	/**
	 * Função que retorna o valor de um dos coeficientes da matriz
	 * 
	 * @param row    índice da linha
	 * @param column índica da coluna
	 * @return valor do coeficiente
	 */
	public V get(int row, int column);
	
	/**
	 * Função que retorna o número de entradas explícitas da matriz
	 * @return Number of values, including explicit zeros
	 */
	public int getNNZ();
}

package br.com.pereiraeng.math.advanced.dsp;

import br.com.pereiraeng.math.Complex;

/**
 * Classe do objeto que representa uma série de dados amostrados sobre o qual
 * calculou-se a série harmônica, dispondo-se de seus coeficientes
 * {@link #getCoefs()}
 * 
 * @author Philipe PEREIRA
 * @version October 14th, 2020
 *
 */
public abstract class HarmSerieSampled extends SerieSampled {

	protected double error;

	/**
	 * Função que retorna os {@link HarmSerie#getCoefs() coeficientes harmônicos} de
	 * uma série de dados amostrados
	 * 
	 * @return vetor de número complexos dos coeficientes da série harmônica
	 *         correspondente ao sinal amostrado
	 */
	protected abstract Complex[] getCoefs();

	public abstract HarmSerie getSerie();

	public void setError(double error) {
		this.error = error;
	}
}

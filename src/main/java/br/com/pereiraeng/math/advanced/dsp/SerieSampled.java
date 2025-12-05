package br.com.pereiraeng.math.advanced.dsp;

/**
 * Classe do objeto que representa uma série de dados amostrados
 * 
 * @author Philipe PEREIRA
 * @version October 14th, 2020
 *
 */
public class SerieSampled {

	/**
	 * valor da primeira abscissa do sinal amostrado (t=0)
	 */
	protected double offset;

	public void setOffset(double offset) {
		this.offset = offset;
	}

	public double getOffset() {
		return offset;
	}
}

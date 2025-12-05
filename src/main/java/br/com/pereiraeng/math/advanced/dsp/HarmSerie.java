package br.com.pereiraeng.math.advanced.dsp;

import br.com.pereiraeng.math.Complex;

/**
 * Classe do objeto que reprseenta uma série harmônica
 * 
 * @author Philipe PEREIRA
 * @data October 14th, 2020
 *
 */
public abstract class HarmSerie {

	/**
	 * frequência fundamental, em rad/u.t.
	 */
	protected final double ff;

	/**
	 * Número de harmônicos
	 */
	protected int nh;

	/**
	 * Construtor da série harmônica
	 * 
	 * @param ff frequência fundamental, em rad/u.t.
	 * @param nh número total de harmônicos
	 */
	public HarmSerie(double ff, int nh) {
		this.ff = ff;
		this.nh = nh;
	}

	/**
	 * Função que retorna a frequência fundamental
	 * 
	 * @return frequência fundamental, em rad/u.t.
	 */
	public double getFf() {
		return ff;
	}

	/**
	 * Função que retorna a frequência da harmônica de maior ordem
	 * 
	 * @return frequência da harmônica de maior ordem, em rad/u.t.
	 */
	public double getFM() {
		return getFf() * getNh();
	}

	/**
	 * Função que retorna o número de harmônicos
	 * 
	 * @param nh número de harmônicos
	 */
	public void setNh(int nh) {
		this.nh = nh;
	}

	/**
	 * Função que estabelece o número de harmônicos
	 * 
	 * @return número de harmônicos
	 */
	public int getNh() {
		return nh;
	}

	/**
	 * Função que retorna os coeficientes harmônicos de um sinal
	 * 
	 * @return vetor de número complexos dos coeficientes da série harmônica
	 *         correspondente ao sinal
	 */
	public abstract Complex[] getCoefs();
}

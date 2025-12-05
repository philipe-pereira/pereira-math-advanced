package br.com.pereiraeng.math.advanced.dsp;

import java.util.Arrays;

import br.com.pereiraeng.math.Complex;
import br.com.pereiraeng.core.ExtendedMath;

/**
 * Classe do objeto que reprseenta uma série dos cossenos
 * 
 * @author Philipe PEREIRA
 * @data October 14th, 2020
 *
 */
public class PairFourierSerie extends HarmSerie {

	private double[] coefs;

	public PairFourierSerie(double ff, int n) {
		this(ff, new double[n]);
	}

	public PairFourierSerie(double ff, double[] coefs) {
		super(ff, coefs.length);
		this.coefs = coefs;
	}

	@Override
	public void setNh(int n) {
		this.coefs = Arrays.copyOf(this.coefs, n);
		super.setNh(n);
	}

	public void setCoefs(double[] coefs) {
		this.coefs = coefs;
		super.setNh(coefs.length);
	}

	@Override
	public Complex[] getCoefs() {
		Complex[] out = new Complex[coefs.length];
		for (int i = 0; i < coefs.length; i++)
			out[i] = new Complex(coefs[i], 0);
		return out;
	}

	public double[] getCoefsD() {
		return coefs;
	}

	// -------------------------------------------------------------------

	// TRANSFORMADA DISCRETA DOS COSSENOS

	// ---- direta ----

	/**
	 * Função que calcula a transformada discreta dos cossenos de uma sequência de
	 * sinais discretos <strong>regularmente amostrados</strong>
	 * 
	 * @param y  vetor com os valores dos sinais discretos
	 * @param dt espaço de tempo suposto entre duas amostras
	 * @return vetor com os números reais do sinal transformado
	 */
	public static double[] dct(double[] y, double dt) {
		return dct(y, dt, ExtendedMath.TWO_PI / (y.length * dt));
	}

	/**
	 * Função que calcula a transformada discreta dos cossenos de uma sequência de
	 * sinais discretos <strong>regularmente amostrados</strong>
	 * 
	 * @param y  vetor com os valores dos sinais discretos
	 * @param dt espaço de tempo suposto entre duas amostras
	 * @param w  frequência angular, em rad/u.t.
	 * @return vetor com os números reais do sinal transformado
	 */
	public static double[] dct(double[] y, double dt, double w) {
		int N = (int) Math.round((ExtendedMath.TWO_PI / w) / dt);
		double[] out = new double[N];
		for (int i = 0; i < y.length; i++) {
			for (int j = 0; j < y.length; j++)
				out[i] += y[j] * Math.cos(-i * w * j * dt);
		}
		return out;
	}

	// ---- inversa ----

	// um valor

	/**
	 * Função que calcula o valor da transformada discreta dos cossenos para um dado
	 * ponto
	 * 
	 * @param t  abscissa do ponto a ser calculado
	 * @param c  números reais que representam a transformação (seus coefientes
	 *           harmônicos, somente módulo - DCT)
	 * @param w  frequência angular, em rad/u.t.
	 * @param t0 instante de tempo inicial
	 * @param dt espaço de tempo suposto entre duas amostras
	 * @return valor da transformada no ponto dado
	 */
	public static double dct(double t, double[] c, double w, double t0, double dt) {
		return dct(t, ExtendedMath.TWO_PI / w, c, t0, dt);
	}

	/**
	 * Função que calcula o valor da transformada discreta dos cossenos para um dado
	 * ponto
	 * 
	 * @param t  abscissa do ponto a ser calculado
	 * @param c  números reais que representam a transformação (seus coefientes
	 *           harmônicos, somente módulo - DCT)
	 * @param t0 instante de tempo inicial
	 * @param dt espaço de tempo suposto entre duas amostras
	 * @return valor da transformada no ponto dado
	 */
	public static double dct(double t, double[] c, double t0, double dt) {
		return dct(t, dt * c.length, c, t0, dt);
	}

	private static double dct(double t, double T, double[] c, double t0, double dt) {
		t += T - t0;
		if (t >= T)
			t %= T;
		double dt2 = dt / 2.;
		double w2 = Math.PI / T;

		double out = c[0] / 2;
		for (int i = 1; i < c.length; i++)
			out += c[i] * Math.cos(i * w2 * (dt2 + t));
		return 2 * out;
	}

	// todos valores

	/**
	 * Função que calcula o valor da transformada discreta dos cossenos para uma
	 * série de pontos
	 * 
	 * @param t  vetor com os valores de tempo
	 * @param c  números reais que representam a transformação (seus coefientes
	 *           harmônicos, somente módulo - DCT)
	 * @param dt espaço de tempo suposto entre duas amostras
	 * @return valor da transformada numa série de pontos
	 */
	public static double[] dct(double[] t, double[] c, double dt) {
		double T = dt * c.length;
		double d = dt / T;

		double[] out = new double[t.length];
		for (int i = 0; i < t.length; i++) {
			double y = c[0] / 2;
			for (int j = 1; j < c.length; j++)
				y += c[j] * Math.cos(j * Math.PI * (d / 2. + (t[i] - t[0]) * d / dt));
			out[i] = 2 * y;
		}
		return out;
	}

	// -----------------------
	// espaçada irregularmente
	// -----------------------

	// ---- direta ----

	/**
	 * Função que calcula a transformada discreta dos cossenos
	 * <strong>irregularmente amostrada</strong> (Non-uniform Discrete Cossinus
	 * Transform) de uma sequência de sinais discretos no tempo
	 * 
	 * @param t  vetor com os valores de tempo
	 * @param y  vetor com os valores dos sinais discretos
	 * @param dt espaço de tempo suposto entre duas amostras
	 * @return vetor com os números reais do sinal transformado
	 */
	public static double[] nudct(double[] t, double[] y, double dt) {
		double[] out = new double[y.length];
		double T = t[t.length - 1] - t[0] + dt;
		double d = dt / T;

		for (int i = 0; i < y.length; i++) {
			for (int j = 0; j < y.length; j++)
				out[i] += y[j] * Math.cos(i * Math.PI * (d / 2. + (t[j] - t[0]) * d / dt));
			out[i] /= y.length;
		}
		return out;
	}

	/**
	 * Função que calcula a transformada discreta dos cossenos
	 * <strong>irregularmente amostrada</strong> (Non-uniform Discrete Cossinus
	 * Transform) de uma sequência de sinais discretos no tempo
	 * 
	 * @param t  vetor com os valores de tempo
	 * @param y  vetor com os valores dos sinais discretos
	 * @param dt espaço de tempo suposto entre duas amostras
	 * @param w  frequência fundamental considerada (2*pi/T, onde T é a largura do
	 *           período que compreende os valores amostrados)
	 * @return vetor com os números reais do sinal transformado
	 */
	public static double[] nudct(double[] t, double[] y, double dt, double w) {
		double T = ExtendedMath.TWO_PI / w;
		double d = dt / T;

		double[] out = new double[(int) Math.round(T / dt)];
		for (int i = 0; i < out.length; i++) {
			for (int j = 0; j < y.length; j++)
				out[i] += y[j] * Math.cos(i * Math.PI * d * (.5 + (t[j] - t[0]) / dt));
			out[i] /= y.length;
		}
		return out;
	}

	// ---- inversa ----

	// dct(double, double, double, double[]) & dct(double, double[], double[])
	// funcionam como inversa das funções nudct's
}
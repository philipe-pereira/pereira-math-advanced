package br.com.pereiraeng.math.advanced.dsp;

import java.util.Arrays;

import br.com.pereiraeng.math.Complex;
import br.com.pereiraeng.core.ExtendedMath;

/**
 * Classe do objeto que reprseenta uma série de Fourier
 * 
 * @author Philipe PEREIRA
 * @data October 14th, 2020
 *
 */
public class FourierSerie extends HarmSerie {

	private Complex[] coefs;

	public FourierSerie(double ff, int n) {
		this(ff, new Complex[n]);
	}

	public FourierSerie(double ff, Complex[] coefs) {
		super(ff, coefs.length);
		this.coefs = coefs;
	}

	@Override
	public void setNh(int n) {
		this.coefs = Arrays.copyOf(this.coefs, n);
		super.setNh(n);
	}

	public void setCoefs(Complex[] coefs) {
		this.coefs = coefs;
		super.setNh(coefs.length);
	}

	@Override
	public Complex[] getCoefs() {
		return coefs;
	}

	// -------------------------------------------------------------------

	// Parceval

	public double getPower() {
		return getPower(this.coefs);
	}

	public static double getPower(Complex[] coefs) {
		double out = 0.;
		for (int i = 0; i < coefs.length; i++)
			out += Math.pow(coefs[i].getMod(), 2);
		return Math.sqrt(out);
	}

	// -------------------------------------------------------------------

	// TRANSFORMADA DE FOURIER

	// ---- direta ----

	/**
	 * Função que calcula a transformada discreta de Fourier de uma sequência de
	 * sinais discretos <strong>regularmente amostrados</strong>
	 * 
	 * @param y  vetor com os valores dos sinais discretos
	 * @param dt espaço de tempo suposto entre duas amostras
	 * @return vetor com os números complexos do sinal transformado
	 */
	public static Complex[] dft(double[] y, double dt) {
		return dft(y, dt, ExtendedMath.TWO_PI / (y.length * dt));
	}

	/**
	 * Função que calcula a transformada discreta de Fourier de uma sequência de
	 * sinais discretos <strong>regularmente amostrados</strong>
	 * 
	 * @param y  vetor com os valores dos sinais discretos
	 * @param dt espaço de tempo suposto entre duas amostras
	 * @param w  frequência angular, em rad/u.t.
	 * @return vetor com os números complexos do sinal transformado
	 */
	public static Complex[] dft(double[] y, double dt, double w) {
		int N = (int) Math.round((ExtendedMath.TWO_PI / w) / dt);
		Complex[] out = new Complex[N];
		for (int i = 0; i < N; i++) {
			out[i] = new Complex();
			for (int j = 0; j < y.length; j++)
				out[i] = Complex.sum(out[i], Complex.mult(y[j], new Complex(1, -w * i * j * dt, false)));
			out[i].div(y.length);
		}
		return out;
	}

	// ---- inversa ----

	// um valor

	/**
	 * Função que calcula o valor da transformada de Fourier para um dado ponto
	 * 
	 * @param t abscissa do ponto a ser calculado
	 * @param f números complexo que representam a transformação (seus coefientes
	 *          harmônicos, com módulo e fase)
	 * @param w frequência angular, em rad/u.t.
	 * @return valor da transformada no ponto dado
	 */
	public static double dft(double t, Complex[] f, double w) {
		double out = 0.;
		for (int i = 0; i < f.length; i++)
			out += f[i].getMod() * Math.cos(w * i * t + f[i].getArg());
		return out;
	}

	// vários valores

	/**
	 * Função que calcula o valor da transformada de Fourier para uma série de
	 * pontos
	 * 
	 * @param t vetor com as abscissas dos pontos a serem calculados
	 * @param f números complexo que representam a transformação (seus coefientes
	 *          harmônicos, com módulo e fase)
	 * @param w frequência angular, em rad/u.t.
	 * @return valor da transformada numa série de pontos
	 */
	public static double[] dft(double[] t, Complex[] f, double w) {
		double[] out = new double[t.length];
		for (int j = 0; j < t.length; j++)
			out[j] = dft(t[j], f, w);
		return out;
	}

	// -----------------------
	// espaçada irregularmente
	// -----------------------

	// ---- direta ----

	/**
	 * Função que calcula a transformada discreta de Fourier <strong>irregularmente
	 * amostrada</strong> (Non-uniform Discrete Fourier Transform) de uma sequência
	 * de sinais discretos no tempo
	 * 
	 * @param t  vetor com os valores de tempo
	 * @param y  vetor com os valores dos sinais discretos
	 * @param dt espaço de tempo suposto entre duas amostras
	 * @return vetor com os números complexos do sinal transformado
	 */
	public static Complex[] nudft(double[] t, double[] y, double dt) {
		return nudft(t, y, dt, ExtendedMath.TWO_PI / (t[t.length - 1] - t[0] + dt));
	}

	/**
	 * Função que calcula a transformada discreta de Fourier <strong>irregularmente
	 * amostrada</strong> (Non-uniform Discrete Fourier Transform) de uma sequência
	 * de sinais discretos no tempo
	 * 
	 * @param t  vetor com os valores de tempo
	 * @param y  vetor com os valores dos sinais discretos
	 * @param dt espaço de tempo suposto entre duas amostras
	 * @param w  frequência fundamental considerada (2*pi/T, onde T é a largura do
	 *           período que compreende os valores amostrados)
	 * @return vetor com os números complexos do sinal transformado
	 */
	public static Complex[] nudft(double[] t, double[] y, double dt, double w) {
		double T = ExtendedMath.TWO_PI / w;
		int N = (int) Math.round(T / dt);

		Complex[] out = new Complex[N];

		for (int i = 0; i < N; i++) {
			out[i] = new Complex();
			for (int j = 0; j < y.length; j++)
				out[i] = Complex.sum(out[i], Complex.mult(y[j], new Complex(1, -w * i * t[j], false)));
			out[i].div(y.length);
		}
		return out;
	}

	// ---- inversa ----

	// dft(double, Complex[], double) & dft(double[], Complex[], double) funcionam
	// como inversa das funções nudft's
}
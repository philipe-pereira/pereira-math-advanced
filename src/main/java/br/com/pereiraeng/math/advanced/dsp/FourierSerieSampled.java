package br.com.pereiraeng.math.advanced.dsp;

import br.com.pereiraeng.math.Complex;
import br.com.pereiraeng.core.ExtendedMath;

/**
 * Classe do objeto que representa uma série de dados amostrados sobre o qual
 * calculou-se a transformada discreta de Fourier
 * 
 * @author Philipe PEREIRA
 * @version October 14th, 2020
 *
 */
public class FourierSerieSampled extends HarmSerieSampled {

	private final FourierSerie fs;

	public FourierSerieSampled(double ff, Complex[] coefs) {
		fs = new FourierSerie(ff, coefs);
	}

	public FourierSerieSampled(FourierSerieSampled fss, Filter f) {
		Complex[] nf = f.apply(fss.getFf(), fss.getCoefs());
		this.fs = new FourierSerie(fss.getFf(), nf);
		setOffset(fss.getOffset());
	}

	public FourierSerieSampled(FourierSerieSampled fss, int trunc) {
		Complex[] nf = fss.getCoefs();

		Complex[] newNf = new Complex[nf.length];
		newNf[0] = new Complex(nf[0]);
		double power = 0., remainingPower = 0.;
		for (int i = 1; i < nf.length; i++) {
			double m2 = nf[i].getMod2();
			power += m2;
			if (i <= trunc) {
				newNf[i] = new Complex(nf[i]);
				remainingPower += m2;
			} else
				newNf[i] = new Complex();
		}

		// esta é provalvelmente o maior acochambramento matemático da história da
		// engenharia...
		double m = power / remainingPower;
//		double m = Math.sqrt(power / remainingPower);

		for (int i = 1; i <= trunc; i++)
			newNf[i].mult(m);

		this.fs = new FourierSerie(fss.getFf(), newNf);
		setOffset(fss.getOffset());
	}

	// ---------------------- MÉTODOS DE INTERFACEAMENTO -------------------------

	/**
	 * Função que retorna a frequência fundamental
	 * 
	 * @return frequência fundamental, em rad/u.t.
	 */
	public double getFf() {
		return this.fs.getFf();
	}

	/**
	 * Função que retorna a frequência da harmônica de maior ordem
	 * 
	 * @return frequência da harmônica de maior ordem, em rad/u.t.
	 */
	public double getFM() {
		return this.fs.getFM();
	}

	@Override
	public Complex[] getCoefs() {
		return this.fs.getCoefs();
	}

	@Override
	public HarmSerie getSerie() {
		return fs;
	}

	// ---------------------------------------------------------------------------

	/**
	 * Função que calcula os coeficientes da série de Fourier de um sinal amostrado
	 * 
	 * @param t  série temporal
	 * @param y  sinal amostrado
	 * @param dt espaço entre duas amostras (não precisa ser necessariamente a
	 *           distância entre as medições - afinal, se trata de
	 *           {@link FourierSerie#nudft(double, double[], double[])} - mas neste
	 *           caso poderá ocorrer problemas de dispersão espectral)
	 * @return coeficientes da série harmônica
	 */
	public static FourierSerieSampled getFourierSerie(double[] t, double[] y, double dt) {
		// período da análise
		double offset = t[0]; // primeira ordenada
		double dtfl = t[t.length - 1] - offset; // última ordenada - primeira = período - dt

		// total de minutos da análise
		double T = dtfl + dt;
		double w = ExtendedMath.TWO_PI / T;

		// efetua a análise harmônica
		FourierSerieSampled f = new FourierSerieSampled(w, FourierSerie.nudft(t, y, dt, w));
		f.setOffset(offset);
		return f;
	}

	/**
	 * Função que calcula os coeficientes da série de Fourier de um sinal amostrado
	 * 
	 * @param t  série temporal
	 * @param y  sinal amostrado
	 * @param dt espaço entre duas amostras (não precisa ser necessariamente a
	 *           distância entre as medições - afinal, se trata de
	 *           {@link FourierSerie#nudft(double, double[], double[])} - mas neste
	 *           caso poderá ocorrer problemas de dispersão espectral)
	 * @param T  período sobre o qual se supõe periodicidade
	 * @return coeficientes da série harmônica
	 */
	public static FourierSerieSampled getFourierSerie(double[] t, double[] y, double dt, double T) {

		double w0 = ExtendedMath.TWO_PI / T;

		FourierSerieSampled f = new FourierSerieSampled(w0, FourierSerie.nudft(t, y, dt, w0));

		f.setOffset(t[0]);
		return f;
	}

	// ---------------------------------------------------------------------------

	/**
	 * Função que obtém a transformada discreta de Fourier de uma série amostrada e,
	 * a partir dela, reconstroi o sinal para um número de pontos quaisquer
	 * 
	 * @param t   série temporal
	 * @param y0  sinal amostrado original
	 * @param dt  espaço entre duas amostras
	 * @param pts novo número de pontos da série harmônica
	 * @return vetor com a série harmônica
	 */
	public static double[] getSerie(double[] t, double[] y, double dt, int pts) {
		// efetua a análise harmônica
		FourierSerieSampled fss = getFourierSerie(t, y, dt);

		double w = fss.fs.getFf();

		double T = ExtendedMath.TWO_PI / w;
		double begin = fss.getOffset();

		// de posse da curva contínua, reconstroi-se usando-se o passo que quiser (novo
		// número de pontos)
		double[] yout = new double[pts];
		double newStep = T / (pts - 1); // TODO pts - 1 ?
		for (int j = 0; j < pts; j++)
			yout[j] = FourierSerie.dft(begin + j * newStep, fss.fs.getCoefs(), w);
		return yout;
	}

	/**
	 * Função que calcula um período de uma série harmônica a partir de seus
	 * coeficientes
	 * 
	 * @param fss coeficientes da série harmônica
	 * @return matriz com a série temporal e a série harmônica
	 */
	public static double[][] getSerie(FourierSerieSampled fss) {
		Complex[] cs = fss.getCoefs();
		int pts = cs.length;
		double w = fss.getFf();
		double Ttotal = ExtendedMath.TWO_PI / w;
		double dt = Ttotal / pts;

		// reconstruir sinal
		double[][] ty = new double[2][pts];
		for (int i = 0; i < pts; i++)
			ty[1][i] = FourierSerie.dft(ty[0][i] = fss.getOffset() + i * dt, cs, w);
		return ty;
	}

	/**
	 * Função que calcula uma série harmônica a partir de seus coeficientes
	 * 
	 * @param fss coeficientes da série harmônica
	 * @param T   tamanho do intervalo sobre o qual a função será calculada
	 * @return matriz com a série temporal e a série harmônica
	 */
	public static double[][] getSerie(FourierSerieSampled fss, double T) {
		Complex[] cs = fss.getCoefs();
		double w = fss.getFf();
		double Treal = ExtendedMath.TWO_PI / w;

		int pts = (int) (cs.length * (T / Treal));
		double dt = T / pts;

		// reconstruir sinal
		double[][] ty = new double[2][pts];
		for (int i = 0; i < pts; i++)
			ty[1][i] = FourierSerie.dft(ty[0][i] = fss.getOffset() + i * dt, cs, w);
		return ty;
	}
}

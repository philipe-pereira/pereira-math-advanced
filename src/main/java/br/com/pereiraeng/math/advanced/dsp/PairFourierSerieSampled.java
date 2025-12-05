package br.com.pereiraeng.math.advanced.dsp;

import java.util.Arrays;

import br.com.pereiraeng.math.Complex;
import br.com.pereiraeng.core.ExtendedMath;

/**
 * Classe do objeto que representa uma série de dados amostrados sobre o qual
 * calculou-se a transformada discreta dos cossenos
 * 
 * @author Philipe PEREIRA
 * @version October 14th, 2020
 *
 */
public class PairFourierSerieSampled extends HarmSerieSampled {

	private final PairFourierSerie pfs;

	public PairFourierSerieSampled(double ff, double[] coefs) {
		pfs = new PairFourierSerie(ff, coefs);
	}

	public PairFourierSerieSampled(PairFourierSerieSampled pfss, Filter f) {
		double[] nf = f.apply(pfss.getFf(), pfss.getCoefsD());
		this.pfs = new PairFourierSerie(pfss.getFf(), nf);
		setOffset(pfss.getOffset());
	}

	public PairFourierSerieSampled(PairFourierSerieSampled pfss, int trunc) {
		double[] nf = pfss.getCoefsD();
		nf = Arrays.copyOf(nf, nf.length);
		for (int i = trunc + 1; i < nf.length; i++)
			nf[i] = 0.;
		this.pfs = new PairFourierSerie(pfss.getFf(), nf);
		setOffset(pfss.getOffset());
	}

	// ---------------------- MÉTODOS DE INTERFACEAMENTO -------------------------

	/**
	 * Função que retorna a frequência fundamental
	 * 
	 * @return frequência fundamental, em rad/u.t.
	 */
	public double getFf() {
		return this.pfs.getFf();
	}

	/**
	 * Função que retorna a frequência da harmônica de maior ordem
	 * 
	 * @return frequência da harmônica de maior ordem, em rad/u.t.
	 */
	public double getFM() {
		return this.pfs.getFM();
	}

	@Override
	public Complex[] getCoefs() {
		return this.pfs.getCoefs();
	}

	public double[] getCoefsD() {
		return this.pfs.getCoefsD();
	}

	@Override
	public HarmSerie getSerie() {
		return pfs;
	}

	// ---------------------------------------------------------------------------

	/**
	 * Função que calcula os coeficientes da série dos cossenos de um sinal
	 * amostrado
	 * 
	 * @param t  série temporal
	 * @param y  sinal amostrado
	 * @param dt espaço entre duas amostras (não precisa ser necessariamente a
	 *           distância entre as medições - afinal, se trata de
	 *           {@link PairFourierSerie#nudct(double[], double[], double) } - mas
	 *           neste caso poderá ocorrer problemas de dispersão espectral)
	 * @return coeficientes da série harmônica
	 */
	public static PairFourierSerieSampled getPairFourierSerie(double dt, double[] t, double[] y) {
		PairFourierSerieSampled f = new PairFourierSerieSampled(ExtendedMath.TWO_PI / (t[t.length - 1] - t[0] + dt),
				PairFourierSerie.nudct(t, y, dt));
		f.setOffset(t[0]);
		return f;
	}

	/**
	 * Função que calcula os coeficientes da série dos cossenos de um sinal
	 * amostrado
	 * 
	 * @param t  série temporal
	 * @param y  sinal amostrado
	 * @param dt espaço entre duas amostras (não precisa ser necessariamente a
	 *           distância entre as medições - afinal, se trata de
	 *           {@link PairFourierSerie#nudct(double[], double[], double) } - mas
	 *           neste caso poderá ocorrer problemas de dispersão espectral)
	 * @param T  período sobre o qual se supõe periodicidade
	 * @return coeficientes da série harmônica
	 */
	public static PairFourierSerieSampled getPairFourierSerie(double[] t, double[] y, double dt, double T) {
		double w0 = ExtendedMath.TWO_PI / T;
		PairFourierSerieSampled f = new PairFourierSerieSampled(w0, PairFourierSerie.nudct(t, y, dt, w0));
		f.setOffset(t[0]);
		return f;
	}

	// ---------------------------------------------------------------------------

	/**
	 * Função que obtém a transformada discreta dos cossenos de uma série amostrada
	 * e, a partir dela, reconstroi o sinal para um número de pontos quaisquer
	 * 
	 * @param t   série temporal
	 * @param y0  sinal amostrado original
	 * @param dt  espaço entre duas amostras
	 * @param pts novo número de pontos da série harmônica
	 * @return vetor com a série harmônica
	 */
	public static double[] getSerie(double[] t, double[] y0, double dt, int pts) {
		// efetua a análise harmônica
		PairFourierSerieSampled pfss = getPairFourierSerie(dt, t, y0);

		double w = pfss.pfs.getFf();

		double T = ExtendedMath.TWO_PI / w;
		double begin = pfss.getOffset();

		// de posse da curva contínua, reconstroi-se usando-se o passo que quiser (novo
		// número de pontos)
		double[] y = new double[pts];
		double newStep = T / (pts - 1);
		for (int j = 0; j < pts; j++)
			y[j] = PairFourierSerie.dct(begin + j * newStep, pfss.pfs.getCoefsD(), begin, dt);
		return y;
	}

	/**
	 * Função que calcula um período de uma série harmônica a partir de seus
	 * coeficientes
	 * 
	 * @param pfss coeficientes da série harmônica
	 * @return matriz com a série temporal e a série harmônica
	 */
	public static double[][] getSerie(PairFourierSerieSampled pfss) {
		double[] cs = pfss.getCoefsD();
		int pts = cs.length;
		double w = pfss.getFf();
		double Ttotal = ExtendedMath.TWO_PI / w;
		double dt = Ttotal / pts;

		// reconstruir sinal
		double[][] ty = new double[2][pts];
		for (int i = 0; i < pts; i++)
			ty[1][i] = PairFourierSerie.dct(ty[0][i] = pfss.getOffset() + i * dt, cs, w, pfss.getOffset(), dt);
		return ty;
	}

	/**
	 * Função que calcula uma série harmônica a partir de seus coeficientes
	 * 
	 * @param pfss coeficientes da série harmônica
	 * @param T    tamanho do intervalo sobre o qual a função será calculada
	 * @return matriz com a série temporal e a série harmônica
	 */
	public static double[][] getSerie(PairFourierSerieSampled pfss, double T) {
		double[] cs = pfss.getCoefsD();
		double w = pfss.getFf();
		double Treal = ExtendedMath.TWO_PI / w;
		int pts = (int) (cs.length * (T / Treal));
		double dt = T / pts;

		// reconstruir sinal
		double[][] ty = new double[2][pts];
		for (int i = 0; i < pts; i++)
			ty[1][i] = PairFourierSerie.dct(ty[0][i] = pfss.getOffset() + i * dt, cs, w, pfss.getOffset(), dt);
		return ty;
	}
}

package br.com.pereiraeng.math.advanced.dsp;

import br.com.pereiraeng.math.Complex;
import br.com.pereiraeng.math.ExtendedMathComplex;
import br.com.pereiraeng.math.Vec;
import br.com.pereiraeng.core.ExtendedMath;

/**
 * Classe que representa um filtro linear
 * 
 * @author Philipe PEREIRA
 *
 */
public class Filter {

	private static final double A_DEF = 2463.4769331170933454375253832454;

	private FilterType type;

	public enum FilterType {
		BESSEL, BUTTERWORTH, CHEBYSHEV_1, CHEBYSHEV_2, ELLIPTIC, SPECIAL;
	}

	private Object[] params;

	public Filter(FilterType type, Object... params) {
		this.type = type;
		this.params = params;
	}

	public FilterType getType() {
		return type;
	}

	public Object[] getParams() {
		return params;
	}

	@Override
	public String toString() {
		return String.format("%s/order %d", type, params[0]);
	}

	// ------------------------------------------------------------------

	public Complex get(double w) {
		Complex out = null;

		// TODO esperar consolidar a função get(w,nc)

		return out;
	}

	public Complex[] get(double w, int nc) {
		Complex[] out = new Complex[nc];

		int n = (int) params[0];
		double wc = (double) params[1];

		switch (type) {
		case BUTTERWORTH:

			Complex[] sk = new Complex[n];

			for (int k = 1; k <= sk.length; k++)
				sk[k - 1] = new Complex(wc, (2 * k + n - 1) * Math.PI / (2 * n), false);

			for (int i = 0; i < nc; i++) {
				Complex s = new Complex(0, w * i);

				Complex hs = new Complex(1, 0);
				for (int k = 0; k < n; k++)
					hs.mult(Complex.mult(1 / wc, Complex.sub(s, sk[k])));
				hs = Complex.inv(hs);

				out[i] = hs;
			}
			break;
		case CHEBYSHEV_1:
			double eps = 0.5;

			Complex[] spm = new Complex[n];

			for (int m = 1; m <= n; m++) {
				double tm = (2 * m - 1) * Math.PI / 2 / n;
				double arg = ExtendedMath.asinh(1 / eps) / n;
				spm[m - 1] = new Complex(-Math.sinh(arg) * Math.sin(tm), Math.cosh(arg) * Math.cos(tm));
			}

			for (int i = 0; i < nc; i++) {
				Complex s = new Complex(0, w * i);

				Complex hs = new Complex(1, 0);
				for (int m = 0; m < n; m++)
					hs.mult(Complex.sub(Complex.mult(1 / wc, s), spm[m]));
				hs = Complex.mult(1. / (eps * Math.pow(2, n - 1)), Complex.inv(hs));

				out[i] = hs;
			}
			break;
		case CHEBYSHEV_2:
			double A = Math.sqrt(2);

			Complex[] zk = new Complex[n];
			Complex[] pk = new Complex[n];

			double gamma = Math.pow(A + Math.sqrt(A * A - 1), 1. / n);

			for (int k = 1; k <= n; k++) {
				double arg = Math.PI * (2 * k - 1) / 2. / n;

				zk[k - 1] = new Complex(0, wc / Math.cos(arg));

				double alpha = -(gamma - 1 / gamma) * Math.sin(arg) / 2.;
				double betha = (gamma + 1 / gamma) * Math.cos(arg) / 2.;

				double a2b2 = Math.hypot(alpha, betha);
				pk[k - 1] = new Complex(wc * alpha / a2b2, -wc * betha / a2b2);
			}

			Complex H0 = new Complex(1, 0);
			for (int k = 1; k <= n; k++)
				H0.mult(Complex.mult(-1., pk[k - 1]));
			for (int k = 1; k <= n - (n % 2 == 0 ? 0 : 1); k++)
				H0.div(Complex.mult(-1., zk[k - 1]));

			for (int i = 0; i < nc; i++) {
				Complex s = new Complex(0, w * i);

				Complex hs = new Complex(H0);

				for (int k = 1; k <= n - (n % 2 == 0 ? 0 : 1); k++)
					hs.mult(Complex.sub(s, zk[k - 1]));
				for (int k = 1; k <= n; k++)
					hs.div(Complex.sub(s, pk[k - 1]));

				out[i] = hs;
			}
			break;
		case BESSEL:

			Complex tn0 = ExtendedMathComplex.revBesselPol(new Complex(), n);
			double iw0 = 1 / wc;

			for (int i = 0; i < nc; i++) {
				Complex s = new Complex(0, w * i);

				Complex hs = Complex.div(tn0, ExtendedMathComplex.revBesselPol(Complex.mult(iw0, s), n));

				out[i] = hs;
			}

			break;
		case ELLIPTIC:
			// TODO

			break;
		case SPECIAL:
			// QUE MERDA É ESSE BUTTERWORTH COM SINAL TROCADO E PÓLO GIRADO?!

			sk = new Complex[n];

			for (int k = 0; k < sk.length; k++)
				sk[k] = new Complex(wc, (2 * k + n - 1) * Math.PI / (2 * n), false);

			for (int i = 0; i < nc; i++) {
				Complex s = new Complex(0, w * i);

				Complex hs = new Complex(1, 0);
				for (int k = 0; k < n; k++)
					hs.mult(Complex.mult(1 / wc, Complex.sub(s, sk[k])));
				hs = Complex.mult(-1, Complex.inv(hs));

				out[i] = hs;
			}
			break;
		}

		Vec.normatize(out, 2); // normatiza

		return out;
	}

	public Complex[] getZeros() {
		int n = (int) params[0];
		double wc = (double) params[1];

		Complex[] out = new Complex[n];

		switch (type) {
		case BUTTERWORTH:
			// TODO
			break;
		case CHEBYSHEV_1:
			// TODO
			break;
		case CHEBYSHEV_2:
			for (int k = 1; k <= n; k++) {
				double arg = Math.PI * (2 * k - 1) / 2. / n;

				out[k - 1] = new Complex(0, wc / Math.cos(arg));
			}
			break;
		case BESSEL:
			// TODO

			break;
		case ELLIPTIC:
			// TODO

			break;
		case SPECIAL:
			// TODO

			break;
		}
		return out;
	}

	public Complex[] getPoles() {
		int n = (int) params[0];
		double wc = (double) params[1];

		Complex[] out = new Complex[n];

		switch (type) {
		case BUTTERWORTH:
			// TODO
			break;
		case CHEBYSHEV_1:
			// TODO
			break;
		case CHEBYSHEV_2:
			double gamma = Math.pow(A_DEF + Math.sqrt(A_DEF * A_DEF - 1), 1. / n);

			for (int k = 1; k <= n; k++) {
				double arg = Math.PI * (2 * k - 1) / 2. / n;

				double alpha = -(gamma - 1 / gamma) * Math.sin(arg) / 2.;
				double betha = (gamma + 1 / gamma) * Math.cos(arg) / 2.;

				double a2b2 = Math.hypot(alpha, betha);
				out[k - 1] = new Complex(wc * alpha / a2b2, -wc * betha / a2b2);
			}
			break;
		case BESSEL:
			// TODO

			break;
		case ELLIPTIC:
			// TODO

			break;
		case SPECIAL:
			// TODO

			break;
		}
		return out;
	}

	/**
	 * Função que aplica o filtro sobre um espectro
	 * 
	 * @param w  frequência fundamental (menor frequência/distância entre duas
	 *           componentes). Está associada com o tamanho da janela amostral
	 * @param cn espectro (vetor de números complexos para cada um dos componentes
	 *           espectrais)
	 * @return vetor com os componentes espectrais modificadas
	 */
	public Complex[] apply(double w, Complex[] cn) {
		Complex[] out = new Complex[cn.length];
		Complex[] fn = get(w, cn.length); // norma 1, por definição
		for (int i = 0; i < cn.length; i++) { // para cada coordenada
			out[i] = new Complex();
			for (int j = 0; j < cn.length; j++)
				out[i].sum(Complex.mult(cn[j], Complex.conj(fn[j])));
			out[i].mult(fn[i]);
		}
		return out;
	}

	public double[] apply(double w, double[] cn) {
		double[] out = new double[cn.length];
		Complex[] fn = get(w, cn.length); // norma 1, por definição

		for (int i = 0; i < cn.length; i++) {
			for (int j = 0; j < cn.length; j++)
				out[i] += (cn[j] * fn[j].getRe());
			out[i] *= fn[i].getRe();
		}

		return out;
	}
}

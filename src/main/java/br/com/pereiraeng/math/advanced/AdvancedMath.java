package br.com.pereiraeng.math.advanced;

import br.com.pereiraeng.core.ExtendedMath;

public class AdvancedMath {

	

	// Bessel de tipo I

	private static final int BESSEL_INFINITE_TERMS = 13;

	/**
	 * Função que calcula o valor da função de Bessel de tipo I de uma dada ordem
	 * para um dado valor (J<sub>&#x03B1;</sub>)
	 * 
	 * @param x     argumento da função
	 * @param alpha ordem da função
	 * @return valor da função
	 */
	public static double bessel(double x, double alpha) {
		return bessel(x, alpha, true);
	}

	private static double bessel(double x, double alpha, boolean alternate) {
		double out = 0.;
		for (int m = 0; m < BESSEL_INFINITE_TERMS; m++) {
			double p = Math.pow(x / 2, 2 * m + alpha) / (ExtendedMath.fatorial(m) * ExtendedMath.gama(m + 1 + alpha));
			if (alternate && m % 2 == 1)
				p *= -1.;
			out += p;
		}
		return out;
	}

	// Neumann (a.k.a. Bessel de tipo II)

	/**
	 * Função que retorna o valor da função de Neumann (também conhecida como função
	 * de Bessel de tipo II) de uma dada ordem para um dado valor
	 * (Y<sub>&#x03B1;</sub>)
	 * 
	 * @param x     argumento da função
	 * @param alpha ordem da função
	 * @return valor da função
	 */
	public static double neumann(double x, double alpha) {
		if (((int) alpha) == alpha)
			return neumannIntegerOrder(x, (int) alpha);
		else
			return (bessel(x, alpha) * Math.cos(alpha * Math.PI) - bessel(x, -alpha)) / Math.sin(alpha * Math.PI);
	}

	private static double neumannIntegerOrder(double x, int alpha) {
		if (alpha == 0 || alpha == 1) {
			return neumann01(x, alpha);
		} else {
			double tox = 2. / x;
			double yn = neumann01(x, 1), ynl1 = neumann01(x, 0), out = 0;
			for (int j = 1; j < alpha; j++) {
				out = j * tox * yn - ynl1;
				ynl1 = yn;
				yn = out;
			}
			return out;
		}
	}

	private static double neumann01(double x, int alpha) {
		final double[][] p = { { 1., -.1098628627e-2, .2734510407e-4, -.2073370639e-5, .2093887211e-6 },
				{ 1., .183105e-2, -.3516396496e-4, .2457520174e-5, -.240337019e-6 } },
				q = { { -.1562499995e-1, .1430488765e-3, -.6911147651e-5, .7621095161e-6, -.934945152e-7 },
						{ .04687499995, -.2002690873e-3, .8449199096e-5, -.88228987e-6, .105787412e-6 } },
				r = { { -2957821389., 7062834065., -512359803.6, 10879881.29, -86327.92757, 228.4622733 },
						{ -.4900604943e13, .1275274390e13, -.5153438139e11, .7349264551e9, -.4237922726e7,
								.8511937935e4 } },
				s = { { 40076544269., 745249964.8, 7189466.438, 47447.26470, 226.1030244, 1. }, { .2499580570e14,
						.4244419664e12, .3733650367e10, .2245904002e8, .1020426050e6, .3549632885e3, 1. } };

		if (x < 8) {
			double y = x * x, num = r[alpha][5], dem = (alpha == 0) ? s[alpha][5] : (s[alpha][5] + y * s[alpha][6]);
			for (int i = 4; i >= 0; i--) {
				num = (r[alpha][i] + y * num);
				dem = (s[alpha][i] + y * dem);
			}
			return (alpha == 0 ? 1 : x) * (num / dem)
					+ .636619772 * (bessel(x, alpha) * Math.log(x) - (alpha == 0 ? 0 : 1 / x));
		} else {
			double y = Math.pow(8. / x, 2), xx = x - (2 * alpha + 1) * Math.PI / 4, py = p[alpha][4], qy = q[alpha][4];
			for (int i = 3; i >= 0; i--) {
				py = (p[alpha][i] + y * py);
				qy = (q[alpha][i] + y * qy);
			}
			return Math.sqrt(.636619772 / x) * (Math.sin(xx) * py + (alpha == 0 ? 1 : 8. / x) * Math.cos(xx) * qy);
		}
	}

	// Bessel modificada de tipo I

	/**
	 * Função que calcula o valor da função de Bessel modificada de tipo I de uma
	 * dada ordem para um dado valor (I<sub>&#x03B1;</sub>)
	 * 
	 * @param x     argumento da função
	 * @param alpha ordem da função
	 * @return valor da função
	 */
	public static double besselI(double x, double alpha) {
		return bessel(x, alpha, false);
	}

	// Bessel modificada de tipo II

	/**
	 * Função que calcula o valor da função de Bessel modificada de tipo II de uma
	 * dada ordem para um dado valor (K<sub>&#x03B1;</sub>)
	 * 
	 * @param x     argumento da função
	 * @param alpha ordem da função
	 * @return valor da função
	 */
	public static double besselK(double x, double alpha) {
		if (((int) alpha) == alpha)
			return besselKintegerOrder(x, (int) alpha);
		else
			return ExtendedMath.PI_2 * (besselI(x, -alpha) - besselI(x, alpha)) / Math.sin(alpha * Math.PI);
	}

	private static double besselKintegerOrder(double x, int n) {
		if (n == 0)
			return besselK0(x);
		if (n == 1)
			return besselK1(x);

		double tox = 2 / x;
		double bkm = besselK0(x);
		double out = besselK1(x);
		for (int j = 1; j < n; j++) {
			double bkp = bkm + j * tox * out;
			bkm = out;
			out = bkp;
		}
		return out;
	}

	/**
	 * <p>
	 * Função que calcula a função de Bessel modificada do tipo II de ordem 0
	 * </p>
	 * 
	 * M.Abramowitz and I.A.Stegun, Handbook of Mathematical Functions, Mathematics
	 * Series vol. 55 (1964), Washington.
	 * 
	 * @param x variável da função
	 * @return valor da função
	 */
	private static double besselK0(double x) {
		final double p1 = -0.57721566, p2 = 0.42278420, p3 = 0.23069756, p4 = 3.488590e-2, p5 = 2.62698e-3,
				p6 = 1.0750e-4, p7 = 7.4e-6;
		final double q1 = 1.25331414, q2 = -7.832358e-2, q3 = 2.189568e-2, q4 = -1.062446e-2, q5 = 5.87872e-3,
				q6 = -2.51540e-3, q7 = 5.3208e-4;

		double out = 0;

		if (x <= 2) {
			double y = x * x / 4;
			out = (-Math.log(x / 2.) * besselI(x, 0))
					+ (p1 + y * (p2 + y * (p3 + y * (p4 + y * (p5 + y * (p6 + y * p7))))));
		} else {
			double y = 2 / x;
			out = (Math.exp(-x) / Math.sqrt(x)) * (q1 + y * (q2 + y * (q3 + y * (q4 + y * (q5 + y * (q6 + y * q7))))));
		}
		return out;
	}

	/**
	 * <p>
	 * Função que calcula a função de Bessel modificada do tipo II de ordem 1
	 * </p>
	 * 
	 * M.Abramowitz and I.A.Stegun, Handbook of Mathematical Functions, Applied
	 * Mathematics Series vol. 55 (1964), Washington.
	 * 
	 * @param x variável da função
	 * @return valor da função
	 */
	private static double besselK1(double x) {
		final double p1 = 1., p2 = 0.15443144, p3 = -0.67278579, p4 = -0.18156897, p5 = -1.919402e-2, p6 = -1.10404e-3,
				p7 = -4.686e-5;
		final double q1 = 1.25331414, q2 = 0.23498619, q3 = -3.655620e-2, q4 = 1.504268e-2, q5 = -7.80353e-3,
				q6 = 3.25614e-3, q7 = -6.8245e-4;

		double y = 0, result = 0;

		if (x <= 2) {
			y = x * x / 4;
			result = (Math.log(x / 2.) * besselI(x, 1))
					+ (1. / x) * (p1 + y * (p2 + y * (p3 + y * (p4 + y * (p5 + y * (p6 + y * p7))))));
		} else {
			y = 2 / x;
			result = (Math.exp(-x) / Math.sqrt(x))
					* (q1 + y * (q2 + y * (q3 + y * (q4 + y * (q5 + y * (q6 + y * q7))))));
		}
		return result;
	}

	// Kelvin-Bessel

	/**
	 * Parte real da função de Kelvin-Bessel
	 * 
	 * @param z  argumento
	 * @param nu ordem da função
	 * @return valor da função
	 */
	public static double ber(double z, double nu) {
		double out = 0.;
		for (int n = 0; n < BESSEL_INFINITE_TERMS; n++) {
			out += Math.cos((3. * nu / 4. + n / 2.) * Math.PI) * Math.pow(z / 2., 2. * n + nu)
					/ (ExtendedMath.fatorial(n) * ExtendedMath.gama(n + nu + 1.));
		}
		return out;
	}

	/**
	 * Parte imaginária da função de Kelvin-Bessel
	 * 
	 * @param z  argumento da função
	 * @param nu ordem da função
	 * @return valor da função
	 */
	public static double bei(double z, double nu) {
		double out = 0.;
		for (int n = 0; n < BESSEL_INFINITE_TERMS; n++) {
			out += Math.sin((3. * nu / 4. + n / 2.) * Math.PI) * Math.pow(z / 2., 2. * n + nu)
					/ (ExtendedMath.fatorial(n) * ExtendedMath.gama(n + nu + 1.));
		}
		return out;
	}

}

package br.com.pereiraeng.math.advanced.dsp;

import java.util.Iterator;
import java.util.Map.Entry;

import br.com.pereiraeng.math.Spline;
import br.com.pereiraeng.math.probability.ProbEstat;
import br.com.pereiraeng.math.timeseries.SrT;
import br.com.pereiraeng.core.ExtendedMath;
import br.com.pereiraeng.core.ReflectionUtils;
import br.com.pereiraeng.core.collections.ArrayUtils;

import java.util.TreeMap;

/**
 * Classe das funções de processamento digital de sinais
 * 
 * @author Philipe PEREIRA
 *
 */
public class DSP {

	/**
	 * <code>true</code> para encontrar o zero a partir da reta que passa pelos
	 * pontos de sinais opostos, <code>false</code> para calcular o spline
	 */
	public static final boolean ZERO_LINE = true;

	/**
	 * pontos utilizados para se calcular o {@link Spline}, (só é utilizado se
	 * {@link #ZERO_LINE} for <code>false</code>)
	 */
	public static final int SPLINE_POINTS = 4;

	/**
	 * Função que calcula frequência de um sinal
	 * 
	 * @param srt        sinais, representado por um registro que armazena várias
	 *                   medições para um dado instante de tempo
	 * @param pos        inteiro que indica qual a posição das medições dentro do
	 *                   registro contém o sinal cuja frequência será calculada
	 * @param timeFactor fator de escala temporal (a frequência calculada será
	 *                   dividida por este fator)
	 * @return tabela de dispersão ordenada que associa para instante de tempo (nas
	 *         unidades do registro de entrada), a frequência calculada do sinal
	 */
	public static <T extends Number> TreeMap<T, Float> getFreq(SrT<T> srt, int pos, double timeFactor) {
		TreeMap<T, Float> out = new TreeMap<>();

		Iterator<Entry<T, float[]>> it = srt.entrySet().iterator();
		Entry<T, float[]> e = it.next();

		T lastTime = e.getKey();
		float lastValue = e.getValue()[pos];

		boolean s0 = lastValue > 0;
		T lastZero = null;

		double[][] buffer = null;
		if (!DSP.ZERO_LINE) {
			buffer = new double[2][DSP.SPLINE_POINTS];
			buffer[0][DSP.SPLINE_POINTS - 1] = lastTime.doubleValue();
			buffer[1][DSP.SPLINE_POINTS - 1] = lastValue;
		}

		int c = 1;
		while (it.hasNext()) {
			e = it.next();

			T t = e.getKey();
			float v = e.getValue()[pos];

			boolean si = v > 0;
			double z = Double.NaN;

			if (DSP.ZERO_LINE) {
				// aproximar por uma reta
				if (si ^ s0)
					z = ExtendedMath.getZero(lastTime.doubleValue(), t.doubleValue(), lastValue, v);
			} else {
				// aproximar por um spline

				// buffer

				ArrayUtils.shiftedArray(buffer[0], -1);
				ArrayUtils.shiftedArray(buffer[1], -1);
				buffer[0][DSP.SPLINE_POINTS - 1] = t.doubleValue();
				buffer[1][DSP.SPLINE_POINTS - 1] = v;
				if (c > 0) {
					// enchendo
					c++;
					if (c == DSP.SPLINE_POINTS)
						c = 0;
				} else if (c == 0) {
					// cheio, esperando zero
					if (si ^ s0)
						c = -1;
				} else if (c < 0) {
					// cheio, achou o zero
					c--;

					if (-c == DSP.SPLINE_POINTS / 2) {
						// zero pronto para ser calculado
						double[] m = Spline.getSpline(buffer[0], buffer[1]);
						z = Spline.solve(buffer[0], buffer[1], m, /* POINTS / 2 */0);
						c = 0;
					}
				}
			}

			if (!Double.isNaN(z)) {
				// achou um novo zero!
				T newZero = ReflectionUtils.double2number(t, z);
				if (lastZero != null) {
					double f = .5 / ((newZero.doubleValue() - lastZero.doubleValue()) * timeFactor);
					T tf = ReflectionUtils.double2number(t, (newZero.doubleValue() + lastZero.doubleValue()) * .5);
					out.put(tf, (float) f);
				}

				s0 = si;
				lastZero = newZero;
			}

			lastTime = t;
			lastValue = v;
		}

		// TODO filtro passa baixa...

		DSP.filtroMedia(out, 10);

		return out;
	}

	// ================================ FILTRAGEM ================================

	/**
	 * 
	 * @param serie
	 * @param last  número de pontos usados para se calcular a média
	 */
	public static <T> void filtroMedia(TreeMap<T, Float> serie, final int last) {
		int c = 0;
		double[] buffer = new double[last];
		Iterator<Entry<T, Float>> it0 = serie.entrySet().iterator();
		while (it0.hasNext()) {
			Entry<T, Float> e0 = it0.next();

			ArrayUtils.shiftedArray(buffer, -1);
			buffer[last - 1] = e0.getValue();

			if (c >= 0) {
				// enchendo
				c++;
				it0.remove();
				if (c == last - 1)
					c = -1;
			} else if (c == -1) {
				// cheio
				e0.setValue((float) ProbEstat.media(buffer));
			}
		}
	}
}

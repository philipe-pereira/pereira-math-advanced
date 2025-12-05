package br.com.pereiraeng.math.advanced.statistics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import br.com.pereiraeng.math.timeseries.Reg;
import br.com.pereiraeng.math.timeseries.unit.Med;
import br.com.pereiraeng.core.TimeUtils;

/**
 * Classe do objeto que traz as informações sobre as ultrapassagens em um dado
 * registro de valores
 * 
 * @author Philipe PEREIRA
 *
 */
public class Ult implements Sta, Comparable<Ult> {

	private int begin;
	private int end;

	private int timeMax;
	private float extremeValue;

	private final boolean above;
	private final float threshold;

	/**
	 * 
	 * @param above        <code>true</code> para uma ultrapassagem acima de um
	 *                     valor, <code>false</code> para uma abaixo de um valor
	 * @param threshold    percentagem que foi ultrapassada
	 * @param initialValue valor inicial
	 * @param begin        instante inicial
	 */
	public Ult(boolean above, float threshold, float initialValue, int begin) {
		this(above, threshold, begin, begin, initialValue, Integer.MAX_VALUE);
	}

	/**
	 * 
	 * @param above      <code>true</code> para um valor limite máximo,
	 *                   <code>false</code> para um valor limite mínimo
	 * @param extremeMed medição extrema
	 */
	public Ult(boolean above, Med extremeMed) {
		this(above, Float.NaN, TimeUtils.toInt(extremeMed.getTime()), extremeMed.getValue());
	}

	private Ult(boolean above, float threshold, int timeMax, float extremeValue) {
		this(above, threshold, timeMax, timeMax, extremeValue, timeMax);
	}

	/**
	 * 
	 * @param above        <code>true</code> para uma ultrapassagem acima de um
	 *                     valor, <code>false</code> para uma abaixo de um valor
	 * @param threshold    percentagem que foi ultrapassada
	 * @param begin        instante inicial
	 * @param timeMax      instante do valor limite
	 * @param extremeValue valor limite
	 * @param end          instante final
	 */
	public Ult(boolean above, float threshold, int begin, int timeMax, float extremeValue, int end) {
		this.above = above;
		this.threshold = threshold;
		this.begin = begin;
		this.timeMax = timeMax;
		this.extremeValue = extremeValue;
		this.end = end;
	}

	public float getThreshold() {
		return threshold;
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}

	/**
	 * Função que retorna a largura da ultrapassagem
	 * 
	 * @return tempo da ultrapassagem, em minutos
	 */
	public int getWidth() {
		return (this.end - this.begin) / 60;
	}

	public int getTimeMax() {
		return timeMax;
	}

	public float getExtremeValue() {
		return extremeValue;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	/**
	 * Função que indica um possível candidato a valor extremo a ser verificado
	 * 
	 * @param time  instante de tempo do valor monitorado
	 * @param value candidato a limite do valor monitorado
	 */
	public void setExtreme(int time, float value) {
		if (above ? value > this.extremeValue : value < this.extremeValue) {
			this.timeMax = time;
			this.extremeValue = value;
		}
	}

	public static List<Ult> listUlt(Reg reg, int pos, float ref, final boolean above, final float... thresholds) {
		return listUlt(reg, pos, ref, above, false, thresholds);
	}

	/**
	 * 
	 * @param pos        posição do registro onde serão analisados os dados
	 * @param ref        valor de referência
	 * @param above      <code>true</code> para contar valores acima da referência,
	 *                   <code>false</code> para abaixo
	 * @param thresholds porcentagens do valor de referência em que conta a
	 *                   ultrapassagem
	 * @return
	 */
	public static List<Ult> listUlt(Reg reg, int pos, float ref, final boolean above, boolean abs,
			final float... thresholds) {
		List<Ult> out = new LinkedList<>();
		if (reg.size() == 0)
			return out;

		Ult[] us = new Ult[thresholds.length];

		float[] t = new float[thresholds.length + 2];
		t[0] = Float.NEGATIVE_INFINITY;
		for (int i = 0; i < thresholds.length; i++)
			t[i + 1] = thresholds[i] * ref;
		t[thresholds.length + 1] = Float.POSITIVE_INFINITY;

		// iterator
		Iterator<Entry<Integer, float[]>> it = reg.entrySet().iterator();

		// primeira iteração
		Entry<Integer, float[]> e = it.next();

		float v = e.getValue()[pos];
		if (abs)
			v = Math.abs(v);

		int r = -1;
		if (above) {
			for (int i = 0; i < t.length - 1; i++) {
				if (v >= t[i] && v < t[i + 1]) {
					r = i;
					break;
				} else {
					us[i] = new Ult(above, thresholds[i], v, e.getKey());
					out.add(us[i]);
				}
			}
		} else {
			for (int i = t.length - 2; i >= 0; i--) {
				if (v >= t[i] && v < t[i + 1]) {
					r = i;
					break;
				} else {
					us[r] = new Ult(above, thresholds[r], v, e.getKey());
					out.add(us[r]);
				}
			}
		}

		float[] range = new float[] { t[r], t[r + 1] };
		while (it.hasNext()) {
			e = it.next();

			v = e.getValue()[pos];
			if (abs)
				v = Math.abs(v);

			if (v > range[1]) {
				// ultrapassagem positiva
				if (above) {
					us[r] = new Ult(above, thresholds[r], v, e.getKey());
					out.add(us[r]);
				} else {
					us[r].setEnd(e.getKey());
					us[r] = null;
				}
				r++;
				range[0] = range[1];
				range[1] = t[r + 1];
			} else if (v < range[0]) {
				// ultrapassagem negativa
				r--;
				if (above) {
					us[r].setEnd(e.getKey());
					us[r] = null;
				} else {
					us[r] = new Ult(above, thresholds[r], v, e.getKey());
					out.add(us[r]);
				}
				range[1] = range[0];
				range[0] = t[r];
			} else {
				// se não ultrapassou para cima nem para baixo, inspeciona para
				// ver se tem algum máximo e/ou mínimo
				if (above) {
					for (int i = 0; i < r; i++)
						us[i].setExtreme(e.getKey(), v);
				} else {
					for (int i = r; i < thresholds.length; i++)
						us[i].setExtreme(e.getKey(), v);
				}
			}
		}
		for (int i = 0; i < thresholds.length; i++)
			if (us[i] != null)
				us[i].setEnd(reg.lastKey());

		Collections.sort(out);
		return out;
	}

	public Med getMedLimit() {
		return new Med(TimeUtils.toCalendar(this.timeMax), this.extremeValue);
	}

	@Override
	public String toString() {
		return begin + "\t" + threshold + "\t" + this.extremeValue + "\t" + end;
	}

	@Override
	public int compareTo(Ult anotherUlt) {
		return Float.compare(this.getThreshold(), anotherUlt.getThreshold());
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject)
			return true;
		if (anObject instanceof Ult) {
			Ult t = (Ult) anObject;

			return begin == t.getBegin() && end == t.getEnd() && timeMax == t.getTimeMax()
					&& extremeValue == t.getExtremeValue() && threshold == t.getThreshold();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return begin + end + timeMax + (int) extremeValue;
	}

	// -------------------------- ORGANIZAR --------------------------

	public static boolean hasUlt(List<Ult> us) {
		if (us.size() == 1)
			return !Float.isNaN(us.iterator().next().getThreshold());
		else
			return true;
	}

	/**
	 * Função que agrupa as ultrapassagens de um valor de refência em uma tabela que
	 * remete para cada percentual a lista de ultrapassagens
	 * 
	 * @param us lista de ultrapassagens (com diferentes percentuais)
	 * @return tabela de dispersão que associa para cada percentual a lista de
	 *         ultrapassagens
	 */
	public static Map<Float, List<Ult>> group(List<? extends Sta> us) {
		Map<Float, List<Ult>> out = new HashMap<>();
		for (Sta r : us) {
			Ult u = (Ult) r;
			float v = u.getThreshold();
			List<Ult> l = out.get(v);
			if (l == null)
				out.put(v, l = new LinkedList<>());
			l.add(u);
		}
		return out;
	}
}
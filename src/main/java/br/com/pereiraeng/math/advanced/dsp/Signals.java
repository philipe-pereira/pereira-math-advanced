package br.com.pereiraeng.math.advanced.dsp;

import java.util.LinkedList;
import java.util.List;

import br.com.pereiraeng.core.ExtendedMath;

/**
 * 
 * @author Philipe PEREIRA
 *
 */
public enum Signals {
	/**
	 * sawtooth
	 */
	ST(true),
	/**
	 * reverse sawtooth
	 */
	STI(true),
	/**
	 * triangular
	 */
	TRI(true);

	private boolean carrier;

	private Signals(boolean carrier) {
		this.carrier = carrier;
	}

	/**
	 * Função que retorna o valor de um onda triangular
	 * 
	 * @param carrier tipo de onda triangular
	 * @param fs      frequência da onda triangular, em Hertz
	 * @param t       tempo, em segundos
	 * @return valor da onda
	 */
	public static double tri(Signals carrier, double fs, double t) {
		double c = 0.;
		switch (carrier) {
		case ST:
			c = 2 * (fs * t - Math.floor(0.5 + fs * t));
			break;
		case STI:
			c = 2 * (-fs * t + Math.floor(0.5 + fs * t));
			break;
		case TRI:
			c = ExtendedMath.TWO_BY_PI * Math.asin(Math.sin(2 * Math.PI * fs * t)); // TODO aff, tudo isso...
			break;
		}
		return c;
	}

	/**
	 * Sinais tipicamente usados na eletrônica de potência para serem portadoras na
	 * modulação por largura de pulso
	 * 
	 * @return
	 */
	public static Signals[] carriers() {
		List<Signals> out = new LinkedList<>();
		for (Signals s : Signals.values())
			if (s.carrier)
				out.add(s);
		return out.toArray(new Signals[out.size()]);
	}

	public String getPt() {
		switch (this) {
		case ST:
			return "Dente de serra";
		case STI:
			return "Dente de serra invertida";
		case TRI:
			return "Triangular";
		}
		return null;
	}
}

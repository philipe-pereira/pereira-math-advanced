package br.com.pereiraeng.math.advanced.statistics;

import br.com.pereiraeng.math.probability.ProbEstat;
import br.com.pereiraeng.math.advanced.dsp.PairFourierSerie;

public class Prf implements Sta {

	private double media;
	private double dpa;
	private double[] dct;
	private int[][] freeze;

	public double getMedia() {
		return media;
	}

	public double getDpa() {
		return dpa;
	}

	public double[] getDct() {
		return dct;
	}

	public int[][] getFreeze() {
		return freeze;
	}

	public double[] getNota(double mi, double ms, double dpa0, double dpa1) {
		double notaM = 0.;
		double ma = getMedia();
		if (ma < mi && ma > 0.) // subtensão
			notaM = (ma - mi) / dpa0;
		else if (ma > ms) // sobretensão
			notaM = (ma - ms) / dpa0;
		else
			notaM = 0.;

		// -----------------------------------

		double notaD = 0.;
		double dpa = getDpa();
		if (dpa == 0.)
			notaD = 10.;
		else if (dpa > dpa0)
			notaD = (dpa - dpa0) / dpa0;
		else if (dpa < dpa1)
			notaD = Math.log10(dpa);
		else
			notaD = 0.;

		// -----------------------------------

		double notaF = 0.;
		double[] fourier = getDct();
		if (fourier != null)
			for (int i = 1; i < fourier.length; i++)
				notaF += Math.pow(fourier[i], 2.);

		if (notaF == 0.)
			notaF = 10.;
		else {
			notaF = Math.log10(Math.sqrt(notaF));
			if (notaF > -4. && notaF < 0.)
				notaF = 0.;
		}

		// -----------------------------------

		double notaC = 0.;
		int[][] freeze = getFreeze();
		if (freeze != null)
			for (int i = 0; i < freeze.length; i++)
				notaC += freeze[i][1];

		if (notaC > 0.)
			notaC /= 4.;

		return new double[] { notaM, notaD, notaF, notaC };
	}

	public void setTimeValues(double[] times, double[] values) {
		double media = ProbEstat.media(values);
		this.media = media;
		this.dpa = ProbEstat.desvioPadraoAmostral(values, media);
		this.dct = PairFourierSerie.nudct(times, values, 900.);
		this.freeze = ProbEstat.mapConst(16, times, values);
	}
}

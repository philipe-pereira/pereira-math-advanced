package br.com.pereiraeng.math.advanced.dsp;

import br.com.pereiraeng.core.ExtendedMath;

public class Windowing {

	private WindowType type;

	public enum WindowType {
		HANNING, HAMMING;
	}

	public Windowing(WindowType type) {
		this.type = type;
	}

	// ---------------- regularly espaced ----------------

	public double[] apply(double[] xn) {
		double[] out = new double[xn.length];
		switch (type) {
		case HANNING:
			for (int i = 0; i < xn.length; i++)
				out[i] = xn[i] * ExtendedMath.haversin(ExtendedMath.TWO_PI * i / (xn.length - 1));
			break;
		case HAMMING:
			for (int i = 0; i < xn.length; i++)
				out[i] = xn[i] * (0.54 - 0.46 * Math.cos(ExtendedMath.TWO_PI * i / (xn.length - 1)));
			break;
		}
		return out;
	}

	public double[] reverse(double[] xn) {
		double[] out = new double[xn.length];
		switch (type) {
		case HANNING:
			for (int i = 0; i < xn.length; i++)
				out[i] = xn[i] / ExtendedMath.haversin(ExtendedMath.TWO_PI * i / (xn.length - 1));
			break;
		case HAMMING:
			for (int i = 0; i < xn.length; i++)
				out[i] = xn[i] / (0.54 - 0.46 * Math.cos(ExtendedMath.TWO_PI * i / (xn.length - 1)));
			break;
		}
		return out;
	}

	// ---------------- non-uniform espaced ----------------

	public double[] apply(double[] tn, double[] xn, double T) {
		double[] out = new double[xn.length];
		switch (type) {
		case HANNING:
			for (int i = 0; i < tn.length; i++)
				out[i] = xn[i] * ExtendedMath.haversin(ExtendedMath.TWO_PI * (tn[i] - tn[0]) / T);
			break;
		case HAMMING:
			for (int i = 0; i < xn.length; i++)
				out[i] = xn[i] * (0.54 - 0.46 * Math.cos(ExtendedMath.TWO_PI * (tn[i] - tn[0]) / T));
			break;
		}
		return out;
	}

	public double[] reverse(double[] tn, double[] xn, double T) {
		double[] out = new double[xn.length];
		switch (type) {
		case HANNING:
			for (int i = 0; i < tn.length; i++)
				out[i] = xn[i] / ExtendedMath.haversin(ExtendedMath.TWO_PI * (tn[i] - tn[0]) / T);
			break;
		case HAMMING:
			for (int i = 0; i < xn.length; i++)
				out[i] = xn[i] / (0.54 - 0.46 * Math.cos(ExtendedMath.TWO_PI * (tn[i] - tn[0]) / T));
			break;
		}
		return out;
	}
}

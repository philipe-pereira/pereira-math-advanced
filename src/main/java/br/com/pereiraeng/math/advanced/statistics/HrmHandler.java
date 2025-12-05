package br.com.pereiraeng.math.advanced.statistics;

import java.util.Map;

import br.com.pereiraeng.math.advanced.dsp.FourierSerie;

public class HrmHandler<K> extends EstHandler<K> {

	private final Map<K, FourierSerie[]> series;

	// transformação afim sobre o argumento

	/**
	 * coeficiente linear - instante inicial
	 */
	private final double offset;

	/**
	 * coeficiente angular - frequência/período
	 */
	private final double scale;

	public HrmHandler(Map<K, FourierSerie[]> series, double offset, double scale) {
		this.series = series;
		this.offset = offset;
		this.scale = scale;
	}
	
	public Map<K, FourierSerie[]> getSeries() {
		return series;
	}
	
	public double getScale() {
		return scale;
	}
	
	public double getOffset() {
		return offset;
	}

	// h

	@Override
	public double[][] get(double c) {
		// TODO Auto-generated method stub
		return null;
	}

	// v

	@Override
	public double[][] get(K c) {
		// TODO Auto-generated method stub
		return null;
	}

}

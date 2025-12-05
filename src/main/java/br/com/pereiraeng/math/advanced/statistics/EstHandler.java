package br.com.pereiraeng.math.advanced.statistics;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import br.com.pereiraeng.core.collections.ArrayUtils;

/**
 * Classe do objeto que retorna os dados feitos a partir de uma análise
 * estatística de uma série temporal
 * 
 * @author Philipe PEREIRA
 *
 * @param <K> indexador das séries temporais
 */
public abstract class EstHandler<K> {

	// horizontal: escolhe-se previamente as tags e vai consultando os diferentes
	// instantes de tempo

	protected List<K> h;

	public void setH(List<K> h) {
		this.h = h;
	}

	/**
	 * Função que retorna, para todos as chaves previamente designadas, os valores
	 * da função para um dado valor no argumento
	 * 
	 * @param c argumento
	 * @return matriz com um número de linhas igual ao de chaves e 2*m valores
	 *         (valor da função e erro)
	 */
	public abstract double[][] get(double c);

	public Iterator<K> getKeys() {
		return h.iterator();
	}

	// vertical: escolhe-se os instantes de tempo e vai consultando as diferentes
	// tags

	protected double[] ns;

	/**
	 * Função que retorna, para todos os valores do argumento previamente
	 * designados, os valores da função para uma dada chave
	 * 
	 * @param c chave
	 * @return matriz com um número de linhas igual ao de instantes e 2*m valores
	 *         (valor da função e erro)
	 */
	public abstract double[][] get(K c);

	public void setV(TreeSet<? extends Number> ns) {
		this.ns = new double[ns.size()];
		int i = 0;
		for (Number ni : ns)
			this.ns[i++] = ni.doubleValue();
	}

	public Iterator<Double> getTimes() {
		return Arrays.asList(ArrayUtils.box(this.ns)).iterator();
	}
}

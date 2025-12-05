package br.com.pereiraeng.math.advanced.geometry;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.pereiraeng.math.geometry.Circle;
import br.com.pereiraeng.math.geometry.Geom;
import br.com.pereiraeng.math.geometry.Triangle;

/**
 * Classe que contém funções necessárias ao processo de triangularização de
 * Delaunay
 * 
 * @author Philipe PEREIRA
 *
 */
public class Delaunay {

	/**
	 * Função que procede com a triangularização de Delaunay de um conjunto de
	 * pontos de <strong>precisão float</strong>.
	 * 
	 * @param points pontos Float
	 * @return relação de trincas dos pontos Float
	 */
	public static <F extends Point2D.Float> Collection<List<F>> delaunayTriangulationF(Set<? extends F> points) {
		// faz-se uma tabela intermediária que associa os pontos aos vértices
		// (eles não podem ser o mesmo objeto pois as coordenadas são
		// Point2D.Float, enquanto que a função Delaunay só aceita
		// Point2D.Double)
		Map<Point2D.Double, F> d2f = new HashMap<>();
		for (F p : points)
			d2f.put(new Point2D.Double(p.getX(), p.getY()), p);

		// faz-se a triangularização
		Set<Triangle> ts = Delaunay.delaunayTriangulation(d2f.keySet());

		// desfaz-se a operação inicial
		Collection<List<F>> out = new LinkedList<>();
		for (Triangle t : ts)
			out.add(Arrays.asList(d2f.get(t.getVertice(0)), d2f.get(t.getVertice(1)), d2f.get(t.getVertice(2))));
		return out;
	}

	/**
	 * Função que procede com a triangularização de Delaunay de um conjunto de
	 * pontos. O algoritmo empregado está descrito no capítulo 9 do livro
	 * <a href= "http://www.cs.uu.nl/geobook/">Computational Geometry: Algorithms
	 * and Applications</a>.<br>
	 * 
	 * O algoritmo funciona para todos os casos, exceto um: <strong>não pode haver
	 * mais de um ponto com a ordenada máxima</strong> (esta restrição também se
	 * aplica ao algoritmo de
	 * {@link Voronoi#voronoiDiagram(Set, java.awt.geom.Point2D.Double[]) Voronoi})
	 * 
	 * @param points conjunto de pontos
	 * @return conjunto de triângulos obtidos a partir da triangularização de
	 *         Delaunay
	 */
	public static Set<Triangle> delaunayTriangulation(Set<Point2D.Double> points) {
		Triangle t = triangularHull(points);
		return delaunayTriangulation(points, t.getVertice(1), t.getVertice(2));
	}

	private static Triangle triangularHull(Set<Point2D.Double> points) {
		// maior ordenada
		Point2D.Double pM = Geom.getExtreme(points, 1);
		// envoltória convexa
		List<Point2D.Double> ch = ConvexHull.quickhull(points);
		// posição do ponto de maior ordenada na envoltória
		int pos = ch.indexOf(pM);

		// direções
		Point2D.Double pM1 = ch.get(pos == 0 ? ch.size() - 1 : pos - 1);
		Point2D.Double pM2 = ch.get(pos == ch.size() - 1 ? 0 : pos + 1);

		// menor ordenada
		double ymm = Geom.getExtreme(points, 3).y - 1.;
		double xmm1 = Geom.retaX(pM, pM1, ymm);
		double xmm2 = Geom.retaX(pM, pM2, ymm);
		if (xmm1 > xmm2) {
			xmm1 += 1.;
			xmm2 -= 1.;
		} else {
			xmm2 += 1.;
			xmm1 -= 1.;
		}

		Triangle t = new Triangle(pM, new Point2D.Double(xmm1, ymm), new Point2D.Double(xmm2, ymm));

		// conferir se todos os pontos estão dentro do triângulo inicial
		for (Point2D.Double p : points) {
			if (p.equals(pM))
				continue;
			if (!t.hasInside(p))
				System.err.println("Os pontos iniciais propostos não formam um triângulo que contém todos os pontos.");
		}

		return t;
	}

	/**
	 * 
	 * @param points
	 * 
	 * @param pm1    um dos pontos iniciais distantes
	 * @param pm2    outro ponto inicial distante
	 * @return
	 */
	private static Set<Triangle> delaunayTriangulation(Set<? extends Point2D.Double> points, Point2D.Double pm1,
			Point2D.Double pm2) {
		// o terceiro ponto inicial é aquele que pois a maior ordenada
		Point2D.Double pm0 = Geom.getExtreme(points, 1);

		// triângulo inicial
		Triangle t = new Triangle(pm1, pm2, pm0);

		Set<Triangle> triangulation = new HashSet<>();
		triangulation.add(t);

		for (Point2D.Double p : points) {
			// pular o ponto inicial
			if (p.equals(pm0))
				continue;

			t = getContainerTriangle(p, triangulation);
			if (t == null) {
//				// ponto que não está no interior de nenhum triângulo... ele está na aresta dois
//				// deles
				Triangle[] q = new Triangle[2];
				int[] edges = getContainerQuadrilateral(p, triangulation, q);

				Triangle t1 = new Triangle(p, q[0].getVertice(edges[0] + 1), q[0].getVertice(edges[0] + 2));
				triangulation.add(t1);

				Triangle t2 = new Triangle(p, q[1].getVertice(edges[1] + 1), q[1].getVertice(edges[1] + 2));
				triangulation.add(t2);

				// tira o triângulo da lista...
				triangulation.remove(q[0]);
				// ... o altera...
				q[0].setVertice(edges[0] + 1, p);
				// ... e devolve para a lista
				triangulation.add(q[0]);

				// tira o triângulo da lista...
				triangulation.remove(q[1]);
				// ... o altera...
				q[1].setVertice(edges[1] + 1, p);
				// ... e devolve para a lista
				triangulation.add(q[1]);
			} else {
				Point2D.Double p1 = t.getVertice(0), p2 = t.getVertice(1), p3 = t.getVertice(2);

				triangulation.add(new Triangle(p, p2, p3));
				triangulation.add(new Triangle(p, p1, p3));

				// tira o triângulo da lista...
				triangulation.remove(t);
				// ... o altera...
				t.setVertice(2, p);
				// ... e devolve para a lista
				triangulation.add(t);

				// legalizar novos triângulos
				legalizeEdge(p, new Point2D.Double[] { p1, p2 }, triangulation);
				legalizeEdge(p, new Point2D.Double[] { p2, p3 }, triangulation);
				legalizeEdge(p, new Point2D.Double[] { p1, p3 }, triangulation);
			}
		}

		// remover triângulo que possuam os pontos incluídos no começo
		Iterator<Triangle> it = triangulation.iterator();
		while (it.hasNext()) {
			t = it.next();
			if (t.containsVertice(pm1) || t.containsVertice(pm2))
				it.remove();
		}

		return triangulation;
	}

	/**
	 * Função que determina qual dos triângulo de um conjunto é aquele que contém um
	 * dado ponto
	 * 
	 * @param p    ponto a ser analisado
	 * @param tris conjunto de triângulos
	 * @return triângulo que contém em seu interior o ponto dado
	 */
	private static Triangle getContainerTriangle(Point2D.Double p, Collection<Triangle> tris) {
		for (Triangle t : tris)
			if (t.hasInside(p))
				return t;
		return null;
	}

//	/**
//	 * Função que determina quais dos triângulos tem uma aresta em comum que contém
//	 * um dado ponto
//	 * 
//	 * @param p             ponto a ser analisado
//	 * @param triangulation conjunto de triângulos
//	 * @param out           vetor com duas dimensões a ser preenchido com o par de
//	 *                      triângulos que compartilham uma aresta que contém o
//	 *                      ponto
//	 * @return aresta em comum aos dois triângulos
//	 */
	private static int[] getContainerQuadrilateral(Point2D.Double p, Collection<Triangle> tris, Triangle[] out) {
		int[] edges = new int[] { -1, -1 };
		Point2D.Double[] commonEdge = null;

		// achar um dos triângulos
		for (Triangle t : tris) {
			int e = t.belongsToAEdge(p);
			if (e >= 0) {
				out[0] = t;
				edges[0] = e;
				commonEdge = out[0].getEdge(e);
				break;
			}
		}

		if (edges[0] < 0)
			return null;

		// achar o outro triângulo
		for (Triangle t : tris) {
			if (t != out[0]) {
				int e = t.hasEdge(commonEdge);
				if (e >= 0) {
					out[1] = t;
					edges[1] = e;
					break;
				}
			}
		}

		if (edges[1] < 0)
			return null;

		return edges;
	}

	/**
	 * Função que faz mudanças nos triângulos adjacentes até que este satisfaçam o
	 * critéria de Delaunay
	 * 
	 * @param inserted      vértice que acabou de ser inserido
	 * @param edge          aresta oposta ao vértice inserido
	 * @param triangulation conjunto de triângulos
	 */
	private static void legalizeEdge(Point2D.Double inserted, Point2D.Double[] edge, Set<Triangle> triangulation) {
		Triangle[] triangles = removeAdjacentsTriangles(edge, triangulation);
		if (triangles != null) {
			Triangle[] flippedTriangles = isIllegal(edge, triangles);
			if (flippedTriangles != null) {
				// se forem ilegais, flip
				triangles[0].setVertices(flippedTriangles[0]);
				triangles[1].setVertices(flippedTriangles[1]);

				// reinsere na lista, agora com as alterações
				/* boolean b = */ triangulation.add(triangles[0]);
//				if (!b) TODO e?
//					System.out.println();

				/* b= */ triangulation.add(triangles[1]);
//				if (!b) TODO e?
//					System.out.println();

				// novos pontos em comum
				Point2D.Double[][] newCommon = Triangle.getCommonEdge(triangles[0], triangles[1]);
				Point2D.Double otherVertice = inserted.equals(newCommon[0][0]) ? newCommon[0][1] : newCommon[0][0];

				legalizeEdge(inserted, new Point2D.Double[] { otherVertice, newCommon[1][0] }, triangulation);
				legalizeEdge(inserted, new Point2D.Double[] { otherVertice, newCommon[1][1] }, triangulation);
			} else {
				// se não forem legais, só reinsere no conjunto sem alterar
				triangulation.add(triangles[0]);
				triangulation.add(triangles[1]);
			}
		}
	}

	/**
	 * 
	 * Função que determina se dois triângulos são ilegais ou não segundo o critério
	 * de Delaunay (o ponto oposto à aresta compartilhada de um triângulo está
	 * dentro do circuncírculo do outro triângulo)
	 * 
	 * @param edge      aresta compartilhada pelos triângulos adjacentes
	 * @param triangles vetor contendo os dois triângulos adjacentes
	 * @return se eles forem ilegais, já se retorna <b>novos</b> triângulos com a
	 *         aresta compartilhada alterada, senão retorna-se <code>null</code>
	 */
	private static Triangle[] isIllegal(Point2D.Double[] edge, Triangle[] triangles) {
		Circle c = new Circle(triangles[0]);
		return c.hasInside(triangles[1].getOppositeVertice(edge)) ? flip(triangles) : null;
	}

	/**
	 * Função que altera o vértice que é comum a dois triângulos adjacentes
	 * 
	 * @param triangles vetor contendo dois triângulos adjacentes
	 * @return vetor com dois novos triângulos adjacentes,porém a aresta em comum é
	 *         aquela do segmento de reta que liga os dois vértices que não eram
	 *         compartilhados
	 */
	private static Triangle[] flip(Triangle[] triangles) {
		Triangle[] out = new Triangle[2];

		Point2D.Double[][] edge = Triangle.getCommonEdge(triangles[0], triangles[1]);

		// os novos triângulos terão em comum os pontos que não eram em comum
		// (os da linha 1) e aqueles que eram comuns, cada um fica com um
		out[0] = new Triangle(edge[1][0], edge[1][1], edge[0][0]);
		out[1] = new Triangle(edge[1][0], edge[1][1], edge[0][1]);

		return out;
	}

	/**
	 * Função que procura na lista de triângulos aquele par que compartilham uma
	 * aresta em comum
	 * 
	 * @param edge          aresta em comum
	 * @param triangulation lista de triângulos
	 * @return vetor com o par de triângulos que possuem a dada aresta
	 *         compartilhada, ou <code>null</code> se não há um par que tenha tal
	 *         aresta em comum
	 */
	private static Triangle[] removeAdjacentsTriangles(Point2D.Double[] edge, Set<Triangle> triangulation) {
		Triangle[] out = new Triangle[2];
		int j = 0;
		for (Triangle t : triangulation) {
			if (t.hasEdge(edge) >= 0)
				out[j++] = t;
			if (j == 2) { // se achou os dois...
				if (!isConvex(edge, out)) // ... ver se o par forma um polígono convexo
					return null;
				break;
			}
		}

		if (j < 2)
			return null;

		triangulation.remove(out[0]);
		triangulation.remove(out[1]);

		return out;
	}

	/**
	 * Função que analise de dois triângulo que compartilham uma dada aresta são de
	 * fato adjacentes vendo se eles juntos formam um quadrilátero convexo ou não
	 * 
	 * @param edge      aresta em comum
	 * @param triCommon triângulos que compartilham uma aresta em comum
	 * @return <code>true</code> se eles juntos formam um polígono convexo,
	 *         <code>false</code> senão
	 */
	private static boolean isConvex(Point2D.Double[] edge, Triangle[] triCommon) {
		Point2D.Double o0 = triCommon[0].getOppositeVertice(edge);
		Point2D.Double o1 = triCommon[1].getOppositeVertice(edge);

		Triangle t1 = new Triangle(o0, o1, edge[0]);
		if (t1.hasInside(edge[1]))
			return false;

		t1 = new Triangle(o0, o1, edge[1]);
		if (t1.hasInside(edge[0]))
			return false;

		return true;
	}
}

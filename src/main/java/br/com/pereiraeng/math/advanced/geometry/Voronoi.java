package br.com.pereiraeng.math.advanced.geometry;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import br.com.pereiraeng.graph.tree.BinaryNode;
import br.com.pereiraeng.math.DuplaO;
import br.com.pereiraeng.math.Vec;
import br.com.pereiraeng.math.geometry.Circle;
import br.com.pereiraeng.math.geometry.Geom;
import br.com.pereiraeng.math.geometry.Line;
import br.com.pereiraeng.math.geometry.Polygon;
import br.com.pereiraeng.core.ExtendedMath;
import br.com.pereiraeng.core.collections.sortedlist.SortedList;

public class Voronoi {

	private static final double MARGIN = .1;

	private static final double TOL = 1E-10;

	public static <F extends Point2D.Float> Map<F, List<Point2D.Double>> voronoiDiagramFP(Set<? extends F> points,
			Point2D.Double[] mM) {
		// faz-se uma tabela intermediária que associa os pontos aos vértices
		// (eles não podem ser o mesmo objeto pois as coordenadas são
		// Point2D.Float, enquanto que a função Voronoi só aceita
		// Point2D.Double)
		Map<Point2D.Double, F> d2f = new HashMap<>();
		for (F f : points)
			d2f.put(new Point2D.Double(f.getX(), f.getY()), f);

		// faz-se a diagramação de Voronoi
		Map<Point2D.Double, List<Point2D.Double>> ts = toPolygons(gatherEdges(voronoiDiagram(d2f.keySet(), mM)));

		// desfaz-se a operação inicial
		Map<F, List<Point2D.Double>> out = new HashMap<>();
		for (Entry<Point2D.Double, List<Point2D.Double>> e : ts.entrySet())
			out.put(d2f.get(e.getKey()), e.getValue());

		return out;
	}

	public static <F extends Point2D.Float> Map<DuplaO<F>, Line> voronoiDiagramFE(Set<? extends F> points,
			Point2D.Double[] mM) {
		// faz-se uma tabela intermediária que associa os pontos aos vértices
		// (eles não podem ser o mesmo objeto pois as coordenadas são
		// Point2D.Float, enquanto que a função Voronoi só aceita
		// Point2D.Double)
		Map<Point2D.Double, F> d2f = new HashMap<>();
		for (F f : points)
			d2f.put(new Point2D.Double(f.getX(), f.getY()), f);

		System.out.println("Tabelas prontas!");

		// faz-se a diagramação de Voronoi
		Map<Line, Line> ts = voronoiDiagram(d2f.keySet(), mM);

		// desfaz-se a operação inicial
		Map<DuplaO<F>, Line> out = new HashMap<>();
		for (Entry<Line, Line> e : ts.entrySet()) {
			Line l = e.getKey();
			out.put(new DuplaO<F>(d2f.get(l.getFrom()), d2f.get(l.getTo())), e.getValue());
		}

		return out;
	}

	// =====================================================================

	/**
	 * Função que procede com a diagramação de Delaunay de um conjunto de pontos. O
	 * algoritmo empregado está descrito no capítulo 7 do livro
	 * <a href= "http://www.cs.uu.nl/geobook/">Computational Geometry: Algorithms
	 * and Applications</a>.<br>
	 * 
	 * O algoritmo funciona para todos os casos, exceto um: <strong>não pode haver
	 * mais de um ponto com a ordenada máxima</strong> (esta restrição também se
	 * aplica ao algoritmo de {@link Delaunay#delaunayTriangulation(Set) Delaunay})
	 * 
	 * @param points conjunto de pontos
	 * @param mM     vetor com dois pontos, a ser preenchido com os limites da caixa
	 *               envoltória do diagrama
	 * @return tabela de dispersão que associa para os pares de pontos da entrada o
	 *         vértice comum das células de Voronoi destes pontos
	 */
	public static Map<Line, Line> voronoiDiagram(Set<Point2D.Double> points, Point2D.Double[] mM) {

		SortedList<Point2D.Double> events = new SortedList<>(new Geom.PointComparator(false, false));
		events.addAll(points);

		System.out.println("Lista ordenada!");

		Point2D.Double m = new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
				M = new Point2D.Double(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		for (Point2D.Double p : points) {
			if (p.x < m.x)
				m.x = p.x;
			if (p.y < m.y)
				m.y = p.y;
			if (p.x > M.x)
				M.x = p.x;
			if (p.y > M.y)
				M.y = p.y;
		}

		System.out.println("Máximos calculados!");

		Map<Point2D.Double, BinaryNode> circleEvents = new HashMap<>();

		LinkedHashMap<Line, Line> edgeList = new LinkedHashMap<>();

		BinaryNode tree = new BinaryNode(null);

		int count = 0;
		while (events.size() > 0) {
			Point2D.Double e = events.poll();
			BinaryNode bn = circleEvents.remove(e);

			if (bn == null) // site event
				handleSiteEvent(e, tree, events, circleEvents, edgeList);
			else // circle event
				handleCircleEvent(e, bn, events, circleEvents, edgeList);
			count++;
			System.out.printf("%d/%d\n", count, events.size());
		}

		// ----------------------------------------------

		Point2D.Double bm = new Point2D.Double(m.x, m.y), bM = new Point2D.Double(M.x, M.y);
		for (Entry<Line, Line> e : edgeList.entrySet()) {
			Line l = e.getValue();
			Point2D.Double p = l.getFrom();

			if (p.x < bm.x)
				bm.x = p.x;
			if (p.y < bm.y)
				bm.y = p.y;
			if (p.x > bM.x)
				bM.x = p.x;
			if (p.y > bM.y)
				bM.y = p.y;

			p = l.getTo();
			if (p != null) {
				if (p.x < bm.x)
					bm.x = p.x;
				if (p.y < bm.y)
					bm.y = p.y;
				if (p.x > bM.x)
					bM.x = p.x;
				if (p.y > bM.y)
					bM.y = p.y;
			}
		}

		// The internal nodes still present in T correspond to the half-infinite edges
		// of the Voronoi diagram. Compute a bounding box that contains all vertices of
		// the Voronoi diagram in its interior, and attach the half-infinite edges to
		// the bounding box by updating the doubly-connected edge list appropriately.

		Stack<BinaryNode> arguments = new Stack<>();
		arguments.push(tree);
		while (!arguments.empty()) {
			BinaryNode bn = arguments.pop();
			if (!bn.isLeaf()) {
				// os nós internos da árvore contém duplas de pontos (site events) numa ordem
				// tal que o "vértice no infinito" do diagrama de Voronoi está sempre à direita
				// da direção definida pelos pontos (assim é possível saber qual das
				// intersecções com a caixa envoltória é aquela que representará o vértice no
				// infinito)
				Point2D.Double[] e = (Point2D.Double[]) bn.getUserObject();
				Line edge = new Line(e[0], e[1]);
				Line halfEdge = edgeList.get(edge);

				if (halfEdge != null ? halfEdge.getFrom() != null : false)
					halfEdge.addVertex(getBisectorIntersection(edge, halfEdge.getFrom(), bm, bM));

				arguments.push((BinaryNode) bn.getChildAt(0));
				arguments.push((BinaryNode) bn.getChildAt(1));
			}
		}

		// adicionar borda (mas não maior que o delimitado pelos vértices externos)
		double b = (M.x - m.x) * MARGIN;
		m.x = Math.max(m.x - b, bm.x);
		M.x = Math.min(M.x + b, bM.x);
		b = (M.y - m.y) * MARGIN;
		m.y = Math.max(m.y - b, bm.y);
		M.y = Math.min(M.y + b, bM.y);
		// uma das saída da função
		mM[0] = m;
		mM[1] = M;

		// trim
		Iterator<Entry<Line, Line>> it = edgeList.entrySet().iterator();
		while (it.hasNext()) {
			Line ps = it.next().getValue();
			if (ps.getType() != 0)
				it.remove();
			else {
				boolean flag = trimPoint(ps, m, M, true);
				if (!flag)
					it.remove();
				else {
					flag = trimPoint(ps, m, M, false);
					if (!flag)
						it.remove();
					else if (ps.distance() < TOL)
						it.remove();
				}
			}
		}

		return edgeList;
	}

	private static void handleSiteEvent(Point2D.Double pi, BinaryNode tree, SortedList<Point2D.Double> events,
			Map<Point2D.Double, BinaryNode> circleEvent, Map<Line, Line> edgeList) {
		if (tree.getUserObject() == null) {
			// If T is empty, insert pi into it (so that T consists of a single leaf storing
			// pi) and return.
			tree.setUserObject(new Object[] { pi, null, null }); //
		} else {
			// Search in T for the arc vertically above pi.
			BinaryNode arcAbove = search(pi, tree);
			Object[] obj = (Object[]) arcAbove.getUserObject();
			if (obj[1] != null) {
				// If the leaf representing the arc has a pointer to a circle event in Q, then
				// this circle event is a false alarm and it must be deleted from Q

				circleEvent.remove(obj[1]);
				events.remove(obj[1]);
				obj[1] = null;
				obj[2] = null;
			}

			// Replace the leaf of T that represents the arc with a subtree having three
			// leaves. The middle leaf stores the new site pi and the other two leaves store
			// the site pj that was originally stored with the arc. Store the tuples <pj,
			// pi> and <pi, pj> representing the new breakpoints at the two new internal
			// nodes.

			Point2D.Double pj = (Point2D.Double) obj[0];

			BinaryNode arcLeafL = null, arcLeafR = null;
			if (pi.y == pj.y) {
				if (pi.x > pj.x) {
					arcAbove.setUserObject(new Point2D.Double[] { pj, pi });

					arcAbove.insert(arcLeafL = new BinaryNode(new Object[] { pj, null, null }), 0);
					arcAbove.insert(arcLeafR = new BinaryNode(new Object[] { pi, null, null }), 1);
				} else {
					arcAbove.setUserObject(new Point2D.Double[] { pi, pj });

					arcAbove.insert(arcLeafL = new BinaryNode(new Object[] { pi, null, null }), 0);
					arcAbove.insert(arcLeafR = new BinaryNode(new Object[] { pj, null, null }), 1);
				}
			} else {
				if (pi.x > pj.x) {
					arcAbove.setUserObject(new Point2D.Double[] { pj, pi });

					arcAbove.insert(arcLeafL = new BinaryNode(new Object[] { pj, null, null }), 0);

					BinaryNode m2 = new BinaryNode(new Point2D.Double[] { pi, pj });
					m2.insert(new BinaryNode(new Object[] { pi, null, null }), 0);
					m2.insert(arcLeafR = new BinaryNode(new Object[] { pj, null, null }), 1);
					arcAbove.insert(m2, 1);
				} else {
					arcAbove.setUserObject(new Point2D.Double[] { pi, pj });

					BinaryNode m2 = new BinaryNode(new Point2D.Double[] { pj, pi });
					m2.insert(arcLeafL = new BinaryNode(new Object[] { pj, null, null }), 0);
					m2.insert(new BinaryNode(new Object[] { pi, null, null }), 1);
					arcAbove.insert(m2, 0);

					arcAbove.insert(arcLeafR = new BinaryNode(new Object[] { pj, null, null }), 1);
				}
			}

			// Create new half-edge records in the Voronoi diagram structure for the edge
			// separating V(pi) and V(pj), which will be traced out by the two new
			// breakpoints.

			edgeList.put(new Line(pi, pj), new Line());

			// Check the triple of consecutive arcs where the new arc for pi is the left arc
			// to see if the breakpoints converge. If so, insert the circle event into Q and
			// add pointers between the node in T and the node in Q. Do the same for the
			// triple where the new arc is the right arc.

			BinaryNode n = arcAbove.getPredecessor();
			if (n != null) {
				Point2D.Double pk = (Point2D.Double) ((Object[]) n.getUserObject())[0];
				Point2D.Double ce = checkTriple(pk, pj, pi, arcLeafL);
				if (ce != null) {
					events.add(ce);
					circleEvent.put(ce, arcLeafL);
				}
			}

			n = arcAbove.getSuccessor();
			if (n != null) {
				Point2D.Double pk = (Point2D.Double) ((Object[]) n.getUserObject())[0];
				Point2D.Double ce = checkTriple(pi, pj, pk, arcLeafR);
				if (ce != null) {
					events.add(ce);
					circleEvent.put(ce, arcLeafR);
				}
			}
		}
	}

	private static BinaryNode search(Point2D.Double pi, BinaryNode tree) {
		if (tree.isLeaf())
			return tree;
		else {
			Point2D.Double[] pipj = (Point2D.Double[]) tree.getUserObject();
			double breakPointX = getBreakpointX(pipj[0], pipj[1], pi.y);
			return search(pi, (BinaryNode) tree.getChildAt(pi.x < breakPointX ? 0 : 1));
		}
	}

	private static void handleCircleEvent(Point2D.Double ce, BinaryNode n, SortedList<Point2D.Double> events,
			Map<Point2D.Double, BinaryNode> circleEvent, Map<Line, Line> edgeList) {
		BinaryNode l = n.getPredecessor();
		BinaryNode r = n.getSuccessor();

		// Delete the leaf that represents the disappearing arc from T. Update the
		// tuples representing the breakpoints at the internal nodes. Perform
		// rebalancing operations on T if necessary.

		BinaryNode p = (BinaryNode) n.getParent();
		BinaryNode gp = (BinaryNode) p.getParent();

		int indexR = p.getIndex(n);
		int indexF = 1 - indexR;
		Point2D.Double[] ps = (Point2D.Double[]) p.getUserObject();

		BinaryNode anc = n.getCommonParent();
		Point2D.Double[] tuplet = (Point2D.Double[]) anc.getUserObject();
		tuplet[indexF] = ps[indexF];

		gp.insert((BinaryNode) p.getChildAt(indexF), gp.getIndex(p));

		// Delete all circle events involving the arc from Q; these can be found using
		// the pointers from the predecessor and the successor of the left in T. (The
		// circle event where the arc is the middle arc is currently being handled, and
		// has already been deleted from Q.)

		Object[] obj = (Object[]) l.getUserObject();
		if (obj[1] != null) {
			circleEvent.remove(obj[1]);
			events.remove(obj[1]);
			obj[1] = null;
			obj[2] = null;
		}

		obj = (Object[]) r.getUserObject();
		if (obj[1] != null) {
			circleEvent.remove(obj[1]);
			events.remove(obj[1]);
			obj[1] = null;
			obj[2] = null;
		}

		// Add the center of the circle causing the event as a vertex record to the
		// doubly-connected edge list D storing the Voronoi diagram under construction.
		// Create two half-edge records corresponding to the new breakpoint of the beach
		// line. Set the pointers between them appropriately. Attach the three new
		// records to the half-edge records that end at the vertex.

		obj = (Object[]) n.getUserObject();

		Point2D.Double pi = (Point2D.Double) ((Object[]) l.getUserObject())[0];
		Point2D.Double pj = (Point2D.Double) obj[0];
		Point2D.Double pk = (Point2D.Double) ((Object[]) r.getUserObject())[0];

		Point2D.Double vv = (Point2D.Double) obj[2];

		// três novas arestas
		Line ve = edgeList.get(new Line(pi, pj));
		ve.addVertex(vv);

		ve = edgeList.get(new Line(pj, pk));
		ve.addVertex(vv);

		edgeList.put(new Line(pi, pk), new Line(vv));

		// Check the new triple of consecutive arcs that has the former left neighbor
		// of the arc as its middle arc to see if the two breakpoints of the triple
		// converge. If so, insert the corresponding circle event into Q. and set
		// pointers between the new circle event in Q and the corresponding leaf of T.
		// Do the same for the triple where the former right neighbor is the middle arc.

		n = l.getPredecessor();
		if (n != null) {
			Point2D.Double pl = (Point2D.Double) ((Object[]) n.getUserObject())[0];
			Point2D.Double nce = checkTriple(pl, pi, pk, l);
			if (nce != null) {
				events.add(nce);
				circleEvent.put(nce, l);
			}
		}

		n = r.getSuccessor();
		if (n != null) {
			Point2D.Double pl = (Point2D.Double) ((Object[]) n.getUserObject())[0];
			Point2D.Double nce = checkTriple(pi, pk, pl, r);
			if (nce != null) {
				events.add(nce);
				circleEvent.put(nce, r);
			}
		}
	}

	// ------------------------ CARTESIANA ------------------------

	private static Point2D.Double checkTriple(Point2D.Double pi, Point2D.Double pj, Point2D.Double pk,
			BinaryNode arcLeaf) {
		Point2D.Double vv = new Point2D.Double();
		double radius = Circle.getCircle(pi, pj, pk, vv);

		Point2D.Double ce = null;
		if (radius < 0.) {
			// se os pontos não forem colineares (radius = +inf) e se os pontos de parada
			// forem convergentes...
			ce = new Point2D.Double(vv.x, vv.y - Math.abs(radius));
			Object[] obj = (Object[]) arcLeaf.getUserObject();
			obj[1] = ce;
			obj[2] = vv;
		}
		return ce;
	}

	private static double getBreakpointX(Point2D.Double pi, Point2D.Double pj, double ly) {
		if (pi.y == pj.y)
			return .5 * (pi.x + pj.x);
		else {
			if (pj.y == ly)
				return pj.x;
			else {
				double mly2 = ly * ly;
				double cj = pj.x * pj.x + pj.y * pj.y - mly2;
				double r = (pi.y - ly) / (pj.y - ly);
				double[] xs = ExtendedMath.bhaskara(r - 1, -2. * pj.x * r + 2. * pi.x,
						r * cj - (pi.x * pi.x + pi.y * pi.y - mly2));
				if (pi.y > pj.y)
					return xs[0];
				else
					return xs[1];
			}
		}
	}

	// ------------------------ CAIXA ENVOLTÓRIA ------------------------

	/**
	 * Função que detecta onde uma aresta que aponta para o infinito intercepta a
	 * caixa envoltória
	 * 
	 * @param edge   aresta contendo os pontos que equidistam do vértice da aresta
	 *               que aponta para o infinito
	 * @param vertex ponto de partida da aresta que aponta para o infinito
	 * @param m      ponto de menor abscissa e menor ordenada da caixa envoltória
	 * @param M      ponto de maior abscissa e maior ordenada da caixa envoltória
	 * @return ponto onde a aresta intercepta a caixa envoltória
	 */
	private static Point2D.Double getBisectorIntersection(Line edge, Point2D.Double vertex, Point2D.Double m,
			Point2D.Double M) {
		Point2D.Double pm = edge.getMidPoint();
		double dx = pm.x - vertex.x;
		Point2D.Double[] out = null;
		if (Math.abs(dx) < TOL) // reta vertical: intercepta as retas horizontais (superior ou inferior)
			out = new Point2D.Double[] { new Point2D.Double(pm.x, m.y), new Point2D.Double(pm.x, M.y) };
		else {
			double dy = pm.y - vertex.y;
			if (Math.abs(dy) < TOL) // reta horizontal: intercepta as retas verticais (esquerda ou direita)
				out = new Point2D.Double[] { new Point2D.Double(m.x, pm.y), new Point2D.Double(M.x, pm.y) };
			else {
				// intercepta as quatro retas
				double m0 = dy / dx;

				out = new Point2D.Double[4];
				int j = 0;

				Point2D.Double p = new Point2D.Double(m.x, m0 * (m.x - vertex.x) + vertex.y);
				if (p.y >= m.y && p.y <= M.y)
					out[j++] = p;

				p = new Point2D.Double(M.x, m0 * (M.x - vertex.x) + vertex.y);
				if (p.y >= m.y && p.y <= M.y)
					out[j++] = p;

				if (j < 2) {
					p = new Point2D.Double(vertex.x + (m.y - vertex.y) / m0, m.y);
					if (p.x >= m.x && p.x <= M.x)
						out[j++] = p;
				}

				if (j < 2) {
					p = new Point2D.Double(vertex.x + (M.y - vertex.y) / m0, M.y);
					if (p.x >= m.x && p.x <= M.x)
						out[j++] = p;
				}
			}
		}

		double a = Vec.det3(new double[][] { { edge.getFrom().x, edge.getFrom().y, 1. },
				{ edge.getTo().x, edge.getTo().y, 1. }, { out[0].x, out[0].y, 1. } });
		if (a <= 0.)
			return out[0];
		else
			return out[1];
	}

	/**
	 * Função que trunca as arestas do diagrama de Voronoi de modo que caiba numa
	 * dada caixa envoltória
	 * 
	 * @param ps  aresta do diagrama de Voronoi
	 * @param m   ponto de menor abscissa e menor ordenada da caixa envoltória
	 * @param M   ponto de maior abscissa e maior ordenada da caixa envoltória
	 * @param pos <code>true</code> para truncar pelo lado do primeiro vértice da
	 *            aresta, <code>false</code> pelo lado do segundo
	 * @return <code>true</code> se a truncagem fez com que a aresta coubesse na
	 *         caixa, <code>false</code> senão
	 */
	private static boolean trimPoint(Line line, Point2D.Double m, Point2D.Double M, boolean pos) {
		Point2D.Double pt = pos ? line.getFrom() : line.getTo();
		Point2D.Double po = pos ? line.getTo() : line.getFrom();

		if (pt.x < m.x) {
			double ny = Geom.retaY(pt, po, m.x);
			pt = new Point2D.Double(m.x, ny);
			if (pos)
				line.setFrom(pt);
			else
				line.setTo(pt);
		} else if (pt.x > M.x) {
			double ny = Geom.retaY(pt, po, M.x);
			pt = new Point2D.Double(M.x, ny);
			if (pos)
				line.setFrom(pt);
			else
				line.setTo(pt);
		}

		if (pt.y < m.y) {
			double nx = Geom.retaX(pt, po, m.y);
			pt = new Point2D.Double(nx, m.y);
			if (pos)
				line.setFrom(pt);
			else
				line.setTo(pt);
		} else if (pt.y > M.y) {
			double nx = Geom.retaX(pt, po, M.y);
			pt = new Point2D.Double(nx, M.y);
			if (pos)
				line.setFrom(pt);
			else
				line.setTo(pt);
		}

		return pt.x >= m.x && pt.x <= M.x && pt.y >= m.y && pt.y <= M.y;
	}

	// ------------------------ AUXILIARES ------------------------

	public static Map<Point2D.Double, List<Point2D.Double[]>> gatherEdges(Map<Line, Line> ts) {
		Map<Point2D.Double, List<Point2D.Double[]>> out = new HashMap<>();
		for (Entry<Line, Line> e : ts.entrySet()) {
			Line edge = e.getKey();
			Point2D.Double[] vs = e.getValue().getPointArray();

			List<Point2D.Double[]> es = out.get(edge.getTo());
			if (es == null)
				out.put(edge.getTo(), es = new LinkedList<>());
			es.add(vs);

			es = out.get(edge.getFrom());
			if (es == null)
				out.put(edge.getFrom(), es = new LinkedList<>());
			es.add(vs);
		}
		return out;
	}

	public static Map<Point2D.Double, List<Point2D.Double>> toPolygons(
			Map<Point2D.Double, List<Point2D.Double[]>> v2es) {
		Map<Point2D.Double, List<Point2D.Double>> out = new HashMap<>();
		for (Entry<Point2D.Double, List<Point2D.Double[]>> e : v2es.entrySet()) {
			List<Point2D.Double> polyline = new ArrayList<>(e.getValue().size() + 1);
			Polygon.edges2vertexes(polyline, e.getValue());
			out.put(e.getKey(), polyline);
		}
		return out;
	}

	// --------------------- TESTE ---------------------

//	public static void main(String[] args) {
//		Plan<String> p2d = new Plan<>(Color.WHITE, 630, 600);
//
//		HashSet<Point2D.Float> set = new HashSet<>();
//
////		set.add(new Point2D.Float(0f, 0f));
////		set.add(new Point2D.Float(.5f, 0f));
////		set.add(new Point2D.Float(1f, 0f));
////		set.add(new Point2D.Float(0f, 0.5f));
////		set.add(new Point2D.Float(.5f, 0.5f));
////		set.add(new Point2D.Float(1f, 0.5f));
////		set.add(new Point2D.Float(0f, 1f));
////		set.add(new Point2D.Float(.5f, 1f));
////		set.add(new Point2D.Float(1f, 1f));
////		set.add(new Point2D.Float(0.25f, 1.25f));
//
//		for (int i = 0; i < 5000; i++)
//			set.add(new Point2D.Float((float) Math.random(), (float) Math.random()));
//
//		int j = 0;
//		for (Point2D.Float p : set)
//			p2d.put(String.format("T%d", j++), p);
//
//		Point2D.Double[] mM = new Point2D.Double[2];
//		Map<Point2D.Float, List<Point2D.Double>> voronoi = Voronoi.voronoiDiagramFP(set, mM);
//		p2d.setColor(Color.BLUE);
//		for (Entry<Point2D.Float, List<Point2D.Double>> e : voronoi.entrySet())
//			p2d.put(String.format("V%d", j++), new Polygon.Double(e.getValue()));
//		p2d.setColor(Color.ORANGE);
//		p2d.put(String.format("R%d", j++), new Rectangle(mM[0], mM[1]));
//
//		Collection<List<Point2D.Float>> delaunay = Delaunay.delaunayTriangulationF(set);
//		p2d.setColor(Color.GREEN.darker());
//		for (List<Point2D.Float> t : delaunay)
//			p2d.put(String.format("T%d", j++), new Triangle(t.get(0), t.get(1), t.get(2)));
//
//		JFrame fm = new JFrame();
//		fm.setContentPane(p2d);
//		p2d.repaint();
//		fm.setSize(1000, 800);
//		fm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		fm.setVisible(true);
//	}
}

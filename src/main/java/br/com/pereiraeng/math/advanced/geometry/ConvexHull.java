package br.com.pereiraeng.math.advanced.geometry;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import br.com.pereiraeng.math.geometry.Geom;
import br.com.pereiraeng.core.collections.ListUtils;

/**
 * Classe que contém as funções para o cálculo da envoltória complexa de um
 * conjunto de pontos
 * 
 * @author Philipe Pereira
 *
 */
public class ConvexHull {

	/**
	 * Find convex hull from the set S of n points
	 * 
	 * @param points
	 * @return
	 */
	public static List<Point2D.Double> quickhull(Set<? extends Point2D.Double> points) {
		List<Point2D.Double> ch = new LinkedList<>();

		// Find left and right most points, say A & B...
		Point2D.Double a = Geom.getExtreme(points, 2), b = Geom.getExtreme(points, 0);

		// ... and add A & B to convex hull
		ch.add(a);
		ch.add(b);

		// Segment AB divides the remaining (n-2) points into 2 groups S1 and S2 where
		// S1 are points in S that are on the right side of the oriented line from A to
		// B, and S2 are points in S that are on the right side of the oriented line
		// from B to A
		Set<Point2D.Double> s1 = new HashSet<>();
		Set<Point2D.Double> s2 = new HashSet<>();

		for (Point2D.Double pd : points) {
			if (pd != a && pd != b) {
				double det = Geom.area(a.x, a.y, b.x, b.y, pd.x, pd.y);
				if (det > 0.)
					s1.add(pd);
				else
					s2.add(pd);
			}
		}

		findHull(ch, s1, a, b);
		findHull(ch, s2, b, a);

		return ch;
	}

	/**
	 * Find points on convex hull from the set Sk of points that are on the right
	 * side of the oriented line from P to Q
	 * 
	 * @param ch
	 * @param sd
	 * @param a
	 * @param b
	 */
	private static void findHull(List<Point2D.Double> ch, Set<Point2D.Double> sk, Point2D.Double p, Point2D.Double q) {
		// If Sk has no point, then return.
		if (sk.size() == 0)
			return;

		// From the given set of points in Sk, find farthest point, say C, from segment
		// PQ
		double max = -1.;
		Point2D.Double c = null;
		for (Point2D.Double pd : sk) {
			double det = Geom.area(p.x, p.y, q.x, q.y, pd.x, pd.y);
			if (det > max) {
				c = pd;
				max = det;
			}
		}

		// Add point C to convex hull at the location between P and Q
		ListUtils.addAfter(ch, c, p);

		// Three points P, Q, and C partition the remaining points of Sk into 3 subsets:
		// S0, S1, and S2 where S0 are points inside triangle PCQ, S1 are points on the
		// right side of the oriented line from P to C, and S2 are points on the right
		// side of the oriented line from C to Q.
		Set<Point2D.Double> s1 = new HashSet<>();
		Set<Point2D.Double> s2 = new HashSet<>();

		for (Point2D.Double pd : sk) {
			if (pd != p && pd != q && pd != c) {
				double det1 = Geom.area(p.x, p.y, c.x, c.y, pd.x, pd.y);
				double det2 = Geom.area(c.x, c.y, q.x, q.y, pd.x, pd.y);
				if (det1 > 0.)
					s1.add(pd);
				if (det2 > 0.)
					s2.add(pd);
			}
		}

		findHull(ch, s1, p, c);
		findHull(ch, s2, c, q);
	}
}

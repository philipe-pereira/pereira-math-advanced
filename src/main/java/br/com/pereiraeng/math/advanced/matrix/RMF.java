package br.com.pereiraeng.math.advanced.matrix;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Locale;

/**
 * Classe do objeto que representa uma matriz esparsa em que os dados são
 * estocados no formato utilizado pelo <a href="https://www.usbr.gov/">Bureau of
 * Reclamation</a>. É similar ao {@link CSR}, com a exceção de que os elementos
 * da diagonal da matriz são estocadas num vetor a parte.
 * 
 * @author Philipe PEREIRA
 *
 */
public class RMF implements Sparse<Double> {

//	public static void main(String[] args) {
//		File dat = new File("C:\\Users\\Usuario\\workspace\\data\\dnc\\readme.dat");
//
//		RMF rmf = new RMF(new double[][] { { 2.5, 0, -1, -.6, 0, -3.3, 0 }, { 0, 9, -9.8, 0, -3.9, 0, 0 },
//				{ -3, -5.2, 8.1, -3.4, 0, 0, 0 }, { -.9, 0, -4, 6.6, 0, -4.7, 0 }, { 0, -9.9, 0, 0, 1.4, -5.7, 0 },
//				{ -9.1, 0, 0, -1.3, -9.7, .5, -4.4 }, { 0, 0, 0, 0, 0, -.5, 8.4 } });
//		rmf.saveFile(dat);
//
//		RMF rmf = RMF.readFile(dat);
//		System.out.println(rmf);
//	}

	/**
	 * pointer to access off-diagonal element in ACOEF given a diagonal
	 */
	private int[] lclfc1;

	/**
	 * pointer to diagonal element ID given the off-diagonal pointer
	 */
	private int[] lclfc3;

	/**
	 * All Diagonal Elements (size=ncellt)
	 */
	private double[] ap;

	/**
	 * All off-diagonal non-zero Elements (size=Nclfc)
	 */
	private double[] acoef;

	/**
	 * 
	 * @param lclfc1 pointer to access off-diagonal element in ACOEF given a
	 *               diagonal
	 * @param lclfc3 pointer to diagonal element ID given the off-diagonal pointer
	 * @param ap     All Diagonal Elements (size=ncellt)
	 * @param acoef  All off-diagonal non-zero Elements (size=Nclfc)
	 */
	public RMF(int[] lclfc1, int[] lclfc3, double[] ap, double[] acoef) {
		this.lclfc1 = lclfc1;
		this.lclfc3 = lclfc3;
		this.ap = ap;
		this.acoef = acoef;
	}

	/**
	 * 
	 * @param coefs
	 */
	public RMF(double[][] coefs) {
		this.lclfc1 = new int[coefs.length + 1];
		this.ap = new double[coefs.length];

		int nnzNonDiag = 0;
		for (int i = 0; i < coefs.length; i++) {
			for (int j = 0; j < coefs[i].length; j++) {
				if (i != j) {
					if (coefs[i][j] != 0.)
						nnzNonDiag++;
				} else
					ap[i] = coefs[i][i];
			}
		}
		this.lclfc3 = new int[nnzNonDiag];
		this.acoef = new double[nnzNonDiag];

		this.lclfc1[0] = 1;
		int k = 0;
		for (int i = 0; i < coefs.length; i++) {
			this.lclfc1[i + 1] = this.lclfc1[i];
			for (int j = 0; j < coefs[i].length; j++) {
				if (i != j) {
					if (coefs[i][j] != 0.) {
						this.lclfc1[i + 1]++;
						this.lclfc3[k] = j + 1;
						this.acoef[k] = coefs[i][j];
						k++;
					}
				}
			}
		}
	}

	@Override
	public void set(int row, int column, Double value) {
		// não faz nada: matriz imutável
	}

	@Override
	public Double get(int row, int column) {
		if (row == column)
			return ap[row];
		else {
			int lower = lclfc1[row] - 1;
			int upper = lclfc1[row + 1] - 1;
			if (lower == upper) // no elements in this row
				return 0.;
			else {
				for (int i = lower; i < upper; i++)
					if (lclfc3[i] - 1 == column)
						return acoef[i];
				return 0.;
			}
		}
	}

	@Override
	public int getNNZ() {
		return ap.length + acoef.length;
	}

	public static RMF readFile(File dat) {
		int ncellt = 0;

		int[] lclfc1 = null;
		int[] lclfc3 = null;

		double[] ap = null;
		double[] acoef = null;

		RMF rmf = null;

		try {
			RandomAccessFile raf = new RandomAccessFile(dat, "r");
			String s = raf.readLine();

			// ncellt= number of diagonal elements of A
			ncellt = Integer.parseInt(s.substring(0, 10).trim());
			// nclfc = total number of off-diagonal non-zero elements
			int nclfc = Integer.parseInt(s.substring(10, 20).trim());

			byte[] buffer = new byte[10];

			// pointer to access off-diagonal element in ACOEF given a diagonal

			lclfc1 = new int[ncellt + 1];

			int lb = 0;
			for (int i = 0; i < lclfc1.length; i++) {
				raf.read(buffer);
				lclfc1[i] = Integer.parseInt(new String(buffer).trim());
				lb++;
				if (lb == 10) {
					raf.skipBytes(2);
					lb = 0;
				}
			}
			raf.skipBytes(2);

			// pointer to diagonal element ID given the off-diagonal pointer

			lclfc3 = new int[nclfc];

			lb = 0;
			for (int i = 0; i < lclfc3.length; i++) {
				raf.read(buffer);
				lclfc3[i] = Integer.parseInt(new String(buffer).trim());
				lb++;
				if (lb == 10) {
					raf.skipBytes(2);
					lb = 0;
				}
			}
			raf.skipBytes(2);

			// pointer used in the CGS solver given the off-diagonal pointer

			int[] lacac = new int[nclfc];

			lb = 0;
			for (int i = 0; i < lacac.length; i++) {
				raf.read(buffer);
				lacac[i] = Integer.parseInt(new String(buffer).trim());
				lb++;
				if (lb == 10) {
					raf.skipBytes(2);
					lb = 0;
				}
			}
			raf.skipBytes(2);

			// All Diagonal Elements (size=ncellt)

			ap = new double[ncellt];

			lb = 0;
			buffer = new byte[18];
			for (int i = 0; i < ap.length; i++) {
				raf.read(buffer);
				ap[i] = Double.parseDouble(new String(buffer).trim());
				lb++;
				if (lb == 10) {
					raf.skipBytes(2);
					lb = 0;
				}
			}
			raf.skipBytes(2);

			// All off-diagonal non-zero Elements (size=Nclfc)

			acoef = new double[nclfc];

			lb = 0;
			for (int i = 0; i < acoef.length; i++) {
				raf.read(buffer);
				acoef[i] = Double.parseDouble(new String(buffer).trim());
				lb++;
				if (lb == 10) {
					raf.skipBytes(2);
					lb = 0;
				}
			}
			raf.skipBytes(2);

			raf.close();

			rmf = new RMF(lclfc1, lclfc3, ap, acoef);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return rmf;
	}

	public void saveFile(File dat) {
		try {
			RandomAccessFile raf = new RandomAccessFile(dat, "rw");

			// ncellt= number of diagonal elements of A
			raf.writeBytes(String.format("% 10d", lclfc1.length - 1));
			// nclfc = total number of off-diagonal non-zero elements
			raf.writeBytes(String.format("% 10d", lclfc3.length));

			raf.writeBytes("\r\n");

			// pointer to access off-diagonal element in ACOEF given a diagonal

			int lb = 0;
			for (int i = 0; i < lclfc1.length; i++) {
				raf.writeBytes(String.format("% 10d", lclfc1[i]));
				lb++;
				if (lb == 10) {
					raf.writeBytes("\r\n");
					lb = 0;
				}
			}
			raf.writeBytes("\r\n");

			// pointer to diagonal element ID given the off-diagonal pointer

			lb = 0;
			for (int i = 0; i < lclfc3.length; i++) {
				raf.writeBytes(String.format("% 10d", lclfc3[i]));
				lb++;
				if (lb == 10) {
					raf.writeBytes("\r\n");
					lb = 0;
				}
			}
			raf.writeBytes("\r\n");

			// pointer used in the CGS solver given the off-diagonal pointer
			// TODO ainda nao sei o que lacac é

			lb = 0;
			for (int i = 0; i < lclfc3.length; i++) {
				raf.writeBytes("         0");
				lb++;
				if (lb == 10) {
					raf.writeBytes("\r\n");
					lb = 0;
				}
			}
			raf.writeBytes("\r\n");

			// All Diagonal Elements (size=ncellt)

			lb = 0;
			for (int i = 0; i < ap.length; i++) {
				raf.writeBytes(String.format(Locale.US, "% 18f", ap[i]));
				lb++;
				if (lb == 10) {
					raf.writeBytes("\r\n");
					lb = 0;
				}
			}
			raf.writeBytes("\r\n");

			// All off-diagonal non-zero Elements (size=Nclfc)

			lb = 0;
			for (int i = 0; i < acoef.length; i++) {
				raf.writeBytes(String.format(Locale.US, "% 18f", acoef[i]));
				lb++;
				if (lb == 10) {
					raf.writeBytes("\r\n");
					lb = 0;
				}
			}
			raf.writeBytes("\r\n");

			raf.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

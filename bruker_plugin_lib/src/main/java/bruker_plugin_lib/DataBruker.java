package bruker_plugin_lib;

import java.util.ArrayList;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.NDArrayIndex;

public class DataBruker extends ArrayList<INDArray> {
	private ArrayList<INDArray> data;
	private INDArray real, imag;
	private float[] realData;
	private float[] imagData;
	public DataBruker(ArrayList<INDArray> data) {
		super();
		this.data = data;
		this.real = data.get(0);
		this.imag = data.get(1);
	}

	public float[] getRealData() {
		int dim = 0;
		float[] m_absorptionChannelTDraw = getRealData(dim);
		return m_absorptionChannelTDraw;
	}
	
	public float[] getRealData(int dim) {
		try {
			long[] dims = real.shape();
			int size = (int) real.length();
			// INDArray dup_real =
			// real.reshape(real.vectorsAlongDimension(0)*dims[0]).dup();
			float[] m_absorptionChannelTDraw = new float[size];
			for (int i = 0; i < real.vectorsAlongDimension(dim); i++) {
				float[] re = real.vectorAlongDimension((int) i, dim).toFloatVector();
				System.arraycopy(re, 0, m_absorptionChannelTDraw, (int) (i * dims[0]), (int) dims[0]);
			}
			return m_absorptionChannelTDraw;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public void setRealData(float[] realData) {
		this.realData = realData;
	}
	
	public void setImagData(float[] imagData) {
		this.imagData = imagData;
	}

	public float[] getImagData() {
		int dim = 0;
		float[] m_absorptionChannelTDraw = getImagData(dim);
		return m_absorptionChannelTDraw;
	}

	public float[] getImagData(int dim) {
		try {
			long[] dims = imag.shape();
			int size = (int) imag.length();
			// INDArray dup_imag =
			// imag.reshape(imag.vectorsAlongDimension(0)*dims[0]).dup();
			float[] m_absorptionChannelTDraw = new float[size];
			for (int i = 0; i < imag.vectorsAlongDimension(dim); i++) {
				float[] re = imag.vectorAlongDimension((int) i, dim).toFloatVector();
				System.arraycopy(re, 0, m_absorptionChannelTDraw, (int) (i * dims[0]), (int) dims[0]);
			}
			return m_absorptionChannelTDraw;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public double[][][] toDoubleArray() {
		long[] fid_dims = real.shape();
		double[][][] real_array = new double[(int) fid_dims[0]][(int) fid_dims[1]][(int) fid_dims[2]];
		for (int l = 0; l < fid_dims[0]; l++)
			for (int i = 0; i < fid_dims[1]; i++) {
				for (int j = 0; j < fid_dims[2]; j++) {
					real_array[l][i][j] = real.getDouble(l, i, j);
				}
			}
		return real_array;
	}

	public float[][][] toFloatArray() {
		long[] fid_dims = real.shape();
		float[][][] real_array = new float[(int) fid_dims[0]][(int) fid_dims[1]][(int) fid_dims[2]];
		for (int l = 0; l < fid_dims[0]; l++)
			for (int i = 0; i < fid_dims[1]; i++) {
				for (int j = 0; j < fid_dims[2]; j++) {
					int[] indx = new int[] { l, i, j };
					real_array[l][i][j] = real.getFloat(indx);
				}

			}
		return real_array;
	}

	public double[][][] toMatrix(INDArray indarr) {
		long[] fid_dims = real.shape();
		double[][][] real_array = new double[(int) fid_dims[0]][(int) fid_dims[1]][(int) fid_dims[2]];
		for (int j = 0; j < fid_dims[2]; j++) {
			real_array[j] = indarr.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(j)).toDoubleMatrix();
		}
		return real_array;
	}

	public double[][][][][] get_abs_fid() {
		long[] fid_dims = real.shape();
		double[][][][][] fid = new double[(int) fid_dims[1]][(int) fid_dims[0]][(int) fid_dims[4]][(int) fid_dims[3]][(int) fid_dims[2]];
		for (int n = 0; n < fid_dims[2]; n++) {
			for (int m = 0; m < fid_dims[3]; m++) {
				for (int l = 0; l < fid_dims[4]; l++) {
					for (int i = 0; i < fid_dims[1]; i++) {
						for (int j = 0; j < fid_dims[0]; j++) {
							fid[i][j][l][m][n] = Math.sqrt(Math.sqrt(
									Math.pow(imag.getInt(j, i, l, m, n), 2) + Math.pow(real.getInt(j, i, l, m, n), 2)));
						}
					}
				}
			}
		}
		return fid;
	}
}

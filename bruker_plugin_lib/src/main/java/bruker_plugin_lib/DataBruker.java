package bruker_plugin_lib;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.util.ArrayList;

public class DataBruker extends ArrayList<INDArray> {
	private ArrayList<INDArray> data;
	public INDArray real, imag;
	private float[] realData;
	private float[] imagData;
	Bruker bruker;
	/**
	 * Constructor of DataBruker Class
	 * @param bruker : the output of the getData method of Bruker class
	 */
	public DataBruker(Bruker bruker) {
		super();
		this.bruker = bruker;
		this.data = bruker.data;
		this.real = data.get(0);
		if (bruker.isRaw())
		this.imag = data.get(1);
	}

	/**
	 * return real data
	 * @return real data
	 */
	public float[] getRealData() {
		int dim = 0;
		float[] m_absorptionChannelTDraw = getRealData(dim);
		return m_absorptionChannelTDraw;
	}

	/**
	 * return real data in desired dimension
	 * @param dim dimension
	 * @return
	 */
	public float[] getRealData(int dim) {
		if(bruker.isRaw()) {
			try {
				long[] dims = real.shape();
				int size = (int) real.length();
				// INDArray dup_imag =
				// imag.reshape(imag.vectorsAlongDimension(0)*dims[0]).dup();
				for (int i = 0; i < real.vectorsAlongDimension(dim); i++) {
					switch (bruker.getParameters().dataType) {
//					case DOUBLE:
//						Object m_absorptionChannelTDraw_double = new double[size];
//						double[] re_double = real.vectorAlongDimension((int) i, dim).toDoubleVector();
//						System.arraycopy(re_double, 0, m_absorptionChannelTDraw_double, (int) (i * dims[0]), (int) dims[0]);
//						return m_absorptionChannelTDraw_double;
					default:
						float[] m_absorptionChannelTDraw_float = new float[size];
						float[] re_float = real.vectorAlongDimension((int) i, dim).toFloatVector();
						System.arraycopy(re_float, 0, m_absorptionChannelTDraw_float, (int) (i * dims[0]), (int) dims[0]);
						return m_absorptionChannelTDraw_float;
					}
					
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		} else {
			try {
				long[] dims = bruker.getCplxDims();
				if (dims.length == 5) {
					real = real.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(0));
				}
				if (dims.length == 4) {
					real = real.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(dim));
				}
				if (dims.length == 3) {
					real = real.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(dim));
				}
				if (dims.length == 2) {
					return null;
				}
				int size = (int) real.length();
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
		return null;
	}

	public void setRealData(float[] realData) {
		this.realData = realData;
	}

	public void setImagData(float[] imagData) {
		this.imagData = imagData;
	}

	/**
	 * return imaginary data
	 * @return
	 */
	public Object getImagData() {
		if(bruker.isRaw()) {
		int dim = 0;
		Object m_absorptionChannelTDraw = getImagData(dim);
		return m_absorptionChannelTDraw; 
		} else {
			return null;
		}
	}

	/**
	 * return real data in desired dimension
	 * @param dim
	 * @return
	 */
	public float[] getImagData(int dim) {
		if(bruker.isRaw()) {
			try {
				long[] dims = imag.shape();
				int size = (int) imag.length();
				// INDArray dup_imag =
				// imag.reshape(imag.vectorsAlongDimension(0)*dims[0]).dup();
//				float[] m_absorptionChannelTDraw = new float[size];
				for (int i = 0; i < imag.vectorsAlongDimension(dim); i++) {
					switch (bruker.getParameters().dataType) {
//					case DOUBLE:
//						Object m_disportionChannelTDraw_double = new double[size];
//						double[] im_double = imag.vectorAlongDimension((int) i, dim).toDoubleVector();
//						System.arraycopy(im_double, 0, m_disportionChannelTDraw_double, (int) (i * dims[0]), (int) dims[0]);
//						return m_disportionChannelTDraw_double;
					default:
						float[] m_disportionChannelTDraw_float = new float[size];
						float[] re_float = real.vectorAlongDimension((int) i, dim).toFloatVector();
						System.arraycopy(re_float, 0, m_disportionChannelTDraw_float, (int) (i * dims[0]), (int) dims[0]);
						return m_disportionChannelTDraw_float;
					}
					
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		} else {
			try {
				long[] dims = bruker.getCplxDims();
				if (dims.length == 5) {
					imag = imag.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(1));
				}
				if (dims.length == 4) {
					imag = imag.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(1));
				}
				if (dims.length == 3) {
					imag = imag.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(1));
				}
				if (dims.length == 2) {
					return null;
				}
				int size = (int) imag.length();
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
		return null;
	}

	/**
	 * Convert INDArray to double array
	 * @return 3 dimension double array
	 */
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

	/**
	 * Convert INDArray to float array
	 * @return 3 dimension float array
	 */
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

	/**
	 * Convert INDArray to double array
	 * @param indarr
	 * @return 3 dimension double array
	 */
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

package bruker_plugin_lib;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.nd4j.linalg.util.ArrayUtil;

import com.rits.cloning.IInstantiationStrategy;

public class Reco {
	private String path;
	private Map<String, Object> reco;
	private Map<String, Object> visu_pars;
	private Object FG_TYPES;
	private DataInputStream buffReader;
	private int BD;
	private ArrayList<Integer> ArrBD = new ArrayList<Integer>();
	private Object BD_f;
	public INDArray data, real, imag;
	private INDArray ndarr;
	private List<Object> list_scan_result;
	private Map<String, Object> acqp;
	private Map<String, Object> method;
	private String Mpath;
	private String cmplx_flag = null;

	public String ident_ACQS_TYPE() {
        Integer ACQ_dim = (Integer) acqp.get("ACQ_dim");
        Integer NPro;
        Integer NECHOES;
        String CSISignalType;
        try {
                      NPro = (Integer) method.get("NPro");
        } catch (Exception e) {
                      NPro = null;
        }
        try {
                      NECHOES = (Integer) acqp.get("NECHOES");
        } catch (Exception e) {
                      NECHOES = null;
        }                           
        try {
                      CSISignalType = (String) method.get("CSISignalType");
        } catch (Exception e) {
                      CSISignalType = null;
        }
        String ACQS_TYPE;
        if(NPro != null && ACQ_dim ==2) {
                      ACQS_TYPE = "RADIAL_2D";
        } else if(NPro != null && ACQ_dim ==3) {
                      ACQS_TYPE = "RADIAL_3D";
        } else if(CSISignalType != null) {
                      ACQS_TYPE = "CSI";
        } else if(ACQ_dim == 3) {
                      ACQS_TYPE = "CART_3D";
        } else {
                      ACQS_TYPE = "CART_2D";
        }
        return ACQS_TYPE;
}

	public double[][][] toDoubleArray(INDArray indarr) {
		long[] fid_dims = indarr.shape();
		double[][][] ret = new double[(int) fid_dims[0]][(int) fid_dims[1]][(int) fid_dims[2]];
		for (int l = 0; l < fid_dims[0]; l++)
			for (int i = 0; i < fid_dims[1]; i++) {
				for (int j = 0; j < fid_dims[2]; j++) {
					ret[l][i][j] = indarr.getDouble(l, i, j);
				}

			}
		return ret;
	}

	public float[][][] toFloatArray(INDArray indarr) {
		long[] fid_dims = indarr.shape();
		float[][][] ret = new float[(int) fid_dims[0]][(int) fid_dims[1]][(int) fid_dims[2]];
		for (int l = 0; l < fid_dims[0]; l++)
			for (int i = 0; i < fid_dims[1]; i++) {
				for (int j = 0; j < fid_dims[2]; j++) {
					int[] indx = new int[] { l, i, j };
					ret[l][i][j] = indarr.getFloat(indx);
				}

			}
		return ret;
	}

	public double[][][] toMatrix(INDArray indarr) {

		long[] fid_dims = indarr.shape();
		double[][][] ret = new double[(int) fid_dims[0]][(int) fid_dims[1]][(int) fid_dims[2]];
//					for(int l=0; l< fid_dims[0];l++) {
//						for(int i=0; i< fid_dims[1];i++) {
//							for(int j=0; j< fid_dims[2];j++) {
//								ret[l][i][j] = indarr.getDouble(l,i,j);
//								//System.out.println(n+ "," +m+ "," +l+ "," +i+ "," +j+ "," + ret[n][m][l][i][j]);
//							}}}

		for (int j = 0; j < fid_dims[2]; j++) {
			ret[j] = indarr.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(j)).toDoubleMatrix();
		}
//	        for(int i = 0; i < ret.length; i++) {
//	            ret[i] = getRow(i).dup().data().asDouble();
//	        }

		return ret;
	}
	private boolean CheckFileExist(String Filename) {
		if(list_scan_result.contains(Filename)) {
			return true;
		} else {
			return false;
		}
	}

	public void list_scan_dir(String path) {
		try (Stream<Path> walk = Files.walk(Paths.get(path))) {
			list_scan_result = walk.filter(Files::isRegularFile).map(x -> x.getFileName().toString()).collect(Collectors.toList());
			// result.forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	public boolean ACQ_Parameter_Reco(String TAG) {
		try {
			ndarr = (NDArray) reco.get(TAG);
			if (ndarr == null) {
				ndarr = (NDArray) visu_pars.get(TAG);
				if(ndarr != null) return true;
			}
			if (ndarr == null) {
				//System.out.println("The key was not found or null:code=-1");
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("parameter extraction got an error");
			return false;
		}
		return true;
	}
	public boolean ACQ_Parameter_Scan(String TAG) {
		try {
			ndarr = (NDArray) acqp.get(TAG);
			if (ndarr == null) {
				ndarr = (NDArray) method.get(TAG);
				if(ndarr != null) return true;
			}
			if (ndarr == null) {
				//System.out.println("The key was not found or null:code=-1");
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("parameter extraction got an error");
			return false;
		}
		return true;
	}
	public double[][][] getDouble3DMatrix(String TAG) {
		if(ACQ_Parameter_Reco(TAG)==false) {
			if(ACQ_Parameter_Scan(TAG)==false) {
				System.out.println("The key was not found or null in visu_pars , veco, method, acqp:code=-1");
			};
		}
		if (ndarr.rank() > 2) {
			return toDoubleArray(ndarr);
		} else {
			System.out.println("the input is not a 3d matrix");
			return null;
		}
	}

	public float[][][] getFloat3DMatrix(String TAG) {
		if(ACQ_Parameter_Reco(TAG)==false) {
			if(ACQ_Parameter_Scan(TAG)==false) {
				System.out.println("The key was not found or null in visu_pars , veco, method, acqp:code=-1");
			};
		}
		if (ndarr.rank() > 2) {
			return toFloatArray(ndarr);
		} else {
			System.out.println("the input is not a 3d matrix");
			return null;
		}
	}

	public double[] getDoubleVector(String TAG) {
		if(ACQ_Parameter_Reco(TAG)==false) {
			if(ACQ_Parameter_Scan(TAG)==false) {
				System.out.println("The key was not found or null in visu_pars , veco, method, acqp:code=-1");
			};
		}
		if (ndarr.isVector()) {
			double[] double_param = ndarr.data().asDouble();
			return double_param;
		} else {
			System.out.println("the input is not a Vector");
			return null;
		}
	}
	public String[] getStringVector(String TAG) {
		
		String[] strStr = null;
		try {
			List<String> strArr = (List<String>) reco.get(TAG);
			strStr = strArr.toArray(new String[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strStr;
	}

	public double[][] getDoubleMatrix(String TAG) {
		if(ACQ_Parameter_Reco(TAG)==false) {
			if(ACQ_Parameter_Scan(TAG)==false) {
				System.out.println("The key was not found or null in visu_pars , veco, method, acqp:code=-1");
			};
		}
		if (ndarr.isMatrix()) {
			double[][] double_param = ndarr.toDoubleMatrix();
			return double_param;
		} else {
			System.out.println("the input is not a Matrix");
			return null;
		}

	}

	public float[] getFloatVector(String TAG) {
		if(ACQ_Parameter_Reco(TAG)==false) {
			if(ACQ_Parameter_Scan(TAG)==false) {
				System.out.println("The key was not found or null in visu_pars , veco, method, acqp:code=-1");
			};
		}
		if (ndarr.isVector()) {
			float[] float_param = ndarr.data().asFloat();
			return float_param;
		} else {
			System.out.println("the input is not a Vector");
			return null;
		}
	}

	public float[][] getFloatMatrix(String TAG) {
		if(ACQ_Parameter_Reco(TAG)==false) {
			if(ACQ_Parameter_Scan(TAG)==false) {
				System.out.println("The key was not found or null in visu_pars , veco, method, acqp:code=-1");
			};
		}
		if (ndarr.isMatrix()) {
			float[][] float_param = ndarr.toFloatMatrix();
			return float_param;
		} else {
			System.out.println("the input is not a matrix");
			return null;
		}

	}
	public Float getFloat(String TAG) {
		Float param = null;
		boolean float_flag = false;
		boolean int_flag = false;
		boolean ndArray_flag = false;
			try {
				float_flag = visu_pars.get(TAG).getClass().getSimpleName().contentEquals("Float");
				int_flag = visu_pars.get(TAG).getClass().getSimpleName().contentEquals("Integer");
				ndArray_flag = visu_pars.get(TAG).getClass().getSimpleName().contentEquals("NDArray");
			} catch (Exception e) {
						System.out.println("there is no" + TAG + "parameter in visu_pars");
			};
			try {
					float_flag = reco.get(TAG).getClass().getSimpleName().contentEquals("Float");
					int_flag = reco.get(TAG).getClass().getSimpleName().contentEquals("Integer");
					ndArray_flag = reco.get(TAG).getClass().getSimpleName().contentEquals("NDArray");
			} catch (Exception e2) {
						System.out.println("there is no" + TAG + "parameter in reco");
			};
			try {
				float_flag = acqp.get(TAG).getClass().getSimpleName().contentEquals("Float");
				int_flag = acqp.get(TAG).getClass().getSimpleName().contentEquals("Integer");
				ndArray_flag = acqp.get(TAG).getClass().getSimpleName().contentEquals("NDArray");
			} catch (Exception e2) {
						System.out.println("there is no" + TAG + "parameter in acqp");
			}
			try {
				float_flag = method.get(TAG).getClass().getSimpleName().contentEquals("Float");
				int_flag = method.get(TAG).getClass().getSimpleName().contentEquals("Integer");
				ndArray_flag = method.get(TAG).getClass().getSimpleName().contentEquals("NDArray");
			} catch (Exception e2) {
				System.out.println("there is no " + TAG + "parameter in method");
			}
	
		if (float_flag) {
		try {
			param = (Float) visu_pars.get(TAG);
		} catch (Exception e) {
			System.out.println(e);
		}
		if (param == null) {
			try {
				param = (Float) reco.get(TAG);
			} catch (Exception e1) {
				System.out.println(e1);
			}
		}
		if (param == null) {
			try {
				param = (Float) acqp.get(TAG);
			} catch (Exception e1) {
				System.out.println(e1);
			}
		}
		if (param == null) {
			try {
				param = (Float) method.get(TAG);
			} catch (Exception e1) {
				System.out.println(e1);
			}
		}
		} else if(int_flag) {
			Integer int_param = null;
			try {
				int_param = (Integer) visu_pars.get(TAG);
			} catch (Exception e) {
				System.out.println(e);
			}
			if (int_param == null) {
				try {
					int_param = (Integer) reco.get(TAG);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
			if (int_param == null) {
				try {
					int_param = (Integer) acqp.get(TAG);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
			if (int_param == null) {
				try {
					int_param = (Integer) method.get(TAG);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
			param = int_param.floatValue();	
			} else if(ndArray_flag) {
			NDArray nd_param = null;
			try {
				nd_param = (NDArray) visu_pars.get(TAG);
			} catch (Exception e) {
				System.out.println(e);
			}
			if (nd_param == null) {
				try {
					nd_param = (NDArray) reco.get(TAG);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
			if (nd_param == null) {
				try {
					nd_param = (NDArray) acqp.get(TAG);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
			if (nd_param == null) {
				try {
					nd_param = (NDArray) method.get(TAG);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
			
			param = nd_param.getFloat(0);	
			}
		return param;
	}
//	public Float getFloat(String TAG) {
//		Float param = null;
//		try {
//			param = (Float) visu_pars.get(TAG);
//		} catch (Exception e) {
//			System.out.println(e);
//		}
//		if (param == null) {
//			try {
//				param = (Float) reco.get(TAG);
//			} catch (Exception e1) {
//				System.out.println(e1);
//			}
//		}
//		if (param == null) {
//		try {
//			param = (Float) acqp.get(TAG);
//		} catch (Exception e) {
//			System.out.println(e);
//		}
//		if (param == null) {
//			try {
//				param = (Float) method.get(TAG);
//			} catch (Exception e1) {
//				System.out.println(e1);
//			}
//		  }
//		}
//		return param;
//	}

	public int getInt(String TAG) {
		Integer param = null;
		try {
			param = (Integer) visu_pars.get(TAG);
		} catch (Exception e) {
			System.out.println(e);
		}
		if (param == null) {
			try {
				param = (Integer) reco.get(TAG);
			} catch (Exception e1) {
				System.out.println(e1);
			}
		}
		if (param == null) {
		try {
			param = (Integer) acqp.get(TAG);
		} catch (Exception e) {
			System.out.println(e);
		}
		if (param == null) {
			try {
				param = (Integer) method.get(TAG);
			} catch (Exception e1) {
				System.out.println(e1);
			}
		  }
		}
		return param;
	}

	public double getDouble(String TAG) {
		Double param = null;
		try {
			param = (Double) visu_pars.get(TAG);
		} catch (Exception e) {
			System.out.println(e);
		}
		if (param == null) {
			try {
				param = (Double) reco.get(TAG);
			} catch (Exception e1) {
				System.out.println(e1);
			}
		}
		if (param == null) {
		try {
			param = (Double) acqp.get(TAG);
		} catch (Exception e) {
			System.out.println(e);
		}
		if (param == null) {
			try {
				param = (Double) method.get(TAG);
			} catch (Exception e1) {
				System.out.println(e1);
			}
		  }
		}
		return param;
	}

	public String getString(String TAG) {
		String param = null;
		try {
			param = (String) visu_pars.get(TAG);
		} catch (Exception e) {
			System.out.println(e);
		}
		if (param == null) {
			try {
				param = (String) reco.get(TAG);
			} catch (Exception e1) {
				System.out.println(e1);
			}
		}
		if (param == null) {
		try {
			param = (String) acqp.get(TAG);
		} catch (Exception e) {
			System.out.println(e);
		}
		if (param == null) {
			try {
				param = (String) method.get(TAG);
			} catch (Exception e1) {
				System.out.println(e1);
			}
		  }
		}
		return param;
	}
	public long[] get_complex_dims() {
		return data.shape();
	}
	public long[] get_dims() {
		long[] dims = get_complex_dims();
		if (dims.length == 5) {
			real = data.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(0));
		} 
		if (dims.length == 4) {
			real = data.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(0));
		} 
		if (dims.length == 3) {
			real = data.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(0));
		}
		if (dims.length == 2) {
			real = data.get(NDArrayIndex.all(), NDArrayIndex.all());
		}
		return real.shape();
	}
	public float[] getRealData(int dim) {
		try {		
			long[] dims = get_complex_dims();
			get_dims();
			int size = (int) real.length();
			long[] realshap = real.shape();
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
	public float[] getImagData(int dim) {
		try {
			long[] dims = get_complex_dims();
			if (dims.length == 5) {
				imag = data.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(1));
			} 
			if (dims.length == 4) {
				imag = data.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(1));
			} 
			if (dims.length == 3) {
				imag = data.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(1));
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
	public Reco(String path) throws EOFException, IOException {
		this.Mpath = path;
		initialize();
	}
	private Object[] initialize() throws EOFException, IOException {
		String path = this.Mpath;
		list_scan_dir(path);
		this.path = path;
		list_scan_dir(path);
		if (CheckFileExist("visu_pars") && CheckFileExist("reco")) {
		reco = Jcampdx.read_jcampdx_file(path + "reco");
		visu_pars = Jcampdx.read_jcampdx_file(path + "visu_pars");
		Path pathL = Paths.get(path);
		acqp = Jcampdx.read_jcampdx_file(pathL.getParent().getParent() + "/acqp");
		method = Jcampdx.read_jcampdx_file(pathL.getParent().getParent() + "/method");
		ident_fg_types();
		read_2dseq();
		} else {
			System.out.println("the visu_pars or reco dosn't exist");
		}
		return new Object[] { data };	
	}
	private void read_2dseq() throws EOFException, IOException {
		// TODO Auto-generated method stub
		read_2dseq_file();
		reshape_2dseq();
		scale();
		form_frame_groups();
		form_complex();
		long[] dims = data.shape();
	}
	private void form_complex() {
		// TODO Auto-generated method stub
		/*
		 * if not FG_TYPE.FG_COMPLEX in self.FG_TYPES: return reco_mat
		 */
        Object VisuFGOrderDesc = visu_pars.get("VisuFGOrderDesc");
		int VisuCoreDim = (int) visu_pars.get("VisuCoreDim");
		int complex_dim = 0;
//		for(int fg_indx=0; fg_indx< VisuFGOrderDesc.length(); fg_indx++) {
//			INDArray temp = VisuFGOrderDesc.get(NDArrayIndex.point(fg_indx),NDArrayIndex.point(1));
//			if(temp.equals("FG_COMPLEX")) {
//			complex_dim = VisuCoreDim + fg_indx;
//			}
//		}
		for(int dim=0;dim<data.shape().length; dim++) {
			if(dim == complex_dim) {
/*		        for dim in range(0,len(reco_mat.shape)):
		            if dim == complex_dim:
		                slice_real += (0,)
		                slice_imag += (1,)
		            else:
		                slice_real += (slice(0,reco_mat.shape[dim]),)
		                slice_imag += (slice(0,reco_mat.shape[dim]),)

		        return np.squeeze(reco_mat[slice_real]) + 1j * np.squeeze(reco_mat[slice_imag])*/
			} else {
				
			}
		}
	}
	private void form_frame_groups() {
		// TODO Auto-generated method stub
		ArrayList<Object> VisuFGOrderDesc = (ArrayList<Object>) visu_pars.get("VisuFGOrderDesc");
		int VisuFGOrderDescDim = 0;
		try {
			VisuFGOrderDescDim = (int) visu_pars.get("VisuFGOrderDescDim");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("VisuFGOrderDescDim not found");
		}
		INDArray VisuCoreSize = (INDArray) visu_pars.get("VisuCoreSize");
		int[] fg_dims = new int[VisuFGOrderDescDim];
		
		
		for(int i=0; i< VisuFGOrderDescDim; i++) {
			Object[] obj = (Object[]) VisuFGOrderDesc.get(i);
			String str_obj = obj[0].toString();
			cmplx_flag = obj[1].toString();
			int int_obj = Integer.parseInt(str_obj);
			fg_dims[i] = int_obj;
		}
		
		int[] reshape_scheme = new int[(int) (VisuCoreSize.length() + fg_dims.length)];
        for (int i=0; i < VisuCoreSize.length(); i++) {
        	reshape_scheme[i] = VisuCoreSize.getInt(i);
        }
        for (int i=(int) VisuCoreSize.length(); i < VisuCoreSize.length() + fg_dims.length; i++) {
        	reshape_scheme[i] = fg_dims[(int) (i-VisuCoreSize.length())];
        }
		//int[] reshape_scheme = fg_dims;
        data = data.reshape('f',reshape_scheme);
    	//imag = imag.reshape('f',reshape_scheme);
	}
	private void scale() {
		// TODO Auto-generated method stub
		INDArray VisuCoreDataSlope = (INDArray) visu_pars.get("VisuCoreDataSlope");
		Object VisuCoreDataOffs = visu_pars.get("VisuCoreDataOffs)");
		int VisuCoreFrameCount = (int) visu_pars.get("VisuCoreFrameCount");
		int VisuCoreDim = (int) visu_pars.get("VisuCoreDim");
		long[] fid_dims = data.shape();
		for(int i=0; i<VisuCoreFrameCount; i++) {
			
			if(VisuCoreDim == 1) {
				data.getColumn(i).add(VisuCoreDataSlope.getFloat(i));
			}
			else if(VisuCoreDim == 2) {
				for(int j=0; j < fid_dims[2]; j++) {
				INDArrayIndex[] indx = {NDArrayIndex.all(),NDArrayIndex.all(),NDArrayIndex.point(j)};
				data.get(indx).add(VisuCoreDataSlope.getFloat(i));
				}
			}	
			else if(VisuCoreDim == 3) {
				for(int j=0; j<fid_dims[3]; j++) {
					INDArrayIndex[] indx = {NDArrayIndex.all(),NDArrayIndex.all(),NDArrayIndex.all(),NDArrayIndex.point(j)};
					data.get(indx).add(VisuCoreDataSlope.getFloat(i));
					}
			}
		}
	}
	private void reshape_2dseq() {
		// TODO Auto-generated method stub
		INDArray VisuCoreSize = (INDArray) visu_pars.get("VisuCoreSize");
		int VisuCoreFrameCount = (int) visu_pars.get("VisuCoreFrameCount");
		data = Nd4j.zeros(ArrBD.size());
        //imag = Nd4j.zeros(ArrBD.size()/2);
        for(int i = 0;i<ArrBD.size();i++) {
       	 data.putScalar(i,  ArrBD.get(i));
       	 //imag.putScalar(i/2,  ArrBD.get(i+1));
        }
        int[] reshape_scheme = new int[(int) (VisuCoreSize.length() + 1)];
        for (int i=0; i < VisuCoreSize.length(); i++) {
        	reshape_scheme[i] = VisuCoreSize.getInt(i);
        }
        reshape_scheme[(int) (VisuCoreSize.length())] = VisuCoreFrameCount;
        //int[] reshape_scheme = new int[] {VisuCoreSize.getInt(0), VisuCoreSize.getInt(1), VisuCoreFrameCount};
        data = data.reshape('f',reshape_scheme);
    	//imag = imag.reshape('f',reshape_scheme);
	}
	private void read_2dseq_file() throws EOFException, IOException {
		// TODO Auto-generated method stub
		String inputFile = path + "2dseq";
        FileInputStream dataStream = new FileInputStream(inputFile);
        DataInputStream dataFilter = new DataInputStream(dataStream);
        buffReader = new DataInputStream(new BufferedInputStream(dataFilter));
        dtype();
	}
	private void dtype() throws EOFException, IOException {
		String VisuCoreWordType = null;
		String VisuCoreByteOrder = null;
		try {
			VisuCoreWordType = (String) visu_pars.get("VisuCoreWordType");
			VisuCoreByteOrder = (String) visu_pars.get("VisuCoreByteOrder");
		} catch (Exception e) {
			System.out.println("Parameters missing: VisuCoreByteOrder, or VisuCoreByteOrder");
		}
		if(VisuCoreWordType.contentEquals("_32BIT_SGN_INT") && VisuCoreByteOrder.contentEquals("littleEndian")) {
			rev_int32_conversion();
        } else if(VisuCoreWordType.contentEquals("_16BIT_SGN_INT") && VisuCoreByteOrder.contentEquals("littleEndian")) { 
        	rev_int16_conversion();
        } else if(VisuCoreWordType.contentEquals("_32BIT_FLOAT") && VisuCoreByteOrder.contentEquals("littleEndian")) {
        	rev_float_conversion();
        } else if(VisuCoreWordType.contentEquals("_32BIT_SGN_INT") && VisuCoreByteOrder.contentEquals("bigEndian")) {
        	int_conversion();
        } else if(VisuCoreWordType.contentEquals("_16BIT_SGN_INT") && VisuCoreByteOrder.contentEquals("bigEndian")) {
        	int16_conversion();
        } else if(VisuCoreWordType.contentEquals("_32BIT_FLOAT") && VisuCoreByteOrder.contentEquals("bigEndian")) {
        	float_conversion();
        } else {
        	rev_int32_conversion();
        	System.out.println("Data format not specified correctly, set to int32, little endian");
        }
	}
		private void rev_int32_conversion() throws EOFException, IOException {
			while(buffReader.available()>0) {
	        	int ch1 = buffReader.read();
	            int ch2 = buffReader.read();
	            int ch3 = buffReader.read();
	            int ch4 = buffReader.read();
	            if ((ch1 | ch2 | ch3 | ch4) < 0)
	                throw new EOFException();
	            BD = ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
	            ArrBD.add(BD);
	      		}
		}
		private void int_conversion() throws IOException {
			while(buffReader.available()>0) {
	            BD = buffReader.readInt();;
	            ArrBD.add(BD);
	      		} 
		}
		private void rev_int16_conversion() throws EOFException, IOException {
			while(buffReader.available()>0) {
	        	int ch1 = buffReader.read();
	            int ch2 = buffReader.read();
	            if ((ch1 | ch2 ) < 0)
	                throw new EOFException();
	            BD = ((ch2 << 8) + (ch1 << 0));
	            ArrBD.add(BD);
	      		} 
		}
		private void int16_conversion() throws EOFException, IOException {
			while(buffReader.available()>0) {
	        	int ch1 = buffReader.read();
	            int ch2 = buffReader.read();
	            if ((ch1 | ch2 ) < 0)
	                throw new EOFException();
	            BD = ((ch1 << 8) + (ch2 << 0));
	            ArrBD.add(BD);
	      		} 
		}
		private void rev_float_conversion() throws IOException {
			while(buffReader.available()>0) {
	        	int ch1 = buffReader.read();
	            int ch2 = buffReader.read();
	            int ch3 = buffReader.read();
	            int ch4 = buffReader.read();
	            if ((ch1 | ch2 | ch3 | ch4) < 0)
	                throw new EOFException();
	            BD_f = Float.intBitsToFloat(((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0)));
	            ArrBD.add(BD);
	      		} 
		}
		private void float_conversion() throws IOException {
			while(buffReader.available()>0) {
	            BD_f = buffReader.readFloat();
	            ArrBD.add(BD);
	      		} 
		}
	private void ident_fg_types() {
		FG_TYPES = visu_pars.get("VisuFGOrderDesc");
	}
}

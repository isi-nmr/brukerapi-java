package bruker_plugin_lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JcampdxData {
	private Map<String, Object> parameters;
	private Logger logger = LoggerFactory.getLogger(JcampdxData.class);

	/**
	 * constructor of JcampdxData Object
	 * @param parameters map of parameters : acqp, method,...
	 */
	public JcampdxData(Map<String, Object> parameters) {
		super();
		this.parameters = parameters;
	}

	/**
	 * get string type parameter
	 * @param TAG : the key string
	 * @return String parameter
	 */
	public String getString(String TAG) {
		String param = null;
		try {
			param = (String) parameters.get(TAG);
		} catch (Exception e) {
			logger.error("the {} file is missing", TAG);
		}
		return param;
	}

	/**
	 * get float type parameter
	 * @param TAG : the key string
	 * @param defValue defult value
	 * @return float parameter
	 */
	public Float getFloat(String TAG, float defValue) {
		Float floatValue = getFloat(TAG);
		if(floatValue==null)  {
			floatValue = defValue;
			logger.error("Problem to load {}. Default value used: {}", TAG, defValue);
		}

		return floatValue;
	}

	/**
	 * get float type parameter without default parameter
	 * @param TAG
	 * @return
	 */
	public Float getFloat(String TAG) {
		Float param = null;
		boolean float_flag = false;
		boolean int_flag = false;
		boolean ndArray_flag = false;
		try {
			float_flag = parameters.get(TAG).getClass().getSimpleName().contentEquals("Float");
			int_flag = parameters.get(TAG).getClass().getSimpleName().contentEquals("Integer");
			ndArray_flag = parameters.get(TAG).getClass().getSimpleName().contentEquals("NDArray");
		} catch (Exception e) {
			logger.error("the {} file is missing", TAG);
		}
		;
		if (float_flag) {
			try {
				param = (Float) parameters.get(TAG);
			} catch (Exception e) {
				logger.error("The reeor is {}", e);
			}
		} else if (int_flag) {
			Integer int_param = null;
			try {
				int_param = (Integer) parameters.get(TAG);
			} catch (Exception e) {
				logger.error("The reeor is {}", e);
			}
			param = int_param.floatValue();
		} else if (ndArray_flag) {
			NDArray nd_param = null;
			try {
				nd_param = (NDArray) parameters.get(TAG);
			} catch (Exception e) {
				logger.error("The reeor is {}", e);
			}
			param = nd_param.getFloat(0);
		}
		return param;
	}


	/**
	 * get Integer type parameter
	 * @param TAG : : the key string
	 * @return
	 */
	public Integer getInt(String TAG) {
		Integer param = null;
		boolean float_flag = false;
		boolean int_flag = false;
		boolean ndArray_flag = false;
		try {
			float_flag = parameters.get(TAG).getClass().getSimpleName().contentEquals("Float");
			int_flag = parameters.get(TAG).getClass().getSimpleName().contentEquals("Integer");
			ndArray_flag = parameters.get(TAG).getClass().getSimpleName().contentEquals("NDArray");
		} catch (Exception e) {
			logger.error("the {} file is missing", TAG);
		};
		if (float_flag) {
			Float float_param = null;
			try {
				float_param = (Float) parameters.get(TAG);
			} catch (Exception e) {
				logger.error("The reeor is {}", e);
			}
			param = float_param.intValue();
		} else if (int_flag) {
			try {
				param = (Integer) parameters.get(TAG);
			} catch (Exception e) {
				logger.error("The reeor is {}", e);
			}
		} else if (ndArray_flag) {
			NDArray nd_param = null;
			try {
				nd_param = (NDArray) parameters.get(TAG);
			} catch (Exception e) {
				logger.error("The reeor is {}", e);
			}
			param = nd_param.getInt(0);
		}
		return param;
	}

	/**
	 * get INDArray type in case the parameter is an array of diffrent type
	 * @param TAG : the key string
	 * @return
	 */
	public INDArray getINDArray(String TAG) {
		INDArray param = null;
		boolean float_flag = false;
		boolean int_flag = false;
		boolean ndArray_flag = false;
		try {
			float_flag = parameters.get(TAG).getClass().getSimpleName().contentEquals("Float");
			int_flag = parameters.get(TAG).getClass().getSimpleName().contentEquals("Integer");
			ndArray_flag = parameters.get(TAG).getClass().getSimpleName().contentEquals("NDArray");
		} catch (Exception e) {
			logger.error("the {} file is missing", TAG);
		};
		if (float_flag) {
			Float float_param = null;
			try {
				float_param = (Float) parameters.get(TAG);
			} catch (Exception e) {
				logger.error("The reeor is {}", e);
			}
			param = param.add(float_param);
		} else if (int_flag) {
			Integer int_param = null;
			try {
				int_param = (Integer) parameters.get(TAG);
			} catch (Exception e) {
				logger.error("The reeor is {}", e);
			}
			param = param.add(int_param);
		} else if (ndArray_flag) {
			try {
				param = (NDArray) parameters.get(TAG);
			} catch (Exception e) {
				logger.error("The reeor is {}", e);
			}
		}
		return param;
	}

	/**
	 * get array list type parameter
	 * @param TAG : the key string
	 * @return
	 */
	public ArrayList getArrayList(String TAG) {
		ArrayList param = new ArrayList();
		try {
			param =  (ArrayList) parameters.get(TAG);
		} catch (Exception e) {
			logger.error("the {} file is missing", TAG);
		}
		return param;
	}

	/**
	 * get field of view
	 * @param fov  : default value
	 * @return float array
	 */
	public float[] getFov(float[] fov)
	{
		float[] FOV = null;
		String[] dic = {"PVM_FovCm","RECO_fov", "ACQ_fov"};
		for(String element:dic) {
			try {
				FOV = getINDArray(element).data().asFloat();
				if(FOV != null) {
					break;
				}
			} catch(Exception exception) {

			}
		}
		if(FOV == null) {
			FOV = fov;
			logger.error("Problem to load FOV. Default value used: {}", fov);
		}
		//why???
		for(int i=0; i<FOV.length;i++)
			FOV[i]*=10;
		return FOV;
	}

	/**
	 * get the field of view of Voxel of Interst
	 * @param fov : default
	 * @return
	 */
	public float[] getFovVoi(float[] fov)
	{
		float [] FOV = getINDArray("PVM_VoxArrSize").data().asFloat();
		if(FOV==null )
		{
			FOV = fov;
			logger.error("Problem to load FOV. Default value used: {}", fov);
		}
		return FOV;
	}

	/**
	 * get 3d matrix type parameters
	 * @param TAG : the key string
	 * @return 3d float
	 */
	public float[][][] getFloat3DMatrix(String TAG) {
		INDArray ndarr = getINDArray(TAG);
		if (ndarr.rank() > 2) {
			long[] fid_dims = ndarr.shape();
			float[][][] ret = new float[(int) fid_dims[0]][(int) fid_dims[1]][(int) fid_dims[2]];
			for (int l = 0; l < fid_dims[0]; l++)
				for (int i = 0; i < fid_dims[1]; i++) {
					for (int j = 0; j < fid_dims[2]; j++) {
						int[] indx = new int[] { l, i, j };
						ret[l][i][j] = ndarr.getFloat(indx);
					}
				}
			return ret;
		} else {
			logger.error("the input {} is not a 3d matrix", TAG);
			return null;
		}
	}

	/**
	 * get an array of Read, Phase and Slice positions
	 * @param pos
	 * @return
	 */
	public float[] getPositionRPS(float[] pos)
	{
		// To do : make sure the pos has three args
		pos[0]= -getFloat("PVM_SPackArrReadOffset",pos[0]);
		pos[1]= -getFloat("PVM_SPackArrPhase1Offset",pos[1]);
		pos[2]= -getFloat("PVM_SPackArrSliceOffset",pos[2]) ;
		return pos;
	}

	/**
	 * get position of voxel of interst
	 * @param defValue
	 * @return
	 */
	public float[] getPositionVoi( float[] defValue){
		float[] positionVoi = getINDArray("PVM_VoxArrPosition").data().asFloat();
		if(positionVoi==null )
		{
			positionVoi = defValue;
			logger.error("Problem to load FOV. Default value used: {}", defValue);
		}

		for(int i=0; i<positionVoi.length;i++)
			positionVoi[i]*=-1;
		return positionVoi;
	}
	/**
	 * determine that data is in KSpace or not
	 * @return
	 */
	boolean isKspace()
	{
		String version = getString("PV");
		if(version!=null)
		{
			String[] ver = version.split("\\.");
			if(ver!=null)
			{
				try
				{
					int v = Integer.parseInt(ver[0]);
					if(v>5)
						return false;
				}
				catch (NumberFormatException nfe)
				{
					logger.error("Problem to detect version of Paravision.");
				}
			}
		}
		return true;
	}

	/**
	 * get Nucleus of study
	 * @return
	 */
	public String getNucleus()
	{
		return getString("PVM_Nucleus1Enum");
	}

	/**
	 * get resonance frequency of study
	 * @param defValue
	 * @return
	 */
	public double getResonaceFreq(float defValue)
	{
		return getFloat("SFO1",defValue);
	}

	/**
	 * get slice thickness of study
	 * @param defValue
	 * @return
	 */
	public float getSliceThick(float defValue)
	{
		return getFloat("ACQ_slice_thick",defValue);
	}

	/**
	 * get TE of study
	 * @param defValue
	 * @return
	 */
	public float getTE(float defValue)
	{
		return  getFloat("PVM_EchoTime",defValue);
	}

	/**
	 * get TR of study
	 * @param defValue
	 * @return
	 */
	public float getTR(float defValue)
	{
		return  getFloat("PVM_RepetitionTime",defValue);
	}

	/**
	 * get spectral width
	 * @param defValue
	 * @return
	 */
	public float getSW(float defValue)
	{
		return  getFloat("SW_h",defValue);
	}

	/**
	 * get the gradient matrix
	 * @param dim
	 * @param defGradMatrix default value
	 * @return 2d matrix
	 */
	public float[][] getGradMatrix(int dim, float[][] defGradMatrix)
	{
		float[][] gradMatrix = getFloat3DMatrix("ACQ_grad_matrix")[0];
		if(gradMatrix==null && dim<gradMatrix.length) {
			gradMatrix = defGradMatrix;
			logger.error("Problem to load Grad Matrix. Default value used: {}", defGradMatrix.toString());
		}
		return gradMatrix;
	}

	/**
	 * get the gradient matrix of voxel of Interest
	 * @param dim
	 * @param defGradMatrix
	 * @return
	 */
	public float[][] getGradMatrixVoi(int dim, float[][] defGradMatrix)
	{
		float[][] gradMatrix = getFloat3DMatrix("PVM_VoxArrGradOrient")[0];
		if(gradMatrix==null && dim<gradMatrix.length) {
			gradMatrix = defGradMatrix;
			logger.error("Problem to load Grad Matrix. Default value used: {}", defGradMatrix.toString());
		}
		return gradMatrix;
	}

	public String[] getStringVector(String TAG) {

		String[] strStr = null;


			try {
				List<String> strArr = (List<String>) parameters.get(TAG);
				if (strArr != null)
					strStr = strArr.toArray(new String[0]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		return strStr;
	}

}

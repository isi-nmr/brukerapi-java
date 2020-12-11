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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.util.ArrayUtil;
import com.google.common.primitives.Ints;

// slack is connected
public class Scan {
	private DataInputStream dataFilter;
	private DataInputStream buffReader;
	private Integer BD;
	private Float BD_f;
	private INDArray real, imag;
	private int[] reshape_scheme_1, reshape_scheme_2, permute_scheme_1, reshape_scheme, permute_scheme;
	private ArrayList<Integer> ArrBD = new ArrayList<Integer>();
	private Map<String, Object> acqp;
	private Map<String, Object> method;
	private INDArray ACQ_size;
	private INDArray ndarr;

	public void ACQ_Parameter(String TAG) {
		try {
			ndarr = (NDArray) acqp.get(TAG);
			if (ndarr == null) {
				ndarr = (NDArray) method.get(TAG);
			}
			if (ndarr == null) {
				System.out.println("The key was not found or null:code=-1");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("parameter extraction got an error");
		}
	}

	public double[][][] getDouble3DMatrix(String TAG) {
		ACQ_Parameter(TAG);
		if (ndarr.rank() > 2) {
			return toDoubleArray(ndarr);
		} else {
			System.out.println("the input is not a 3d matrix");
			return null;
		}
	}

	public float[][][] getFloat3DMatrix(String TAG) {
		ACQ_Parameter(TAG);
		if (ndarr.rank() > 2) {
			return toFloatArray(ndarr);
		} else {
			System.out.println("the input is not a 3d matrix");
			return null;
		}
	}

	public double[] getDoubleVector(String TAG) {
		ACQ_Parameter(TAG);
		if (ndarr.isVector()) {
			double[] double_param = ndarr.data().asDouble();
			return double_param;
		} else {
			System.out.println("the input is not a Vector");
			return null;
		}
	}
	
	public double[][] getDoubleMatrix(String TAG) {
		ACQ_Parameter(TAG);
		
		if (ndarr.isMatrix()) {
			double[][] double_param = ndarr.toDoubleMatrix();
			return double_param;
		} else {
			System.out.println("the input is not a Matrix");
			return null;
		}

	}

	public float[] getFloatVector(String TAG) {
		ACQ_Parameter(TAG);
		if (ndarr.isVector()) {
			float[] float_param = ndarr.data().asFloat();
			return float_param;
		} else {
			System.out.println("the input is not a Vector");
			return null;
		}
	}

	public float[][] getFloatMatrix(String TAG) {
		ACQ_Parameter(TAG);
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
		boolean float_flag;
		boolean int_flag;
		boolean ndArray_flag = false;
			try {
				float_flag = acqp.get(TAG).getClass().getSimpleName().contentEquals("Float");
				int_flag = acqp.get(TAG).getClass().getSimpleName().contentEquals("Integer");
				ndArray_flag = acqp.get(TAG).getClass().getSimpleName().contentEquals("NDArray");
			} catch (Exception e2) {
				float_flag = method.get(TAG).getClass().getSimpleName().contentEquals("Float");
				int_flag = method.get(TAG).getClass().getSimpleName().contentEquals("Integer");
				ndArray_flag = method.get(TAG).getClass().getSimpleName().contentEquals("NDArray");
			} 
		
		if (float_flag) {
		try {
			param = (Float) acqp.get(TAG);
		} catch (Exception e) {
			System.out.println(e);
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
				int_param = (Integer) acqp.get(TAG);
			} catch (Exception e) {
				System.out.println(e);
			}
			if (int_param == null) {
				try {
					int_param = (Integer) method.get(TAG);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
			
			param = int_param.floatValue();	
			}
		else if(ndArray_flag) {
			NDArray nd_param = null;
			try {
				nd_param = (NDArray) acqp.get(TAG);
			} catch (Exception e) {
				System.out.println(e);
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

	public int getInt(String TAG) {
		Integer param = null;
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
		return param;
	}

	public double getDouble(String TAG) {
		Double param = null;
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
		return param;
	}

	public String getString(String TAG) {
		String param = null;
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
		return param;
	}

	public float[] getRealData() {
		int dim = 0;
		float[] m_absorptionChannelTDraw = getRealData(dim);
		return m_absorptionChannelTDraw;
	}

	public float[] getRealData(int dim) {
		try {
			long[] dims = getFid_dims();
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

	public float[] getImagData() {
		int dim = 0;
		float[] m_absorptionChannelTDraw = getImagData(dim);
		return m_absorptionChannelTDraw;
	}

	public float[] getImagData(int dim) {
		try {
			long[] dims = getFid_dims();
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

	public Object getAcqp(String TAG) {
		Object ret;
		try {
			ret = acqp.get(TAG);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ret = "The key was not found:code=-1";

		}
		return ret;
	}

	private List<Object> list_scan_result;
	private long[] fid_dims;
	private Object ACQ_grad_matrix;
	private Object FOV;
	private String Mpath;
	private String ACQS_TYPE;
	private int ACQ_dim;

	public long[] getFid_dims() {
		return imag.shape();
	}

	//
	public Object getACQ_grad_matrix() {
		return ACQ_grad_matrix;
	}

	public Object getFOV() {
		return FOV;
	}

	public double[] getDoubleVector(INDArray ndarr) {
		double[] double_param = ndarr.data().asDouble();
		return double_param;
	}

	public float[] getFloatVector(INDArray ndarr) {
		float[] float_param = ndarr.data().asFloat();
		return float_param;
	}

	public Scan(String getpath) {
		this.Mpath = getpath;
		initialize();
	}

	private Object[] initialize() {
		String path = this.Mpath;
		list_scan_dir(path);
		
		if (CheckFileExist("acqp") && CheckFileExist("method")) {
		acqp = Jcampdx.read_jcampdx_file(path + "acqp");
		method = Jcampdx.read_jcampdx_file(path + "method");
		ACQS_TYPE = ident_ACQS_TYPE();
		read_fid(path, ACQS_TYPE);
		FOV = acqp.get("ACQ_fov");
		ACQ_grad_matrix = acqp.get("ACQ_grad_matrix");
		return new Object[] { real, imag };
		} else {
			System.out.println("the acqp file was not found");
			return null;
		}
		
		
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

	public void read_fid(String path, String ACQS_TYPE) {
		ACQ_dim = (int) acqp.get("ACQ_dim");
		try {
			read_fid_file(path);
		} catch (IOException e) {
			System.out.println("There is a problem with reading the fid file");
		}
		reshape_fid(ACQS_TYPE);
		if (ACQ_dim == 2) {
			reorder_fid_lines_2d();
			reorder_fid_frames_2d();
		} else if (ACQ_dim == 3) {
			reorder_fid_3d();
		}
	}

	private void reorder_fid_3d() {
		// TODO Auto-generated method stub

	}

	public void reshape_fid(String ACQS_TYPE) {
		real = Nd4j.zeros(ArrBD.size() / 2);
		imag = Nd4j.zeros(ArrBD.size() / 2);
		for (int i = 0; i < ArrBD.size(); i = i + 2) {
			real.putScalar(i / 2, ArrBD.get(i));
			imag.putScalar(i / 2, ArrBD.get(i + 1));
		}
		get_reorder_schemes_fid(ACQS_TYPE);
		fid_dims = imag.shape();
		if (fid_dims[0] != product(reshape_scheme_1)) {
			reshape_scheme_1[0] = (int) (fid_dims[0]
					/ product(Arrays.copyOfRange(reshape_scheme_1, 1, reshape_scheme_1.length)));
			int[] trim = get_acq_trim(ACQS_TYPE);
			real = real.reshape('f', reshape_scheme_1);
			imag = imag.reshape('f', reshape_scheme_1);
			real = real.get(NDArrayIndex.interval(trim[0], trim[1]), NDArrayIndex.all(), NDArrayIndex.all(),
					NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all());
			imag = real.get(NDArrayIndex.interval(trim[0], trim[1]), NDArrayIndex.all(), NDArrayIndex.all(),
					NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all());
		} else {
			real = real.reshape('f', reshape_scheme_1);
			imag = imag.reshape('f', reshape_scheme_1);
		}
		real = real.permute(permute_scheme_1);
		imag = imag.permute(permute_scheme_1);

		real = real.reshape('f', reshape_scheme_2);
		imag = imag.reshape('f', reshape_scheme_2);
		// System.out.println(real);
	}

	int product(int ar[]) {
		int result = 1;
		for (int i = 0; i < ar.length; i++)
			result = result * ar[i];
		return result;
	}

	public void reorder_fid_lines_2d() {

		int PVM_EncNReceivers = (int) method.get("PVM_EncNReceivers");
		INDArray PVM_EncSteps1 = (INDArray) method.get("PVM_EncSteps1");
		int NR = (int) acqp.get("NR");
		int NI = (int) acqp.get("NI");
		// ArrayUtil.argsort(PVM_EncSteps1);
		int[] sorted = argsort(PVM_EncSteps1.data().asInt(), true);
		INDArray OBJ_imag, OBJ_real;
		for (int reciever = 0; reciever < PVM_EncNReceivers; reciever++) {
			for (int repition = 0; repition < NR; repition++) {
				for (int Object_cnt = 0; Object_cnt < NI; Object_cnt++) {
					INDArrayIndex[] indx = { NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(Object_cnt),
							NDArrayIndex.point(repition), NDArrayIndex.point(reciever) };
					OBJ_imag = imag.get(indx);
					imag.put(indx, OBJ_imag.getColumns(sorted));
					OBJ_real = real.get(indx);
					real.put(indx, OBJ_real.getColumns(sorted));
				}
			}
		}

	}

	public void reorder_fid_frames_2d() {

		INDArray PVM_ObjOrderList_IndArr = (INDArray) method.get("PVM_ObjOrderList");
		int[] PVM_ObjOrderList = PVM_ObjOrderList_IndArr.data().asInt();
		int[] PVM_ObjOrderList_sorted = ArrayUtil.argsort(PVM_ObjOrderList);

		int NSLICES = (int) acqp.get("NSLICES");
		int NI = (int) acqp.get("NI");
		fid_dims = imag.shape();
		int PVM_EncNReceivers = (int) method.get("PVM_EncNReceivers");
		if (NSLICES != NI) {
			int NR = (int) (NI / NSLICES);
			NI = NSLICES;
			reshape_scheme = new int[] { (int) fid_dims[0], (int) fid_dims[1], NR, NI, PVM_EncNReceivers };
			permute_scheme = new int[] { 0, 1, 3, 2, 4 };
			real = real.reshape('f', reshape_scheme);
			imag = imag.reshape('f', reshape_scheme);

			real = real.permute(permute_scheme);
			imag = imag.permute(permute_scheme);
			// System.out.println(real);
		}
		INDArray newOBJ_imag, newOBJ_real;
		for (int i = 0; i < PVM_ObjOrderList.length; i++) {
			INDArrayIndex[] indx = { NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(i), NDArrayIndex.all(),
					NDArrayIndex.all() };

			INDArrayIndex[] indx1 = { NDArrayIndex.all(), NDArrayIndex.all(),
					NDArrayIndex.point(PVM_ObjOrderList_sorted[i]), NDArrayIndex.all(), NDArrayIndex.all() };

			newOBJ_imag = imag.get(indx1);
			imag.put(indx, newOBJ_imag);
			newOBJ_real = real.get(indx1);
			real.put(indx, newOBJ_real);
		}
		// System.out.println(imag);
	}

	public void read_fid_file(String path) throws IOException {
		String inputFile = path + "fid";
		FileInputStream dataStream = new FileInputStream(inputFile);
		dataFilter = new DataInputStream(dataStream);
		buffReader = new DataInputStream(new BufferedInputStream(dataFilter));
		dtype(buffReader);
//        

	}

	private void dtype(DataInputStream buffReader) throws EOFException, IOException {
		String acqp_GO_raw_data_format = null;
		String acqp_BYTORDA = null;
		try {
			acqp_GO_raw_data_format = (String) acqp.get("GO_raw_data_format");
			acqp_BYTORDA = (String) acqp.get("BYTORDA");
		} catch (Exception e) {
			System.out.println("Parameters missing: acqp_BYTORDA, or acqp_BYTORDA");
		}
		if (acqp_GO_raw_data_format.contentEquals("GO_32BIT_SGN_INT") && acqp_BYTORDA.contentEquals("little")) {
			rev_int32_conversion();
		} else if (acqp_GO_raw_data_format.contentEquals("GO_16BIT_SGN_INT") && acqp_BYTORDA.contentEquals("little")) {
			rev_int16_conversion();
		} else if (acqp_GO_raw_data_format.contentEquals("GO_32BIT_FLOAT") && acqp_BYTORDA.contentEquals("little")) {
			rev_float_conversion();
		} else if (acqp_GO_raw_data_format.contentEquals("GO_32BIT_SGN_INT") && acqp_BYTORDA.contentEquals("big")) {
			int_conversion();
		} else if (acqp_GO_raw_data_format.contentEquals("GO_16BIT_SGN_INT") && acqp_BYTORDA.contentEquals("big")) {
			int16_conversion();
		} else if (acqp_GO_raw_data_format.contentEquals("GO_32BIT_FLOAT") && acqp_BYTORDA.contentEquals("big")) {
			float_conversion();
		} else {
			rev_int32_conversion();
			System.out.println("Data format not specified correctly, set to int32, little endian");
		}
	}

	private void rev_int32_conversion() throws EOFException, IOException {
		while (buffReader.available() > 0) {
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
		while (buffReader.available() > 0) {
			BD = buffReader.readInt();
			;
			ArrBD.add(BD);
		}
	}

	private void rev_int16_conversion() throws EOFException, IOException {
		while (buffReader.available() > 0) {
			int ch1 = buffReader.read();
			int ch2 = buffReader.read();
			if ((ch1 | ch2) < 0)
				throw new EOFException();
			BD = ((ch2 << 8) + (ch1 << 0));
			ArrBD.add(BD);
		}
	}

	private void int16_conversion() throws EOFException, IOException {
		while (buffReader.available() > 0) {
			int ch1 = buffReader.read();
			int ch2 = buffReader.read();
			if ((ch1 | ch2) < 0)
				throw new EOFException();
			BD = ((ch1 << 8) + (ch2 << 0));
			ArrBD.add(BD);
		}
	}

	private void rev_float_conversion() throws IOException {
		while (buffReader.available() > 0) {
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
		while (buffReader.available() > 0) {
			BD_f = buffReader.readFloat();
			ArrBD.add(BD);
		}
	}

	public void read_traj() {
//		traj = read_traj_file();
//		reshape_scheme = get_reorder_schemes_traj();
//		traj = reshape_traj(traj, reshape_scheme);
//		return traj;
	}

	public void read_traj_file() {
		// same as read_fid_file but read traj and datatype is float32
	}

	public void reshape_traj() {
		String PVM_TrajIntAll = (String) method.get("PVM_TrajIntAll");
		String PVM_TrajSamples = (String) method.get("PVM_TrajSamples");
		String PVM_TrajDims = (String) method.get("PVM_TrajDims");
		// return np.reshape(traj, (PVM_TrajDims, PVM_TrajSamples, PVM_TrajIntAll),
		// order='F').copy()
		// so what is the function of reshape_scheme
	}

	public void simple_reco() {
//		reco = np.empty(self.fid.shape, dtype=self.fid.dtype)
//
//		        for channel in range(0,self.fid.shape[4]):
//		            for repetition in range(0, self.fid.shape[3]):
//		                for slice in range(0, self.fid.shape[2]):
//		                    reco[:,:,slice,repetition,channel] = np.fft.fftshift(np.fft.fft2(self.fid[:,:,slice,repetition,channel]))
//
//		        return reco
	}

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
		if(CSISignalType == null) {
			ArrayList<String> ACQ_dim_desc = (ArrayList<String>) acqp.get("ACQ_dim_desc");
			if(ACQ_dim_desc.equals(Arrays.asList((new String[] {"Spectroscopic", "Spatial", "Spatial"})))){
			return ACQS_TYPE = "CSI";
			}
			if(ACQ_dim_desc.equals(Arrays.asList((new String[] {"Spectroscopic", "Spectroscopic"})))){
				return ACQS_TYPE = "...";
				}
			
		}		
		String ACQS_TYPE;
		if (NPro != null && ACQ_dim == 2) {
			ACQS_TYPE = "RADIAL_2D";
		} else if (NPro != null && ACQ_dim == 3) {
			ACQS_TYPE = "RADIAL_3D";
		} else if (CSISignalType != null) {
			ACQS_TYPE = "CSI";
		} else if (ACQ_dim == 3) {
			ACQS_TYPE = "CART_3D";
		} else {
			ACQS_TYPE = "CART_2D";
		}
		return ACQS_TYPE;
	}

	public void get_reorder_schemes_fid(String ACQS_TYPE) {
		int NR = (int) acqp.get("NR");
		int NI = (int) acqp.get("NI");
		int NPro;
		try {
			NPro = (int) acqp.get("NPro");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			NPro = 0;
		}
		int ACQ_phase_factor = (int) acqp.get("ACQ_phase_factor");
		ACQ_size = (INDArray) acqp.get("ACQ_size");
		ACQ_size.putScalar(0, ACQ_size.getInt(0) / 2);
		INDArray PVM_EncMatrix = (INDArray) method.get("PVM_EncMatrix");
		int PVM_EncNReceivers;
		int PVM_EncTotalAccel;
		try {
			PVM_EncNReceivers = (int) method.get("PVM_EncNReceivers");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			PVM_EncNReceivers = 0;

		}
		try {

			PVM_EncTotalAccel = (int) method.get("PVM_EncTotalAccel");
		} catch (Exception e) {
			// TODO Auto-generated catch block

			PVM_EncTotalAccel = 0;
		}
		if (ACQS_TYPE == "CART_2D") {
			reshape_scheme_1 = new int[] { PVM_EncMatrix.getInt(0), PVM_EncNReceivers, ACQ_phase_factor, NI,
					PVM_EncMatrix.getInt(1) / ACQ_phase_factor, NR };
			reshape_scheme_2 = new int[] { PVM_EncMatrix.getInt(0), PVM_EncMatrix.getInt(1), NI, NR,
					PVM_EncNReceivers };
			permute_scheme_1 = new int[] { 0, 2, 4, 3, 5, 1 };
		} else if (ACQS_TYPE == "CART_3D") {
			reshape_scheme_1 = new int[] { PVM_EncMatrix.getInt(0) / PVM_EncTotalAccel, PVM_EncNReceivers,
					ACQ_phase_factor, PVM_EncMatrix.getInt(1) / ACQ_phase_factor, PVM_EncMatrix.getInt(2), NR };
			reshape_scheme_2 = new int[] { PVM_EncMatrix.getInt(0), PVM_EncMatrix.getInt(1), PVM_EncMatrix.getInt(2),
					NR, PVM_EncNReceivers };
			permute_scheme_1 = new int[] { 0, 2, 3, 4, 5, 1 };
		} else if (ACQS_TYPE == "RADIAL_2D") {
			reshape_scheme_1 = new int[] { ACQ_size.getInt(0), PVM_EncNReceivers, ACQ_phase_factor, NI,
					NPro / ACQ_phase_factor, NR };
			reshape_scheme_2 = new int[] { ACQ_size.getInt(0), NPro, NI, NR, PVM_EncNReceivers };
			permute_scheme_1 = new int[] { 0, 2, 4, 3, 5, 1 };
		} else if (ACQS_TYPE == "RADIAL_3D") {
			reshape_scheme_1 = new int[] { ACQ_size.getInt(0), PVM_EncNReceivers, ACQ_phase_factor, NI,
					NPro / ACQ_phase_factor, NR };
			reshape_scheme_2 = new int[] { ACQ_size.getInt(0), NPro, NI, NR, PVM_EncNReceivers };
			permute_scheme_1 = new int[] { 0, 2, 4, 3, 5, 1 };
		} else if (ACQS_TYPE == "CSI") {
			reshape_scheme_1 = new int[] { ACQ_size.getInt(0), ACQ_size.getInt(1), ACQ_size.getInt(2) };
			reshape_scheme_2 = reshape_scheme_1;
			permute_scheme_1 = new int[] { 0, 1, 2 };
		}
	}

	// this function is not neccerray
	public void get_reorder_schemes_traj() {

	}

	public int[] get_acq_trim(String ACQS_TYPE) {
		if (ACQS_TYPE == "RADIAL_2D" || ACQS_TYPE == "RADIAL_3D") {
			return new int[] { 0, (int) ACQ_size.getInt(0) / 2 };
			// again divided to 2?
		} else {
			return new int[] { 0, (int) ACQ_size.getInt(0) };
		}
	}

//	private int[] convertor_String2Int(String input, int size){
//        String output = input.replaceAll("[()]", "");
//        String[] output1 = output.split(" ");
//        int[] outputint = new int[size];
//        for(int i=0;i<size;i++) {
//        outputint[i] = Integer.parseInt(output1[i+2]);
//        }
//        return outputint;
//    }		
	public int[] argsort(final int[] a, final boolean ascending) {
		Integer[] indexes = new Integer[a.length];
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = i;
		}
		Arrays.sort(indexes, new Comparator<Integer>() {
			public int compare(final Integer i1, final Integer i2) {
				return (ascending ? 1 : -1) * Ints.compare(a[i1], a[i2]);
			}
		});

		int[] ret = new int[indexes.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = indexes[i];

		return ret;
	}

	public double[][][] toDoubleArray(INDArray indarr) {
		fid_dims = indarr.shape();
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
		fid_dims = indarr.shape();
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

		fid_dims = indarr.shape();
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

	public double[][][][][] get_abs_fid() {
		fid_dims = getFid_dims();
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
//	public void main(String[] args) throws IOException {
//		String path3 = "D:/T drive/data/bruker/CSI/Mouse/7/";
//		long startTime = System.nanoTime();
//		Object[] newimag = Scan(path3);
//		long endTime = System.nanoTime();
//		System.out.println((endTime - startTime)/1000000);
	// double[][][][][] arr = toMatrix(imag);
	// System.out.println(imag.getDouble(159,160,9,0,3));
	// System.out.println(String.valueOf(arr[159][160][9][0][3]));
//		FileWriter csvWriter = new FileWriter("new.cv");
//		long[] fid_dims = imag.shape();
//		double[][][][][] fid = new double[(int) fid_dims[1]][(int) fid_dims[0]][(int) fid_dims[4]][(int) fid_dims[3]][(int) fid_dims[2]];
//		for(int n=0; n< fid_dims[2];n++) {
//			for(int m=0; m< fid_dims[3];m++) {
//				for(int l=0; l< fid_dims[4];l++) {
//					for(int i=0; i< fid_dims[1];i++) {
//						for(int j=0; j< fid_dims[0];j++) {
//								fid[i][j][l][m][n] = Math.sqrt(Math.sqrt(Math.pow(imag.getInt(j,i,l,m,n),2)+Math.pow(real.getInt(j,i,l,m,n), 2)));
//								csvWriter.write(String.valueOf(fid[i][j][l][m][n]));
//								csvWriter.write(",");
//			
//			}
//		
//		}
//        
//	}
//			}
//		}
//	}
}

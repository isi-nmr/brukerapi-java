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

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.util.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Ints;

public class Bruker {
	private Logger logger = LoggerFactory.getLogger(Bruker.class);
	private String[] SUPPORTED_DATA_FILE_TYPES = new String[] {"fid","2dseq","1i","1r"};
	Jcampdx jcampdx;
	private Path path;
	private ArrayList<INDArray> data;
	private String ACQS_TYPE;
	private int[] reshape_scheme_1;
	private int[] reshape_scheme_2;
	private int[] permute_scheme_1;
	private int[] reshape_scheme;
	private int[] permute_scheme;
	
	
	
	public Jcampdx getJcampdx() {
		return jcampdx;
	}


	public void setJcampdx(Jcampdx jcampdx) {
		this.jcampdx = jcampdx;
	}
	
	public int[] getPermute_scheme_1() {
		return permute_scheme_1;
	}


	public void setPermute_scheme_1(int[] permute_scheme_1) {
		this.permute_scheme_1 = permute_scheme_1;
	}


	public int[] getReshape_scheme_1() {
		return reshape_scheme_1;
	}


	public void setReshape_scheme_1(int[] reshape_scheme_1) {
		this.reshape_scheme_1 = reshape_scheme_1;
	}


	public int[] getReshape_scheme_2() {
		return reshape_scheme_2;
	}


	public void setReshape_scheme_2(int[] reshape_scheme_2) {
		this.reshape_scheme_2 = reshape_scheme_2;
	}

	
	
	public Bruker() {
		
	}


	public Path getPath() {
		return path;
	}


	public void setPath(Path path) {
		this.path = path;
		logger.info("({}) is set tht path",path );
		String filename = path.getFileName().toString(); 
		if(Arrays.stream(SUPPORTED_DATA_FILE_TYPES).anyMatch(filename::equals)) {
			logger.info("File {} is a supported Bruker data type",filename );
		} else {
			logger.error("File {} is not a supported Bruker data type",filename );
		}
		jcampdx = new Jcampdx(path);
	}
	
	public void ident_ACQS_TYPE() {
		Integer ACQ_dim =  jcampdx.getAcqp().getInt("ACQ_dim");
		Integer NPro;
		Integer NECHOES;
		String CSISignalType;
		try {
			NPro = jcampdx.getMethod().getInt("NPro");
		} catch (Exception e) {
			NPro = null;
		}
		try {
			NECHOES = jcampdx.getAcqp().getInt("NECHOES");
		} catch (Exception e) {
			NECHOES = null;
		}
		try {
			CSISignalType = (String) jcampdx.getMethod().getString("CSISignalType");
		} catch (Exception e) {
			CSISignalType = null;
		}
		if(CSISignalType == null) {
			ArrayList<String> ACQ_dim_desc = jcampdx.getAcqp().getArrayList("ACQ_dim_desc");
			if(ACQ_dim_desc.equals(Arrays.asList((new String[] {"Spectroscopic", "Spatial", "Spatial"})))){
			ACQS_TYPE = "CSI";
			setACQS_TYPE(ACQS_TYPE);
			}
			if(ACQ_dim_desc.equals(Arrays.asList((new String[] {"Spectroscopic", "Spectroscopic"})))){
				ACQS_TYPE = "UNKNOWN";
				setACQS_TYPE(ACQS_TYPE);
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
		setACQS_TYPE(ACQS_TYPE);
	}
	
	public  Boolean isImage()
    {
     return !getACQS_TYPE().contentEquals("CSI")  ;  
    }   
	
	public boolean isDataValid()
	{
	    return data != null;
	}   
	
	public DataBruker getData() {
		List<Object> dir_rslt = scan_dir(path.getParent());
		
		if (dir_rslt.contains("acqp") && dir_rslt.contains("method") && path.getFileName().toString().contains("fid")) {
			JcampdxData acqp = jcampdx.getAcqp();
			JcampdxData method = jcampdx.getMethod();
			data = read_fid(acqp,method);
		}
		if (dir_rslt.contains("visu_pars") && dir_rslt.contains("reco") && path.getFileName().toString().contains("2dseq")) {
			JcampdxData visu_pars = jcampdx.getVisu_pars();
			JcampdxData reco = jcampdx.getReco();
//			Object FG_TYPES = visu_pars.get("VisuFGOrderDesc");
			data = read_2dseq(visu_pars, reco);
		}
		return new DataBruker(data);
	}

	
	public void setData(ArrayList<INDArray> data) {
		this.data = data;
	}
	
	private List<Object> scan_dir(Path path) {
		List<Object> list_scan_result = null;
		try (Stream<Path> walk = Files.walk(path)) {
			list_scan_result= walk.filter(Files::isRegularFile).map(x -> x.getFileName().toString()).collect(Collectors.toList());
		} catch (IOException e) {
			logger.error("Bruker is not able to scan directory {}", path);
		}
		return list_scan_result;
	}


	public String getACQS_TYPE() {
		return ACQS_TYPE;
	}


	public void setACQS_TYPE(String aCQS_TYPE) {
		ACQS_TYPE = aCQS_TYPE;
	}
	
	private ArrayList<INDArray> read_2dseq(JcampdxData visu_pars, JcampdxData reco) {
		ArrayList<Object> ArrBD = new ArrayList<>();
		ArrayList<INDArray> data_array = new ArrayList<INDArray>();
		try {
			ArrBD = read_2dseq_file(visu_pars,reco);
		} catch (IOException e) {
			logger.error("There is a problem with reading the 2dseq file");
		}
		data_array = reshape_2dseq(ArrBD, visu_pars, reco);
		data_array = scale(data_array, visu_pars, reco);
		data_array = form_frame_groups(data_array, visu_pars, reco);
//		form_complex();
		return data_array;
	}
	
	private ArrayList<Object> read_2dseq_file(JcampdxData visu_pars, JcampdxData reco) throws EOFException, IOException {
		FileInputStream dataStream = new FileInputStream(path.toString());
		DataInputStream dataFilter = new DataInputStream(dataStream);
		DataInputStream buffReader = new DataInputStream(new BufferedInputStream(dataFilter));
		String VisuCoreWordType = null;
		String VisuCoreByteOrder = null;
		try {
			VisuCoreWordType = visu_pars.getString("VisuCoreWordType");
			VisuCoreByteOrder = visu_pars.getString("VisuCoreByteOrder");
		} catch (Exception e) {
			System.out.println("Parameters missing: VisuCoreByteOrder, or VisuCoreByteOrder");
		}
		ArrayList<Object> ArrBD = dtype(buffReader, VisuCoreWordType, VisuCoreByteOrder);
		return ArrBD;
	}
	
	private ArrayList<INDArray> reshape_2dseq(ArrayList<Object> ArrBD, JcampdxData visu_pars, JcampdxData reco) {
		// TODO Auto-generated method stub
		INDArray VisuCoreSize =  visu_pars.getINDArray("VisuCoreSize");
		int VisuCoreFrameCount = visu_pars.getInt("VisuCoreFrameCount");
		INDArray real = Nd4j.zeros(ArrBD.size());
		INDArray imag = Nd4j.zeros(ArrBD.size());
        for(int i = 0;i<ArrBD.size();i++) {
       	 real.putScalar(i,  (Integer) ArrBD.get(i));
        }
        int[] reshape_scheme = new int[(int) (VisuCoreSize.length() + 1)];
        for (int i=0; i < VisuCoreSize.length(); i++) {
        	reshape_scheme[i] = VisuCoreSize.getInt(i);
        }
        reshape_scheme[(int) (VisuCoreSize.length())] = VisuCoreFrameCount;
        real = real.reshape('f',reshape_scheme);
        ArrayList<INDArray> data_array = new ArrayList<INDArray>();
		data_array.add(real);
		data_array.add(imag);
		return data_array;
	}
	
	private ArrayList<INDArray> scale(ArrayList<INDArray> data_array, JcampdxData visu_pars, JcampdxData reco) {
		// TODO Auto-generated method stub
		INDArray VisuCoreDataSlope =  visu_pars.getINDArray("VisuCoreDataSlope");
//		Object VisuCoreDataOffs = visu_pars.get("VisuCoreDataOffs)");
		int VisuCoreFrameCount = visu_pars.getInt("VisuCoreFrameCount");
		int VisuCoreDim = visu_pars.getInt("VisuCoreDim");
		long[] fid_dims = data_array.get(0).shape();
		for(int i=0; i<VisuCoreFrameCount; i++) {
			if(VisuCoreDim == 1) {
				data_array.get(0).getColumn(i).add(VisuCoreDataSlope.getFloat(i));
			}
			else if(VisuCoreDim == 2) {
				for(int j=0; j < fid_dims[2]; j++) {
				INDArrayIndex[] indx = {NDArrayIndex.all(),NDArrayIndex.all(),NDArrayIndex.point(j)};
				data_array.get(0).get(indx).add(VisuCoreDataSlope.getFloat(i));
				}
			}	
			else if(VisuCoreDim == 3) {
				for(int j=0; j<fid_dims[3]; j++) {
					INDArrayIndex[] indx = {NDArrayIndex.all(),NDArrayIndex.all(),NDArrayIndex.all(),NDArrayIndex.point(j)};
					data_array.get(0).get(indx).add(VisuCoreDataSlope.getFloat(i));
					}
			}
		}
		return data_array;
	}
	
	private ArrayList<INDArray> form_frame_groups(ArrayList<INDArray> data_array, JcampdxData visu_pars, JcampdxData reco) {
		// TODO Auto-generated method stub
		ArrayList VisuFGOrderDesc = visu_pars.getArrayList("VisuFGOrderDesc");
		int VisuFGOrderDescDim = 0;
		try {
			VisuFGOrderDescDim = visu_pars.getInt("VisuFGOrderDescDim");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("VisuFGOrderDescDim not found");
		}
		INDArray VisuCoreSize = visu_pars.getINDArray("VisuCoreSize");
		int[] fg_dims = new int[VisuFGOrderDescDim];
		
		
		for(int i=0; i< VisuFGOrderDescDim; i++) {
			Object[] obj = (Object[]) VisuFGOrderDesc.get(i);
			String str_obj = obj[0].toString();
			String cmplx_flag = obj[1].toString();
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
        data_array.get(0).reshape('f',reshape_scheme);
        return data_array;
	}
	public ArrayList<INDArray> read_fid(JcampdxData acqp, JcampdxData method) {
		int ACQ_dim = acqp.getInt("ACQ_dim");
		ArrayList<Object> ArrBD = new ArrayList<>();
		ArrayList<INDArray> data_array = new ArrayList<INDArray>();
		try {
			ArrBD = read_fid_file(acqp,method);
		} catch (IOException e) {
			logger.error("There is a problem with reading the fid file");
		}

		data_array = reshape_fid(ArrBD,acqp,method);
		
		if (ACQ_dim == 2) {
			data_array = reorder_fid_lines_2d(data_array, acqp, method);
			data_array = reorder_fid_frames_2d(data_array, acqp, method);
		} else if (ACQ_dim == 3) {
			reorder_fid_3d();
		}
		return data_array;
	}
	
	private void reorder_fid_3d() {
		// TODO Auto-generated method stub

	}
	
//	private void paraVision360(JcampdxData acqp, JcampdxData method) {
//		 Object numberOfPoint = method.get("PVM_SpecMatrix");
//		 Object PVM_Matrix = method.get("PVM_Matrix");
//	}
	
	
	
	public ArrayList<INDArray> reorder_fid_frames_2d(ArrayList<INDArray> data_array, JcampdxData acqp, JcampdxData method) {

		INDArray PVM_ObjOrderList_IndArr = method.getINDArray("PVM_ObjOrderList");
		int[] PVM_ObjOrderList = PVM_ObjOrderList_IndArr.data().asInt();
		int[] PVM_ObjOrderList_sorted = ArrayUtil.argsort(PVM_ObjOrderList);
		INDArray real = data_array.get(0);
		INDArray imag = data_array.get(1);
		int NSLICES = acqp.getInt("NSLICES");
		int NI =  acqp.getInt("NI");
		long[] fid_dims = imag.shape();
		int PVM_EncNReceivers = method.getInt("PVM_EncNReceivers");
		if (NSLICES != NI) {
			int NR = (int) (NI / NSLICES);
			NI = NSLICES;
			reshape_scheme = new int[] { (int) fid_dims[0], (int) fid_dims[1], NR, NI, PVM_EncNReceivers };
			permute_scheme = new int[] { 0, 1, 3, 2, 4 };
			real = real.reshape('f', reshape_scheme);
			imag = imag.reshape('f', reshape_scheme);

			real = real.permute(permute_scheme);
			imag = imag.permute(permute_scheme);
			
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
		
		data_array.add(0, real);
		data_array.add(1, imag);
		return data_array;
		
	}
	
	public ArrayList<INDArray> reorder_fid_lines_2d(ArrayList<INDArray> data_array, JcampdxData acqp, JcampdxData method) {

		int PVM_EncNReceivers = method.getInt("PVM_EncNReceivers");
		INDArray PVM_EncSteps1 = method.getINDArray("PVM_EncSteps1");
		INDArray real = data_array.get(0);
		INDArray imag = data_array.get(1);
		int NR = acqp.getInt("NR");
		int NI = acqp.getInt("NI");
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
		data_array.add(0, real);
		data_array.add(1, imag);
		return data_array;
		

	}
	
	public ArrayList<INDArray> reshape_fid(ArrayList<Object> ArrBD, JcampdxData acqp, JcampdxData method) {
		INDArray real = Nd4j.zeros(ArrBD.size() / 2);
		INDArray imag = Nd4j.zeros(ArrBD.size() / 2);
		for (int i = 0; i < ArrBD.size(); i = i + 2) {
			real.putScalar(i / 2,   Double.valueOf(ArrBD.get(i).toString()));
			imag.putScalar(i / 2,   Double.valueOf(ArrBD.get(i+1).toString()));
		}
		get_reorder_schemes_fid(acqp,method);
		long[] fid_dims = imag.shape();
		if (fid_dims[0] != product(reshape_scheme_1)) {
			reshape_scheme_1[0] = (int) (fid_dims[0]
					/ product(Arrays.copyOfRange(reshape_scheme_1, 1, reshape_scheme_1.length)));
			int[] trim = get_acq_trim(acqp);
			real = real.reshape('f', reshape_scheme_1);
			imag = imag.reshape('f', reshape_scheme_1);
			// to do : out of index
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
		
		ArrayList<INDArray> data_array = new ArrayList<INDArray>();
		data_array.add(real);
		data_array.add(imag);
		return data_array;
	}
	
	public int[] get_acq_trim(JcampdxData acqp) {
		String ACQS_TYPE = getACQS_TYPE();
		INDArray ACQ_size = acqp.getINDArray("ACQ_size");
		if (ACQS_TYPE == "RADIAL_2D" || ACQS_TYPE == "RADIAL_3D") {
			return new int[] { 0, (int) ACQ_size.getInt(0) / 2 };
			// again divided to 2?
		} else {
			return new int[] { 0, (int) ACQ_size.getInt(0) };
		}
	}
	
	public void get_reorder_schemes_fid(JcampdxData acqp, JcampdxData method) {
		String ACQS_TYPE = getACQS_TYPE();
		int NR = acqp.getInt("NR");
		int NI = acqp.getInt("NI");
		int NPro;
		try {
			NPro = acqp.getInt("NPro");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			NPro = 0;
		}
		int ACQ_phase_factor = acqp.getInt("ACQ_phase_factor");
		INDArray ACQ_size = acqp.getINDArray("ACQ_size");
		ACQ_size.putScalar(0, ACQ_size.getInt(0) / 2);
		INDArray PVM_EncMatrix =  method.getINDArray("PVM_EncMatrix");
		int PVM_EncNReceivers;
		int PVM_EncTotalAccel;
		try {
			PVM_EncNReceivers = method.getInt("PVM_EncNReceivers");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			PVM_EncNReceivers = 0;

		}
		try {

			PVM_EncTotalAccel = method.getInt("PVM_EncTotalAccel");
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
		} else if (ACQS_TYPE.contentEquals("CSI")) {
			if(acqp.getString("ACQ_sw_version").contains("PV-360")) {
				INDArray numberOfPoint =  method.getINDArray("PVM_SpecMatrix");
				INDArray PVM_Matrix = method.getINDArray("PVM_Matrix");
				reshape_scheme_1 = reshape_scheme_1 = new int[] { (int) numberOfPoint.getInt(0), PVM_Matrix.getInt(0), PVM_Matrix.getInt(1) };
				reshape_scheme_2 = reshape_scheme_1;
				permute_scheme_1 = new int[] { 0, 1, 2 };
			} else {
			reshape_scheme_1 = new int[] { ACQ_size.getInt(0), ACQ_size.getInt(1), ACQ_size.getInt(2) };
			reshape_scheme_2 = reshape_scheme_1;
			permute_scheme_1 = new int[] { 0, 1, 2 };
			}
		}
	}
	
	public ArrayList<Object> read_fid_file(JcampdxData acqp, JcampdxData method) throws IOException {
		FileInputStream dataStream = new FileInputStream(path.toString());
		DataInputStream dataFilter = new DataInputStream(dataStream);
		DataInputStream buffReader = new DataInputStream(new BufferedInputStream(dataFilter));
		String acqp_GO_raw_data_format = null;
		String acqp_BYTORDA = null;
		// check it is working without to string
		if (acqp.getString("ACQ_sw_version").contains("PV-360")) {
			try {
				String dataType =  acqp.getString("DTYPA");
				if (dataType.contentEquals("Double")) {
					acqp_GO_raw_data_format = "GO_32BIT_DOUBLE";
				}
				acqp_BYTORDA = acqp.getString("BYTORDA");
			} catch (Exception e) {
				logger.error("Parameters missing: acqp_BYTORDA, or acqp_BYTORDA");
			}
		} else {
		try {
			acqp_GO_raw_data_format = acqp.getString("GO_raw_data_format");
			acqp_BYTORDA = acqp.getString("BYTORDA");
		} catch (Exception e) {
			logger.error("Parameters missing: acqp_BYTORDA, or acqp_BYTORDA");
		}
		}
		ArrayList<Object> ArrBD = dtype(buffReader, acqp_GO_raw_data_format, acqp_BYTORDA);
		return ArrBD;
	}
	
	private ArrayList<Object> dtype(DataInputStream buffReader, String arg1, String arg2) throws EOFException, IOException {
		ArrayList<Object> ArrBD = new ArrayList<>();
		if ((arg1.contentEquals("GO_32BIT_SGN_INT") || arg1.contentEquals("_32BIT_SGN_INT")) && (arg2.contentEquals("little") || arg2.contentEquals("littleEndian"))) {
			ArrBD = rev_int32_conversion(buffReader);
		} else if ((arg1.contentEquals("GO_16BIT_SGN_INT") || arg1.contentEquals("_16BIT_SGN_INT")) && (arg2.contentEquals("little") || arg2.contentEquals("littleEndian")) ) {
			ArrBD = rev_int16_conversion(buffReader);
		} else if ((arg1.contentEquals("GO_32BIT_FLOAT") || arg1.contentEquals("_32BIT_FLOAT")) && (arg2.contentEquals("little") || arg2.contentEquals("littleEndian"))) {
			ArrBD = rev_float_conversion(buffReader);
		} else if ((arg1.contentEquals("GO_32BIT_SGN_INT") || arg1.contentEquals("_32BIT_SGN_INT")) && (arg2.contentEquals("big") || arg2.contentEquals("bigEndian"))) {
			ArrBD = int_conversion(buffReader);
		} else if ((arg1.contentEquals("GO_16BIT_SGN_INT") || arg1.contentEquals("_16BIT_SGN_INT")) && (arg2.contentEquals("big") || arg2.contentEquals("bigEndian"))) {
			ArrBD = int16_conversion(buffReader);
		} else if ((arg1.contentEquals("GO_32BIT_FLOAT") || arg1.contentEquals("_32BIT_FLOAT")) && (arg2.contentEquals("big") || arg2.contentEquals("bigEndian"))) {
			ArrBD = float_conversion(buffReader);
		} else if ((arg1.contentEquals("GO_32BIT_DOUBLE")|| arg1.contentEquals("_32BIT_DOUBLE")) && (arg2.contentEquals("little") || arg2.contentEquals("littleEndian"))) {
			ArrBD = double_conversion(buffReader);
		} else {
			ArrBD = rev_int32_conversion(buffReader);
			logger.error("Data format not specified correctly, set to int32, little endian");
		}
		return ArrBD;
	}
	
	
	private ArrayList<Object> rev_int32_conversion(DataInputStream buffReader) throws EOFException, IOException {
		ArrayList<Object> ArrBD = new ArrayList<>();
		while (buffReader.available() > 0) {
			int ch1 = buffReader.read();
			int ch2 = buffReader.read();
			int ch3 = buffReader.read();
			int ch4 = buffReader.read();
			if ((ch1 | ch2 | ch3 | ch4) < 0)
				throw new EOFException();
			int BD = ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
			ArrBD.add(BD);
		}
		return ArrBD;
	}

	private ArrayList<Object> int_conversion(DataInputStream buffReader) throws IOException {
		ArrayList<Object> ArrBD = new ArrayList<>();
		while (buffReader.available() > 0) {
			Integer BD = buffReader.readInt();
			ArrBD.add(BD);
		}
		return ArrBD;
	}

	private ArrayList<Object> rev_int16_conversion(DataInputStream buffReader) throws EOFException, IOException {
		ArrayList<Object> ArrBD = new ArrayList<>();
		while (buffReader.available() > 0) {
			int ch1 = buffReader.read();
			int ch2 = buffReader.read();
			if ((ch1 | ch2) < 0)
				throw new EOFException();
			Integer BD = ((ch2 << 8) + (ch1 << 0));
			ArrBD.add(BD);
		}
		return ArrBD;
	}

	private ArrayList<Object> int16_conversion(DataInputStream buffReader) throws EOFException, IOException {
		ArrayList<Object> ArrBD = new ArrayList<>();
		while (buffReader.available() > 0) {
			int ch1 = buffReader.read();
			int ch2 = buffReader.read();
			if ((ch1 | ch2) < 0)
				throw new EOFException();
			int BD = ((ch1 << 8) + (ch2 << 0));
			ArrBD.add(BD);
		}
		return ArrBD;
	}

	private ArrayList<Object> rev_float_conversion(DataInputStream buffReader) throws IOException {
		ArrayList<Object> ArrBD = new ArrayList<>();
		while (buffReader.available() > 0) {
			int ch1 = buffReader.read();
			int ch2 = buffReader.read();
			int ch3 = buffReader.read();
			int ch4 = buffReader.read();
			if ((ch1 | ch2 | ch3 | ch4) < 0)
				throw new EOFException();
			Float BD = Float.intBitsToFloat(((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0)));
			ArrBD.add(BD);
		}
		return ArrBD;
	}

	private ArrayList<Object> float_conversion(DataInputStream buffReader) throws IOException {
		ArrayList<Object> ArrBD = new ArrayList<>();
		while (buffReader.available() > 0) {
			Float BD = buffReader.readFloat();
			ArrBD.add(BD);
		}
		return ArrBD;
	}
	
	private ArrayList<Object> rev_double_conversion(DataInputStream buffReader) throws IOException {
		ArrayList<Object> ArrBD = new ArrayList<>();
		while (buffReader.available() > 0) {
			int ch1 = buffReader.read();
			int ch2 = buffReader.read();
			int ch3 = buffReader.read();
			int ch4 = buffReader.read();
			int ch5 = buffReader.read();
			int ch6 = buffReader.read();
			int ch7 = buffReader.read();
			int ch8 = buffReader.read();
			if ((ch1 | ch2 | ch3 | ch4 | ch5 | ch6 | ch7 | ch8) < 0)
				throw new EOFException();
			Double BD = Double.longBitsToDouble(((ch8 << 56) + (ch7 << 48) + (ch6 << 40) + (ch5 << 32) + (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0)));
			ArrBD.add(BD);
		}
		return ArrBD;
	}
	
	private ArrayList<Object> double_conversion(DataInputStream buffReader) throws IOException {
		ArrayList<Object> ArrBD = new ArrayList<>();
		while (buffReader.available() > 0) {
			Double BD = buffReader.readDouble();
			ArrBD.add(BD);
		}
		return ArrBD;
	}
	
	int product(int ar[]) {
		int result = 1;
		for (int i = 0; i < ar.length; i++)
			result = result * ar[i];
		return result;
	}
	
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
	
//	public Bruker(String path) {
//		this.pathL = Paths.get(path);
//		String filename = pathL.getFileName().toString(); 
//		if(Arrays.stream(SUPPORTED_DATA_FILE_TYPES).anyMatch(filename::equals)) {
//			logger.info("File {} is a supported Bruker data type",filename );
//		} else {
//			logger.error("File {} is not a supported Bruker data type",filename );
//		}	
//		
//		Jcampdx jcampdx = new Jcampdx(pathL);
//		jcampdx.getAcqp();
//		System.out.println("finish");
//	}
}
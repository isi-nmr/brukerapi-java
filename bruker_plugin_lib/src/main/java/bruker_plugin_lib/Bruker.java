
import com.google.common.primitives.Ints;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.util.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Bruker {
	private Logger logger = LoggerFactory.getLogger(Bruker.class);
	private String[] SUPPORTED_DATA_FILE_TYPES = new String[] { "fid", "2dseq", "1i", "1r", "ser" };
	private Parameters parameters;
	private Jcampdx jcampdx;
	private Conditions conditions;
	public ArrayList<INDArray> data;
	private int[] reshape_scheme_1;
	private int[] reshape_scheme_2;
	private int[] permute_scheme_1;
	private int[] reshape_scheme;
	private int[] permute_scheme;
	
	
	public Parameters getParameters() {
		return parameters;
	}
	
	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}

	public Conditions getConditions() {
		return conditions;
	}

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
		this.conditions = new Conditions(this);
	}


	/**
	 * set study path
	 *
	 * @param path
	 */
	public void setPath(Path path) {
		conditions.setPath(path);
		logger.info("({}) is set tht path", path);		
		if (Arrays.stream(SUPPORTED_DATA_FILE_TYPES).anyMatch(conditions.getFilename()::equals)) {
			logger.info("File {} is a supported Bruker data type", conditions.getFilename());
		} else {
			logger.error("File {} is not a supported Bruker data type", conditions.getFilename());
		}
	}


	/**
	 * determine the datset contains image data or not
	 *
	 * @return
	 */
	public Boolean isImage() {
		return !conditions.getACQS_TYPE().equals(ACQ_TYPE.CSI);
	}

	/**
	 * determine the data is reconstructed correctly or not
	 *
	 * @return
	 */
	public boolean isDataValid() {
		return data != null;
	}

	/**
	 * the main method of api which gets data of the dataset
	 *
	 * @return a DataBruker Object
	 */
	public DataBruker getData() {
	if(data == null) {
		if (isRaw()) {
			data = read_fid();
		}
		if (!isRaw()) {
			if (isIR()) {
				data = read_ir();
			} else {
				data = read_2dseq();
			}
		}
	}
		return new DataBruker(this);
	}

	public long[] getCplxDims() {
		long[] cplxshape = null;
		if (data != null) {
			if (!isRaw()) {
				cplxshape = data.get(0).shape();
			} else if (isRaw()) {
					logger.error("the data is not complex");
			}
		}
		return cplxshape;
	}

	public long[] getDims() {
		long[] realshape = null;
		long[] imagShape = null;
		if (data != null) {
			if (isRaw()) {
				imagShape = data.get(1).shape();
				realshape = data.get(0).shape();
				if (imagShape != realshape)
					logger.error("mismatch shapes {} = {}", imagShape, realshape);
			} else if (!isRaw()) {
				long[] dims = data.get(0).shape();
				if (dims.length == 5) {
					realshape = data.get(0).get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all(),
							NDArrayIndex.all(), NDArrayIndex.point(0)).shape();
				}
				if (dims.length == 4) {
					realshape = data.get(0)
							.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(0))
							.shape();
				}
				if (dims.length == 3) {
					realshape = data.get(0).get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(0)).shape();
				}
				if (dims.length == 2) {
					realshape = data.get(0).get(NDArrayIndex.all(), NDArrayIndex.all()).shape();
				}
				if (realshape == null)
					logger.error("Shape of Proccessed data is null");
			}
		}
		return realshape;
	}

	public Boolean isRaw() {
		boolean rawFlag = false;
		if (getConditions().getFiles().contains("acqp") && getConditions().getFiles().contains("method") && 
					(getConditions().getFilename().toString().contains("fid") || getConditions().getFilename().toString().contains("ser"))) {
			rawFlag = true;
		} else if (getConditions().getFiles().contains("visu_pars") && getConditions().getFiles().contains("reco")
				&& getConditions().getFilename().toString().contains("2dseq")) {
			rawFlag = false;
		}
		return rawFlag;
	}
	
	public Boolean isIR() {
		boolean irFlag = false;
		if (!isRaw()) {
				if (getConditions().getFiles().contains("1r") && getConditions().getFiles().contains("1i"))
					irFlag = true;
		}
		return irFlag;
	}
	public void setData(ArrayList<INDArray> data) {
		this.data = data;
	}



//	/**
//	 * get acquisition type
//	 *
//	 * @return a string of the acquisition type
//	 */
//	public String getACQS_TYPE() {
//		return ACQS_TYPE;
//	}
//
//	/**
//	 * set acquisition type externally
//	 *
//	 * @param aCQS_TYPE
//	 */
//	public void setACQS_TYPE(String aCQS_TYPE) {
//		ACQS_TYPE = aCQS_TYPE;
//	}

	/**
	 * read 2dseq file and then reshape & scale and form the frame group
	 *
	 * @param visu_pars a JcampdxData Object which contains a map of parameters of
	 *                  visu_pars
	 * @param reco      a JcampdxData Object which contains a map of parameters of
	 *                  reco
	 * @return an ArrayList of INDArray Objects
	 */
	private ArrayList<INDArray> read_2dseq() {
		Object ArrBD = new Object();
		INDArray data_array;
		try {
			ArrBD = read_2dseq_file();
		} catch (IOException e) {
			logger.error("There is a problem with reading the 2dseq file");
		}
		data_array = reshape_processed(ArrBD);
		data_array = scale(data_array);
		data_array = form_frame_groups(data_array);
//		form_complex();
		ArrayList<INDArray> arraylist = new ArrayList<INDArray>();
		arraylist.add(data_array);
		return arraylist ;
	}
	private ArrayList<INDArray> read_ir() {
		Object ArrBD = new Object();
		INDArray data_array = null;
		ArrayList<INDArray> arraylist = new ArrayList<INDArray>();
		String[] path2ir = new String[] {
				jcampdx.getPath_2dseq() + File.separator + "1r",
				jcampdx.getPath_2dseq() + File.separator + "1i"
				};
		for (String path : path2ir) {
			try {
				ArrBD = read_ir_file(path);
			} catch (IOException e) {
				logger.error("There is a problem with reading the 2dseq file");
			}
			data_array = reshape_processed(ArrBD);
			data_array = scale(data_array);
			data_array = form_frame_groups(data_array);
//			form_complex();
			
			arraylist.add(data_array);
		}
		
		return arraylist ;
	}
	
	private Object read_ir_file(String path)
			throws EOFException, IOException {
		FileInputStream dataStream = new FileInputStream(path);
		DataInputStream dataFilter = new DataInputStream(dataStream);
		DataInputStream buffReader = new DataInputStream(new BufferedInputStream(dataFilter));
		Object ArrBD = dtype(buffReader, parameters.VisuCoreWordType, parameters.VisuCoreByteOrder);
		return ArrBD;
	}
	
	
	
	/**
	 * read 2dseq binary file
	 *
	 * @param visu_pars a JcampdxData Object which contains a map of parameters of
	 *                  visu_pars
	 * @param reco      a JcampdxData Object which contains a map of parameters of
	 *                  reco
	 * @return an ArrayList of Objects
	 * @throws EOFException
	 * @throws IOException
	 */
	private Object read_2dseq_file()
			throws EOFException, IOException {
		FileInputStream dataStream = new FileInputStream(conditions.getPath().toString());
		DataInputStream dataFilter = new DataInputStream(dataStream);
		DataInputStream buffReader = new DataInputStream(new BufferedInputStream(dataFilter));
		Object ArrBD = dtype(buffReader, parameters.VisuCoreWordType, parameters.VisuCoreByteOrder);
		return ArrBD;
	}

	private INDArray reshape_processed(Object ArrBD) {
		// TODO Auto-generated method stub

//		INDArray real = Nd4j.zeros(ArrBD.size());
////		INDArray imag = Nd4j.zeros(ArrBD.size());
//		for (int i = 0; i < ArrBD.size(); i++) {
//			real.putScalar(i, (Integer) ArrBD.get(i));
//		}
		
		INDArray data = null;
		switch (parameters.dataType) {
		case DOUBLE:
			data = Nd4j.create((double[]) ArrBD, new long[] {getConditions().getBinarySize()} , DataType.FLOAT);
			break;
		case INTEGER:
			data = Nd4j.create((int[]) ArrBD, new long[] {getConditions().getBinarySize()} , DataType.FLOAT);
			break;
		case FLOAT:
			data = Nd4j.createFromArray((float[]) ArrBD);
			break;
		default:
			break;
		}
		
		ArrBD = null;
		System.gc();
		
		
		data = data.reshape('f', getConditions().reshape_scheme_2dseq);
		
		return data;
	}

	/**
	 * Scale data
	 *
	 * @param data_array
	 * @param visu_pars
	 * @param reco
	 * @return an ArrayList of INDArray Objects
	 */
	private INDArray scale(INDArray data_array) {
		// TODO check scaling must be done or not
		Parameters p = getParameters();
		long[] shape = data_array.shape();
//		for (int i = 0; i < p.VisuCoreFrameCount; i++) {
			if (p.VisuCoreDim == 1) {
				data_array.getColumn(0).mul(p.VisuCoreDataSlope.getFloat(0));
				data_array.getColumn(0).add(p.VisuCoreDataOffs.getFloat(0));
			} else if (p.VisuCoreDim == 2) {
				for (int j = 0; j < shape[2]; j++) {
					INDArrayIndex[] indx = { NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(j) };
					data_array.get(indx).muli(p.VisuCoreDataSlope.getFloat(j));
					data_array.get(indx).add(p.VisuCoreDataOffs.getFloat(j));
				}
			} else if (p.VisuCoreDim == 3) {
				for (int j = 0; j < shape[3]; j++) {
					INDArrayIndex[] indx = { NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all(),
							NDArrayIndex.point(j) };
					data_array.get(indx).mul(p.VisuCoreDataSlope.getFloat(j));
					data_array.get(indx).add(p.VisuCoreDataOffs.getFloat(j));
				}
			}
		return data_array;
	}

	/**
	 * Form frame group of data
	 *
	 * @param data_array
	 * @param visu_pars  : a JcampdxData Object
	 * @param reco       : a JcampdxData Object
	 * @return an ArrayList of INDArray Objects
	 */
	private INDArray form_frame_groups(INDArray data_array) {
		data_array.reshape('f', getConditions().storage);
		return data_array;
	}

	public ArrayList<INDArray> read_fid() {
		Object ArrBD = new ArrayList<>();
		ArrayList<INDArray> data_array = new ArrayList<INDArray>();
		try {
			ArrBD = read_fid_file();
		} catch (IOException e) {
			logger.error("There is a problem with reading the fid file/n {}", e);
		}

		try {
			data_array = reshape_fid(ArrBD);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("There is a problem with reading the fid file /n {}", e);
		}


		return data_array;
	}

	private void reorder_fid_3d() {
		// TODO will be empty???
		
	}

//	private void paraVision360(JcampdxData acqp, JcampdxData method) {
//		 Object numberOfPoint = method.get("PVM_SpecMatrix");
//		 Object PVM_Matrix = method.get("PVM_Matrix");
//	}

	/**
	 * reorder frames of fid file
	 *
	 * @param data_array
	 * @param acqp
	 * @param method
	 * @return an ArrayList of INDArray Objects
	 */
	public ArrayList<INDArray> reorder_fid_frames_2d(ArrayList<INDArray> data_array) {
		
		int[] PVM_ObjOrderList_sorted = ArrayUtil.argsort(parameters.PVM_ObjOrderList);
		INDArray real = data_array.get(0);
		INDArray imag = data_array.get(1);
		long[] fid_dims = imag.shape();
		if (parameters.NSLICES != parameters.NI) {
			int NR_new = (int) (parameters.NI / parameters.NSLICES);
			int NI_new = parameters.NSLICES;
			reshape_scheme = new int[] { (int) fid_dims[0], (int) fid_dims[1], NR_new, NI_new, parameters.PVM_EncNReceivers };
			permute_scheme = new int[] { 0, 1, 3, 2, 4 };
			real = real.reshape('f', reshape_scheme);
			imag = imag.reshape('f', reshape_scheme);

			real = real.permute(permute_scheme);
			imag = imag.permute(permute_scheme);

		}
		INDArray newOBJ_imag, newOBJ_real;
		for (int i = 0; i < parameters.PVM_ObjOrderList.length; i++) {
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

	/**
	 *
	 * @param data_array
	 * @param acqp       : a JcampdxData Object
	 * @param method     : a JcampdxData Object
	 * @return an ArrayList of INDArray Objects
	 */
	public ArrayList<INDArray> reorder_fid_lines_2d(ArrayList<INDArray> data_array) {		
		INDArray real = data_array.get(0);
		INDArray imag = data_array.get(1);
		INDArray PVM_EncSteps1;
		try {
			PVM_EncSteps1 = parameters.PVM_EncSteps1;
			int[] sorted = argsort(PVM_EncSteps1.toIntVector(), true);
			INDArray OBJ_imag, OBJ_real;
			
			int[] matrix = Arrays.stream(real.shape()).mapToInt(x -> (int) x).toArray();
			ArrayList<int[]> mat = indices(matrix, 2, matrix.length) ;
			for(int[] indices : mat) {
				INDArrayIndex[] indx = { NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(indices[0]),
						NDArrayIndex.point(indices[1]), NDArrayIndex.point(indices[2]) };
				OBJ_imag = imag.get(indx);
				imag.put(indx, OBJ_imag.getColumns(sorted));
				OBJ_real = real.get(indx);
				real.put(indx, OBJ_real.getColumns(sorted));
			}

			data_array.clear();
			data_array.add(0, real);
			data_array.add(1, imag);
			
		} catch (Exception e) {
			// TODO: handle exception
			
		}
		
		
		/**
		 *         if dir == 'BW':
            PVM_EncSteps1_sorted = self.permutation_inverse(PVM_EncSteps1_sorted)


        if np.array_equal(PVM_EncSteps1_sorted,PVM_EncSteps1):
            return data
		 */
		
		
		return data_array;
		
	}
	
	
	public ArrayList<INDArray> mirror_odd_lines(ArrayList<INDArray> data_array) {	
		INDArray real = data_array.get(0);
		INDArray imag = data_array.get(1);
		INDArray OBJ_imag, OBJ_real;
				int[] matrix = Arrays.stream(real.shape()).mapToInt(x -> (int) x).toArray();
				ArrayList<int[]> mat = indices(matrix, 2, matrix.length) ;
				for(int[] indices : mat) {
					
						
					INDArrayIndex[] indx = { NDArrayIndex.all(), NDArrayIndex.interval(1, 2, real.shape()[1]), NDArrayIndex.point(indices[0]),
							NDArrayIndex.point(indices[1]), NDArrayIndex.point(indices[2]) };
					
					OBJ_imag = imag.get(indx);
					OBJ_real = real.get(indx);

					int[] idx = new int[(int) OBJ_imag.shape()[0]];
					for(int i = 0 ; i<OBJ_imag.shape()[0] ; i ++)
						idx[i] = (int) (OBJ_imag.shape()[0] - i -1);
					imag.put(indx, OBJ_imag.getRows(idx));
					real.put(indx, OBJ_real.getRows(idx));
				}
		
		data_array.clear();
		data_array.add(0, real);
		data_array.add(1, imag);
		return data_array;
	}

	/**
	 * reshape fid file
	 *
	 * @param ArrBD
	 * @param acqp   : a JcampdxData Object
	 * @param method : a JcampdxData Object
	 * @return an ArrayList of INDArray Objects
	 */
	public ArrayList<INDArray> reshape_fid(Object ArrBD) {
		INDArray real = null;
		INDArray imag = null;
		INDArray data = null;
		
		switch (parameters.dataType) {
		case DOUBLE:
			data = Nd4j.createFromArray((double[]) ArrBD);
			break;
		case INTEGER:
			data = Nd4j.createFromArray((int[]) ArrBD);
			break;
		case FLOAT:
			data = Nd4j.createFromArray((float[]) ArrBD);
			break;
		default:
			break;
		}
		ArrBD = null;
		System.gc();
		
		data = data.reshape('f', conditions.getStorage());
		
		data = acquistion_trim(data);
		
		real = data.get(NDArrayIndex.interval(0, 2, data.size(0)));
		imag = data.get(NDArrayIndex.interval(1, 2, data.size(0))) ;

		real = real.reshape('f', conditions.encoding_space_shape);
		imag = imag.reshape('f', conditions.encoding_space_shape);
			
		real = real.permute(conditions.permute_scheme);
		imag = imag.permute(conditions.permute_scheme);

		real = real.reshape('f', conditions.k_space_shape);
		imag = imag.reshape('f', conditions.k_space_shape);

		ArrayList<INDArray> data_array = new ArrayList<INDArray>();
		data_array.add(real);
		data_array.add(imag);
		
		data_array = reorder_fid_lines_2d(data_array);
		
		if (conditions.getACQS_TYPE() == ACQ_TYPE.EPI) {
			data_array = mirror_odd_lines(data_array);
		}
		
		return data_array;
	}

	private INDArray acquistion_trim(INDArray data) {
		// TODO Auto-generated method stub
		int acquisition_offset = conditions.getAcquisition_position()[0];
		int acquisition_length = conditions.getAcquisition_position()[1];
		int block_length = conditions.getStorage()[0];
		
		if (acquisition_offset > 0) {
			int blocks = conditions.getStorage()[1];
			int channels = conditions.k_space_shape[conditions.k_space_dim_desc.indexOf("channel")];
			acquisition_offset = Math.floorDiv(acquisition_offset, channels);
		    acquisition_length = Math.floorDiv(acquisition_length, channels);
		    data = data.reshape('f', new int[] {conditions.getStorage()[0] , channels, blocks});
			data = data.get(NDArrayIndex.interval(acquisition_offset, acquisition_offset+acquisition_length),NDArrayIndex.all(),NDArrayIndex.all()).reshape('f', new int[] {acquisition_length*channels, blocks});
			return data;
		} else {
			if (acquisition_length != block_length) {
				data = data.get(NDArrayIndex.interval(0, acquisition_length),NDArrayIndex.all());
				return data;
			} else {
				return data;
			}
		}
	}

	/**
	 *
	 * @param acqp : a JcampdxData Object
	 * @return
	 */
	public int[] get_acq_trim() {
		ACQ_TYPE ACQS_TYPE = conditions.getACQS_TYPE();
		INDArray ACQ_size = parameters.ACQ_size;
		if (ACQS_TYPE == ACQ_TYPE.RADIAL) {
			return new int[] { 0, (int) ACQ_size.getInt(0) / 2 };
			// again divided to 2?
		} else {
			return new int[] { 0, (int) ACQ_size.getInt(0) };
		}
	}

	/**
	 * reorder fid matrix
	 *
	 * @param acqp   : a JcampdxData Object
	 * @param method : a JcampdxData Object
	 */
	public void get_reorder_schemes_fid() {
//		parameters.ACQ_size.putScalar(0, parameters.ACQ_size.getInt(0) / 2);
		conditions.getEncoding_space_shape();
	}

	/**
	 * read fid file
	 *
	 * @param acqp   : a JcampdxData Object
	 * @param method : a JcampdxData Object
	 * @return
	 * @throws IOException
	 */
	public Object read_fid_file() throws IOException {
		FileInputStream dataStream = new FileInputStream(conditions.getPath().toString());
		DataInputStream dataFilter = new DataInputStream(dataStream);
		DataInputStream buffReader = new DataInputStream(new BufferedInputStream(dataFilter));
		Object ArrBD = dtype(buffReader, parameters.Acqp_GO_raw_data_format, parameters.Acqp_BYTORDA);
		return ArrBD;
	}

	/**
	 * convert fid binary file to decimal
	 *
	 * @param buffReader
	 * @param arg1       byte format
	 * @param arg2       byte order
	 * @return
	 * @throws EOFException
	 * @throws IOException
	 */
	private Object dtype(DataInputStream buffReader, String arg1, String arg2)
			throws EOFException, IOException {
		
		Object ArrBD;
		if ((arg1.contentEquals("GO_32BIT_SGN_INT") || arg1.contentEquals("_32BIT_SGN_INT"))
				&& (arg2.contentEquals("little") || arg2.contentEquals("littleEndian"))) {
			parameters.dataType = DATA_TYPE.INTEGER;
			ArrBD = rev_int32_conversion(buffReader);
		} else if ((arg1.contentEquals("GO_16BIT_SGN_INT") || arg1.contentEquals("_16BIT_SGN_INT"))
				&& (arg2.contentEquals("little") || arg2.contentEquals("littleEndian"))) {
			ArrBD = rev_int16_conversion(buffReader);
			parameters.dataType = DATA_TYPE.INTEGER;
		} else if ((arg1.contentEquals("GO_32BIT_FLOAT") || arg1.contentEquals("_32BIT_FLOAT"))
				&& (arg2.contentEquals("little") || arg2.contentEquals("littleEndian"))) {
			ArrBD = rev_float_conversion(buffReader);
			parameters.dataType = DATA_TYPE.FLOAT;
		} else if ((arg1.contentEquals("GO_32BIT_SGN_INT") || arg1.contentEquals("_32BIT_SGN_INT"))
				&& (arg2.contentEquals("big") || arg2.contentEquals("bigEndian"))) {
			parameters.dataType = DATA_TYPE.INTEGER;
			ArrBD = int_conversion(buffReader);
		} else if ((arg1.contentEquals("GO_16BIT_SGN_INT") || arg1.contentEquals("_16BIT_SGN_INT"))
				&& (arg2.contentEquals("big") || arg2.contentEquals("bigEndian"))) {
			parameters.dataType = DATA_TYPE.INTEGER;
			ArrBD = int16_conversion(buffReader);
		} else if ((arg1.contentEquals("GO_32BIT_FLOAT") || arg1.contentEquals("_32BIT_FLOAT"))
				&& (arg2.contentEquals("big") || arg2.contentEquals("bigEndian"))) {
			parameters.dataType = DATA_TYPE.FLOAT;
			ArrBD = float_conversion(buffReader);
		} else if ((arg1.contentEquals("GO_32BIT_DOUBLE") || arg1.contentEquals("_32BIT_DOUBLE"))
				&& (arg2.contentEquals("little") || arg2.contentEquals("littleEndian"))) {
			parameters.dataType = DATA_TYPE.DOUBLE;
			ArrBD = rev_double_conversion(buffReader);
		} else if ((arg1.contentEquals("GO_32BIT_DOUBLE") || arg1.contentEquals("_32BIT_DOUBLE"))
				&& (arg2.contentEquals("big") || arg2.contentEquals("bigEndian"))) {
			parameters.dataType = DATA_TYPE.DOUBLE;
			ArrBD = double_conversion(buffReader);
		} else {
			parameters.dataType = DATA_TYPE.INTEGER;
			ArrBD = rev_int32_conversion(buffReader);
			logger.error("Data format not specified correctly, set to int32, little endian");
		}
		return ArrBD;
	}

	/**
	 * convert binary to 32-bit Integer format in little Indian order
	 *
	 * @para DataInputStream Object of buffReader binary buffer
	 * @return
	 * @throws EOFException
	 * @throws IOException
	 */
	private Object rev_int32_conversion(DataInputStream buffReader) throws EOFException, IOException {

		int storage = conditions.getBinarySize();
		int[] data = new int[storage];
		int i = 0;
		while (buffReader.available() > 0) {
			if(i == storage) 
				break;
			int ch1 = buffReader.read();
			int ch2 = buffReader.read();
			int ch3 = buffReader.read();
			int ch4 = buffReader.read();
			if ((ch1 | ch2 | ch3 | ch4) < 0)
				throw new EOFException();
			data [i] = ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
			i++;
		}
		return data;
		
	}

	/**
	 * convert binary to 32-bit Integer format in big Indian order
	 *
	 * @param buffReader
	 * @return
	 * @throws IOException
	 */
	private Object int_conversion(DataInputStream buffReader) throws IOException {
		int storage = conditions.getBinarySize();
		int[] data = new int[storage];
		int i = 0;
		while (buffReader.available() > 0) {
			if(i == storage) 
				break;
			data[i] = buffReader.readInt();
			i++;
		}
		return data;
	}

	/**
	 * convert binary to 16-bit Integer format in little Indian order
	 *
	 * @param buffReader
	 * @return
	 * @throws EOFException
	 * @throws IOException
	 */
	private Object rev_int16_conversion(DataInputStream buffReader) throws EOFException, IOException {
		int storage = conditions.getBinarySize();
		int[] data = new int[storage];
		int i = 0;
		while (buffReader.available() > 0) {
			if(i == storage) 
				break;
			int ch1 = buffReader.read();
			int ch2 = buffReader.read();
			if ((ch1 | ch2) < 0)
				throw new EOFException();
			data[i] = ((ch2 << 8) + (ch1 << 0));
			i++;
			
		}
		return data;
	}

	/**
	 * convert binary to 16-bit Integer format in big Indian order
	 *
	 * @param buffReader
	 * @return
	 * @throws EOFException
	 * @throws IOException
	 */
	private Object int16_conversion(DataInputStream buffReader) throws EOFException, IOException {
		int storage = conditions.getBinarySize();
		int[] data = new int[storage];
		int i = 0;
		while (buffReader.available() > 0) {
			if(i == storage) 
				break;
			int ch1 = buffReader.read();
			int ch2 = buffReader.read();
			if ((ch1 | ch2) < 0)
				throw new EOFException();
			data[i] = ((ch1 << 8) + (ch2 << 0));
			i++;
			
		}
		return data;
	}

	/**
	 * convert binary to Float format in little Indian order
	 *
	 * @param buffReader
	 * @return
	 * @throws IOException
	 */
	private Object rev_float_conversion(DataInputStream buffReader) throws IOException {
		int storage = conditions.getBinarySize();
		float[] data = new float[storage];
		int i = 0;
		while (buffReader.available() > 0) {
			if(i == storage) 
				break;
			int ch1 = buffReader.read();
			int ch2 = buffReader.read();
			int ch3 = buffReader.read();
			int ch4 = buffReader.read();
			if ((ch1 | ch2 | ch3 | ch4) < 0)
				throw new EOFException();
			data[i] = Float.intBitsToFloat(((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0)));
			i++;
		}
		return data;
	}

	/**
	 * convert binary to Float format in big Indian order
	 *
	 * @param buffReader
	 * @return
	 * @throws IOException
	 */
	private Object float_conversion(DataInputStream buffReader) throws IOException {
		int storage = conditions.getBinarySize();
		float[] data = new float[storage];
		int i = 0;
		while (buffReader.available() > 0) {
			if(i == storage) 
				break;
			data[i] = buffReader.readFloat();
			i++;
		}
		return data;
	}

	/**
	 * convert binary to Double format in little Indian order
	 *
	 * @param buffReader
	 * @return
	 * @throws IOException
	 */
	private Object rev_double_conversion(DataInputStream buffReader) throws IOException {
		int storage = conditions.getBinarySize();
		double[] data = new double[storage];
		int i = 0;
		while (buffReader.available() > 0) {
			if(i == storage) 
				break;
			long ch1 = buffReader.read();
			long ch2 = buffReader.read();
			long ch3 = buffReader.read();
			long ch4 = buffReader.read();
			long ch5 = buffReader.read();
			long ch6 = buffReader.read();
			long ch7 = buffReader.read();
			long ch8 = buffReader.read();
			if ((ch1 | ch2 | ch3 | ch4 | ch5 | ch6 | ch7 | ch8) < 0)
				throw new EOFException();
			data[i] = Double.longBitsToDouble(((ch8 << 56) + (ch7 << 48) + (ch6 << 40) + (ch5 << 32) + (ch4 << 24)
					+ (ch3 << 16) + (ch2 << 8) + (ch1 << 0)));
			i++;
		}
		return data;
	}

	/**
	 * convert binary to Double format in big Indian order
	 *
	 * @param buffReader
	 * @return
	 * @throws IOException
	 */
	private Object double_conversion(DataInputStream buffReader) throws IOException {
		int storage = conditions.getBinarySize();
		double[] data = new double[storage];
 		int i = 0;
		while (buffReader.available() > 0) {
			if(i == storage) 
				break;
			data[i] = buffReader.readDouble();
			buffReader.readDouble();
			i++;
		}
		return data;
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
	
	
	public ArrayList<int[]> indices(int[] shape) {
		return indices(shape, 0, shape.length );
	}
	
	public ArrayList<int[]> indices(int[] shape, int from, int to ) {
		int[] realshape = Arrays.copyOfRange(shape, from, to);
		int[] indx = new int[realshape.length];
		boolean endFlag = true;
		int temp = 0;
		ArrayList<int[]> mat = new ArrayList<int[]>();
		int l = 0;
		boolean flag = true;
		mat.add(indx.clone());
		
		int j = realshape.length - 1;
		for (int i = 0; i < product(realshape) - 1; i++) {
			
			if ((j == realshape.length - 1)) {
				while ((indx[j] == realshape[j] - 1)) {
					if (j > 0) {
						indx[j] = 0;
						j -= 1;
					} else {
						j = realshape.length - 1;
					}
				}
			} else {
				if ((indx[j] > realshape[j] - 1)) {
					if (j > 0) {
						indx[j] = 0;
						j -= 1;
					} else {
						j = realshape.length - 1;
					}
				} else {
					j = realshape.length - 1;
				}
			}
			indx[j] += 1;
			j = realshape.length - 1;
			mat.add(indx.clone());
		}
		return mat;
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
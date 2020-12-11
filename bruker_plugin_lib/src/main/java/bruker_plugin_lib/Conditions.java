package bruker_plugin_lib;


import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Conditions {
	private Logger logger = LoggerFactory.getLogger(Conditions.class);
	private Path path;
	private ACQ_TYPE ACQS_TYPE;
	private List<Object> files;
	private String filename;
	private Bruker bruker;
	int block_count;
	int[] encoding_space_shape;
	int[] k_space_shape;
	int[] permute_scheme;
	ArrayList<String> k_space_dim_desc;
	Integer single_acq_length;
	Integer block_size;
	Integer itemsize;
	int[] storage;
	int binarySize;
	int[] acquisition_position;
	Boolean is_single_slice = null;
	ArrayList<Object> dim_type;
	Integer frames;
	int[] frame_groups;
	long[] reshape_scheme_2dseq;
	
	public int getBinarySize() {
		binarySize = product(storage);
		return binarySize;
	}

	public void setBinarySize(int binarySize) {
		this.binarySize = binarySize;
	}

	public int[] getStorage() {
		return storage;
	}

	public void setStorage(int[] storage) {
		this.storage = storage;
	}

	
	
	public int[] getAcquisition_position() {
		return acquisition_position;
	}

	public void setAcquisition_position(int[] acquisition_position) {
		this.acquisition_position = acquisition_position;
	}

	public Conditions(Bruker bruker) {
		this.bruker = bruker;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
		files = scan_dir(path.getParent());
		bruker.setJcampdx(new Jcampdx(bruker));
		bruker.setParameters(new Parameters(bruker));
		if (bruker.isRaw()) {
			getFidConds();
		} else if (!bruker.isRaw()) {
			get2dseqConds();
		}
		
	}

	private void get2dseqConds() {
		// TODO Auto-generated method stub
		Parameters p = bruker.getParameters();
		int[] dim_size = new int[0];
		
		try {
			dim_size = new int[p.VisuFGOrderDescDim];
			for (int i = 0; i < p.VisuFGOrderDescDim; i++) {
				Object[] obj = (Object[]) p.VisuFGOrderDesc.get(i);
				String str_obj = obj[0].toString();
				String cmplx_flag = obj[1].toString();
				int int_obj = Integer.parseInt(str_obj);
				dim_size[i] = int_obj;
			}
		} catch (Exception e) {
		}

		storage = new int[(int) (p.VisuCoreSize.length() + dim_size.length)];
		for (int i = 0; i < p.VisuCoreSize.length(); i++) {
			storage[i] = p.VisuCoreSize.getInt(i);
		}
		
		try {
			for (int i = (int) p.VisuCoreSize.length(); i < p.VisuCoreSize.length() + dim_size.length; i++) {
				if (dim_size[(int) (i - p.VisuCoreSize.length())] == 0) {
					storage[i] = 1;
				} else {
					storage[i] = dim_size[(int) (i - p.VisuCoreSize.length())];
				}
				
			}
		} catch (Exception e) {
		}

		
		frame_groups = dim_size;
        frames = p.VisuCoreFrameCount;
        

		reshape_scheme_2dseq = new long[(int) (p.VisuCoreSize.length() + 1)];
		for (int i = 0; i < p.VisuCoreSize.length(); i++) {
			reshape_scheme_2dseq[i] = p.VisuCoreSize.getInt(i);
		}
			reshape_scheme_2dseq[(int) (p.VisuCoreSize.length())] = p.VisuCoreFrameCount;
        
	}

	private void getFidConds() {
		// TODO Auto-generated method stub
		ident_ACQS_TYPE();
		iden_data_type();
		ident_single_acq_length(null);
		ident_block_size(null);
		ident_Block_count();
		storage = new int[] {block_size, block_count};
		// move encoding space kspace ....
		if (ACQS_TYPE == ACQ_TYPE.EPI) {
			acquisition_position = new int[] {block_size - single_acq_length, single_acq_length};
		} else {
			acquisition_position = new int[] {0, single_acq_length};
		}
		ident_Shape();
	}
	
	public ACQ_TYPE getACQS_TYPE() {
		return ACQS_TYPE;
	}

	public void setACQS_TYPE(ACQ_TYPE aCQS_TYPE) {
		ACQS_TYPE = aCQS_TYPE;
	}

	public List<Object> getFiles() {
		return files;
	}

	public String getFilename() {
		return path.getFileName().toString();
	}

	public int getBlock_count() {
		return block_count;
	}

	public void setBlock_count(int block_count) {
		this.block_count = block_count;
	}

	public int[] getEncoding_space_shape() {
		ident_Shape();
		return encoding_space_shape;
	}

	public void setEncoding_space_shape(int[] encoding_space_shape) {
		this.encoding_space_shape = encoding_space_shape;
	}

	private void ident_Block_count() {
		Parameters p = bruker.getParameters();
		switch (ACQS_TYPE) {
		case CART_2D:
			block_count = p.PVM_EncMatrix.getInt(1) * p.NI * p.NR;
			break;
		case CART_3D:
			block_count = p.NR * p.ACQ_size.getInt(1) * p.ACQ_size.getInt(2);
			break;
		case RADIAL:
			block_count = p.NPro * p.NR * p.NI;
			break;
		case FIELD_MAP:
			block_count = p.PVM_EncMatrix.getInt(1) * p.PVM_EncMatrix.getInt(2) * p.PVM_NEchoImages;
			break;
		case ZTE:
			block_count = p.NPro * p.NR * p.NI;
			break;
		case SPIRAL:
			block_count = p.PVM_SpiralNbOfInterleaves * p.NI * p.NR;
			break;
		case CSI:
			if (p.PV_version.contains("360")) {
				block_count = p.PVM_EncMatrix.getInt(0) * p.PVM_EncMatrix.getInt(1);
				break;
			} else {
				block_count = p.ACQ_size.getInt(1) * p.ACQ_size.getInt(2);
				break;
			}
		case SPECTROSCOPY:
			block_count = p.NR;
			break;
		case EPI:
			block_count = p.NSegments*p.NI*p.NR;
		default:
			break;
		}
	}

	private void ident_Shape() {
		Parameters p = bruker.getParameters();
		switch (ACQS_TYPE) {
		case CART_2D:
			encoding_space_shape = new int[] { 
					p.PVM_EncMatrix.getInt(0),
					p.PVM_EncNReceivers,
					p.ACQ_phase_factor,
					p.NI,
					p.PVM_EncMatrix.getInt(1) / p.ACQ_phase_factor,
					p.NR };
			k_space_shape = new int[] {
					p.PVM_EncMatrix.getInt(0),
					p.PVM_EncMatrix.getInt(1),
					p.NI,
					p.NR,
					p.PVM_EncNReceivers };
			permute_scheme = new int[] { 0, 2, 4, 3, 5, 1 };
			k_space_dim_desc = new ArrayList<String>();
			k_space_dim_desc.add("kspace_encode_step_0");
			k_space_dim_desc.add("kspace_encode_step_1");
			k_space_dim_desc.add("slice");
			k_space_dim_desc.add("repetition");
			k_space_dim_desc.add("channel");
			break;
		case CART_3D:
			encoding_space_shape = new int[] { 
					p.ACQ_size.getInt(0)/2,
					p.PVM_EncNReceivers,
					p.ACQ_phase_factor,
					p.ACQ_size.getInt(2),
					p.ACQ_size.getInt(1) / p.ACQ_phase_factor,
					p.NR };
			k_space_shape = new int[] { 
					p.PVM_EncMatrix.getInt(0), 
					p.ACQ_size.getInt(1), 
					p.ACQ_size.getInt(2),
					p.NR, 
					p.PVM_EncNReceivers };
			permute_scheme = new int[] { 0, 2, 3, 4, 5, 1 };
			break;
		case RADIAL:
			encoding_space_shape = new int[] {
					p.ACQ_size.getInt(0)/2,
					p.PVM_EncNReceivers,
					p.ACQ_phase_factor,
					p.NI,
					p.NPro / p.ACQ_phase_factor,
					p.NR };
			k_space_shape = new int[] {
					p.ACQ_size.getInt(0), 
					p.NPro, 
					p.NI, 
					p.NR, 
					p.PVM_EncNReceivers };
			permute_scheme = new int[] { 0, 2, 4, 3, 5, 1 };
			break;
		case CSI:
			if (bruker.getParameters().PV_version.contains("360")) {
				encoding_space_shape = new int[] { 
						p.numberOfPoint.getInt(0),
						p.PVM_Matrix.getInt(0),
						p.PVM_Matrix.getInt(1) };
				k_space_shape = encoding_space_shape;
				permute_scheme = new int[] { 0, 1, 2 };
			} else {
				encoding_space_shape = new int[] { 
						p.ACQ_size.getInt(0)/2,
						p.ACQ_size.getInt(1), 
						p.ACQ_size.getInt(2) };
				k_space_shape = encoding_space_shape;
				permute_scheme = new int[] { 0, 1, 2 };
			}

			break;
		case EPI:
			encoding_space_shape = new int[] { 
			                         p.PVM_EncMatrix.getInt(0) * p.PVM_EncMatrix.getInt(1) / p.NSegments,
			                         p.PVM_EncNReceivers,
			                         p.NSegments,
			                         p.NI,
			                         p.NR};
			k_space_shape = new int[] {
			                         p.PVM_EncMatrix.getInt(0),
			                         p.PVM_EncMatrix.getInt(1),
			                         p.NI,
			                         p.NR,
			                         p.PVM_EncNReceivers};
			permute_scheme = new int[] { 0,2,3,4,1 };
			k_space_dim_desc = new ArrayList<String>();
			k_space_dim_desc.add("kspace_encode_step_0");
			k_space_dim_desc.add("kspace_encode_step_1");
			k_space_dim_desc.add("slice");
			k_space_dim_desc.add("repetition");
			k_space_dim_desc.add("channel");
			break;
		case SPIRAL:
			encoding_space_shape = new int[] { 
					p.ACQ_size.getInt(0)/2,
					p.PVM_EncNReceivers,
					p.PVM_SpiralNbOfInterleaves,
					p.NI,
					p.NR };
			k_space_shape = new int[] {
					p.ACQ_size.getInt(0)/2,
					p.PVM_SpiralNbOfInterleaves,
					p.NI,
					p.NR, 
					p.PVM_EncNReceivers};
			permute_scheme = new int[] { 0,2,3,4,1 };
		case SPECTROSCOPY:
			encoding_space_shape = new int[] { 
					p.ACQ_size.getInt(0)/2,
					p.NR };
			k_space_shape = new int[] {
					p.ACQ_size.getInt(0)/2,
					p.NR, };
			permute_scheme = new int[] { 0, 1 };
		default:
			break;
		}
	}

	/**
	 * Scan the directory of study and list all files
	 *
	 * @param path : a path to study directory
	 * @return a List array of Objects
	 */
	private List<Object> scan_dir(Path path) {
		List<Object> list_scan_result = null;
		try (Stream<Path> walk = Files.walk(path)) {
			list_scan_result = walk.filter(Files::isRegularFile).map(x -> x.getFileName().toString())
					.collect(Collectors.toList());
		} catch (IOException e) {
			logger.error("Bruker is not able to scan directory {}", path);
		}
		return list_scan_result;
	}

	/**
	 * identify acquisition type of data set
	 */
	private void ident_ACQS_TYPE() {
		if (bruker.getParameters().CSISignalType == null) {
			if (bruker.getParameters().ACQ_dim_desc
					.equals(Arrays.asList((new String[] { "Spectroscopic", "Spatial", "Spatial" })))) {
				setACQS_TYPE(ACQ_TYPE.CSI);
				return;
			}
			/*
			 * if (bruker.getParameters().ACQ_dim_desc .equals(Arrays.asList((new String[] {
			 * "Spectroscopic", "Spectroscopic" })))) { ACQS_TYPE = ACQ_TYPE.EPI;
			 * setACQS_TYPE(ACQS_TYPE); }
			 */
		}
		if (bruker.getParameters().ACQ_dim == 1) {
			setACQS_TYPE(ACQ_TYPE.SPECTROSCOPY);
			return;}
		if (bruker.getParameters().ACQ_method.toUpperCase().contains("EPI") || 
				bruker.getParameters().ACQ_method.toUpperCase().contains("EPSI")) {
			setACQS_TYPE(ACQ_TYPE.EPI);
			return;}
		if (bruker.getParameters().NPro != null && bruker.getParameters().ACQ_dim == 2) {
			setACQS_TYPE(ACQ_TYPE.RADIAL);
			return;
		} else if (bruker.getParameters().NPro != null && bruker.getParameters().ACQ_dim == 3) {
			setACQS_TYPE(ACQ_TYPE.RADIAL);
			return;
		} else if (bruker.getParameters().CSISignalType != null) {
			setACQS_TYPE(ACQ_TYPE.CSI);
			return;
		} else if (bruker.getParameters().ACQ_dim == 3) {
			setACQS_TYPE(ACQ_TYPE.CART_3D);
			return;
		} else if (bruker.getParameters().ACQ_method.contains("Spiral")) {
			setACQS_TYPE(ACQ_TYPE.SPIRAL);
			return;
		} else {
			setACQS_TYPE(ACQ_TYPE.CART_2D);
			return;
		}

	}
	
	public void ident_single_acq_length(Integer channels) {
		Parameters p = bruker.getParameters();
		int segments;
		INDArray ACQ_size = p.ACQ_size;
		String GO_block_size = p.GO_block_size;
		Integer PVM_EncNReceivers = null;
		if (bruker.getParameters().PV_version.contains("360")) {
			//to do -> not tested
			ArrayList mat = bruker.getJcampdx().getAcqp().getArrayList("ACQ_jobs");
			Object o = mat.get(0);
			Integer out = Integer.valueOf(((Object[])o)[0].toString());
			single_acq_length = out;
		} else {
			if (channels != null) {
				PVM_EncNReceivers = channels;
			} else {
				PVM_EncNReceivers = p.PVM_EncNReceivers;
			}
			if (getACQS_TYPE() == ACQS_TYPE.SPIRAL)
				GO_block_size = "Standard_KBlock_Format";
			if (p.PULPROG.contains("EPSI.ppg")) {
				segments = p.NSegments;
			} else {
				segments = 1;
			}
			
			
			if(p.ACQ_dim_desc.stream().anyMatch("Spectroscopic" :: contains))
				PVM_EncNReceivers = 1;
			
			if (GO_block_size == "Standard_KBlock_Format") {
				single_acq_length =  ACQ_size.getInt(0) * PVM_EncNReceivers;
			} else {
				single_acq_length =  Math.floorDiv(2 * p.PVM_DigNp  * PVM_EncNReceivers, segments);
			}
		}
	}
	
	
	public void ident_block_size(Integer channels) {
		Parameters p = bruker.getParameters();
		int segments;
		INDArray ACQ_size = p.ACQ_size;
		String GO_block_size = p.GO_block_size;
		Integer PVM_EncNReceivers = null;
		if (bruker.getParameters().PV_version.contains("360")) {
			//to do -> not tested
			ArrayList mat = bruker.getJcampdx().getAcqp().getArrayList("ACQ_jobs");
			Object o = mat.get(0);
			Integer out = Integer.valueOf(((Object[])o)[0].toString());
			block_size =  out;
		} else {
			if (channels != null) {
				PVM_EncNReceivers = channels;
			} else {
				PVM_EncNReceivers = p.PVM_EncNReceivers;
			}
			
			try {
				if(p.ACQ_dim_desc.get(0).contains("Spectroscopic"))
					PVM_EncNReceivers = 1;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				
			}
			Integer single_acq = ACQ_size.getInt(0) * PVM_EncNReceivers;
			if (GO_block_size == "Standard_KBlock_Format") {
				block_size =  (int) ((Math.ceil(single_acq * itemsize  / 1024.) * 1024. / itemsize));
			} else {
				block_size =  single_acq;
			}
		}
	}
	
	public void iden_data_type () {
		dtype(bruker.getParameters().Acqp_GO_raw_data_format, bruker.getParameters().Acqp_BYTORDA);
	}
	
	private void dtype(String arg1, String arg2) {
		
		ArrayList<Object> ArrBD = new ArrayList<>();
		if ((arg1.contentEquals("GO_32BIT_SGN_INT") || arg1.contentEquals("_32BIT_SGN_INT"))
				&& (arg2.contentEquals("little") || arg2.contentEquals("littleEndian"))) {
			itemsize = 4;
			bruker.getParameters().dataType = DATA_TYPE.INTEGER;
		} else if ((arg1.contentEquals("GO_16BIT_SGN_INT") || arg1.contentEquals("_16BIT_SGN_INT"))
				&& (arg2.contentEquals("little") || arg2.contentEquals("littleEndian"))) {
			itemsize = 2;
			bruker.getParameters().dataType = DATA_TYPE.INTEGER;
		} else if ((arg1.contentEquals("GO_32BIT_FLOAT") || arg1.contentEquals("_32BIT_FLOAT"))
				&& (arg2.contentEquals("little") || arg2.contentEquals("littleEndian"))) {
			itemsize = 4;
			bruker.getParameters().dataType = DATA_TYPE.FLOAT;
		} else if ((arg1.contentEquals("GO_32BIT_SGN_INT") || arg1.contentEquals("_32BIT_SGN_INT"))
				&& (arg2.contentEquals("big") || arg2.contentEquals("bigEndian"))) {
			itemsize = 4;
			bruker.getParameters().dataType = DATA_TYPE.INTEGER;
		} else if ((arg1.contentEquals("GO_16BIT_SGN_INT") || arg1.contentEquals("_16BIT_SGN_INT"))
				&& (arg2.contentEquals("big") || arg2.contentEquals("bigEndian"))) {
			itemsize = 2;
			bruker.getParameters().dataType = DATA_TYPE.INTEGER;
		} else if ((arg1.contentEquals("GO_32BIT_FLOAT") || arg1.contentEquals("_32BIT_FLOAT"))
				&& (arg2.contentEquals("big") || arg2.contentEquals("bigEndian"))) {
			bruker.getParameters().dataType = DATA_TYPE.FLOAT;
			itemsize = 4;
		} else if ((arg1.contentEquals("GO_32BIT_DOUBLE") || arg1.contentEquals("_32BIT_DOUBLE"))
				&& (arg2.contentEquals("little") || arg2.contentEquals("littleEndian"))) {
			bruker.getParameters().dataType = DATA_TYPE.DOUBLE;
			itemsize = 8;
		} else {
			bruker.getParameters().dataType = DATA_TYPE.INTEGER;
			itemsize = 4;
			logger.error("Data format not specified correctly, set to int32, little endian");
		}
	}
	
	public boolean is_single_slice() {
		boolean is_fg_size;
		if (is_single_slice != null) {
			return is_single_slice;
		} else {
			if (dim_type.contains("FG_SLICE")) {
				is_fg_size = true;
			} else {
				is_fg_size = false;
			}
			if (is_fg_size && bruker.getParameters().VisuCoreDim > 3 ) {
				is_single_slice = false;
			} else {
				is_single_slice = true;
			}
			return is_single_slice;
		}
	}
	public ArrayList<Object> dim_type() {
		if (dim_type != null) {
			return dim_type;
		} else {
			Parameters p = bruker.getParameters();
			for(int i=0; i<p.VisuCoreDimDesc.size(); i++) {
				dim_type.add(p.VisuCoreDimDesc.get(i));
			}
			for(int i=0; i<p.VisuFGOrderDesc.size(); i++) {
//				dim_type.add(VisuCoreDimDesc(i))
			}
			if((dim_type.get(0) == "spatial") && p.VisuCoreDim < 3 && dim_type.contains("FG_SLICE"))
					dim_type.add(2, "spatial");
			return dim_type;
		}
	}
	
	int product(int ar[]) {
		int result = 1;
		for (int i = 0; i < ar.length; i++)
			result = result * ar[i];
		return result;
	}
}

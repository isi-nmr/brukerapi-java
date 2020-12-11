

import java.util.ArrayList;
import java.util.Arrays;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Parameters {
	private Logger logger = LoggerFactory.getLogger(Parameters.class);
	INDArray PVM_EncMatrix;
	Integer PVM_EncNReceivers;
	Integer ACQ_phase_factor;
	INDArray ACQ_size;
	Integer NR;
	Integer NI;
	Integer NSLICES;
	Integer ACQ_dim;
	Integer NPro;
	Integer NECHOES;
	String CSISignalType;
	Integer PVM_NEchoImages;
	String PV_version;
	ArrayList<String> ACQ_dim_desc;
	Integer PVM_EncTotalAccel;
	INDArray numberOfPoint;
	INDArray PVM_Matrix;
	private Bruker bruker;
	private String[] PV_VERSIONS =  new String[] {"5.1", "6.0", "6.0.1", "360"};
	String Acqp_GO_raw_data_format = null;
	String Acqp_BYTORDA = null;
	DATA_TYPE dataType;
	INDArray PVM_EncSteps1;
	int[] PVM_ObjOrderList;
	Integer PVM_SpecMatrix;
	Integer NSegments;
	String ACQ_method;
	Integer PVM_SpiralNbOfInterleaves;
	String GO_block_size;
	String PULPROG;
	Integer PVM_DigNp;
	String VisuCoreWordType;
	String VisuCoreByteOrder;
	ArrayList VisuFGOrderDesc;
	Integer VisuFGOrderDescDim;
	Integer VisuCoreDim;
	ArrayList VisuCoreDimDesc;
	INDArray VisuCoreSize;
	Integer VisuCoreFrameCount;
	INDArray VisuCoreDataSlope;
	INDArray VisuCoreDataOffs;
	
	
	
	public Parameters(Bruker  bruker) {
		this.bruker = bruker;
		if (bruker.isRaw()) {
			getFidParams();
		} else if (!bruker.isRaw()) {
			get2dseqParams();
		}
	}

	private void get2dseqParams() {
		// TODO Auto-generated method stub
		try {
			VisuCoreWordType = bruker.getJcampdx().getVisu_pars().getString("VisuCoreWordType");
			VisuCoreByteOrder = bruker.getJcampdx().getVisu_pars().getString("VisuCoreByteOrder");
		} catch (Exception e) {
			System.out.println("Parameters missing: VisuCoreByteOrder, or VisuCoreByteOrder");
		}
		try {
			VisuFGOrderDesc = bruker.getJcampdx().getVisu_pars().getArrayList("VisuFGOrderDesc");
		} catch (Exception e) {
			System.out.println("Parameters missing: VisuFGOrderDesc");
		}
		VisuFGOrderDescDim = bruker.getJcampdx().getVisu_pars().getInt("VisuFGOrderDescDim");
		VisuCoreDim = bruker.getJcampdx().getVisu_pars().getInt("VisuCoreDim");
		VisuCoreDimDesc = bruker.getJcampdx().getVisu_pars().getArrayList("VisuCoreDim");
		VisuCoreSize = bruker.getJcampdx().getVisu_pars().getINDArray("VisuCoreSize");
		VisuCoreFrameCount = bruker.getJcampdx().getVisu_pars().getInt("VisuCoreFrameCount");
		VisuCoreDataSlope = bruker.getJcampdx().getVisu_pars().getINDArray("VisuCoreDataSlope");
		VisuCoreDataOffs = bruker.getJcampdx().getVisu_pars().getINDArray("VisuCoreDataOffs");
	}

	private void getFidParams() {
		// TODO Auto-generated method stub
		PVM_EncMatrix = bruker.getJcampdx().getMethod().getINDArray("PVM_EncMatrix");
		PVM_EncNReceivers = bruker.getJcampdx().getMethod().getInt("PVM_EncNReceivers");
		PVM_EncTotalAccel =  bruker.getJcampdx().getMethod().getInt("PVM_EncTotalAccel");
		ACQ_phase_factor = bruker.getJcampdx().getAcqp().getInt("ACQ_phase_factor");
		ACQ_size = bruker.getJcampdx().getAcqp().getINDArray("ACQ_size");
		NR = bruker.getJcampdx().getAcqp().getInt("NR");
		NI = bruker.getJcampdx().getAcqp().getInt("NI");
		NSLICES = bruker.getJcampdx().getAcqp().getInt("NSLICES");
		ACQ_dim = bruker.getJcampdx().getAcqp().getInt("ACQ_dim");
		PVM_EncSteps1 = bruker.getJcampdx().getMethod().getINDArray("PVM_EncSteps1");
		try {
			PVM_ObjOrderList =  bruker.getJcampdx().getMethod().getINDArray("PVM_ObjOrderList").data().asInt();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			
		}
		PVM_SpecMatrix = bruker.getJcampdx().getMethod().getInt("PVM_SpecMatrix");
		NSegments = bruker.getJcampdx().getMethod().getInt("NSegments");
		ACQ_method = bruker.getJcampdx().getAcqp().getString("ACQ_method");
		GO_block_size = bruker.getJcampdx().getAcqp().getString("GO_block_size");
		PULPROG = bruker.getJcampdx().getAcqp().getString("PULPROG");
		PVM_DigNp = bruker.getJcampdx().getMethod().getInt("PVM_DigNp");
		try {
			NPro = bruker.getJcampdx().getMethod().getInt("NPro");
		} catch (Exception e) {
			// 0 or null??????
			NPro = 0;
		}
		try {
			NECHOES = bruker.getJcampdx().getAcqp().getInt("NECHOES");
		} catch (Exception e) {
			NECHOES = null;
		}
		try {
			CSISignalType = (String) bruker.getJcampdx().getMethod().getString("CSISignalType");
		} catch (Exception e) {
			CSISignalType = null;
		}
		try {
			PVM_EncNReceivers = bruker.getJcampdx().getMethod().getInt("PVM_EncNReceivers");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			PVM_EncNReceivers = 0;

		}
		try {

			PVM_EncTotalAccel = bruker.getJcampdx().getMethod().getInt("PVM_EncTotalAccel");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			PVM_EncTotalAccel = 0;
		}
		
		ACQ_dim_desc = bruker.getJcampdx().getAcqp().getArrayList("ACQ_dim_desc");
		numberOfPoint = bruker.getJcampdx().getMethod().getINDArray("PVM_SpecMatrix");
		PVM_Matrix = bruker.getJcampdx().getMethod().getINDArray("PVM_Matrix");
		
		iden_PV_version();
		
		if (PV_version.contains("360")) {
			try {
				String dataType = bruker.getJcampdx().getAcqp().getString("DTYPA");
				if (dataType.contentEquals("Double")) {
					Acqp_GO_raw_data_format = "GO_32BIT_DOUBLE";
				}
				Acqp_BYTORDA = bruker.getJcampdx().getAcqp().getString("BYTORDA");
			} catch (Exception e) {
				logger.error("Parameters missing: Acqp_BYTORDA, or Acqp_BYTORDA");
			}
		} else {
			try {
				Acqp_GO_raw_data_format = bruker.getJcampdx().getAcqp().getString("GO_raw_data_format");
				Acqp_BYTORDA = bruker.getJcampdx().getAcqp().getString("BYTORDA");
			} catch (Exception e) {
				logger.error("Parameters missing: Acqp_BYTORDA, or Acqp_BYTORDA");
			}
		}
		
		PVM_SpiralNbOfInterleaves = bruker.getJcampdx().getMethod().getInt("PVM_SpiralNbOfInterleaves");
		
	}

	private void iden_PV_version() {
			PV_version = (String) bruker.getJcampdx().getAcqp().getString("PV");
			if(PV_version == null) {
				if(bruker.getJcampdx().getAcqp().getString("ACQ_sw_version").contains("PV-360"))
					PV_version = "360";
			}
			if (Arrays.stream(PV_VERSIONS).anyMatch(PV_version::equals)) {
				logger.info("File {} is a supported PV version", PV_version);
			} else {
				logger.error("File {} is not a supported PV version", PV_version);
			}		
	} 
	
	
}

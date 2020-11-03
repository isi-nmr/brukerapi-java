package bruker_plugin_lib;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.sound.midi.Soundbank;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrukerConvertor {
	private static JSONObject test_data_meta;
	private String path;

	public BrukerConvertor(String path) {
		super();
		this.path = path;
		Scan scan = new Scan("D:/T drive/data/bruker/CSI/Mouse/28/");
	}

	public static void main(String[] args) throws EOFException, IOException {
		Logger logger = LoggerFactory.getLogger(BrukerConvertor.class);
		String fs_prefix = "D:\\DATA SETs\\";
		String json = "fid_schemes.json";
		JSONParser jsonParser = new JSONParser();
		try (FileReader reader = new FileReader(json)) {
			try {
				JSONObject jsonobject = (JSONObject) jsonParser.parse(reader);
//			test_data_meta = (JSONObject) jsonobject.get(dataset_id);			
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		

		


			
//		  ArrayList<int[]> mat = indices(new int[] {2, 3, 2, 1, 3}, 1, 4);
	
		
//		

//		  String dataset_path = fs_prefix + test_data_meta.get("path"); 
//		  String ACQS_TYPE = (String) test_data_meta.get("acq_type"); 
		  long startTime = System.nanoTime();
		  Bruker bruker = new Bruker(); 
		  
		  bruker.setPath(Paths.get("D:/DATA SETs/for test Jbruker/20200612_094625_lego_phantom_3_1_2/35/fid")); 
		  bruker.isImage(); // bruker.setACQS_TYPE("CSI");
		  DataBruker data = bruker.getData(); 
		  long endTime = System.nanoTime();
		  System.out.println((endTime - startTime)/1000000);
		  startTime = System.nanoTime();
		   double[] a = data.real.data().asDouble();
		  endTime = System.nanoTime();
		  System.out.println((endTime - startTime)/1000000);
		  startTime = System.nanoTime();
		  Object realdata = data.getRealData();
		  endTime = System.nanoTime();
		  System.out.println((endTime - startTime)/1000000);
		
		
		
		
//		  float[] imagdata = data.getImagData(); 
		  System.out.println("finished");
		 
//	bruker.getPath();
		/*
		 * JcampdxData acqp = bruker.getJcampdx().getAcqp(); boolean iskspace =
		 * acqp.isKspace(); float[] getPositionVoi =
		 * bruker.getJcampdx().getMethod().getPositionVoi(new float[] {1,2,3}); float[]
		 * getPositionRPS = bruker.getJcampdx().getMethod().getPositionRPS(new float[]
		 * {1,2,3}); float[][] getFloat3DMatrix =
		 * acqp.getFloat3DMatrix("ACQ_grad_matrix")[0]; float[] getFovVoi =
		 * bruker.getJcampdx().getMethod().getFovVoi(new float[] {1,2,3}); float[]
		 * getFov = acqp.getFov(new float[] {1,2,3}); float[] getFovDirect =
		 * bruker.getJcampdx().getFov(new float[] {1,2,3}); String neclus =
		 * bruker.getJcampdx().getMethod().getNucleus(); double resFr =
		 * bruker.getJcampdx().getMethod().getResonaceFreq((float)(127.728513)); float
		 * thick = acqp.getSliceThick(1); float TE =
		 * bruker.getJcampdx().getMethod().getTE(1); float[][]
		 * gradMatrix={{1,0,0},{0,1,0},{0,0,1}}; float[][] gradMat =
		 * acqp.getGradMatrix(0, gradMatrix); float[][] gradMatVoi =
		 * bruker.getJcampdx().getMethod().getGradMatrixVoi(0, gradMatrix);
		 */
//	bruker.getJcampdx().getMethod().getGradMatrix(dim, defGradMatrix)

//	DataBruker dataBruker = new DataBruker(data);
		logger.debug("succesfull");

		/*
		 * Reco reco = new Reco("D:\\DATA SETs\\Data for test spectIm\\7\\pdata\\1\\");
		 * //scan.initialize(); float[] real = reco.getRealData(0); float[] imag =
		 * reco.getImagData(0);
		 */

//	
		Scan scan = new Scan("D:\\DATA SETs\\Data for test spectIm\\20200122_101204_phantom_orientation_1_1\\7\\");
		// scan.initialize();
//	float[] real = scan.getRealData(0);
//	float[] imag = scan.getImagData(0);
////	//INDArray b = (INDArray) a[0];
////	////long startTime = System.nanoTime();
////	//double[][][] c = scan.toMatrix(b);
////	//long endTime = System.nanoTime();
////	//System.out.println((endTime - startTime)/1000000);
////	//double[][][] d = scan.toVector(b);
////	//long endTime1 = System.nanoTime();
////	//System.out.println((endTime1 - endTime)/1000000);
////	//long[] a = scan.getFid_dims();
////	System.out.println("finished");
////	//System.out.println(c==d);
////	 //NDArray FOV = scan.getAcqp("ACQ_fov");
////	 //double[][][] aout = scan.getDouble3DMatrix("ACQ_grad_matrix");
////	 Float out1 = scan.getFloat("FW");
////	  //float[] out12 = scan.getFloatVector("ACQ_fov");
////	 //String out2 = scan.getString("ACQ_scan_size");
////	 
////	 //float a = (float) scan.getAcqp("SFO1");
////	 //System.out.println(Float.valueOf(out1));
////  	 //double[] bout = aout.getDoubleVector();
////	 //scan.getData(); 
////	 System.out.println("finished");
////	Reco reco = new Reco("D:/DATA SETs/data from jana/data for oreintation/brukerImage/6/pdata/1/");
////	long[] a = reco.get_dims();
////	float[] real = reco.getRealData(0);
////	float[] imag = reco.getImagData(0);
//	//scan.getAcqp("ACQ_phase1_offset");
//	//Float f = reco.getFloat("ACQ_slice_thick");
////	String[] f = reco.getStringVector("RECO_qopts");
//	//double[][] fov = scan.getDoubleMatrix("PVM_VoxArrPosition");
//	//String PV = scan.getString("PV");
//	//Float PVM_EchoTime = scan.getFloat("ACQ_phase1_offset");
//	
////	int visufgo = reco.getInt("VisuFGOrderDescDim");
////	Float out1 = reco.getFloat("FW");
////	String out2 = reco.getString("ACQ_scan_size");
////	double[][][] aout = reco.getDouble3DMatrix("ACQ_grad_matrix");
//	//float out12 = reco.getFloat("PVM_EchoTime");
//	//float[] data = reco.getImagData(0);
//	//long[] dims = reco.get_dims();
//	
////	System.out.println(visufgo);
//    //System.out.println(PV);

	}
	/**
	 * jsbckjsdcj,msd ckjsdhcusnc scnsjlcsoudc
	 * @return result
	 */
	static int product(int ar[]) {
		int result = 1;
		for (int i = 0; i < ar.length; i++)
			result = result * ar[i];
		return result;
	}
	
	

}

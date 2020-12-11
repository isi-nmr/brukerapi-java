import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class Test {
	public static void main(String[] args) throws IOException {
		
		
		
//		test_pv5_fid();
//		System.out.println("pv5 fid=======================finished");
//		test_pv5_2dseq();
//		System.out.println("pv5 2dseq=======================finished");
//		test_pv601_fid();
//		System.out.println("pv601 fid=======================finished");
//		test_pv601_2dseq();
//		System.out.println("pv601 2dseq=======================finished");
		test_pv360_fid();
		System.out.println("pv360 fid=======================finished");
//		test_pv360_2dseq();
//		System.out.println("pv360 2dseq=======================finished");
//		
		

		
		


	}

	private static void test_pv360_2dseq() {
		// TODO Auto-generated method stub
		  Bruker bruker_pv360 = new Bruker();
		  String pathTemp_pv360 = "D:\\DATA SETs\\from jana\\Cristina\\20200310_093147_rat_10032020_2_rat_10032020_testFIDCSI_1_1_Jana\\27\\pdata\\1\\2dseq";
		  bruker_pv360.setPath(Paths.get(pathTemp_pv360)); 
		  DataBruker data_pv360 = bruker_pv360.getData();
		  float[] realdata = data_pv360.getRealData();

	}

	private static void test_pv360_fid() {
		// TODO Auto-generated method stub
		  Bruker bruker_pv360 = new Bruker();
		  // "D:\\DATA SETs\\from jana\\Cristina\\20200310_093147_rat_10032020_2_rat_10032020_testFIDCSI_1_1_Jana\\27\\fid",
		  String[] paths = new String[] {

				"D:\\DATA SETs\\from jana\\Paravison360\\Phantom__perfect__06052020_1_Jana\\15_metabolites\\fid"
		  };
		for (String path: paths
			 ) {
			bruker_pv360.setPath(Paths.get(path));
//			DataBruker data_pv360 = bruker_pv360.getData();
			System.out.println(bruker_pv360.getJcampdx().getAcqp().getINDArray("ACQ_abs_time"));
//			float[] realdata = data_pv360.getRealData();
//			Object imagedata = data_pv360.getImagData();
		}


	}

	private static void test_pv601_fid() throws IOException {
		// TODO Auto-generated method stub
		String fs_prefix_pv6 = "D:\\\\DATA SETs\\\\for test Jbruker\\\\";
		String json_pv6 = "test_config_pv601.json";
		JSONParser jsonParser_pv6 = new JSONParser();
		JSONObject jsonobject_pv6;
		String[] acceptable_acq_type_pv6 = new String[] { "CART_2D", "CART_3D", "CSI", "EPI", "SPECTROSCOPY" };
		try (FileReader reader_pv6 = new FileReader(json_pv6)) {
			try {
				jsonobject_pv6 = (JSONObject) jsonParser_pv6.parse(reader_pv6);
				JSONObject test_data_meta = (JSONObject) jsonobject_pv6.get("test_io");
				Set keys = test_data_meta.keySet();
				Iterator it = keys.iterator();
				while (it.hasNext()) {
					Bruker bruker = new Bruker();
					Object type = it.next();
					JSONObject rslt = (JSONObject) test_data_meta.get(type);

				if (type.toString().contains("FID_PRESS")) {

					Object acq_scheme = null;
					try {
						acq_scheme = rslt.get("acq_scheme");
						Object path = rslt.get("path");

						if (Arrays.stream(acceptable_acq_type_pv6).anyMatch(acq_scheme.toString()::equals)) {
							
							try {
								String pathTemp = fs_prefix_pv6 + path.toString();
								bruker.setPath(Paths.get(pathTemp));
								DataBruker data = bruker.getData();
								Object realdata = data.getRealData();
								Object imagedata = data.getImagData();
								
								System.out.println(type+ " : " + acq_scheme.toString() + " | " + 
										bruker.getConditions().getACQS_TYPE() + " : " + Arrays.toString(data.real.shape()));
							
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
//									System.out.println("out of readable data types");
						}
					} catch (Exception e) {
//						e.printStackTrace();
					}

			// end of type if
			}
			}
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void test_pv5_2dseq() throws IOException {
		// TODO Auto-generated method stub
		String fs_prefix_pv5 = "D:\\\\DATA SETs\\\\for test Jbruker\\\\0.2H2\\\\";
		String json_pv5 = "test_config_pv51.json";
		JSONParser jsonParser_pv5 = new JSONParser();
		JSONObject jsonobject_pv5;
		String[] acceptable_acq_type_pv5 = new String[] { "CART_2D", "CART_3D", "CSI", "EPI" };
		try (FileReader reader_pv5 = new FileReader(json_pv5)) {
			try {
				jsonobject_pv5 = (JSONObject) jsonParser_pv5.parse(reader_pv5);
				JSONObject test_data_meta = (JSONObject) jsonobject_pv5.get("test_io");
				Set keys = test_data_meta.keySet();
				Iterator it = keys.iterator();
				while (it.hasNext()) {
					Bruker bruker = new Bruker();
					Object type = it.next();
					JSONObject rslt = (JSONObject) test_data_meta.get(type);

					if (type.toString().contains("2DSEQ_")) {

						Object acq_scheme = null;
						try {
							acq_scheme = rslt.get("acq_scheme");
							Object path = rslt.get("path");

//							if (Arrays.stream(acceptable_acq_type_pv5).anyMatch(acq_scheme.toString()::equals)) {

								try {
									String pathTemp = fs_prefix_pv5 + path.toString();
									bruker.setPath(Paths.get(pathTemp));
									DataBruker data = bruker.getData();
									Object realdata = data.getRealData();
									
									System.out.println(type+ " | "  + Arrays.toString(data.real.shape()));
									
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
//							} else {
//								System.out.println("out of readable data types");
//							}
						} catch (Exception e) {
//						e.printStackTrace();
						}

						// end of type if
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void test_pv601_2dseq() throws IOException {
		// TODO Auto-generated method stub
		String fs_prefix_pv6 = "D:\\\\DATA SETs\\\\for test Jbruker\\\\";
		String json_pv6 = "test_config_pv601.json";
		JSONParser jsonParser_pv6 = new JSONParser();
		JSONObject jsonobject_pv6;
		String[] acceptable_acq_type_pv6 = new String[] { "CART_2D", "CART_3D", "CSI", "EPI" };
		try (FileReader reader_pv6 = new FileReader(json_pv6)) {
			try {
				jsonobject_pv6 = (JSONObject) jsonParser_pv6.parse(reader_pv6);
				JSONObject test_data_meta = (JSONObject) jsonobject_pv6.get("test_io");
				Set keys = test_data_meta.keySet();
				Iterator it = keys.iterator();
				while (it.hasNext()) {
					Bruker bruker = new Bruker();
					Object type = it.next();
					JSONObject rslt = (JSONObject) test_data_meta.get(type);

				if (type.toString().contains("2DSEQ_")) {

					Object acq_scheme = null;
					try {
						acq_scheme = rslt.get("acq_scheme");
						Object path = rslt.get("path");

//						if (Arrays.stream(acceptable_acq_type_pv6).anyMatch(acq_scheme.toString()::equals)) {
							
						try {
								String pathTemp = fs_prefix_pv6 + path.toString();
								bruker.setPath(Paths.get(pathTemp));
								DataBruker data = bruker.getData();
								Object realdata = data.getRealData();
								System.out.println(type+ " | "  + Arrays.toString(data.real.shape()));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
//						} else {
//									System.out.println("out of readable data types");
//						}
					} catch (Exception e) {
//						e.printStackTrace();
					}

			// end of type if
			}
			}
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void test_pv5_fid() throws IOException {
		
		String fs_prefix_pv5 = "D:\\\\DATA SETs\\\\for test Jbruker\\\\0.2H2\\\\";
		String json_pv5 = "test_config_pv51.json";
		JSONParser jsonParser_pv5 = new JSONParser();
		JSONObject jsonobject_pv5;
		String[] acceptable_acq_type_pv5 = new String[] { "CART_2D", "CART_3D", "CSI", "EPI" };
		try (FileReader reader_pv5 = new FileReader(json_pv5)) {
			try {
				jsonobject_pv5 = (JSONObject) jsonParser_pv5.parse(reader_pv5);
				JSONObject test_data_meta = (JSONObject) jsonobject_pv5.get("test_io");
				Set keys = test_data_meta.keySet();
				Iterator it = keys.iterator();
				while (it.hasNext()) {
					Bruker bruker = new Bruker();
					Object type = it.next();
					JSONObject rslt = (JSONObject) test_data_meta.get(type);

					if (type.toString().contains("FID_")) {

						Object acq_scheme = null;
						try {
							acq_scheme = rslt.get("acq_scheme");
							Object path = rslt.get("path");

							if (Arrays.stream(acceptable_acq_type_pv5).anyMatch(acq_scheme.toString()::equals)) {
								try {
									String pathTemp = fs_prefix_pv5 + path.toString();
									bruker.setPath(Paths.get(pathTemp));
									DataBruker data = bruker.getData();
									Object realdata = data.getRealData();
									Object imagedata = data.getImagData();
					
									System.out.println(type+ " : " + acq_scheme.toString() + " | " + 
											bruker.getConditions().getACQS_TYPE() + " : " + Arrays.toString(data.real.shape()));
									
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							} else {
//								System.out.println("out of readable data types");
							}
						} catch (Exception e) {
//						e.printStackTrace();
						}

						// end of type if
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}

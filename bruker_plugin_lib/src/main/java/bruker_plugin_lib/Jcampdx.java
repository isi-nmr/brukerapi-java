package bruker_plugin_lib;


import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Jcampdx {
	private JcampdxData acqp;
	private JcampdxData method;
	private JcampdxData reco;
	private JcampdxData visu_pars;
	private Logger logger = LoggerFactory.getLogger(Jcampdx.class);
	private List<Object> scan_result_2dseq;
	private List<Object> scan_result_fid;
	private Path path_fid;
	private Path path_2dseq;
	private Bruker bruker;
	
	
	
	public Path getPath_fid() {
		return path_fid;
	}

	public void setPath_fid(Path path_fid) {
		this.path_fid = path_fid;
	}

	public Path getPath_2dseq() {
		return path_2dseq;
	}

	public void setPath_2dseq(Path path_2dseq) {
		this.path_2dseq = path_2dseq;
	}

	/**
	 * get acqp file
	 *
	 * @return a map with String keys and Object value of accp
	 */
	public JcampdxData getAcqp() {
		if (CheckFileExist("acqp", scan_result_fid)) {
			if (acqp == null) {
				this.acqp = new JcampdxData(read_jcampdx_file(path_fid.toString() + "\\acqp"));
			}
		} else {
			logger.error("the {} file is missing", "acqp");
		}
		;
		return acqp;
	}

	public void setAcqp(JcampdxData acqp) {
		this.acqp = acqp;
	}

	/**
	 * get method file
	 *
	 * @return a map with String keys and Object value of method
	 */
	public JcampdxData getMethod() {
		if (CheckFileExist("method", scan_result_fid)) {
			if (method == null) {
				this.method = new JcampdxData(read_jcampdx_file(path_fid.toString() + "\\method"));
			}
		} else {
			logger.error("the {} file is missing", "method");
		}
		;
		return method;
	}

	public void setMethod(JcampdxData method) {
		this.method = method;
	}

	/**
	 * get reco file
	 *
	 * @return a map with String keys and Object value of reco
	 */
	public JcampdxData getReco() {
		if (CheckFileExist("reco", scan_result_2dseq)) {
			if (reco == null) {
				this.reco = new JcampdxData(read_jcampdx_file(path_2dseq.toString() + "\\reco"));
			}
		} else {
			logger.error("the {} file is missing", "reco");
		}
		;
		return reco;
	}

	public void setReco(JcampdxData reco) {
		this.reco = reco;
	}

	/**
	 * get visu_pars file
	 *
	 * @return a map with String keys and Object value of visu_pars
	 */
	public JcampdxData getVisu_pars() {
		if (CheckFileExist("visu_pars", scan_result_2dseq)) {
			if (visu_pars == null) {
				this.visu_pars = new JcampdxData(read_jcampdx_file(path_2dseq.toString() + "\\visu_pars"));
			}
		} else {
			logger.error("the {} file is missing", "visu_pars");
		}
		;
		return visu_pars;
	}

	public void setVisu_pars(JcampdxData visu_pars) {
		this.visu_pars = visu_pars;
	}

	/**
	 * constructor of Jcampdx Object
	 *
	 * @param bruker : path to the main directory
	 */
	public Jcampdx(Bruker bruker) {
		this.bruker = bruker;
		Path pathL = bruker.getConditions().getPath();
//		path_fid = path_2dseq.getParent().getParent();

		if (pathL.getFileName().toString().contentEquals("2dseq") || 
				pathL.getFileName().toString().contentEquals("1i") || pathL.getFileName().toString().contentEquals("1r")) {
			try {
				path_2dseq = pathL.getParent();
				path_fid = path_2dseq.getParent().getParent();
			} catch (Exception e) {
				logger.error("API cannot locate {} ", "path_2dseq");
			}
		} else if (pathL.getFileName().toString().contentEquals("fid")) {
			try {
				path_fid = pathL.getParent();
				Path temp_path = Paths.get("pdata\\1");
				path_2dseq = path_fid.resolve(temp_path);
			} catch (Exception e) {
				logger.error("API cannot locate {} ", "path_2dseq");
			}
		}

		scan_result_2dseq = list_scan_dir(path_2dseq);
		scan_result_fid = list_scan_dir(path_fid);

	}

	/**
	 * scan a list of all files in the path
	 *
	 * @param path the path of bruker data
	 * @return list of all files in path
	 */
	private List<Object> list_scan_dir(Path path) {
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
	 * Check specific file exists in list directory or not
	 *
	 * @param Filename
	 * @param list_scan_result
	 * @return boolean value 1:existed 0:not existed
	 */
	private boolean CheckFileExist(String Filename, List<Object> list_scan_result) {
		if (list_scan_result.contains(Filename)) {
			return true;
		} else {
			return false;
		}
	}

	public static Object proc_value_clean(String value_string) {
		try {
			return Integer.parseInt(value_string);
		} catch (Exception e) {
			try {
				return Float.valueOf(value_string);
			} catch (NumberFormatException e1) {
				return value_string.replaceAll("<", "").replaceAll(">", "");
			}
		}
	}

	private static Object proc_single_list(String value_string) {
		String[] sizes = value_string.split("\\)");
		String size0 = sizes[0].replace("(", "").trim();
		String[] S_size_list = size0.split(",");
		INDArray array = Nd4j.zeros(S_size_list.length);
		for (String element: S_size_list
			 ) {
			array.add(Double.valueOf(element));
		}
		return array;
	}

	public static Object proc_array(String value_string) {
		String[] sizes = value_string.split("\\)");
		String size0 = sizes[0].replace("(", "").trim();
		String[] S_size_list = size0.split(",");
		int[] I_size_list = new int[S_size_list.length];
		for (int i = 0; i < S_size_list.length; i++) {
			I_size_list[i] = Integer.parseInt(S_size_list[i].trim());
		}
		if (value_string.contains("<")) {
			I_size_list[I_size_list.length - 1] = 1;
		}
		String data_string = value_string.substring(value_string.indexOf(")") + 1).trim().replaceAll("  ", " ");
		String[] data_List;
		if (data_string.contains("<")) {
			data_string = data_string.replace("<", "").replace(" ", "");
			data_List = data_string.split(">");
			data_List = Arrays.copyOfRange(data_List, 0, I_size_list.length);
		} else {
			data_List = data_string.split(" ");
		}
		String format_;
		INDArray value_float = null;
		List<String> value_str = new ArrayList<String>();
		try {
			Float.parseFloat(data_List[0]);
			format_ = "float";
			long rslt = 1;
			for (int idx_size = 0; idx_size < I_size_list.length; idx_size++) {
				rslt = I_size_list[idx_size] * rslt;
			}
			value_float = Nd4j.zeros(1, rslt);
		} catch (Exception e) {
			format_ = "String";
		}
		for (int i = 0; i < data_List.length; i++) {
			if (format_ == "float") {
				try {
					value_float.putScalar(i, Float.valueOf(data_List[i]));
				} catch (NumberFormatException e) {
					System.out.println("unexpected format");
				}
			} else {
				value_str.add(data_List[i]);
			}
		}
		if (format_ == "float") {
			if (data_List.length > 1) {
				value_float = value_float.reshape(I_size_list);
				return value_float;
			} else {
				return value_float;
			}
		} else {
			if (I_size_list[0] > 1) {
				return value_str;
			} else {
				return value_str.get(0);
			}
		}

	}

	public static Object proc_nested_list(String value_string) {
		ArrayList values = new ArrayList<Object>();
		String sizes = value_string.split("\\)")[0].replace("(", "").trim().replaceAll(" ", "");
		String data_string = value_string.substring(value_string.indexOf(")") + 1).trim();
		String[] S_size_list = sizes.split(",");
		if (!data_string.isEmpty()) {
			int[] I_size_list = new int[S_size_list.length];
		for (int i = 0; i < S_size_list.length; i++) {
			try {
				I_size_list[i] = Integer.valueOf(S_size_list[i]);
			} catch (NumberFormatException e) {
				I_size_list[i] = 0;
			}
		}

		if (value_string.startsWith("<")) {
			I_size_list[I_size_list.length] = 1;
		}
		data_string = data_string.replaceAll(" ", "");

		for (int i = 0; i < I_size_list[0]; i++) {
			int position = data_string.indexOf(")(");
			String data_frac = null;
			if (!data_string.contentEquals("")) {
				if (position > 0) {
					data_frac = data_string.substring(1, position);
				} else {
					data_frac = data_string.substring(1, data_string.length() - 1);
				}
				data_frac = data_frac.replaceAll(" ", "");
				String[] value = data_frac.split(",");
				Object[] obj_value = new Object[value.length];
				for (int j = 0; j < value.length; j++) {
					obj_value[j] = proc_value_clean(value[j]);
				}

				values.add(obj_value);
				data_string = data_string.substring(position + 1);
				data_string = data_string.replaceFirst("\\s++$", "");
			}
		}
	} else
	{

		for (String element: S_size_list
		) {

			try {
				(values).add(Double.valueOf(element));
			} catch (Exception e) {
				(values).add(element);
			}
		}

	}
		return values;
	}

	public static Object proc_entry(String value_string) {
		if (!value_string.contains("(")) {
			return proc_value_clean(value_string);
		} else if (value_string.startsWith("(") && !value_string.endsWith(")")) {
			return proc_array(value_string);
		} else if (value_string.startsWith("(") && value_string.endsWith(")")) {
			if (value_string.endsWith(" )")) {
				return proc_single_list(value_string);
			} else {
				return proc_nested_list(value_string);
			}
		} else {
			System.out.println("parsing of a parameter was not done well ");
		}
		return value_string;

	}



	/**
	 * read X jcmpdx file
	 *
	 * @param filename : the path to desire directory (an example:
	 *                 path_fid.toString() + "\\acqp")
	 * @return a map of keys and values
	 */
	public static Map<String, Object> read_jcampdx_file(String filename) {
		String thisLine = null;
		String temp = "";
		String tempdollar = "";
		boolean rslt;
		Map<String, Object> aMap = new HashMap<String, Object>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			while ((thisLine = br.readLine()) != null) {
				if (thisLine.contains("$$")) {
					tempdollar = tempdollar + thisLine + " ";
					if (thisLine.contains("/opt/PV")) {
						String version = thisLine.split("/")[2];
						aMap.put("PV", version.substring(2));
					}
				}
				if (!thisLine.contains("$$")) {
					temp = temp + thisLine + " ";
				}
			}

			String[] splited = temp.split("##");
			for (String s : splited) {
				if (s.startsWith("$")) {
					s = s.replace("$", "");
					String a, b;
					String[] splited2 = s.split("=");
					a = splited2[0];
					b = splited2[1];
					aMap.put(a, proc_entry(b.replaceFirst("\\s++$", "")));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return aMap;
	}

	public float[] getFov(float[] fov) {
		float[] FOV = null;
		String[] dic = { "PVM_FovCm", "RECO_fov", "ACQ_fov" };
		FOV = getMethod().getINDArray(dic[0]).data().asFloat();
		if (bruker.isRaw()) {
			FOV = getAcqp().getINDArray(dic[2]).data().asFloat();
		} else if (!bruker.isRaw()) {
			FOV = getReco().getINDArray(dic[1]).data().asFloat();
		}
		if (FOV == null) {
			FOV = fov;
			logger.error("Problem to load FOV. Default value used: {}", fov);
		}
		// why???
		for (int i = 0; i < FOV.length; i++)
			FOV[i] *= 10;
		return FOV;
	}

	public float[][] getGradMatrix(int dim, float[][] gradMatrix) {
		return getAcqp().getGradMatrix(dim, gradMatrix);
	}

	public float[][] getGradMatrixVoi(int dim, float[][] gradMatrix) {
		return getMethod().getGradMatrixVoi(dim, gradMatrix);
	}

	public float[] getPositionRPS(float[] pos) {
		return getMethod().getPositionRPS(pos);
	}

	public float[] getPositionVoi(float[] defValue) {
		return getMethod().getPositionVoi(defValue);
	}

	public float[] getFovVoi(float[] fov) {
		return getMethod().getFovVoi(fov);
	}

	public float getSliceThick(float def) {
		return getAcqp().getSliceThick(def);
	}

	public String getNucleus() {
		return getMethod().getNucleus();
	}

	public double getSW(float defvalue) {
		return getAcqp().getSW(defvalue);
	}

	public String[] getRecoQOPTS(String reco_qopts) {
		return getReco().getStringVector("RECO_qopts");
	}

	public float getGRPDLY(String grpdly, int i) {
		return getAcqp().getFloat("GRPDLY", 0);
	}

	public Object getResonaceFreq(float v) {
		return getAcqp().getResonaceFreq((float)(127.728513))*1e6;
	}

	public double getTE(int i) {
		return getMethod().getTE(i);
	}

	public double getTR(int i) {
		return getMethod().getTR(i);
	}
}

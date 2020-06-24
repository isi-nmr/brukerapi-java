package bruker_plugin_lib;

public interface getData {
	public int get_dim();
	public int[] get_size();
	public float[] get_dataFloatReal();
	public float[] get_dataFloatImag();
	public double[] get_dataDoubleReal();
	public double[] get_dataDoubleImag();
	public float[] get_dataFloatReal_vector();
	public float[] get_dataFloatImag_vector();
	public String[] get_dimensionNames()
	public float[] get_fov();
	public float get_sw();
	public double get_transmitterFreq();
	public float[][][] get_rotMatrix();
}


**JBruker (JAVA API for Bruker Datasets)**  
================================================
*A JAVA package providing I/O interface for Bruker data sets*  
>download library from maven:https://search.maven.org/remotecontent?filepath=com/github/isi-nmr/JBruker/1.0.0/JBruker-1.0.0.jar  
>javadoc : https://javadoc.io/doc/com.github.isi-nmr/JBruker/1.0.1  

(c) 2020 Ústav přístrojové techniky AV ČR, v. v. i. 

Bruker API is a multi-language package for providing input and output interface for Bruker data sets. The API is available in Python, JAVA, and Matlab. The JAVA version, so-called JBruker, is an open-source library for handling datasets.  
The purposes of developing the JAVA version are illuminated as follows: First, the main purpose is not using the library as a standalone software but rather to use as a third-party library in other well-developed standalone MRI and NMR(MRS) data processing software. Second, the power of Java cross-platform programming enables the API to run on most or all systems.  

**Features:**  
JAVA version:	8  
GPU Support:	Yes  
Cross-Platform:	Yes  
GUI:	No  
Logging:	Yes  


ParaVision Compatibility:	ParaVision v5.1, 
ParaVision v6.0.1, 
ParaVision v360  
Pulse Sequences Compatibility:	FLASH.ppg
MGE.ppg
MSME.ppg
RARE.ppg
FAIR_RARE.ppg
RAREVTR.ppg
RAREst.ppg
MDEFT.ppg
FISP.ppg
FLOWMAP.ppg
DtiStandard.ppg
EPI.ppg
FAIR_EPI.ppg
DtiEpi.ppg
T1_EPI.ppg
T2_EPI.ppg
T2S_EPI.ppg
SPIRAL.ppg
DtiSpiral.ppg
UTE.ppg
UTE3D.ppg
ZTE.ppg
CSI.ppg
FieldMap.ppg
NSPECT.ppg
PRESS.ppg
STEAM.ppg
igFLASH.ppg  

File Compatibility:	fid, ser, 
2dseq

**Installation:** 
The library source code is available in the GitHub repository of the NMR group of the Institute of Scientific Instruments of the Czech Academy of Sciences (https://github.com/isi-nmr/brukerapi-java). This project is open-source, and we would be happy if other developers want to contribute to this project. In this case, they must import from Git (in Eclipse: File -> Import -> Git -> Project from Git).  
In addition to it, the Jar file can be downloaded from here. In case the project is a maven project, the following lines must add to the POM file:  

    <!-- https://mvnrepository.com/artifact/com.github.isi-nmr/JBruker -->
    <dependency>
        <groupId>com.github.isi-nmr</groupId>
       <artifactId>JBruker</artifactId>
        <version>1.0.0</version>
    </dependency>

Alternatively, you can find it in maven central repositories (https://mvnrepository.com/ , https://search.maven.org/) by searching “JBruker”.
 
 


Examples:
The first step is instantiating a Bruker Class.  

    Bruker bruker = new Bruker();  

This is a simple constructor without any argument. The next step is defining a path to the desired dataset.  

    bruker.setPath(Paths.get("D:\\DATA SETs\\CSI\\Mouse\\28\\fid"));   

it is worth bearing in mind that the input of this method is not String type, but it is Path type.  
The third step is getting data. If the developer is sure about the type of acquisition, they can set it with this line of code:  

    bruker.setACQS_TYPE("CSI");

but in another case, the getData() method will identify the type of acquisition, automatically.

    DataBruker data = bruker.getData();

It should be noted that the getData() method returns DataBruker Type.
In addition to getting image or spectroscopy data, It is possible to access the other parameters files such as method, acqp, visu_pars, and reco . For example, to access the acqp file in the above mentioned dataset, the developer can get the Jcampdx instance via getJcampdx() method, then get acqp parameters using getAcqp. in addition, The getMethod, getVisu_pars, and getReco are for method, visu_pars, and reco files, respectively.  

    JcampdxData acqp = bruker.getJcampdx().getAcqp();

In the following there are some examples of getting parameters:  
	

    float[] getPositionVoi = bruker.getJcampdx().getMethod().getPositionVoi(new float[] {1,2,3});
    	float[] getPositionRPS = bruker.getJcampdx().getMethod().getPositionRPS(new float[] {1,2,3});
    	float[][] getFloat3DMatrix = acqp.getFloat3DMatrix("ACQ_grad_matrix")[0];
    	float[] getFovVoi = bruker.getJcampdx().getMethod().getFovVoi(new float[] {1,2,3});
    	float[] getFov = acqp.getFov(new float[] {1,2,3});
    	String neclus = bruker.getJcampdx().getMethod().getNucleus();
    	double resFr = bruker.getJcampdx().getMethod().getResonaceFreq((float)(127.728513));
    	float thick = acqp.getSliceThick(1);
    	float TE = bruker.getJcampdx().getMethod().getTE(1);
    	float[][] gradMatrix={{1,0,0},{0,1,0},{0,0,1}};  
    	float[][] gradMat = acqp.getGradMatrix(0, gradMatrix);
    	float[][] gradMatVoi = bruker.getJcampdx().getMethod().getGradMatrixVoi(0, gradMatrix);

For further information, see the JAVA document of the library. 

## Development of Java code was supported by:
  
- European Union's Horizon 2020 research and innovation program under the Marie Sklodowska-Curie grant agreement No 813120 (INSPiRE-MED)


Python, Matlab by

- Ministry of Education, Youth and Sports of the Czech Republic CZ.02.1.01/0.0/0.0/16_013/0001775

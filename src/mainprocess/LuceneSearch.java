package mainprocess;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import sourcecode.CodeDataProcessor;
import sourcecode.CodeFeatureExtractor;
import sourcecode.SourceCodeCorpus;
import utils.FileUtils;
import utils.MatrixUtil;
import Jama.Matrix;
import bug.BugDataProcessor;
import bug.BugFeatureExtractor;
import bug.BugRecord;
import config.Config;
import eval.FileEvaluate;
import eval.MAP;
import eval.MRR;
import eval.TopK;
import feature.CodeLength;
import feature.LuceneSimiScore;
import feature.LuceneVectorCreator;
import feature.RevisedVSMScore;
import feature.SimiScore;
import feature.VSMScore;
import feature.VectorCreator;

public class LuceneSearch {
	public static void run() throws Exception{
		String bugCorpusDirPath=Paths.get(Config.getInstance().getIntermediateDir(), "bug").toString();
		Config.getInstance().setBugCorpusDir(bugCorpusDirPath);
		BugDataProcessor.indexBugCorpus(BugDataProcessor.importFromXML());
		
		String codeCorpusDirPath=Paths.get(Config.getInstance().getIntermediateDir(), "code").toString();
		Config.getInstance().setCodeCorpusDir(codeCorpusDirPath);
		SourceCodeCorpus corpus=CodeDataProcessor.extractCodeData();
		CodeDataProcessor.indexCodeData(corpus);
		
		String codeLengthFilePath=Paths.get(Config.getInstance().getIntermediateDir(), "codelength").toString();
		CodeLength.generate(corpus, codeLengthFilePath);
//		CodeDataProcessor.exportCodeData(CodeDataProcessor.extractCodeData());
	
		
		String simMatFilePath=Paths.get(Config.getInstance().getIntermediateDir(), "ElementScore").toString();
		LuceneSimiScore.generate(bugCorpusDirPath, codeCorpusDirPath, simMatFilePath);
		
//		String revisedSimMatFilePath=Paths.get(Config.getInstance().getIntermediateDir(), "RevisedVSMScore").toString();
//		RevisedVSMScore.generate(simMatFilePath, codeLengthFilePath,revisedSimMatFilePath);
		
//		String simiScoreMatFilePath = Paths.get(Config.getInstance().getIntermediateDir(), "SimiScore").toString();
//		SimiScore.generate(codeVecFilePath, bugVecFilePath, bugList, scoreMatFilePath);
		String fixedFilePath=Paths.get(bugCorpusDirPath, "fixedFiles").toString();
		if(Config.getInstance().getMRRUsed()){
			MRR mrr=new MRR();
			mrr.set(BugFeatureExtractor.extractFixedFiles(fixedFilePath));
			FileUtils.write_append2file("MRR"+"\t"+mrr.evaluate(simMatFilePath)+"\n", Config.getInstance().getOutputFile());
		}
				
		if(Config.getInstance().getMAPUsed()){
			MAP map=new MAP();
			map.set(BugFeatureExtractor.extractFixedFiles(fixedFilePath));
			FileUtils.write_append2file("MAP"+"\t"+map.evaluate(simMatFilePath)+"\n", Config.getInstance().getOutputFile());
		}
				
		if(Config.getInstance().getTopKUsed()){
			TopK topK=new TopK(Config.getInstance().getK());
			topK.set(BugFeatureExtractor.extractFixedFiles(fixedFilePath));
			FileUtils.write_append2file("TopK"+"\t"+topK.evaluate(simMatFilePath)+"\n", Config.getInstance().getOutputFile());
		}			

	}
//	public static String getPackage(ArrayList<String> file)
	public static void main(String []args) throws Exception{
		String rootDirPath="C:/Users/ql29/Documents/EClipse";
		String configFilePath=Paths.get(rootDirPath, "property").toString();
		String datasetsDirPath=Paths.get(rootDirPath,"SeekChanges","ApacheDatasets").toString();
		String intermediateDirPath=Paths.get(rootDirPath, "Corpus").toString();
		if(!new File(intermediateDirPath).isDirectory()){
			new File(intermediateDirPath).mkdir();
		}
		String outputFilePath=Paths.get(rootDirPath, "Evaluation", "output").toString();
		if(new File(outputFilePath).isFile()){
			new File(outputFilePath).delete();
		}
		String evaluationDirPath=Paths.get(rootDirPath, "Evaluation", "eval").toString();
		String projectName="swt";
		String datasetDirPath="";
		String bugLogFilePath="";
		
		if(projectName=="myfaces"){
			datasetDirPath=Paths.get(datasetsDirPath,"MYFACES-2.0.1", "myfaces-2.0.0").toString();
			bugLogFilePath=Paths.get(datasetsDirPath, "MYFACES-2.0.1", "All_Repository.xml").toString();
			Config.getInstance().setPaths(datasetDirPath, bugLogFilePath, intermediateDirPath, outputFilePath);
			Config.getInstance().setEvaluations(evaluationDirPath, true, true, 5, true);
			Config.getInstance().exportConfig(configFilePath);
			run();
		}
		else if(projectName=="pig"){
			datasetDirPath=Paths.get(datasetsDirPath,"PIG-0.9.0", "pig-0.8.1").toString();
			bugLogFilePath=Paths.get(datasetsDirPath, "PIG-0.9.0", "All_Repository.xml").toString();
			Config.getInstance().setPaths(datasetDirPath, bugLogFilePath, intermediateDirPath, outputFilePath);
			Config.getInstance().setEvaluations(evaluationDirPath, true, true, 5, true);
			Config.getInstance().exportConfig(configFilePath);
			run();
		}
		else if(projectName=="wicket"){
			datasetDirPath=Paths.get(datasetsDirPath,"WICKET-6.1.0", "wicket-6.0.0-beta3").toString();
			bugLogFilePath=Paths.get(datasetsDirPath, "WICKET-6.1.0", "All_Repository.xml").toString();
		}
		else if(projectName=="zookeeper"){
			datasetDirPath=Paths.get(datasetsDirPath,"ZOOKEEPER-3.4.0", "zookeeper-3.3.6").toString();
			bugLogFilePath=Paths.get(datasetsDirPath, "ZOOKEEPER-3.4.0", "All_Repository.xml").toString();
		}
		else if(projectName=="jedit"){
			datasetDirPath=Paths.get(datasetsDirPath,"JEdit4.3", "src").toString();
			bugLogFilePath=Paths.get(datasetsDirPath, "JEdit4.3", "JEdit_4.3.xml").toString();
		}
		else if(projectName=="ArgoUML0.22"){
			datasetDirPath=Paths.get(datasetsDirPath,"ArgoUML0.22", "src").toString();
			bugLogFilePath=Paths.get(datasetsDirPath,"ArgoUML0.22","ArgoUML_0.22.xml").toString();
		}
		else if(projectName=="ArgoUML0.24"){
			datasetDirPath=Paths.get(datasetsDirPath,"ArgoUML0.24", "src").toString();
			bugLogFilePath=Paths.get(datasetsDirPath,"ArgoUML0.24","ArgoUML_0.24.xml").toString();
		}
		else if(projectName=="ArgoUML0.26.2"){
			datasetDirPath=Paths.get(datasetsDirPath,"ArgoUML0.26.2", "src").toString();
			bugLogFilePath=Paths.get(datasetsDirPath,"ArgoUML0.26.2","ArgoUML_0.26.2.xml").toString();
		}
		else{
			System.out.println("The project name is invalid");
			return;
		}
		
		if(!new File(evaluationDirPath).isDirectory()){
			new File(evaluationDirPath).mkdir();
		}
		Config.getInstance().setPaths(datasetDirPath, bugLogFilePath, intermediateDirPath, outputFilePath);
		Config.getInstance().setEvaluations(evaluationDirPath, true, true, 5, true);
		Config.getInstance().exportConfig(configFilePath);
		run();
	}

}

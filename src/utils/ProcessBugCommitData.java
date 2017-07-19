package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import bug.BugDataProcessor;
import bug.BugRecord;

public class ProcessBugCommitData {

	public static HashMap<String,HashMap<String,ArrayList<String>>> ExtractOracles(String targetFilePath) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(targetFilePath));
		String line="";
		HashMap<String,HashMap<String,ArrayList<String>>> bugOracles=new HashMap<String, HashMap<String, ArrayList<String>>>();
		String bugID= null;
		HashMap<String, ArrayList<String>> commitFileMap = new HashMap<String, ArrayList<String>>();
		String commitID = null;
		ArrayList<String> commitFiles = new ArrayList<String> ();
		while((line=reader.readLine())!=null){
			String []strs=line.split("\t");

//			System.out.println(strs.length+"\t"+line.split(" ").length);
			if((strs.length<3) && (line.split(" ").length==1)){
				if(commitID!=null && !commitFileMap.containsKey(commitID)){
					commitFileMap.put(commitID, commitFiles);
				}
				if(bugID!=null && !bugOracles.containsKey(bugID)){
//					System.out.println(commitFileMap.size());
					bugOracles.put(bugID, commitFileMap);	
				}
				commitFileMap = new HashMap<String, ArrayList<String>>();
				commitFiles = new ArrayList<String> ();
				bugID = line.split(" ")[0];
				commitID = null;
			}
			else if((strs.length<3) && (line.split(" ").length==2)){
				if(commitID!=null && !commitFileMap.containsKey(commitID)){
					commitFileMap.put(commitID, commitFiles);
				}
				commitFiles = new ArrayList<String> ();
				commitID = line.split(" ")[1];
			}
			else if(strs.length==3) {
				System.out.println(line);
				if(strs[2].endsWith(".java")){
					if(strs[2].lastIndexOf("org")!=-1){
						commitFiles.add(strs[1]+";"+strs[2].substring(strs[2].lastIndexOf("org"), strs[2].length()).replace("/", "."));
					}
				}
				
			}
		}
		reader.close();
		return bugOracles;
	}
	public static ArrayList<BugRecord> merge(HashMap<String,HashMap<String,ArrayList<String>>> fullOracles, String oracleDirPath) throws Exception{
		
		ArrayList<BugRecord> bugCorpus = new ArrayList<BugRecord> ();
		HashMap<String, ArrayList<String>> issueCommitMaps = new HashMap<String, ArrayList<String>> ();
		String issueCommitMapsPath = Paths.get(oracleDirPath, "IssuesToSVNCommitsMapping.txt").toString();
		BufferedReader reader = new BufferedReader(new FileReader(issueCommitMapsPath));
		String line;
		while((line = reader.readLine())!=null){
			String [] strs = line.split("\t");
			String issueID = strs[0];
			ArrayList<String> commitList = new ArrayList<String>();
			int numCommits = Integer.parseInt(strs[1]);
			
			for(int i=2;i<2+numCommits;i++){
				commitList.add(strs[i]);
			}
			issueCommitMaps.put(issueID, commitList);
		}
		reader.close();
		
		
//		String goldSetDirPath = Paths.get(oracleDirPath, "GoldSets").toString();
		String issueQueryDirPath = Paths.get(oracleDirPath, "Queries").toString();
		for(String issueID: issueCommitMaps.keySet()){
//			String issueGoldSetFilePath = Paths.get(goldSetDirPath,"GoldSet"+issueID+".txt").toString();
//			ArrayList<String> goldSet = getStringArrayFromFile(issueGoldSetFilePath);
//			ArrayList<String> fullClassNameSet = new ArrayList<String> ();
//			for(String oneFile: goldSet){
//				String fullClassName = "";
//				for(String str: oneFile.split("\\.")){
//					if(str.toLowerCase().equals(str)){
//						fullClassName+=str+".";
//					}
//					else{
//						fullClassName+=str+".java";
//						break;
//					}
//				}
//				fullClassNameSet.add(fullClassName);
////				System.out.println(fullClassName);
//			}	
//			HashMap<String, ArrayList<String>> goldSetList = fullOracles.get(issueID);
//			int maxMatch = 0;
//			String matchedCommit = null;
//			System.out.println("-------------");
//			for(Entry<String, ArrayList<String>> commitFileList: goldSetList.entrySet()){
//				int match = 0;
//				System.out.println(commitFileList.getKey());
//				for(String str: commitFileList.getValue()){
//					String fullClassName = str.split(";")[1];
//					System.out.println(fullClassName);
//					if(fullClassNameSet.contains(fullClassName)){
//						match++;
//					}
//				}
//				if(match>maxMatch){
//					maxMatch = match;
//					matchedCommit = commitFileList.getKey();
//				}
//			}
//			ArrayList<String> finalGoldenSet = goldSetList.get(matchedCommit);
			String issueInformationFilePath = Paths.get(issueQueryDirPath,"Query"+issueID+".shortLongDescription").toString();
			if(!new File(issueInformationFilePath).isFile()){
				continue;
			}
			ArrayList<String> commitList = issueCommitMaps.get(issueID);
			if(!fullOracles.containsKey(issueID)){
				continue;
			}
			HashMap<String, ArrayList<String>> commitFileList = fullOracles.get(issueID);
			
			BufferedReader r = new BufferedReader(new FileReader(issueInformationFilePath));
			String issueSummary = r.readLine();
			String l="";
			String issueDescription = "";
			while((l = r.readLine())!=null){
				issueDescription +=l;
			}
			r.close();
			
			BugRecord bug = new BugRecord();
			bug.setBugId(issueID);
			bug.setBugDescription(issueDescription);
			bug.setBugSummary(issueSummary);
			for(String oneCommit : commitList){
				System.out.println(oneCommit);
				if(!commitFileList.containsKey(oneCommit)){
					System.out.println("issue #"+issueID+" does not have relevant commit #"+oneCommit+" in the full oracle.");
					
				}
				else if(commitFileList.size()==0){
					System.out.println("commit #"+ oneCommit+ "has no touched java files.");
				}
				else for(String oneFile : commitFileList.get(oneCommit)){
					bug.addFixedFile(oneFile);
				}
			}
			bugCorpus.add(bug);
		}
		return bugCorpus;
		
	}
	
	public static ArrayList<String> getStringArrayFromFile(String targetFilePath) throws IOException{
		BufferedReader reader = new BufferedReader (new FileReader(targetFilePath));
		String line="";
		ArrayList<String> stringList = new ArrayList<String> ();
		while((line= reader.readLine())!=null){
			stringList.add(line);
		}
		reader.close();
		return stringList;
	}
	public static void mergeLongShortDescriptions(String targetDirPath, String issueListPath) throws Exception{
		ArrayList<String> issueArray = getStringArrayFromFile(issueListPath);
		for(String oneIssue: issueArray){
			String shortDescriptionPath = Paths.get(targetDirPath, "ShortDescription"+oneIssue+".txt").toString();
			StringBuffer buf = new StringBuffer();
			BufferedReader r = new BufferedReader(new FileReader(shortDescriptionPath));
		
			String l="";
			while((l = r.readLine())!=null){
				buf.append(l);
			}
			r.close();
			buf.append("\n");
			String longDescriptionPath = Paths.get(targetDirPath, "LongDescription"+oneIssue+".txt").toString();
			
			r = new BufferedReader(new FileReader(longDescriptionPath));
		
			while((l = r.readLine())!=null){
				buf.append(l);
			}
			r.close();
			String shortLongDescriptionPath = Paths.get(targetDirPath, "Query"+oneIssue+".shortLongDescription").toString();
			FileWriter writer =new FileWriter(shortLongDescriptionPath);
			writer.write(buf.toString());
			writer.close();
		}
	}
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String filePath = "C:/Users/John/Documents/EClipse/SeekChanges/ApacheDatasets/JEdit4.3/JEdit-4.3.merged.log";
		HashMap<String,HashMap<String,ArrayList<String>>> bugOracles=ExtractOracles(filePath);		
		System.out.println(bugOracles.size());
		String oracleDirPath = "C:/Users/John/Documents/EClipse/SeekChanges/ApacheDatasets/JEdit4.3";
		ArrayList<BugRecord> bugCorpus = merge(bugOracles,oracleDirPath);
		String XMLFilePath = "C:/Users/John/Documents/EClipse/test.xml";
		BugDataProcessor.exportToXML(bugCorpus, XMLFilePath);
		
//		String issueListPath = "C:/Users/John/Documents/EClipse/SeekChanges/ApacheDatasets/JEdit4.3/listOfAllBugsFeaturesPatches.txt";
//		String targetDirPath = "C:/Users/John/Documents/EClipse/SeekChanges/ApacheDatasets/JEdit4.3/Queries";
//		mergeLongShortDescriptions(targetDirPath, issueListPath);
		
		
		
		
		
		
		
		
		
//		for(Entry<String, HashMap<String, ArrayList<String>>> pair:bugOracles.entrySet()){
//			System.out.println("bugID:"+pair.getKey());
//			HashMap<String, ArrayList<String>> commitMap = pair.getValue();
//			for(Entry<String, ArrayList<String>> pairs: commitMap.entrySet()){
//				System.out.println(pairs.getKey());
//				for(String str: pairs.getValue()){
//					System.out.println(str);
//				}
//
//			}
//		}

	}
	

}


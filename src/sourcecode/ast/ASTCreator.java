package sourcecode.ast;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class ASTCreator {

	private String content = null;

	public void getFileContent(File file) {
		this.getFileContent(file.getAbsolutePath());
	}

	//获取文件内容
	public void getFileContent(String absoluteFilePath) {
		try {
			StringBuffer contentBuffer = new StringBuffer();
			String line = null;
			BufferedReader reader = new BufferedReader(new FileReader(
					absoluteFilePath));
			while ((line = reader.readLine()) != null)
				contentBuffer.append(line+"\r\n");
			content = contentBuffer.toString();
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	//获取文件对应的CompilationUnit
	public CompilationUnit getCompilationUnit() {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(content.toCharArray());
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		return cu;
	}
	public static void main(String []args){
		String filePath="C:/Users/dell/git/mct_txl_nicad/mct_txl_nicad/datasets/ArgoUML-0.22/ArgoUML-0.22/argouml/src/model/src/org/argouml/model/AbstractActivityGraphsHelperDecorator.java";
		ASTCreator creator=new ASTCreator();
		File f=new File(filePath);
		creator.getFileContent(f);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		if(f.exists()){
			System.out.println(creator.content);
			parser.setSource(creator.content.toCharArray());
			CompilationUnit cu = (CompilationUnit)parser.createAST(null);
		}
		
		
	}
}

package edu.handong.csee.pp1.dp;

import java.io.File;
import java.util.ArrayList;

import edu.handong.csee.pp1.dp.utils.JavaASTParser;
import edu.handong.csee.pp1.dp.utils.Utils;
import edu.handong.csee.pp1.dp.utils.WekaUtils;
import weka.core.Instances;

public class MetricCollector {
	
	String gitPath;
	String arffFilePathToBeSaved;
	ArrayList<File> javaFiles;
	ArrayList<RawInstance> rawInstances;

	public MetricCollector(String gitPath,String arffFilePathToBeSaved) {
		this.gitPath = gitPath;
		this.arffFilePathToBeSaved = arffFilePathToBeSaved;
	}

	public void collect() {
		
		if(Main.INFO)
			System.out.println("Get a list of Java files");
		// git path안에 있는 모든 Java파일 목록 가져오기.
		javaFiles = Utils.getJavaFiles(gitPath);
		
		if(Main.INFO)
			System.out.println(javaFiles.size() + " java files were found. Instances with metrics will be generated");
		
		rawInstances = new ArrayList<RawInstance>(); // Instancew저장할 수 있는 ArrayList 메모리에 생성.
		
		// 각 Java파일에 있는 코드에서 메트릭을 뽑아내기.
		collectMetrics();
		if(Main.INFO)
			System.out.println("Metric values were succesfully collected!");	
	}

	private void collectMetrics() {
		
		for(File file:javaFiles) {
			
			// 소스코드에서 메트릭을 계산해서 만들어야 하기 때문에,
			// 파일 목록에 있는 JAva 소스코드 파일을 하나씩 읽어서 String으로 저장.
			String sourceCode = Utils.readAFile(file.getPath());
			
			// 문법대로 작성된 소스코드 파일을 자바 코드를 구성하는 단위 별로 분석하는 Parser를 실행해서
			// Abstract Systax Tree 만들기
			// Abstract Syntax Tree는 자바 소스코드의 각 구성요소를 Tree 데이터 구조의 형태로 저장한 것임
			// 예를들자면,
			// int a = 1;라는 코드가 있다고 하면, int는 type a는 변수, 1은 값으로 분석한 내용이 저장되어 있음
			JavaASTParser codeAST = new JavaASTParser(sourceCode);
			
			// 각 파일별로 다양한 종류의 메트릭이 생성되면 instance라는 파일명 + 메트리값들의 한 줄데이터로 저장함.
			// 이 것을 instance라고 부름.
			// 인스턴스를 생성할 때, 파일 이름이 인스턴스으로 사용하도록 인스턴스에 파일이름을 저장하면, 새로운 인스턴스 생성.
			RawInstance instance = new RawInstance(file.getPath());
			
			// (1) 첫번째 매트릭 계산: 가법게 코드 Line수 계산
			// 계산 후 instance안에 있는 linesOfCode값 부여.
			instance.setLinesOfCode(computeLinesOfCode(sourceCode));
			
			// (2) 두번째 메트릭 계산: 파일 안에 있는 변수의 개수
			instance.setNumOfVariables(computeNumOfVariables(codeAST));
			
			// (3) 세번째 메트릭 계산: 파일 안에 있는 매소드의 개수
			instance.setNumOfMethods(computeNumOfMethods(codeAST));
			
			// (4) 네번째 메트릭 계산: 메소드 호출 개수 (method invocation)
			instance.setNumOfMethodInvocations(computeNumOfMethodInvocations(codeAST));
			
			// (5) 다섯번째 메트릭 계산: 파일 안에 있는 for 루프의 개수
			instance.setNumOfForLoops(computeNumOfForLoops(codeAST));
			
			// (6) 다섯번째 메트릭 계산: 파일 안에 있는 if 구문의 개수
			instance.setNumOfIfStatements(computeNumOfIfStatements(codeAST));
			
			rawInstances.add(instance);
		}	
	}

	private int computeLinesOfCode(String sourceCode) {
		// 소스코드를 엔터(new line, \n)으로 나누어서 (split) 생성된 어레이의 크기를 리턴하면 라인수가 됨.
		return sourceCode.split("\n").length;
	}
	
	private int computeNumOfVariables(JavaASTParser codeAST) {
		
		// codeAST에서 변수선언된 요소들을 가져오고 몇 개나 있는지 크기를 가져온다.
		return codeAST.getVariableDeclarationFragments().size();
	}
	
	private int computeNumOfMethods(JavaASTParser codeAST) {
		// codeAST에서 매소드가 선언된 개수를 가져온다.
		return codeAST.getMethodDeclarations().size();
	}
	
	private int computeNumOfMethodInvocations(JavaASTParser codeAST) {
		// TASK1 TODO codeAST에서 매소드 호출된 개수를 가져온다.
		return codeAST.getMethodInvocations().size();
	}
	
	private int computeNumOfForLoops(JavaASTParser codeAST) {
		// TASK1 TODO codeAST에서 for loop의 개수를 가져온다.
		return codeAST.getForStatements().size();
	}
	
	private int computeNumOfIfStatements(JavaASTParser codeAST) {
		// TASK1 TODO codeAST에서 if문의 개수를 가져온다.
		return codeAST.getIfStatements().size();
	}
	
	public void saveAnArffFile() {
		if(Main.INFO)
			System.out.println("Saving instances in " + arffFilePathToBeSaved);
		Instances instances = WekaUtils.generateWekaInstances(rawInstances);
		WekaUtils.writeADataFile(instances, arffFilePathToBeSaved);
		Utils.writeADataFile(rawInstances, arffFilePathToBeSaved + ".file_path.txt");
		if(Main.INFO)
			System.out.println("Done");
	}
}
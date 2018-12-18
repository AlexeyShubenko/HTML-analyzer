# HTML-analyzer
HTML analyzer

To obtain jar file, run commands:
1. mvn clean compile;
2. mvn package.
In project package, in package target you will find jar file AnalyzerTest-1.0-SNAPSHOT-jar-with-dependencies, use it.

To run this app from console use (example):
java -cp C:\Users\AnalyzerTest.jar com.analyzer.Start C:\Users\sample-0-origin.html C:\Users\sample-1-evil-gemini.html make-everything-ok-button

Enter next parameters you shoud provide:
1. write path to your jar file;
2. specify main class;
3. original html file path;
4. diff-case html file path;
5. target element id.

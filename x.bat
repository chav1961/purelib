set MAVEN_USERNAME=chav1961
set MAVEN_PASSWORD=Kb12212949
mvn -X deploy:deploy-file -Dfile=./target/purelib-0.0.4.jar -DpomFile=./pom.xml -DrepositoryId=github -Durl=https://maven.pkg.github.com -Dtoken=0.0.4 -Dregistry=https://maven.pkg.github.com/chav1961 >zzz

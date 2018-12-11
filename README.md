# Pure Library

This project is a set of useful classes and packages, what I use in my own projects. This library is used only and only JRE and **nothing** more, and it was one of the most important goals of this projects. The most important functionality is:

* class loader for building pluggable applications
* a set of facades for different file systems
* an internationalization tool for use in the Java applications
* a Writer class, that implements **Java byte code macro-assembler**
* a Writer class, that implements **Creole markup language** processor
* classes to use in quick data parsers (quick syntax tree, quick line parsers)
* classes for simplify console command processors development

## Getting Started

### Prerequisites

To use Pure library, you need installed Java 1.8 version 31 and later. Goto [ORACLE Java download](https://www.oracle.com/downloads/index.html) center and download this software

### Installing

Download project from [GitHub](https://github.com/chav1961/purelib) repository and place it to any directory you wish. Use [Maven](https://maven.apache.org/) to get access to the project:

```XML
	<dependency>
		<groupId>com.github.chav1961</groupId>
		<artifactId>purelib</artifactId>
		<version>0.0.1</version>
	<dependency>
```

Also add repository GitHub repository description to your xml.pom file:

```XML
	<repositories>
	    <repository>
	        <id>purelib-mvn-repo</id>
	        <url>https://raw.github.com/chav1961/purelib/mvn-repo/</url>
	        <snapshots>
	            <enabled>true</enabled>
	            <updatePolicy>always</updatePolicy>
	        </snapshots>
	    </repository>
	</repositories>
```

## Built With

* [Eclipse](http://www.eclipse.org) - The Eclipse IDE
* [Maven](https://maven.apache.org/) - Dependency Management

## Documentation

Use JavaDoc of the Pure Library to use it in your projects. Also use [Wiki](https://github.com/chav1961/purelib/wiki) of Pure Library. It is under construction process, so sorry for some artifacts in it :-) Library contains much more classes, than is described, but don't use them because they are under development and I not guarantee their functionality yet.

## Authors

* **Alexander Charnomyrdin** - *Initial work* 

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

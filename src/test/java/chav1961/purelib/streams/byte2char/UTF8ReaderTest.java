package chav1961.purelib.streams.byte2char;


import java.io.OutputStream;
import java.io.InputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import chav1961.purelib.basic.Utils;

public class UTF8ReaderTest {

	@Test
	public void basicTest() throws UnsupportedEncodingException, IOException {
		final File		temp = File.createTempFile("test", ".txt");
		final String	test = "abcdeАБВГД\uFE16\uFE17\uFE18"; 
		
		try(final OutputStream	os = new FileOutputStream(temp)) {
			try(final Writer	wr = new OutputStreamWriter(os, "utf-8")) {
				wr.write(test);
			}
			
			try(final Reader	rdr = new UTF8Reader(temp, 4);
				final BufferedReader	brdr = new BufferedReader(rdr)) {
				
				Assert.assertEquals(test, brdr.readLine());
			}
		} finally {
			temp.delete();
		}
	}

	@Test
	public void xmlPerfomanceTest() throws UnsupportedEncodingException, IOException, ParserConfigurationException, SAXException {
		final File		temp = new File("c:/tmp/нейросети/dict.opcorpora.xml");
		final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		System.gc();
		final long		start1 = System.currentTimeMillis();
		
		try(final InputStream	is = new FileInputStream(temp)) {
			builder.parse(is);
		}
		System.err.println("XML load[1]="+(System.currentTimeMillis()-start1)+" msec");
		System.gc();
		final long		start2 = System.currentTimeMillis();
		
		try(final Reader	rdr = new FileReader(temp, Charset.forName("utf-8"))) {
			builder.parse(new InputSource(rdr));
		}
		System.err.println("XML load[2]="+(System.currentTimeMillis()-start2)+" msec");
		System.gc();
		final long		start3 = System.currentTimeMillis();
		
		try(final Reader	rdr = new UTF8Reader(temp)) {
			builder.parse(new InputSource(rdr));
		}
		System.err.println("XML load[3]="+(System.currentTimeMillis()-start3)+" msec");
	}	
	
//	@Test
	public void perfomanceTest() throws UnsupportedEncodingException, IOException {
		final File		temp = new File("c:/tmp/нейросети/dict.opcorpora.xml");
		final Writer	nullWriter = Writer.nullWriter();

		System.gc();
		final long		start1 = System.currentTimeMillis();
		
		try(final Reader	rdr = new FileReader(temp, Charset.forName("utf-8"))) {
			
			System.err.println("Copy: "+Utils.copyStream(rdr, nullWriter));
		}
		System.err.println("Load[1]="+(System.currentTimeMillis()-start1)+" msec");
		System.gc();
		final long		start2 = System.currentTimeMillis();
		
		try(final Reader	rdr = new UTF8Reader(temp)) {
			
			System.err.println("Copy: "+Utils.copyStream(rdr, nullWriter));
		}
		System.err.println("Load[2]="+(System.currentTimeMillis()-start2)+" msec");
	}
	
}

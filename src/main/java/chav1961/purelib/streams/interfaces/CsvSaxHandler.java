package chav1961.purelib.streams.interfaces;

import chav1961.purelib.basic.exceptions.ContentException;

public interface CsvSaxHandler {
	void startDoc() throws ContentException; 
	void endDoc() throws ContentException; 
	void startCaption() throws ContentException; 
	void name(int position, String name) throws ContentException; 
	void name(int position, char[] name, int from, int len) throws ContentException; 
	void endCaption() throws ContentException; 
	void startData() throws ContentException;
	void value(int position) throws ContentException;
	void value(int position, long value) throws ContentException;
	void value(int position, double value) throws ContentException;
	void value(int position, String value) throws ContentException; 
	void value(int position, char[] value, int from, int len) throws ContentException;
	void endData() throws ContentException; 
}

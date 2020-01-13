package chav1961.purelib;

import java.nio.file.spi.FileSystemProvider;

class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		for (FileSystemProvider item : FileSystemProvider.installedProviders()) {
			System.err.println("Item: "+item.getScheme());
		}
	}

}

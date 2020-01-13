package chav1961.purelib.fsys.adapter;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.util.Map;

interface InitializingFileAttributeView extends FileAttributeView {

  void initializeRoot();

  void initializeFrom(BasicFileAttributeView basicFileAttributeView) throws IOException;

  void initializeFrom(Map<String, ? extends FileAttributeView> additionalAttributes);

}

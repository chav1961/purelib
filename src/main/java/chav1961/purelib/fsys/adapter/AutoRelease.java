package chav1961.purelib.fsys.adapter;

@FunctionalInterface
interface AutoRelease extends AutoCloseable {


  @Override
  void close();

}

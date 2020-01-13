package chav1961.purelib.fsys.adapter;

enum TwoPathOperation {

  COPY {
    @Override
    boolean isMove() {
      return false;
    }
  },


  MOVE {
    @Override
    boolean isMove() {
      return true;
    }
  };

  abstract boolean isMove();

}
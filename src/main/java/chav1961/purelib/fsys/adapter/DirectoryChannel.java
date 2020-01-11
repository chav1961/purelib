package chav1961.purelib.fsys.adapter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import chav1961.purelib.fsys.adapter.MemoryDirectory.MemoryDirectoryAttributes;

final class DirectoryChannel extends FileChannel {

  private volatile long position;

  @Override
  public void force(boolean metaData) throws IOException {
    this.closedCheck();
    // nothing to do
  }

  @Override
  public int read(ByteBuffer dst) throws IOException {
    this.closedCheck();
    throw new IOException("Is a directory");
  }

  @Override
  public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
    this.closedCheck();
    throw new IOException("Is a directory");
  }

  @Override
  public int read(ByteBuffer dst, long position) throws IOException {
    this.closedCheck();
    throw new IOException("Is a directory");
  }

  @Override
  public int write(ByteBuffer src) throws IOException {
    this.closedCheck();
    throw new NonWritableChannelException();
  }

  @Override
  public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
    this.closedCheck();
    throw new NonWritableChannelException();
  }

  @Override
  public int write(ByteBuffer src, long position) throws IOException {
    this.closedCheck();
    throw new NonWritableChannelException();
  }

  @Override
  public long position() throws IOException {
    this.closedCheck();
    return this.position;
  }

  @Override
  public FileChannel position(long newPosition) throws IOException {
    this.closedCheck();
    if (newPosition < 0L) {
      throw new IllegalArgumentException("position must not be negative");
    }
    this.position = newPosition;
    return this;
  }

  @Override
  public long size() throws IOException {
    this.closedCheck();
    return MemoryDirectoryAttributes.SIZE;
  }

  @Override
  public FileChannel truncate(long size) throws IOException {
    this.closedCheck();
    throw new NonWritableChannelException();
  }

  @Override
  public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
    this.closedCheck();
    throw new IOException("Operation not supported");
  }

  @Override
  public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
    this.closedCheck();
    throw new NonWritableChannelException();
  }

  @Override
  public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
    this.closedCheck();
    throw new UnsupportedOperationException("memory file system does not support mmapped IO");
  }

  @Override
  public FileLock lock(long position, long size, boolean shared) throws IOException {
    this.closedCheck();
    if (!shared) {
      throw new NonWritableChannelException();
    }
    throw new IllegalArgumentException("directory file size is empty");
  }

  @Override
  public FileLock tryLock(long position, long size, boolean shared) throws IOException {
    this.closedCheck();
    if (!shared) {
      throw new NonWritableChannelException();
    }
    throw new IllegalArgumentException("directory file size is empty");
  }

  @Override
  protected void implCloseChannel() {
    // ignore
  }

  void closedCheck() throws ClosedChannelException {
    if (!this.isOpen()) {
      throw new ClosedChannelException();
    }
  }

}

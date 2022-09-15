package com.lyit.csd;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is a subclass of InputStream class and implements its methods.
 */
public class UnclosableInputStreamDecorator extends InputStream {

  /**
   * InputStream object for user input.
   */
  private final InputStream inputStream;

  /**
   * Constructor of the class.
   *
   * @param inputStream with ordered sequence of bytes(reading from input).
   */
  public UnclosableInputStreamDecorator(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  /**
   * @inheritDoc
   */
  @Override
  public int read() throws IOException {
    return inputStream.read();
  }

  /**
   * @inheritDoc
   */
  @Override
  public int read(byte[] b) throws IOException {
    return inputStream.read(b);
  }

  /**
   * @inheritDoc
   */
  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return inputStream.read(b, off, len);
  }

  /**
   * @inheritDoc
   */
  @Override
  public long skip(long n) throws IOException {
    return inputStream.skip(n);
  }

  /**
   * @inheritDoc
   */
  @Override
  public int available() throws IOException {
    return inputStream.available();
  }

  /**
   * @inheritDoc
   */
  @Override
  public synchronized void mark(int readlimit) {
    inputStream.mark(readlimit);
  }

  /**
   * @inheritDoc
   */
  @Override
  public synchronized void reset() throws IOException {
    inputStream.reset();
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean markSupported() {
    return inputStream.markSupported();
  }

  /**
   * @inheritDoc
   */
  @Override
  public void close() throws IOException {
    // Does nothing
  }
}

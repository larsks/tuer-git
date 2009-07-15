package com.jcraft.jorbis;

public class SeekableInputStream extends java.io.InputStream {
    java.io.RandomAccessFile raf=null;
    final String mode="r";
    private SeekableInputStream(){
    }
    SeekableInputStream(String file) throws java.io.IOException{
      raf=new java.io.RandomAccessFile(file, mode);
    }
    public int read() throws java.io.IOException{
      return raf.read();
    }
    public int read(byte[] buf) throws java.io.IOException{
      return raf.read(buf);
    }
    public int read(byte[] buf , int s, int len) throws java.io.IOException{
      return raf.read(buf, s, len);
    }
    public long skip(long n) throws java.io.IOException{
      return (long)(raf.skipBytes((int)n));
    }
    public long getLength() throws java.io.IOException{
      return raf.length();
    }
    public long tell() throws java.io.IOException{
      return raf.getFilePointer();
    }
    public int available() throws java.io.IOException{
      return (raf.length()==raf.getFilePointer())? 0 : 1;
    }
    public void close() throws java.io.IOException{
      raf.close();
    }
    public synchronized void mark(int m){
    }
    public synchronized void reset() throws java.io.IOException{
    }
    public boolean markSupported(){
      return false;
    }
    public void seek(long pos) throws java.io.IOException{
      raf.seek(pos);
    }
}

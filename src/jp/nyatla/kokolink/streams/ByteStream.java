package jp.nyatla.kokolink.streams;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import jp.nyatla.kokolink.compatibility.PyIterator;
import jp.nyatla.kokolink.streams.bytestreams.BasicByteStream;
import jp.nyatla.kokolink.types.Py__class__.IPyIterator;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;

public class ByteStream extends BasicByteStream{
    public static class ByteCastIter implements IPyIterator<Integer>
    {
        private IPyIterator<Byte>  _src;
        public ByteCastIter(IPyIterator<Byte> src)
        {
            this._src = src;
        }
        public ByteCastIter(String src, String encoding) throws UnsupportedEncodingException
        {
        	List<Byte> b=new ArrayList<Byte>();
        	for(var i:src.getBytes(encoding)) {
        		b.add(i);
        	}
            this._src=new PyIterator<Byte>(b);
        }

        public Integer next() throws PyStopIteration
        {
            return this._src.next() & 0x7fffffff;
        }
    }

    private int _pos;
    private IPyIterator<Integer> _iter;
    // """ iterをラップするByteStreamストリームを生成します。
    //     bytesの場合は1バイトづつ返します。
    //     strの場合はbytesに変換してから1バイトづつ返します。
    // """
    public ByteStream(IPyIterator<Integer> src){
    	this(src,0);
    }
    public ByteStream(IPyIterator<Integer> src,int inital_pos){
        this._pos=inital_pos;// #現在の読み出し位置
        this._iter=src;
    }
    public ByteStream(String src) throws UnsupportedEncodingException {
    	this(src,0,"utf-8");
    }
    public ByteStream(String src,int inital_pos,String encoding) throws UnsupportedEncodingException
    {
        this(new ByteCastIter(src, encoding), inital_pos);
    }


    @Override
    public Integer next() throws PyStopIteration{
        var r=this._iter.next();
        this._pos=this._pos+1;
        return r;
    }
    @Override
    public long getPos(){
        return this._pos;
    }
}


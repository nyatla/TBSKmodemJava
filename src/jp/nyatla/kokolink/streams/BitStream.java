package jp.nyatla.kokolink.streams;

import jp.nyatla.kokolink.interfaces.IBitStream;
import jp.nyatla.kokolink.streams.rostreams.BasicRoStream;
import jp.nyatla.kokolink.types.Py__class__.PyIterator;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;
import jp.nyatla.kokolink.utils.BitsWidthConvertIterator;
import jp.nyatla.kokolink.utils.recoverable.RecoverableStopIteration;

public class BitStream extends BasicRoStream<Integer> implements IBitStream
{
    private int _pos;
    private BitsWidthConvertIterator _bw;
    // """ 任意ビット幅のintストリームを1ビット単位のビットストリームに展開します。
    // """
    public BitStream(Iterable<Integer> src) {
    	this(src,8);
    }
    public BitStream(Iterable<Integer> src, int bitwidth)
    {
    	this(new PyIterator<Integer>(src),bitwidth);
    }

    public BitStream(IPyIterator<Integer> src){
    	this(src,8);
    }
    public BitStream(IPyIterator<Integer> src,int bitwidth){
        this._bw=new BitsWidthConvertIterator(src,bitwidth,1);
        this._pos=0;
    }
    @Override
    public Integer next() throws PyStopIteration{
        int r;
        try{
            r=this._bw.next();
        }catch(RecoverableStopIteration e){
            throw e;
        }
        this._pos=this._pos+1;
        return r;
    }
    @Override
    public long getPos(){
        return this._pos;
    }
}




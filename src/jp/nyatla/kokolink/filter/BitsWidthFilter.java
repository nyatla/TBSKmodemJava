package jp.nyatla.kokolink.filter;

import jp.nyatla.kokolink.interfaces.IFilter;
import jp.nyatla.kokolink.interfaces.IRoStream;
import jp.nyatla.kokolink.streams.rostreams.BasicRoStream;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.utils.BitsWidthConvertIterator;

// """ 任意ビット幅のintストリームを任意ビット幅のint値にエンコードします。
// """
public class BitsWidthFilter extends BasicRoStream<Integer> implements IFilter<BitsWidthFilter,IRoStream<Integer>,Integer>
{
    
    private int _pos;
    private int _input_bits;
    private int _output_bits;
    private BitsWidthConvertIterator _iter;
    public BitsWidthFilter(){
    	this(8,1);
    }
    
    public BitsWidthFilter(int input_bits,int output_bits){
    	super();
        this._input_bits=input_bits;
        this._output_bits=output_bits;
        this._iter=null;
    }

    public BitsWidthFilter setInput(IRoStream<Integer> src)
    {
        this._pos=0;
        this._iter=src==null?null:new BitsWidthConvertIterator(src,this._input_bits,this._output_bits);
        return this;
    }

    public Integer next() throws PyStopIteration
    {
        if(this._iter==null){
            throw new PyStopIteration();
        }
        var r=this._iter.next();
        this._pos=this._pos+1;
        return r;
    }
    // @property
    public long getPos(){
        return this._pos;
    }
}


package jp.nyatla.kokolink.filter;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import jp.nyatla.kokolink.interfaces.IFilter;
import jp.nyatla.kokolink.interfaces.IRoStream;
import jp.nyatla.kokolink.streams.rostreams.BasicRoStream;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;
import jp.nyatla.kokolink.utils.BitsWidthConvertIterator;
import jp.nyatla.kokolink.utils.BrokenTextStreamDecoder;
import jp.nyatla.kokolink.utils.recoverable.RecoverableStopIteration;

// """ nBit intイテレータから1バイト単位のbytesを返すフィルタです。
// """
public class Bits2StrFilter extends BasicRoStream<Character > implements IFilter<Bits2StrFilter, IRoStream<Integer>, Character >
{
    private int _pos;
    private int _input_bits;
    private IPyIterator<Integer> _iter;
    private BrokenTextStreamDecoder _decoder;
    public Bits2StrFilter() {
    	this(1,"utf-8");
    }

    public Bits2StrFilter(int input_bits, String encoding)
    {
    	super();
        this._input_bits = input_bits;
        this._decoder=new BrokenTextStreamDecoder(encoding);

    }
    @Override
    public Bits2StrFilter setInput(IRoStream<Integer> src)
    {
        this._pos = 0;
        this._iter = src == null ? null : new BitsWidthConvertIterator(src, this._input_bits, 8);
        return this;
    }

    @Override
    public Character next() throws PyStopIteration
    {
        if (this._iter == null)
        {
            throw new PyStopIteration();
        }
        try
        {
        	while(true) {
                var r=this._decoder.update(this._iter.next().byteValue());
        		if(r!=null) {
        			return r;
        		}
        	}
        }
        catch (RecoverableStopIteration e)
        {
            throw e;
        }catch(PyStopIteration e) {
        	var r=this._decoder.update();
        	if(r==null) {
        		throw e;
        	}
        	return r;
        }

    }
    @Override
    public long getPos()
    {
        return this._pos;
    }

}
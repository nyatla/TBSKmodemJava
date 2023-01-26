package jp.nyatla.kokolink.filter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import jp.nyatla.kokolink.interfaces.IFilter;
import jp.nyatla.kokolink.interfaces.IRoStream;
import jp.nyatla.kokolink.streams.rostreams.BasicRoStream;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;
import jp.nyatla.kokolink.utils.BitsWidthConvertIterator;
import jp.nyatla.kokolink.utils.recoverable.RecoverableStopIteration;

// """ nBit intイテレータから1バイト単位のbytesを返すフィルタです。
// """
public class Bits2StrFilter extends BasicRoStream<Character > implements IFilter<Bits2StrFilter, IRoStream<Integer>, Character >
{
    private int _pos;
    private int _input_bits;
    private String _encoding;
    private List<Byte> _savedata;
    private IPyIterator<Integer> _iter;
    public Bits2StrFilter() {
    	this(1,"utf-8");
    }

    public Bits2StrFilter(int input_bits, String encoding)
    {
    	super();
        this._input_bits = input_bits;
        this._encoding = encoding;
        this._savedata = new ArrayList<Byte>();
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
        while (true)
        {
            int d;
            try
            {
                d = this._iter.next();
            }
            catch (RecoverableStopIteration e)
            {
                throw e;
            }
            this._savedata.add((byte)d);
            try
            {
            	byte[] a=new byte[this._savedata.size()];
            	for(var i=0;i<a.length;i++) {
            		a[i]=this._savedata.get(i);
            	}
                var r = new String(a,this._encoding);// System.Text.Encoding.GetEncoding(this._encoding,new EncoderExceptionFallback(),new DecoderExceptionFallback()).GetChars(this._savedata.ToArray());
                this._savedata.clear();
                return r.charAt(0);
            }
            catch (UnsupportedEncodingException e)
            {
                continue;
            }
        }
    }
    @Override
    public long getPos()
    {
        return this._pos;
    }

}
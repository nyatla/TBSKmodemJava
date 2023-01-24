package jp.nyatla.kokolink.filter;

import jp.nyatla.kokolink.interfaces.IFilter;
import jp.nyatla.kokolink.interfaces.IRoStream;
import jp.nyatla.kokolink.streams.rostreams.BasicRoStream;
import jp.nyatla.kokolink.types.Py__class__.IPyIterator;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.utils.BitsWidthConvertIterator;

// """ nBit intイテレータから1バイト単位のbytesを返すフィルタです。
// """
public class Bits2BytesFilter extends BasicRoStream<Byte> implements IFilter<Bits2BytesFilter, IRoStream<Integer>, Byte>
{
    private int _pos;
    private int _input_bits;
    private IPyIterator<Integer> _iter;

    public Bits2BytesFilter() {
    	this(1);
    }
    public Bits2BytesFilter(int input_bits)
    {
        this._input_bits = input_bits;

    }
    public Bits2BytesFilter setInput(IRoStream<Integer> src)
    {
        this._pos = 0;
        this._iter = src == null ? src : new BitsWidthConvertIterator(src, this._input_bits, 8);
        return this;
    }
    @Override
    public Byte next() throws PyStopIteration
    {
        if (this._iter == null) {
            throw new PyStopIteration();
        }
        var r = this._iter.next();
        this._pos = this._pos + 1;
        assert(0 <= r && r <= 255);
        return (byte)(r & 0xff);

    }
    public long getPos()
    {
        return this._pos;
    }




}

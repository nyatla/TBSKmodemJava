package jp.nyatla.tbskmodem;

import jp.nyatla.kokolink.compatibility;
import jp.nyatla.kokolink.compatibility.TbskIterable;
import jp.nyatla.kokolink.filter.Bits2BytesFilter;
import jp.nyatla.kokolink.filter.Bits2StrFilter;
import jp.nyatla.kokolink.filter.BitsWidthFilter;
import jp.nyatla.kokolink.interfaces.IFilter;
import jp.nyatla.kokolink.interfaces.IRoStream;
import jp.nyatla.kokolink.protocol.tbsk.preamble.Preamble;
import jp.nyatla.kokolink.protocol.tbsk.tbskmodem.TbskDemodulator_impl;
import jp.nyatla.kokolink.protocol.tbsk.toneblock.TraitTone;
import jp.nyatla.kokolink.streams.rostreams.BasicRoStream;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;
import jp.nyatla.kokolink.utils.recoverable.RecoverableException;
import jp.nyatla.kokolink.utils.recoverable.RecoverableStopIteration;

public class TbskDemodulator extends TbskDemodulator_impl
{
    // """ nBit intイテレータから1バイト単位のhex stringを返すフィルタです。
    // """
    private static class Bits2HexStrFilter extends BasicRoStream<String> implements IFilter<Bits2HexStrFilter, IRoStream<Integer>, String>
    {
        final private BitsWidthFilter _src;
        public Bits2HexStrFilter(int input_bits)
        {
            this._src = new BitsWidthFilter(input_bits,8);
        }
        @Override
        public String next() throws PyStopIteration
        {
            while (true)
            {
                int d;
                try
                {
                    d = this._src.next();
                }
                catch (RecoverableStopIteration e)
                {
                    throw e;
                }
                assert(0 < d && d < 256);
                return String.format("%02x",d);

            }
        }
        @Override            
        public Bits2HexStrFilter setInput(IRoStream<Integer> src)
        {
            this._src.setInput(src);
            return this;
        }
        @Override
        public long getPos()
        {
            return this._src.getPos();
        }
    }
	
    public TbskDemodulator(TraitTone tone)
    {
    	super(tone,null);
    }
    public TbskDemodulator(TraitTone tone, Preamble preamble)
    {
    	super(tone,preamble);
    }
    
    public Iterable<Integer> demodulateAsBit(Iterable<Double> src) throws RecoverableException{
    	return TbskIterable.createInstance(super.demodulateAsBit(compatibility.toPyIterator(src)));
    }
    
    
    //    """ TBSK信号からnビットのint値配列を復元します。
    //        関数は信号を検知する迄制御を返しません。信号を検知せずにストリームが終了した場合はNoneを返します。
    //    """
    public class DemodulateAsIntAS extends AsyncDemodulateX<Integer>
    {
        public DemodulateAsIntAS(TbskDemodulator_impl parent, IPyIterator<Double> src, int bitwidth)
        {
            super(parent, src, arg ->new BitsWidthFilter(1, bitwidth).setInput(arg));
        }
    }    
    public Iterable<Integer> demodulateAsInt(IPyIterator<Double> src) throws RecoverableException{
    	return this.demodulateAsInt(src,8);
    }
    public Iterable<Integer> demodulateAsInt(IPyIterator<Double> src, int bitwidth) throws RecoverableException
    {
        assert(!this._asmethod_lock);
        DemodulateAsIntAS asmethod = new DemodulateAsIntAS(this, src, bitwidth);
        if (asmethod.run())
        {
            return TbskIterable.createInstance(asmethod.getResult());
        }
        else
        {
            this._asmethod_lock = true;// #解放はAsyncDemodulateXのcloseで
            throw new RecoverableException(asmethod);
        }
    }
    public Iterable<Integer> demodulateAsInt(Iterable<Double> src) throws RecoverableException
    {
    	return this.demodulateAsInt(src,8);
    }
    public Iterable<Integer> demodulateAsInt(Iterable<Double> src, int bitwidth) throws RecoverableException
    {
        return this.demodulateAsInt(compatibility.toPyIterator(src), bitwidth);
    }
    public class DemodulateAsByteAS extends AsyncDemodulateX<Byte>
    {
        public DemodulateAsByteAS(TbskDemodulator_impl parent, IPyIterator<Double> src) {
            super(parent, src, arg -> new Bits2BytesFilter(1).setInput(arg));
        }
    }
    //    """ TBSK信号からバイト単位でbytesを返します。
    //        途中でストリームが終端した場合、既に読みだしたビットは破棄されます。
    //        関数は信号を検知する迄制御を返しません。信号を検知せずにストリームが終了した場合はNoneを返します。   
    //    """
    public Iterable<Byte> demodulateAsBytes(IPyIterator<Double> src) throws RecoverableException
    {
        assert(!this._asmethod_lock);
        DemodulateAsByteAS asmethod = new DemodulateAsByteAS(this, src);
        if (asmethod.run())
        {
            return TbskIterable.createInstance(asmethod.getResult());
        }
        else
        {
            this._asmethod_lock = true;// #解放はAsyncDemodulateXのcloseで
            throw new RecoverableException(asmethod);
        }
    }
    public Iterable<Byte> demodulateAsBytes(Iterable<Double> src) throws RecoverableException
    {
        return this.demodulateAsBytes(compatibility.toPyIterator(src));
    }



    public class DemodulateAsStrAS extends AsyncDemodulateX<Character>
    {
        public DemodulateAsStrAS(TbskDemodulator_impl parent, IPyIterator<Double> src)
        {
        	this(parent,src,"utf-8");
        }
        public DemodulateAsStrAS(TbskDemodulator_impl parent, IPyIterator<Double> src, String encoding)
        {
            super(parent, src, arg ->  new Bits2StrFilter(1,encoding).setInput(arg));
        }
    }
    
    //    """ TBSK信号からsize文字単位でstrを返します。
    //        途中でストリームが終端した場合、既に読みだしたビットは破棄されます。
    //        関数は信号を検知する迄制御を返しません。信号を検知せずにストリームが終了した場合はNoneを返します。
    //    """
    public Iterable<Character> demodulateAsStr(IPyIterator<Double> src,String encoding) throws RecoverableException
    {
        assert(!this._asmethod_lock);

        DemodulateAsStrAS asmethod = new DemodulateAsStrAS(this, src, encoding);
        if (asmethod.run())
        {
            return TbskIterable.createInstance(asmethod.getResult());
        }
        else
        {
            this._asmethod_lock = true;// #解放はAsyncDemodulateXのcloseで
            throw new RecoverableException(asmethod);
        }
    } 
    public Iterable<Character> demodulateAsStr(Iterable<Double> src) throws RecoverableException{
    	return this.demodulateAsStr(src,"utf-8");
    }
    public Iterable<Character> demodulateAsStr(Iterable<Double> src, String encoding) throws RecoverableException
    {
        return this.demodulateAsStr(compatibility.toPyIterator(src), encoding);
    }


    public class DemodulateAsHexStrAS extends AsyncDemodulateX<String>
    {
        public DemodulateAsHexStrAS(TbskDemodulator_impl parent, IPyIterator<Double> src) {
        	super(parent, src, arg -> new Bits2HexStrFilter(1).setInput(arg));
        }
    }


    public Iterable<String> demodulateAsHexStr(IPyIterator<Double> src) throws RecoverableException
    {
        assert(!this._asmethod_lock);
        DemodulateAsHexStrAS asmethod = new DemodulateAsHexStrAS(this, src);
        if (asmethod.run())
        {
            return TbskIterable.createInstance(asmethod.getResult());
        }
        else
        {
            this._asmethod_lock = true;// #解放はAsyncDemodulateXのcloseで
            throw new RecoverableException(asmethod);
        }
    }
    public Iterable<String> demodulateAsHexStr(Iterable<Double> src) throws RecoverableException
    {
        return this.demodulateAsHexStr(compatibility.toPyIterator(src));
    }
    public Iterable<String> demodulateAsHexStr(double[] src) throws RecoverableException
    {
        return this.demodulateAsHexStr(compatibility.toDoublePyIterator(src));
    }
    public Iterable<String> demodulateAsHexStr(Double[] src, String encoding) throws RecoverableException
    {
        return this.demodulateAsHexStr(compatibility.toDoublePyIterator(src));
    }    
}

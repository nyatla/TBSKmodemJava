package jp.nyatla.tbskmodem;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import jp.nyatla.kokolink.filter.BitsWidthFilter;
import jp.nyatla.kokolink.protocol.tbsk.preamble.CoffPreamble;
import jp.nyatla.kokolink.protocol.tbsk.preamble.Preamble;
import jp.nyatla.kokolink.protocol.tbsk.toneblock.TraitTone;
import jp.nyatla.kokolink.streams.ByteStream;
import jp.nyatla.kokolink.compatibility.Functions;
import jp.nyatla.kokolink.compatibility.TbskIterable;
import jp.nyatla.kokolink.streams.RoStream;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;

public class TbskModulator extends jp.nyatla.kokolink.protocol.tbsk.tbskmodem.TbskModulator_impl
{
    private class SuffixTone implements IPyIterator<Double>
    {
        private TraitTone _tone;
        private int _c = 0;
        public SuffixTone(TraitTone tone)
        {
            this._tone = tone;
            this._c = 0;
        }
        public Double next() throws PyStopIteration
        {
            var c = this._c;
            if (c >= this._tone.size())
            {
                throw new PyStopIteration();
            }
            var tone = this._tone;
            var r = c % 2 == 0 ? tone.get(c) * 0.5 : -tone.get(c) * 0.5;
            this._c++;
            return r;
        }

    }	
    public TbskModulator(TraitTone tone) {
    	this(tone,TbskPreamble.createCoff(tone));
    }
    public TbskModulator(TraitTone tone, int preamble_cycle) {
    	super(tone, new CoffPreamble(tone,CoffPreamble.DEFAULT_TH,preamble_cycle));
    }    
	public TbskModulator(TraitTone tone, Preamble preamble)
    {
    	super(tone,preamble);
    }

	private Iterable<Double> _modulateAsBit(IPyIterator<Integer> src, boolean stopsymbol)
    {
        SuffixTone suffix = null;
        if (stopsymbol)
        {
            suffix = new SuffixTone(this._tone);
        }		
    	var w=super.modulateAsBit(src,suffix,true);
        return w==null?null:TbskIterable.<Double>createInstance(w);
    }
	
	
    public Iterable<Double> modulateAsBit(Iterable<Integer> src, boolean stopsymbol)
    {
    	@SuppressWarnings("unchecked")
		IPyIterator<Integer> s=(src instanceof IPyIterator)?(IPyIterator<Integer>)src:Functions.toPyIterator(src);
    	return this._modulateAsBit(s, stopsymbol);
    }
    public Iterable<Double> modulateAsBit(Iterable<Integer> src)
    {
    	return this.modulateAsBit(src,true);
    }

    public Iterable<Double> modulateAsBit(Integer[] src, boolean stopsymbol)
    {
    	return this._modulateAsBit(Functions.toIntegerPyIterator(src), stopsymbol);
    }
    public Iterable<Double> modulateAsBit(Integer[] src)
    {
    	return this.modulateAsBit(src,true);
    }

    public Iterable<Double> modulateAsBit(int[] src, boolean stopsymbol)
    {
    	return this._modulateAsBit(Functions.toIntegerPyIterator(src), stopsymbol);
    }
    public Iterable<Double> modulateAsBit(int[] src)
    {
    	return this.modulateAsBit(src,true);
    }



    public Iterable<Double> modulate(IPyIterator<Integer> src, int bitwidth, boolean stopsymbol)
    {
        //既にIPyIteratorを持っていたらそのまま使う。
    	return this._modulateAsBit(new BitsWidthFilter(bitwidth,1).setInput(new RoStream<Integer>(src)),stopsymbol);
    }
    public Iterable<Double> modulate(IPyIterator<Integer> src, int bitwidth)
    {
    	return this.modulate(src,bitwidth,true);
    }

    
    public Iterable<Double> modulate(Iterable<Integer> src, int bitwidth, boolean stopsymbol)
    {
    	return this.modulate(Functions.toPyIterator(src),bitwidth,stopsymbol);
    }
    public Iterable<Double> modulate(Iterable<Integer> src, int bitwidth)
    {
    	return this.modulate(src,bitwidth,true);
    }

    
    public Iterable<Double> modulate(Iterable<Integer> src, boolean stopsymbol)
    {
        return this.modulate(src,8,stopsymbol);        	
    }
    public Iterable<Double> modulate(Iterable<Integer> src)
    {
        return this.modulate(src,8,true);
    }

    public Iterable<Double> modulate(String src, boolean stopsymbol) throws UnsupportedEncodingException
    {
    	return this.modulate(src,"utf-8",stopsymbol);
    }
    public Iterable<Double> modulate(String src) throws UnsupportedEncodingException
    {
    	return this.modulate(src,true);
    }


    public Iterable<Double> modulate(String src,String encoding, boolean stopsymbol) throws UnsupportedEncodingException
    {
    	return this._modulateAsBit(new BitsWidthFilter(8,1).setInput(new ByteStream(src,0,encoding)),stopsymbol);
    }
    public Iterable<Double> modulate(String src,String encoding) throws UnsupportedEncodingException
    {
    	return this.modulate(src,encoding,true);
    }

    
    public Iterable<Double> modulateAsByte(Iterable<Byte> src, boolean stopsymbol)
    {
        //既にIPyIteratorを持っていたらそのまま使う。
        return this._modulateAsBit(new BitsWidthFilter(8,1).setInput(new ByteStream(new ByteStream.ByteCastIter(Functions.toPyIterator(src)))),stopsymbol);
    }    
    public Iterable<Double> modulateAsByte(Iterable<Byte> src)
    {
        return this.modulateAsByte(src,true);
    }    

    
    public Iterable<Double> modulateAsHexStr(String src, boolean stopsymbol)
    {
        // """ hex stringを変調します。
        //     hex stringは(0x)?[0-9a-fA-F]{2}形式の文字列です。
        //     hex stringはbytesに変換されて送信されます。
        // """
        assert(src.length() % 2 == 0);
        if (src.substring(0, 2) == "0x")
        {
            src = src.substring(2, src.length() - 2);
        }
        var d = new ArrayList<Integer>();
        for (var i = 0; i < src.length() / 2; i++)
        {
            d.add(Integer.parseInt(src.substring(i*2,i*2+2)));
        }
        return this.modulate(d,stopsymbol);
    }
    
    public Iterable<Double> modulateAsHexStr(String src){
        return this.modulateAsHexStr(src,true);
    }
    
}

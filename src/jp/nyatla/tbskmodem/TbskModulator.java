package jp.nyatla.tbskmodem;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import jp.nyatla.kokolink.filter.BitsWidthFilter;
import jp.nyatla.kokolink.protocol.tbsk.preamble.Preamble;
import jp.nyatla.kokolink.protocol.tbsk.toneblock.TraitTone;
import jp.nyatla.kokolink.streams.ByteStream;
import jp.nyatla.kokolink.compatibility;
import jp.nyatla.kokolink.compatibility.TbskIterable;
import jp.nyatla.kokolink.streams.RoStream;
import jp.nyatla.kokolink.types.Py__class__.PyIterator;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;

public class TbskModulator extends jp.nyatla.kokolink.protocol.tbsk.tbskmodem.TbskModulator_impl
{
    public TbskModulator(TraitTone tone) {
    	this(tone,TbskPreamble.createCoff(tone));
    }
	public TbskModulator(TraitTone tone, Preamble preamble)
    {
    	super(tone,preamble);
    }    
    public Iterable<Double> modulateAsBit(Iterable<Integer> src)
    {
    	@SuppressWarnings("unchecked")
		IPyIterator<Integer> s=(src instanceof IPyIterator)?(IPyIterator<Integer>)src:new PyIterator<Integer>(src);
        return TbskIterable.<Double>createInstance(
    		super.modulateAsBit(s)
    	);
    }
    public Iterable<Double> modulateAsBit(Integer[] src)
    {
    	return TbskIterable.<Double>createInstance(
    		super.modulateAsBit(compatibility.toIntegerPyIterator(src))
    	);
    }
    public Iterable<Double> modulateAsBit(int[] src)
    {
    	return TbskIterable.<Double>createInstance(
    		super.modulateAsBit(compatibility.toIntegerPyIterator(src))
    	);
    }



    public Iterable<Double> modulate(IPyIterator<Integer> src, int bitwidth)
    {
        //既にIPyIteratorを持っていたらそのまま使う。
        return TbskIterable.<Double>createInstance(
        	this.modulateAsBit(
        		new BitsWidthFilter(bitwidth,1).setInput(new RoStream<Integer>(src))
        		)
        	);
    }
    public Iterable<Double> modulate(Iterable<Integer> src, int bitwidth)
    {
    	return this.modulate(src,bitwidth);
    }
    public Iterable<Double> modulate(Iterable<Integer> src)
    {
        return this.modulate(src,8);        	
    }

    public Iterable<Double> modulate(String src) throws UnsupportedEncodingException
    {
    	return this.modulate(src,"utf-8");
    }
    public Iterable<Double> modulate(String src,String encoding) throws UnsupportedEncodingException
    {
    	return TbskIterable.<Double>createInstance(
    		this.modulateAsBit(
            new BitsWidthFilter(8,1).setInput(new ByteStream(src,0,encoding))
            ));
    }

    public Iterable<Double> modulateAsByte(Iterable<Byte> src)
    {
        //既にIPyIteratorを持っていたらそのまま使う。
    	return TbskIterable.<Double>createInstance(
        this.modulateAsBit(
            new BitsWidthFilter(8,1).setInput(new ByteStream(new ByteStream.ByteCastIter(compatibility.toPyIterator(src))))
        ));
    }    
    
    public Iterable<Double> modulateAsHexStr(String src)
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
        return this.modulate(d);
    }
    
}

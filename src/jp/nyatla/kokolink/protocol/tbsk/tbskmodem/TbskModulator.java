package jp.nyatla.kokolink.protocol.tbsk.tbskmodem;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import jp.nyatla.kokolink.compatibility.Functions;
import jp.nyatla.kokolink.compatibility.PyIterator;
import jp.nyatla.kokolink.compatibility.TbskIterable;
import jp.nyatla.kokolink.filter.BitsWidthFilter;
import jp.nyatla.kokolink.interfaces.IBitStream;
import jp.nyatla.kokolink.interfaces.IRoStream;
import jp.nyatla.kokolink.protocol.tbsk.preamble.CoffPreamble;
import jp.nyatla.kokolink.protocol.tbsk.preamble.Preamble;
import jp.nyatla.kokolink.protocol.tbsk.toneblock.TraitTone;
import jp.nyatla.kokolink.protocol.tbsk.traitblockcoder.TraitBlockEncoder;
import jp.nyatla.kokolink.streams.BitStream;
import jp.nyatla.kokolink.streams.ByteStream;
import jp.nyatla.kokolink.streams.RoStream;
import jp.nyatla.kokolink.streams.rostreams.BasicRoStream;
import jp.nyatla.kokolink.types.Py__class__.IPyIterator;
import jp.nyatla.kokolink.types.Py__class__.IterChain;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.types.Py__class__.Repeater;

// """ TBSKの変調クラスです。
//     プリアンブルを前置した後にビットパターンを置きます。
// """
public class TbskModulator
{
    // """ ビット配列を差動ビットに変換します。
    // """
    private class DiffBitEncoder extends BasicRoStream<Integer> implements IBitStream
    {
        private int _last_bit;
        private IRoStream<Integer> _src;
        private boolean _is_eos;
        private int _pos;
        public DiffBitEncoder(int firstbit, IRoStream<Integer> src)
        {

            this._last_bit = firstbit;
            this._src = src;
            this._is_eos = false;
            this._pos = 0;
        }
        @Override
        public Integer next() throws PyStopIteration
        {
            if (this._is_eos)
            {
                throw new PyStopIteration();
            }
            if (this._pos == 0)
            {
                this._pos = this._pos + 1;
                return this._last_bit; //#1st基準シンボル
            }
            int n;
            try
            {
                n = this._src.next();
            }
            catch (PyStopIteration e)
            {
                this._is_eos = true;
                throw new PyStopIteration(e);
            }
            if (n == 1)
            {
                //pass
            }
            else
            {
                this._last_bit = (this._last_bit + 1) % 2;
            }
            return this._last_bit;
        }
        @Override
        public long getPos()
        {
            return this._pos;
        }
    }



    private TraitTone _tone;
    private Preamble _preamble;
    private TraitBlockEncoder _enc;
    public TbskModulator(TraitTone tone) {
    	this(tone,new CoffPreamble(tone));
    }
    public TbskModulator(TraitTone tone, Preamble preamble)
    {
        // """
        //     Args:
        //         tone
        //             特徴シンボルのパターンです。
        // """
        this._tone = tone;
        this._preamble = preamble;
        this._enc = new TraitBlockEncoder(tone);
    }
    public Iterable<Double> modulateAsBit(IPyIterator<Integer> src)
    {
        var ave_window_shift = Math.max((int)(this._tone.size() * 0.1), 2) / 2; //#検出用の平均フィルタは0.1*len(tone)//2だけずれてる。ここを直したらTraitBlockDecoderも直せ
        return TbskIterable.<Double>createInstance (new IterChain<Double>(
            this._preamble.getPreamble(),
            this._enc.setInput(new DiffBitEncoder(0, new BitStream(src, 1))),
            new Repeater<Double>(0., ave_window_shift)    //#demodulatorが平均値で補正してる関係で遅延分を足してる。
        ));
    }
    
    public Iterable<Double> modulateAsBit(Iterable<Integer> src)
    {
        //既にIPyIteratorを持っていたらそのまま使う。
    	@SuppressWarnings("unchecked")
		IPyIterator<Integer> s=(src instanceof IPyIterator)?(IPyIterator<Integer>)src:new PyIterator<Integer>(src);
        return this.modulateAsBit(s);
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


    public Iterable<Double> modulate(Iterable<Integer> src)
    {
        return this.modulate(src,8);        	
    }
    public Iterable<Double> modulate(Iterable<Integer> src, int bitwidth)
    {
        //既にIPyIteratorを持っていたらそのまま使う。
        return this.modulateAsBit(
            new BitsWidthFilter(bitwidth,1).setInput(new RoStream<Integer>(new PyIterator<Integer>(src)))
            );
    }
    public Iterable<Double> modulateAsByte(Iterable<Byte> src)
    {
        //既にIPyIteratorを持っていたらそのまま使う。
        return this.modulateAsBit(
            new BitsWidthFilter(8,1).setInput(new ByteStream(new ByteStream.ByteCastIter(Functions.<Byte>toPyIter(src))))
            );
    }
    public Iterable<Double> modulate(String src) throws UnsupportedEncodingException
    {
    	return this.modulate(src,"utf-8");
    }
    public Iterable<Double> modulate(String src,String encoding) throws UnsupportedEncodingException
    {
        return this.modulateAsBit(
            new BitsWidthFilter(8,1).setInput(new ByteStream(src,0,encoding))
            );
    }




}
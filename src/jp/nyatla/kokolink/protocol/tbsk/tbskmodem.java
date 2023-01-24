package jp.nyatla.kokolink.protocol.tbsk;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.function.Function;

import jp.nyatla.kokolink.compatibility.Functions;
import jp.nyatla.kokolink.compatibility.PyIterator;
import jp.nyatla.kokolink.compatibility.TbskIterable;
import jp.nyatla.kokolink.interfaces.IBitStream;
import jp.nyatla.kokolink.interfaces.IFilter;
import jp.nyatla.kokolink.interfaces.IRoStream;
import jp.nyatla.kokolink.filter.Bits2BytesFilter;
import jp.nyatla.kokolink.filter.Bits2StrFilter;
import jp.nyatla.kokolink.filter.BitsWidthFilter;
import jp.nyatla.kokolink.protocol.tbsk.toneblock.TraitTone;
import jp.nyatla.kokolink.protocol.tbsk.traitblockcoder.TraitBlockDecoder;
import jp.nyatla.kokolink.protocol.tbsk.traitblockcoder.TraitBlockEncoder;
import jp.nyatla.kokolink.protocol.tbsk.preamble.CoffPreamble;
import jp.nyatla.kokolink.protocol.tbsk.preamble.Preamble;
import jp.nyatla.kokolink.streams.BitStream;
import jp.nyatla.kokolink.streams.ByteStream;
import jp.nyatla.kokolink.streams.RoStream;
import jp.nyatla.kokolink.streams.rostreams.BasicRoStream;
import jp.nyatla.kokolink.types.Py__class__.IPyIterator;
import jp.nyatla.kokolink.types.Py__class__.IterChain;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.types.Py__class__.Repeater;
import jp.nyatla.kokolink.utils.AsyncMethod;
import jp.nyatla.kokolink.utils.recoverable.RecoverableException;
import jp.nyatla.kokolink.utils.recoverable.RecoverableStopIteration;

public class tbskmodem
{










    // """ TBSKの変調クラスです。
    //     プリアンブルを前置した後にビットパターンを置きます。
    // """
    public static class TbskModulator
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


    public static class TbskDemodulator
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




        public class AsyncDemodulateX<T> extends AsyncMethod<Iterable<T>>
        {


            public AsyncDemodulateX(TbskDemodulator parent, IPyIterator<Double> src, Function<TraitBlockDecoder,Iterable<T>> resultbuilder) 
            {
                this._tone_ticks = parent._tone.size();
                this._result = null;
                this._stream = (src instanceof IRoStream) ? (IRoStream<Double>) src : new RoStream<Double>(src);
                this._peak_offset = null;
                this._parent = parent;
                this._wsrex = null;
                this._co_step = 0;
                this._closed = false;
                this._resultbuilder = resultbuilder;

            }
            @Override
            public Iterable<T> getResult()
            {
                assert(this._co_step >= 4);
                return this._result;
            }


            @Override
            public void close()
            {
                if (!this._closed)
                {
                    try
                    {
                        if (this._wsrex != null)
                        {
                            this._wsrex.close();
                        }
                    }
                    finally
                    {
                        this._wsrex = null;
                        this._parent._asmethod_lock = false;
                        this._closed = true;
                    }

                }

            }
            private Function<TraitBlockDecoder, Iterable<T>> _resultbuilder;
            private boolean _closed;
            private int _tone_ticks;
            private CoffPreamble.WaitForSymbolAS _wsrex;
            private Integer _peak_offset;
            private IRoStream<Double> _stream;
            private TbskDemodulator _parent;
            private int _co_step;
            private Iterable<T> _result;
            @Override
            public boolean run()
            {
                //# print("run",self._co_step)
                assert(!this._closed);

                if (this._co_step == 0)
                {
                    try
                    {
                        this._peak_offset = this._parent._pa_detector.waitForSymbol(this._stream); //#現在地から同期ポイントまでの相対位置
                        assert(this._wsrex == null);
                        this._co_step = 2;
                    }
                    catch (RecoverableException rexp)
                    {
                        this._wsrex = rexp.detach();
                        this._co_step = 1;
                        return false;
                    }
                }
                if (this._co_step == 1)
                {
                    if (!this._wsrex.run())
                    {
                        return false;
                    }
                    else
                    {
                        this._peak_offset = this._wsrex.getResult();
                        this._wsrex = null;
                        this._co_step = 2;
                    }
                }
                if (this._co_step == 2)
                {
                    if (this._peak_offset == null)
                    {
                        this._result = null;
                        this.close();
                        this._co_step = 4;
                        return true;
                    }
                    //# print(self._peak_offset)
                    this._co_step = 3;
                }
                if (this._co_step == 3)
                {
                    try
                    {
                        assert(this._peak_offset != null);
                        //# print(">>",self._peak_offset+self._stream.pos)
                        this._stream.seek(this._tone_ticks + (int)this._peak_offset);// #同期シンボル末尾に移動
                        //# print(">>",stream.pos)
                        var tbd = new TraitBlockDecoder(this._tone_ticks);
                        this._result = this._resultbuilder.apply(tbd.setInput(this._stream));
                        this.close();
                        this._co_step = 4;
                        return true;

                    }
                    catch (RecoverableStopIteration e)
                    {
                        return false;

                    }
                    catch (PyStopIteration e)
                    {
                        this._result = null;
                        this.close();
                        this._co_step = 4;
                        return true;

                    }
                }
                throw new RuntimeException();

            }
        }

        private TraitTone _tone;
        private Preamble _pa_detector;
        private boolean _asmethod_lock;

        public TbskDemodulator(TraitTone tone) {
        	this(tone,null);
        }
        public TbskDemodulator(TraitTone tone, Preamble preamble)
        {
            this._tone = tone;
            this._pa_detector = preamble != null ? preamble : new CoffPreamble(tone, 1.0,4);
            this._asmethod_lock = false;
        }
        public class DemodulateAsBitAS extends AsyncDemodulateX<Integer>
        {
            public DemodulateAsBitAS(TbskDemodulator parent, IPyIterator<Double> src)
            {            	
                super(parent, src, arg -> TbskIterable.createInstance(arg));            	
            }
        }
        public class DemodulateAsIntAS extends AsyncDemodulateX<Integer>
        {
            public DemodulateAsIntAS(TbskDemodulator parent, IPyIterator<Double> src, int bitwidth)
            {
                super(parent, src, arg ->TbskIterable.createInstance(new BitsWidthFilter(1, bitwidth).setInput(arg)));
            }
        }


        //""" TBSK信号からビットを復元します。
        //    関数は信号を検知する迄制御を返しません。信号を検知せずにストリームが終了した場合はNoneを返します。
        //"""
        public Iterable<Integer> demodulateAsBit(IPyIterator<Double> src) throws RecoverableException
        {
            assert(!this._asmethod_lock);
            DemodulateAsBitAS asmethod = new DemodulateAsBitAS(this, src);
            if (asmethod.run())
            {
                return asmethod.getResult();
            }
            else
            {
                this._asmethod_lock = true;// #解放はAsyncDemodulateXのcloseで
                throw new RecoverableException(asmethod);
            }
        }
        public Iterable<Integer> demodulateAsBit(Iterable<Double> src) throws RecoverableException
        {
            return this.demodulateAsBit(Functions.<Double>toPyIter(src));
        }

        //    """ TBSK信号からnビットのint値配列を復元します。
        //        関数は信号を検知する迄制御を返しません。信号を検知せずにストリームが終了した場合はNoneを返します。
        //    """
        public Iterable<Integer> demodulateAsInt(IPyIterator<Double> src) throws RecoverableException{
        	return this.demodulateAsInt(src,8);
        }
        
        public Iterable<Integer> demodulateAsInt(IPyIterator<Double> src, int bitwidth) throws RecoverableException
        {
            assert(!this._asmethod_lock);
            DemodulateAsIntAS asmethod = new DemodulateAsIntAS(this, src, bitwidth);
            if (asmethod.run())
            {
                return asmethod.getResult();
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
            return this.demodulateAsInt(Functions.toPyIter(src), bitwidth);
        }
        public class DemodulateAsByteAS extends AsyncDemodulateX<Byte>
        {
            public DemodulateAsByteAS(TbskDemodulator parent, IPyIterator<Double> src) {
                super(parent, src, arg -> TbskIterable.createInstance(new Bits2BytesFilter(1).setInput(arg)));
            }
        }
        public class DemodulateAsStrAS extends AsyncDemodulateX<Character>
        {
            public DemodulateAsStrAS(TbskDemodulator parent, IPyIterator<Double> src)
            {
            	this(parent,src,"utf-8");
            }
            public DemodulateAsStrAS(TbskDemodulator parent, IPyIterator<Double> src, String encoding)
            {
                super(parent, src, arg ->  TbskIterable.createInstance(new Bits2StrFilter(1,encoding).setInput(arg)));
            }
        }
        public class DemodulateAsHexStrAS extends AsyncDemodulateX<String>
        {
            public DemodulateAsHexStrAS(TbskDemodulator parent, IPyIterator<Double> src) {
            	super(parent, src, arg -> TbskIterable.createInstance(new Bits2HexStrFilter(1).setInput(arg)));
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
                return asmethod.getResult();
            }
            else
            {
                this._asmethod_lock = true;// #解放はAsyncDemodulateXのcloseで
                throw new RecoverableException(asmethod);
            }
        }
        public Iterable<Byte> demodulateAsBytes(Iterable<Double> src) throws RecoverableException
        {
            return this.demodulateAsBytes(Functions.toPyIter(src));
        }
        public Iterable<Character> demodulateAsStr(Iterable<Double> src) throws RecoverableException{
        	return this.demodulateAsStr(src,"utf-8");
        }
        public Iterable<Character> demodulateAsStr(Iterable<Double> src, String encoding) throws RecoverableException
        {
            return this.demodulateAsStr(Functions.toPyIter(src), encoding);
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
                return asmethod.getResult();
            }
            else
            {
                this._asmethod_lock = true;// #解放はAsyncDemodulateXのcloseで
                throw new RecoverableException(asmethod);
            }
        }


        public Iterable<String> DemodulateAsHexStr(IPyIterator<Double> src) throws RecoverableException
        {
            assert(!this._asmethod_lock);
            DemodulateAsHexStrAS asmethod = new DemodulateAsHexStrAS(this, src);
            if (asmethod.run())
            {
                return asmethod.getResult();
            }
            else
            {
                this._asmethod_lock = true;// #解放はAsyncDemodulateXのcloseで
                throw new RecoverableException(asmethod);
            }
        }
        public Iterable<String> DemodulateAsHexStr(Iterable<Double> src) throws RecoverableException
        {
            return this.DemodulateAsHexStr(Functions.toPyIter(src));
        }

    }




}

package jp.nyatla.kokolink.protocol.tbsk.tbskmodem;

import java.util.function.Function;

import jp.nyatla.kokolink.compatibility.TbskIterable;
import jp.nyatla.kokolink.interfaces.IRoStream;
import jp.nyatla.kokolink.protocol.tbsk.preamble.CoffPreamble;
import jp.nyatla.kokolink.protocol.tbsk.preamble.Preamble;
import jp.nyatla.kokolink.protocol.tbsk.toneblock.TraitTone;
import jp.nyatla.kokolink.protocol.tbsk.traitblockcoder.TraitBlockDecoder;
import jp.nyatla.kokolink.streams.RoStream;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;
import jp.nyatla.kokolink.utils.AsyncMethod;
import jp.nyatla.kokolink.utils.recoverable.RecoverableException;
import jp.nyatla.kokolink.utils.recoverable.RecoverableStopIteration;

public class TbskDemodulator_impl
{




    public class AsyncDemodulateX<T> extends AsyncMethod<IPyIterator<T>>
    {


        public AsyncDemodulateX(TbskDemodulator_impl parent, IPyIterator<Double> src, Function<TraitBlockDecoder,IPyIterator<T>> resultbuilder) 
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
        public IPyIterator<T> getResult()
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
        private Function<TraitBlockDecoder, IPyIterator<T>> _resultbuilder;
        private boolean _closed;
        private int _tone_ticks;
        private CoffPreamble.WaitForSymbolAS _wsrex;
        private Integer _peak_offset;
        private IRoStream<Double> _stream;
        private TbskDemodulator_impl _parent;
        private int _co_step;
        private IPyIterator<T> _result;
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
    protected boolean _asmethod_lock;

    public TbskDemodulator_impl(TraitTone tone, Preamble preamble)
    {
        this._tone = tone;
        this._pa_detector = preamble != null ? preamble : new CoffPreamble(tone, 1.0,4);
        this._asmethod_lock = false;
    }
    public class DemodulateAsBitAS extends AsyncDemodulateX<Integer>
    {
        public DemodulateAsBitAS(TbskDemodulator_impl parent, IPyIterator<Double> src)
        {            	
            super(parent, src, arg -> arg);            	
        }
    }



    //""" TBSK信号からビットを復元します。
    //    関数は信号を検知する迄制御を返しません。信号を検知せずにストリームが終了した場合はNoneを返します。
    //"""
    public IPyIterator<Integer> demodulateAsBit(IPyIterator<Double> src) throws RecoverableException
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




}



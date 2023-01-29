package jp.nyatla.kokolink.protocol.tbsk.tbskmodem;


import jp.nyatla.kokolink.compatibility.TbskIterable;
import jp.nyatla.kokolink.interfaces.IBitStream;
import jp.nyatla.kokolink.interfaces.IRoStream;
import jp.nyatla.kokolink.protocol.tbsk.preamble.Preamble;
import jp.nyatla.kokolink.protocol.tbsk.toneblock.TraitTone;
import jp.nyatla.kokolink.protocol.tbsk.traitblockcoder.TraitBlockEncoder;
import jp.nyatla.kokolink.streams.BitStream;
import jp.nyatla.kokolink.streams.rostreams.BasicRoStream;
import jp.nyatla.kokolink.types.Py__class__.IterChain;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.types.Py__class__.Repeater;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;

// """ TBSKの変調クラスです。
//     プリアンブルを前置した後にビットパターンを置きます。
// """
public class TbskModulator_impl
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
    public TbskModulator_impl(TraitTone tone, Preamble preamble)
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
    public IPyIterator<Double> modulateAsBit(IPyIterator<Integer> src)
    {
        var ave_window_shift = Math.max((int)(this._tone.size() * 0.1), 2) / 2; //#検出用の平均フィルタは0.1*len(tone)//2だけずれてる。ここを直したらTraitBlockDecoderも直せ
        return new IterChain<Double>(
            this._preamble.getPreamble(),
            this._enc.setInput(new DiffBitEncoder(0, new BitStream(src, 1))),
            new Repeater<Double>(0., ave_window_shift)    //#demodulatorが平均値で補正してる関係で遅延分を足してる。
        );
    }
}
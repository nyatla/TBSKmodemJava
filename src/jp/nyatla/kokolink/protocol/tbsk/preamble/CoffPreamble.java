package jp.nyatla.kokolink.protocol.tbsk.preamble;

import java.util.ArrayList;
import java.util.Comparator;

import jp.nyatla.kokolink.compatibility.Functions;
import jp.nyatla.kokolink.interfaces.IRoStream;
import jp.nyatla.kokolink.protocol.tbsk.AlgorithmSwitch;
import jp.nyatla.kokolink.protocol.tbsk.toneblock.TraitTone;
import jp.nyatla.kokolink.utils.AsyncMethod;
import jp.nyatla.kokolink.utils.BufferedIterator;
import jp.nyatla.kokolink.utils.RingBuffer;
import jp.nyatla.kokolink.utils.math.AverageIterator;
import jp.nyatla.kokolink.utils.recoverable.RecoverableException;
import jp.nyatla.kokolink.utils.recoverable.RecoverableStopIteration;
import jp.nyatla.kokolink.protocol.tbsk.traitblockcoder.TraitBlockEncoder;
import jp.nyatla.kokolink.streams.BitStream;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;

// """ 台形反転信号プリアンブルです。
// """
public class CoffPreamble implements Preamble
{
    public class PreambleBits extends TraitBlockEncoder
    {
        public PreambleBits(TraitTone symbol,int cycle)
        {
        	super(symbol);
            var b = Functions.repeat(cycle, 1);// [1]*self._cycle;
            var c = new ArrayList<Integer>();
            //var c =[i % 2 for i in range(this._cycle)];
            for (var i = 0; i< cycle; i++)
            {
                c.add(i % 2);
            }
            //var d =[(1 + c[-1]) % 2, (1 + c[-1]) % 2, c[-1],];
            var last=c.get(c.size()-1);            
            var d = Functions.<Integer>toList((1 + last) % 2, (1 + last) % 2, last);
            //var w = new BitStream(Functions.Flatten(new int[] { 0, 1 }, b, new int[] { 1 }, c, d), bitwidth: 1);
            //var w2=Functions.ToEnumerable(w);
            //Console.WriteLine(w2.Count());
            this.setInput(
        		new BitStream(
    				Functions.<Integer>flatten(
						Functions.<Integer>toList(0, 1),
						b,
						Functions.<Integer>toList(1),
						c, d),
    				1)
        		);
            // return enc.SetInput(new BitStream([0, 1] + b +[1] + c + d, 1));
            // # return enc.setInput(BitStream([0,1]+[1,1]+[1]+[0,1]+[0,0,1],1))
            // # return enc.setInput(BitStream([0,1,1,1,1,0,1,0,0,1],1))
        }

    }
    @Override
    public int getNumberOfTicks() {
    	//2+cycle+1+cycle+3
    	int c=this._cycle;
        return (2+c+1+c+3)*this._symbol.size();
    }
	
	
    private double _threshold;
    private TraitTone _symbol;
    private int _cycle; //#平坦部分のTick数
    private boolean _asmethtod_lock;

    public final static double DEFAULT_TH=1.0;
    public final static int DEFAULT_CYCLE=4;
    


    public CoffPreamble(TraitTone tone, double threshold,int cycle){
        this._threshold=threshold;
        this._symbol=tone;
        this._cycle=cycle; //#平坦部分のTick数
        this._asmethtod_lock = false;
    }

    @Override
    public IRoStream<Double> getPreamble()
    {
    	return new PreambleBits(this._symbol,this._cycle);
    }

    public class WaitForSymbolAS extends AsyncMethod<Integer>{
        private CoffPreamble _parent;
        private BufferedIterator<Double> _cof;
        private AverageIterator _avi;
        private int _sample_width;
        private int _cofbuf_len;
        private int _symbol_ticks;
        private RingBuffer<Double> _rb;
        private double _gap;
        private int _nor;
        private double _pmax;
        private int _co_step;
        private Integer _result;
        private boolean _closed;

        public WaitForSymbolAS(CoffPreamble parent,IRoStream<Double> src)
        {
        	super();
            var symbol_ticks = parent._symbol.size();
            //#後で見直すから10シンボル位記録しておく。
            var cofbuf_len = symbol_ticks * (6 + parent._cycle * 2);
            //# cofbuf_len=symbol_ticks*10
            this._parent = parent;
            this._cof = new BufferedIterator<Double>(AlgorithmSwitch.createSelfCorrcoefIterator(symbol_ticks, src, symbol_ticks), cofbuf_len, 0.);
            this._avi = new AverageIterator(this._cof, symbol_ticks);
            var sample_width = parent._cycle + 1;
            //# rb=RingBuffer(symbol_ticks*3,0)
            this._sample_width = sample_width;
            this._cofbuf_len = cofbuf_len;
            this._symbol_ticks = symbol_ticks;
            this._rb = new RingBuffer<Double>(symbol_ticks * sample_width, 0.);
            this._gap = 0; //#gap
            this._nor = 0; //#ストリームから読みだしたTick数
            //this._pmax;
            this._co_step = 0;
            this._result = null;
            this._closed = false;

        }
        @Override
        public Integer getResult()
        {
            assert(this._co_step >= 4);
            return this._result;
        }
        @Override
        public void close()
        {
            if (!this._closed)
            {
                this._parent._asmethtod_lock = false;
                this._closed = true;
            }
        }
        private class PcTuple{
        	final public int pos;
        	final public double cof;
        	public PcTuple(int p,double c) {
        		this.pos=p;
        		this.cof=c;
        	}
        }
        public boolean run()
        {
            assert(!this._closed);
            //# print("wait",self._co_step)
            if (this._closed)
            {
                return true;
            }
            //# ローカル変数の生成
            var avi = this._avi;
            var cof = this._cof;
            var rb = this._rb;
            try
            {
                while (true)
                {
                    //# ギャップ探索
                    if (this._co_step == 0)
                    {
                        this._gap = 0;
                        this._co_step = 1;
                    }
                    //# ASync #1
                    if (this._co_step == 1)
                    {
                        while (true)
                        {
                            try
                            {
                                rb.append(avi.next());
                                //# print(rb.tail)
                                this._nor = this._nor + 1;
                                this._gap = rb.getTop() - rb.getTail();
                                if (this._gap < 0.5)
                                {
                                    continue;
                                }
                                if (rb.getTop() < 0.1)
                                {
                                    continue;
                                }
                                if (rb.getTail() > -0.1)
                                {
                                    continue;
                                }
                                break;
                            }
                            catch (RecoverableStopIteration e)
                            {
                                return false;
                            }
                        }
                        this._co_step = 2; //#Co進行
                    }
                    if (this._co_step == 2)
                    {
                        //# print(1,self._nor,rb.tail,rb.top,self._gap)
                        //# ギャップ最大化
                        while (true)
                        {
                            try
                            {
                                rb.append(avi.next());
                                this._nor = this._nor + 1;
                                var w = rb.getTop() - rb.getTail();
                                if (w >= this._gap)
                                {
                                    //# print(w,self._gap)
                                    this._gap = w;
                                    continue;
                                }
                                break;
                            }
                            catch (RecoverableStopIteration e)
                            {
                                return false;
                            }
                        }
                        //# print(2,nor,rb.tail,rb.top,self._gap)
                        if (this._gap < this._parent._threshold)
                        {
                            this._co_step = 0;// #コルーチンをリセット
                            continue;
                        }
                        //# print(3,nor,rb.tail,rb.top,self._gap)
                        //# print(2,nor,self._gap)
                        this._pmax = rb.getTail();
                        this._co_step = 3;
                    }
                    if (this._co_step == 3)
                    {
                        //#同期シンボルピーク検出
                        while (true)
                        {
                            try
                            {
                                var n = avi.next();
                                this._nor = this._nor + 1;
                                if (n > this._pmax)
                                {
                                    this._pmax = n;
                                    continue;
                                }
                                if (this._pmax > 0.1)
                                {
                                    break;
                                }
                            }
                            catch (RecoverableStopIteration e)
                            {
                                return false;
                            }
                        }
                        this._co_step = 4; //#end
                        var symbol_ticks = this._symbol_ticks;
                        var sample_width = this._sample_width;
                        var cofbuf_len = this._cofbuf_len;
                        var cycle = this._parent._cycle;

                        //# print(4,self._nor,rb.tail,rb.top,self._gap)
                        //# print(3,self._nor)
                        //# #ピーク周辺の読出し
                        //# [next(cof) for _ in range(symbol_ticks//4)]
                        //# バッファリングしておいた相関値に3値平均フィルタ
                        var buf=Functions.toList(cof.getBuf().subIter(cof.getBuf().getLength() -symbol_ticks, symbol_ticks));                        

                        //var b =[(i + self._nor - symbol_ticks + 1, buf[i] + buf[i + 1] + buf[2]) for i in range(len(buf) - 2)];// #位置,相関値
                        var b = new ArrayList<PcTuple>();
                        for (var i = 0; i < buf.size() - 2; i++)
                        {
                            b.add(new PcTuple(i + this._nor - symbol_ticks + 1, buf.get(i) + buf.get(i + 1) + buf.get(i+2)));
                        }
//	                    b.sort(new IComparator (a, b) => a.Item2 == b.Item2 ? 0 : (a.Item2 < b.Item2 ? 1 : -1));
                        b.sort(new Comparator<PcTuple>(){
							@Override
							public int compare(PcTuple a, PcTuple b) {
								// TODO Auto-generated method stub
								return a.cof == b.cof ? 0 : (a.cof < b.cof ? 1 : -1);
							}});

                        //#ピークを基準に詳しく様子を見る。
                        var peak_pos = b.get(0).pos;//b[0][0];
                        //# print(peak_pos-symbol_ticks*3,(self._nor-(peak_pos+symbol_ticks*3)))
                        //# Lレベルシンボルの範囲を得る
                        //# s=peak_pos-symbol_ticks*3-(self._nor-cofbuf_len)
                        var s = peak_pos - symbol_ticks * sample_width - (this._nor - cofbuf_len);
                        var lw = Functions.toList(cof.getBuf().subIter(s, cycle * symbol_ticks));

//                        Array.Sort(lw);//cof.buf[s: s + cycle * symbol_ticks]
                        Functions.sort(lw);
                        //lw = lw.subList(0,lw.size() * 3 / 2 + 1); //lw[:len(lw) * 3 / 2 + 1];
                        if ( Functions.sum(lw) / lw.size() > lw.get(0) * 0.66)
                        {
                            //# print("ERR(L",peak_pos+src.pos,sum(lw)/len(lw),min(lw))
                            this._co_step = 0;//#co_step0からやり直す。
                            continue;// #バラツキ大きい
                        }
                        //#Hレベルシンボルの範囲を得る
                        //# s=peak_pos-symbol_ticks*6-(self._nor-cofbuf_len)
                        s = peak_pos - symbol_ticks * sample_width * 2 - (this._nor - cofbuf_len);
                        var lh = Functions.toList(cof.getBuf().subIter(s, cycle * symbol_ticks));

//                        Array.Sort(lh);
//                        Array.Reverse(lh);
                        Functions.sort(lh,true);
                        //lh = lh.subList(0,lh.size() * 3 / 2 + 1); //lh = lh[:len(lh) * 3 / 2 + 1]

                        if (Functions.sum(lh) / lh.size() < lh.get(0) * 0.66)
                        {
                            //# print("ERR(H",peak_pos+src.pos,sum(lh)/len(lh),max(lh))
                            this._co_step = 0;// #co_step0からやり直す。
                            continue; //#バラツキ大きい
                        }
                        //#値の高いのを抽出してピークとする。
                        //# print(peak_pos)
                        this._result = peak_pos - this._nor;//#現在値からの相対位置
                        this.close();
                        return true;
                    }
                    throw new Exception("Invalid co_step");
                }
            }
            catch (PyStopIteration e) {
                this._co_step = 4; //#end
                this.close();
                this._result = null;
                return true;
                //# print("END")
            } catch (Exception e) {
                this._co_step = 4; //#end
                this.close();
                throw new RuntimeException(e);
            }
        }
    }
    //""" 尖形のピーク座標を返します。座標は[0:-1],[1:1],[2:1],[3:-1]の[2:1]の末尾に同期します。
    //    値はマイナスの事もあります。
    //    @raise
    //        入力からRecoverableStopInterationを受信した場合、RecoverableExceptionを送出します。
    //        呼び出し元がこの関数を処理しない限り, 次の関数を呼び出すことはできません。
    //        終端に到達した場合は、Noneを返します。
    //"""
	@Override
    public Integer waitForSymbol(IRoStream<Double> src) throws RecoverableException
    {
        assert(!this._asmethtod_lock);
        var asmethtod = new WaitForSymbolAS(this, src);
        if (asmethtod.run())
        {
            return asmethtod.getResult();
        }
        else
        {
            //# ロックする（解放はASwaitForSymbolのclose内で。）
            this._asmethtod_lock = true;
            throw new RecoverableException(asmethtod);
        }
    }
}

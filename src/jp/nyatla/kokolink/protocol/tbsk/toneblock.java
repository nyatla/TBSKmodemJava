package jp.nyatla.kokolink.protocol.tbsk;

import java.util.ArrayList;

import jp.nyatla.kokolink.types.Py__class__.IPyIterator;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.utils.math.MSequence;
import jp.nyatla.kokolink.utils.math.XorShiftRand31;

public class toneblock
{
    static public class TraitTone extends ArrayList<Double>{
        /**
		 * 
		 */
		private static final long serialVersionUID = 1784727942928505888L;
		public TraitTone(Iterable<Double> d)
        {
        	super();
        	for(var i:d) {
        		this.add(i);
        	}
        }
        // """ 信号強度をv倍します。
        // """
        public TraitTone Mul(double v){
            for (var i = 0; i < this.size(); i++)
            {
                this.set(i,this.get(i) * v);
            }
            return this;
        }
    }

    // """ Sin波形のトーン信号です。
    //     このトーン信号を使用したTBSKはDPSKと同じです。
    // """
    static public class SinTone extends TraitTone{
        /**
		 * 
		 */
		private static final long serialVersionUID = -4237686593350268747L;
		static private Iterable<Double> _constructor_init(int points, int cycle)
        {
            var s = Math.PI * 2 / points * 0.5;
            var d1 = new ArrayList<Double>();
            for (var i = 0; i < points; i++)
            {
                d1.add((double)Math.sin(s + i * Math.PI * 2 / points));
            }
            var d2 = new ArrayList<Double>();
            for (var i = 0; i < cycle; i++)
            {
                d2.addAll(d1);
            }
            return d2;

        }
        public SinTone(int points)
        {
        	this(points,1);
        }
        public SinTone(int points,int cycle) {
        	super(SinTone._constructor_init(points,cycle));
        }
    }

    // """ トーン信号を巡回符号でBPSK変調した信号です。
    //     2^bits-1*len(base_tone)の長さです。
    // """
    static public class MSeqTone extends TraitTone{
        /**
		 * 
		 */
		private static final long serialVersionUID = 2756328812035355226L;
		private Iterable<Integer> _sequence;
        static private Iterable<Double> _constructor_init(MSequence mseq,TraitTone base_tone)
        {
            var tone = base_tone != null ? base_tone : new SinTone(20, 1);
            var a = new ArrayList<Double>();
            for (var i :mseq.genOneCycle())
            {
                for (var j : tone)
                {
                    a.add(j * (i * 2 - 1));
                }
            }
            return a;
        }
        public MSeqTone(MSequence mseq) {
        	this(mseq,null);
        }

        public MSeqTone(MSequence mseq, TraitTone base_tone) {
        	super(_constructor_init(mseq, base_tone));
            this._sequence = mseq.genOneCycle();
        };

        public MSeqTone(int bits,int tap) {
        	this(new MSequence(bits, tap),null);
        }
        public MSeqTone(int bits,int tap,TraitTone base_tone)
        {
            this(new MSequence(bits, tap), base_tone);
        }
        public Iterable<Integer> getSequence()
        {
            return this._sequence;
        }
    }

    static public class PnTone extends TraitTone{
        /**
		 * 
		 */
		private static final long serialVersionUID = 8218960567645922492L;
		static private Iterable<Double> _constructor_init(int seed, int interval, TraitTone base_tone)
        {
            var tone = base_tone != null ? base_tone : new SinTone(20, 8);
            var pn = new XorShiftRand31(seed, 29);
            var c = 0;
            int f=0;
            var d = new ArrayList<Double>();
            for (var i: tone)
            {
                if (c % interval == 0)
                {
                    f = (pn.next() & 0x02) - 1;
                }
                c = c + 1;
                d.add(i * f);
            }
            return d;
        }
        // """ トーン信号をPN符号でBPSK変調した信号です。
        //     intervalティック単位で変調します。
        // """
        public PnTone(int seed) {
        	this(seed, 2,null);
        }
        public PnTone(int seed,int interval,TraitTone base_tone) {
        	super(_constructor_init(seed, interval,base_tone));
        }
    }



    // """ Sin波形をXPSK変調したトーン信号です。
    //     1Tick単位でshiftイテレータの返す値×2pi/divの値だけ位相をずらします。
    // """
    static public class XPskSinTone extends TraitTone{
        /**
		 * 
		 */
		private static final long serialVersionUID = -3249838318502812915L;
		static class DefaultIter implements IPyIterator<Integer>
        {
            private XorShiftRand31 _pn;
            public DefaultIter()
            {
                this._pn = new XorShiftRand31(999, 299);
            }
            public Integer next()
            {
                return ((this._pn.next() & 0x01) * 2 - 1);
            }
        }
        // """
        //     Args:
        //     shift   -1,0,1の3値を返すイテレータです。省略時は乱数値です。
        // """

        static private Iterable<Double> _constructor_init(int points, int cycle, int div, IPyIterator<Integer> shift)
        {
            var delta = Math.PI * 2 / points;
            var lshift = (shift != null) ? shift:new DefaultIter();
            var s = delta * 0.5;
            var d = new ArrayList<Double>();
            for (var i = 0; i<points * cycle; i++)
            {
            	try {
                    s = s + delta + lshift.next() * (Math.PI * 2 / div);
            	}catch(PyStopIteration e) {
            		throw new RuntimeException(e);
            	}
                d.add(Math.sin(s));
            }
            return d;
        }
        public XPskSinTone(int points) {
        	this(points,1,8,null);
        }
        public XPskSinTone(int points,int cycle,int div, IPyIterator<Integer> shift) {
        	super(_constructor_init(points, cycle, div, shift));
        }
    }
}



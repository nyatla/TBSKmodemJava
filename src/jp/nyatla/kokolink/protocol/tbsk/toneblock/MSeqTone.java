package jp.nyatla.kokolink.protocol.tbsk.toneblock;

import java.util.ArrayList;

import jp.nyatla.kokolink.utils.math.MSequence;

// """ トーン信号を巡回符号でBPSK変調した信号です。
//     2^bits-1*len(base_tone)の長さです。
// """
public class MSeqTone extends TraitTone{
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
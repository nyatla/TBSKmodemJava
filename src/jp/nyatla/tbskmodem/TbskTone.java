package jp.nyatla.tbskmodem;

import jp.nyatla.kokolink.compatibility.Functions;
import jp.nyatla.kokolink.protocol.tbsk.toneblock.MSeqTone;
import jp.nyatla.kokolink.protocol.tbsk.toneblock.PnTone;
import jp.nyatla.kokolink.protocol.tbsk.toneblock.SinTone;
import jp.nyatla.kokolink.protocol.tbsk.toneblock.TraitTone;
import jp.nyatla.kokolink.protocol.tbsk.toneblock.XPskSinTone;
import jp.nyatla.kokolink.types.Py__class__.Repeater;
import jp.nyatla.kokolink.utils.math.MSequence;

public class TbskTone{
	public static SinTone createSin() {
		return createSin(10,10);
	}
	public static SinTone createSin(int points) {
		return createSin(points,1);
	}
	public static SinTone createSin(int points,int cycle) {
		return new SinTone(points,cycle);
	}

	public static XPskSinTone createXPskSin() {
    	return createXPskSin(10,10);
    }
	public static XPskSinTone createXPskSin(int points) {
    	return createXPskSin(points,1);
    }
    public static XPskSinTone createXPskSin(int points,int cycle)
    {
    	return new XPskSinTone(points,cycle,8,null);
    }	

    public static PnTone createPn(int seed,int interval,TraitTone base_tone) {
    	return new PnTone(seed,interval,base_tone);
    }	
    public static PnTone createPn(int seed,int interval,int length) {
    	return createPn(seed, interval,new TraitTone(Functions.toDoubleArray(new Repeater<Double>(1.0,length))));
    }
    public static PnTone createPn(int seed,int length) {
    	return createPn(seed, 1,new TraitTone(Functions.toDoubleArray(new Repeater<Double>(1.0,length))));
    }
    
    
    public static MSeqTone createMSeq(MSequence mseq) {
    	return new MSeqTone(mseq,null);
    }
    public static MSeqTone createMSeq(int bits,int tap) {
    	return createMSeq(new MSequence(bits, tap));
    }
    public static MSeqTone createMSeq(int bits,int tap,TraitTone base_tone)
    {
    	return new MSeqTone(new MSequence(bits, tap), base_tone);
    }    
    public static TraitTone createCustom(Iterable<Double> d)
    {
    	return new TraitTone(Functions.toDoubleArray(d));
    }    
    public static TraitTone createCustom(Double[] d)
    {
    	return new TraitTone(d);
    }    
    
}

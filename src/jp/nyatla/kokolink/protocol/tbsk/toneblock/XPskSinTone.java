package jp.nyatla.kokolink.protocol.tbsk.toneblock;

import java.util.ArrayList;

import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;
import jp.nyatla.kokolink.utils.math.XorShiftRand31;

// """ Sin波形をXPSK変調したトーン信号です。
//     1Tick単位でshiftイテレータの返す値×2pi/divの値だけ位相をずらします。
// """
public class XPskSinTone extends TraitTone{
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
    public XPskSinTone(int points,int cycle)
    {
    	this(points,cycle,8,null);
    }
    
    public XPskSinTone(int points,int cycle,int div, IPyIterator<Integer> shift) {
    	super(_constructor_init(points, cycle, div, shift));
    }
}
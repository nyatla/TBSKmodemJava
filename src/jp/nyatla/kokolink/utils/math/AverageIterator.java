package jp.nyatla.kokolink.utils.math;

import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;
import jp.nyatla.kokolink.utils.recoverable.RecoverableStopIteration;

// """ 末尾からticksまでの平均値を連続して返却します。
//     このイテレータはRecoverableStopInterationを利用できます。
// """
public class AverageIterator extends SumIterator{
    private int _length;
    public AverageIterator(IPyIterator<Double> src,int ticks)
    {
    	super(src,ticks);
        this._length=ticks;
    }
    public Double next() throws PyStopIteration{
        double r;
        try{
            r = super.next();
        }catch(RecoverableStopIteration e){
            throw e;
        }
        return r/this._length;
    }
}
package jp.nyatla.kokolink.utils;


import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;
import jp.nyatla.kokolink.utils.recoverable.BasicRecoverableIterator;

// """ ストリームの読み出し位置から過去N個の合計を返すイテレータです。
//     このイテレータはRecoverableStopInterationを利用できます。
// """
public class SumIterator extends BasicRecoverableIterator<Double>
{
    private IPyIterator<Double> _src;
    private double _sum;
    private RingBuffer<Double> _buf;

    // def __init__(self,src:Iterator[T],length:int):
    //     self._src=src
    //     self._buf=RingBuffer[T](length,0)
    //     self._sum=0
    //     # self._length=length
    //     # self._num_of_input=0
    //     self._gen=None
    //     return
    public SumIterator(IPyIterator<Double> src,int length){
        this._src=src;
        this._buf=new RingBuffer<Double>(length,0.);
        this._sum=0;
        // # self._length=length
        // # self._num_of_input=0
        // self._gen=None
        return;

    }

    // def __next__(self) -> T:
    //     try:
    //         s=next(self._src)
    //     except RecoverableStopIteration as e:
    //         raise RecoverableStopIteration(e)
    //     d=self._buf.append(s)
    //     self._sum=self._sum+s-d
    //     # self._num_of_input=self._num_of_input+1
    //     return self._sum
    @Override
    public Double next() throws PyStopIteration
    {
        var s=this._src.next();
        var d=this._buf.append(s);
        this._sum=this._sum+s-d;
        // # self._num_of_input=self._num_of_input+1
        return this._sum;
    }
    // @property
    // def buf(self)->RingBuffer[T]:
    //     return self._buf
    public RingBuffer<Double> getBuf(){
    	return this._buf;
    }

}





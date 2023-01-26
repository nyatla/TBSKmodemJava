package jp.nyatla.kokolink.utils;

import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;
import jp.nyatla.kokolink.utils.recoverable.BasicRecoverableIterator;
import jp.nyatla.kokolink.utils.recoverable.RecoverableStopIteration;

// """ 任意範囲のログを取りながら返すイテレータ
//     このイテレータはRecoverableStopInterationを利用できます。
// """
public class BufferedIterator<T> extends BasicRecoverableIterator<T>{
    private RingBuffer<T> _buf;
    private IPyIterator<T> _src;

    // def __init__(self,src:Iterator[T],size:int):
    //     self._src=src
    //     self._buf=RingBuffer(size,0)
    public BufferedIterator(IPyIterator<T> src,int size,T pad){
        this._src=src;
        this._buf=new RingBuffer<T>(size, pad);
    }
    @Override
    public T next() throws PyStopIteration{
        T d;
        try{
            d=this._src.next();
        }catch(RecoverableStopIteration e){
            throw e;
        }
        this._buf.append(d);
        return d;
    }
    // @property
    // def buf(self)->RingBuffer:
    //     return self._buf
    public RingBuffer<T> getBuf(){
        return this._buf;
    }
}


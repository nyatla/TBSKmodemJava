package jp.nyatla.kokolink.streams;

import jp.nyatla.kokolink.streams.rostreams.BasicRoStream;
import jp.nyatla.kokolink.types.Py__class__.IPyIterator;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.utils.recoverable.RecoverableStopIteration;

// """T型のRoStreamです。
// """
public class RoStream<T> extends BasicRoStream<T>{
    private IPyIterator<T> _src;
    private int _pos;
    public RoStream(IPyIterator<T> src)
    {
    	super();
        this._src=src;
        this._pos=0;
    }
    @Override
    public T next() throws PyStopIteration{
        T r;
        try{
            r= this._src.next(); //#RecoverableStopInterationを受け取っても問題ない。
        }catch(RecoverableStopIteration e){
            throw e;
        }
        this._pos+=1;
        return r;
    }
    @Override
    public long getPos(){
        return this._pos;
    }
}




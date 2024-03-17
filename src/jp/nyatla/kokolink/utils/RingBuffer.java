package jp.nyatla.kokolink.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;




public class RingBuffer<T>
{
    private List<T> _buf;
    private int _p;


    private static <T> List<T> _genIEnumerable(int length, T pad)
    {
    	var r=new ArrayList<T>(length);
    	for(var i=0;i<length;i++) {
    		r.add(pad);
    	}
        return r;
    }


    public RingBuffer(int length, T pad)
    {
        this(RingBuffer.<T>_genIEnumerable(length, pad));
    }
    public RingBuffer(List<T> buf){
        assert(buf!=null);
        this._buf=buf;
        this._p=0;
    }

    public T append(T v){
        var length=this._buf.size();
        var ret=this._buf.get(this._p);
        this._buf.set(this._p,v);
        this._p=(this._p+1)%length;
        return ret;

    }
    public void extend(Collection<? extends T> v){
        for (T i : v){
            this.append(i);
        }
    }
    public IPyIterator<T> subIter(int pos ,int size)
    {
    	class Iter implements IPyIterator<T>
    	{
    		private int _pos;
    		private int _size;
    		private List<T> _buf;
    		public Iter(List<T> buf,int pos,int size)
    		{
    			this._buf=buf;
    			this._pos=pos;
    			this._size=size;
    		}
			@Override
			public T next() throws PyStopIteration {
				if(this._size==0) {
					throw new PyStopIteration();
				}
                this._size = this._size - 1;
                int p = this._pos;
                this._pos = (this._pos + 1) % this._buf.size();
                return this._buf.get(p);
			}
    	};
    	return new Iter(this._buf,(this._p+pos)%this.getLength(),size);
    }

    // @property
    // def tail(self)->T:
    //     """ バッファの末尾 もっとも新しい要素"""
    //     length=len(self._buf)
    //     return self._buf[(self._p-1+length)%length]
    public T getTail()
    {
        // """ バッファの末尾 もっとも新しい要素"""
        var length=this._buf.size();
        return this._buf.get((this._p-1+length)%length);
    }
    // @property
    // def top(self)->T:
    //     """ バッファの先頭 最も古い要素"""
    //     return self._buf[self._p]
    public T getTop()
    {
        // """ バッファの先頭 最も古い要素"""
        return this._buf.get(this._p);
    }



    public T get(int s)
    {
        var b=this._buf;
        if(this._p!=0){
            var l=b.size();
            var b2=new ArrayList<T>(l);
            for(var i=0;i<l;i++){
                b2.set(i,b.get((i+this._p)%l));//                b2[i]=b[(i+this._p)%l];
            }
            this._buf=b2;
        }
        this._p=0;
        return this._buf.get(s);
    }    

    // def __len__(self)->int:
    //     return len(self._buf)
    public int getLength()
    {
        return this._buf.size();
    }
}

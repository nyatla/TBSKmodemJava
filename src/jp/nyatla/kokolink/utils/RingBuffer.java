package jp.nyatla.kokolink.utils;

import java.util.ArrayList;
import java.util.List;




public class RingBuffer<T>
{
    private List<T> _buf;
    private int _p;


    private static <T> List<T> _genIEnumerable(int length, T pad)
    {
    	var r=new ArrayList<T>(length);
    	for(var i=0;i<length;i++) {
    		r.set(i, pad);
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
    public void extend(Iterable<T> v){
        for (T i : v){
            this.append(i);
        }
    }
    /** リストの一部を切り取って返します。
        この関数はバッファの再配置を行いません。
    */    
    public List<T> sublist(int pos,int size){
        var l=this._buf.size();
        if(pos>=0){
            var p=this._p+pos;
            if(size>=0){
                assert(pos+size<=l);
                var ret=new ArrayList<T>(size);
                for(var i=0;i<size;i++){
                    ret.set(i,this._buf.get((p+i)%l));
//                    ret[i]=this._buf[(p+i)%l];
                }
                return ret;
            }else{
                assert(pos+size+1>=0);
                var ret=new ArrayList<T>(-size);
                // return tuple([self._buf[(p+size+i+1)%l] for i in range(-size)])
                for(var i=0;i<-size;i++){
                    ret.set(i,this._buf.get((p+size+i+1)%l));
//                    ret[i]=this._buf[(p+size+i+1)%l];
                }
                return ret;
            }
        }else{
            var p=this._p+l+pos;
            if(size>=0){
                assert(l+pos+size<=l);
                // return tuple([self._buf[(p+i)%l] for i in range(size)])
                var ret=new ArrayList<T>(size);
                for(var i=0;i<size;i++){
                    ret.set(i,this._buf.get((p+i)%l));
//                    ret[i]=this._buf[(p+i)%l];
                }
                return ret;
            }else{
                assert(l+pos+size+1>=0);
                // return tuple([self._buf[(p-i+l)%l] for i in range(-size)])
                var ret=new ArrayList<T>(size);
                for(var i=0;i<-size;i++){
                    ret.set(i,this._buf.get((p-i+l)%l));
//                    ret[i]=this._buf[(p-i+l)%l];
                }
                return ret;
            }
        }
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

    // def __getitem__(self,s)->List[T]:
    //     """ 通常のリストにして返します。
    //         必要に応じて再配置します。再配置せずに取り出す場合はsublistを使用します。
    //     """
    //     b=self._buf
    //     if self._p!=0:
    //         self._buf= b[self._p:]+b[:self._p]
    //     self._p=0
    //     return self._buf[s]
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

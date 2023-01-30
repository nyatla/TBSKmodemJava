package jp.nyatla.kokolink.streams;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import jp.nyatla.kokolink.interfaces.IRoStream;
import jp.nyatla.kokolink.types.Py__class__.BasicIterator;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.utils.recoverable.RecoverableStopIteration;

//import java.util.ArrayDeque;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Queue;
//
//import jp.nyatla.kokolink.interfaces.IRoStream;
//import jp.nyatla.kokolink.types.Py__class__.BasicIterator;
//import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
//import jp.nyatla.kokolink.utils.recoverable.RecoverableStopIteration;
//
public class rostreams
{
	static abstract public class BasicRoStream<T> extends BasicIterator<T> implements IRoStream<T>
	{
	    private Queue<T> _savepoint;
	    // """ IRoStreamの基本実装です。
	    // __next__,posメソッドを実装することで機能します。seek,getsはgetをラップしてエミュレートします。
	    // __next__メソッドの中でself#_posを更新してください。
	    // """
	    public BasicRoStream()
	    {
	        this._savepoint=new ArrayDeque<T>();
	    }
	    public T get() throws InterruptedException, PyStopIteration{
	        if(this._savepoint.size()>0){
	            // #読出し済みのものがあったらそれを返す。
	            var r=this._savepoint.poll();
	            assert(r!=null);
	            // this._savepoint=self._savepoint[1:]
	            // if(this._savepoint.Length==0){
	            //     self._savepoint=null;
	            // }
	            return r;
	        }
	        return this.next();
	    }
	    public List<T> gets(int maxsize) throws PyStopIteration{
	    	return this.gets(maxsize,false);
	    }
	    @Override
	    public List<T> gets(int maxsize,boolean fillup) throws PyStopIteration{
	        var r=this._savepoint;
	        try{
	        	var len=maxsize-r.size();
	            for(var i=0;i<len;i++){
	                r.add(this.next());
	            }
	        }catch(RecoverableStopIteration e){
	            throw e;
	            // self._savepoint=r
	            // raise RecoverableStopIteration(e)
	        }catch(PyStopIteration e){
	            if(fillup || r.size()==0){
	                throw e;
	            }
	        }
	        assert(r.size()<=maxsize);
	        var ret=new ArrayList<T>();
	        while(r.size()>0){
	        	var w=r.poll();
	            assert(w!=null);
	            ret.add(w);
	        }
	        return ret;

	    }
	    @Override
	    public void seek(int size) throws PyStopIteration{
	        try{
	            this.gets(size,true);
	        }catch(RecoverableStopIteration e){
	            throw e;
	        }catch(PyStopIteration e){
	            throw e;
	        }
	        return;
	    }
	    abstract public long getPos();
	}
}


// class FlattenRoStream(BasicRoStream[T],Generic[T]):
//     """T型の再帰構造のIteratorを直列化するストリームです。
//     最上位以外にあるインレータは値の取得時に全て読み出されます。
//     T型はIterator/Iterable/Noneな要素ではないことが求められます。
//     """
//     def __init__(self,src:Union[Iterator[T],Iterable[T]]):
//         super().__init__()
//         self._pos=0
//         def toIterator(s):
//             if isinstance(s,Iterable):
//                 return iter(s)
//             else:
//                 return s
//         def rextends(s:Union[Iterator[T],Iterable[T]]):
//             while True:
//                 try:
//                     i=next(s)
//                 except RecoverableStopIteration:
//                     yield None
//                     continue
//                 except StopIteration:
//                     break
//                 if isinstance(i,(Iterable,Iterator)) and not isinstance(i,(str,bytes)):
//                     yield from rextends(toIterator(i))
//                     continue
//                 else:
//                     yield i
//                     continue

//         self._gen=rextends(toIterator(src))
//     def __next__(self):
//         r=next(self._gen)
//         if r is None:
//             raise RecoverableStopIteration()
//         self._pos+=1 #posのインクリメント
//         return r
//     @property
//     def pos(self):
//         return self._pos



// class PeekRoStream(BasicRoStream[T],Generic[T]):
//     """PeekableStreamをラップしてPeekを使ったRoStreamを生成します。
//     ラップしているストリームを途中で操作した場合、このインスタンスの取得値は影響を受けます。
//     """
//     def __init__(self,src:IPeekableStream):
//         self._src=src
//         self._pos=0
//     def __next__(self)->T:
//         try:
//             r=self._src.peek(self._pos)
//         except RecoverableStopIteration as e:
//             raise RecoverableStopIteration(e)
//         self._pos+=1
//         return r
//     def pos(self)->int:
//         return self._pos
        



        
//}















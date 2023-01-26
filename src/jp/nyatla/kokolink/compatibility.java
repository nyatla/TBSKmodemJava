package jp.nyatla.kokolink;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import jp.nyatla.kokolink.types.Py__class__.PyIterator;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;
import jp.nyatla.kokolink.utils.recoverable.RecoverableStopIteration;


public class compatibility
{

	static public interface Disposable{
		void dispose();
	}

	public static interface IBinaryReader{		
	    Byte[] readBytes(int size) throws IOException;
	    int readInt32LE() throws IOException;
	    int readInt16LE() throws IOException;
	}
	
	public static interface IBinaryWriter{
	    int writeBytes(List<Byte> buf) throws IOException;
		int writeBytes(byte[] buf) throws IOException;
	}
	static public class MemBuffer extends ArrayList<Byte>
	{
		private static final long serialVersionUID = 1L;
		public int writeBytes(IBinaryReader s,int size) throws IOException {
			return this.writeBytes(s.readBytes(size));

		}
	
		private int _writeBytes(Byte[] s) {
			for(var i:s) {
				this.add(i);
			}
			return this.size()-(s.length);
		}		

		private int _writeBytes(byte[] s) {
			for(var i:s) {
				this.add(i);
			}
			return this.size()-(s.length);
		}		

		public int writeBytes(String s,int size) {
			try {
				var a = s.getBytes("US-ASCII");
				if(a.length!=size) {
					throw new RuntimeException();
				}
				return this._writeBytes(a);
			}catch(UnsupportedEncodingException e) {
				throw new RuntimeException();
				
			}
		}
		public int writeBytes(Byte[] s) {
			return this.writeBytes(s,0);
		}
		public int writeBytes(Byte[] s,int padding) {
			var r=this._writeBytes(s);
			var t=new Byte[] {0};
			for(var i=0;i<padding;i++) {
				this._writeBytes(t);
			}
			return r;
		}		
	    public int writeInt16LE(int v) {
	        var w=new Byte[]{
	        (byte)((v >> 0) & 0xff),
	        (byte)((v >> 8) & 0xff)
	        };
	        return this.writeBytes(w);
	    }
	    public int writeInt32LE(int v) {
	    	var w = new Byte[]{
	        (byte)((v >> 0) & 0xff),
	        (byte)((v >> 8) & 0xff),
	        (byte)((v >> 16) & 0xff),
	        (byte)((v >> 24) & 0xff)
	        };
	        return this.writeBytes(w);
	    };
	    public byte[] asBytes(int idx,int size) {
	    	var r=new byte[size];
	    	for(var i=0;i<size;i++) {
	    		r[i]=this.get(i+idx);
	    	}
	    	return r;
	    }
	    public String asStr(int idx,int size) {
	    	var s=this.subList(idx, size);
	    	byte[] b=new byte[s.size()];
	    	for(int i=0;i<s.size();i++){
	    		b[i]=s.get(i);
	    	}
	    	return new String(b);	    	
	    }
	    public int asInt32LE(int idx) {
	    	var w=this.subList(idx,idx+4).toArray(new Byte[0]);
	        return ((int)(0xff&w[3]) << 24) | ((int)(0xff&w[2]) << 16) | ((int)(0xff&w[1]) << 8) | (0xff&w[0]);
	    }
	    public int asInt16LE(int idx){
	    	var w=this.subList(idx,idx+2).toArray(new Byte[0]);
	        return ((int)((0xff&w[1]) << 8) | (0xff&w[0]));
	    }	    
		public int dump(IBinaryWriter dest) throws IOException {
			return dest.writeBytes(this);
		}
	}

	public static interface ITbskIterator<T> extends Iterator<T>{
		/**
		 * hasNextがfalseの時に、継続使用の可能性を返します。
		 * @return
		 */
		public boolean isRrecoveable();
	}
	
//    //  IPyEnumeratorをソースにしたIEnumerator
//    //  MoveToはIpyIteratorの仕様を引き継いでRecoverableStopIterationをスローすることがあります。
    public static class PyIterSuorceIterator<T> implements ITbskIterator<T>
    {
        private IPyIterator<T> _src;
        private T _current;
        private boolean _is_recoverable;
        public PyIterSuorceIterator(IPyIterator<T> src)
        {
            assert(src instanceof Iterable); //Enumulableを持たないこと
            this._src = src;
            this._is_recoverable=false;
        }
        public boolean isRrecoveable()
        {
            if (this._src != null && this._current!=null) {
            	return this._is_recoverable;
            }else {
            	return false;
            }
        }
		@Override
		public boolean hasNext()
		{
            try
            {
                var c= this._src.next();
                if (c == null)
                {
                    throw new RuntimeException();
                }
                this._current = c;
                return true;
            }
            catch (RecoverableStopIteration e)
            {
            	this._is_recoverable=true;
            	return false;
            }
            catch (PyStopIteration e)
            {
                this._is_recoverable=false;
                return false;
            }
        }
		@Override
		public T next() {
          if (this._src != null && this._current!=null)
          {
              return this._current;
          }
          throw new RuntimeException();
		}        
    }

	/**
	 * 常に同じイテラブルを提供します。
	 */
	public static class TbskIterable<T> implements Iterable<T>{
		private ITbskIterator<T> _iter;
		TbskIterable(ITbskIterator<T> src){
			this._iter=src;
		}
		@Override
		public ITbskIterator<T> iterator()
		{
			return this._iter;
		}
		public static <T> TbskIterable<T> createInstance(IPyIterator<T> src) {
			return new TbskIterable<T>(new PyIterSuorceIterator<T>(src));
		}		
	}




    public static byte[] toPrimitiveByteArray(List<Byte> s) {
    	var r=new byte[s.size()];
    	for(int i=0;i<s.size();i++) {
    		r[i]=s.get(i);
    	}
    	return r;
    }    
    public static double[] toPrimitiveDoubleArray(List<Double> s) {
    	var r=new double[s.size()];
    	for(int i=0;i<s.size();i++) {
    		r[i]=s.get(i);
    	}
    	return r;
    }    

    public static byte[] toPrimitiveArray(Byte[] s) {
    	var r=new byte[s.length];
    	for(int i=0;i<s.length;i++) {
    		r[i]=s[i];
    	}
    	return r;
    }
    public static Byte[] fromPrimitiveByteArray(byte[] s) {
    	var r=new Byte[s.length];
    	for(int i=0;i<s.length;i++) {
    		r[i]=s[i];
    	}
    	return r;
    }
    public static double[] toPrimitiveArray(double[] s) {
    	var r=new double[s.length];
    	for(int i=0;i<s.length;i++) {
    		r[i]=s[i];
    	}
    	return r;
    }
    public static Double[] fromPrimitiveDoubleArray(double[] s) {
    	var r=new Double[s.length];
    	for(int i=0;i<s.length;i++) {
    		r[i]=s[i];
    	}
    	return r;
    }



    public static class Functions
    {
    	static public Double sum(Double[] a){
    		Double s=0.;
    		for(var i:a) {
    			s=s+i;
    		}
    		return s;
    	}
    	static public Double sum(Iterable<Double> a){
    		Double s=0.;
    		for(var i:a) {
    			s=s+i;
    		}
    		return s;
    	}
    	static public void sort(List<Double> a) {
    		Functions.sort(a,false);
    	}
    	static public void sort(List<Double> a,boolean reverse) {
    		if(reverse) {
    			a.sort(new Comparator<Double>(){
    				@Override
    				public int compare(Double a, Double b) {
    					if((double)a==(double)b) {
    						return 0;
    					}
    					return (a < b ? 1 : -1);
    				}});    			
    		}else {
    			a.sort(new Comparator<Double>(){
    				@Override
    				public int compare(Double a, Double b) {
    					if((double)a==(double)b) {
    						return 0;
    					}
    					return (a > b ? 1 : -1);
    				}});    			
    		}
        }



        @SuppressWarnings("unchecked")
		static public <T> IPyIterator<T> toPyIter(Iterable<T> s)
        {
            if ((s instanceof IPyIterator))
            {
                return (IPyIterator<T>) s;
            }
            return new PyIterator<T>(s);
        }


        @SafeVarargs
    	static public <T> List<T> toList(T ... s){
            List<T> d = new ArrayList<T>();
            for (T i : s)
            {
                d.add(i);
            }
            return d;    		
    	}
        @SafeVarargs
        static public <T> List<T> flatten(Collection<T>  ... s)
        {
            List<T> d = new ArrayList<T>();
            for (var i : s)
            {
                d.addAll(i);
            }
            return d;
        }
        static public <T> List<T> repeat(int n,T pad)
        {
            var r=new ArrayList<T>();
            for(var i=0;i<n;i++) {
            	r.add(i,pad);
            }
            return r;
        }



    }








}

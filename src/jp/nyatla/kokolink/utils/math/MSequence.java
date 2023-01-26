package jp.nyatla.kokolink.utils.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;

public class MSequence implements IPyIterator<Integer>
{
    private int _bits;
    private int _tap;
    private int _mask;
    private int _sr;
    // """M系列シーケンスを返すイテレータを生成します。
    // """
    // def __init__(self,bits:int,tap:int=None,sr=1):
    //     assert(bits<64)
    //     assert(bits>=2)
    //     assert((tap is None) or tap<bits)
    //     assert(sr!=0)
    //     self._bits=bits-1 #最大ビット位置
    //     self._tap=tap if tap is not None else bits-2 #タップビット位置
    //     self._mask=(2**bits-1)
    //     self._sr=sr&self._mask
    public MSequence(int bits){
    	this(bits,bits-2,1);
    }
    public MSequence(int bits,int tap){
    	this(bits,tap,1);
    }

    public MSequence(int bits,int tap,int sr){
        assert(bits<64);
        assert(bits>=2);
        assert(tap<bits);
        assert(sr!=0);
        this._bits=bits-1;// #最大ビット位置
        this._tap=tap;// #タップビット位置
        this._mask = (int)(Math.pow(2,bits) - 1);//(2**bits-1);
        this._sr=sr&this._mask;
    }
    public Integer next(){
        var b=this._bits;
        var t=this._tap;
        var sr=this._sr;
        // # type(sr)
        
        var m=(sr>>b);
        var n=(sr>>t);
        // # print(sr,(sr<<1) , (n^m),(sr<<1)+ (n^m))
        var bit=(n^m) & 1;
        sr=(sr<<1) | bit;
        this._sr=sr & this._mask;
        return (int)bit;
    }
    // def maxcycle(self):
    //     """最大周期を返します。
    //     """
    //     return 2**(self._bits+1)-1
    public int getMaxcycle()
    {
        return (int)(Math.pow(2,this._bits)+1)-1;
    }
    // @property
    // def gets(self,n:int):
    //     """n個の成分を返します。
    //     """
    //     return tuple([next(self) for i in range(n)])
    public List<Integer> gets(int n){
        var r=new ArrayList<Integer>();
        for(var i=0;i<n;i++){
            r.add(this.next());
        }
        return r;
    }
    // def getOneCycle(self):
    //     """1サイクル分のシーケンスを得ます
    //     """
    //     return self.gets(self.cycles())
    public List<Integer> genOneCycle(){
        return this.gets(this.getCycles());
    }
    // def cycles(self)->int:
    //     """M系列の周期を計測します。
    //     """
    //     old_sr=self._sr
    //     l=self.maxcycle
    //     b=[]
    //     b.append(next(self))
    //     mv=0
    //     for i in range(l+1):
    //         b.append(next(self))
    //         #チェック
    //         if b[0:i]==b[i:i*2]:                
    //             mv=i
    //         b.append(next(self))
    //     # print("".join([str(i)for i in b]))
    //     self._sr=old_sr
    //     return mv
    public int getCycles(){
        var old_sr=this._sr;
        var l= this.getMaxcycle();
        var b=new ArrayList<Integer>();
        b.add(this.next());
        var mv=0;
        for(var i=0;i<l+1;i++){
            b.add(this.next());
            // #チェック
            // if b[0:i]==b[i:i*2]:                
            //     mv=i
//            if(b.GetRange(0,i).Equals(b.GetRange(i,i))){
            if(b.subList(0,i).equals(b.subList(i,i+i))){
                mv=i;
            }
            b.add(this.next());
        }
        // # print("".join([str(i)for i in b]))
        this._sr=old_sr;
        return mv;
    }
    // def getCyclesMap(cls,bits)->Dict[int,int]:
    //     """bitsのサイクル一覧を返します。
    //     n番目の要素にtap=nのサイクル数が返ります。
    //     """
    //     r={}
    //     for i in range(bits-1):
    //         m=MSequence(bits,i)
    //         r[i]=m.cycles()
    //     return r        
    static public Map<Integer,Integer> getCycleMap(int bits){
        var r=new HashMap<Integer,Integer>();
        for(var i=0;i<bits-1;i++){
            var m=new MSequence(bits,i);
            r.put(i,m.getCycles());
        }
        return r;
    }
        




// if __name__ == '__main__':

//     """タップ位置の一覧を計算すりゅ
//     """
//     for bits in range(2,8):
//         m=MSequence.getCyclesMap(bits)
//         for tap in m:
//             s=MSequence(bits,tap)
//             print(bits,tap,m[tap],"".join([str(i)for i in s.gets(m[tap])]))

}

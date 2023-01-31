package jp.nyatla.kokolink.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * 微妙に壊れた文字を逐次確定するUTF-8デコーダ
 * @author nyatla
 *
 */
public class BrokenTextStreamDecoder
{
	final private CharsetDecoder _decoder;
	final static public int MAX_CHARACTOR_BYTES=8;
	final private byte[] _a=new byte[MAX_CHARACTOR_BYTES];
	private int _len=0;
	public BrokenTextStreamDecoder(String encoding){
		this._decoder=Charset.forName(encoding).newDecoder();
	}    	
	public Character _decode(int length)
	{
        try
        {
        	var bb=ByteBuffer.wrap(this._a,0,length);
            CharBuffer cb=this._decoder.decode(bb);
            return cb.charAt(0);
        } catch (CharacterCodingException e) {
        	return null;
		}
	}
	/**
	 * 解析キューの先頭からsizeバイト取り除きます。
	 */
	public void shift(int size)
	{
		assert(size>0);
		if(this._len==0) {
			return;
		}
        var a=this._a;
        for(var i=size;i<a.length;i++) {
        	a[i-size]=a[i];
        }
        this._len-=size;
        return;
	}
	/**
	 * 解析キューを変更せずに、先頭から1バイトを読み出します。
	 * @return
	 * 読みだした値。値がないときはnull
	 */
	public Byte peekFront(){
		if(this._len>0) {
			return this._a[0];
		}
		return null;		
	}


	/**
	 * バッファの先頭から文字コードを構成する文字数を返す。
	 * @return
	 * 0の場合文字コードは存在しない。-1の場合、文字コードは存在しない。
	 */
	public int test()
	{
		if(this._len==0) {
			return 0;
		}
		for(var i=1;i<=this._len;i++) {
			var r=this._decode(i);
			if(r!=null) {
				return i; 
			}
		}
		return -1;
	}
	/**
	 * バッファに文字を追記してから、先頭から文字コードを構成する文字数を返す。
	 * @return
	 * 0の場合文字コードは存在しない。-1の場合、文字コードは存在しない。
	 */	
	public int test(byte d)
	{
		var a=this._a;
		//シフト
		if(this._len>=a.length) {
			this.shift(1);
		}
		//追記
		a[this._len]=d;
		this._len=this._len+1;		
		//テスト
		return this.test();
	}

	/**
	 * 新規入力をともなうアップデート
	 * @param d
	 * @return
	 * 変換不能ならnull
	 */
	public Character update(byte d)
	{
		int len=this.test(d);
		switch(len) {
			case -1:
				if(this._len==this._a.length) {
					return '?';//キューがいっぱいならシフトが起きてる。
				}else {
					return null;
				}
			case 0:
				return null;
			default:
				Character r=this._decode(len);
				this.shift(len);
				return r;
		}
	}
	/**
	 * 新規入力のないアップデート
	 * @return
	 * 変換不能ならnull.nullなら終端。
	 */
	public Character update()
	{
		int len=this.test();
		switch(len){
			case -1:
				//文字コードが見つからない
				this.shift(1);
				return '?';
			case 0:
				return null;
			default:
				Character r=this._decode(len);
				this.shift(len);
				return r;
		}
	}
	/**
	 * 解析バッファのデータサイズを返します。
	 * @return
	 */
	public int holdLen() {
		return this._len;
	}
	public boolean isBufferFull() {
		return this._len==this._a.length;
	}
}
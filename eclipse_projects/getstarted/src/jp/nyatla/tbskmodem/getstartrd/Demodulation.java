package jp.nyatla.tbskmodem.getstartrd;

import java.io.FileInputStream;
import java.io.IOException;
import jp.nyatla.tbskmodem.TbskDemodulator;
import jp.nyatla.tbskmodem.TbskTone;
import jp.nyatla.kokolink.utils.recoverable.RecoverableException;
import jp.nyatla.kokolink.utils.wavefile.PcmData;
public class Demodulation {
	public static void main(String[] args)
	{
		var tone = TbskTone.createXPskSin(10, 10);
		var demod = new TbskDemodulator(tone);
		PcmData pcm = null;
        try (FileInputStream input= new FileInputStream("./modulate.wav")) {
        	pcm=PcmData.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
    		var ret=demod.demodulateAsBit(pcm.getDataAsDouble());
    		if (ret == null)
    		{
    			System.out.print("None");
    		}
    		else
    		{
    			int c=0;
    			for(var i:ret){
    				System.out.print(c);
    				System.out.print(":");
    				System.out.print(i);
    				System.out.println();
    				c=c+1;
    			}
    		}
        	
        }catch(RecoverableException e) {
        	
        }

	}
}

package jp.nyatla.tbskmodem.getstartrd;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import jp.nyatla.kokolink.protocol.tbsk.tbskmodem.TbskModulator;
import jp.nyatla.kokolink.protocol.tbsk.toneblock.XPskSinTone;
import jp.nyatla.kokolink.utils.wavefile.PcmData;
public class Modulation {
	public static void main(String[] args)
	{
		var tone = new XPskSinTone(10, 10).mul(0.5);
		//var tone = new SinTone(10, 10).Mul(0.5);
		var payload = new ArrayList<Integer>();
		for (int i = 0; i < 1; i++)
		{
			for(var j:new int[] { 0, 1, 0, 1, 0, 1, 0, 1 }) {
				payload.add(j);
			}
		}

		var carrier = 8000;
		var mod = new TbskModulator(tone);

		var src_pcm = new ArrayList<Double>();
		for(var i:mod.modulateAsBit(payload)){
			src_pcm.add(i);
		}

        try (FileOutputStream output= new FileOutputStream("./modulate.wav")) {
		    var pcm=new PcmData(src_pcm.toArray(new Double[0]),16, carrier);
		    PcmData.dump(pcm,output);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}

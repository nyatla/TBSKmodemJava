# TBSK modem for Java


English documente 👉[README.md](README.md)


Javaで実装したTBSKmodemです。Python版の同等のAPIを備えています。

🐓[TBSKmodem](https://github.com/nyatla/TBSKmodem)

APIは概ねPythonと同一です。オーディオインタフェイスは未実装です。



# ライセンス

本ソフトウェアは、MITライセンスで提供します。ホビー・研究用途では、MITライセンスに従って適切に運用してください。

産業用途では、特許の取り扱いに注意してください。

このライブラリはMITライセンスのオープンソースソフトウェアですが、特許フリーではありません。



# GetStarted

VisualStadioで作成したSolutionがあります。

## ソースコードのセットアップ
サンプルを含めたソースコードは、githubからcloneします。

```
>git clone https://github.com/nyatla/TBSKmodemJava.git
```


## サンプルプログラム

Eclipse用のサンプルプログラムがあります。

### データをwaveファイルに変換
バイナリデータを再生可能な音声信号に変換します。
https://github.com/nyatla/TBSKmodemJava/blob/master/eclipse_projects/getstarted/src/jp/nyatla/tbskmodem/getstartrd/Modulation.java

### wavファイルから復調
wavファイルからデータを取り出します。
https://github.com/nyatla/TBSKmodemJava/blob/master/eclipse_projects/getstarted/src/jp/nyatla/tbskmodem/getstartrd/Demodulation.java


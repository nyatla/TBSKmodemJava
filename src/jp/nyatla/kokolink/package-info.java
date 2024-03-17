package jp.nyatla.kokolink;
/**
 * kokolinkパッケージはPython実装のTbskmodemを出来るだけ少ない実装でポートする目的があります。
 * 以下の点に留意してください。
 * 
 * ・公開APIの引数にItrable/Iteratorを使用しない。
 * ・Javaフレンドリクラスはtbskmodemに記述する。
 * 
 * kokolinkパッケージはPyIteratorを基盤として構築されています。混乱を防止するため、IPyIteratorを使用してください。
 * 
 * Javaフレンドリなクラス群は、tbskmodemパッケージに記述します。ここには、kokolink配下のクラスのうち、利用率の高い機能を
 * ラップして宣言します。前述のIPyIterator関数は、Iterable/Iteratorの相互変換を使用して関数を再定義し、隠蔽してください。
 * 
 * Itetable/Iteratorはcompatibility.javaにのみ存在するようにしてください。
 *
 * 
 * 
 */

/**
 * Namespace: AIT
 */
AIT = {};

/**
 * Relays an array from an external window/iframe context to
 * make sure that instanceof checks work
 */
AIT.relayArray = function(o) {
	var a = new Array();
	for(var i = 0, m = o.length; i < m; i++){
		a[i] = o[i];
	}
	return a;
}
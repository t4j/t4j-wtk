jQuery.noConflict();

jQuery(document).ready(function() {

	jQuery(document).on('mouseenter', '.t4j-top-menu-item, .t4j-table .v-table-body tr, .t4j-tree-table .v-table-body tr', function(event) {

		jQuery(this).addClass('highlighted');
	});

	jQuery(document).on('mouseleave', '.t4j-top-menu-item, .t4j-table .v-table-body tr, .t4j-tree-table .v-table-body tr', function(event) {

		jQuery(this).removeClass('highlighted');
	});

	jQuery(document).on('mouseenter', '.t4j-left-menu-item', function(event) {

		jQuery(this).addClass('item-highlighted');
	});

	jQuery(document).on('mouseleave', '.t4j-left-menu-item', function(event) {

		jQuery(this).removeClass('item-highlighted');
	});

	jQuery(document).on('mouseenter', '.t4j-tree .v-tree-node-caption', function(event) {

		jQuery(this).addClass('tree-node-highlighted');
	});

	jQuery(document).on('mouseleave', '.t4j-tree .v-tree-node-caption', function(event) {

		jQuery(this).removeClass('tree-node-highlighted');
	});

	jQuery(document).on('mouseenter', '.t4j-date-selection-dialog .day-button', function(event) {

		jQuery(this).addClass('day-button-highlighted');
	});

	jQuery(document).on('mouseleave', '.t4j-date-selection-dialog .day-button', function(event) {

		jQuery(this).removeClass('day-button-highlighted');
	});
});

function getUrlParameterValue(pName) {

	return getUrlParameterValue(pName, true);
}

function getUrlParameterValue(pName, unescape) {

	if (null == pName || undefined == pName || 0 == pName.length) {

		return null;
	}

	var urlQuery = window.parent.location.search;

	if (null == urlQuery || undefined == urlQuery || 0 == urlQuery.length) {

		urlQuery = window.parent.location.hash;
	}

	urlQuery = urlQuery.split("+").join(" ");

	var indexOf = urlQuery.indexOf("?");

	if (indexOf != -1) {

		urlQuery = urlQuery.substring(indexOf + 1);
	}

	var urlParameters = urlQuery.split("&");

	var methodResult = null;

	for (var i = 0; i < urlParameters.length; i++) {

		var urlParameter = urlParameters[i];

		var tmpArray = urlParameter.split("=");

		var name = tmpArray[0];
		var value = tmpArray[1];

		if (name == pName) {

			if (unescape) {
				methodResult = unescape(value);
			} else {
				methodResult = value;
			}

			break;
		}
	}

	return methodResult;
}

function setupLoginForm() {	
	
	setLoginFormTarget('login');
}

function setLoginFormTarget(eventType) {
	
	document.LoginForm.loginFormEventType.value = eventType;
	document.LoginForm.action = applicationUri;	
	
	if ("login" == eventType) {
		
		var pUserId = getUrlParameterValue('userId');
		
		if (null != pUserId) {
			
			document.LoginForm.username.value = pUserId;
		}
		
		var pFragment = getUrlParameterValue('fragment');
	
		if (null != pFragment) {
			
			document.LoginForm.fragment.value = pFragment;
		}
	}
	
	document.LoginForm.username.focus();
}

function submitLoginFormOnEnter(e) {
	
	var keycode = e.keyCode || e.which;
	
	if (keycode == 13) {
		
		submitLoginForm(e);
	}
}

function submitLoginForm(e) {
	
	encodeLoginFormFieldsBase64();
	document.LoginForm.submit();	
	return true;
}

function encodeLoginFormFieldsBase64() {
	
	var tmpValue = null;
	
	tmpValue = document.LoginForm.username.value;
	
	if(null == tmpValue || 0 == jQuery.trim(tmpValue).length) {		
		document.LoginForm.usernameBase64.value = '';
	} else {
		document.LoginForm.usernameBase64.value = Base64.encode(document.LoginForm.username.value);	
	}
	
	tmpValue = document.LoginForm.password.value;
	
	if(null == tmpValue || 0 == jQuery.trim(tmpValue).length) {		
		document.LoginForm.passwordBase64.value = '';
	} else {
		document.LoginForm.passwordBase64.value = Base64.encode(document.LoginForm.password.value);	
	}
	
	tmpValue = document.LoginForm.fragment.value;
	
	if(null == tmpValue || 0 == jQuery.trim(tmpValue).length) {		
		document.LoginForm.fragmentBase64.value = '';
	} else {
		document.LoginForm.fragmentBase64.value = Base64.encode(document.LoginForm.fragment.value);	
	}
}

/**
 * 
 * Base64 encode / decode http://www.webtoolkit.info/
 * 
 */
var Base64 = {

	// private property
	_keyStr : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",

	// public method for encoding
	encode : function(input) {

		var output = "";
		var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
		var i = 0;

		input = Base64._utf8_encode(input);

		while (i < input.length) {

			chr1 = input.charCodeAt(i++);
			chr2 = input.charCodeAt(i++);
			chr3 = input.charCodeAt(i++);

			enc1 = chr1 >> 2;
			enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
			enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
			enc4 = chr3 & 63;

			if (isNaN(chr2)) {
				enc3 = enc4 = 64;
			} else if (isNaN(chr3)) {
				enc4 = 64;
			}

			output = output + this._keyStr.charAt(enc1)
					+ this._keyStr.charAt(enc2) + this._keyStr.charAt(enc3)
					+ this._keyStr.charAt(enc4);

		}

		return output;
	},

	// public method for decoding
	decode : function(input) {

		var output = "";
		var chr1, chr2, chr3;
		var enc1, enc2, enc3, enc4;
		var i = 0;

		input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

		while (i < input.length) {

			enc1 = this._keyStr.indexOf(input.charAt(i++));
			enc2 = this._keyStr.indexOf(input.charAt(i++));
			enc3 = this._keyStr.indexOf(input.charAt(i++));
			enc4 = this._keyStr.indexOf(input.charAt(i++));

			chr1 = (enc1 << 2) | (enc2 >> 4);
			chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
			chr3 = ((enc3 & 3) << 6) | enc4;

			output = output + String.fromCharCode(chr1);

			if (enc3 != 64) {
				output = output + String.fromCharCode(chr2);
			}
			if (enc4 != 64) {
				output = output + String.fromCharCode(chr3);
			}

		}

		output = Base64._utf8_decode(output);

		return output;

	},

	// private method for UTF-8 encoding
	_utf8_encode : function(string) {

		string = string.replace(/\r\n/g, "\n");
		var utftext = "";

		for (var n = 0; n < string.length; n++) {

			var c = string.charCodeAt(n);

			if (c < 128) {
				utftext += String.fromCharCode(c);
			} else if ((c > 127) && (c < 2048)) {
				utftext += String.fromCharCode((c >> 6) | 192);
				utftext += String.fromCharCode((c & 63) | 128);
			} else {
				utftext += String.fromCharCode((c >> 12) | 224);
				utftext += String.fromCharCode(((c >> 6) & 63) | 128);
				utftext += String.fromCharCode((c & 63) | 128);
			}

		}

		return utftext;
	},

	// private method for UTF-8 decoding
	_utf8_decode : function(utftext) {

		var string = "";
		var i = 0;
		var c = c1 = c2 = 0;

		while (i < utftext.length) {

			c = utftext.charCodeAt(i);

			if (c < 128) {
				string += String.fromCharCode(c);
				i++;
			} else if ((c > 191) && (c < 224)) {
				c2 = utftext.charCodeAt(i + 1);
				string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
				i += 2;
			} else {
				c2 = utftext.charCodeAt(i + 1);
				c3 = utftext.charCodeAt(i + 2);
				string += String.fromCharCode(((c & 15) << 12)
						| ((c2 & 63) << 6) | (c3 & 63));
				i += 3;
			}

		}

		return string;
	}
};

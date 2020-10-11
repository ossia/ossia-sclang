/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

+ Boolean {

	*ossiaWsWrite {	|anOssiaParameter, ws|
		ws.writeOsc(anOssiaParameter.path, anOssiaParameter.value);
	}

	*ossiaSendMsg {	|anOssiaParameter, addr|
		addr.sendMsg(anOssiaParameter.path,
			if (anOssiaParameter.value) { $T } { $F });
	}

	*ossiaBounds { |mode|
		^{ |value, domain| value.asBoolean };
	}

	*ossiaNaNFilter { |newVal, oldval|
		^newVal;
	}

	*ossiaJson { ^"\"F\""; }

	*ossiaDefaultValue { ^false; }

	*ossiaWidget { |anOssiaParameter|
		var event = { | param |
			if (param.value != param.widgets.value) {
				{ param.widgets.value_(param.value); }.defer;
			};
		};

		StaticText(anOssiaParameter.window, 100@20).string_(anOssiaParameter.name)
		.stringColor_(OSSIA.pallette.color('baseText', 'active')).align_(\right);

		anOssiaParameter.widgets = Button(anOssiaParameter.window, 288@20).states_([
			["false",
				OSSIA.pallette.color('light', 'active'),
				OSSIA.pallette.color('middark', 'active')
			],
			["true",
				OSSIA.pallette.color('middark', 'active'),
				OSSIA.pallette.color('light', 'active')
			]
		]).action_({ | val | anOssiaParameter.value_(val.value); })
		.onClose_({ anOssiaParameter.removeDependant(event);
		}).focusColor_(OSSIA.pallette.color('midlight', 'active'));

		{ anOssiaParameter.widgets.value_(anOssiaParameter.value); }.defer;
	}
}
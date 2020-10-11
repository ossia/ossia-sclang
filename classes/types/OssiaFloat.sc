/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

+ Float {

	*ossiaWsWrite {	|anOssiaParameter, ws|
		ws.writeOsc(anOssiaParameter.path, anOssiaParameter.value);
	}

	*ossiaSendMsg {	|anOssiaParameter, addr|
		addr.sendRaw(([anOssiaParameter.path] ++ anOssiaParameter.value).asRawOSC);
	}

	*ossiaBounds { |mode|
		switch(mode,
			'free', {
				^{ |value, domain| value.asFloat };
			},
			'clip', {
				^{ |value, domain| value.clip(domain.min, domain.max).asFloat };
			},
			'low', {
				^{ |value, domain| value.max(domain.min).asFloat };
			},
			'high', {
				^{ |value, domain| value.min(domain.max).asFloat };
			},
			'wrap', {
				^{ |value, domain| value.wrap(domain.min, domain.max).asFloat };
			},
			'fold', {
				^{ |value, domain| value.fold(domain.min, domain.max).asFloat };
			}, {
				^{ |value, domain| domain[2].detect({ |item|
					item == value.asFloat });
				};
		});
	}

	*ossiaDefaultValue { ^0.0; }

	*ossiaNaNFilter { |newVal, oldval|
		if (newVal.isNil) { newVal }
		{ if (newVal.isNaN) { ^oldval } { ^newVal }};
	}

	*ossiaJson { ^"\"f\""; }

	*ossiaWidget { |anOssiaParameter|

		var event = { | param |
			{
				if (param.value != param.widgets.value) {
					param.widgets.value_(param.value);
				};
			}.defer;
		};

		anOssiaParameter.addDependant(event);

		anOssiaParameter.widgets = EZSlider(anOssiaParameter.window, 392@20, anOssiaParameter.name,
			action:{ | val | anOssiaParameter.value_(val.value); },
			labelWidth:100,
			initVal:anOssiaParameter.value,
			gap:4@0).onClose_({
			anOssiaParameter.removeDependant(event);
		}).setColors(
			stringColor:OSSIA.pallette.color('baseText', 'active'),
			sliderBackground:OSSIA.pallette.color('middark', 'active'),
			numNormalColor:OSSIA.pallette.color('windowText', 'active'),
			knobColor:OSSIA.pallette.color('light', 'active'));

		anOssiaParameter.widgets.sliderView.focusColor_(
			OSSIA.pallette.color('midlight', 'active'));

		anOssiaParameter.widgets.numberView.maxDecimals_(3)
		.step_(0.001).scroll_step_(0.001);

		if (anOssiaParameter.domain.min.notNil) {
			anOssiaParameter.widgets.controlSpec.minval_(anOssiaParameter.domain.min);
			anOssiaParameter.widgets.controlSpec.maxval_(anOssiaParameter.domain.max);
		};
	}
}
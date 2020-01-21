/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

+ Float {

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
		if (newVal.isNaN) { ^oldval } { ^newVal };
	}

	*ossiaWidget { |anOssiaParameter|
		var widgets;

		widgets = EZSlider(anOssiaParameter.window, 392@20, anOssiaParameter.name,
			action:{ | val | anOssiaParameter.value_(val.value); },
			initVal: anOssiaParameter.value, labelWidth:100, gap:0@0).onClose_({
			anOssiaParameter.removeFromEvenGui_(anOssiaParameter.name.asSymbol); });

		if(anOssiaParameter.domain.min.notNil) {
			widgets.controlSpec.minval_(anOssiaParameter.domain.min);
			widgets.controlSpec.maxval_(anOssiaParameter.domain.max);
		};

		anOssiaParameter.addToEvenGui_(
			name.asSymbol,
			{
				if (anOssiaParameter.value != widgets.value) {
					widgets.value_(anOssiaParameter.value);
				};
			};
		);
	}
}
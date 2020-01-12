/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

+ Integer {

	*ossiaSendMsg {	|anOssiaParameter, addr|
		addr.sendRaw(([anOssiaParameter.path] ++ anOssiaParameter.value).asRawOSC);
	}

	*ossiaBounds { |mode|
		switch(mode,
			'free', {
				^{ |value, domain| value.asInteger };
			},
			'clip', {
				^{ |value, domain| value.clip(domain.min, domain.max).asInteger };
			},
			'low', {
				^{ |value, domain| value.max(domain.min).asInteger };
			},
			'high', {
				^{ |value, domain| value.max(domain.min).asInteger };
			},
			'wrap', {
				^{ |value, domain| value.wrap(domain.min, domain.max).asInteger };
			},
			'fold', {
				^{ |value, domain| value.fold(domain.min, domain.max).asInteger }
			}, {
				^{ |value, domain| domain[2].do({ |item| if (item == value)
					{ value.asInteger }; });
				};
		});
	}

	*ossiaDefaultValue { ^0; }

	*ossiaWidget { |anOssiaParameter|
		var widgets;

		widgets = EZSlider(anOssiaParameter.window, 392@20, anOssiaParameter.name,
			action:{ | val | anOssiaParameter.value_(val.value); },
			initVal: anOssiaParameter.value, labelWidth:100, gap:0@0).onClose_({
			anOssiaParameter.removeFromEvenGui_(anOssiaParameter.name.asSymbol); });

		widgets.controlSpec.step_(1);

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
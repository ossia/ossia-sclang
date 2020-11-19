/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

+ OSSIA
{
	*initClass
	{
		palette = QPalette.auto(Color.fromHexString("#222222"), Color.fromHexString("#1d1c1a"));

		palette.setColor(Color.fromHexString("#c0c0c0c0"), 'windowText');
		palette.setColor(Color.fromHexString("#e0b01e"), 'buttonText'); // f0f0f0
		palette.setColor(Color.fromHexString("#161514"), 'base');
		palette.setColor(Color.fromHexString("#03C3DD"), 'alternateBase'); // blue interval "#1e1d1c"
		palette.setColor(Color.fromHexString("#161514"), 'toolTipBase');
		palette.setColor(Color.fromHexString("#c0c0c0c0"), 'toolTipText');
		palette.setColor(Color.fromHexString("#9062400a"), 'highlight');
		palette.setColor(Color.fromHexString("#FDFDFD"), 'highlightText');

		palette.setColor(Color.fromHexString("#e0b01e"), 'light'); // welow slider
		palette.setColor(Color.fromHexString("#62400a"), 'midlight'); // brown contour
		palette.setColor(Color.fromHexString("#363636"), 'middark'); // widget background
		palette.setColor(Color.fromHexString("#94FF00"), 'baseText'); // green param "#a7dd0d"
		palette.setColor(Color.fromHexString("#c58014"), 'brightText');
	}

	*makeDropDownGui
	{ | anOssiaParameter, win, layout |

		var event, widget, bounds, label;

		event = { | param |
			{
				var i = param.domain.values.detectIndex({ | item | item == param.value });
				widget.value_(i)
			}.defer;
		};

		anOssiaParameter.addDependant(event);

		if (layout == \minimal)
		{
			bounds = 100@20;
		} {
			bounds = (win.bounds.width - 6)@40;
			label = anOssiaParameter.name;
		};

		widget = EZPopUpMenu(
			parentView: win,
			bounds: bounds,
			label: label,
			items: anOssiaParameter.domain.values,
			globalAction: { | obj | anOssiaParameter.value_(obj.item) },
			layout: 'vert',
			gap: 2@0)
		.setColors(
			stringColor: win.asView.palette.color('baseText', 'active'),
			menuStringColor: win.asView.palette.color('light', 'active')
		);

		widget.onClose_({ anOssiaParameter.removeDependant(event);
			anOssiaParameter.removeClosed(win);
		});

		event.value(anOssiaParameter); // initialise

		^widget;
	}

	*makeNumberGui
	{ | anOssiaParameter, win, layout |

		var event, widget;

		event = { | param |
			{
				if (param.value != widget.value)
				{ widget.value_(param.value) };
			}.defer;
		};

		anOssiaParameter.addDependant(event);

		if (layout == \minimal)
		{
			widget = EZNumber(
				parent: win,
				bounds: 45@20)
			.setColors(
				numNormalColor: win.asView.palette.color('windowText', 'active')
			);
		} {
			widget = EZSlider(
				parent: win,
				bounds: (win.bounds.width - 6)@40,
				label: anOssiaParameter.name,
				layout: 'line2',
				gap: 2@0)
			.setColors(
				stringColor: win.asView.palette.color('baseText', 'active'),
				sliderBackground: win.asView.palette.color('middark', 'active'),
				numNormalColor: win.asView.palette.color('windowText', 'active')
			);

			widget.sliderView.focusColor_(
				win.asView.palette.color('midlight', 'active');
			);

			if (anOssiaParameter.unit.notNil)
			{
				if (anOssiaParameter.unit.string == "gain.decibel")
				{ widget.controlSpec.warp_(\db) };
			};
		};

		if (anOssiaParameter.domain.min.notNil)
		{
			widget.controlSpec.minval_(anOssiaParameter.domain.min);
			widget.controlSpec.maxval_(anOssiaParameter.domain.max);
		};

		// set GUI action and valued after min and max are set
		widget.action_({ | val | anOssiaParameter.value_(val.value) });
		{ widget.value_(anOssiaParameter.value) }.defer;

		widget.onClose_({ anOssiaParameter.removeDependant(event);
			anOssiaParameter.removeClosed(win);
		});

		^widget;
	}

	*makeButtonGui
	{ | anOssiaParameter, win |

		var widget = [
			StaticText(
				parent: win,
				bounds: (win.bounds.width - 6)@20)
			.string_(anOssiaParameter.name)
			.stringColor_(win.asView.palette.color('baseText', 'active')),
			Button(
				parent: win,
				bounds: (win.bounds.width - 6)@20)
			.focusColor_(win.asView.palette.color('midlight', 'active'))
		];

		^widget;
	}

	*makeTxtGui
	{ | anOssiaParameter, win, layout |

		var widget, event, bounds, label;

		event = { | param |
			{
				if (param.value != widget.value)
				{ widget.value_(param.value) };
			}.defer;
		};

		anOssiaParameter.addDependant(event);

		if (layout == \minimal)
		{
			bounds = 100@20;
		} {
			bounds = (win.bounds.width - 6)@40;
			label = anOssiaParameter.name;
		};

		widget = EZText(
			parent: win,
			bounds: bounds,
			label: label,
			action: { | val | anOssiaParameter.value_(val.value) },
			initVal: anOssiaParameter.value,
			labelWidth: 100,
			layout: 'vert',
			gap: 2@0)
		.setColors(
			stringColor: win.asView.palette.color('baseText', 'active'),
			textBackground: win.asView.palette.color('middark', 'active'),
			textStringColor: win.asView.palette.color('windowText', 'active'));

		widget.onClose_({ anOssiaParameter.removeDependant(event);
			anOssiaParameter.removeClosed(win);
		});

		^widget;
	}

	*fieldOrMenu
	{ | anOssiaParameter, win, layout |

		var widget;

		if (anOssiaParameter.domain.values == [])
		{
			widget = OSSIA.makeTxtGui(anOssiaParameter, win, layout);
		} {
			widget = OSSIA.makeDropDownGui(anOssiaParameter, win, layout);
		};

		// add EZ widgets as compositeViews since this is how they are stored in the parent view's children
		anOssiaParameter.widgets = anOssiaParameter.widgets ++ widget.view;
	}
}

+ EZGui
{
	parent { ^view.parent }
	isClosed { ^view.isClosed }
}

+ Array
{
	*ossiaWidget { | anOssiaParameter, win, layout | OSSIA.fieldOrMenu(anOssiaParameter, win, layout) }
}

+ Char
{
	*ossiaWidget { | anOssiaParameter, win, layout | OSSIA.fieldOrMenu(anOssiaParameter, win, layout) }
}

+ String
{
	*ossiaWidget { | anOssiaParameter, win, layout | OSSIA.fieldOrMenu(anOssiaParameter, win, layout) }
}

+ Impulse
{
	*ossiaWidget
	{ | anOssiaParameter, win, layout |

		var widget;

		if (layout == \minimal)
		{
			widget = Button(win, 20@20)
			.focusColor_(win.asView.palette.color('midlight', 'active'));

			widget.action_({ | check | anOssiaParameter.value_(check.value) });

			{ widget.value_(anOssiaParameter.value) }.defer;

			widget.onClose_({ anOssiaParameter.removeClosed(win) });
		} {
			widget = OSSIA.makeButtonGui(anOssiaParameter, win);

			// Impulse specific states and action
			widget[1].states_([
				["Pulse"]
			]).action_({ | val | anOssiaParameter.value_() });

			widget[1].onClose_({ widget[0].remove;
				anOssiaParameter.removeClosed(win);
			});
		};

		anOssiaParameter.widgets = anOssiaParameter.widgets ++ widget;
	}
}

+ Boolean
{
	*ossiaWidget
	{ | anOssiaParameter, win, layout |

		var widget, event;

		if (layout == \minimal)
		{
			event = { | param |
				{
					if (param.value != widget.value)
					{ widget.value_(param.value) };
				}.defer;
			};

			widget = CheckBox(win, 20@20);

			widget.action_({ | check | anOssiaParameter.value_(check.value) });

			{ widget.value_(anOssiaParameter.value) }.defer;

			widget.onClose_({ anOssiaParameter.removeDependant(event);
				anOssiaParameter.removeClosed(win);
			});
		} {
			event = { | param |
				{
					if (param.value != widget[1].value)
					{ widget[1].value_(param.value) };
				}.defer;
			};

			widget = OSSIA.makeButtonGui(anOssiaParameter, win);

			// Boolean specific states, actions and initial value
			widget[1].states_(
				[
					[
						"false",
						win.asView.palette.color('light', 'active'),
						win.asView.palette.color('middark', 'active')
					],
					[
						"true",
						win.asView.palette.color('middark', 'active'),
						win.asView.palette.color('light', 'active')
					]
				]
			).action_({ | val | anOssiaParameter.value_(val.value) });

			{ widget[1].value_(anOssiaParameter.value) }.defer;

			widget[1].onClose_({ anOssiaParameter.removeDependant(event);
				widget[0].remove;
				anOssiaParameter.removeClosed(win);
			});
		};

		anOssiaParameter.addDependant(event);

		anOssiaParameter.widgets = anOssiaParameter.widgets ++ widget;
	}
}

+ Integer
{
	*ossiaWidget
	{ | anOssiaParameter, win, layout |

		var widget;

		if (anOssiaParameter.domain.values == [])
		{
			widget = OSSIA.makeNumberGui(anOssiaParameter, win, layout);

			// Integer specific decimals and steps
			widget.numberView.maxDecimals_(0)
			.step_(1).scroll_step_(1);
		} {
			widget = OSSIA.makeDropDownGui(anOssiaParameter, win, layout);
		};

		// add EZ widgets as compositeViews since this is how they are stored in the parent view's children
		anOssiaParameter.widgets = anOssiaParameter.widgets ++ widget.view;
	}
}

+ Float
{
	*ossiaWidget
	{ | anOssiaParameter, win, layout |

		var widget;

		if (anOssiaParameter.domain.values == [])
		{
			widget = OSSIA.makeNumberGui(anOssiaParameter, win, layout);

			// float specific decimals and steps
			widget.numberView.maxDecimals_(3)
			.step_(0.001).scroll_step_(0.001);
		} {
			widget = OSSIA.makeDropDownGui(anOssiaParameter, win, layout);
		};

		// add EZ widgets as compositeViews since this is how they are stored in the parent view's children
		anOssiaParameter.widgets = anOssiaParameter.widgets ++ widget.view;
	}
}

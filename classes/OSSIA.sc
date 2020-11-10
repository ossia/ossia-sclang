/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

OSSIA_Base // base classe for OSSIA_Device and OSSIA_Node
{
	var <name;
	var <path;
	var <children;

	// overwriten by OSSIA_Device and OSSIA_Node
	parent { }

	addChild { | anOssiaNode | children = children.add(anOssiaNode) }

	explore
	{ | with_attributes = false, parameters_only = false |

		if (parameters_only)
		{
			^this.prParamExplore;
		} {
			^this.prNodeExplore;
		}
	}

	tree
	{ |with_attributes = false, parameters_only = false|

		var exp = this.explore(with_attributes, parameters_only);

		exp.do({ | item |

			var str = "";

			item.do({ | subitem | str = str + subitem });
			str.postln;
		});

	}

	snapshot
	{ |... exclude|

		var exp = this.explore(false, true);
		var res = [];

		exp.do({ | item |

			var unique = item[0].split($/).last ++ "_" ++ item[1];

			res = res.add(unique.asSymbol);
			res = res.add(item[2]);
		});

		if(exclude.notEmpty)
		{
			exclude.do({ | item |

				var index = res.indexOf(item.sym);

				2.do({ res.removeAt(index) });
			});
		};

		^res
	}

	find
	{ | nodePath |

		var array, string = this.prCheckPath(nodePath);

		array = this.prPath2Array(string);

		^this.findFromArray(array);
	}

	findFromArray
	{ | namesArray |

		if (namesArray.size == 1)
		{
			children.do({ | item |

				if (item.name == namesArray[0])
				{ ^item; }
			})
		} {
			children.do({ | item |

				if (item.name == namesArray[0])
				{
					namesArray.removeAt(0);
					^item.findFromArray(namesArray);
				}
			})
		};

		^namesArray.addFirst(this);
	}

	//-------------------------------------------//
	//              PRIVATE METHODS              //
	//-------------------------------------------//

	prHandlePath
	{ | nm |

		var curent_path = this.parent.find(nm);

		if (curent_path.class == Array)
		{
			var lasName, previousNode = curent_path[0];

			curent_path.removeAt(0);

			// preserve the last name in the string and remove it from the path
			lasName = curent_path.removeAt(curent_path.size - 1);

			curent_path.do({ | item, i |
				previousNode = OSSIA_Node(previousNode, item);
			});

			this.prCreateOrIncrement(previousNode, lasName);
		} {
			this.prCreateOrIncrement(curent_path, curent_path);
		}
	}

	prCheckPath
	{ | roughPath |

		var string;

		switch (roughPath.class,
			Symbol,
			{ string = roughPath.asString },
			String,
			{ string = roughPath },
			{ ^Error("nodePath must be of type String").throw }
		);

		^string;
	}

	prPath2Array
	{ | pathString |

		var splitedPath = pathString.split();

		// clean up first and laast $/ as they are not needed
		if (splitedPath.first == "") { splitedPath.removeAt(0) };
		if (splitedPath.last == "")
		{ splitedPath.removeAt(splitedPath.size - 1) };

		^splitedPath;
	}

	// overwriten by OSSIA_Device and OSSIA_Node
	prNodeExplore { }
	prParamExplore { }
	prCreateOrIncrement { }
}

	//-------------------------------------------//
	//               VIRTUAL CLASS               //
	//-------------------------------------------//

OSSIA
{
	classvar <palette, server;

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

	*stringify
	{ | ossiaNodes |

		var json = "";

		if (ossiaNodes.isArray)
		{
			ossiaNodes.do({ | item, count |
				json = json
				++ if (count == 0) {"{"} {","}
				++ item.json
			});

			json = json ++ "}";

		} {
			json = json ++ ossiaNodes.json;
		};

		^json;
	}

	*server  { if (server.isNil) { ^Server.default } { ^server } }
	*server_ { | target | server = target }

	*domain { | min, max, values | ^OSSIA_domain(min, max, values) }
	*access_mode { ^OSSIA_access_mode }
	*bounding_mode { ^OSSIA_bounding_mode }

	*vec2f { | v1 = 0.0, v2 = 0.0 | ^OSSIA_vec2f(v1, v2) }
	*vec3f { | v1 = 0.0, v2 = 0.0, v3 = 0.0 | ^OSSIA_vec3f(v1, v2, v3) }
	*vec4f { | v1 = 0.0, v2 = 0.0, v3 = 0.0, v4 = 0.0 | ^OSSIA_vec4f(v1, v2, v3, v4) }

	*device { | name | ^OSSIA_Device(name) }

	*node { | parent_node, name | ^OSSIA_Node(parent_node, name) }

	*parameter
	{ | parent_node, name, type, domain, default_value, bounding_mode = 'free',
		critical = false, repetition_filter = false |

		^OSSIA_Parameter(parent_node, name, type, domain, default_value,
			bounding_mode, critical, repetition_filter);
	}

	*parameter_array
	{ | size, parent_node, name, type, domain, default_value, bounding_mode = 'free',
		critical = false, repetition_filter = false |

		^OSSIA_Parameter.array(size, parent_node, name, type, domain, default_value,
			bounding_mode, critical, repetition_filter);
	}

	//-------------------------------------------//
	//                    GUI                    //
	//-------------------------------------------//

	*makeDropDownGui
	{ | anOssiaParameter, win, layout |

		var event, widget;

		event = { | param |
			{
				var i = param.domain.values.detectIndex({ | item | item == param.value });
				widget.value_(i)
			}.defer;
		};

		anOssiaParameter.addDependant(event);

		if (layout == \minimal)
		{
			widget = PopUpMenu(win, 100@20);

			widget.items_(anOssiaParameter.domain.values.collect({ | item | item.asSymbol }));

			widget.action_({ | obj | anOssiaParameter.value_(obj.item) });
		} {
			widget = EZPopUpMenu(
				parentView: win,
				bounds: (win.bounds.width - 6)@40,
				label: anOssiaParameter.name,
				items: anOssiaParameter.domain.values.collect({ | item | item.asSymbol }),
				globalAction: { | obj | anOssiaParameter.value_(obj.item) },
				layout: 'vert',
				gap: 2@0)
			.setColors(
				stringColor: win.asView.palette.color('baseText', 'active'),
				menuStringColor: win.asView.palette.color('light', 'active')
			);
		};

		widget.onClose_({ anOssiaParameter.removeDependant(event);
			anOssiaParameter.removeClosed();
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
			widget = NumberBox(win, 45@20)
			.action_({ | num | anOssiaParameter.value_(num.value) })
			.background_(win.asView.palette.color('base', 'active'))
			.normalColor_(win.asView.palette.color('windowText', 'active'));

			if (anOssiaParameter.domain.min.notNil)
			{
				widget.clipLo_(anOssiaParameter.domain.min);
				widget.clipHi_(anOssiaParameter.domain.max);
			};

			{ widget.value_(anOssiaParameter.value) }.defer;
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

			if (anOssiaParameter.domain.min.notNil)
			{
				widget.controlSpec.minval_(anOssiaParameter.domain.min);
				widget.controlSpec.maxval_(anOssiaParameter.domain.max);
			};

			if (anOssiaParameter.unit.notNil)
			{
				if (anOssiaParameter.unit.string == "gain.decibel")
				{ widget.controlSpec.warp_(\db) };
			};
		};

		// set GUI action and valued after min and max are set
		widget.action_({ | val | anOssiaParameter.value_(val.value) });
		{ widget.value_(anOssiaParameter.value) }.defer;

		widget.onClose_({ anOssiaParameter.removeDependant(event);
			anOssiaParameter.removeClosed();
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

		var widget, event;

		event = { | param |
			{
				if (param.value != widget.value)
				{ widget.value_(param.value) };
			}.defer;
		};

		anOssiaParameter.addDependant(event);

		if (layout == \minimal)
		{
			widget = TextField(win, 100@20);

			widget.action_({ | field | anOssiaParameter.value_(field.value) });

			{ widget.value_(anOssiaParameter.value) }.defer;
		} {
			widget = EZText(
				parent: win,
				bounds: (win.bounds.width - 6)@40,
				label: anOssiaParameter.name,
				action: { | val | anOssiaParameter.value_(val.value) },
				initVal: anOssiaParameter.value,
				labelWidth: 100,
				layout: 'vert',
				gap: 2@0)
			.setColors(
				stringColor: win.asView.palette.color('baseText', 'active'),
				textBackground: win.asView.palette.color('middark', 'active'),
				textStringColor: win.asView.palette.color('windowText', 'active'));
		};

		widget.onClose_({ anOssiaParameter.removeDependant(event);
			anOssiaParameter.removeClosed();
		});

		^widget;
	}
}

+ EZGui
{
	parent { ^view.parent }
	isClosed { ^view.isClosed }
}
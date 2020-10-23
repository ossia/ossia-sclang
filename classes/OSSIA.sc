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

	addChild { | anOssiaNode | children = children.add(anOssiaNode) }

	tree
	{ | with_attributes = false, parameters_only = false |

		if (parameters_only)
		{
			^this.paramExplore;
		} {
			^this.nodeExplore;
		}
	}

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

	find
	{ | nodePath |

		var array, string = this.prCheckPath(nodePath);

		array = this.prPath2Array(string);

		^this.findFromArray(array);
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

	// overwriten by OSSIA_Device and OSSIA_Node
	nodeExplore { }
	paramExplore { }
	parent { }
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
		palette = QPalette.auto(Color.fromHexString("#1d1c1a"), Color.fromHexString("#222222"));

		palette.setColor(Color.fromHexString("#222222"), 'window');
		palette.setColor(Color.fromHexString("#c0c0c0c0"), 'windowText');
		palette.setColor(Color.fromHexString("#222222"), 'button');
		palette.setColor(Color.fromHexString("#f0f0f0"), 'buttonText');
		palette.setColor(Color.fromHexString("#161514"), 'base');
		palette.setColor(Color.fromHexString("#1e1d1c"), 'alternateBase');
		palette.setColor(Color.fromHexString("#161514"), 'toolTipBase');
		palette.setColor(Color.fromHexString("#c0c0c0c0"), 'toolTipText');
		palette.setColor(Color.fromHexString("#9062400a"), 'highlight');
		palette.setColor(Color.fromHexString("#FDFDFD"), 'highlightText');

		palette.setColor(Color.fromHexString("#e0b01e"), 'light'); // welow slider
		palette.setColor(Color.fromHexString("#62400a"), 'midlight'); // brown contour
		palette.setColor(Color.fromHexString("#363636"), 'middark'); // widget background
		palette.setColor(Color.fromHexString("#a7dd0d"), 'baseText'); // green param
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
	{ | anOssiaParameter |

		var event = { | param |
			var i = param.domain.values.detectIndex({ | item | item == param.value });

			{ param.widgets.value_(i) }.defer;
		};

		anOssiaParameter.addDependant(event);

		anOssiaParameter.widgets = EZPopUpMenu(
			parentView: anOssiaParameter.window,
			bounds: (anOssiaParameter.window.bounds.width - 6)@40,
			label: anOssiaParameter.name,
			items: anOssiaParameter.domain.values.collect({ | item | item.asSymbol }),
			globalAction: { | obj | anOssiaParameter.value_(obj.item) },
			layout: 'vert',
			gap: 2@0
		).onClose_(
			{ anOssiaParameter.removeDependant(event) }
		).setColors(
			stringColor: anOssiaParameter.window.view.palette.color('baseText', 'active'),
			menuStringColor: anOssiaParameter.window.view.palette.color('light', 'active')
		);
	}

	*makeSliderGui
	{ | anOssiaParameter |

		var event = { | param |
			{
				if (param.value != param.widgets.value)
				{ param.widgets.value_(param.value) };
			}.defer;
		};

		anOssiaParameter.addDependant(event);

		anOssiaParameter.widgets = EZSlider(
			parent: anOssiaParameter.window,
			bounds: (anOssiaParameter.window.bounds.width - 6)@40,
			label: anOssiaParameter.name,
			action: { | val | anOssiaParameter.value_(val.value) },
			layout: 'line2',
			gap: 2@0
		).onClose_(
			{ anOssiaParameter.removeDependant(event) }
		).setColors(
			stringColor: anOssiaParameter.window.view.palette.color('baseText', 'active'),
			sliderBackground: anOssiaParameter.window.view.palette.color('middark', 'active'),
			numNormalColor: anOssiaParameter.window.view.palette.color('windowText', 'active'),
			knobColor: anOssiaParameter.window.view.palette.color('light', 'active')
		);

		anOssiaParameter.widgets.sliderView.focusColor_(
			anOssiaParameter.window.view.palette.color('midlight', 'active');
		);

		if (anOssiaParameter.domain.min.notNil)
		{
			anOssiaParameter.widgets.controlSpec.minval_(anOssiaParameter.domain.min);
			anOssiaParameter.widgets.controlSpec.maxval_(anOssiaParameter.domain.max);
		};

		// set GUI value after min and max are set
		{ anOssiaParameter.widgets.value_(anOssiaParameter.value); }.defer;
	}

	*makeButtonGui
	{ | anOssiaParameter |

		var event = { | param |
			{
				if (param.value != param.widgets.value)
				{ param.widgets.value_(param.value) };
			}.defer;
		};

		StaticText(
			parent: anOssiaParameter.window,
			bounds: (anOssiaParameter.window.bounds.width - 6)@20)
		.string_(anOssiaParameter.name)
		.stringColor_(anOssiaParameter.window.view.palette.color('baseText', 'active'));

		anOssiaParameter.widgets = Button(
			parent: anOssiaParameter.window,
			bounds: (anOssiaParameter.window.bounds.width - 6)@20)
		.onClose_({ anOssiaParameter.removeDependant(event); })
		.focusColor_(anOssiaParameter.window.view.palette.color('midlight', 'active'));
	}

	*makeTxtGui
	{ | anOssiaParameter |

		var event = { | param |
			{
				if (param.value != param.widgets.value)
				{ param.widgets.value_(param.value) };
			}.defer;
		};

		anOssiaParameter.addDependant(event);

		anOssiaParameter.widgets = EZText(
			parent: anOssiaParameter.window,
			bounds: (anOssiaParameter.window.bounds.width - 6)@40,
			label: anOssiaParameter.name,
			action: { | val | anOssiaParameter.value_(val.value) },
			initVal: anOssiaParameter.value,
			labelWidth: 100,
			layout: 'vert',
			gap: 2@0
		).onClose_(
			{ anOssiaParameter.removeDependant(event) }
		).setColors(
			stringColor: anOssiaParameter.window.view.palette.color('baseText', 'active'),
			textBackground: anOssiaParameter.window.view.palette.color('middark', 'active'),
			textStringColor: anOssiaParameter.window.view.palette.color('windowText', 'active'));
	}
}
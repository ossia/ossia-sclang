/*
* This project is a fork of Pierre Cohard's ossia-supercollider
* https://github.com/OSSIA/ossia-supercollider.git
* Form his sclang files, the aim is to provide the same message structure
* specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
* and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
*/

//-------------------------------------------//
//                    NODE                   //
//-------------------------------------------//

OSSIA_Node {

	var <parent;
	var <name;
	var <path;
	var <device;
	var <>description;
	var <children;
	var m_ptr_data;
	var <window;

	addChild { |anOssiaNode|
		children = children.add(anOssiaNode);
	}

	*new { |parent, name|
		^super.newCopyArgs(parent, name).nodeCtor();
	}

	nodeCtor {
		var parent_path = parent.path;

		if (parent_path != $/) {
			device = parent.device;
			path = parent_path++$/++name;
		} {
			device = parent;
			path = $/++name;
		};

		parent.addChild(this);
		children = [];
	}

	tree { |with_attributes = false, parameters_only = false|

		if (parameters_only) {
			^this.paramExplore;
		} {
			^this.nodeExplore;
		}
	}

	nodeExplore { ^[this, children.collect(_.nodeExplore)]; }

	paramExplore { ^[children.collect(_.paramExplore)]; }

	free {
		children.collect(_.free);
		parent.children.remove(this);
		^super.free;
	}

	//-------------------------------------------//
	//                   JSON                    //
	//-------------------------------------------//

	json {
		^"\""++ name ++"\":"
		++"{\"FULL_PATH\":\""++ path ++"\""
		++ this.jsonParams
		++ if (description.notNil) {
			",\"DESCRIPTION\":\""++ description ++"\""
		} { "" }
		++ if (children.isEmpty.not) {
			",\"CONTENTS\":"++ OSSIA_Tree.stringify(children)
		} { "" }
		++"}"
	}

	jsonParams { ^""; }

	//-------------------------------------------//
	//                    GUI                    //
	//-------------------------------------------//

	gui { |parent_window, childrenDepth = 1|

		this.windowIfNeeded(parent_window);
		this.childGui(childrenDepth);
	}

	windowIfNeeded { |win|

		if (win.isNil) {
			window = Window(name).front; // resize later to the flow layout size
			window.view.palette_(OSSIA.pallette);
			window.view.background_(OSSIA.pallette.base);
			window.addFlowLayout;
		} {
			window = win;
		};
	}

	childGui { |childrenDepth|

		if (childrenDepth > 0) {
			children.do({ |item|
				item.gui(window, childrenDepth - 1);
			});
		};
	}

	//-------------------------------------------//
	//     PRIMITIVE CALLS & METHODS (TOREDO)    //
	//-------------------------------------------//
	//
	// snapshot { |... exclude|
	// 	var exp = this.explore(false, true);
	// 	var res = [];
	//
	// 	exp.do({|item|
	// 		var unique = item[0].split($/).last ++ "_" ++ item[1];
	// 		res = res.add(unique.asSymbol);
	// 		res = res.add(item[2]);
	// 	});
	//
	// 	if(exclude.notEmpty)
	// 	{
	// 		exclude.do({|item|
	// 			var index = res.indexOf(item.sym);
	// 			2.do({ res.removeAt(index) });
	// 		});
	// 	};
	//
	// 	^res
	// }
	//
	// is_disabled {
	// 	_OSSIA_NodeGetDisabled
	// 	^this.primitiveFailed
	// }
	//
	// enable { ^this.pyrDisabled_(false) }
	// disable { ^this.pyrDisabled_(true) }
	//
	// pyrDisabled_ { |aBool|
	// 	_OSSIA_NodeSetDisabled
	// 	^this.primitiveFailed
	// }
	//
	// hidden {
	// 	_OSSIA_NodeGetHidden
	// 	^this.primitiveFailed
	// }
	//
	// hidden_ { |aBool|
	// 	_OSSIA_NodeSetHidden
	// 	^this.primitiveFailed
	// }
	//
	// muted {
	// 	_OSSIA_NodeGetMuted
	// 	^this.primitiveFailed
	// }
	//
	// muted_ { |aBool|
	// 	_OSSIA_NodeSetMuted
	// 	^this.primitiveFailed
	// }
	//
	// tags {
	// 	_OSSIA_NodeGetTags
	// 	^this.primitiveFailed
	// }
	//
	// tags_ { |aSymbolList|
	// 	_OSSIA_NodeSetTags
	// 	^this.primitiveFailed
	// }
	//
	// zombie {
	// 	_OSSIA_NodeGetZombie
	// 	^this.primitiveFailed
	// }
	//
	// load_preset { |path| // TODO: force the .json extension
	// 	path !? { this.pyrPresetLoad(path) };
	// 	path ?? { Dialog.openPanel({|p| this.pyrPresetLoad(p)}); };
	// }
	//
	// save_preset { |path|
	// 	path !? { this.pyrPresetSave(path) };
	// 	path ?? { Dialog.savePanel({|p| this.pyrPresetSave(p)}); };
	// }
	//
	// pyrPresetLoad { |path|
	// 	_OSSIA_PresetLoad
	// 	^this.primitiveFailed
	// }
	//
	// pyrPresetSave { |path|
	// 	_OSSIA_PresetSave
	// 	^this.primitiveFailed
	// }

}

OSSIA_Parameter : OSSIA_Node {

	var <value;
	var <type;
	var <domain;
	var <bounding_mode;
	var <critical;
	var <repetition_filter;
	var <access_mode = 3;
	var <unit;
	var <m_callback;
	var >listening = true;
	var <widgets;

	*new { |parent_node, name, type, domain, default_value,
		bounding_mode = 'free', critical = false,
		repetition_filter = false|

		^super.new(parent_node, name).parameterCtor(type, domain, default_value,
			bounding_mode, critical, repetition_filter);
	}

	*array { |size, parent_node, name, type, domain, default_value,
		bounding_mode = 'free', critical = false , repetition_filter = false|

		^Array.fill(size, {|i|
			OSSIA_Parameter(parent_node, name ++ '_' ++ i, type, domain,
				default_value, bounding_mode, critical, repetition_filter);
		});
	}

	parameterCtor { |tp, dm, dv, bm, cl, rf|
		var dom_slot, df_val­­­­­­;

		switch(tp.class,
			Meta_Symbol, { type = String },
			Meta_List, { type = Array },
			{ type = tp };
		);

		if (dm.isNil) {
			dom_slot = [nil, nil, []];
		} { dom_slot = dm; };

		if (dom_slot.size != 3) { dom_slot = dom_slot.add([]); };

		domain = OSSIA_domain(dom_slot[0], dom_slot[1], dom_slot[2], type);

		if (dv.isNil) {
			df_val = type.ossiaDefaultValue();
		} {
			df_val = dv;
		};

		bounding_mode = OSSIA_bounding_mode(bm, type, domain);

		critical = cl;
		repetition_filter = rf;

		value = df_val;
		device.instantiateParameter(this);

		m_callback = {};
	}

	paramExplore { ^[this, children.collect(_.paramExplore)]; }

	free {
		children.collect(_.free);
		device.freeParameter(this);
		parent.children.remove(this);
		^super.free;
	}

	//-------------------------------------------//
	//                PROPERTIES                 //
	//-------------------------------------------//

	value_ { |v|
		var handle_value = bounding_mode.bound(type.ossiaNaNFilter(v, value));

		if (access_mode != 1) { // if differnet from get

			if (repetition_filter.nand(handle_value == value)) {
				value = handle_value;

				this.changed();
				this.pvOnCallback();

				if (listening) { device.updateParameter(this); };
			};
		};
	}

	valueQuiet { |v| // same as value_ without sending the updated value back to the device
		var handle_value = bounding_mode.bound(type.ossiaNaNFilter(v, value));

		if (access_mode != 1) { // if differnet from get

			if (repetition_filter.nand( (handle_value == value) )) {
				value = handle_value;

				this.changed();
				this.pvOnCallback();
			};
		};
	}

	domain_ { |min, max, values|
		var recall_mode = bounding_mode.md;

		bounding_mode.free;
		domain.free;

		domain = OSSIA_domain(max, max, values, type);
		bounding_mode = OSSIA_bounding_mode(recall_mode, type, domain);
	}

	bounding_mode_ { |mode|

		bounding_mode.free;
		bounding_mode = OSSIA_bounding_mode(mode, type, domain);
	}

	unit_ { | anOssiaUnit |
		if (unit.notNil) { unit.free };
		unit = anOssiaUnit;
	}

	access_mode_ { | anOssiaAccessMode |
		access_mode = anOssiaAccessMode;
	}

	critical_ { |aBool|
		critical = aBool;
	}

	jsonParams {
		^",\"TYPE\":"++ type.ossiaJson
		++ ",\"VALUE\":"
		++ if (type == String) {
			"\""++ value ++"\""
		} { value }
		++ domain.json(type.ossiaDefaultValue)
		++",\"CLIPMODE\":\""++ bounding_mode.mode ++"\""
		++ if (unit.notNil) {
			",\"UNIT\":[\""++ unit.string ++"\"]"
			++ if (unit.extended_types.notNil) {
				",\"EXTENDED_TYPE\":"++ unit.extended_types
			} { "" }
		} { "" }
		++",\"ACCESS\":\""++ access_mode ++"\""
		++",\"CRITICAL\":"++ critical
	}

	// priority {
	// 	_OSSIA_ParameterGetPriority
	// 	^this.primitiveFailed
	// }
	//
	// priority_ { |aFloat|
	// 	_OSSIA_ParameterSetPriority
	// 	^this.primitiveFailed
	// }
	//
	//-------------------------------------------//
	//                 CALLBACKS                 //
	//-------------------------------------------//
	//
	// prEnableCallback
	// {
	// 	_OSSIA_ParameterSetCallback
	// 	^this.primitiveFailed
	// }
	//
	// prDisableCallback
	// {
	// 	_OSSIA_ParameterRemoveCallback
	// 	^this.primitiveFailed
	// }

	callback { ^m_callback }

	callback_ { |callback_function|

		// if(m_callback.notNil()) { this.removeDependant(m_callback); };

		m_callback = callback_function;
	}

	// interpreter callback from attached ossia lambda
	pvOnCallback {
		m_callback.value(value);
	}

	//-------------------------------------------//
	//            SHORTCUTS & ALIASES            //
	//-------------------------------------------//

	v { ^this.value() }
	v_ { |value| this.value_(value) }
	sv { |value| this.value_(value) }

	// CONVENIENCE DEF MTHODS

	sym { ^(this.name ++ "_" ++ m_ptr_data.asSymbol).asSymbol }
	aar { ^[this.sym, this.value()] }

	kr { | bind = true |
		if(bind) {

			if(m_callback.notNil()) { this.removeDependant(m_callback); };

			m_callback = { |v| OSSIA.server.sendMsg("/n_set", 0, this.sym, v) };
		}

		^this.sym.kr
	}

	ar { | bind = true |
		if(bind) {

			if(m_callback.notNil()) { this.removeDependant(m_callback); };

			m_callback = { |v| OSSIA.server.sendMsg("/n_set", 0, this.sym, v) };
		}

		^this.sym.ar
	}

	tr { ^this.sym.tr }

	//-------------------------------------------//
	//                    GUI                    //
	//-------------------------------------------//

	gui { |parent_window, childrenDepth = 0|

		this.windowIfNeeded(parent_window);

		type.ossiaWidget(this);
		this.childGui(childrenDepth);

		if ((window.view.decorator.used.height - window.bounds.height) != 2.0) { //resize to flow layout
			window.bounds_(window.bounds.height_(window.view.decorator.used.height + 2.0));
		};
	}
	//
	// closeWigets {
	//
	// 	this.windowIfNeeded(parent_window);
	//
	// 	type.ossiaWidget(this);
	// 	this.childGui(childrenDepth);
	//
	// 	if ((window.view.decorator.used.height - window.bounds.height) != 2.0) { //resize to flow layout
	// 		window.bounds_(window.bounds.height_(window.view.decorator.used.height + 2.0));
	// 	};
	// }

}

//-------------------------------------------//
//     PRIMITIVE CALLS & METHODS (TOREDO)    //
//-------------------------------------------//
//
//
// OSSIA_MirrorParameter : OSSIA_Parameter {
// 	*new { |device, address|
// 		^super.newFromChild.mirrorInit.pyrGetMirror(device, address);
// 	}
//
// 	mirrorInit { m_has_callback = false }
//
// 	pyrGetMirror { |device, addr|
// 		_OSSIA_NodeGetMirror
// 		^this.primitiveFailed
// 	}
// }
//
// // basically the same thing, but with different inheritance
//
// OSSIA_MirrorNode : OSSIA_Node {
// 	*new { |device, address|
// 		^super.newFromChild.pyrGetMirror(device, address)
// 	}
//
// 	pyrGetMirror { |device, addr|
// 		_OSSIA_NodeGetMirror
// 		^this.primitiveFailed
// 	}
// }

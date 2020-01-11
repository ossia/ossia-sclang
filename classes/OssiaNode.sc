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

	var parent;
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

	//-------------------------------------------//
	//                    GUI                    //
	//-------------------------------------------//

	gui { |parent, childrenDepth = 1|
		this.windowIfNeeded(parent);
		this.childGui(childrenDepth);
	}

	windowIfNeeded { |win|
		if (win.isNil) {
			window = Window(name).front; // resize later to the flow layout size
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

	free {
		children.collect(_.free);
		parent.children.remove(this);
		^super.free;
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
	var <access_mode;
	var <unit;
	var <m_callback;
	var m_has_callback;
	var widgets;

	classvar skipJack, evenGui;

	*new { |parent_node, name, type, domain, default_value,
		bounding_mode = 'free', critical = false,
		repetition_filter = false|

		^super.new(parent_node, name).parameterCtor(type, domain, default_value,
			bounding_mode, critical, repetition_filter);
	}

	*array { |size, parent_node, name, type, domain, default_value,
		bounding_mode = 'free', critical = false , repetition_filter = false|

		^Array.fill(size, {|i|
			OSSIA_Parameter(parent_node, name++'_'++i, type, domain,
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

		type = tp;

		if (dm.isNil) {
			dom_slot = [nil, nil];
		} {
			dom_slot = dm;
		};

		domain = OSSIA_domain(dom_slot[0], dom_slot[1], type:type);

		if (dv.isNil) {
			switch(type.class,
				Meta_Float, { df_val = 0.0 },
				Meta_Integer, { df_val = 0 },
				Meta_OSSIA_vec2f, { df_val = [0.0, 0.0] },
				Meta_OSSIA_vec3f, { df_val = [0.0, 0.0, 0.0] },
				Meta_OSSIA_vec4f, { df_val = [0.0, 0.0, 0.0, 0.0] },
				Meta_Boolean, { df_val = false },
				Meta_String, { df_val = "" },
				Meta_Array, { df_val = [] },
				Meta_Char, { df_val = $ }
			);
		} {
			df_val = dv;
		};

		bounding_mode = OSSIA_bounding_mode(bm, type, domain);

		critical = cl;
		repetition_filter = rf;
		access_mode = 'bi';
		m_has_callback = false;

		value = bounding_mode.bound(df_val);
		device.instantiateParameter(this);
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
		var handle_value = bounding_mode.bound(v);

		if (access_mode != 'get') {

			if (repetition_filter.nand( (handle_value == value) )) {

				if (m_has_callback)
				{
					m_callback.value(handle_value);
				};

				value = handle_value;
				device.updateParameter(this);
			};
		};
	}

	valueQuiet { |v| // same as value_ without sending the updated value back to the device
		var handle_value = bounding_mode.bound(v);

		if (access_mode != 'set') {

			if (repetition_filter.nand( (handle_value == value) )) {

				if (m_has_callback)
				{
					m_callback.value(handle_value);
				};

				value = handle_value;
			};
		};
	}

	domain_ { |min, max, values|
		var recall_mode = bounding_mode.md;

		bounding_mode.free;
		domain.free;

		domain = OSSIA_domain(max, max, values);
		bounding_mode = OSSIA_bounding_mode(recall_mode, type, domain);
	}

	bounding_mode_ { |mode|
		bounding_mode.free;
		bounding_mode = OSSIA_bounding_mode(mode, type, domain);
	}

	unit_ { |anOssiaUnit|
		if (unit.notNil) { unit.free };
		unit = anOssiaUnit;
	}

	access_mode_ { |aSymbol|
		access_mode = aSymbol;
	}

	critical_ { |aBool|
		critical = aBool;
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
		if(not(m_has_callback)) {
			m_has_callback = true;
		} {
			if(callback_function.isNil()) {
				m_has_callback = false;
			}
		};

		m_callback = callback_function;
	}

	// interpreter callback from attached ossia lambda
	pvOnCallback { |v|
		m_callback.value(v);
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
			if(not(m_has_callback)) { m_has_callback = true };
			m_callback = { |v| OSSIA.server.sendMsg("/n_set", 0, this.sym, v) };
		}

		^this.sym.kr
	}

	ar { | bind = true |
		if(bind) {
			if(not(m_has_callback)) { m_has_callback = true };
			m_callback = { |v| OSSIA.server.sendMsg("/n_set", 0, this.sym, v) };
		}

		^this.sym.ar
	}

	tr { ^this.sym.tr }

	//-------------------------------------------//
	//                    GUI                    //
	//-------------------------------------------//

	gui { |parent, childrenDepth = 0|

		this.windowIfNeeded(parent);

		if (evenGui.isNil) {
			evenGui = ();
		};

		if (skipJack.isNil) {
			skipJack = SkipJack({
				evenGui.do(_.defer);
			},
			0.1,
			evenGui.size == 0,
			name: this.name
			);
		};

		this.addWidget();
		this.childGui(childrenDepth);

		if ((window.view.decorator.used.height - window.bounds.height) != 2.0) { //resize to flow layout
			window.bounds_(window.bounds.height_(window.view.decorator.used.height + 2.0));
		};
	}

	addWidget { |childDepth|
		var widgets;

		switch(type.class,
			Meta_Float, {
				widgets = EZSlider(window, 392@20, name,
					action:{ | val | this.value_(val.value); },
					initVal: value, labelWidth:100, gap:0@0).onClose_({
					evenGui.removeAt(this.name.asSymbol); });

				if(domain.min.notNil) {
					widgets.controlSpec.minval_(domain.min);
					widgets.controlSpec.maxval_(domain.max);
				};

				evenGui[name.asSymbol] = {
					if (value != widgets.value) {
						widgets.value_(value);
					};
				};
			},
			Meta_Integer, {
				widgets = EZSlider(window, 392@20, name,
					action:{ | val | this.value_(val.value); },
					initVal: value, labelWidth:100, gap:0@0).onClose_({
					evenGui.removeAt(this.name.asSymbol); });

				widgets.controlSpec.step_(1);

				if(domain.min.notNil) {
					widgets.controlSpec.minval_(domain.min);
					widgets.controlSpec.maxval_(domain.max);
				};

				evenGui[name.asSymbol] = {
					if (value != widgets.value) {
						widgets.value_(value);
					};
				};
			},
			Meta_OSSIA_vec2f, {
				widgets = EZRanger(window, 392@20, name,
					action:{ | val | this.value_(val.value); },
					initVal: value, labelWidth:100, gap:0@0).onClose_({
					evenGui.removeAt(this.name.asSymbol); });

				if(domain.min.notNil) {
					widgets.controlSpec.minval_(domain.min[0]);
					widgets.controlSpec.maxval_(domain.max[1]);
				};

				evenGui[name.asSymbol] = {
					if (value != widgets.value) {
						widgets.value_(value);
					};
				};
			},
			Meta_OSSIA_vec3f, {
				widgets = [
					EZNumber(window, 194@20, name,
						action:{ | val | this.value_([val.value, value[1], value[2]]); },
						initVal: value[0], labelWidth:100, gap:0@0),
					EZNumber(window, 95@20,
						action:{ | val | this.value_([value[0], val.value, value[2]]); },
						initVal: value[1], gap:0@0),
					EZNumber(window, 95@20,
						action:{ | val | this.value_([value[0], value[1], val.value]); },
						initVal: value[2], gap:0@0).onClose_({
					evenGui.removeAt(this.name.asSymbol); })
				];

				if(domain.min.notNil) {
					widgets[0].controlSpec.minval_(domain.min[0]);
					widgets[0].controlSpec.maxval_(domain.max[0]);
					widgets[1].controlSpec.minval_(domain.min[1]);
					widgets[1].controlSpec.maxval_(domain.max[1]);
					widgets[2].controlSpec.minval_(domain.min[2]);
					widgets[2].controlSpec.maxval_(domain.max[2]);
				};

				evenGui[name.asSymbol] = {
					if (value != [widgets[0].value, widgets[1].value, widgets[2].value]) {
						widgets[0].value_(value[0]);
						widgets[1].value_(value[1]);
						widgets[2].value_(value[2]);
					};
				};
			},
			Meta_OSSIA_vec4f, {
				widgets = [
					EZNumber(window, 170@20, name,
						action:{ | val | this.value_([val.value, value[1], value[2], value[3]]); },
						initVal: value[0], labelWidth:100, gap:0@0),
					EZNumber(window, 70@20,
						action:{ | val | this.value_([value[0], val.value, value[2], value[3]]); },
						initVal: value[1], gap:0@0),
					EZNumber(window, 70@20,
						action:{ | val | this.value_([value[0], value[1], val.value, value[3]]); },
						initVal: value[2], gap:0@0),
					EZNumber(window, 70@20,
						action:{ | val | this.value_([value[0], value[1], value[2], val.value]); },
						initVal: value[3], gap:0@0).onClose_({
					evenGui.removeAt(this.name.asSymbol); })
				];

				if(domain.min.notNil) {
					widgets[0].controlSpec.minval_(domain.min[0]);
					widgets[0].controlSpec.maxval_(domain.max[0]);
					widgets[1].controlSpec.minval_(domain.min[1]);
					widgets[1].controlSpec.maxval_(domain.max[1]);
					widgets[2].controlSpec.minval_(domain.min[2]);
					widgets[2].controlSpec.maxval_(domain.max[2]);
					widgets[2].controlSpec.minval_(domain.min[3]);
					widgets[2].controlSpec.maxval_(domain.max[3]);
				};

				evenGui[name.asSymbol] = {
					if (value != [widgets[0].value, widgets[1].value, widgets[2].value,
						widgets[3].value]) {
						widgets[0].value_(value[0]);
						widgets[1].value_(value[1]);
						widgets[2].value_(value[2]);
						widgets[3].value_(value[3]);
					};
				};
			},
			Meta_Boolean, {
				StaticText(window, 100@20).string_(this.name).align_(\right);

				widgets = widgets.add(Button(window, 288@20).states_([
					["true", Color.black, Color.green()],
					["false", Color.white, Color.red()]
				]).action_({ | val | this.value_(val.value); }).onClose_({
					evenGui.removeAt(this.name.asSymbol); }));

				evenGui[this.name.asSymbol] = {
					if (this.value != widgets.value) {
						widgets.value_(this.value);
					};
				};
			},
			Meta_Impulse, {
				StaticText(window, 100@20).string_(name).align_(\right);

				widgets = Button(window, 288@20).states_([
					["Pulse"]]).action_({ | val | this.value_(); }).onClose_({
					evenGui.removeAt(this.name.asSymbol); });
			},
			{
				widgets = EZText(window, 392@20, this.name,
					action:{ | val | this.value_(val.value); },
					initVal: value, labelWidth:100, gap:0@0).onClose_({
					evenGui.removeAt(this.name.asSymbol); });

				evenGui[this.name.asSymbol] = {
					if (value != widgets.value) {
						widgets.value_(value);
					};
				};
			};
		);
	}

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

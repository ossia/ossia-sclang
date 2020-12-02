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

OSSIA_Node : OSSIA_Base
{
	var parent;
	var <device;
	var <>description;
	var m_ptr_data;

	parent { ^parent } // homogeneity with OSSIA_Device

	*new { | parent, name |

		^super.new.prHandleParent(parent).prHandlePath(name).nodeCtor();
	}

	nodeCtor
	{
		var parent_path = parent.path;

		if (parent_path != $/)
		{
			device = parent.device;
			path = parent_path ++ $/ ++ name;
		} {
			device = parent;
			path = $/ ++ name;
		};

		parent.addChild(this);
		children = [];

		this.prInstantiateNode();
	}

	instantiate { this.prInstantiateNode() }

	free
	{
		children.reverseDo(_.free);
		this.prFreeNode();
		parent.children.remove(this);
		^super.free;
	}

	//-------------------------------------------//
	//                   JSON                    //
	//-------------------------------------------//

	json
	{
		^"\""++ name ++"\":"
		++"{\"FULL_PATH\":\""++ path ++"\""
		++ this.jsonParams
		++ if (description.notNil) {
			",\"DESCRIPTION\":\""++ description ++"\""
		} { "" }
		++ if (children.isEmpty.not) {
			",\"CONTENTS\":"++ OSSIA.stringify(children)
		} { "" }
		++"}"
	}

	jsonParams { ^"" }

	//-------------------------------------------//
	//              PRIVATE METHODS              //
	//-------------------------------------------//

	prInstantiateNode { device.instantiateNode(path) }

	prFreeNode { device.freeNode(path) }

	prHandleParent
	{ | pn |

		if (pn.isNil)
		{
			if (OSSIA_Device.g_devices == [])
			{
				Error("No parent_node or OSSIA_Device found").throw;
			} {
				// if no parent is provided, set it as the first OSSIA_Device
				parent = OSSIA_Device.g_devices[0];
			}
		} {
			parent = pn;
		};
	}

	prCreateOrIncrement
	{ | p, n |

		parent = p;

		if ((n.class.superclass == OSSIA_Node) || (n.class.superclass == OSSIA_Base))
		{
			// check if the node has a index
			var index, splitedName = n.name.split($.);

			index = splitedName[splitedName.size - 1].asInteger;
			// returns 0 if no index is found

			if ((index == 0))
			{
				name = n.name ++ ".1";
			} {
				splitedName.removeAt(splitedName.size - 1);

				name = splitedName.join($.) ++ '.' ++ (index + 1);
			}
		} {
			name = n;
		}
	}

	prNodeExplore { ^[this, children.collect(_.prNodeExplore)] }

	prParamExplore { ^[children.collect(_.prParamExplore)] }

	//-------------------------------------------//
	//     PRIMITIVE CALLS & METHODS (TOREDO)    //
	//-------------------------------------------//
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

OSSIA_Parameter : OSSIA_Node
{
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
	var <>widgets;

	*new
	{ | parent_node, name, type, domain, default_value,
		bounding_mode = 'free', critical = false,
		repetition_filter = false |

		^super.new(parent_node, name).parameterCtor(type, domain, default_value,
			bounding_mode, critical, repetition_filter);
	}

	*array
	{ | size, parent_node, name, type, domain, default_value, bounding_mode = 'free',
		critical = false , repetition_filter = false |

		^Array.fill(size, { | i |
			this.new(parent_node, name ++ '_' ++ i, type, domain,
				default_value, bounding_mode, critical, repetition_filter);
		});
	}

	parameterCtor
	{ | tp, dm, dv, bm, cl, rf |

		var dom_slot, df_val­­­­­­;

		this.prHandleType(tp);

		if (dm.isNil)
		{
			dom_slot = [nil, nil, []];
		} {
			dom_slot = dm;
		};

		if (dom_slot.size != 3) { dom_slot = dom_slot.add([]) };

		domain = OSSIA_domain(dom_slot[0], dom_slot[1], dom_slot[2], type);

		if (dv.isNil)
		{
			df_val = type.ossiaDefaultValue();
		} {
			df_val = dv;
		};

		bounding_mode = OSSIA_bounding_mode(bm, type, domain);

		critical = cl;
		repetition_filter = rf;

		m_callback = {};

		value = bounding_mode.bound(type.ossiaNaNFilter(df_val, df_val));

		device.instantiateParameter(this);
	}

	instantiate { device.instantiateParameter(this) }

	free
	{
		device.freeParameter(this);
		this.closeGui();
		^super.free;
	}

	//-------------------------------------------//
	//                PROPERTIES                 //
	//-------------------------------------------//

	value_
	{ | v |

		var handle_value = bounding_mode.bound(type.ossiaNaNFilter(v, value));

		if (access_mode != 1)
		{ // if differnet from get

			if (repetition_filter.nand(handle_value == value))
			{
				value = handle_value;

				this.changed();
				this.pvOnCallback();

				if (listening) { device.updateParameter(this); };
			};
		};
	}

	valueQuiet
	{ | v | // same as value_ without sending the updated value back to the device

		var handle_value = bounding_mode.bound(type.ossiaNaNFilter(v, value));

		if (access_mode != 1)
		{ // if differnet from get

			if (repetition_filter.nand( (handle_value == value) ))
			{
				value = handle_value;

				this.changed();
				this.pvOnCallback();
			};
		};
	}
/*
	type_
	{ | newType |

		var recall_mode = bounding_mode.md;

		bounding_mode.free;
		domain.free;

		domain = OSSIA_domain(max, max, values, type);
		bounding_mode = OSSIA_bounding_mode(recall_mode, type, domain);
	}*/

	domain_
	{ | min, max, values |

		var recall_mode = bounding_mode.md;

		bounding_mode.free;
		domain.free;

		domain = OSSIA_domain(max, max, values, type);
		bounding_mode = OSSIA_bounding_mode(recall_mode, type, domain);
	}

	bounding_mode_
	{ | mode |

		bounding_mode.free;
		bounding_mode = OSSIA_bounding_mode(mode, type, domain);
	}

	unit_
	{ | anOssiaUnit |

		if (unit.notNil) { unit.free };
		unit = anOssiaUnit;
	}

	access_mode_ { | anOssiaAccessMode | access_mode = anOssiaAccessMode }

	critical_ { | aBool | critical = aBool }

	jsonParams
	{
		^",\"TYPE\":"++ type.ossiaJson
		++ ",\"VALUE\":"
		++ if ((type == String) || (type == Char)) {
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

	//-------------------------------------------//
	//              PRIVATE METHODS              //
	//-------------------------------------------//

	prHandleType
	{ | newType |

		switch (newType,
			Signal, { type = Impulse },
			Symbol, { type = String },
			List, { type = Array },
			{ type = newType };
		);
	}

	prInstantiateNode { }

	prFreeNode { }

	prParamExplore { ^[this, children.collect(_.prParamExplore)] }

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

	callback_{ | callback_function | m_callback = callback_function }

	// interpreter callback from attached ossia lambda
	pvOnCallback { m_callback.value(value) }

	//-------------------------------------------//
	//            SHORTCUTS & ALIASES            //
	//-------------------------------------------//

	v { ^this.value() }
	v_ { | value | this.value_(value) }
	sv { | value | this.value_(value) }

	// CONVENIENCE DEF MTHODS

	sym { ^(this.name ++ "_" ++ m_ptr_data.asSymbol).asSymbol }
	aar { ^[this.sym, this.value()] }

	kr
	{ | bind = true |

		if(bind)
		{
			if(m_callback.notNil()) { this.removeDependant(m_callback) };

			m_callback = { | v | OSSIA.server.sendMsg("/n_set", 0, this.sym, v) };
		}

		^this.sym.kr
	}

	ar
	{ | bind = true |

		if(bind)
		{
			if(m_callback.notNil()) { this.removeDependant(m_callback) };

			m_callback = { | v | OSSIA.server.sendMsg("/n_set", 0, this.sym, v) };
		}

		^this.sym.ar
	}

	tr { ^this.sym.tr }
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

/*
 *
 *
 *
 */

OSSIA_Node {

	var <name;
	var <path;
	var <device;
	var <description;
	var <children;
	var m_ptr_data;

	addChild { |anOssiaNode|
		children = children.add(anOssiaNode);
	}

	*new { |parent, name|
		^super.new.nodeCtor(parent, name);
	}

	nodeCtor { |p, n|
		var parent = p.path;

		name =  n;

		if (parent != $/) {
			device = p.device;
			path = parent++$/++name;
		} {
			device = p;
			path = $/++name;
		};

		p.addChild(this);
		children = [];
	}

	tree { |with_attributes = false, parameters_only = false|
		if (parameters_only) {
			^this.paramExplore;
		} {
			^this.nodeExplore;
		}
	}

	nodeExplore {
		^[this, children.collect(_.nodeExplore)];
	}

	paramExplore {
		if (this.class == OSSIA_Parameter) {
			^[this, children.collect(_.paramExplore)];
		} {
		^[children.collect(_.paramExplore)];
		}
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
	// tree { |with_attributes = false, parameters_only = false|
	// 	var exp = this.explore(with_attributes, parameters_only);
	//
	// 	exp.do({|item|
	// 		var str = "";
	// 		item.do({|subitem|
	// 			str = str + subitem;
	// 		});
	// 		str.postln;
	// 	});
	//
	// }
	//
	// explore { |with_attributes = true, parameters_only = false|
	// 	_OSSIA_NodeExplore
	// 	^this.primitiveFailed
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

	var <value, <type, domain, default_value, bounding_mode,
	critical, repetition_filter, m_callback, m_has_callback;

	*new {|parent_node, name, type, domain, default_value,
		bounding_mode = 'free', critical = false,
		repetition_filter = false|

		^super.new(parent_node, name).parameterCtor(type, domain, default_value,
			                bounding_mode, critical, repetition_filter);
	}

	*array { |size, parent_node, name, type, domain, default_value,
		bounding_mode = 'free', critical = false , repetition_filter = false|
		^Array.fill(size, {|i|
			OSSIA_Parameter(parent_node, format(name,i), type, domain, default_value, bounding_mode,
				critical, repetition_filter);
		});
	}

	parameterCtor { |type, domain, default_value,
		bounding_mode, critical, repetition_filter|

		m_has_callback = false;

		device.instantiateParameter(this);
	}

	free {
		device.freeParameter(path);
	}

	//-------------------------------------------//
	//                PROPERTIES                 //
	//-------------------------------------------//

	value_ { |v|

		if(m_has_callback)
		{
			m_callback.value(v);
		};

		value = v;
		device.updateParameter(this);
	}

	valueQiet { |v|

		if(m_has_callback)
		{
			m_callback.value(v);
		};

		value = v;
	}

	//
	// pyrSetValue { |v|
	// 	_OSSIA_ParameterSetValue
	// 	^this.primitiveFailed
	// }
	//
	// access_mode {
	// 	_OSSIA_ParameterGetAccessMode
	// 	^this.primitiveFailed
	// }
	//
	// access_mode_ { |aSymbol|
	// 	_OSSIA_ParameterSetAccessMode
	// 	^this.primitiveFailed
	// }
	//
	// domain {
	// 	_OSSIA_ParameterGetDomain
	// 	^this.primitiveFailed
	// }
	//
	// domain_ { |aList|
	// 	_OSSIA_ParameterSetDomain
	// 	^this.primitiveFailed
	// }
	//
	// bounding_mode {
	// 	_OSSIA_ParameterGetBoundingMode
	// 	^this.primitiveFailed
	// }
	//
	// bounding_mode_ { |aSymbol|
	// 	_OSSIA_ParameterSetBoundingMode
	// 	^this.primitiveFailed
	// }
	//
	// repetition_filter {
	// 	_OSSIA_ParameterGetRepetitionFilter
	// 	^this.primitiveFailed
	// }
	//
	// repetition_filter_ { |aBool|
	// 	_OSSIA_ParameterSetRepetitionFilter
	// 	^this.primitiveFailed
	// }
	//
	// unit {
	// 	_OSSIA_ParameterGetUnit
	// 	^this.primitiveFailed
	// }
	//
	// unit_ { |aSymbol|
	// 	_OSSIA_ParameterSetUnit
	// 	^this.primitiveFailed
	// }
	//
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
	// critical {
	// 	_OSSIA_ParameterGetCritical
	// 	^this.primitiveFailed
	// }
	//
	// critical_ { |aBool|
	// 	_OSSIA_ParameterSetCritical
	// 	^this.primitiveFailed
	// }

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
			this.prEnableCallback();
			m_has_callback = true;
		} {
			if(callback_function.isNil()) {
				this.prDisableCallback;
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
			if(not(m_has_callback)) { this.prEnableCallback; m_has_callback = true };
			m_callback = { |v| OSSIA.server.sendMsg("/n_set", 0, this.sym, v) };
		}

		^this.sym.kr
	}

	ar { | bind = true |

		if(bind) {
			if(not(m_has_callback)) { this.prEnableCallback; m_has_callback = true };
			m_callback = { |v| OSSIA.server.sendMsg("/n_set", 0, this.sym, v) };
		}

		^this.sym.ar
	}

	tr { ^this.sym.tr}

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

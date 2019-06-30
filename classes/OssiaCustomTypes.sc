/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

	//-------------------------------------------//
	//                CUSTOM TYPES               //
	//-------------------------------------------//

OSSIA {

	classvar server;

	*server  { if(server.isNil) { ^Server.default } { ^server } }
	*server_ { |target| server = target }
	*domain { |min, max, values| ^OSSIA_domain(min, max, values)}
	*access_mode { ^OSSIA_access_mode }
	*bounding_mode { ^OSSIA_bounding_mode }

	*vec2f { |v1 = 0.0, v2 = 0.0| ^OSSIA_vec2f(v1, v2) }
	*vec3f { |v1 = 0.0, v2 = 0.0, v3 = 0.0| ^OSSIA_vec3f(v1, v2, v3) }
	*vec4f { |v1 = 0.0, v2 = 0.0, v3 = 0.0, v4 = 0.0| ^OSSIA_vec4f(v1, v2, v3, v4) }

	*device  { |name| ^OSSIA_Device(name) }
	*node { |parent_node, name| ^OSSIA_Node(parent_node, name) }

	*parameter { |parent_node, name, type, domain, default_value, bounding_mode = 'free',
		critical = false, repetition_filter = false |
		^OSSIA_Parameter(parent_node, name, type, domain,
			default_value, bounding_mode, critical, repetition_filter);
	}

	*parameter_array { |size, parent_node, name, type, domain, default_value,
		bounding_mode = 'free', critical = false, repetition_filter = false|
		^OSSIA_Parameter.array(size, parent_node, name, type, domain, default_value,
			bounding_mode, critical, repetition_filter);
	}
}

OSSIA_domain
{
	var m_domain;

	*new { |min, max, values|

		if((not(values.class == Array)) && (values.notNil)) {
			Error("values argument should be an array").throw;
		};

		^super.new.domainCtor(min, max, values);
	}

	domainCtor { |min, max|
		m_domain = [min, max];
	}

	at {|i| ^m_domain[i] }
	put { |index, item| m_domain[index] = item }

	min      { ^m_domain[0] }
	min_     { |v| m_domain[0] = v }
	max      { ^m_domain[1] }
	max_     { |v| m_domain[1] = v }
	values   { ^m_domain[2] }
	values_  { |anArray|
		if((not(anArray.class == Array)) && (anArray.notNil)) {
			Error("values argument should be an array").throw;
		};
		m_domain[2] = anArray;
	}
}

OSSIA_vec2f : OSSIA_FVector
{
	*new {|v1 = 0.0, v2 = 0.0|
		^super.new(2, v1.asFloat, v2.asFloat);
	}

	*asOssiaVec { |anArray|
		^[anArray[0].asFloat, anArray[1].asFloat];
	}
}

OSSIA_vec3f : OSSIA_FVector
{
	*new {|v1 = 0.0, v2 = 0.0, v3 = 0.0|
		^super.new(3, v1.asFloat, v2.asFloat, v3.asFloat);
	}

	*asOssiaVec { |anArray|
		^[anArray[0].asFloat, anArray[1].asFloat,  anArray[2].asFloat];
	}
}

OSSIA_vec4f : OSSIA_FVector
{
	*new {|v1 = 0.0, v2 = 0.0, v3 = 0.0, v4 = 0.0|
		^super.new(4, v1.asFloat, v2.asFloat, v3.asFloat, v4.asFloat);
	}

	*asOssiaVec { |anArray|
		^[anArray[0].asFloat, anArray[1].asFloat,  anArray[2].asFloat,  anArray[3].asFloat];
	}
}

OSSIA_FVector {

	var <am_val, m_sz;

	*new {|sz ... values|
		^super.new.init(sz, values);
	}

	init { |sz, v|

		v.do({|item|
			if((item.isFloat.not) && (item.isInteger.not)) {
				Error("OSSIA: Error! Arguments are not of Float type").throw;
			};
		});

		am_val = v;
		m_sz = sz;
	}

	at {|i| ^am_val[i] }
	put { |index, item| am_val[index] = item.asFloat }

}

OSSIA_access_mode {
	*bi { ^'bi' }
	*get { ^'get' }
	*set { ^'set' }
}

OSSIA_bounding_mode {

	classvar switch_tree, func_array;
	var <mode, domain, type, handle_bounds;

	*initClass {

		func_array = [
			// Integer (from index 0)
			{ |value, domain, type| value.asInteger },
			{ |value, domain, type| value.clip(domain.min, domain.max).asInteger },
			{ |value, domain, type| value.max(domain.min).asInteger },
			{ |value, domain, type| value.min(domain.max).asInteger },
			{ |value, domain, type| value.wrap(domain.min, domain.max).asInteger },
			{ |value, domain, type| value.fold(domain.min, domain.max).asInteger },
			{ |value, domain, type| domain[2].do({ |item| if (item == value)
				{ ^value.asInteger };
			});
			// Float (from index 7)
			}, { |value, domain, type| value.asFloat },
			{ |value, domain, type| value.clip(domain.min, domain.max).asFloat },
			{ |value, domain, type| value.max(domain.min).asFloat },
			{ |value, domain, type| value.min(domain.max).asFloat },
			{ |value, domain, type| value.wrap(domain.min, domain.max).asFloat },
			{ |value, domain, type| value.fold(domain.min, domain.max).asFloat },
			{ |value, domain, type| domain[2].do({ |item| if (item == value)
				{ ^value.asFloat };
			});
			// Boolean (index 14)
			}, { |value, domain, type| value.asBoolean },
			// Char (from index 15)
			{ |value, domain, type| value.asAscii },
			{ |value, domain, type| value.clip(domain.min, domain.max).asAscii },
			{ |value, domain, type| value.max(domain.min).asAscii },
			{ |value, domain, type| value.min(domain.max).asAscii },
			{ |value, domain, type| domain[2].do({ |item| if (item == value)
				{ ^value.asAscii };
			});
			// String (from index 20)
			}, { |value, domain, type| domain[2].do({ |item|
				if (item == value) { ^value.asString };
			});
			}, { |value, domain, type| value.asString },
			// Array (from index 22)
			{ |value, domain, type| value.asArray },
			{ |value, domain, type| value.clip(domain.min, domain.max).asArray },
			{ |value, domain, type| value.max(domain.min).asArray },
			{ |value, domain, type| value.min(domain.max).asArray },
			{ |value, domain, type| value.wrap(domain.min, domain.max).asArray },
			{ |value, domain, type| value.fold(domain.min, domain.max).asArray },
			{ |value, domain, type| domain[2].do({ |item| if (item == value)
				{ ^value.asArray };
			});
			// VecNf (from index 29)
			}, { |value, domain, type| type.asOssiaVec(value.clip(domain.min, domain.max)) },
			{ |value, domain, type| type.asOssiaVec(value.max(domain.min)) },
			{ |value, domain, type| type.asOssiaVec(value.min(domain.max)) },
			{ |value, domain, type| type.asOssiaVec(value.wrap(domain.min, domain.max)) },
			{ |value, domain, type| type.asOssiaVec(value.wrap(domain.min, domain.max)) },
			{ |value, domain, type| domain[2].do({ |item| if (item == value)
				{ ^type.asOssiaVec(value) };
			});
			}
		];

		switch_tree = { |mode, type, domain|
			switch(type.class,
				Meta_Integer, {
					switch(mode,
						'free', {
							func_array[0];
						},
						'clip', {
							func_array[1];
						},
						'low', {
							func_array[2];
						},
						'high', {
							func_array[3];
						},
						'wrap', {
							func_array[4];
						},
						'fold', {
							func_array[5];
						}, {
							func_array[6];
					});
				},
				Meta_Float, {
					switch(mode,
						'free', {
							func_array[7];
						},
						'clip', {
							func_array[8];
						},
						'low', {
							func_array[9];
						},
						'high', {
							func_array[10];
						},
						'wrap', {
							func_array[11];
						},
						'fold', {
							func_array[12];
						}, {
							func_array[13];
					});
				},
				Meta_Boolean, {
					func_array[14];
				},
				Meta_Char, {
					switch(mode,
						'free', {
							func_array[15];
						},
						'clip', {
							func_array[16];
						},
						'low', {
							func_array[17];
						},
						'high', {
							func_array[18];
						}, {
							func_array[19];
					});
				},
				Meta_String, {
					if (mode == 'values') {
						func_array[20];
					} {
						func_array[21];
					};
				},
				Meta_Array, {
					switch(mode,
						'free', {
							func_array[22];
						},
						'clip', {
							func_array[23];
						},
						'low', {
							func_array[24];
						},
						'high', {
							func_array[25];
						},
						'wrap', {
							func_array[26];
						},
						'fold', {
							func_array[27];
						}, {
							func_array[28];
					});
				},
				Meta_OSSIA_vec2f, {
					switch(mode,
						'free', {
							func_array[29];
						},
						'clip', {
							func_array[30];
						},
						'low', {
							func_array[31];
						},
						'high', {
							func_array[32];
						},
						'wrap', {
							func_array[33];
						},
						'fold', {
							func_array[34];
						}, {
							func_array[35];
					});
				},
				Meta_OSSIA_vec3f, {
					switch(mode,
						'free', {
							func_array[29];
						},
						'clip', {
							func_array[30];
						},
						'low', {
							func_array[31];
						},
						'high', {
							func_array[32];
						},
						'wrap', {
							func_array[33];
						},
						'fold', {
							func_array[34];
						}, {
							func_array[35];
					});
				},
				Meta_OSSIA_vec4f, {
					switch(mode,
						'free', {
							func_array[29];
						},
						'clip', {
							func_array[30];
						},
						'low', {
							func_array[31];
						},
						'high', {
							func_array[32];
						},
						'wrap', {
							func_array[33];
						},
						'fold', {
							func_array[34];
						}, {
							func_array[35];
					});
				};
			);
		};
	}

	*new { |mode, anOssiaType, anOssiaDomain|

		^super.new.ctor(mode, anOssiaType, anOssiaDomain);
	}

	ctor { |bm, tp, dn|

		domain = dn;

		if (domain.values.notNil) {
			mode = 'values';
		} {
			mode = bm;
		};

		type = tp;

		handle_bounds = switch_tree.value(mode, type, domain);

	}

	bound { |value|
		^handle_bounds.value(value, domain, type);
	}

	*free { ^'free' }
	*clip { ^'clip' }
	*low { ^'low' }
	*high { ^'high' }
	*wrap { ^'wrap' }
	*fold { ^'fold' }

}
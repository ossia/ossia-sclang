/*
 *
 *
 *
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

OSSIA_domain[slot]
{
	var m_domain;

	*new { |min, max, values|

		if((not(values.class == Array)) && (values.notNil)) {
			Error("values argument should be an array").throw;
		};

		^super.new.domainCtor(min, max, values);
	}

	domainCtor { |min, max, values|
		m_domain = [min, max, values];
	}

	at {|i| ^m_domain[i] }
	put { |index, item| m_domain[index] = item }

	min      { ^this[0] }
	min_     { |v| this[0] = v }
	max      { ^this[1] }
	max_     { |v| this[1] = v }
	values   { ^this[2] }
	values_  { |anArray|
		if((not(anArray.class == Array)) && (anArray.notNil)) {
			Error("values argument should be an array").throw;
		};
		this[2] = anArray;
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

	var <md, handle_bounds;

	*new { |mode, anOssiaType, anOssiaDomain|
		^super.new.boundCtor(mode, anOssiaType, anOssiaDomain);
	}

	boundCtor { |mode, type, domain|

		md = mode;

		switch(type.class,
			Meta_Integer, {
				if (domain[2].size == 0) {
					switch(mode,
						'free', {
							handle_bounds = { |value|
								value };
						},
						'clip', {
							handle_bounds = { |value|
								value.clip(domain.min, domain.max).asInteger };
						},
						'low', {
							handle_bounds = { |value|
								value.max(domain.min).asInteger };
						},
						'high', { handle_bounds = { |value|
							value.min(domain.max).asInteger };
						},
						'wrap', { handle_bounds = { |value|
							value.wrap(domain.min, domain.max).asInteger };
						},
						'fold', { handle_bounds = { |value|
							value.fold(domain.min, domain.max).asInteger };
						};
					);
				} {
					handle_bounds = { |value|
						domain[2].do({ |item|
							if (item == value) { ^value.asInteger };
						});
					};
				};
			},
			Meta_Float, {
				if (domain[2].size == 0) {
					switch(mode,
						'free', {
							handle_bounds = { |value|
								value.asFloat };
						},
						'clip', {
							handle_bounds = { |value|
								value.clip(domain.min, domain.max).asFloat };
						},
						'low', {
							handle_bounds = { |value|
								value.max(domain.min).asFloat };
						},
						'high', { handle_bounds = { |value|
							value.min(domain.max).asFloat };
						},
						'wrap', { handle_bounds = { |value|
							value.wrap(domain.min, domain.max).asFloat };
						},
						'fold', { handle_bounds = { |value|
							value.fold(domain.min, domain.max).asFloat };
						};
					);
				} {
					handle_bounds = { |value|
						domain[2].do({ |item|
							if (item == value) { ^value.asFloat };
						});
					};
				};
			},
			Meta_Boolean, { handle_bounds = { |value|
				value.asBoolean } },
			Meta_Char, {
				if (domain[2].size == 0) {
					switch(mode,
						'clip', {
							handle_bounds = { |value|
								value.clip(domain.min, domain.max).asAscii };
						},
						'low', {
							handle_bounds = { |value|
								value.max(domain.min).asAscii };
						},
						'high', { handle_bounds = { |value|
							value.min(domain.max).asAscii };
						},
						{ handle_bounds = { |value|
							value.asAscii } };
					);
				} {
					handle_bounds = { |value|
						domain[2].do({ |item|
							if (item == value) { ^value.asAscii };
						});
					};
				};
			},
			Meta_String, {
				if (domain[2].size == 0) {
					handle_bounds = { |value| value.asString }
				} {
					handle_bounds = { |value|
						domain[2].do({ |item|
							if (item == value) { ^value.asString };
						});
					};
				};
			},
			Meta_Symbol, {
				if (domain[2].size == 0) {
					handle_bounds = { |value| value.asSymbol }
				} {
					handle_bounds = { |value|
						domain[2].do({ |item|
							if (item == value) { ^value.asSymbol };
						});
					};
				};
			},
			Meta_Array, {
				if (domain[2].size == 0) {
					switch(mode,
						'free', {
							handle_bounds = { |value|
								value.asArray };
						},
						'clip', {
							handle_bounds = { |value|
								value.clip(domain.min, domain.max).asArray };
						},
						'low', {
							handle_bounds = { |value|
								value.max(domain.min).asArray };
						},
						'high', { handle_bounds = { |value|
							value.min(domain.max).asArray };
						},
						'wrap', { handle_bounds = { |value|
							value.wrap(domain.min, domain.max).asArray };
						},
						'fold', { handle_bounds = { |value|
							value.fold(domain.min, domain.max).asArray };
						};
					);
				} {
					handle_bounds = { |value|
						domain[2].do({ |item|
							if (item == value) { ^value.asArray };
						});
					};
				};
			},
			Meta_List, {
				if (domain[2].size == 0) {
					switch(mode,
						'free', {
							handle_bounds = { |value|
								value.asArray };
						},
						'clip', {
							handle_bounds = { |value|
								value.clip(domain.min, domain.max).asList };
						},
						'low', {
							handle_bounds = { |value|
								value.max(domain.min).asList };
						},
						'high', { handle_bounds = { |value|
							value.min(domain.max).asList };
						},
						'wrap', { handle_bounds = { |value|
							value.wrap(domain.min, domain.max).asList };
						},
						'fold', { handle_bounds = { |value|
							value.fold(domain.min, domain.max).asList };
						};
					);
				} {
					handle_bounds = { |value|
						domain[2].do({ |item|
							if (item == value) { ^value.asList };
						});
					};
				};
			},
			Meta_OSSIA_vec2f, {
				if (domain[2].size == 0) {
					switch(mode,
						'free', {
							handle_bounds = { |value|
								value.asArray };
						},
						'clip', {
							handle_bounds = { |value|
								type.asOssiaVec(value.clip(domain.min, domain.max)) };
						},
						'low', {
							handle_bounds = { |value|
								type.asOssiaVec(value.max(domain.min)) };
						},
						'high', { handle_bounds = { |value|
							type.asOssiaVec(value.min(domain.max)) };
						},
						'wrap', { handle_bounds = { |value|
							type.asOssiaVec(value.wrap(domain.min, domain.max)) };
						},
						'fold', { handle_bounds = { |value|
							type.asOssiaVec(value.fold(domain.min, domain.max)) };
						};
					);
				} {
					handle_bounds = { |value|
						domain[2].do({ |item|
							if (item == value) { ^type.asOssiaVec(value) };
						});
					};
				};
			},
			Meta_OSSIA_vec3f, {
				if (domain[2].size == 0) {
					switch(mode,
						'free', {
							handle_bounds = { |value|
								value.asArray };
						},
						'clip', {
							handle_bounds = { |value|
								type.asOssiaVec(value.clip(domain.min, domain.max)) };
						},
						'low', {
							handle_bounds = { |value|
								type.asOssiaVec(value.max(domain.min)) };
						},
						'high', { handle_bounds = { |value|
							type.asOssiaVec(value.min(domain.max)) };
						},
						'wrap', { handle_bounds = { |value|
							type.asOssiaVec(value.wrap(domain.min, domain.max)) };
						},
						'fold', { handle_bounds = { |value|
							type.asOssiaVec(value.fold(domain.min, domain.max)) };
						};
					);
				} {
					handle_bounds = { |value|
						domain[2].do({ |item|
							if (item == value) { ^type.asOssiaVec(value) };
						});
					};
				};
			},
			Meta_OSSIA_vec4f, {
				if (domain[2].size == 0) {
					switch(mode,
						'free', {
							handle_bounds = { |value|
								value.asArray };
						},
						'clip', {
							handle_bounds = { |value|
								type.asOssiaVec(value.clip(domain.min, domain.max)) };
						},
						'low', {
							handle_bounds = { |value|
								type.asOssiaVec(value.max(domain.min)) };
						},
						'high', { handle_bounds = { |value|
							type.asOssiaVec(value.min(domain.max)) };
						},
						'wrap', { handle_bounds = { |value|
							type.asOssiaVec(value.wrap(domain.min, domain.max)) };
						},
						'fold', { handle_bounds = { |value|
							type.asOssiaVec(value.fold(domain.min, domain.max)) };
						};
					);
				} {
					handle_bounds = { |value|
						domain[2].do({ |item|
							if (item == value) { ^type.asOssiaVec(value) };
						});
					};
				};
			};
		);

	}

	bound { |value|
		^handle_bounds.value(value);
	}

	*free { ^'free' }
	*clip { ^'clip' }
	*low { ^'low' }
	*high { ^'high' }
	*wrap { ^'wrap' }
	*fold { ^'fold' }

}
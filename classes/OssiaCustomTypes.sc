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
}

OSSIA_vec3f : OSSIA_FVector
{
	*new {|v1 = 0.0, v2 = 0.0, v3 = 0.0|
		^super.new(3, v1.asFloat, v2.asFloat, v3.asFloat);
	}
}

OSSIA_vec4f : OSSIA_FVector
{
	*new {|v1 = 0.0, v2 = 0.0, v3 = 0.0, v4 = 0.0|
		^super.new(4, v1.asFloat, v2.asFloat, v3.asFloat, v4.asFloat);
	}
}

OSSIA_FVector {

	var am_val, m_sz;

	*new {|sz ... values|
		^super.new.init(sz, values);
	}

	init { |sz,v|

		v.do({|item|
			if((item.isFloat.not) && (item.isInteger.not)) {
				Error("OSSIA: Error! Arguments are not of Float type").throw;
			};
		});

		am_val = v;
		m_sz = sz;
	}

	at {|i| ^am_val[i] }
	put { |index, item| am_val[index] = item }
}

OSSIA_access_mode {
	*bi { ^'bi' }
	*get { ^'get' }
	*set { ^'set' }
}

OSSIA_bounding_mode {
	*free { ^'free' }
	*clip { ^'clip' }
	*low { ^'low' }
	*high { ^'high' }
	*wrap { ^'wrap' }
	*fold { ^'fold' }
}
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

	*new { |min, max, values, type|

		if(not(values.class == Array)) {
			Error("values argument should be an array").throw;
		};

		^super.new.domainCtor(min, max, values, type);
	}

	domainCtor { |min, max, values, tp|

		if(tp.superclass == OSSIA_FVector && (min.notNil && max.notNil)) {
			m_domain = [tp.asOssiaVec(min), tp.asOssiaVec(max), []];
		} {
			m_domain = [min, max, []];
		};

		this.values_(values);
	}

	at { |i| ^m_domain[i] }
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

	json { | type |
		var range = "";

		if (type.isArray && type.isString.not) {
			type.do({ |item, index|
				if (this.values.size > 0) {
					if (this.values[index].size != type.size) {
						Error("values aray should be the same size as the parameter type").throw;
					};
					if(range == "") { range = range ++"{" } { range = range ++",{" };
					range = range ++"\"VALS\":"
					++ if (type.class == String) {
						this.values[index].collect({ |item| "\""++ item ++"\""});
					} { this.values[index] };

				} {
					if (this.min.notNil) {
						if (range == "") { range = range ++"{" } { range = range ++",{" };
						range = range ++"\"MIN\":"++ this.min[index];
					};

					if (this.max.notNil) {
						if (range == "") { range = range ++"{" } { range = range ++"," };
						range = range ++"\"MAX\":"++ this.max[index];
					};
				};

				if (range != "") { range = range ++"}"; };

			});
		} {
			if (this.values.size > 0) {
				range = "{\"VALS\":"
				++ if (type.class == String) {
					this.values.collect({ |item| "\""++ item ++"\""});
				} { this.values };

			} {
				if(this.min.notNil) {
					if(range == "") { range = "{" };
					range = range ++"\"MIN\":"++ this.min;
				};

				if (this.max.notNil) {
					if(range == "") { range = range ++"{" } { range = range ++"," };
					range = range ++"\"MAX\":"++ this.max;
				};
			};

			if (range != "") { range = range ++"}"; };

		};

		if (range != "") { range = ",\"RANGE\":["++ range ++"]"; };

		^range;
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

	*ossiaDefaultValue { ^[0.0, 0.0]; }

	*ossiaNaNFilter { |newVal, oldval|
		^[if (newVal[0].isNaN) { oldval[0] } { newVal[0] },
			if (newVal[1].isNaN) { oldval[1] } { newVal[1] } ];
	}

	*ossiaJson { ^"\"ff\"" }

	*ossiaWidget { |anOssiaParameter|
		var widgets, isCartesian = false, specX = ControlSpec(), specY = ControlSpec();

		if(anOssiaParameter.unit.notNil) {
			if(anOssiaParameter.unit.string == "position.cart2D") { isCartesian = true };
		};

		if(isCartesian) {
			widgets = [
				EZNumber(anOssiaParameter.window, 244@20, anOssiaParameter.name,
					action:{ | val | anOssiaParameter.value_([
						val.value,
						anOssiaParameter.value[1]
					])},labelWidth:100, gap:4@0),
				EZNumber(anOssiaParameter.window, 144@20,
					action:{ | val | anOssiaParameter.value_([
						anOssiaParameter.value[0],
						val.value
					])}, gap:0@0)
			];

			if(anOssiaParameter.domain.min.notNil) {
				widgets[0].controlSpec.minval_(anOssiaParameter.domain.min[0]);
				widgets[1].controlSpec.minval_(anOssiaParameter.domain.min[1]);
				widgets[0].controlSpec.maxval_(anOssiaParameter.domain.max[0]);
				widgets[1].controlSpec.maxval_(anOssiaParameter.domain.max[1]);
				specX.minval_(anOssiaParameter.domain.min[0]);
				specY.minval_(anOssiaParameter.domain.min[1]);
				specX.maxval_(anOssiaParameter.domain.max[0]);
				specY.maxval_(anOssiaParameter.domain.max[1]);
			};

			widgets[0].value_(anOssiaParameter.value[0]);
			widgets[1].value_(anOssiaParameter.value[1]);

			widgets = widgets ++ Slider2D(anOssiaParameter.window, 392@392)
			.x_(specX.unmap(anOssiaParameter.value[0])) // initial location of x
			.y_(specY.unmap(anOssiaParameter.value[1])) // initial location of y
			.action_({ | val | anOssiaParameter.value_([
				specX.map(val.x),
				specY.map(val.y)
			])
			}).onClose_({
				anOssiaParameter.removeFromEvenGui_(anOssiaParameter.name.asSymbol); });

			anOssiaParameter.addToEvenGui_(
				anOssiaParameter.name.asSymbol,
				{
					if (anOssiaParameter.value != [widgets[0].value, widgets[1].value]) {
						widgets[0].value_(anOssiaParameter.value[0]);
						widgets[1].value_(anOssiaParameter.value[1]);
					};

					if (anOssiaParameter.value != [specX.map(widgets[2].x), specY.map(widgets[2].y)]) {
						widgets[2].x_(specX.unmap(anOssiaParameter.value[0]));
						widgets[2].y_(specY.unmap(anOssiaParameter.value[1]));
					};
				};
			);

		} {
			widgets = EZRanger(anOssiaParameter.window, 392@20, anOssiaParameter.name,
				action:{ | val | anOssiaParameter.value_(val.value); },
				labelWidth:100, gap:4@0).onClose_({
				anOssiaParameter.removeFromEvenGui_(anOssiaParameter.name.asSymbol); });

			if(anOssiaParameter.domain.min.notNil) {
				widgets.controlSpec.minval_(anOssiaParameter.domain.min[0]);
				widgets.controlSpec.maxval_(anOssiaParameter.domain.max[1]);
			};

			widgets.value_(anOssiaParameter.value);

			anOssiaParameter.addToEvenGui_(
				name.asSymbol,
				{
					if (anOssiaParameter.value != widgets.value) {
						widgets.value_(anOssiaParameter.value);
					};
				};
			);
		};
	}
}

OSSIA_vec3f : OSSIA_FVector
{
	*new {|v1 = 0.0, v2 = 0.0, v3 = 0.0|
		^super.new(3, v1.asFloat, v2.asFloat, v3.asFloat);
	}

	*asOssiaVec { |anArray|
		^[anArray[0].asFloat, anArray[1].asFloat, anArray[2].asFloat];
	}

	*ossiaDefaultValue { ^[0.0, 0.0, 0.0]; }

	*ossiaNaNFilter { |newVal, oldval|
		^[if (newVal[0].isNaN) { oldval[0] } { newVal[0] },
			if (newVal[1].isNaN) { oldval[1] } { newVal[1] },
			if (newVal[2].isNaN) { oldval[2] } { newVal[2] },];
	}

	*ossiaJson { ^"\"fff\"" }

	*ossiaWidget { |anOssiaParameter|
		var widgets, isCartesian = false, specX = ControlSpec(), specY = ControlSpec(), specZ = ControlSpec();

		widgets = [
			EZNumber(anOssiaParameter.window, 196@20, anOssiaParameter.name,
				action:{ | val | anOssiaParameter.value_([
					val.value,
					anOssiaParameter.value[1],
					anOssiaParameter.value[2]
				])}, labelWidth:100, gap:4@0),
			EZNumber(anOssiaParameter.window, 94@20,
				action:{ | val | anOssiaParameter.value_([
					anOssiaParameter.value[0],
					val.value,
					anOssiaParameter.value[2],
				])}, gap:0@0),
			EZNumber(anOssiaParameter.window, 94@20,
				action:{ | val | anOssiaParameter.value_([
					anOssiaParameter.value[0],
					anOssiaParameter.value[1],
					val.value
				])}, gap:0@0).onClose_({
				anOssiaParameter.removeFromEvenGui_(anOssiaParameter.name.asSymbol); })
		];

		if(anOssiaParameter.unit.notNil) {
			if(anOssiaParameter.unit.string == "position.cart3D") { isCartesian = true };
		};

		if(isCartesian) {

			if(anOssiaParameter.domain.min.notNil) {
				widgets[0].controlSpec.minval_(anOssiaParameter.domain.min[0]);
				widgets[1].controlSpec.minval_(anOssiaParameter.domain.min[1]);
				widgets[0].controlSpec.maxval_(anOssiaParameter.domain.max[0]);
				widgets[1].controlSpec.maxval_(anOssiaParameter.domain.max[1]);
				widgets[2].controlSpec.minval_(anOssiaParameter.domain.min[2]);
				widgets[2].controlSpec.maxval_(anOssiaParameter.domain.max[2]);
				specX.minval_(anOssiaParameter.domain.min[0]);
				specY.minval_(anOssiaParameter.domain.min[1]);
				specX.maxval_(anOssiaParameter.domain.max[0]);
				specY.maxval_(anOssiaParameter.domain.max[1]);
				specZ.minval_(anOssiaParameter.domain.min[2]);
				specZ.maxval_(anOssiaParameter.domain.max[2]);
			};

			widgets[0].value_(anOssiaParameter.value[0]);
			widgets[1].value_(anOssiaParameter.value[1]);
			widgets[2].value_(anOssiaParameter.value[2]);

			widgets = widgets ++ [Slider2D(anOssiaParameter.window, 368@368)
				.x_(specX.unmap(anOssiaParameter.value[0])) // initial location of x
				.y_(specY.unmap(anOssiaParameter.value[1])) // initial location of y
				.action_({ | val | anOssiaParameter.value_([
					specX.map(val.x),
					specY.map(val.y),
					anOssiaParameter.value[2]
				])
				}),
				Slider(anOssiaParameter.window, 20@368)
				.orientation_(\vertical)
				.value_(specZ.unmap(anOssiaParameter.value[2])) // initial location of x
				.action_({ | val | anOssiaParameter.value_([
					anOssiaParameter.value[0],
					anOssiaParameter.value[1],
					specZ.map(val.value)
				])
				})
			];

			anOssiaParameter.addToEvenGui_(
				anOssiaParameter.name.asSymbol,
				{
					if (anOssiaParameter.value != [widgets[0].value, widgets[1].value, widgets[2].value]) {
						widgets[0].value_(anOssiaParameter.value[0]);
						widgets[1].value_(anOssiaParameter.value[1]);
						widgets[2].value_(anOssiaParameter.value[2]);
					};

					if (anOssiaParameter.value !=
						[specX.map(widgets[3].x), specY.map(widgets[3].x), specZ.map(widgets[4].value)]) {
						widgets[3].x_(specX.unmap(anOssiaParameter.value[0]));
						widgets[3].y_(specY.unmap(anOssiaParameter.value[1]));
						widgets[4].value_(specZ.unmap(anOssiaParameter.value[2]));
					};
				};
			);

		} {

			if(anOssiaParameter.domain.min.notNil) {
				widgets[0].controlSpec.minval_(anOssiaParameter.domain.min[0]);
				widgets[0].controlSpec.maxval_(anOssiaParameter.domain.max[0]);
				widgets[1].controlSpec.minval_(anOssiaParameter.domain.min[1]);
				widgets[1].controlSpec.maxval_(anOssiaParameter.domain.max[1]);
				widgets[2].controlSpec.minval_(anOssiaParameter.domain.min[2]);
				widgets[2].controlSpec.maxval_(anOssiaParameter.domain.max[2]);
			};

			anOssiaParameter.addToEvenGui_(
				anOssiaParameter.name.asSymbol,
				{
					if (anOssiaParameter.value != [widgets[0].value, widgets[1].value, widgets[2].value]) {
						widgets[0].value_(anOssiaParameter.value[0]);
						widgets[1].value_(anOssiaParameter.value[1]);
						widgets[2].value_(anOssiaParameter.value[2]);
					};
				};
			);
		};
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

	*ossiaDefaultValue { ^[0.0, 0.0, 0.0, 0.0]; }

	*ossiaNaNFilter { |newVal, oldval|
		^[if (newVal[0].isNaN) { oldval[0] } { newVal[0] },
			if (newVal[1].isNaN) { oldval[1] } { newVal[1] },
			if (newVal[2].isNaN) { oldval[2] } { newVal[2] },
			if (newVal[3].isNaN) { oldval[3] } { newVal[3] } ];
	}

	*ossiaJson { ^"\"ffff\"" }

	*ossiaWidget { |anOssiaParameter|
		var widgets;

		widgets = [
			EZNumber(anOssiaParameter.window, 170@20, anOssiaParameter.name,
				action:{ | val | anOssiaParameter.value_([
					val.value,
					anOssiaParameter.value[1],
					anOssiaParameter.value[2],
					anOssiaParameter.value[3]
				])}, labelWidth:100, gap:4@0),
			EZNumber(anOssiaParameter.window, 70@20,
				action:{ | val | anOssiaParameter.value_([
					anOssiaParameter.value[0],
					val.value,
					anOssiaParameter.value[2],
					anOssiaParameter.value[3]
				])}, gap:0@0),
			EZNumber(anOssiaParameter.window, 70@20,
				action:{ | val | anOssiaParameter.value_([
					anOssiaParameter.value[0],
					anOssiaParameter.value[1],
					val.value,
					anOssiaParameter.value[3]
				])},
				initVal: anOssiaParameter.value[2], gap:0@0),
			EZNumber(anOssiaParameter.window, 70@20,
				action:{ | val | anOssiaParameter.value_([
					anOssiaParameter.value[0],
					anOssiaParameter.value[1],
					anOssiaParameter.value[2],
					val.value
				])}, gap:0@0).onClose_({
				anOssiaParameter.removeFromEvenGui_(anOssiaParameter.name.asSymbol); })
		];

		if(anOssiaParameter.domain.min.notNil) {
			widgets[0].controlSpec.minval_(anOssiaParameter.domain.min[0]);
			widgets[0].controlSpec.maxval_(anOssiaParameter.domain.max[0]);
			widgets[1].controlSpec.minval_(anOssiaParameter.domain.min[1]);
			widgets[1].controlSpec.maxval_(anOssiaParameter.domain.max[1]);
			widgets[2].controlSpec.minval_(anOssiaParameter.domain.min[2]);
			widgets[2].controlSpec.maxval_(anOssiaParameter.domain.max[2]);
			widgets[2].controlSpec.minval_(anOssiaParameter.domain.min[3]);
			widgets[2].controlSpec.maxval_(anOssiaParameter.domain.max[3]);
		};

		widgets[0].value_(anOssiaParameter.value[0]);
		widgets[1].value_(anOssiaParameter.value[1]);
		widgets[2].value_(anOssiaParameter.value[2]);
		widgets[3].value_(anOssiaParameter.value[3]);


		anOssiaParameter.addToEvenGui_(
			anOssiaParameter.name.asSymbol,
			{
				if (anOssiaParameter.value != [widgets[0].value, widgets[1].value, widgets[2].value,
					widgets[3].value]) {
					widgets[0].value_(anOssiaParameter.value[0]);
					widgets[1].value_(anOssiaParameter.value[1]);
					widgets[2].value_(anOssiaParameter.value[2]);
					widgets[3].value_(anOssiaParameter.value[3]);
				};
			};
		);
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

	*ossiaSendMsg {	|anOssiaParameter, addr|
		addr.sendRaw(([anOssiaParameter.path] ++ anOssiaParameter.value).asRawOSC);
	}

	*ossiaBounds { |mode|
		switch(mode,
			'free', {
				^{ |value, domain| this.asOssiaVec(value) };
			},
			'clip', {
				^{ |value, domain| this.asOssiaVec(
				value.collect({ |item, i|
					item.clip(domain.min[i], domain.max[i]);
					});
				)};
			},
			'low', {
				^{ |value, domain| this.asOssiaVec(
					value.collect({ |item, i|
						item.max(domain.min[i])};
					);
				)};
			},
			'high', {
				^{ |value, domain| this.asOssiaVec(
					value.collect({ |item, i|
						item.min(domain.max[i]);
					});
				)};
			},
			'wrap', {
				^{ |value, domain| this.asOssiaVec(
					value.collect({ |item, i|
						item.wrap(domain.min[i], domain.max[i]);
					});
				)};
			},
			'fold', {
				^{ |value, domain| this.asOssiaVec(
					value.collect({ |item, i|
						item.fold(domain.min[i], domain.max[i]);
					});
				)};
			}, {
				^{ |value, domain| domain[2].detect({ |item|
					item == this.asOssiaVec(value) });
				};
		});
	}
}

OSSIA_access_mode {
	*get { ^1 }
	*set { ^2 }
	*bi { ^3 }
}

OSSIA_bounding_mode {

	classvar switch_tree, func_array;
	var <mode, domain, type, handle_bounds;

	*new { |mode, anOssiaType, anOssiaDomain|

		^super.new.ctor(mode, anOssiaType, anOssiaDomain);
	}

	ctor { |bm, tp, dn|

		domain = dn;

		if (domain.values.size != 0) {
			mode = 'values';
		} {
			mode = bm;
		};

		type = tp;

		handle_bounds = type.ossiaBounds(mode);
	}

	bound { |value|
		^handle_bounds.value(value, domain);
	}

	*free { ^'free' }
	*clip { ^'clip' }
	*low { ^'low' }
	*high { ^'high' }
	*wrap { ^'wrap' }
	*fold { ^'fold' }

}
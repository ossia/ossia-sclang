/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

	//-------------------------------------------//
	//                  UTILITIES                //
	//-------------------------------------------//

OSSIA {

	classvar <pallette, server;

	*initClass {

		pallette = QPalette.auto(Color.fromHexString("#1d1c1a"), Color.fromHexString("#222222"));

		pallette.setColor(Color.fromHexString("#222222"), 'window');
		pallette.setColor(Color.fromHexString("#c0c0c0c0"), 'windowText');
		pallette.setColor(Color.fromHexString("#222222"), 'button');
		pallette.setColor(Color.fromHexString("#f0f0f0"), 'buttonText');
		pallette.setColor(Color.fromHexString("#161514"), 'base');
		pallette.setColor(Color.fromHexString("#1e1d1c"), 'alternateBase');
		pallette.setColor(Color.fromHexString("#161514"), 'toolTipBase');
		pallette.setColor(Color.fromHexString("#c0c0c0c0"), 'toolTipText');
		pallette.setColor(Color.fromHexString("#9062400a"), 'highlight');
		pallette.setColor(Color.fromHexString("#FDFDFD"), 'highlightText');

		pallette.setColor(Color.fromHexString("#e0b01e"), 'light'); // welow slider
		pallette.setColor(Color.fromHexString("#62400a"), 'midlight'); // brown contour
		pallette.setColor(Color.fromHexString("#363636"), 'middark'); // widget background
		pallette.setColor(Color.fromHexString("#a7dd0d"), 'baseText'); // green param
		pallette.setColor(Color.fromHexString("#c58014"), 'brightText');
	}

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
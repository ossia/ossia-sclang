/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

	//-------------------------------------------//
	//                   UNITS                   //
	//-------------------------------------------//

OSSIA_Unit
{
	var <string, <extended_types;

	*position { ^OSSIA_position }
	*orientation { ^OSSIA_orientation }
	*color { ^OSSIA_color }
	*angle { ^OSSIA_angle }
	*distance { ^OSSIA_distance }
	*time { ^OSSIA_time }
	*gain { ^OSSIA_gain }
	*speed { ^OSSIA_speed }

	*fmt
	{ | method |

		var separr = method.cs.drop(11).split($.);
		var res = separr[0] ++ "." ++ separr[1].split($')[1];
		^res;
	}
}

OSSIA_position : OSSIA_Unit
{
	*polar
	{ ^this.newCopyArgs(this.fmt(thisMethod),
		"[\"position.polar.r\", \"position.polar.t\"")
	}

	*cart2D
	{ ^this.newCopyArgs(this.fmt(thisMethod),
		"[\"position.cartesian.x\", \"position.cartesian.y\"]")
	}

	*opengl
	{ ^this.newCopyArgs(this.fmt(thisMethod),
		"[\"position.openGL.x\", \"position.openGL.y\", \"position.openGL.z\"]")
	}

	*cart3D
	{ ^this.newCopyArgs(this.fmt(thisMethod),
		"[\"position.cartesian.x\", \"position.cartesian.y\", \"position.cartesian.z\"]")
	}

	*spherical
	{ ^this.newCopyArgs(this.fmt(thisMethod),
		"[\"position.spherical.r\", \"position.spherical.t\", \"position.spherical.p\"]")
	}

	*cylindrical
	{ ^this.newCopyArgs(this.fmt(thisMethod),
		"[\"position.cylindrical.r\", \"position.cylindrical.t\", \"position.cylindrical.z\"]")
	}
}

OSSIA_orientation : OSSIA_Unit
{
	*quaternion
	{ ^this.newCopyArgs(this.fmt(thisMethod),
		"[\"orientation.quaternion.a\", \"orientation.quaternion.b\", \"orientation.quaternion.c\", \"orientation.quaternion.d\"]")
	}

	*axis
	{ ^this.newCopyArgs(this.fmt(thisMethod),
		"[\"orientation.axis.x\", \"orientation.axis.y\", \"orientation.axis.z\", \"orientation.axis.w\"]")
	}

	*euler
	{ ^this.newCopyArgs(this.fmt(thisMethod),
		"[\"orientation.euler.r\", \"orientation.euler.p\", \"orientation.euler.y\"]")
	}
}

OSSIA_color : OSSIA_Unit
{
	*hsv
	{^this.newCopyArgs(this.fmt(thisMethod),
		"[\"color.hsv.h\", \"color.hsv.s\", \"color.hsv.v\"]")
	}

	*rgb
	{ ^this.newCopyArgs(this.fmt(thisMethod),
		"[\"color.rgb.r\", \"color.rgb.g\", \"color.rgb.b\"]")
	}

	*bgr
	{ ^this.newCopyArgs(this.fmt(thisMethod),
		"[\"color.argb.b\", \"color.argb.g\", \"color.argb.r\"]")
	}

	*cmy8
	{ ^this.newCopyArgs(this.fmt(thisMethod),
		"[\"color.cmy8.c\", \"color.cmy8.m\", \"color.cmy8.y\"]")
	}

	*cie_xyz
	{ ^this.newCopyArgs(this.fmt(thisMethod),
		"[\"color.cie_xyz.x\", \"color.cie_xyz.y\", \"color.cie_xyz.z\"]")
	}

	*argb
	{ ^this.newCopyArgs(this.fmt(thisMethod),
		"[\"color.argb.a\", \"color.argb.r\", \"color.argb.g\", \"color.argb.b\"]")
	}

	*rgba
	{ ^this.newCopyArgs(this.fmt(thisMethod),
		"[\"color.rgba.r\", \"color.rgba.g\", \"color.rgba.b\", \"color.rgba.a\"]")
	}

	*argb8
	{ ^this.newCopyArgs(this.fmt(thisMethod),
		"[\"color.argb8.a\", \"color.argb8.r\", \"color.argb8.g\", \"color.argb8.b\"]")
	}

	*rgba8
	{ ^this.newCopyArgs(this.fmt(thisMethod),
		"[\"color.rgba8.r\", \"color.rgba8.g\", \"color.rgba8.b\", \"color.rgba8.a\"]")
	}
}

OSSIA_angle : OSSIA_Unit
{
	*degree { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*radian { ^this.newCopyArgs(this.fmt(thisMethod)) }
}

OSSIA_distance : OSSIA_Unit
{
	*meter { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*kilometer { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*decimeter { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*centimeter { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*millimeter { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*micrometer { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*nanometer { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*picometer { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*inch { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*foot { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*mile { ^this.newCopyArgs(this.fmt(thisMethod)) }
}

OSSIA_time : OSSIA_Unit
{
	*second { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*bark { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*bpm { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*cent { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*frequency { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*mel { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*midinote { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*ms { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*speed { ^this.newCopyArgs(this.fmt(thisMethod)) }
}

OSSIA_gain : OSSIA_Unit
{
	*linear { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*midigain { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*decibel { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*decibel_raw { ^this.newCopyArgs(this.fmt(thisMethod)) }
}

OSSIA_speed : OSSIA_Unit
{
	*meter_per_second { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*miles_per_hour { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*kilometer_per_hour { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*knot { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*foot_per_second { ^this.newCopyArgs(this.fmt(thisMethod)) }
	*foot_per_hour { ^this.newCopyArgs(this.fmt(thisMethod)) }
}
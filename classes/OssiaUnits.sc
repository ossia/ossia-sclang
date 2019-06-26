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

OSSIA_Unit {
	*position{ ^OSSIA_position }
	*orientation{^OSSIA_orientation }
	*color{^OSSIA_color}
	*angle{^OSSIA_angle}
	*distance{^OSSIA_distance}
	*time{^OSSIA_time}
	*gain{^OSSIA_gain}
	*speed{^OSSIA_speed}

	*fmt { |method|
		var separr = method.cs.drop(11).split($.);
		var res = separr[0] ++ "." ++ separr[1].split($')[1];
		^res;
	}
}

OSSIA_position : OSSIA_Unit {
	*cart2D{ ^this.fmt(thisMethod) }
	*cart3D{ ^this.fmt(thisMethod)}
	*spherical{ ^this.fmt(thisMethod) }
	*polar{ ^this.fmt(thisMethod) }
	*opengl{^this.fmt(thisMethod) }
	*cylindrical{^this.fmt(thisMethod) }
}

OSSIA_orientation : OSSIA_Unit {
	*quaternion{^this.fmt(thisMethod) }
	*euler{^this.fmt(thisMethod) }
	*axis{^this.fmt(thisMethod) }
}

OSSIA_color : OSSIA_Unit {
	*argb{^this.fmt(thisMethod) }
	*rgba{^this.fmt(thisMethod) }
	*rgb{^this.fmt(thisMethod) }
	*bgr{^this.fmt(thisMethod) }
	*argb8{^this.fmt(thisMethod) }
	*hsv{^this.fmt(thisMethod) }
	*cmy8{^this.fmt(thisMethod) }
	*css{^this.fmt(thisMethod) } // TODO
}

OSSIA_angle : OSSIA_Unit {
	*degree{^this.fmt(thisMethod) }
	*radian{^this.fmt(thisMethod) }
}

OSSIA_distance : OSSIA_Unit {
	*meter{^this.fmt(thisMethod) }
	*kilometer{^this.fmt(thisMethod) }
	*decimeter{^this.fmt(thisMethod) }
	*centimeter{^this.fmt(thisMethod) }
	*millimeter{^this.fmt(thisMethod) }
	*micrometer{^this.fmt(thisMethod) }
	*nanometer{^this.fmt(thisMethod) }
	*picometer{^this.fmt(thisMethod) }
	*inch{^this.fmt(thisMethod) }
	*foot{^this.fmt(thisMethod) }
	*mile{^this.fmt(thisMethod) }
}

OSSIA_time : OSSIA_Unit {
	*second{^this.fmt(thisMethod) }
	*bark{^this.fmt(thisMethod) }
	*bpm{^this.fmt(thisMethod) }
	*cent{^this.fmt(thisMethod) }
	*frequency{^this.fmt(thisMethod) }
	*mel{^this.fmt(thisMethod) }
	*midi_pitch{^this.fmt(thisMethod) }
	*millisecond{^this.fmt(thisMethod) }
	*playback_speed{^this.fmt(thisMethod) }
}

OSSIA_gain : OSSIA_Unit {
	*linear{^this.fmt(thisMethod) }
	*midigain{^this.fmt(thisMethod) }
	*decibel{^this.fmt(thisMethod) }
	*decibel_raw{^this.fmt(thisMethod) }
}

OSSIA_speed : OSSIA_Unit {
	*meter_per_second{^this.fmt(thisMethod) }
	*miles_per_hour{^this.fmt(thisMethod) }
	*kilometer_per_hour{^this.fmt(thisMethod) }
	*knot{^this.fmt(thisMethod) }
	*foot_per_second{^this.fmt(thisMethod) }
	*foot_per_hour{^this.fmt(thisMethod) }
}
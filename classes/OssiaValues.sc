/*
* This project is a fork of Pierre Cohard's ossia-supercollider
* https://github.com/OSSIA/ossia-supercollider.git
* Form his sclang files, the aim is to provide the same message structure
* specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
* and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
*/

OSSIA_domain
{
	var m_domain;

	*new
	{ | min, max, values, type |

		if (not(values.class == Array))
		{ Error("values argument should be an array").throw };

		^super.new.domainCtor(min, max, values, type);
	}

	domainCtor
	{ | min, max, values, tp |

		if (tp.superclass == OSSIA_FVector && (min.notNil && max.notNil))
		{
			m_domain = [tp.asOssiaVec(min), tp.asOssiaVec(max), []];
		} {
			m_domain = [min, max, []];
		};

		this.values_(values);
	}

	at { | i | ^m_domain[i] }
	put { | index, item | m_domain[index] = item }

	min      { ^m_domain[0] }
	min_     { | v | m_domain[0] = v }
	max      { ^m_domain[1] }
	max_     { | v | m_domain[1] = v }
	values   { ^m_domain[2] }
	values_
	{ | anArray |

		if ((not(anArray.class == Array)) && (anArray.notNil))
		{ Error("values argument should be an array").throw };

		m_domain[2] = anArray;
	}

	json
	{ | type |
		var range = "";

		if (type.isArray && type.isString.not)
		{
			type.do({ |item, index|

				if (this.values.size > 0)
				{
					if (this.values[index].size != type.size)
					{ Error("values aray should be the same size as the parameter type").throw };

					if (range == "") { range = range ++"{" } { range = range ++",{" };

					range = range ++"\"VALS\":"
					++ if (type.class == String)
					{
						this.values[index].collect({ |item| "\""++ item ++"\""});
					} { this.values[index] };
				} {
					if (this.min.notNil)
					{
						if (range == "") { range = range ++"{" } { range = range ++",{" };
						range = range ++"\"MIN\":"++ this.min[index];
					};

					if (this.max.notNil)
					{
						if (range == "") { range = range ++"{" } { range = range ++"," };
						range = range ++"\"MAX\":"++ this.max[index];
					};
				};

				if (range != "") { range = range ++"}"; };
			});
		} {
			if (this.values.size > 0)
			{
				range = "{\"VALS\":"
				++ if (type.class == String)
				{
					this.values.collect({ |item| "\""++ item ++"\""});
				} { this.values };
			} {
				if (this.min.notNil)
				{
					if(range == "") { range = "{" };
					range = range ++"\"MIN\":"++ this.min;
				};

				if (this.max.notNil)
				{
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

OSSIA_access_mode
{
	*get { ^1 }
	*set { ^2 }
	*bi { ^3 }
}

OSSIA_bounding_mode
{
	var <mode, domain, type, handle_bounds;

	*new
	{ | mode, anOssiaType, anOssiaDomain |

		^super.new.ctor(mode, anOssiaType, anOssiaDomain);
	}

	ctor
	{ | bm, tp, dn |

		domain = dn;

		if (domain.values.size != 0)
		{
			mode = 'values';
		} {
			mode = bm;
		};

		type = tp;

		handle_bounds = type.ossiaBounds(mode);
	}

	bound { | value | ^handle_bounds.value(value, domain) }

	*free { ^'free' }
	*clip { ^'clip' }
	*low { ^'low' }
	*high { ^'high' }
	*wrap { ^'wrap' }
	*fold { ^'fold' }
}
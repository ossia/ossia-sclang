/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

	//-------------------------------------------//
	//                 PROTOCOLS                 //
	//-------------------------------------------//

OSSIA_OSCProtocol
{
	var remoteAddr;
	var remotePort;
	var localPort;
	var device;
	var netAddr;

	*new { |remoteAddr, remotePort, localPort, device|
		^this.newCopyArgs(remoteAddr, remotePort, localPort, device).oscProtocolCtor;
	}

	oscProtocolCtor {
		netAddr = NetAddr(remoteAddr, remotePort);
		device.tree(parameters_only: true).do(this.instantiateParameter(_));
	}

	push { |anOssiaParameter|
		netAddr.sendRaw(
			([anOssiaParameter.path] ++ anOssiaParameter.value).asRawOSC);
	}

	instantiateParameter { |anOssiaParameter|
		var path = anOssiaParameter.path;

		OSCdef(path.asSymbol,
			{ |msg|
				if (msg.size == 2) {
					anOssiaParameter.valueQuiet(msg[1]);
				} { msg.removeAt(0);
					anOssiaParameter.valueQuiet(msg);
				};
			},
			path, recvPort: localPort);

		this.push(anOssiaParameter);
	}

	freeParameter { |anOssiaParameter|
		OSCdef(anOssiaParameter.path.asSymbol).free;
	}

	free {
		device.tree(parameters_only: true).do(this.freeParameter(_));
		^super.free;
	}
}

OSSIA_OSCQSProtocol
{
	var name;
	var osc_port;
	var ws_port;
	var device;
	var netAddr;
	var ws_server;
	var ws_connect_count = 0;
	var zeroconf_service;
	var host_info;
	var json_tree;

	*new { |name, osc_port, ws_port, device|
		^this.newCopyArgs(name, osc_port, ws_port, device).oscQuerryProtocolCtor;
	}

	oscQuerryProtocolCtor {

		ws_server = WebSocketServer(ws_port, name, "_oscjson._tcp");
		zeroconf_service = ZeroconfService(name, "_oscjson._tcp", ws_port);
		host_info = "{"
		++"\"NAME\":\""++ name ++"\""
		++",\"OSC_PORT\":"++ osc_port ++""
		++",\"OSC_TRANSPORT\":\"UDP\""
		++",\"EXTENSIONS\":"
		++"{"
		++"\"TYPE\":true"
		++",\"ACCESS\":true"
		++",\"VALUE\":true"
		++",\"RANGE\":true"
		++",\"TAGS\":true"
		++",\"CLIPMODE\":true"
		++",\"UNIT\":true"
		++",\"CRITICAL\":true"
		++",\"DESCRIPTION\":true"
		++",\"HTML\":true"
		++",\"OSC_STREAMING\":true"
		++",\"LISTEN\":true"
		++",\"ECHO\":true"
		++",\"PATH_CHANGED\":false"
		++",\"PATH_RENAMED\":true"
		++",\"PATH_ADDED\":true"
		++",\"PATH_REMOVED\":true"
		++"}"
		++"}";

		ws_server.onNewConnection = { |con|
			postln(format("[websocket-server] new connection from %:%", con.address, con.port));
			ws_connect_count = ws_connect_count + 1;

			con.onTextMessageReceived = { |msg|
				var command = msg.parseYAML;
				postln(format("[websocket-server] new message from: %:%", con.address, con.port));
				postln(msg);
				con.writeText(msg);
				if (command["COMMAND"] == "START_OSC_STREAMING") {
					netAddr = NetAddr(con.address,
						command["DATA"]["LOCAL_SERVER_PORT"].asInt;
					);
				};
			};

			con.onOscMessageReceived = { |array|
				postln(format("[websocket-server] new osc message from: %:%", con.address, con.port));
				postln(array);
			};
		};

		ws_server.onHttpRequestReceived = { |req|

			postln("[http-server] request received");
			postln(format("[http-server] uri: %", req.uri));
			postln(req.query);

			if (req.query.isEmpty().not()) {
				postln(format("[http-server] query: %", req.query));
			};

			if (req.uri == "/") {
				if (req.query == "HOST_INFO") {
					req.replyJson(host_info);
					if (ws_server[ws_connect_count - 1].notNil) { ws_server[ws_connect_count - 1].writeText(host_info) };
				} {
					json_tree = "{\"FULL_PATH\":\"/\",\"CONTENTS\":"++ OSSIA_Tree.stringify(device.children) ++"}";
					ws_server[ws_connect_count - 1].writeText(json_tree);
				}
			}
		};

		device.tree(parameters_only: true).do(this.instantiateOSC(_));
	}

	push { |anOssiaParameter|
		netAddr.sendRaw(
			([anOssiaParameter.path] ++ anOssiaParameter.value).asRawOSC);
	}

	instantiateParameter { |anOssiaParameter|
		this.instantiateOSC(anOssiaParameter);
	}

	instantiateOSC { |anOssiaParameter|
		var path = anOssiaParameter.path;

		OSCdef(path.asSymbol,
			{ |msg|
				if (msg.size == 2) {
					anOssiaParameter.valueQuiet(msg[1]);
				} { msg.removeAt(0);
					anOssiaParameter.valueQuiet(msg);
				};
			},
			path, recvPort: osc_port);
	}

	freeParameter { |anOssiaNode|
		if (anOssiaNode.class == OSSIA_Parameter) { OSCdef(anOssiaNode.path.asSymbol).free };
	}

	free {
		device.tree(parameters_only: true).do(this.freeParameter(_));
		^super.free;
	}
}

OSSIA_Tree
{
	*stringify { |ossiaNodes|

		var json = "";

		if (ossiaNodes.isArray) {

			ossiaNodes.do({ |item, count|
				json = json
				++ if (count == 0) {"{"} {","}
				++ this.fmt(item)
			});

			json = json ++ "}";

		} {
			json = json ++ this.fmt(ossiaNodes);
		};

		^json;
	}

	*fmt { |anOssiaNode|

		^"\""++ anOssiaNode.name ++"\":"
		++"{\"FULL_PATH\":\""++ anOssiaNode.path ++"\""
		++ if (anOssiaNode.class == OSSIA_Parameter) {
			",\"TYPE\":"
			++ switch (anOssiaNode.type.class,
				Meta_Float, "\"f\"",
				Meta_Integer, "\"i\"",
				Meta_OSSIA_vec2f, "\"ff\"",
				Meta_OSSIA_vec3f, "\"fff\"",
				Meta_OSSIA_vec4f, "\"ffff\"",
				Meta_Boolean, "\"F\"",
				Meta_Impulse, "\"I\"",
				Meta_Signal, "\"I\"",
				Meta_String, "\"s\"",
				Meta_Array, "\"l\"",
				Meta_Char, "\"c\""
			)
			++ if (anOssiaNode.domain.min.notNil && anOssiaNode.domain.max.notNil) {
				",\"VALUE\":"++ anOssiaNode.value
			} { "" }
			++ if (anOssiaNode.domain.min.notNil && anOssiaNode.domain.max.notNil) {
				",\"RANGE\":[{\"MIN\":"++ anOssiaNode.domain.min ++",\"MAX\":"++ anOssiaNode.domain.max ++"}]"
			} { "" }
			++",\"CLIPMODE\":\""++ anOssiaNode.bounding_mode.mode ++"\""
			++ if (anOssiaNode.domain.values.notNil) {
				",\"VALUES\":[\""++ anOssiaNode.domain.values ++"\"]"
			} { "" }
			++ if (anOssiaNode.unit.notNil) {
				",\"UNIT\":[\""++ anOssiaNode.unit ++"\"]"
			} { "" }
			++",\"ACCESS\":\""++ anOssiaNode.access_mode ++"\""
		} { "" }
		++ if (anOssiaNode.description.notNil) {
			",\"DESCRIPTION\":\""++ anOssiaNode.description ++"\""
		} { "" }
		++ if (anOssiaNode.children.isEmpty.not) {
			",\"CONTENTS\":"++ this.stringify(anOssiaNode.children)
		} { "" }
		++"}"
	}
}
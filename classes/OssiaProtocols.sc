/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

	//-------------------------------------------//
	//                   OSC                     //
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
		anOssiaParameter.type.ossiaSendMsg(anOssiaParameter, netAddr);
	}

	instantiateParameter { |anOssiaParameter|
		var path = anOssiaParameter.path;

		OSCFunc(
			{ |msg|
				if (msg.size == 2) {
					anOssiaParameter.valueQuiet(msg[1]);
				} {
					msg.removeAt(0);
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

	//-------------------------------------------//
	//              OSCQUERY SERVER              //
	//-------------------------------------------//

OSSIA_OSCQSProtocol
{
	var name;
	var osc_port;
	var ws_port;
	var device;
	var netAddr;
	var ws_server;
	var zeroconf_service;
	var host_info;
	var json_tree;
	var <dictionary;

	*new { |name, osc_port, ws_port, device|
		^this.newCopyArgs(name, osc_port, ws_port, device).oscQuerryProtocolCtor;
	}

	oscQuerryProtocolCtor {

		netAddr = NetAddr();
		ws_server = WebSocketServer(ws_port).granularity_(25);
		zeroconf_service = ZeroconfService(name, "_oscjson._tcp", ws_port);
		dictionary = IdentityDictionary.new;
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

			con.onTextMessageReceived = { |msg|
				var command = msg.parseYAML;
				postln(format("[websocket-server] new message from: %:%", con.address, con.port));
				postln(msg);
				switch (command["COMMAND"],
					"START_OSC_STREAMING", {
						netAddr.hostname_(con.address);
						netAddr.port_(command["DATA"]["LOCAL_SERVER_PORT"].asInt);
					}, "LISTEN", {
						dictionary.at(command["DATA"].asSymbol).listening_(true);
					}, "IGNORE", {
						dictionary.at(command["DATA"].asSymbol).listening_(false);
				});
			};

			con.onOscMessageReceived = { |array|
				postln(format("[websocket-server] new osc message from: %:%", con.address, con.port));
				postln(array);
				dictionary.at(array[0].asSymbol).valueQuiet(array[1]);
			};
		};

		ws_server.onHttpRequestReceived = { |req|

			postln("[http-server] request received");
			postln(format("[http-server] uri: %", req.uri));
			postln(req.query);

			if (req.query == "VALUE") {
				req.replyJson("{\"VALUE\": " ++ dictionary.at(req.uri.asSymbol).value ++"}");
				//this.push(dictionary.at(req.uri.asSymbol));
				postln(format("[http-server] reply sent"));
			};

			if (req.uri == "/") {
				if (req.query == "HOST_INFO") {
					req.replyJson(host_info);
				} {
					json_tree = "{\"FULL_PATH\":\"/\",\"CONTENTS\":"++ OSSIA_Tree.stringify(device.children) ++"}";
					req.replyJson(json_tree);
				}
			}
		};

		ws_server.onDisconnection = { |con|
			postln(format("[websocket-server] client %:% disconnected", con.address, con.port));
		};

		device.tree(parameters_only: true).flat.do(this.instantiateParameter(_));
	}

	push { |anOssiaParameter|

		ws_server.numConnections.do({ |i|
			// ws_server[i].writeOsc(anOssiaParameter.path, anOssiaParameter.value)});
			ws_server[i].writeOsc([anOssiaParameter.path] ++ anOssiaParameter.value)});

		if (anOssiaParameter.critical.not && (ws_server.numConnections == 0)) {
			anOssiaParameter.type.ossiaSendMsg(anOssiaParameter, netAddr);
		};
	}

	instantiateParameter { |anOssiaParameter|

		dictionary.put(anOssiaParameter.path.asSymbol, anOssiaParameter);

		if (anOssiaParameter.critical.not) {

			var path = anOssiaParameter.path;

			OSCFunc(
				{ |msg|
					if (msg.size == 2) {
						anOssiaParameter.valueQuiet(msg[1]);
					} {
						msg.removeAt(0);
						anOssiaParameter.valueQuiet(msg);
					};
				}, path, recvPort: osc_port);
		};
	}

	freeParameter { |anOssiaNode|

		if (anOssiaNode.class == OSSIA_Parameter) {
			if (anOssiaNode.critical.not) {
				OSCdef(anOssiaNode.path.asSymbol).free;
			};
		};
	}

	free {
		device.tree().flat.do(this.freeParameter(_));
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
				++ item.json
			});

			json = json ++ "}";

		} {
			json = json ++ ossiaNodes.json;
		};

		^json;
	}
}

	//-------------------------------------------//
	//              OSCQUERY MIRROR              //
	//-------------------------------------------//

OSSIA_OSCQMProtocol
{
	var host_addr;
	var device;
	var netAddr;
	var ws_client;
	var zeroconf_service;
	var host_info;
	var <dictionary;

	*new { |host_addr, device|
		^this.newCopyArgs(host_addr, device).oscQuerryProtocolCtor;
	}

	oscQuerryProtocolCtor {

		ws_client = WebSocketClient();
		zeroconf_service = ZeroconfBrowser("_oscjson._tcp", host_addr, { |target|
			postln(format("[zeroconf] target resolved: % (%) at address: %:%",
				target.name, target.domain, target.address, target.port));

			target.onDisconnected = {
				postln(format("[zeroconf] target % is now offline", target.name));
				this.free;
			};

			ws_client.connect(target.address, target.port);
			netAddr = NetAddr(target.address.asString);
		});

		dictionary = IdentityDictionary.new;

		ws_client.onConnected = {
			// client connection callback
			postln("[websocket-client] connected!");

			// requests root and host_info (for oscquery)
			ws_client.request("/?HOST_INFO");
			ws_client.request("/");
		};

		ws_client.onTextMessageReceived = { |msg|
			var command = msg.parseYAML;
			postln(format("[websocket-client] new message from: %:%", msg));
			postln(msg);

			switch (command["COMMAND"],
					"LISTEN", {
					dictionary.at(command["DATA"].asSymbol).listening_(true);
				}, "IGNORE", {
					dictionary.at(command["DATA"].asSymbol).listening_(false);
			});
		};

		ws_client.onOscMessageReceived = { |array|
			postln(format("[websocket-client] new osc message from: %:%", array));
			postln(array);
		};

		ws_client.onHttpReplyReceived = { |reply|
			var host = reply.body.parseYAML;
			postln(format("[http-client] reply from server for uri: %, %", reply.uri, reply.body));

			if (host["OSC_PORT"].notNil) {
				netAddr.port_(host["OSC_PORT"].asInt);
			};
		};

		ws_client.onDisconnected = { |con|
			postln(format("[websocket-client] client %:% disconnected", con.address, con.port));
		};

		device.tree(parameters_only: true).flat.do(this.instantiateParameter(_));
	}

	push { |anOssiaParameter|

		if (anOssiaParameter.critical) {
			ws_client.writeOsc(anOssiaParameter.path, anOssiaParameter.value);
		} {
			anOssiaParameter.type.ossiaSendMsg(anOssiaParameter, netAddr);
		};
	}

	instantiateParameter { |anOssiaParameter|

		dictionary.put(anOssiaParameter.path.asSymbol, anOssiaParameter);
	}

	freeParameter { |anOssiaNode|

		if (anOssiaNode.class == OSSIA_Parameter) {
			if (anOssiaNode.critical.not) {
				OSCdef(anOssiaNode.path.asSymbol).free;
			};
		};
	}

	free {
		device.tree().flat.do(this.freeParameter(_));
		^super.free;
	}
}
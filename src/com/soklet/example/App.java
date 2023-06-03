/*
 * Copyright 2022-2023 Revetware LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soklet.example;

import com.soklet.Soklet;
import com.soklet.SokletConfiguration;
import com.soklet.annotation.GET;
import com.soklet.annotation.QueryParameter;
import com.soklet.annotation.Resource;
import com.soklet.core.Response;
import com.soklet.core.Server;
import com.soklet.core.Utilities;
import com.soklet.core.impl.MicrohttpServer;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="https://www.revetware.com">Mark Allen</a>
 */
public class App {
	@Resource
	public static class ExampleResource {
		@GET("/")
		public String index() {
			return "Hello, world!";
		}

		@GET("/test-input")
		public Response testInput(@QueryParameter Integer input) {
			return new Response.Builder()
					.headers(Map.of("Content-Type", Set.of("application/json; charset=UTF-8")))
					// A real application would not construct JSON in this manner
					.body(String.format("{\"input\": %d}", input))
					.build();
		}
	}

	public static void main(String[] args) throws Exception {
		int port = 8080;
		Server server = new MicrohttpServer.Builder(port).build();
		SokletConfiguration sokletConfiguration = new SokletConfiguration.Builder(server).build();

		// In an interactive console environment, it makes sense to stop on `Return` keypress.
		// In a Docker container, it makes sense to join on the current thread (no stdin)
		boolean stopOnKeypress = !"true".equals(System.getenv("RUNNING_IN_DOCKER"));

		try (Soklet soklet = new Soklet(sokletConfiguration)) {
			soklet.start();

			System.out.printf("Soklet Example App started on port %d (%s virtual threads).\n",
					port, Utilities.virtualThreadsAvailable() ? "with" : "without");

			if (stopOnKeypress) {
				System.out.println("Press [enter] to exit");
				System.in.read();
			} else {
				Thread.currentThread().join();
			}
		}
	}	
}

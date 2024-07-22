# GraphDB GeoSPARQL/stSPARQL Plugin

This is a modified version of the GraphDB GeoSPARQL plugin. It supports a subset of stSPARQL, which is necessary for running the queries contained in [GeoQuestions1089](https://github.com/AI-team-UoA/GeoQuestions1089/).

More information about it is available in the GraphDB documentation here:
http://graphdb.ontotext.com/documentation/enterprise/geosparql-support.html

## Building the plugin

The plugin is a Maven project.

Run ` mvn clean package -Dmaven.test.skip=true` to build the plugin and execute the tests.

The built plugin can be found in the `target` directory:

- `graphdb-geosparql-plugin-graphdb-plugin.zip`

## Installing the plugin

External plugins are installed under `lib/plugins` in the GraphDB distribution
directory. To install the plugin follow these steps:

1. Remove the directory containing another version of the plugin from `lib/plugins` (e.g. `graphdb-geosparql-plugin`).
1. Unzip the built zip file in `lib/plugins`.
1. Restart GraphDB. 

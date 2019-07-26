import 'dart:async';
import 'dart:convert';

import 'package:resource/resource.dart' as resource;

/// Returns a Map based on the logging schema file.
Future<Map<String, dynamic>> getLoggingSchema(String version) async {
  resource.Resource jsonFile = new resource.Resource(
      'packages/platform/specs/app/logging/$version/schema.json');
  String jsonFileString = await jsonFile.readAsString();
  return JSON.decode(jsonFileString) as Map<String, dynamic>;
}

/// Returns a Map based on the telemetry schema file.
Future<Map<String, dynamic>> getTelemetrySchema(String version) async {
  resource.Resource jsonFile = new resource.Resource(
      'packages/platform/specs/app/telemetry/$version/schema.json');
  String jsonFileString = await jsonFile.readAsString();
  return JSON.decode(jsonFileString) as Map<String, dynamic>;
}

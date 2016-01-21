import 'package:json_schema/json_schema.dart';
import 'dart:async';

class Specs {
  static Future<Schema> Logging = Schema.createSchemaFromUrl('../../specs/app/logging/0.0.1/schema.json');
}
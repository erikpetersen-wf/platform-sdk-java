@TestOn('vm')
import 'dart:io';

import 'package:test/test.dart';
import 'package:platform/platform.dart';

main() async {
  group('Platform: verify', () {
    test('that logging specs can be retrieved from each version', () async {
      var dir = new Directory('lib/specs/app/logging/');

      List contents = dir.listSync();
      for (var fileOrDir in contents) {
        if (fileOrDir is Directory) {
          String version = fileOrDir.path.split("/").last;
          Map stuff = await getLoggingSchema(version);
          expect(stuff, isNotNull);
          expect(stuff["version"], equals(version));
        }
      }
    });

    test('that telemetry specs can be retrieved from each version', () async {
      var dir = new Directory('lib/specs/app/telemetry/');

      List contents = dir.listSync();
      for (var fileOrDir in contents) {
        if (fileOrDir is Directory) {
          String version = fileOrDir.path.split("/").last;
          Map stuff = await getTelemetrySchema(version);
          expect(stuff, isNotNull);
          expect(stuff["version"], equals(version));
        }
      }
    });
  });
}

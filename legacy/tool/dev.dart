library tool.dev;

import 'package:dart_dev/dart_dev.dart' show dev, config;

main(List<String> args) async {
  // Define the entry points for static analysis.
  config.analyze
    ..entryPoints = ['lib/', 'test/unit/', 'tool/']
    ..strong = true
    ..fatalWarnings = true;

  // Configure the port on which examples should be served.
  config.examples.port = 9000;

  // Define the directories to include when running the
  // Dart formatter.
  config.format.directories = ['lib/', 'test/', 'tool/'];

  // Define the location of your test suites.
  config.test
    ..unitTests = ['test/unit/']
    ..platforms = ['vm'];

  // Execute the dart_dev tooling.
  await dev(args);
}

library tool.dev;

import 'package:dart_dev/dart_dev.dart' show dev, config;

main(args) async {
  // Define the entry points for static analysis.
  config.analyze
    ..entryPoints = ['lib/', 'test/', 'tool/']
    ..strong = true
    ..fatalWarnings = true;

  // Configure whether or not the HTML coverage report should be generated.
  config.coverage.html = true;

  // Configure the port on which examples should be served.
  config.examples.port = 9000;

  // Define the directories to include when running the
  // Dart formatter.
  config.format.directories = ['lib/', 'test/', 'tool/'];

  // Define the location of your test suites.
  config.test
    ..unitTests = ['test/unit/']
    ..platforms = ['vm', 'content-shell'];

  // Execute the dart_dev tooling.
  await dev(args as List<String>) as List<String>;
}

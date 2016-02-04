#Application Telemetry Specification (Dart , C#)
 
###Status: draft

As our gen2 application frameworks are built out and we move to a unified compositional architecture, telemetry will be critical to ensuring our developers and stakeholders can maintain healthy and performant next-gen systems. 

Standardised telemetry across all of our applications is going to be critical. This document details the standards we will use to gather our telemetry.

This spec is closely related to the existing [Gen2 Telemetry Specification](https://docs.google.com/document/d/1g0QUF0kRRKRzODOP0-W2feL_6QjQxSLl-r5cnZXaUTc/edit#heading=h.8zjz5hyt3t31).

###Sources of telemetry
Telemetry data is expected to originate from:

- Dart-based (javascript) browser applications involving both authenticated and unauthenticated users from both trusted and untrusted networks.
- Dart-based server applications involving only authenticated  users from only trusted networks.

While having a holistic view of an application and its interactions with other services/users is desirable, we must also balance the performance impact this may have on the browser/network connections and by extension, the user.  This requires we support batching telemetry data and bulk-sending them.

###Authenticated vs Non-Authenticated

There are use cases for both authenticated and unauthenticated telemetry.  We expect the frequency of telemetry to be higher for authenticated users than for unauthenticated users.  One concern with allowing unauthenticated telemetry is the potential for DOS types of attacks.  To mitigate this risk, we can rate-limit telemetry for unauthenticated users based on headers.

###Browser Client telemetry

All Workiva browser/mobile clients must adhere to the same telemetry standards. Each browser client is expected to use the same library for telemetry to ensure this expectation.  This library will forward metrics to a specific endpoint hosted on a centralised endpoint per environment (e.g. local, wk-dev, sandbox).

Those endpoints will be web-service endpoints located at:___________________________.

It accepts json encoded telemetry data of the format:
```json
{
  "version": "0.9.0",
  "timestamp": "2000-01-01T00:00:00.123456Z",
  "type": "telemetry",
  "client": "user analytics viewer==1.2.3",
  "metadata": {
    "path": "/uav",
    "host": "wk-dev",
    "service": "ie::bigdata_fe, not necessarily used in browser-applications",
    "correlationId": "1234",
    "accountId": "1234",
    "documentId": "1234",
    "userId": "1234",
    "anything_else": "all other metadata"
  },
  "time_series": {
    "rpc_total_count": {
      "values": [
        {
          "timestamp": "2000-01-01T00:00:00Z",
          "value": 212
        },
        {
          "timestamp": "2000-01-01T00:00:01Z",
          "value": 121
        }
      ],
      "metadata": {
        "rpc": "1"
      },
      "sample_rate": 0.5
    },
    "exec_time": {
      "units": "ms",
      "values": [
        {
          "timestamp": "2000-01-01T00:00:00Z",
          "value": 21
        },
        {
          "timestamp": "2000-01-01T00:00:01Z",
          "value": 12
        }
      ]
    }
  },
  "counters": {
    "status_code.200": {
      "values": [
        {
          "timestamp": "2000-01-01T00:00:00Z",
          "value": 1
        }
      ],
      "metadata": {
        "http": "1"
      }
    },
    "read.ops": {
      "values": [
        {
          "timestamp": "2000-01-01T00:00:00Z",
          "value": 1234
        }
      ]
    }
  },
  "gauges": {
    "read.iops": {
      "values": [
        {
          "timestamp": "2000-01-01T00:00:00Z",
          "value": 4321
        }
      ]
    }
  }
}
```

###Format specification
**Types:**

field | required | description
----- | -------- | ----------- 
| version | yes | semver compatible version string
timestamp | yes | RFC3339 compatible string. Precision not greater than microsecond. For instance, golang can produce nanosecond timestamps that are not compatible with other languages' parsing libraries. The top level timestamp corresponds to the time the telemetry entry was written.
type | yes | only the value “telemetry” is allowed
client | no | a string representing the client/SDK used to generate the telemetry. Useful to help track down message errors.
metadata | no | dictionary of key/value pairs which to apply as custom tags/dimensions to the telemetry. 
time_series | no | dictionary of time series data. See description below.
counters | no | dictionary of counter data. See description below.
gauges | no | dictionary of gauge data. See description below.

###Data Types
**Value**

This is a base-type used within the time series and timer data types.

field | required | description
----- | -------- | -----------
timestamp | no | RFC3339 compatible string. Precision not greater than microsecond.   This timestamp corresponds to the time the metric was measured, which is probably earlier than the top level timestamp. However, if missing, use the timestamp from the top level of the message.
value | yes | an integer or float

###Time Series

field | required | description
----- | -------- | -----------
units | no | units of the measurement, as a string
values | yes | a list of value dictionaries. See above description.
sample_rate | no | a float indicating the uniform sampling rate used to collect the datapoints. 0.0 == 0%, 1.0 == 100%.


A time_series datatype is used when you want to simply inject an arbitrary metric, with arbitrary units, into the ecosystem and want to rely on the default statistical aggregation supplied by downstream pipelines. For instance, 

```json
"time_series": {
     "kittyhawk.test.execution_time": {
        "units": "s",
        "values": [{"timestamp": "2000-01-01T00:00:00Z", "value": 317},
             {"timestamp": "2000-01-01T00:00:01Z", "value": 713}],
        "sample_rate": 0.5
    }
}
```

NOTE: Time Series may have units or be unit-less.

###Counter
A counter is just a gauge for an AtomicLong instance. You can increment or decrement its value. 

field | required | description
----- | -------- | -----------
units | no | units of the measurement, as a string
values | yes | a list of value dictionaries. See above description.

If there are multiple entries for a given key, these should be considered separate count events for the same key, and downstream aggregation should add them.
 
A counter datatype is used when you want to send a countable metric into the ecosystem and want downstream pipelines to maintain that counter value, or associated derived values like rates.
```json
"counters": {


    "editor.refresh": {
       "values": [{"timestamp": "2000-01-01T00:00:00Z", "value": 1}], 
       "metadata": {"account_id": "00012345"}
    }
}
```
**NOTE**: Counters may have units or be unitless.

**NOTE**: When developing an SDK, it might make sense to also update counters inside the application prior to sending downstream.

**TODO**: In-app aggregation of counters potentially affects downstream rate calculations. Todo: make note of this somehow.

###Gauge
A gauge is an instantaneous measurement of a value.

field | required | description
----- | -------- | -----------
units | no | units of the measurement, as a string
values | yes | a list of value dictionaries. See above description.
	
If there are multiple entries for a given key, these should be considered separate gauge events for the same key, and downstream aggregation should use the final (most recent) value as the current value of the gauge.

A gauge datatype is used when you want to send a metric into the ecosystem and either don’t want statistical aggregation done on it, or you want to indicate that those calculations have already been done. For instance, a gauge would be used to update the Nth percentiles of a particular metric.

Some example gauges used for in-application aggregation are given in the following sections.

####Percentiles

These percentiles would be calculated by the client application (eg. dropwizard metrics).
```json
  "gauges": {

    "metadata": {
      "path": "/foo/bar",
      "account_id": "00012345"
    },
    "sox.query.time.50percentile": {
       "units": "ms",
       "values": [{"timestamp": "2000-01-01T00:00:00Z", "value": 111}]
    },
    "something.interesting.95percentile": {
       "units": "ms",
       "values": [{"timestamp": "2000-01-01T00:00:00Z", "value": 221}]
    },
    "something.interesting.99percentile": {
       "units": "ms",
       "values": [{"timestamp": "2000-01-01T00:00:00Z", "value": 331}]
    }
}
```


####Rates

These percentiles would be calculated by a client application (eg. dropwizard metrics).
```json
"gauges": {
    "document.validations.perHour": {
       "units": "hour^-1",
       "values": [{"timestamp": "2000-01-01T00:00:00Z", "value": 100}]
    },
    "document.validations.perDay": {
       "units": "day^-1",
       "values": [{"timestamp": "2000-01-01T00:00:00Z", "value": 1211}]
    }  
}
```
**NOTE**: Gauges may have units or be unit-less.

###Stopwatch

The dart:core library (https://api.dartlang.org/136058/dart-core/Stopwatch-class.html ) supports the concept of a stopwatch, used to measure elapsed time. This telemetry protocol supports those using gauges.
Single Message
```json
"gauges": {
    "long.running.method": {
       "values": [{"timestamp": "2000-01-01T00:00:00Z", "value": "START"},
                  {"timestamp": "2000-01-01T00:00:59Z", "value": "STOP"}]
    },
    "metadata": {"guid": "4bd64fcd5512e2b1f800a4c1a34b08c2"}
}
```

**NOTE**: You cannot have more than a single stopwatch event for a single key in a single message

Multiple Messages
```json
"gauges": {
    "long.running.method": {
       "values": [{"timestamp": "2000-01-01T00:00:00Z", "value": "START"}]
    },
    "metadata": {"guid": "4bd64fcd5512e2b1f800a4c1a34b08c2"}
}
```
```json
"gauges": {
    "long.running.method": {
       "values": [{"timestamp": "2000-01-01T00:00:00Z", "value": "STOP"}]
    },
    "metadata": {"guid": "4bd64fcd5512e2b1f800a4c1a34b08c2"}
}
```
**NOTE**: When multiple messages are used, downstream systems should be written to accommodate out-of-order messages.

###Supported Units

The following units are supported values for the “units” field in the specification. Downstream numerical aggregation will make use of these units.

The use of ad-hoc units like “yards”, “mcycles” is supported, but these will be ignored by the downstream aggregation systems.

####Time

Units | Abbreviation
----- | ------------
days | day
hours | hour
minutes | min
seconds (default) | s
milliseconds | ms
microseconds | us

####Rate

Units | Abbreviation
----- | ------------
per day | day^-1
per hour | hour^-1
per minute | min^-1
per second (default) | s^-1
per millisecond | ms^-1
per microsecond | us^-1

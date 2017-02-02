# LambdaTestCapture

[![Maven Central](https://img.shields.io/maven-central/v/com.fortysevendeg/lambda-test-capture_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.fortysevendeg/lambda-test-capture_2.12)

LambdaTestCapture is an extension to 
[LambdaTest](https://github.com/47deg/LambdaTest). 
LambdaTest is a functional testing library for Scala.
LambdaTestCapture extends LambdaTest with support for 
capturing test output as an in-memory data structure or as Json to a file
or log.

One of the goals of LambdaTest was to provide a small clean system that can be easily 
extended to suport new features and to customize it for specific projects. 
LambdaTest includes the ability to define reporters, classes that receive test results
and output them. 
LambdaTestCapture is a good example of this extensibility where without any changes 
to the base system, it adds test result capture by defining the new `capture` function
and a new capture reporter.

You should review the features of the base LambdaTest system before 
reading the documentation below.

## Quick Start

Include LambdaTestCapture jar

    "com.fortysevendeg" % "lambda-test-capture_2.12" % "1.3.0" % "test"
    
In your tests include


    import com.fortysevendeg.lambdatest._
    import CaptureLambdaReporter.capture

    
## Example

See the [Example](https://github.com/47deg/LambdaTestAsync/blob/master/src/test/scala/demo/Example.scala) 
demo. Here we run the test using `capture` rather than `run`. The result of `capture` is
a data structure consisting of maps and lists that contains the test results.

This data structure can be converted to Json using the `Pretty` or `Compact` fucntions defined in
[https://github.com/nestorpersist/json](https://github.com/nestorpersist/json).

Test results can also be logged using the Persist Scala logger
[https://github.com/nestorpersist/logging](https://github.com/nestorpersist/logging).

## Example Json

Here is the Json output from the Example demo. Note that for the in-memory data structure,
Json objects are represented as`Map[String,Any]` and Json arrays are represented as
`List[Any]`.

```
{"name":"example",
 "result":{"failed":1,"tests":2},
 "sub":
   [{"cmd":"label",
     "msg":"Initial Tests",
     "sub":
       [{"cmd":"test",
         "msg":"Eq test",
         "ok":true,
         "sub":
           [{"cmd":"assert",
             "data":"3",
             "msg":"Int eq test",
             "ok":true,
             "pos":{"file":"Example.scala","line":15}
            }
           ]
        }
       ]
    },
    {"cmd":"label",
     "msg":"Simple Tests",
     "sub":
       [{"cmd":"test",
         "msg":"Assert Test",
         "ok":false,
         "sub":
           [{"cmd":"assert",
             "data":"1 != 2",
             "msg":"Bad Int eq test",
             "ok":false,
             "pos":{"file":"Example.scala","line":20}
            },
            {"cmd":"assert",
             "msg":"should work",
             "ok":true,
             "pos":{"file":"Example.scala","line":21}
            }
           ]
        }
       ]
    }
   ]
}
```



##Fabric - A real-time stream processing framework <br>

###What?
A scalable, practical and safe real-time computation framework designed for easy operability and extension.<br>

Fabric is proven to work very well for:<br>
- High velocity multi-destination event ingestion with guaranteed persistence.<br>
- Rules/Filter based real-time triggers for advertising/broadcast<br>
- Online Fraud detection<br>
- Real-time pattern matching<br>
- Basic Streaming analytics<br>

###Why?
* Highly scalable and guaranteed availability using battle-tested clustering capabilities provided by Apache Mesos and Marathon
* Framework level guarantees against message loss, support for replay, multiple sources and complex tuple trees.
* Event batching is supported at the core level.
* Source level event partitioning used as unit for scalability.
* Uses capabilities provided by docker to ensure strong application and partition level isolation guarantees
* Supports multiple topology types
	* Handcrafted topologies (ala Apache Storm) packaged as docker containers
	* Configuration driven, dynamically generated topologies that are created on the fly
* Complete separation of specification from the code, so that minimal code changes are required to update running topologies and deploy new ones.
* On the fly topology creation and deployment by dynamically assembling topologies using components directly from artifactory.
* Inbuilt support for custom metrics and custom code level healthchecks to catch application failures right when they happen.
* Framework level time based aggregation support with completely independent processor level clock pulse generators.
* Single JVM processing per partition effectively eliminating message transfers over the wire, thereby providing very high throughputs upto 150k events per second per partition.
* Easy to write components(processors and sources) without getting bogged down by the complexities of stream processing. The compute framework abstracts out the threading model and provides a simplified single-threaded code model (ala NodeJS), thereby eliminating the need to implement complex threading models.
* Provides capabilities to create processors that can be easily configured from outside.
* Inherent support for Jackson based json serialization and deserialization thus allowing events to have nested structure and be processed easily using common libraries like json-path.

## Getting Started
### Glossary
**Computation**: A computation is a directed acyclic graph which describes the flow of data and the computations done on them. In this graph, the nodes are the components written by users. Nodes can be of two types, *SOURCE* and *PROCESSOR*. A single instance of a topology runs in a single JVM. A computation consists of sources and processors written by users linked together according to a json specification. All sources written by users must extend *PipelineSource*. All processors written by users must extend *StreamingProcessor* or *ScheduledProcessor*.<br>
**EventSet**: An event set is a collection of events. An event set is the basic transmission unit within the computation.<br>
**Source**: A source is a component that ingests event sets into the computation. A source is responsible for managing the state of the events ingested into the computation. Events can be in acknowledged or unacknowledged state.<br>
**Processor**: A processor is a component that performs some computation on an incoming event set and emits an outgoing event set. A processor can be of two types, *Streaming Processor* and *Scheduled Processor*.<br>
**Streaming Processor**: A Streaming Processor is a processor that is triggered whenever and event set is sent to the processor.<br>
**Scheduled Processor**: A Scheduled Processor is a processor which is triggered whenever a fixed period of time elapses in a periodic fashion.<br>

###Walkthrough<br>
Let’s write a word count computation that processes a list of sentences and outputs words frequency counts.<br>
We need three components for this computation:<br>
1. A source that generates random sentences - RandomSentenceSource<br>
2. A processor that splits the sentences by space into its constituent words - SplitterProcessor<br>
3. A processor that outputs the word frequency counts at regular intervals - WordCountProcessor<br>

####RandomSentenceSource.java
```
// Add this annotation for registering the source with the metadata server
@Source(
        namespace = "global",
        name = "random-sentence-source",
        version = "0.1",
        description = "A source that generates random sentences from a pool of sentences",
        cpu = 0.1,
        memory = 64,
        requiredProperties = {},
        optionalProperties = {"randomGeneratorSeed"}
)
public class RandomSentenceSource implements PipelineSource {

    Random random;
    String[] sentences = {
            "A quick brown fox jumped over the lazy dog",
            "Life is what happens to you when you are busy making other plans",
            "Mama always said that life is a box of chocolates",
            "I am going to make you an offer you cannot refuse",
            "I am speaking to a dead man on the other side of the phone",
            "The path of the righteous man is beset on all sides by the inequities of the selfish and the tyranny of evil men"
    };

    @Override
    public void initialize(final String instanceName,
            final Properties global,
            final Properties local,
            final ProcessingContext processingContext,
            final ComponentMetadata componentMetadata) throws Exception {
        // this method is called to initialize the source
        // use this utility method to read properties passed
        int seed = ComponentPropertyReader
                .readInteger(local, global, "randomGeneratorSeed", instanceName, componentMetadata, 42);
        random = new Random(seed);
    }

    @Override
    public RawEventBundle getNewEvents() {
        // this method is called to get new events
        return RawEventBundle.builder()
                .events(getSentences(5).stream()
                        .map(sentence -> Event.builder().id(random.nextInt()).data(sentence.toLowerCase()).build())
                        .collect(Collectors.toCollection(ArrayList::new)))
                .meta(Collections.emptyMap())
                .partitionId(Integer.MAX_VALUE)
                .transactionId(Integer.MAX_VALUE)
                .build();
    }

    private List<String> getSentences(int n) {
        List<String> listOfSentences = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            listOfSentences.add(sentences[random.nextInt(sentences.length)]);
        }
        return listOfSentences;
    }
}
```

####SplitterProcessor.java
```
@Processor(
        namespace = "global",
        name = "splitter-processor",
        version = "0.1",
        cpu = 0.1,
        memory = 32,
        description = "A processor that splits sentences by a given delimiter",
        processorType = ProcessorType.EVENT_DRIVEN,
        requiredProperties = {},
        optionalProperties = {"delimiter"}
)
public class SplitterProcessor extends StreamingProcessor {
    String delimiter;

    @Override
    protected EventSet consume(final ProcessingContext processingContext, final EventSet eventSet) throws
            ProcessingException {
        List<Event> events = new ArrayList<>();
        eventSet.getEvents().stream()
                .forEach(event -> {
                    String sentence = (String) event.getData();
                    String[] words = sentence.split(delimiter);
                    events.add(Event.builder()
                            .data(words)
                            .id(Integer.MAX_VALUE)
                            .properties(Collections.emptyMap())
                            .build());
                });
        return EventSet.eventFromEventBuilder()
                .isAggregate(false)
                .partitionId(eventSet.getPartitionId())
                .events(events)
                .build();
    }

    @Override
    public void initialize(final String instanceName,
            final Properties global,
            final Properties local,
            final ComponentMetadata componentMetadata) throws InitializationException {
        delimiter = ComponentPropertyReader.readString(local, global, "delimiter", instanceName, componentMetadata, ",");

    }

    @Override
    public void destroy() {
        // do some cleanup if necessary
    }
}
```

####WordCountProcessor.java
```
@Processor(
        namespace = "global",
        name = "word-count-processor",
        version = "0.2",
        description = "A processor that prints word frequency counts within a tumbling window",
        cpu = 0.1,
        memory = 128,
        processorType = ProcessorType.TIMER_DRIVEN,
        requiredProperties = {"triggering_frequency"},
        optionalProperties = {}
)
public class WordCountProcessor extends ScheduledProcessor {
    Map<String, Integer> wordCounts = new HashMap<>();

    @Override
    protected void consume(final ProcessingContext processingContext, final EventSet eventSet) throws
            ProcessingException {
        eventSet.getEvents().stream()
                .forEach(event -> {
                    String[] words = (String[]) event.getData();
                    for (String word: words) {
                        if (wordCounts.containsKey(word)) {
                            wordCounts.put(word, wordCounts.get(word) + 1);
                        } else {
                            wordCounts.put(word, 1);
                        }
                    }
                });
    }

    @Override
    public void initialize(final String instanceName,
            final Properties global,
            final Properties local,
            final ComponentMetadata componentMetadata) throws InitializationException {
        // nothing to initialize here
    }

    @Override
    public List<Event> timeTriggerHandler(ProcessingContext processingContext) throws ProcessingException {
        // this method will be called after a fixed interval of time, say 5 seconds
        System.out.println(Joiner.on(",").withKeyValueSeparator("=").join(wordCounts));
        wordCounts.clear();
        // nothing to send to downstream processors
        return Collections.emptyList();

    }

    @Override
    public void destroy() {
        wordCounts.clear();
    }
}
```

`RandomSentenceSource -> SplitterProcessor -> WordCountProcessor`

The json specification for this computation will look like this (assuming you have compiled/packaged everything before-hand)<br>

```
{
    "name": "word-count-topology",
    "sources": [{
        "id": "random-sentence-source",
        "meta": {
            "id": "1cfda5cb-99d4-34e3-83f0-5b1364a92cce",
            "type": "SOURCE",
            "namespace": "global",
            "name": "random-sentence-source",
            "version": "0.1",
            "description": "A source that generates random sentences from a pool of sentences",
            "processorType": null,
            "requiredProperties": [],
            "optionalProperties": [
                "randomGeneratorSeed"
            ],
            "cpu": 0.1,
            "memory": 64,
            "source": {
                "type": "jar",
                "url": "file:///path/to/fabric/fabric-examples/target/fabric-examples-1.0.0-SNAPSHOT.jar"
            }
        },
        "properties": {}
    }],
    "processors": [{
        "id": "splitter-processor",
        "meta": {
            "id": "0b749006-d2dd-3684-a521-f76d6ab0dec8",
            "type": "PROCESSOR",
            "namespace": "global",
            "name": "splitter-processor",
            "version": "0.1",
            "description": "A processor that splits sentences by a given delimiter",
            "processorType": "EVENT_DRIVEN",
            "requiredProperties": [],
            "optionalProperties": [
                "delimiter"
            ],
            "cpu": 0.1,
            "memory": 32,
            "source": {
                "type": "jar",
                "url": "file:///path/to/fabric/fabric-examples/target/fabric-examples-1.0.0-SNAPSHOT.jar"
            }
        },
        "properties": {
            "processor.splitter-processor.delimiter": " "
        }
    }, {
        "id": "word-count-processor",
        "meta": {
            "id": "59f4fe28-b09b-3447-8bb2-26d3c23dd885",
            "type": "PROCESSOR",
            "namespace": "global",
            "name": "word-count-processor",
            "version": "0.2",
            "description": "A processor that prints word frequency counts within a tumbling window",
            "processorType": "TIMER_DRIVEN",
            "requiredProperties": [
                "triggering_frequency"
            ],
            "optionalProperties": [],
            "cpu": 0.1,
            "memory": 128,
            "source": {
                "type": "jar",
                "url": "file:///path/to/fabric/fabric-examples/target/fabric-examples-1.0.0-SNAPSHOT.jar"
            }
        },
        "properties": {
            "processor.word-count-processor.triggering_frequency": "5000"
        }
    }],
    "connections": [{
        "fromType": "SOURCE",
        "from": "random-sentence-source",
        "to": "splitter-processor"
    }, {
        "fromType": "PROCESSOR",
        "from": "splitter-processor",
        "to": "word-count-processor"
    }],
    "properties": {
        "computation.name": "word-count-topology",
        "computation.eventset.is_serialized": "false"
    }
}
```
The spec is available at fabric-examples/src/main/resources/spec.json. Please replace _/path/to/json_ with the place where you've checked out Fabric.

The topology can be run using the following command:
```
java -jar fabric-executor/target/fabric-executor-1.0.0-SNAPSHOT.jar -f fabric-examples/src/main/resources/spec.json
```

###Benchmarks
####Performance Test Configuration
No of messages: 1 million<br>
Payload size: 258 bytes<br>
####Topology
Kafka Source -> Event Counter (Prints number of total events consumed every one second to the console)<br>
No of partitions: 1<br>
Topic Name: end-to-end-latency-perf<br>
No of instances of topology: 1<br>
Kafka source buffer size: 3 MB<br>
Docker CPU (number of cpu shares): 1.0<br>
Docker Memory: 2 GB<br>
JVM Heap Size: 2 GB<br>
####Kafka Broker configuration
2 cores Intel(R) Xeon(R) CPU E5-2666 v3 @ 2.90GHz 8 GB RAM<br>
####Mesos Host configuration
8 cores Intel(R) Xeon(R) CPU E5-2666 v3 @ 2.90GHz 32 GB RAM<br>

End to end latency to process all messages in seconds (ceiling) averaged over multiple runs is presented below<br><br>


| Configuration           | Without gzip | With gzip |
| ----------------------- | ------------ | --------- |
| Local Zk, Local Kafka   |     8        |    5      |
| Remote Zk, Remote Kafka |     36       |    6      |


Throughput with best configuration T ~ 166666 events / second<br>

NOTE: Using Disruptor with YieldWaitingStategy instead of LBQ for channel communication actually reduced the throughput<br>

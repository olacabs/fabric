/*
 * Copyright 2016 ANI Technologies Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.*;
import java.util.stream.Collectors;

import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.compute.source.PipelineSource;
import com.olacabs.fabric.compute.util.ComponentPropertyReader;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.RawEventBundle;
import com.olacabs.fabric.model.source.Source;


/**
 * A Sample Source Implementation which generates
 * Random sentences.
 */

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

    private Random random;
    private String[] sentences = {
        "A quick brown fox jumped over the lazy dog",
        "Life is what happens to you when you are busy making other plans",
        "Mama always said that life is a box of chocolates", "I am going to make you an offer you cannot refuse",
        "I am speaking to a dead man on the other side of the phone",
        "The path of the righteous man is beset on all sides by the inequities of the selfish and the tyranny "
                + "of evil men",
    };

    @Override
    public void initialize(final String instanceName, final Properties global, final Properties local,
            final ProcessingContext processingContext, final ComponentMetadata componentMetadata) throws Exception {
        // this method is called to initialize the source
        // use this utility method to read properties passed
        int seed = ComponentPropertyReader.readInteger(local, global, "randomGeneratorSeed", instanceName,
                componentMetadata, 42);
        random = new Random(seed);
    }

    @Override
    public RawEventBundle getNewEvents() {
        // this method is called to get new events
        return RawEventBundle
                .builder().events(
                        getSentences(5).stream()
                                .map(sentence -> Event.builder().id(random.nextInt()).data(sentence.toLowerCase())
                                        .build())
                                .collect(Collectors.toCollection(ArrayList::new)))
                .meta(Collections.emptyMap()).partitionId(Integer.MAX_VALUE).transactionId(Integer.MAX_VALUE).build();
    }

    private List<String> getSentences(int n) {
        List<String> listOfSentences = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            listOfSentences.add(sentences[random.nextInt(sentences.length)]);
        }
        return listOfSentences;
    }
}



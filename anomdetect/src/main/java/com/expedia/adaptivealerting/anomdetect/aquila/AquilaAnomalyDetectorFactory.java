/*
 * Copyright 2018 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.anomdetect.aquila;

import com.expedia.adaptivealerting.anomdetect.AnomalyDetectorFactory;
import com.expedia.adaptivealerting.anomdetect.util.ModelResource;
import com.expedia.adaptivealerting.anomdetect.util.ModelServiceConnector;
import com.expedia.metrics.jackson.MetricsJavaModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

// TODO Move this class to the Aquila repo. [WLW]

/**
 * @author Willie Wheeler
 */
@Slf4j
public class AquilaAnomalyDetectorFactory implements AnomalyDetectorFactory<AquilaAnomalyDetector> {
    private static final String MODEL_UUID_PARAM = "modelUuid";

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new MetricsJavaModule());
    private String uri;
    private ModelServiceConnector modelServiceConnector;
    
    @Override
    public void init(Config config, ModelServiceConnector modelServiceConnector) {
        this.uri = config.getString("uri");
        this.modelServiceConnector = modelServiceConnector;
        log.info("Initialized AquilaFactory: uri={}", uri);
    }
    
    @Override
    public AquilaAnomalyDetector create(UUID uuid) {
        notNull(uuid, "uuid can't be null");

        final ModelResource model = modelServiceConnector.findLatestModel(uuid);
        log.info("Loaded model: {}", model);
        if (model == null
                || model.getParams() == null
                || model.getParams().get(MODEL_UUID_PARAM) == null) {
            throw new RuntimeException("Valid model not found for uuid=" + uuid);
        }

        return new AquilaAnomalyDetector(
                objectMapper,
                uri,
                UUID.fromString(model.getParams().get(MODEL_UUID_PARAM).toString())
        );
    }
}

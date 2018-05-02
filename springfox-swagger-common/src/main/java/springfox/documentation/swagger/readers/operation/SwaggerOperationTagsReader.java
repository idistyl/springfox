/*
 *
 *  Copyright 2015-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package springfox.documentation.swagger.readers.operation;



import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spring.web.readers.operation.DefaultTagsProvider;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Stream;


import static com.google.common.collect.Lists.*;

import static java.util.stream.Collectors.toSet;
import static springfox.documentation.service.Tags.*;

@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class SwaggerOperationTagsReader implements OperationBuilderPlugin {

  private final DefaultTagsProvider tagsProvider;

  @Autowired
  public SwaggerOperationTagsReader(DefaultTagsProvider tagsProvider) {
    this.tagsProvider = tagsProvider;
  }

  @Override
  public void apply(OperationContext context) {
    Set<String> defaultTags = tagsProvider.tags(context);
    Set<String> tags = Stream.concat(operationTags(context).stream(), controllerTags(context).stream()).collect(toSet());
    if (tags.isEmpty()) {
      context.operationBuilder().tags(defaultTags);
    } else {
      context.operationBuilder().tags(tags);
    }
  }

  private Set<String> controllerTags(OperationContext context) {
    Optional<Api> controllerAnnotation = context.findControllerAnnotation(Api.class);
    return controllerAnnotation.map(tagsFromController()).orElse(new HashSet());
  }

  private Set<String> operationTags(OperationContext context) {
    Optional<ApiOperation> annotation = context.findAnnotation(ApiOperation.class);
    return annotation.map(tagsFromOperation()).orElse(new HashSet());
  }

  private Function<ApiOperation, Set<String>> tagsFromOperation() {
    return new Function<ApiOperation, Set<String>>() {
      @Override
      public Set<String> apply(ApiOperation input) {
        Set<String> tags = new TreeSet();
        tags.addAll(newArrayList(input.tags()).stream().filter(emptyTags()).collect(toSet()));
        return tags;
      }
    };
  }

  private Function<Api, Set<String>> tagsFromController() {
    return new Function<Api, Set<String>>() {
      @Override
      public Set<String> apply(Api input) {
        Set<String> tags = new TreeSet();
        tags.addAll(newArrayList(input.tags()).stream().filter(emptyTags()).collect(toSet()));
        return tags;
      }
    };
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return true;
  }
}

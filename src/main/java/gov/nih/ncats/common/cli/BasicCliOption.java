/*******************************************************************************
 * NCATS-COMMON-CLI
 *
 * Copyright 2019 NIH/NCATS
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/

package gov.nih.ncats.common.cli;


import gov.nih.ncats.common.functions.ThrowableConsumer;
import gov.nih.ncats.common.functions.ThrowableFunction;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

/**
 * Created by katzelda on 5/28/19.
 */
class BasicCliOption implements InternalCliOptionBuilder, BasicCliOptionBuilder {

    private String name, description;
    private String longName;

    private boolean isRequired;

    private String argName;

    private ThrowableConsumer<String, CliValidationException> consumer = (s) ->{}; //no -op

    private boolean isFlag;

    BasicCliOption(String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public BasicCliOptionBuilder argName(String argName){
        this.argName = argName;
        return this;
    }
    @Override
    public BasicCliOptionBuilder longName(String longName){
        this.longName = longName;
        return this;
    }
    @Override
    public BasicCliOptionBuilder description(String description){
        this.description = Objects.requireNonNull(description);
        return this;
    }
    @Override
    public BasicCliOption isFlag(boolean isFlag){
        this.isFlag = isFlag;
        return this;
    }
    public BasicCliOptionBuilder required(boolean isRequired){
        this.isRequired = isRequired;
        return this;
    }

    @Override
    public BasicCliOption setRequired(boolean isRequired) {
        this.isRequired = isRequired;
        return this;
    }

    @Override
    public BasicCliOption setter(Consumer<String> consumer){
        return setter(ThrowableFunction.identity(), ThrowableConsumer.wrap(consumer),null);
    }

    @Override
    public <T extends Throwable, R> BasicCliOption setter(ThrowableFunction<String, R, T> typeConverter,
                                                          ThrowableConsumer<R, T> consumer, Predicate<R> validator){
        if(validator ==null){
            this.consumer=s-> {
                try {
                    consumer.accept(typeConverter.apply(s));
                } catch (Throwable t) {
                    throw new CliValidationException(t.getMessage(), t);
                }
            };
        }else{
            this.consumer = s->{
                R value;
                try {
                    value = typeConverter.apply(s);
                } catch (Throwable t) {
                   throw new CliValidationException(t);
                }
                if(validator.test(value)){
                    try {
                        consumer.accept(value);
                    }catch(Throwable t){
                        throw new CliValidationException(t.getMessage(), t);
                    }
                }else{
                    throw new CliValidationException("setter did not pass validation test");
                }
            };
        }
        return this;
    }
    @Override
    public <T extends Throwable> BasicCliOptionBuilder setter(ThrowableConsumer<String, T> consumer, Predicate<String> validator){
        return setter(ThrowableFunction.identity(), consumer, validator);
    }

    @Override
    public BasicCliOption setToFile(Consumer<File> consumer){
        Objects.requireNonNull(consumer);
        this.consumer = s -> consumer.accept(new File(s));
        return this;
    }
    @Override
    public BasicCliOption setToInt(IntConsumer consumer){
        Objects.requireNonNull(consumer);
        this.consumer = s -> consumer.accept(Integer.parseInt(s));
        return this;
    }

    public String getName() {
        return name;
    }

    public ThrowableConsumer<String, CliValidationException> getConsumer() {
        return consumer;
    }


    org.apache.commons.cli.Option asApacheOption(){
        return org.apache.commons.cli.Option.builder(name)
                .required(isRequired)
                .longOpt(longName)
                .desc(description)
                .hasArg(!isFlag)
                .argName(argName)
                .build();
    }

    @Override
    public InternalCliOption build() {
        return new InternalBasicCliOption(asApacheOption(), consumer, this.isRequired);
    }

    @Override
    public InternalCliOption build(boolean isRequired) {
        org.apache.commons.cli.Option option = asApacheOption();
        option.setRequired(isRequired);
        return new InternalBasicCliOption(option, consumer, this.isRequired);
    }


    private static final class InternalBasicCliOption implements InternalCliOption{

        private final org.apache.commons.cli.Option option;

        private final ThrowableConsumer<String, CliValidationException> consumer;

        private final boolean isRequired;

        private InternalBasicCliOption(org.apache.commons.cli.Option option, ThrowableConsumer<String, CliValidationException> consumer, boolean isRequired){
            this.option = option;
            this.consumer = consumer;
            this.isRequired = isRequired;
        }

        @Override
        public Optional<String> generateUsage(boolean force) {
            if(!force && !isRequired()){
                return Optional.empty();
            }
            return Optional.of("-" + option.getOpt() + (option.getArgName()==null ? "" : " <"+option.getArgName()+">"));
        }

        @Override
        public boolean isRequired() {
            return isRequired;
        }

        @Override
        public void addTo(InternalCliSpecification spec, Boolean forceIsRequired) {
            if(forceIsRequired !=null){
                option.setRequired(forceIsRequired);
            }
            spec.getInternalOptions().addOption(option);
        }

        @Override
        public Optional<String> getMissing(Cli cli) {
            if(isPresent(cli)){
                return Optional.empty();
            }
            return Optional.ofNullable("-" +option.getOpt());
        }

        @Override
        public boolean isPresent(Cli cli) {
            return cli.hasOption(option.getOpt());
        }

        @Override
        public void validate(Cli cli) throws CliValidationException {
            if(option.isRequired() && !isPresent(cli)){
                throw new CliValidationException(option.getOpt() + " is required");
            }
        }

        @Override
        public void fireConsumerIfNeeded(Cli cli) throws CliValidationException {
            if(isPresent(cli)){
                consumer.accept(cli.getOptionValue(option.getOpt()));
            }
        }

        @Override
        public List<String> getSeenList(Cli cli) {
            if(isPresent(cli)){
                return Collections.singletonList(option.getOpt());
            }
            return Collections.emptyList();
        }
    }
}

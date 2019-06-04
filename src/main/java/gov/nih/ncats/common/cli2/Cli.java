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

package gov.nih.ncats.common.cli2;

/**
 * Created by katzelda on 5/28/19.
 */
public class Cli {

    private final org.apache.commons.cli.CommandLine delegate;

    Cli(org.apache.commons.cli.CommandLine cmd){
        this.delegate = cmd;
    }


    public boolean hasOption(String argName) {
        return delegate.hasOption(argName);
    }

    public String getOptionValue(String argName){
        return delegate.getOptionValue(argName);
    }
}

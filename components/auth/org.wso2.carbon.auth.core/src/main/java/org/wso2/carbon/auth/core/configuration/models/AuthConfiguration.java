/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.auth.core.configuration.models;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

/**
 * Class to hold Auth configuration parameters and generate yaml file
 */

@Configuration(namespace = "wso2.carbon.auth", description = "Auth Configuration Parameters")
public class AuthConfiguration {
    
    @Element(description = "User Store Configurations")
    private UserStoreConfiguration userStoreConfiguration = new UserStoreConfiguration();

    @Element(description = "Key Manager Configurations")
    private KeyManagerConfiguration keyManagerConfigs = new KeyManagerConfiguration();

    public UserStoreConfiguration getUserStoreConfiguration() {
        return userStoreConfiguration;
    }

    public KeyManagerConfiguration getKeyManagerConfigs() {
        return keyManagerConfigs;
    }

    public void setUserStoreConfiguration(UserStoreConfiguration userStoreConfiguration) {
        this.userStoreConfiguration = userStoreConfiguration;
    }

}

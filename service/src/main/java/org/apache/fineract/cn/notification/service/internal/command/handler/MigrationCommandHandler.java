/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cn.notification.service.internal.command.handler;

import org.apache.fineract.cn.command.annotation.Aggregate;
import org.apache.fineract.cn.command.annotation.CommandHandler;
import org.apache.fineract.cn.command.annotation.CommandLogLevel;
import org.apache.fineract.cn.command.annotation.EventEmitter;
import org.apache.fineract.cn.lang.ApplicationName;
import org.apache.fineract.cn.mariadb.domain.FlywayFactoryBean;
import org.apache.fineract.cn.notification.api.v1.events.NotificationEventConstants;
import org.apache.fineract.cn.notification.service.ServiceConstants;
import org.apache.fineract.cn.notification.service.internal.command.InitializeServiceCommand;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@SuppressWarnings({
		"unused"
})
@Aggregate
public class MigrationCommandHandler {
	
	private final Logger logger;
	private final DataSource dataSource;
	private final FlywayFactoryBean flywayFactoryBean;
	private final ApplicationName applicationName;
	
	@Autowired
	public MigrationCommandHandler(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
	                               final DataSource dataSource,
	                               final FlywayFactoryBean flywayFactoryBean,
	                               final ApplicationName applicationName) {
		super();
		this.logger = logger;
		this.dataSource = dataSource;
		this.flywayFactoryBean = flywayFactoryBean;
		this.applicationName = applicationName;
	}
	
	@CommandHandler(logStart = CommandLogLevel.INFO, logFinish = CommandLogLevel.INFO)
	@Transactional
	@EventEmitter(selectorName = NotificationEventConstants.SELECTOR_NAME, selectorValue = NotificationEventConstants.INITIALIZE)
	public String initialize(final InitializeServiceCommand initializeServiceCommand) {
		this.logger.debug("Start service migration.");
		this.flywayFactoryBean.create(this.dataSource).migrate();
		return this.applicationName.getVersionString();
	}
}

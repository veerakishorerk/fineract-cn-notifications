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
package org.apache.fineract.cn.notification.service.rest;

import org.apache.fineract.cn.anubis.annotation.AcceptedTokenType;
import org.apache.fineract.cn.anubis.annotation.Permittable;
import org.apache.fineract.cn.command.gateway.CommandGateway;
import org.apache.fineract.cn.lang.ServiceException;
import org.apache.fineract.cn.notification.api.v1.PermittableGroupIds;
import org.apache.fineract.cn.notification.api.v1.domain.SMSConfiguration;
import org.apache.fineract.cn.notification.api.v1.domain.SMSConfiguration;
import org.apache.fineract.cn.notification.service.ServiceConstants;
import org.apache.fineract.cn.notification.service.internal.command.*;
import org.apache.fineract.cn.notification.service.internal.service.NotificationService;
import org.apache.fineract.cn.notification.service.internal.service.SMSService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/configuration/sms/")
public class SMSServiceRestController {
	
	private final Logger logger;
	private final CommandGateway commandGateway;
	private final SMSService smsService;
	
	@Autowired
	public SMSServiceRestController(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
	                                final CommandGateway commandGateway,
	                                final SMSService smsService) {
		super();
		this.logger = logger;
		this.commandGateway = commandGateway;
		this.smsService = smsService;
	}
	
	@Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.SELF_MANAGEMENT)
	@RequestMapping(
			value = "/active",
			method = RequestMethod.GET,
			consumes = MediaType.ALL_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public
	@ResponseBody
	List<SMSConfiguration> findAllActiveSMSConfigurationEntities() {
		return this.smsService.findAllActiveSMSConfigurationEntities();
	}
	
	@Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.SELF_MANAGEMENT)
	@RequestMapping(
			value = "/{identifier}",
			method = RequestMethod.GET,
			consumes = MediaType.ALL_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public
	@ResponseBody
	ResponseEntity<SMSConfiguration> findSMSConfigurationByIdentifier(@PathVariable("identifier") final String identifier) {
		return this.smsService.findSMSConfigurationByIdentifier(identifier)
				.map(ResponseEntity::ok)
				.orElseThrow(() -> ServiceException.notFound("SMS Gateway Configuration with identifier " + identifier + " doesn't exist."));
	}
	
	@Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.SELF_MANAGEMENT)
	@RequestMapping(
			value = "/create",
			method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public
	@ResponseBody
	ResponseEntity<Void> createSMSConfiguration(@RequestBody @Valid final SMSConfiguration smsConfiguration) throws InterruptedException {
		if (this.smsService.smsConfigurationExists(smsConfiguration.getIdentifier())) {
			throw ServiceException.conflict("Configuration {0} already exists.", smsConfiguration.getIdentifier());
		}
		
		this.commandGateway.process(new CreateSMSConfigurationCommand(smsConfiguration));
		return new ResponseEntity<>(HttpStatus.CREATED);
	}
	
	@Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.SELF_MANAGEMENT)
	@RequestMapping(value = "/update",
			method = RequestMethod.PUT,
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public
	@ResponseBody
	ResponseEntity<Void> updateSMSConfiguration(@RequestBody @Valid final SMSConfiguration smsConfiguration) {
		this.commandGateway.process(new UpdateSMSConfigurationCommand(smsConfiguration));
		return ResponseEntity.accepted().build();
	}
	
	@Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.SELF_MANAGEMENT)
	@RequestMapping(value = "/delete/{identifier}",
			method = RequestMethod.DELETE,
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public
	@ResponseBody
	ResponseEntity<Void> deleteSMSConfiguration(@PathVariable @Valid final String identifier) {
		this.commandGateway.process(new DeleteSMSConfigurationCommand(identifier));
		return ResponseEntity.ok().build();
	}
}

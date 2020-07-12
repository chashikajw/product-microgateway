/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package filter_manager

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
)

type ApiData struct {
	apiname string  `json:"apiname"`
	apiversion string `json:"apiversion"`
}

type response struct {
	Message string `json:"message"`
	ApiData ApiData
}

type filterChainRequest struct {
	uuid string `json:"uuid"`
	label string  `json:"label"`
	ip string `json:"ip"`
	port string `json:"port"`
}

type RESTService struct{}

func (rest *RESTService) FilterChainRegisterPOST(w http.ResponseWriter, r *http.Request) {

	requstData := filterChainRequest{
		uuid: r.FormValue("uuid"),
		label: r.FormValue("label"),
		ip:   r.FormValue("ip"),
		port: r.FormValue("port"),
	}

	w.Header().Set("Content-Type", "application/json")
	var managerRespose response

	if requstData.label != "" && requstData.ip != ""{
		w.WriteHeader(http.StatusOK)
		managerRespose.Message = requstData.label + " FIlter chain is registered"
		managerRespose.ApiData = ApiData{
			apiname:    "petstore",
			apiversion: "v3",
		}

	} else {
		w.WriteHeader(http.StatusNotAcceptable)
		managerRespose.Message = "No suffient data to register the filter chain"

	}

	err := json.NewEncoder(w).Encode(managerRespose)

	if err != nil {
		log.Println("error of encoding filter manager response: ", err)
	}

	fmt.Println(requstData)
}

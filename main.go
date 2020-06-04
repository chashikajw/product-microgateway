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
package main

import (
	"fmt"
	"github.com/sirupsen/logrus"
	"github.com/wso2/micro-gw/cmd/microgateway"
	_ "github.com/wso2/micro-gw/internal/pkg/logging"
	"os"
)



func main() {

	var file string
	if len(os.Args) > 1 {
		file = os.Args[1]
		fmt.Println(file)
	}

	logrus.Info("Info level")
	logrus.Warn("warn level")
	logrus.Info("Info level")
	logrus.Debug("Debug level level")
	logrus.Error("Error level")

	microgateway.StartMicroGateway(os.Args)
	logrus.Fatal("Fatal level exit 1")
	//logrus.Panic("panic level")

}

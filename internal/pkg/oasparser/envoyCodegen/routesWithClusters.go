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
package envoyCodegen

import (
	cluster "github.com/envoyproxy/go-control-plane/envoy/config/cluster/v3"
	endpoint "github.com/envoyproxy/go-control-plane/envoy/config/endpoint/v3"
	route "github.com/envoyproxy/go-control-plane/envoy/config/route/v3"
	core "github.com/envoyproxy/go-control-plane/envoy/config/core/v3"
	envoy_type_matcher "github.com/envoyproxy/go-control-plane/envoy/type/matcher/v3"

	swag_operator "github.com/wso2/micro-gw/internal/pkg/oasparser/swaggerOperator"
	logger "github.com/wso2/micro-gw/internal/loggers"
	"github.com/wso2/micro-gw/internal/pkg/oasparser/models/apiDefinition"
	"github.com/wso2/micro-gw/configs"

	"github.com/golang/protobuf/ptypes"
	"strings"
	"time"
)

/**
 * Create envoy routes along with clusters and endpoint instances.
 * This create routes for all the swagger resources and link to clusters.
 * Create clusters for api level production and sandbox endpoints.
 * If a resource has resource level endpoint, it create another cluster and
 * link it. If resources doesn't has resource level endpoints, those clusters are linked
 * to the api level clusters.
 *
 * @param mgwSwagger  mgwSwagger instance
 * @return []*v2route.Route  Production routes
 * @return []*v2.Cluster  Production clusters
 * @return []*core.Address  Production endpoints
 * @return []*v2route.Route  Sandbox routes
 * @return []*v2.Cluster  Sandbox clusters
 * @return []*core.Address  Sandbox endpoints
 */
func CreateRoutesWithClusters(mgwSwagger apiDefinition.MgwSwagger) ([]*route.Route, []*cluster.Cluster, []*core.Address, []*route.Route, []*cluster.Cluster, []*core.Address) {
	var (
		routesProd           []*route.Route
		clustersProd         []*cluster.Cluster
		endpointProd         []apiDefinition.Endpoint
		apiLevelEndpointProd []apiDefinition.Endpoint
		apilevelClusterProd  cluster.Cluster
		cluster_refProd      string
		endpointsProd        []*core.Address

		routesSand           []*route.Route
		clustersSand         []*cluster.Cluster
		endpointSand         []apiDefinition.Endpoint
		apiLevelEndpointSand []apiDefinition.Endpoint
		apilevelClusterSand  cluster.Cluster
		cluster_refSand      string
		endpointsSand        []*core.Address
	)
	//check API level sandbox endpoints availble
	if swag_operator.IsEndpointsAvailable(mgwSwagger.GetSandEndpoints()) {
		apiLevelEndpointSand = mgwSwagger.GetSandEndpoints()
		apilevelAddressSand := createAddress(apiLevelEndpointSand[0].GetHost(), apiLevelEndpointSand[0].GetPort())
		apiLevelClusterNameS := strings.TrimSpace("clusterSand_" + strings.Replace(mgwSwagger.GetTitle(), " ", "", -1) + mgwSwagger.GetVersion())
		apilevelClusterSand = createCluster(apilevelAddressSand, apiLevelClusterNameS)
		clustersSand = append(clustersSand, &apilevelClusterSand)
		endpointsSand = append(endpointsSand, &apilevelAddressSand)
	}

	//check API level production endpoints available
	if swag_operator.IsEndpointsAvailable(mgwSwagger.GetProdEndpoints()) {
		apiLevelEndpointProd = mgwSwagger.GetProdEndpoints()
		apilevelAddressP := createAddress(apiLevelEndpointProd[0].GetHost(), apiLevelEndpointProd[0].GetPort())
		apiLevelClusterNameP := strings.TrimSpace("clusterProd_" + strings.Replace(mgwSwagger.GetTitle(), " ", "", -1) + mgwSwagger.GetVersion())
		apilevelClusterProd = createCluster(apilevelAddressP, apiLevelClusterNameP)
		clustersProd = append(clustersProd, &apilevelClusterProd)
		endpointsProd = append(endpointsProd, &apilevelAddressP)

	} else {
		logger .LoggerOasparser.Warn("API level Producton endpoints are not defined")
	}
	for ind, resource := range mgwSwagger.GetResources() {

		//resource level check sandbox endpoints
		if swag_operator.IsEndpointsAvailable(resource.GetSandEndpoints()) {
			endpointSand = resource.GetSandEndpoints()
			addressSand := createAddress(endpointSand[0].GetHost(), endpointSand[0].GetPort())
			clusterNameSand := strings.TrimSpace("clusterSand_" + strings.Replace(resource.GetId(), " ", "", -1) + string(ind))
			clusterSand := createCluster(addressSand, clusterNameSand)
			clustersSand = append(clustersSand, &clusterSand)
			cluster_refSand = clusterSand.GetName()

			//sandbox endpoints
			routeS := createRoute(mgwSwagger.GetXWso2Basepath(), endpointSand[0], resource.GetPath(), cluster_refSand)
			routesSand = append(routesSand, &routeS)
			endpointsSand = append(endpointsSand, &addressSand)

			//API level check
		} else if swag_operator.IsEndpointsAvailable(mgwSwagger.GetSandEndpoints()) {
			endpointSand = apiLevelEndpointSand
			cluster_refSand = apilevelClusterSand.GetName()

			//sandbox endpoints
			routeS := createRoute(mgwSwagger.GetXWso2Basepath(), endpointSand[0], resource.GetPath(), cluster_refSand)
			routesSand = append(routesSand, &routeS)

		}

		//resource level check production endpoints
		if swag_operator.IsEndpointsAvailable(resource.GetProdEndpoints()) {
			endpointProd = resource.GetProdEndpoints()
			addressProd := createAddress(endpointProd[0].GetHost(), endpointProd[0].GetPort())
			clusterNameProd := strings.TrimSpace("clusterProd_" + strings.Replace(resource.GetId(), " ", "", -1) + string(ind))
			clusterProd := createCluster(addressProd, clusterNameProd)
			clustersProd = append(clustersProd, &clusterProd)
			cluster_refProd = clusterProd.GetName()

			//production endpoints
			routeP := createRoute(mgwSwagger.GetXWso2Basepath(), endpointProd[0], resource.GetPath(), cluster_refProd)
			routesProd = append(routesProd, &routeP)
			endpointsProd = append(endpointsProd, &addressProd)

			//API level check
		} else if swag_operator.IsEndpointsAvailable(mgwSwagger.GetProdEndpoints()) {
			endpointProd = apiLevelEndpointProd
			cluster_refProd = apilevelClusterProd.GetName()

			//production endpoints
			routeP := createRoute(mgwSwagger.GetXWso2Basepath(), endpointProd[0], resource.GetPath(), cluster_refProd)
			routesProd = append(routesProd, &routeP)

		} else {
			logger.LoggerOasparser.Fatalf("Producton endpoints are not defined")
		}
	}

	return routesProd, clustersProd, endpointsProd, routesSand, clustersSand, endpointsSand
}

/**
 * Create a cluster.
 *
 * @param address   Address which has host and port
 * @return v2.Cluster  Cluster instance
 */
func createCluster(address core.Address, clusterName string) cluster.Cluster {
	logger.LoggerOasparser.Debug("creating a cluster....")
	conf, errReadConfig := configs.ReadConfigs()
	if errReadConfig != nil {
		logger.LoggerOasparser.Fatal("Error loading configuration. ", errReadConfig)
	}

	h := &address
	cluster := cluster.Cluster{
		Name:                 clusterName,
		ConnectTimeout:       ptypes.DurationProto(conf.Envoy.ClusterTimeoutInSeconds* time.Second),
		ClusterDiscoveryType: &cluster.Cluster_Type{Type: cluster.Cluster_STRICT_DNS},
		DnsLookupFamily:      cluster.Cluster_V4_ONLY,
		LbPolicy:             cluster.Cluster_ROUND_ROBIN,
		LoadAssignment: &endpoint.ClusterLoadAssignment{
			ClusterName: clusterName,
			Endpoints: []*endpoint.LocalityLbEndpoints{
				{
					LbEndpoints: []*endpoint.LbEndpoint{
						{
							HostIdentifier: &endpoint.LbEndpoint_Endpoint{
								Endpoint: &endpoint.Endpoint{
									Address: h,
								},
							},
						},
					},
				},
			},
		},
	}
	//fmt.Println(h.GetAddress())
	return cluster
}

/**
 * Create a route.
 *
 * @param xWso2Basepath   Xwso2 basepath
 * @param endpoint  Endpoint
 * @param resourcePath  Resource path
 * @param clusterName  Name of the cluster
 * @return v2route.Route  Route instance
 */
func createRoute(xWso2Basepath string,endpoint apiDefinition.Endpoint, resourcePath string, clusterName string) route.Route {
	logger.LoggerOasparser.Debug("creating a route....")
	var (
		router route.Route
		action *route.Route_Route
		match *route.RouteMatch
	)
	routePath := GenerateRoutePaths(xWso2Basepath,endpoint.GetBasepath(), resourcePath)

	match = &route.RouteMatch{
		PathSpecifier: &route.RouteMatch_SafeRegex{
			SafeRegex: &envoy_type_matcher.RegexMatcher{
				EngineType: &envoy_type_matcher.RegexMatcher_GoogleRe2{
					GoogleRe2: &envoy_type_matcher.RegexMatcher_GoogleRE2{
						MaxProgramSize: nil,
					},
				},
				Regex: routePath,
			},
		},
	}


	if xWso2Basepath != "" {
		action = &route.Route_Route{
			Route: &route.RouteAction{
				HostRewriteSpecifier: &route.RouteAction_HostRewriteLiteral{
					HostRewriteLiteral: endpoint.GetHost(),
				},
				RegexRewrite: &envoy_type_matcher.RegexMatchAndSubstitute{
					Pattern: &envoy_type_matcher.RegexMatcher{
						EngineType: &envoy_type_matcher.RegexMatcher_GoogleRe2{
							GoogleRe2: &envoy_type_matcher.RegexMatcher_GoogleRE2{
								MaxProgramSize: nil,
							},
						},
						Regex: xWso2Basepath,
					},
					Substitution: endpoint.GetBasepath(),
				},
				ClusterSpecifier: &route.RouteAction_Cluster{
					Cluster: clusterName,
				},
			},
		}
	} else {
		action =  &route.Route_Route{
			Route: &route.RouteAction{
				HostRewriteSpecifier: &route.RouteAction_HostRewriteLiteral{
					HostRewriteLiteral: endpoint.GetHost(),
				},
				ClusterSpecifier: &route.RouteAction_Cluster{
					Cluster: clusterName,
				},
			},
		}
	}

	router = route.Route{
		Name: "routename",
		Match: match,
		Action: action,
		Metadata: nil,
	}

	//fmt.Println(endpoint.GetHost(), routePath)
	return router
}

/**
 * Generates route paths for the api resources.
 *
 * @param xWso2Basepath   Xwso2 basepath
 * @param basePath  Default basepath
 * @param resourcePath  Resource path
 * @return string  new route path
 */
func GenerateRoutePaths(xWso2Basepath string, basePath string, resourcePath string) string {
	newPath := ""
	if xWso2Basepath != "" {
		fullpath := xWso2Basepath + resourcePath
		newPath = GenerateRegex(fullpath)

	} else {
		fullpath := basePath + resourcePath
		newPath = GenerateRegex(fullpath)
	}

	return newPath
}

/**
 * Generates regex for the resources which have path paramaters.
 * If path has path parameters ({id}), append a regex pattern (pathParaRegex).
 * To avoid query parameter issues, add a regex pattern ( endRegex) for end of all routes.
 *
 * @param fullpath   resource full path
 * @return string  new route path
 */
func GenerateRegex(fullpath string) string {
	pathParaRegex := "([^/]+)"
	endRegex := "(\\?([^/]+))?"
	newPath := ""

	if strings.Contains(fullpath, "{") || strings.Contains(fullpath, "}") {
		res1 := strings.Split(fullpath, "/")

		for i, p := range res1 {
			if strings.Contains(p, "{") || strings.Contains(p, "}") {
				res1[i] = pathParaRegex
			}
		}
		newPath = "^" + strings.Join(res1[:], "/") + endRegex + "$"

	} else {
		newPath = "^" + fullpath + endRegex + "$"
	}
	return newPath
}
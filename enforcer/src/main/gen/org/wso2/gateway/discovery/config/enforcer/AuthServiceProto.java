// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: wso2/discovery/config/enforcer/auth_service.proto

package org.wso2.gateway.discovery.config.enforcer;

public final class AuthServiceProto {
  private AuthServiceProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_wso2_discovery_config_enforcer_AuthService_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_wso2_discovery_config_enforcer_AuthService_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n1wso2/discovery/config/enforcer/auth_se" +
      "rvice.proto\022\036wso2.discovery.config.enfor" +
      "cer\0320wso2/discovery/config/enforcer/thre" +
      "ad_pool.proto\"\242\001\n\013AuthService\022\014\n\004port\030\001 " +
      "\001(\005\022\026\n\016maxMessageSize\030\002 \001(\005\022\026\n\016maxHeader" +
      "Limit\030\003 \001(\005\022\025\n\rkeepAliveTime\030\004 \001(\005\022>\n\nth" +
      "readPool\030\005 \001(\0132*.wso2.discovery.config.e" +
      "nforcer.ThreadPoolB\220\001\n*org.wso2.gateway." +
      "discovery.config.enforcerB\020AuthServicePr" +
      "otoP\001ZNgithub.com/envoyproxy/go-control-" +
      "plane/wso2/discovery/config/enforcer;enf" +
      "orcerb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          org.wso2.gateway.discovery.config.enforcer.ThreadPoolProto.getDescriptor(),
        });
    internal_static_wso2_discovery_config_enforcer_AuthService_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_wso2_discovery_config_enforcer_AuthService_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_wso2_discovery_config_enforcer_AuthService_descriptor,
        new java.lang.String[] { "Port", "MaxMessageSize", "MaxHeaderLimit", "KeepAliveTime", "ThreadPool", });
    org.wso2.gateway.discovery.config.enforcer.ThreadPoolProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
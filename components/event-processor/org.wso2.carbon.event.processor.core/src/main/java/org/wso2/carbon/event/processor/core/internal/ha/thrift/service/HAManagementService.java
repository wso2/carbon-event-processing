/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.processor.core.internal.ha.thrift.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HAManagementService {

  public interface Iface {

    public org.wso2.carbon.event.processor.core.internal.ha.thrift.data.SnapshotData takeSnapshot(int tenantId, String executionPlan, org.wso2.carbon.event.processor.core.internal.ha.thrift.data.CEPMembership passiveMember) throws org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.NotAnActiveMemberException, org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.InternalServerException, org.apache.thrift.TException;

  }

  public interface AsyncIface {

    public void takeSnapshot(int tenantId, String executionPlan, org.wso2.carbon.event.processor.core.internal.ha.thrift.data.CEPMembership passiveMember, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.takeSnapshot_call> resultHandler) throws org.apache.thrift.TException;

  }

  public static class Client extends org.apache.thrift.TServiceClient implements Iface {
    public static class Factory implements org.apache.thrift.TServiceClientFactory<Client> {
      public Factory() {}
      public Client getClient(org.apache.thrift.protocol.TProtocol prot) {
        return new Client(prot);
      }
      public Client getClient(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {
        return new Client(iprot, oprot);
      }
    }

    public Client(org.apache.thrift.protocol.TProtocol prot)
    {
      super(prot, prot);
    }

    public Client(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {
      super(iprot, oprot);
    }

    public org.wso2.carbon.event.processor.core.internal.ha.thrift.data.SnapshotData takeSnapshot(int tenantId, String executionPlan, org.wso2.carbon.event.processor.core.internal.ha.thrift.data.CEPMembership passiveMember) throws org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.NotAnActiveMemberException, org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.InternalServerException, org.apache.thrift.TException
    {
      send_takeSnapshot(tenantId, executionPlan, passiveMember);
      return recv_takeSnapshot();
    }

    public void send_takeSnapshot(int tenantId, String executionPlan, org.wso2.carbon.event.processor.core.internal.ha.thrift.data.CEPMembership passiveMember) throws org.apache.thrift.TException
    {
      takeSnapshot_args args = new takeSnapshot_args();
      args.setTenantId(tenantId);
      args.setExecutionPlan(executionPlan);
      args.setPassiveMember(passiveMember);
      sendBase("takeSnapshot", args);
    }

    public org.wso2.carbon.event.processor.core.internal.ha.thrift.data.SnapshotData recv_takeSnapshot() throws org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.NotAnActiveMemberException, org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.InternalServerException, org.apache.thrift.TException
    {
      takeSnapshot_result result = new takeSnapshot_result();
      receiveBase(result, "takeSnapshot");
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.anme != null) {
        throw result.anme;
      }
      if (result.ise != null) {
        throw result.ise;
      }
      throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "takeSnapshot failed: unknown result");
    }

  }
  public static class AsyncClient extends org.apache.thrift.async.TAsyncClient implements AsyncIface {
    public static class Factory implements org.apache.thrift.async.TAsyncClientFactory<AsyncClient> {
      private org.apache.thrift.async.TAsyncClientManager clientManager;
      private org.apache.thrift.protocol.TProtocolFactory protocolFactory;
      public Factory(org.apache.thrift.async.TAsyncClientManager clientManager, org.apache.thrift.protocol.TProtocolFactory protocolFactory) {
        this.clientManager = clientManager;
        this.protocolFactory = protocolFactory;
      }
      public AsyncClient getAsyncClient(org.apache.thrift.transport.TNonblockingTransport transport) {
        return new AsyncClient(protocolFactory, clientManager, transport);
      }
    }

    public AsyncClient(org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.async.TAsyncClientManager clientManager, org.apache.thrift.transport.TNonblockingTransport transport) {
      super(protocolFactory, clientManager, transport);
    }

    public void takeSnapshot(int tenantId, String executionPlan, org.wso2.carbon.event.processor.core.internal.ha.thrift.data.CEPMembership passiveMember, org.apache.thrift.async.AsyncMethodCallback<takeSnapshot_call> resultHandler) throws org.apache.thrift.TException {
      checkReady();
      takeSnapshot_call method_call = new takeSnapshot_call(tenantId, executionPlan, passiveMember, resultHandler, this, ___protocolFactory, ___transport);
      this.___currentMethod = method_call;
      ___manager.call(method_call);
    }

    public static class takeSnapshot_call extends org.apache.thrift.async.TAsyncMethodCall {
      private int tenantId;
      private String executionPlan;
      private org.wso2.carbon.event.processor.core.internal.ha.thrift.data.CEPMembership passiveMember;
      public takeSnapshot_call(int tenantId, String executionPlan, org.wso2.carbon.event.processor.core.internal.ha.thrift.data.CEPMembership passiveMember, org.apache.thrift.async.AsyncMethodCallback<takeSnapshot_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.tenantId = tenantId;
        this.executionPlan = executionPlan;
        this.passiveMember = passiveMember;
      }

      public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
        prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("takeSnapshot", org.apache.thrift.protocol.TMessageType.CALL, 0));
        takeSnapshot_args args = new takeSnapshot_args();
        args.setTenantId(tenantId);
        args.setExecutionPlan(executionPlan);
        args.setPassiveMember(passiveMember);
        args.write(prot);
        prot.writeMessageEnd();
      }

      public org.wso2.carbon.event.processor.core.internal.ha.thrift.data.SnapshotData getResult() throws org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.NotAnActiveMemberException, org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.InternalServerException, org.apache.thrift.TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
        org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_takeSnapshot();
      }
    }

  }

  public static class Processor<I extends Iface> extends org.apache.thrift.TBaseProcessor implements org.apache.thrift.TProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class.getName());
    public Processor(I iface) {
      super(iface, getProcessMap(new HashMap<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>>()));
    }

    protected Processor(I iface, Map<String,  org.apache.thrift.ProcessFunction<I, ? extends  org.apache.thrift.TBase>> processMap) {
      super(iface, getProcessMap(processMap));
    }

    private static <I extends Iface> Map<String,  org.apache.thrift.ProcessFunction<I, ? extends  org.apache.thrift.TBase>> getProcessMap(Map<String,  org.apache.thrift.ProcessFunction<I, ? extends  org.apache.thrift.TBase>> processMap) {
      processMap.put("takeSnapshot", new takeSnapshot());
      return processMap;
    }

    private static class takeSnapshot<I extends Iface> extends org.apache.thrift.ProcessFunction<I, takeSnapshot_args> {
      public takeSnapshot() {
        super("takeSnapshot");
      }

      protected takeSnapshot_args getEmptyArgsInstance() {
        return new takeSnapshot_args();
      }

      protected takeSnapshot_result getResult(I iface, takeSnapshot_args args) throws org.apache.thrift.TException {
        takeSnapshot_result result = new takeSnapshot_result();
        try {
          result.success = iface.takeSnapshot(args.tenantId, args.executionPlan, args.passiveMember);
        } catch (org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.NotAnActiveMemberException anme) {
          result.anme = anme;
        } catch (org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.InternalServerException ise) {
          result.ise = ise;
        }
        return result;
      }
    }

  }

  public static class takeSnapshot_args implements org.apache.thrift.TBase<takeSnapshot_args, takeSnapshot_args._Fields>, java.io.Serializable, Cloneable   {
    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("takeSnapshot_args");

    private static final org.apache.thrift.protocol.TField TENANT_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("tenantId", org.apache.thrift.protocol.TType.I32, (short)1);
    private static final org.apache.thrift.protocol.TField EXECUTION_PLAN_FIELD_DESC = new org.apache.thrift.protocol.TField("executionPlan", org.apache.thrift.protocol.TType.STRING, (short)2);
    private static final org.apache.thrift.protocol.TField PASSIVE_MEMBER_FIELD_DESC = new org.apache.thrift.protocol.TField("passiveMember", org.apache.thrift.protocol.TType.STRUCT, (short)3);

    public int tenantId; // required
    public String executionPlan; // required
    public org.wso2.carbon.event.processor.core.internal.ha.thrift.data.CEPMembership passiveMember; // required

    /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
      TENANT_ID((short)1, "tenantId"),
      EXECUTION_PLAN((short)2, "executionPlan"),
      PASSIVE_MEMBER((short)3, "passiveMember");

      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, or null if its not found.
       */
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: // TENANT_ID
            return TENANT_ID;
          case 2: // EXECUTION_PLAN
            return EXECUTION_PLAN;
          case 3: // PASSIVE_MEMBER
            return PASSIVE_MEMBER;
          default:
            return null;
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, throwing an exception
       * if it is not found.
       */
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }

      /**
       * Find the _Fields constant that matches name, or null if its not found.
       */
      public static _Fields findByName(String name) {
        return byName.get(name);
      }

      private final short _thriftId;
      private final String _fieldName;

      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }

      public short getThriftFieldId() {
        return _thriftId;
      }

      public String getFieldName() {
        return _fieldName;
      }
    }

    // isset id assignments
    private static final int __TENANTID_ISSET_ID = 0;
    private BitSet __isset_bit_vector = new BitSet(1);

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
    static {
      Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.TENANT_ID, new org.apache.thrift.meta_data.FieldMetaData("tenantId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
      tmpMap.put(_Fields.EXECUTION_PLAN, new org.apache.thrift.meta_data.FieldMetaData("executionPlan", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
      tmpMap.put(_Fields.PASSIVE_MEMBER, new org.apache.thrift.meta_data.FieldMetaData("passiveMember", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.wso2.carbon.event.processor.core.internal.ha.thrift.data.CEPMembership.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(takeSnapshot_args.class, metaDataMap);
    }

    public takeSnapshot_args() {
    }

    public takeSnapshot_args(
      int tenantId,
      String executionPlan,
      org.wso2.carbon.event.processor.core.internal.ha.thrift.data.CEPMembership passiveMember)
    {
      this();
      this.tenantId = tenantId;
      setTenantIdIsSet(true);
      this.executionPlan = executionPlan;
      this.passiveMember = passiveMember;
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public takeSnapshot_args(takeSnapshot_args other) {
      __isset_bit_vector.clear();
      __isset_bit_vector.or(other.__isset_bit_vector);
      this.tenantId = other.tenantId;
      if (other.isSetExecutionPlan()) {
        this.executionPlan = other.executionPlan;
      }
      if (other.isSetPassiveMember()) {
        this.passiveMember = new org.wso2.carbon.event.processor.core.internal.ha.thrift.data.CEPMembership(other.passiveMember);
      }
    }

    public takeSnapshot_args deepCopy() {
      return new takeSnapshot_args(this);
    }

    @Override
    public void clear() {
      setTenantIdIsSet(false);
      this.tenantId = 0;
      this.executionPlan = null;
      this.passiveMember = null;
    }

    public int getTenantId() {
      return this.tenantId;
    }

    public takeSnapshot_args setTenantId(int tenantId) {
      this.tenantId = tenantId;
      setTenantIdIsSet(true);
      return this;
    }

    public void unsetTenantId() {
      __isset_bit_vector.clear(__TENANTID_ISSET_ID);
    }

    /** Returns true if field tenantId is set (has been assigned a value) and false otherwise */
    public boolean isSetTenantId() {
      return __isset_bit_vector.get(__TENANTID_ISSET_ID);
    }

    public void setTenantIdIsSet(boolean value) {
      __isset_bit_vector.set(__TENANTID_ISSET_ID, value);
    }

    public String getExecutionPlan() {
      return this.executionPlan;
    }

    public takeSnapshot_args setExecutionPlan(String executionPlan) {
      this.executionPlan = executionPlan;
      return this;
    }

    public void unsetExecutionPlan() {
      this.executionPlan = null;
    }

    /** Returns true if field executionPlan is set (has been assigned a value) and false otherwise */
    public boolean isSetExecutionPlan() {
      return this.executionPlan != null;
    }

    public void setExecutionPlanIsSet(boolean value) {
      if (!value) {
        this.executionPlan = null;
      }
    }

    public org.wso2.carbon.event.processor.core.internal.ha.thrift.data.CEPMembership getPassiveMember() {
      return this.passiveMember;
    }

    public takeSnapshot_args setPassiveMember(org.wso2.carbon.event.processor.core.internal.ha.thrift.data.CEPMembership passiveMember) {
      this.passiveMember = passiveMember;
      return this;
    }

    public void unsetPassiveMember() {
      this.passiveMember = null;
    }

    /** Returns true if field passiveMember is set (has been assigned a value) and false otherwise */
    public boolean isSetPassiveMember() {
      return this.passiveMember != null;
    }

    public void setPassiveMemberIsSet(boolean value) {
      if (!value) {
        this.passiveMember = null;
      }
    }

    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case TENANT_ID:
        if (value == null) {
          unsetTenantId();
        } else {
          setTenantId((Integer)value);
        }
        break;

      case EXECUTION_PLAN:
        if (value == null) {
          unsetExecutionPlan();
        } else {
          setExecutionPlan((String)value);
        }
        break;

      case PASSIVE_MEMBER:
        if (value == null) {
          unsetPassiveMember();
        } else {
          setPassiveMember((org.wso2.carbon.event.processor.core.internal.ha.thrift.data.CEPMembership)value);
        }
        break;

      }
    }

    public Object getFieldValue(_Fields field) {
      switch (field) {
      case TENANT_ID:
        return Integer.valueOf(getTenantId());

      case EXECUTION_PLAN:
        return getExecutionPlan();

      case PASSIVE_MEMBER:
        return getPassiveMember();

      }
      throw new IllegalStateException();
    }

    /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }

      switch (field) {
      case TENANT_ID:
        return isSetTenantId();
      case EXECUTION_PLAN:
        return isSetExecutionPlan();
      case PASSIVE_MEMBER:
        return isSetPassiveMember();
      }
      throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof takeSnapshot_args)
        return this.equals((takeSnapshot_args)that);
      return false;
    }

    public boolean equals(takeSnapshot_args that) {
      if (that == null)
        return false;

      boolean this_present_tenantId = true;
      boolean that_present_tenantId = true;
      if (this_present_tenantId || that_present_tenantId) {
        if (!(this_present_tenantId && that_present_tenantId))
          return false;
        if (this.tenantId != that.tenantId)
          return false;
      }

      boolean this_present_executionPlan = true && this.isSetExecutionPlan();
      boolean that_present_executionPlan = true && that.isSetExecutionPlan();
      if (this_present_executionPlan || that_present_executionPlan) {
        if (!(this_present_executionPlan && that_present_executionPlan))
          return false;
        if (!this.executionPlan.equals(that.executionPlan))
          return false;
      }

      boolean this_present_passiveMember = true && this.isSetPassiveMember();
      boolean that_present_passiveMember = true && that.isSetPassiveMember();
      if (this_present_passiveMember || that_present_passiveMember) {
        if (!(this_present_passiveMember && that_present_passiveMember))
          return false;
        if (!this.passiveMember.equals(that.passiveMember))
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    public int compareTo(takeSnapshot_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      takeSnapshot_args typedOther = (takeSnapshot_args)other;

      lastComparison = Boolean.valueOf(isSetTenantId()).compareTo(typedOther.isSetTenantId());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTenantId()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tenantId, typedOther.tenantId);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetExecutionPlan()).compareTo(typedOther.isSetExecutionPlan());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetExecutionPlan()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.executionPlan, typedOther.executionPlan);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetPassiveMember()).compareTo(typedOther.isSetPassiveMember());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetPassiveMember()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.passiveMember, typedOther.passiveMember);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }

    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }

    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: // TENANT_ID
            if (field.type == org.apache.thrift.protocol.TType.I32) {
              this.tenantId = iprot.readI32();
              setTenantIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: // EXECUTION_PLAN
            if (field.type == org.apache.thrift.protocol.TType.STRING) {
              this.executionPlan = iprot.readString();
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: // PASSIVE_MEMBER
            if (field.type == org.apache.thrift.protocol.TType.STRUCT) {
              this.passiveMember = new org.wso2.carbon.event.processor.core.internal.ha.thrift.data.CEPMembership();
              this.passiveMember.read(iprot);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
      validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(TENANT_ID_FIELD_DESC);
      oprot.writeI32(this.tenantId);
      oprot.writeFieldEnd();
      if (this.executionPlan != null) {
        oprot.writeFieldBegin(EXECUTION_PLAN_FIELD_DESC);
        oprot.writeString(this.executionPlan);
        oprot.writeFieldEnd();
      }
      if (this.passiveMember != null) {
        oprot.writeFieldBegin(PASSIVE_MEMBER_FIELD_DESC);
        this.passiveMember.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("takeSnapshot_args(");
      boolean first = true;

      sb.append("tenantId:");
      sb.append(this.tenantId);
      first = false;
      if (!first) sb.append(", ");
      sb.append("executionPlan:");
      if (this.executionPlan == null) {
        sb.append("null");
      } else {
        sb.append(this.executionPlan);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("passiveMember:");
      if (this.passiveMember == null) {
        sb.append("null");
      } else {
        sb.append(this.passiveMember);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
      // check for required fields
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
      try {
        write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te);
      }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
      try {
        // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
        __isset_bit_vector = new BitSet(1);
        read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te);
      }
    }

  }

  public static class takeSnapshot_result implements org.apache.thrift.TBase<takeSnapshot_result, takeSnapshot_result._Fields>, java.io.Serializable, Cloneable   {
    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("takeSnapshot_result");

    private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.STRUCT, (short)0);
    private static final org.apache.thrift.protocol.TField ANME_FIELD_DESC = new org.apache.thrift.protocol.TField("anme", org.apache.thrift.protocol.TType.STRUCT, (short)1);
    private static final org.apache.thrift.protocol.TField ISE_FIELD_DESC = new org.apache.thrift.protocol.TField("ise", org.apache.thrift.protocol.TType.STRUCT, (short)2);

    public org.wso2.carbon.event.processor.core.internal.ha.thrift.data.SnapshotData success; // required
    public org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.NotAnActiveMemberException anme; // required
    public org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.InternalServerException ise; // required

    /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
      SUCCESS((short)0, "success"),
      ANME((short)1, "anme"),
      ISE((short)2, "ise");

      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, or null if its not found.
       */
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: // SUCCESS
            return SUCCESS;
          case 1: // ANME
            return ANME;
          case 2: // ISE
            return ISE;
          default:
            return null;
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, throwing an exception
       * if it is not found.
       */
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }

      /**
       * Find the _Fields constant that matches name, or null if its not found.
       */
      public static _Fields findByName(String name) {
        return byName.get(name);
      }

      private final short _thriftId;
      private final String _fieldName;

      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }

      public short getThriftFieldId() {
        return _thriftId;
      }

      public String getFieldName() {
        return _fieldName;
      }
    }

    // isset id assignments

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
    static {
      Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.wso2.carbon.event.processor.core.internal.ha.thrift.data.SnapshotData.class)));
      tmpMap.put(_Fields.ANME, new org.apache.thrift.meta_data.FieldMetaData("anme", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
      tmpMap.put(_Fields.ISE, new org.apache.thrift.meta_data.FieldMetaData("ise", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(takeSnapshot_result.class, metaDataMap);
    }

    public takeSnapshot_result() {
    }

    public takeSnapshot_result(
      org.wso2.carbon.event.processor.core.internal.ha.thrift.data.SnapshotData success,
      org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.NotAnActiveMemberException anme,
      org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.InternalServerException ise)
    {
      this();
      this.success = success;
      this.anme = anme;
      this.ise = ise;
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public takeSnapshot_result(takeSnapshot_result other) {
      if (other.isSetSuccess()) {
        this.success = new org.wso2.carbon.event.processor.core.internal.ha.thrift.data.SnapshotData(other.success);
      }
      if (other.isSetAnme()) {
        this.anme = new org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.NotAnActiveMemberException(other.anme);
      }
      if (other.isSetIse()) {
        this.ise = new org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.InternalServerException(other.ise);
      }
    }

    public takeSnapshot_result deepCopy() {
      return new takeSnapshot_result(this);
    }

    @Override
    public void clear() {
      this.success = null;
      this.anme = null;
      this.ise = null;
    }

    public org.wso2.carbon.event.processor.core.internal.ha.thrift.data.SnapshotData getSuccess() {
      return this.success;
    }

    public takeSnapshot_result setSuccess(org.wso2.carbon.event.processor.core.internal.ha.thrift.data.SnapshotData success) {
      this.success = success;
      return this;
    }

    public void unsetSuccess() {
      this.success = null;
    }

    /** Returns true if field success is set (has been assigned a value) and false otherwise */
    public boolean isSetSuccess() {
      return this.success != null;
    }

    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }

    public org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.NotAnActiveMemberException getAnme() {
      return this.anme;
    }

    public takeSnapshot_result setAnme(org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.NotAnActiveMemberException anme) {
      this.anme = anme;
      return this;
    }

    public void unsetAnme() {
      this.anme = null;
    }

    /** Returns true if field anme is set (has been assigned a value) and false otherwise */
    public boolean isSetAnme() {
      return this.anme != null;
    }

    public void setAnmeIsSet(boolean value) {
      if (!value) {
        this.anme = null;
      }
    }

    public org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.InternalServerException getIse() {
      return this.ise;
    }

    public takeSnapshot_result setIse(org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.InternalServerException ise) {
      this.ise = ise;
      return this;
    }

    public void unsetIse() {
      this.ise = null;
    }

    /** Returns true if field ise is set (has been assigned a value) and false otherwise */
    public boolean isSetIse() {
      return this.ise != null;
    }

    public void setIseIsSet(boolean value) {
      if (!value) {
        this.ise = null;
      }
    }

    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((org.wso2.carbon.event.processor.core.internal.ha.thrift.data.SnapshotData)value);
        }
        break;

      case ANME:
        if (value == null) {
          unsetAnme();
        } else {
          setAnme((org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.NotAnActiveMemberException)value);
        }
        break;

      case ISE:
        if (value == null) {
          unsetIse();
        } else {
          setIse((org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.InternalServerException)value);
        }
        break;

      }
    }

    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();

      case ANME:
        return getAnme();

      case ISE:
        return getIse();

      }
      throw new IllegalStateException();
    }

    /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }

      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case ANME:
        return isSetAnme();
      case ISE:
        return isSetIse();
      }
      throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof takeSnapshot_result)
        return this.equals((takeSnapshot_result)that);
      return false;
    }

    public boolean equals(takeSnapshot_result that) {
      if (that == null)
        return false;

      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }

      boolean this_present_anme = true && this.isSetAnme();
      boolean that_present_anme = true && that.isSetAnme();
      if (this_present_anme || that_present_anme) {
        if (!(this_present_anme && that_present_anme))
          return false;
        if (!this.anme.equals(that.anme))
          return false;
      }

      boolean this_present_ise = true && this.isSetIse();
      boolean that_present_ise = true && that.isSetIse();
      if (this_present_ise || that_present_ise) {
        if (!(this_present_ise && that_present_ise))
          return false;
        if (!this.ise.equals(that.ise))
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    public int compareTo(takeSnapshot_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      takeSnapshot_result typedOther = (takeSnapshot_result)other;

      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetAnme()).compareTo(typedOther.isSetAnme());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetAnme()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.anme, typedOther.anme);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIse()).compareTo(typedOther.isSetIse());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIse()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ise, typedOther.ise);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }

    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }

    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: // SUCCESS
            if (field.type == org.apache.thrift.protocol.TType.STRUCT) {
              this.success = new org.wso2.carbon.event.processor.core.internal.ha.thrift.data.SnapshotData();
              this.success.read(iprot);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: // ANME
            if (field.type == org.apache.thrift.protocol.TType.STRUCT) {
              this.anme = new org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.NotAnActiveMemberException();
              this.anme.read(iprot);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: // ISE
            if (field.type == org.apache.thrift.protocol.TType.STRUCT) {
              this.ise = new org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.InternalServerException();
              this.ise.read(iprot);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
      oprot.writeStructBegin(STRUCT_DESC);

      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        this.success.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetAnme()) {
        oprot.writeFieldBegin(ANME_FIELD_DESC);
        this.anme.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetIse()) {
        oprot.writeFieldBegin(ISE_FIELD_DESC);
        this.ise.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("takeSnapshot_result(");
      boolean first = true;

      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("anme:");
      if (this.anme == null) {
        sb.append("null");
      } else {
        sb.append(this.anme);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ise:");
      if (this.ise == null) {
        sb.append("null");
      } else {
        sb.append(this.ise);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
      // check for required fields
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
      try {
        write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te);
      }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
      try {
        read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te);
      }
    }

  }

}

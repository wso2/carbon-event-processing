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
package org.wso2.carbon.event.processor.core.internal.ha.thrift.data;

import java.nio.ByteBuffer;
import java.util.*;

public class SnapshotData implements org.apache.thrift.TBase<SnapshotData, SnapshotData._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("SnapshotData");

  private static final org.apache.thrift.protocol.TField STATES_FIELD_DESC = new org.apache.thrift.protocol.TField("states", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField NEXT_EVENT_DATA_FIELD_DESC = new org.apache.thrift.protocol.TField("nextEventData", org.apache.thrift.protocol.TType.STRING, (short)2);

  public ByteBuffer states; // required
  public ByteBuffer nextEventData; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    STATES((short)1, "states"),
    NEXT_EVENT_DATA((short)2, "nextEventData");

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
        case 1: // STATES
          return STATES;
        case 2: // NEXT_EVENT_DATA
          return NEXT_EVENT_DATA;
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
    tmpMap.put(_Fields.STATES, new org.apache.thrift.meta_data.FieldMetaData("states", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    tmpMap.put(_Fields.NEXT_EVENT_DATA, new org.apache.thrift.meta_data.FieldMetaData("nextEventData", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(SnapshotData.class, metaDataMap);
  }

  public SnapshotData() {
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public SnapshotData(SnapshotData other) {
    if (other.isSetStates()) {
      this.states = org.apache.thrift.TBaseHelper.copyBinary(other.states);
;
    }
    if (other.isSetNextEventData()) {
      this.nextEventData = org.apache.thrift.TBaseHelper.copyBinary(other.nextEventData);
;
    }
  }

  public SnapshotData deepCopy() {
    return new SnapshotData(this);
  }

  @Override
  public void clear() {
    this.states = null;
    this.nextEventData = null;
  }

  public byte[] getStates() {
    setStates(org.apache.thrift.TBaseHelper.rightSize(states));
    return states == null ? null : states.array();
  }

  public ByteBuffer bufferForStates() {
    return states;
  }

  public SnapshotData setStates(byte[] states) {
    setStates(states == null ? (ByteBuffer)null : ByteBuffer.wrap(states));
    return this;
  }

  public SnapshotData setStates(ByteBuffer states) {
    this.states = states;
    return this;
  }

  public void unsetStates() {
    this.states = null;
  }

  /** Returns true if field states is set (has been assigned a value) and false otherwise */
  public boolean isSetStates() {
    return this.states != null;
  }

  public void setStatesIsSet(boolean value) {
    if (!value) {
      this.states = null;
    }
  }

  public byte[] getNextEventData() {
    setNextEventData(org.apache.thrift.TBaseHelper.rightSize(nextEventData));
    return nextEventData == null ? null : nextEventData.array();
  }

  public ByteBuffer bufferForNextEventData() {
    return nextEventData;
  }

  public SnapshotData setNextEventData(byte[] nextEventData) {
    setNextEventData(nextEventData == null ? (ByteBuffer)null : ByteBuffer.wrap(nextEventData));
    return this;
  }

  public SnapshotData setNextEventData(ByteBuffer nextEventData) {
    this.nextEventData = nextEventData;
    return this;
  }

  public void unsetNextEventData() {
    this.nextEventData = null;
  }

  /** Returns true if field nextEventData is set (has been assigned a value) and false otherwise */
  public boolean isSetNextEventData() {
    return this.nextEventData != null;
  }

  public void setNextEventDataIsSet(boolean value) {
    if (!value) {
      this.nextEventData = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case STATES:
      if (value == null) {
        unsetStates();
      } else {
        setStates((ByteBuffer)value);
      }
      break;

    case NEXT_EVENT_DATA:
      if (value == null) {
        unsetNextEventData();
      } else {
        setNextEventData((ByteBuffer)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case STATES:
      return getStates();

    case NEXT_EVENT_DATA:
      return getNextEventData();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case STATES:
      return isSetStates();
    case NEXT_EVENT_DATA:
      return isSetNextEventData();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof SnapshotData)
      return this.equals((SnapshotData)that);
    return false;
  }

  public boolean equals(SnapshotData that) {
    if (that == null)
      return false;

    boolean this_present_states = true && this.isSetStates();
    boolean that_present_states = true && that.isSetStates();
    if (this_present_states || that_present_states) {
      if (!(this_present_states && that_present_states))
        return false;
      if (!this.states.equals(that.states))
        return false;
    }

    boolean this_present_nextEventData = true && this.isSetNextEventData();
    boolean that_present_nextEventData = true && that.isSetNextEventData();
    if (this_present_nextEventData || that_present_nextEventData) {
      if (!(this_present_nextEventData && that_present_nextEventData))
        return false;
      if (!this.nextEventData.equals(that.nextEventData))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(SnapshotData other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    SnapshotData typedOther = (SnapshotData)other;

    lastComparison = Boolean.valueOf(isSetStates()).compareTo(typedOther.isSetStates());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStates()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.states, typedOther.states);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetNextEventData()).compareTo(typedOther.isSetNextEventData());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetNextEventData()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.nextEventData, typedOther.nextEventData);
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
        case 1: // STATES
          if (field.type == org.apache.thrift.protocol.TType.STRING) {
            this.states = iprot.readBinary();
          } else { 
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: // NEXT_EVENT_DATA
          if (field.type == org.apache.thrift.protocol.TType.STRING) {
            this.nextEventData = iprot.readBinary();
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
    if (this.states != null) {
      if (isSetStates()) {
        oprot.writeFieldBegin(STATES_FIELD_DESC);
        oprot.writeBinary(this.states);
        oprot.writeFieldEnd();
      }
    }
    if (this.nextEventData != null) {
      if (isSetNextEventData()) {
        oprot.writeFieldBegin(NEXT_EVENT_DATA_FIELD_DESC);
        oprot.writeBinary(this.nextEventData);
        oprot.writeFieldEnd();
      }
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("SnapshotData(");
    boolean first = true;

    if (isSetStates()) {
      sb.append("states:");
      if (this.states == null) {
        sb.append("null");
      } else {
        org.apache.thrift.TBaseHelper.toString(this.states, sb);
      }
      first = false;
    }
    if (isSetNextEventData()) {
      if (!first) sb.append(", ");
      sb.append("nextEventData:");
      if (this.nextEventData == null) {
        sb.append("null");
      } else {
        org.apache.thrift.TBaseHelper.toString(this.nextEventData, sb);
      }
      first = false;
    }
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


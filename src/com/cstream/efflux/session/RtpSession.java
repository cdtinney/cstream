/*
 * Copyright 2010 Bruno de Carvalho
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cstream.efflux.session;

import java.util.Map;
import java.util.Set;

import com.cstream.efflux.network.ControlPacketReceiver;
import com.cstream.efflux.network.DataPacketReceiver;
import com.cstream.efflux.packet.CompoundControlPacket;
import com.cstream.efflux.packet.ControlPacket;
import com.cstream.efflux.packet.DataPacket;
import com.cstream.efflux.participant.RtpParticipant;

/**
 * @author <a href="http://bruno.biasedbit.com/">Bruno de Carvalho</a>
 */
public interface RtpSession extends DataPacketReceiver, ControlPacketReceiver {

    String getId();

    Set<Integer> getPayloadType();

    boolean init();

    void terminate();

    boolean sendData(byte[] data, long timestamp, boolean marked);

    boolean sendDataPacket(DataPacket packet);

    boolean sendControlPacket(ControlPacket packet);

    boolean sendControlPacket(CompoundControlPacket packet);

    RtpParticipant getLocalParticipant();

    boolean addReceiver(RtpParticipant remoteParticipant);

    boolean removeReceiver(RtpParticipant remoteParticipant);

    RtpParticipant getRemoteParticipant(long ssrsc);

    Map<Long, RtpParticipant> getRemoteParticipants();

    void addDataListener(RtpSessionDataListener listener);

    void removeDataListener(RtpSessionDataListener listener);

    void addControlListener(RtpSessionControlListener listener);

    void removeControlListener(RtpSessionControlListener listener);

    void addEventListener(RtpSessionEventListener listener);

    void removeEventListener(RtpSessionEventListener listener);
}

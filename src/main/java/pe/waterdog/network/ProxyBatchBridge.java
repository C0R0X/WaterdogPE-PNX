/**
 * Copyright 2020 WaterdogTEAM
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pe.waterdog.network;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.BedrockSession;
import com.nukkitx.protocol.bedrock.handler.BatchHandler;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.NetworkStackLatencyPacket;
import io.netty.buffer.ByteBuf;
import pe.waterdog.logger.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProxyBatchBridge implements BatchHandler {

    private final BedrockSession session;

    public ProxyBatchBridge(BedrockSession session){
        this.session = session;
    }

    @Override
    public void handle(BedrockSession session, ByteBuf buf, Collection<BedrockPacket> packets) {
        List<BedrockPacket> unhandledPackets =  new ArrayList<>();
        boolean wrapperHandled = false;

        for (BedrockPacket packet : packets){
            BedrockPacketHandler handler = session.getPacketHandler();

            if (!(packet instanceof NetworkStackLatencyPacket)) {
                //Logger.getLogger().info(session.getAddress() +" <-> "+packet);
            }

            if (handler != null && packet.handle(handler)){
                wrapperHandled = true;
                continue;
            }

            unhandledPackets.add(packet);
        }

        if (!wrapperHandled){
            this.session.sendWrapped(unhandledPackets, true);
            return;
        }

        buf.readerIndex(1);
        this.session.sendWrapped(buf, this.session.isEncrypted());
    }
}

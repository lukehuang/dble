/*
* Copyright (C) 2016-2018 ActionTech.
* based on code by MyCATCopyrightHolder Copyright (c) 2013, OpenCloudDB/MyCAT.
* License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
*/
package com.actiontech.dble.server.response;

import com.actiontech.dble.backend.mysql.PacketUtil;
import com.actiontech.dble.config.Fields;
import com.actiontech.dble.net.mysql.EOFPacket;
import com.actiontech.dble.net.mysql.FieldPacket;
import com.actiontech.dble.net.mysql.ResultSetHeaderPacket;
import com.actiontech.dble.net.mysql.RowDataPacket;
import com.actiontech.dble.server.ServerConnection;
import com.actiontech.dble.util.LongUtil;

import java.nio.ByteBuffer;

/**
 * @author mycat
 */
public final class SelectTxReadOnly {
    private SelectTxReadOnly() {
    }

    private static final int FIELD_COUNT = 1;
    private static final ResultSetHeaderPacket HEADER = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] FIELDS = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket EOF = new EOFPacket();

    static {
        int i = 0;
        byte packetId = 0;
        HEADER.setPacketId(++packetId);
        FIELDS[i] = PacketUtil.getField("@@session.tx_read_only", Fields.FIELD_TYPE_LONG);
        FIELDS[i].setPacketId(++packetId);
        EOF.setPacketId(++packetId);

    }

    public static void response(ServerConnection c) {
        ByteBuffer buffer = c.allocate();
        buffer = HEADER.write(buffer, c, true);
        for (FieldPacket field : FIELDS) {
            buffer = field.write(buffer, c, true);
        }
        buffer = EOF.write(buffer, c, true);
        byte packetId = EOF.getPacketId();
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        int result = c.isReadOnly() ? 1 : 0;
        row.add(LongUtil.toBytes(result));
        row.setPacketId(++packetId);
        buffer = row.write(buffer, c, true);
        EOFPacket lastEof = new EOFPacket();
        lastEof.setPacketId(++packetId);
        buffer = lastEof.write(buffer, c, true);
        c.write(buffer);
    }

}

/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package office.fc.hssf.record.chart;


import office.fc.hssf.record.RecordInputStream;
import office.fc.hssf.record.StandardRecord;
import office.fc.util.HexDump;
import office.fc.util.LittleEndianOutput;


/**
 * STARTOBJECT - Chart Future Record Type Start Object (0x0854)<br/>
 *
 * @author Patrick Cheng
 */
public final class ChartStartObjectRecord extends StandardRecord {
	public static final short sid = 0x0854;

	private short rt;
	private short grbitFrt;
	private short iObjectKind;
	private short iObjectContext;
	private short iObjectInstance1;
	private short iObjectInstance2;

	public ChartStartObjectRecord(RecordInputStream in) {
		rt = in.readShort();
		grbitFrt = in.readShort();
		iObjectKind = in.readShort();
		iObjectContext = in.readShort();
		iObjectInstance1 = in.readShort();
		iObjectInstance2 = in.readShort();
	}

	@Override
	protected int getDataSize() {
		return 2 + 2 + 2 + 2 + 2 + 2;
	}

	@Override
	public short getSid() {
		return sid;
	}

	@Override
	public void serialize(LittleEndianOutput out) {
		out.writeShort(rt);
		out.writeShort(grbitFrt);
		out.writeShort(iObjectKind);
		out.writeShort(iObjectContext);
		out.writeShort(iObjectInstance1);
		out.writeShort(iObjectInstance2);
	}

	public String toString() {

		StringBuffer buffer = new StringBuffer();

		buffer.append("[STARTOBJECT]\n");
		buffer.append("    .rt              =").append(HexDump.shortToHex(rt)).append('\n');
		buffer.append("    .grbitFrt        =").append(HexDump.shortToHex(grbitFrt)).append('\n');
		buffer.append("    .iObjectKind     =").append(HexDump.shortToHex(iObjectKind)).append('\n');
		buffer.append("    .iObjectContext  =").append(HexDump.shortToHex(iObjectContext)).append('\n');
		buffer.append("    .iObjectInstance1=").append(HexDump.shortToHex(iObjectInstance1)).append('\n');
		buffer.append("    .iObjectInstance2=").append(HexDump.shortToHex(iObjectInstance2)).append('\n');
		buffer.append("[/STARTOBJECT]\n");
		return buffer.toString();
	}
}

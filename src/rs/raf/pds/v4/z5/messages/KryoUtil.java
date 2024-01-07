package rs.raf.pds.v4.z5.messages;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Output;

import java.util.UUID;

public class KryoUtil {
	public static void registerKryoClasses(Kryo kryo) {
		kryo.register(String.class);
		kryo.register(String[].class);
		kryo.register(Login.class);
		kryo.register(ChatMessage.class);
		kryo.register(WhoRequest.class);
		kryo.register(ListUsers.class);
		kryo.register(InfoMessage.class);
		kryo.register(PrivateMessage.class);
		kryo.register(Room.class);
		kryo.register(CreateRoomRequest.class);
		kryo.register(InviteRequest.class);
		kryo.register(JoinRoomRequest.class);
		kryo.register(ListRoomsRequest.class);
		kryo.register(EditMessageRequest.class);
		kryo.register(GetMoreMessagesRequest.class);
		kryo.register(java.util.ArrayList.class);
		kryo.register(java.util.LinkedList.class);
		kryo.register(java.util.UUID.class, new UUIDSerializer());
		kryo.register(com.esotericsoftware.kryonet.Connection.class);
		kryo.register(com.esotericsoftware.kryonet.Server.class);
		kryo.register(ListRooms.class);
		kryo.register(InvitedToRoomMessage.class);
		kryo.register(EditedMessage.class);
		kryo.register(ReplyMessageRequest.class);
	}
}

// Kryonet nema svoj UUID serializer
class UUIDSerializer extends Serializer<UUID> {

	public UUIDSerializer() {
		setImmutable(true);
	}

	@Override
	public void write(final Kryo kryo, final Output output, final UUID uuid) {
		output.writeLong(uuid.getMostSignificantBits());
		output.writeLong(uuid.getLeastSignificantBits());
	}

	@Override
	public UUID read(final Kryo kryo, final Input input, final Class<UUID> uuidClass) {
		return new UUID(input.readLong(), input.readLong());
	}
}

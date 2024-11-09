package gg.xp.xivapi.test.testutils

class TestUtils {
	static <X> X serializeAndDeserialize(X obj) {
		try (
				ByteArrayOutputStream baos = new ByteArrayOutputStream()
				ObjectOutputStream oos = new ObjectOutputStream(baos)
		) {
			// Serialize the object to the byte array output stream
			oos.writeObject(obj)
			oos.flush()
			byte[] serializedData = baos.toByteArray()

			// Prepare for deserialization from the byte array
			ByteArrayInputStream bais = new ByteArrayInputStream(serializedData)
			ObjectInputStream ois = new ObjectInputStream(bais)

			// Deserialize the object from the byte array input stream
			def deserializedObj = ois.readObject()
			println "Object serialized and deserialized successfully"
			return deserializedObj as X
		}
	}
}

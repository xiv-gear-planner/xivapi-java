package gg.xp.xivapi.test.subrow

import gg.xp.xivapi.XivApiClient
import gg.xp.xivapi.pagination.XivApiPaginator
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class SubRowTest {

	@Test
	void subrowSingleTest() {
		var client = new XivApiClient()
		MapMarker marker = client.getBySubrowId MapMarker, 2, 1
		assertEquals 2, marker.rowId
		assertEquals 1, marker.subrowId
		assertEquals 300, marker.x
		assertEquals 500, marker.y
	}

	@Test
	void subrowSingleBackCompatTest() {
		var client = new XivApiClient()
		// 2 translates to 2:0
		MapMarker marker = client.getById MapMarker, 2
		assertEquals 2, marker.rowId
		assertEquals 0, marker.subrowId
		assertEquals 256, marker.x
		assertEquals 128, marker.y
	}

	@Test
	void subrowListTest() {
		var client = new XivApiClient()
		XivApiPaginator<MapMarker> paginator = client.getListIterator MapMarker;
		{
			MapMarker marker = paginator.next()
			assertEquals 0, marker.rowId
			assertEquals 0, marker.subrowId
			assertEquals 0, marker.x
			assertEquals 0, marker.y
		}
		{
			MapMarker marker = paginator.next()
			assertEquals 1, marker.rowId
			assertEquals 0, marker.subrowId
			assertEquals 256, marker.x
			assertEquals 128, marker.y
		}
		{
			MapMarker marker = paginator.next()
			assertEquals 1, marker.rowId
			assertEquals 1, marker.subrowId
			assertEquals 300, marker.x
			assertEquals 500, marker.y
		}

	}

}

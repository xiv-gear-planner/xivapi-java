package gg.xp.xivapi.test

import gg.xp.xivapi.clienttypes.XivApiObject
import gg.xp.xivapi.annotations.XivApiSheet
import groovy.transform.CompileStatic

@CompileStatic
@XivApiSheet("EquipSlotCategory")
interface EquipSlotCategory extends XivApiObject {

	String getName()

	boolean getNIN()

	boolean getWHM()
}
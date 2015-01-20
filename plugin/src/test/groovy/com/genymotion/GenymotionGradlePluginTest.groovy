package test.groovy.com.genymotion

import java.util.List

import main.groovy.com.genymotion.GenymotionEndTask
import main.groovy.com.genymotion.GenymotionLaunchTask
import main.groovy.com.genymotion.GenymotionConfig
import main.groovy.com.genymotion.GMTool
import main.groovy.com.genymotion.GenymotionVirtualDevice
import main.groovy.com.genymotion.GenymotionPluginExtension

import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.gradle.api.Project


import static org.junit.Assert.*
import static org.junit.Assert.assertNotNull

class GenymotionGradlePluginTest {

    Project project

    @BeforeClass
    public static void setUpClass() {
        TestTools.init()
        TestTools.setDefaultUser(true)
    }

    @Before
    public void setUp() {
        project = TestTools.init()
    }


    @Test
    public void canAddsTaskToProject() {
        assertTrue(project.tasks.genymotionLaunch instanceof GenymotionLaunchTask)
        assertTrue(project.tasks.genymotionFinish instanceof GenymotionEndTask)
    }

    @Test
    public void canAddExtensionToProject() {
        assertTrue(project.genymotion instanceof GenymotionPluginExtension)
        assertTrue(project.genymotion.config instanceof GenymotionConfig)
        assertTrue(project.genymotion.devices instanceof List)
    }

    @Test
    public void canConfigGenymotion(){
        String path = "TEST"
        String previousPath = project.genymotion.config.genymotionPath
        project.genymotion.config.genymotionPath = path

        assertEquals(path, project.genymotion.config.genymotionPath)

        project.genymotion.config.genymotionPath = previousPath
    }

    @Test
    public void canAddNoDevice(){

        project.genymotion.devices{}
        assertEquals(0, project.genymotion.devices.size())
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsWhenAddDeviceWithoutNameAndTemplate(){

        project.genymotion.devices {
            "test" {pullAfter "buenos dias"}
        }
        project.genymotion.checkParams()
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsWhenAddDeviceWithNameNotCreated(){

        project.genymotion.devices {
            "DSFGTFSHgfgdfTFGQFQHG"{}
        }
        project.genymotion.checkParams()
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsWhenAddDeviceWithTemplateNotCreated(){

        project.genymotion.devices {
            "test" {
                template "DSFGTFSHgfgdfTFGQFQHG"
            }
        }
        project.genymotion.checkParams()
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsWhenAddDeviceWithNameAndTemplateNotCreated(){

        project.genymotion.devices {
            "DSFGTFSHTFGQFQHG" {
                template "ferrfgfgdshghGFGDFGfgfd"
            }
        }
        project.genymotion.checkParams()
    }

    @Test
    public void throwsWhenAddDeviceWithNameAndTemplateNotCreated2(){

        project.genymotion.devices {
            "DSFGTFSHTFGQFQHG" {
                template "Google Nexus 7 - 4.1.1 - API 16 - 800x1280"
            }
        }
    }

    @Test
    public void canAddDeviceToLaunchByName(){

        String vdName = TestTools.createADevice()

        project.genymotion.devices {
            "$vdName" {}
        }
        assertNull(project.genymotion.devices[0].template)
        assertEquals(vdName, project.genymotion.devices[0].name)

        GMTool.deleteDevice(vdName)
    }

    @Test
    public void canAddDeviceToLaunchByNameWithTemplate(){

        String vdName = TestTools.createADevice()
        String templateName = "Google Nexus 7 - 4.1.1 - API 16 - 800x1280"

        project.genymotion.devices {
            "$vdName" {
                template templateName
            }
        }
        assertEquals(templateName, project.genymotion.devices[0].template)
        assertEquals(vdName, project.genymotion.devices[0].name)

        GMTool.deleteDevice(vdName)
    }


    @Test
    public void canAddDeviceToLaunchByNameWithTemplateNotCreated(){

        String vdName = TestTools.createADevice()

        project.genymotion.devices {
            "$vdName" {
                template "frtfgfdgtgsgrGFGFDGFD"
            }
        }
        project.genymotion.checkParams()

        assertFalse(project.genymotion.devices[0].templateExists)
        assertEquals(vdName, project.genymotion.devices[0].name)

        GMTool.deleteDevice(vdName)
    }

    @Test
    public void canAddDeviceToLaunchByTemplateWithNameNotCreated(){

        project.genymotion.devices {
            "dfsdgffgdgqsdg"{
                template "Google Nexus 7 - 4.1.1 - API 16 - 800x1280"
            }
        }

        project.genymotion.checkParams()

        assertNotNull("No device found", project.genymotion.devices[0])
        assertNotNull("Device not filled", project.genymotion.devices[0].name)
        assertTrue("Device not created", project.genymotion.devices[0].create)
        assertNull(project.genymotion.devices[0].deleteWhenFinish)
    }

    @Test
    public void canAvoidDeviceToBeLaunched(){

        project.genymotion.devices {
            "test" {
                template "Google Nexus 7 - 4.1.1 - API 16 - 800x1280"
                start false
            }
        }

        assertFalse(project.genymotion.devices[0].start)
    }

    @Test
    public void canEditDeviceBeforeLaunch(){

        String vdName = "OKOK-junit"
        def devices = GMTool.getAllDevices(true, false, true)
        if(devices.contains(vdName))
            GMTool.deleteDevice(vdName)

        int intValue = 999
        String densityValue = "mdpi"

        project.genymotion.devices {
            "$vdName" {
                template "Google Nexus 7 - 4.1.1 - API 16 - 800x1280"
                density densityValue
                width intValue
                height intValue
                virtualKeyboard false
                navbarVisible false
                nbCpu 1
                ram 2048
            }
        }

        assertNotNull(project.genymotion.devices[0])
        assertEquals(project.genymotion.devices[0].name, vdName)

        project.genymotion.devices[0].create()
        project.genymotion.devices[0].checkAndEdit()

        GenymotionVirtualDevice device = GMTool.getDevice(vdName, true)
        assertEquals(densityValue, device.density)
        assertEquals(intValue, device.width)
        assertEquals(intValue, device.height)
        assertFalse(device.virtualKeyboard)
        assertFalse(device.navbarVisible)
        assertEquals(1, device.nbCpu)
        assertEquals(2048, device.ram)

        GMTool.deleteDevice(vdName)
    }



    @Test
    public void canSetDeleteWhenFinish(){
        String vdName = TestTools.createADevice()

        project.genymotion.devices {
            "$vdName" {
                deleteWhenFinish true
            }
        }
        project.tasks.genymotionLaunch.exec()
        project.tasks.genymotionFinish.exec()

        assertFalse("The device is still existing", GMTool.isDeviceCreated(vdName, true))
    }

    @Test
    public void canAvoidDeleteWhenFinish(){
        String vdName = TestTools.createADevice()

        project.genymotion.devices {
            "$vdName" {
                deleteWhenFinish false
            }
        }
        project.tasks.genymotionLaunch.exec()
        project.tasks.genymotionFinish.exec()

        assertTrue("The device has been deleted, should still be listed", GMTool.isDeviceCreated(vdName, true))
    }


    @Test
    public void canInstallToDevice() {

        String vdName = TestTools.createADevice()

        project.genymotion.devices {
            "$vdName" {
                install "res/test/test.apk"
            }
        }
        project.tasks.genymotionLaunch.exec()

        boolean installed = false
        GMTool.cmd(["tools/adb", "shell", "pm list packages"], true){line, count ->
            if(line.contains("com.genymotion.test"))
                installed = true
        }
        assertTrue("Install failed", installed)
    }

    @Test
    public void canInstallListOfAppToDevice() {

        String name = TestTools.createADevice()

        def listOfApps = ["res/test/test.apk", "res/test/test2.apk"]
        project.genymotion.devices {
            "$name" {
                install listOfApps
            }
        }
        project.tasks.genymotionLaunch.exec()

        int installed = 0
        GMTool.cmd(["tools/adb", "shell", "pm list packages"], true){line, count ->
            if(line.contains("com.genymotion.test") || line.contains("com.genymotion.test2"))
                installed++
        }
        assertEquals("All apps are not found", listOfApps.size(), installed)
    }



    @Test
    public void canPushBeforeToDevice() {

        String name = TestTools.createADevice()

        project.genymotion.devices{
            "$name" {
                pushBefore "res/test/test.txt"
            }
        }
        project.tasks.genymotionLaunch.exec()

        boolean pushed = false
        GMTool.cmd(["tools/adb", "shell", "ls /sdcard/Download/"], true){line, count ->
            if(line.contains("test.txt"))
                pushed = true
        }
        assertTrue("Push failed", pushed)
    }

    @Test
    public void canPushAfterToDevice() {

        String name = TestTools.createADevice()

        project.genymotion.devices {
            "$name" {
                pushAfter "res/test/test.txt"
                stopWhenFinish false
            }
        }
        project.tasks.genymotionLaunch.exec()

        boolean pushed = false
        GMTool.cmd(["tools/adb", "shell", "ls /sdcard/Download/"], true){line, count ->
            if(line.contains("test.txt"))
                pushed = true
        }
        assertFalse("Push happened but should not happen", pushed)

        project.tasks.genymotionFinish.exec()

        pushed = false
        GMTool.cmd(["tools/adb", "shell", "ls /sdcard/Download/"], true){line, count ->
            if(line.contains("test.txt"))
                pushed = true
        }
        assertTrue("Push failed", pushed)
    }

    @Test
    public void canPushBeforeListToDevice() {

        String name = TestTools.createADevice()

        def listOfFiles = ["res/test/test.txt", "res/test/test2.txt"]
        project.genymotion.devices {
            "$name" {
                pushBefore listOfFiles
            }
        }
        project.tasks.genymotionLaunch.exec()

        int pushed = 0
        GMTool.cmd(["tools/adb", "shell", "ls /sdcard/Download/"], true){line, count ->
            if(line.contains("test.txt") || line.contains("test2.txt"))
                pushed++
        }
        assertEquals("All pushed files are not found", listOfFiles.size(), pushed)
    }

    @Test
    public void canPushAfterListToDevice() {

        String name = TestTools.createADevice()

        def listOfFiles = ["res/test/test.txt", "res/test/test2.txt"]
        project.genymotion.devices {
            "$name" {
                pushAfter listOfFiles
                stopWhenFinish false
            }
        }
        project.tasks.genymotionLaunch.exec()

        int pushed = 0
        GMTool.cmd(["tools/adb", "shell", "ls /sdcard/Download/"], true){line, count ->
            if(line.contains("test.txt") || line.contains("test2.txt"))
                pushed++
        }
        assertEquals("Pushed files, it should not happen", 0, pushed)

        project.tasks.genymotionFinish.exec()

        pushed = 0
        GMTool.cmd(["tools/adb", "shell", "ls /sdcard/Download/"], true){line, count ->
            if(line.contains("test.txt") || line.contains("test2.txt"))
                pushed++
        }
        assertEquals("All pushed files are not found", listOfFiles.size(), pushed)
    }

    @Test
    public void canPushBeforeToDeviceWithDest() {

        String name = TestTools.createADevice()

        def destination = "/sdcard/"
        def listOfFiles = ["res/test/test.txt":destination]
        project.genymotion.devices {
            "$name" {
                pushBefore listOfFiles
            }
        }
        project.tasks.genymotionLaunch.exec()

        boolean pushed = false
        GMTool.cmd(["tools/adb", "shell", "ls", destination], true){line, count ->
            if(line.contains("test.txt"))
                pushed = true
        }
        assertTrue("Push failed", pushed)
    }

    @Test
    public void canPushAfterToDeviceWithDest() {

        String name = TestTools.createADevice()

        def destination = "/sdcard/"
        def listOfFiles = ["res/test/test.txt":destination]
        project.genymotion.devices {
            "$name" {
                pushAfter listOfFiles
                stopWhenFinish false
            }
        }
        project.tasks.genymotionLaunch.exec()

        boolean pushed = false
        GMTool.cmd(["tools/adb", "shell", "ls", destination], true){line, count ->
            if(line.contains("test.txt"))
                pushed = true
        }
        assertFalse("Pushed done. Should not happen", pushed)

        project.tasks.genymotionFinish.exec()

        pushed = false
        GMTool.cmd(["tools/adb", "shell", "ls", destination], true){line, count ->
            if(line.contains("test.txt"))
                pushed = true
        }
        assertTrue("Push failed", pushed)
    }

    @Test
    public void canPushBeforeListToDeviceWithDest() {
        String name = TestTools.createADevice()

        def destination = "/sdcard/"
        def listOfFiles = ["res/test/test.txt":destination, "res/test/test2.txt":destination]
        project.genymotion.devices {
            "$name" {
                pushBefore listOfFiles
            }
        }
        project.tasks.genymotionLaunch.exec()

        int pushed = 0
        GMTool.cmd(["tools/adb", "shell", "ls", destination], true){line, count ->
            if(line.contains("test.txt") || line.contains("test2.txt"))
                pushed++
        }
        assertEquals("All pushed files are not found", listOfFiles.size(), pushed)
    }

    @Test
    public void canPushAfterListToDeviceWithDest() {
        String name = TestTools.createADevice()

        def destination = "/sdcard/"
        def listOfFiles = ["res/test/test.txt":destination, "res/test/test2.txt":destination]
        project.genymotion.devices {
            "$name" {
                pushAfter listOfFiles
                stopWhenFinish false
            }
        }
        project.tasks.genymotionLaunch.exec()


        int pushed = 0
        GMTool.cmd(["tools/adb", "shell", "ls", destination], true){line, count ->
            if(line.contains("test.txt") || line.contains("test2.txt"))
                pushed++
        }
        assertEquals("Pushed done. Should not happen", 0, pushed)

        project.tasks.genymotionFinish.exec()

        pushed = 0
        GMTool.cmd(["tools/adb", "shell", "ls", destination], true){line, count ->
            if(line.contains("test.txt") || line.contains("test2.txt"))
                pushed++
        }
        assertEquals("All pushed files are not found", listOfFiles.size(), pushed)

    }

    @Test
    public void canPullBeforeFromDevice() {
        String name = TestTools.createADevice()

        //removing the pulled files
        TestTools.recreatePulledDirectory()

        project.genymotion.devices {
            "$name" {
                pullBefore "/system/build.prop":"temp/pulled/"
            }
        }
        project.tasks.genymotionLaunch.exec()

        File file = new File("temp/pulled/build.prop")
        assertTrue("Pulled file not found", file.exists())
    }

    @Test
    public void canPullAfterFromDevice() {
        String name = TestTools.createADevice()

        //removing the pulled files
        TestTools.recreatePulledDirectory()

        project.genymotion.devices {
            "$name" {
                pullAfter "/system/build.prop":"temp/pulled/"
                stopWhenFinish false
            }
        }
        project.tasks.genymotionLaunch.exec()

        File file = new File("temp/pulled/build.prop")
        assertFalse("Pulled file found. Should not happen", file.exists())

        project.tasks.genymotionFinish.exec()

        file = new File("temp/pulled/build.prop")
        assertTrue("Pulled file not found", file.exists())
    }

    @Test
    public void canPullBeforeListToDevice() {
        String name = TestTools.createADevice()

        //removing the pulled files
        TestTools.recreatePulledDirectory()

        def listOfFiles = ["/system/build.prop":"temp/pulled/build.prop", "/system/bin/adb":"temp/pulled/adb"]
        project.genymotion.devices {
            "$name" {
                pullBefore listOfFiles
            }
        }
        project.tasks.genymotionLaunch.exec()

        int pushed = 0
        GMTool.cmd(["tools/adb", "shell", "ls /sdcard/Download/"], true){line, count ->
            if(line.contains("test.txt") || line.contains("test2.txt"))
                pushed++
        }

        listOfFiles.each {key, value ->
            File file = new File(value)
            assertTrue("Pulled file not found", file.exists())
        }
    }

    @Test
    public void canPullAfterListToDevice() {
        String name = TestTools.createADevice()

        //removing the pulled files
        TestTools.recreatePulledDirectory()

        def listOfFiles = ["/system/build.prop":"temp/pulled/build.prop", "/system/bin/adb":"temp/pulled/adb"]
        project.genymotion.devices {
            "$name" {
                pullAfter listOfFiles
                stopWhenFinish false
            }
        }
        project.tasks.genymotionLaunch.exec()

        listOfFiles.each {key, value ->
            File file = new File(value)
            assertFalse("Pulled file found. Should not happen", file.exists())
        }

        project.tasks.genymotionFinish.exec()

        listOfFiles.each {key, value ->
            File file = new File(value)
            assertTrue("Pulled file not found", file.exists())
        }
    }


    @Test
    public void canFlashDevice() {
        String name = TestTools.createADevice()

        project.genymotion.devices {
            "$name" {
                flash "res/test/test.zip"
            }
        }
        project.tasks.genymotionLaunch.exec()

        boolean flashed = false
        GMTool.cmd(["tools/adb", "shell", "ls /system"], true){line, count ->
            if(line.contains("touchdown"))
                flashed = true
        }
        assertTrue("Flash failed", flashed)

    }

    @Test
    public void canFlashListToDevice() {
        String name = TestTools.createADevice()

        def exitCode = GMTool.startDevice(name, true)
        assertTrue("Start failed", exitCode == 0)

        def listOfFiles = ["res/test/test.zip", "res/test/test2.zip"]
        project.genymotion.devices {
            "$name" {
                flash listOfFiles
            }
        }
        project.tasks.genymotionLaunch.exec()

        int flashed = 0
        GMTool.cmd(["tools/adb", "shell", "ls /system"], true){line, count ->
            if(line.contains("touchdown") || line.contains("touchdown2"))
                flashed++
        }
        assertEquals("All flashed files are not found", listOfFiles.size(), flashed)
    }

    @After
    public void finishTest(){
        TestTools.cleanAfterTests()
    }
}
/*
 * Copyright 2021 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dueuno.commons.utils.hardware

import dueuno.commons.utils.ProcessUtils
import groovy.transform.CompileStatic
import oshi.SystemInfo
import oshi.hardware.CentralProcessor
import oshi.hardware.GraphicsCard
import oshi.hardware.HardwareAbstractionLayer

/**
 * Utility class to retrieve hardware information of the system.
 * <p>
 * Provides information about CPU, GPU(s), and total RAM.
 * On macOS with Apple Silicon, it also retrieves the number of GPU cores
 * using <code>system_profiler</code>.
 * </p>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * HardwareInfo info = HardwareUtils.getInfo()
 * println "CPU Model: ${info.cpu.model}"
 * println "CPU Architecture: ${info.cpu.architecture}"
 * println "CPU Physical Cores: ${info.cpu.physicalCores}"
 * println "CPU Logical Cores: ${info.cpu.logicalCores}"
 * println "CPU Max Frequency (Hz): ${info.cpu.maxFreqHz}"
 *
 * println "RAM Total (bytes): ${info.ram}"
 *
 * info.gpus.each { gpu ->
 *     println "GPU #${gpu.index} Model: ${gpu.model}"
 *     println "GPU Vendor: ${gpu.vendor}"
 *     println "GPU VRAM: ${gpu.vram} bytes"
 *     if (gpu.cores) {
 *         println "GPU Cores (Apple Silicon): ${gpu.cores}"
 *     }
 * }
 *}</pre>
 */
@CompileStatic
class HardwareUtils {

    /**
     * Collects hardware information about the system, including CPU, GPU(s), and RAM.
     * <p>
     * CPU information includes model, architecture, number of physical and logical cores,
     * and maximum frequency in Hz.
     * GPU information includes model, vendor, VRAM, and for Apple Silicon the number of GPU cores.
     * RAM is returned as total physical memory in bytes.
     * </p>
     *
     * @return a {@link HardwareInfo} object containing CPU, GPU, and RAM information
     *
     * <h3>Usage example:</h3>
     * <pre>{@code
     * HardwareInfo info = HardwareUtils.getInfo()
     * println "CPU Model: ${info.cpu.model}"
     * println "RAM Total: ${info.ram} bytes"
     * info.gpus.each { gpu ->
     *     println "GPU Model: ${gpu.model}, Vendor: ${gpu.vendor}"
     * }
     *}</pre>
     */
    static HardwareInfo getInfo() {

        SystemInfo si = new SystemInfo()
        HardwareAbstractionLayer hw = si.hardware

        def info = new HardwareInfo()

        // ================= CPU =================
        CentralProcessor cpu = hw.processor

        info.cpu = new CpuInfo(
            model: cpu.processorIdentifier.name,
            physicalCores: cpu.physicalProcessorCount,
            logicalCores: cpu.logicalProcessorCount,
            maxFreqHz: cpu.maxFreq,
            architecture: System.getProperty("os.arch"),
        )

        // ================= GPU =================
        Integer appleGpuCores = getAppleGpuCores()
        List<GraphicsCard> cards = hw.graphicsCards

        Integer index = 0
        for (card in cards) {
            GpuInfo gpu = new GpuInfo(
                index: index,
                model: card.name,
                vendor: card.vendor,
                vram: card.getVRam(),
            )

            if (appleGpuCores && card.vendor?.toLowerCase()?.contains('apple')) {
                gpu.cores = appleGpuCores
            }

            info.gpus << gpu
            index++
        }

        info.gpuCount = info.gpus.size()
        info.ram = si.hardware.memory.total

        return info
    }

    /**
     * Retrieves the number of GPU cores on Apple Silicon systems by
     * executing the <code>system_profiler SPDisplaysDataType</code> command.
     * <p>
     * This method returns null on non-macOS platforms.
     * </p>
     *
     * @return the number of GPU cores on Apple Silicon, or null if not available
     *
     * <h3>Usage example:</h3>
     * <pre>{@code
     * Integer appleGpuCores = HardwareUtils.getAppleGpuCores()
     * if (appleGpuCores != null) {
     *     println "Apple GPU cores: ${appleGpuCores}"
     * }
     *}</pre>
     */
    private static Integer getAppleGpuCores() {
        String os = System.getProperty('os.name')?.toLowerCase()
        if (!os?.contains('mac')) {
            return null
        }

        Integer cores
        try {
            ProcessUtils.execute("system_profiler SPDisplaysDataType") { String it ->
                List line = it.split(':')*.trim()
                if (line[0] == 'Total Number of Cores') {
                    cores = line[1] as Integer
                }
            }
        } catch (Exception ignore) {
            // no-op
        }

        return cores
    }
}
